require 'torch'
require 'optim'


create_model = require 'create_model'

local function train(opt,data,corrupt_data,model,criterion)
--every train will only train the model once with these batches.

--data and corrupt_data should be batchsz*contextsz thing

  local params, grads = model:getParameters()
  
--this could be changed by load up last trained params
  
  
  
  local feval = function(x)
    if x ~= params then
      params:copy(x)
    end
    grads:zero()
    --optimizable
    local outputs = model:forward(data)
    local corrupt_output = model:forward(corrupt_data)
    local loss = criterion:forward({outputs,corrupt_output},1)
    local dloss_doutput = criterion:backward({outputs,corrupt_output},1)
    --investigate another method to do this cleanly
    model:backward(data,dloss_doutput[1])
    model:backward(data,dloss_doutput[2])
    
    return loss,grads
    
  end

  local optim_state = {learningRate = opt.learningRate}
  local _,loss = optim.adagrad(feval,params,optim_state)
  return model,loss
  
  
end

  