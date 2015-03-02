require 'torch'
require 'optim'

local function train(opt,data,target,model,criterion,params,grads)
  --DATA SHOULD SENTENCE INDICIES TABLE
  --TARGET SHOULD BE ITS COORESPONDING TAGS
  local feval = function(x)
    if x ~= params then
      params:copy(x)
    end
    grads:zero()
    
    --CANNOT HANDLE WITH VARIED LENGTH OF SENTENCE WITH MATRIX, DAMNED
    local loss = 0
    local counter = 0
    for i = 1,#data do
      local line = torch.Tensor(data[i])
      local tag = torch.Tensor(target[i])
      if (opt.cuda) then
        line = line:cuda()
        tag = tag:cuda()
      end
      local outputs = model:forward(line)
      loss = loss + (criterion:forward(outputs,tag)) --each word avg loss
      counter = counter + (#line)[1]
      local dloss_doutput = criterion:backward(outputs,tag)
      model:backward(line,dloss_doutput)
    end
    
    return loss/counter,grads
  end
  
  
  optim_state = optim_state or {}
  local _,loss = optim.adam(feval,params,optim_state)
  
  --the loss is already been averaged
  return model,loss[1]
  
end

return train
