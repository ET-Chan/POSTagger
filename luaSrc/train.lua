require 'torch'
require 'cutorch'
require 'optim'



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
--    local outputs = model:forward(data):clone()
 --   local corrupt_output = model:forward(corrupt_data)
    local outputs = model:forward({data,corrupt_data})
    --print("Correct data pred: ".. outputs .. "Corrupt_data_outputs" .. corrupt_output)
    
    local loss = criterion:forward(outputs,1)
    local dloss_doutput = criterion:backward(outputs,1)

    --investigate another method to do this cleanly
    model:backward({data,corrupt_data},dloss_doutput)
--    model:backward(data,dloss_doutput[1])
 --   model:backward(corrupt_data,dloss_doutput[2])
    return loss,grads
    
  end
 -- local prev_params = params:clone()
  optim_state = optim_state or {learningRate = opt.learningRate}
  local _,loss = optim.adagrad(feval,params,optim_state)
  loss = loss[1]:float()
  local afterloss = criterion:forward(model:forward{data,corrupt_data},1)
  return model,loss,afterloss
  
  
end

return train