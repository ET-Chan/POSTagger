require 'lfs'

wiki_data = {}

wiki_data.__index = wiki_data

function wiki_data.new(path)
  local self = setmetatable({},wiki_data)
  self.path = path
  self:resolve_paths()
  self.fileCounter = 1
  self.file = nil
  return self
end


function wiki_data:resolve_paths()
    local path = self.path
    self.paths = {}
    for filename in lfs.dir(path) do
      if filename:match("%.txt$") then
        table.insert(self.paths,filename)
      end
    end
end

function wiki_data:read(opt)
  local epochpass = false
  if(self.file) == nil then
    local fullpath = self.path .. "/" .. self.paths[self.fileCounter]
    self.file = io.open(fullpath,"r")
    self.fileCounter = self.fileCounter + 1
    if self.fileCounter > (#self.paths) then
      self.fileCounter = 1
      epochpass = true
    end
    
  end
  local f = self.file
  local paddingidx = opt.paddingidx
  local batchsz = opt.batchsize
  local contextsize = opt.contextsize
  local midcontext = opt.midcontext
  local dictsize = opt.dictsize
  
  --read batchsize line and foreach line, generate contextsize indices vector
  local tokenarr = {}
  for i=1,batchsz do
    local line = f:read("*l")
    if line == nil then
      self.file = nil
      break
    end
    local fullarr = string.split(line,",")
    while(#fullarr) < contextsize do
      table.insert(fullarr,paddingidx)
    end
    
    local winarr = {0}
    for j = 1,contextsize - 1 do
      table.insert(winarr, fullarr[j])
    end
    
    for j = contextsize,(#fullarr) do
      table.remove(winarr,1)
      table.insert(winarr,fullarr[j])
      local winarrclone = deepcopy(winarr)
      table.insert(tokenarr,winarrclone)
    end
    
    
  end
  local correct_data = torch.Tensor(tokenarr)
  local corrupt_data = correct_data:clone()
  
  
  --Using round will lead to appearance of zero idx, which we want to avoid.
  corrupt_data[{{},{midcontext}}] = (torch.rand((#corrupt_data)[1]) * dictsize):ceil()
  if(opt.cuda) then
    correct_data = correct_data:cuda()
    corrupt_data = corrupt_data:cuda()
  end
  
  return correct_data,corrupt_data,epochpass
  
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

function deepcopy(orig)
    local orig_type = type(orig)
    local copy
    if orig_type == 'table' then
        copy = {}
        for orig_key, orig_value in next, orig, nil do
            copy[deepcopy(orig_key)] = deepcopy(orig_value)
        end
        setmetatable(copy, deepcopy(getmetatable(orig)))
    else -- number, string, boolean, etc
        copy = orig
    end
    return copy
end