--require("mobdebug").start()
require 'torch'

wsj_reader = {}

wsj_reader.__index = wsj_reader

function wsj_reader.new(path)
  local self = setmetatable({},wsj_reader)
  self.path = path
  return self
end

function wsj_reader:load(opt)
  local lines = io.lines(self.path)
  self.sentences = {}
  self.tags = {}
  self.capitals = {}
  for line in lines do
    local words = line:split(",")
    local tags = lines():split(",")
    local capts = lines():split(",")
    
    assert(#words == #tags)
    assert(#words == #capts)
    --PADDING, if on
    if(opt.pad_while_read) then
      local padsz = torch.floor(opt.contextsize / 2)
      for i = 1,padsz do
        table.insert(words,opt.paddingidx)
        table.insert(words,1,opt.paddingidx)
    --    table.insert(tags,opt.paddingtagidx)
    --    table.insert(tags,1,opt.paddingtagidx) -- NO NEED FOR TAG...
        table.insert(capts,0)
        table.insert(capts,1,0)
      end
      
    end
    
    
    table.insert(self.sentences, words)
    table.insert(self.tags,tags)
    table.insert(self.capitals,capts)
  end
end

function wsj_reader:sample(opt)
  opt = opt or {ratio = 0.9}
  local ratio = opt.ratio
  local size = #self.sentences
  
  local trans = size*ratio
  local perm = torch.randperm(size)
  
  local train_set = {}
  local test_set = {}
  
  train_set.sentences = {}
  train_set.tags = {}
  train_set.capitals = {}
  
  test_set.sentences = {}
  test_set.tags = {}
  test_set.capitals = {}
  
  local target_set = train_set
  
  for i=1,size do
    if (i>trans) then
      target_set = test_set
    end
    table.insert(target_set.sentences,self.sentences[perm[i]])
    table.insert(target_set.tags,self.tags[perm[i]])
    table.insert(target_set.capitals,self.capitals[perm[i]])
  end
  
  
  return train_set,test_set
  
end



function string:split(delimiter)
  local result = { }
  local from  = 1
  local delim_from, delim_to = string.find( self, delimiter, from  )
  while delim_from do
    table.insert( result, string.sub( self, from , delim_from-1 ) )
    from  = delim_to + 1
    delim_from, delim_to = string.find( self, delimiter, from  )
  end
  table.insert( result, string.sub( self, from  ) )
  return result
end


--local wr = wsj_reader.new("../../res/outcorpulua")
--wr:load()
--local ts,ds = wr:sample()

--for i = 1,1 do 
 -- print(ts.sentences[i])
  --print(ts.tags[i])
  --print(ts.capitals[i])
--end

--print("===================================================")
--for i = 1,1 do 
  
--print(ds.sentences[i])
--  print(ds.tags[i])
--  print(ds.capitals[i])
--end

