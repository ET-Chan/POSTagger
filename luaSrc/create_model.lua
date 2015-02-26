require 'nn'

function create_model(opt)
-- MODEL:
-- LOOKUP TABLE -> LINEAR -> TANH -> LINEAR
-- NOTICE THIS WILL ONLY CREATE ONE MODEL
-- USING THE RANKCRITERION NEEDS TO DEFINE TWO MODEL
-- BEST WAY TO DO IT IS TO MIRRORING THE PARAMETER OF THE FIRST ONE

  local dictsize = opt.dictsize
  local wdvecdim = opt.wdvecdim
  local contextsz = opt.contextsize
  local hu1sz = opt.hu1sz
  local margin = opt.margin
  
  local model = nn.Sequential()
  --LookupTable automatically scales to multiple indices
  local ltw = nn.LookupTable(dictsize,wdvecdim)
  --the output of this layer should be dictsize*contextsize
  local oltwsz = contextsz * wdvecdim
  local rs = nn.Reshape(oltwsz)
  
  local mlp = nn.Sequential()
  mlp:add(ltw)
  mlp:add(rs)
  
  local ll1 = nn.Linear(contextsz,hu1sz)
  local hth = nn.HardTanh()
  local ll2 = nn.Linear(hu1sz,1)
  
  mlp:add(ll1)
  mlp:add(hth)
  mlp:add(ll2)
  
  local criterion = nn.MarginRankingCriterion(margin)
  return init_model(model),criterion
end

return create_model


function init_model(model)
  local params,grads = model:getParameters()
  params:uniform(-0.01,0.01)
end

