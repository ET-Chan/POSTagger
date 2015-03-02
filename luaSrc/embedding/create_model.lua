require 'cunn'
require 'fbcunn'

function create_model(opt)
-- MODEL:
-- LOOKUP TABLE -> LINEAR -> TANH -> LINEAR
-- NOTICE THIS WILL ONLY CREATE ONE MODEL
-- USING THE RANKCRITERION NEEDS TO DEFINE TWO MODEL
-- BEST WAY TO DO IT IS TO MIRRORING THE PARAMETER OF THE FIRST ONE

  local filename = ("../../res/nnembd.net")

  local mlppt = {}
  local mlp = {}
  local mlpmirror = {}
  
  local f = io.open(filename,"r")
  if f~=nil then
    f:close()
    mlppt = torch.load(filename)
    mlp = mlppt:get(1)
    mlpmirror = mlppt:get(2)
    print ("Model loaded")
  else
    local dictsize = opt.dictsize
    local wdvecdim = opt.wdvecdim
    local contextsz = opt.contextsize
    local hu1sz = opt.hu1sz
    local margin = opt.margin
    
   -- local model = nn.Sequential()
    --LookupTable automatically scales to multiple indices
    local ltw = nn.LookupTableGPU(dictsize,wdvecdim)
    --the output of this layer should be dictsize*contextsize
    local oltwsz = contextsz * wdvecdim
    local rs = nn.Reshape(oltwsz)
    
    mlp = nn.Sequential()
    mlp:add(ltw)
    mlp:add(rs)
    
    local ll1 = nn.Linear(oltwsz,hu1sz)
    local hth = nn.HardTanh()
    local ll2 = nn.Linear(hu1sz,1)
    
    mlp:add(ll1)
    mlp:add(hth)
    mlp:add(ll2)
    
      --here, in order to adapt to the criterion, we make a parallel table, loading with the same set of parameteres, notice that, it does not share the output, which is critical.
    mlpmirror = mlp:clone('weight','bias','gradWeight','gradBias')
    
    mlppt = nn.ParallelTable()
    mlppt:add(mlp)
    mlppt:add(mlpmirror)
    init_model(mlppt)
end

  local criterion = nn.MarginRankingCriterion(margin)

  
  if opt.cuda then
    mlppt:cuda()
    criterion:cuda()
    criterion.gradInput[1] = criterion.gradInput[1]:cuda()
    criterion.gradInput[2] = criterion.gradInput[2]:cuda()
    mlpmirror:share(mlp,'weight','bias','gradWeight','gradBias')
  end
  
  
  
  return mlppt,criterion
end



function init_model(model)
  local params,_ = model:getParameters()
  params:uniform(-0.01,0.01)
  return model
end


return create_model