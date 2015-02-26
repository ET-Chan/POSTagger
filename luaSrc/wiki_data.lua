wiki_data = {}

wiki_data.__index = wiki_data

function wiki_data.new(path)
  local self = setmetatable({},wiki_data)
  self.path = path
  self.fileCounter = 1
  self.file = nil
  return self
end


function wiki_data:read(opt)
  if(self.file) = nil
    self.file = io.read(self.path[self.fileCounter],"r")
    self.fileCounter = self.fileCounter + 1
    if(self.fileCounter > path.size)
      self.fileCounter = 1
  end
  local paddingidx = opt.paddingidx
  local batchsz = opt.batchsize
  local contextsz = opt.contextsize
  local midcontext = opt.midcontext
  
  --read batchsize line and foreach line, generate contextsize indices vector
  local tokenarr = {}
  for i=1,batchsz
    local line = f:read("*l")
    if(line = nil)
      self.file = nil
      break
    end
    local fullarr = string.split(line,",")
    while(#arr) < contextsize do
      table.insert(arr,paddingidx)
    end
    local winarr = {0}
    for j = 1,contextsize - 1
      table.insert(winarr, fullarr[j])
    end
    
    for j = contextsize,(#arr)
      table.remove(arr,1)
      table.insert(arr,fullarr[j])
      table.insert(tokenarr,arr)
    end
    
    
  end
  local correct_data = torch.Tensor(tokenarr)
  local corrupt_data = correct_data:clone()
  
  
  --Using round will lead to appearance of zero idx, which we want to avoid.
  corrupt_data[{{},{midcontext}}] = (torch.rand((#corrupt_data)[1]) * dictsize):ceil()
  return correct_data,corrupt_data
  
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

