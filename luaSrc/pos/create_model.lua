require 'fbcunn'

function create_model(opt)
  
-- MODEL:
-- LOOKUP TABLE -> CONV -> TANH -> CONV -> SOFTMAX


--               ->LOOKUP TABLE WORDS -> 
-- SPLITTABLE ->                          -> JOINTABLE->CONV ->TANH->CONV->SOFTMAX
--               ->LOOKUP TABLE CAPTS ->

-- THIS NETWORK ACCEPTS NO MINIBATCH.

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
    local captsize = opt.capitals_lookuptablesz
    
    local ltw = nn.LookupTableGPU(dictsize,wdvecdim)
    
    local cltw = nn.LookupTableGPU(captsize,captsize) -- SAME, I DONT COMPRESS INFORMATION
    --local ltw = nn.LookupTable(dictsize,wdvecdim)
   -- local oltwsz = contextsz * wdvecdim
   -- local rs = nn.Reshape(oltwsz)
    --First stage, LookupTable
    local combltw = nn.ParallelTable()
    local splittw = nn.SplitTable(1)--SPLIT ALONG WITH FIRST DIMENSION,NOTICE THAT, THE INPUT  SHOULD BE TWO DIMENSIONAL TENSORS
    local jointtw = nn.JoinTable(2)
    
    combltw:add(ltw)
    combltw:add(cltw)
    mlp = nn.Sequential()
    --mlp:add(ltw)
    
    -- L * (1 + 1)
    mlp:add(splittw)
    -- {L*1, L*1}
    mlp:add(combltw)
    -- {L* (wdvecdim), L* (captsz)}
    mlp:add(jointtw)
    -- L * (wdvecdim + captsz)
    
    --Second stage, convolution
    local conv1 = nn.TemporalConvolution(wdvecdim + captsize,hu1sz,contextsz)
    local hth = nn.HardTanh()
    --L* (hu1sz * kd)
    
    --Third stage, to tags
    local conv2 = nn.TemporalConvolution(hu1sz,tagsize,1) -- WINDOWS SZ IS ONLY ONE.
    local lsm = nn.LogSoftMax()
    --final output of the model is L * tagsize
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
    
  --disable first layer bp, it is useless, and trigger error
    splittw.updateGradInput = function () end
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