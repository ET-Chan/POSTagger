require 'fbcunn'

function create_model(opt)
  
-- MODEL:
-- LOOKUP TABLE -> CONV -> TANH -> CONV -> SOFTMAX
  local filename = ("../../res/nnpos.net")
  local f = io.open(filename,"r")
  local mlp = nil
  if f~=nil then
    f:close()
    mlp = torch.load(filename)
    print("Model loaded")
  else
    local dictsize = opt.dictsize
    local wdvecdim = opt.wdvecdim
    local contextsz = opt.contextsize
    local hu1sz = opt.hu1sz
    local margin = opt.margin
    local tagsize = opt.tagsize
    local ltw = nn.LookupTableGPU(dictsize,wdvecdim)
    --local ltw = nn.LookupTable(dictsize,wdvecdim)
   -- local oltwsz = contextsz * wdvecdim
   -- local rs = nn.Reshape(oltwsz)
    --First stage, LookupTable
    mlp = nn.Sequential()
    mlp:add(ltw)
    -- L * wdvecdim
    --Second stage, convolution
    local conv1 = nn.TemporalConvolution(wdvecdim,hu1sz,contextsz)
    local hth = nn.HardTanh()
    
    --Third stage, to tags
    local conv2 = nn.TemporalConvolution(hu1sz,tagsize,1) -- WINDOWS SZ IS ONLY ONE.
    local lsm = nn.LogSoftMax()
    --final output of the model is (B) * L * hu2sz
    mlp:add(conv1)
    mlp:add(hth)
    mlp:add(conv2)
    mlp:add(lsm)
    --final output
    --init with random weights
    init_model(mlp)
    --now, the lookuptable will be initialized with embedding
    local ltfname = "/home/et/IdeaProjects/POSTagger/res/lookuptable"
    local ltWeights = torch.load(ltfname)
    ltw.weight:copy(ltWeights)
  end
  local params,grad = mlp:getParameters()
  
  local criterion = nn.ClassNLLCriterion()
  --move everything to gpu
  
  if opt.cuda then
    mlp:cuda()
    criterion:cuda()
  end
  
  return mlp,criterion

end

function init_model(model)
  local params,_ = model:getParameters()
  params:uniform(-0.01,0.01)
  return model
end

return create_model