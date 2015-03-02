require 'torch'


local function test(opt,testdata,testtag,model,criterion)
  
  local loss = 0
  local counter = 0
  local misclasserr = 0
  for i = 1,#testdata do
    local line = torch.Tensor(testdata[i])
    local tag = torch.Tensor(testtag[i])
    if (opt.cuda) then
      require 'cutorch'
      line = line:cuda()
      tag = tag:cuda()
    end
    local outputs = model:forward(line)
    loss = loss + (criterion:forward(outputs,tag)) --each word avg loss
    counter = counter + (#line)[1]
    
    local _,prediction = outputs:max(2)
    prediction = prediction:type('torch.ByteTensor')
    tag = tag:type('torch.ByteTensor')
    misclasserr = misclasserr + (prediction:ne(tag):sum(1):float()/(#tag)[1])[1][1] -- each sentence classerr
  end
  
  
  return model,loss/counter,misclasserr/(#testdata)
end


return test
