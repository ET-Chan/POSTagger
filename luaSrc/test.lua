require 'nn'

require('mobdebug').start()

p1_mlp= nn.Linear(5,2)
p2_mlp= p1_mlp:clone('weight','bias')

prl=nn.ParallelTable()
prl:add(p1_mlp)
prl:add(p2_mlp)

mlp1=nn.Sequential()
mlp1:add(prl)
mlp1:add(nn.DotProduct())

mlp2=mlp1:clone('weight','bias')

mlpa=nn.Sequential()
prla=nn.ParallelTable()
prla:add(mlp1)
prla:add(mlp2)
mlpa:add(prla)

crit=nn.MarginRankingCriterion(0.1)

x=torch.randn(5)
y=torch.randn(5)
z=torch.randn(5)


-- Use a typical generic gradient update function
function gradUpdate(mlp, x, y, criterion, learningRate)
 local pred = mlp:forward(x)
 local err = criterion:forward(pred, y)
 local gradCriterion = criterion:backward(pred, y)
 mlp:zeroGradParameters()
 mlp:backward(x, gradCriterion)
 mlp:updateParameters(learningRate)
end

for i=1,100 do
 gradUpdate(mlpa,{{x,y},{x,z}},1,crit,0.01)
 if true then 
      o1=mlp1:forward{x,y}[1]; 
      o2=mlp2:forward{x,z}[1]; 
      o=crit:forward(mlpa:forward{{x,y},{x,z}},1)
      print(o1,o2,o)
  end
end

print "--"

for i=1,100 do
 gradUpdate(mlpa,{{x,y},{x,z}},-1,crit,0.01)
 if true then 
      o1=mlp1:forward{x,y}[1]; 
      o2=mlp2:forward{x,z}[1]; 
      o=crit:forward(mlpa:forward{{x,y},{x,z}},-1)
      print(o1,o2,o)
  end
end