--require("mobdebug").start()
require 'nn'
require 'torch'
require 'map_data'
local cjson = require 'cjson'

local ltfname = "/home/et/IdeaProjects/POSTagger/res/lookuptable"
--local embnet = torch.load(fname)
--local lookuptable = embnet:get(1):get(1).weight
local lookuptable = torch.load(ltfname)
--lookuptable = lookuptable:float() --move back to cpu, you do not need them on gpu, this is a small calculation
--torch.save("../res/lookuptable",lookuptable)
dictsize = (#lookuptable)[1]
featuresize = (#lookuptable)[2]



function getRank(idx)
  local target = lookuptable[idx]:reshape(1,featuresize):expand(dictsize,featuresize)
  local dist = (lookuptable - target):pow(2):sum(2)
  return torch.sort(dist,1)
end

function friendlyGetRank(str,max)
  max = max or 10
  local mark, idx = getRank(str2idx(str))
  local ret = {}
  for i = 1,max do
    table.insert(ret,{idx2str(idx[i][1]),mark[i][1]})
  end
  return ret
  
end

t = friendlyGetRank("pain",50)

for _,v in pairs(t) do
  print(v[1] ..": ".. v[2])
end
