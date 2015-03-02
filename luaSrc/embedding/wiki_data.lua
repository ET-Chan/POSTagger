--require("mobdebug").start()
require 'torch'

require 'lfs'

wiki_data = {}

wiki_data.__index = wiki_data

function wiki_data.new(path,opt)
  local self = setmetatable({},wiki_data)
  self.path = path
  self:resolve_paths(opt)
  self.fileCounter = 0
  self.file = nil
  return self
end


function wiki_data:resolve_paths(opt)
    local path = self.path
    self.paths = {}
    for filename in lfs.dir(path) do
      if(string.find(filename,opt.filefilter)) then
        table.insert(self.paths,filename)
      end
    end
end

function wiki_data:read(opt)
  local epochpass = false
  
  local f = self.file
  local paddingidx = tostring(opt.paddingidx)
  local batchsz = opt.batchsize
  local contextsize = opt.contextsize
  local midcontext = opt.midcontext
  local dictsize = opt.dictsize
  
  --read batchsize line and foreach line, generate contextsize indices vector
  local tokenarr = {}
  --local corruptindicies = {}
  local shortindicies = {}
  for i=1,batchsz do
    local line = nil
    if f then  
      line = f:read("*l")
    end
    
    if line == nil then
      if self.fileCounter == #self.paths then
        epochpass = true
        break
      end
      if(self.fileCounter ~= 0) then
        os.remove(self.path .. "/" .. self.paths[self.fileCounter])
      end
      self.fileCounter = self.fileCounter + 1
      
      local fullpath = self.path .. "/" .. self.paths[self.fileCounter]
      self.file = io.open(fullpath,"r")
      f = self.file
      line = f:read("*l") --Do not give me an empty text, I will punch you.
    end
    local fullarr = string.split(line,",")
    local shortsentence = false
    local corruptidx
    while(#fullarr) < contextsize do
      shortsentence = true
      corruptidx = torch.ceil((#fullarr)/2)
      table.insert(fullarr,paddingidx)
    end
    
    if shortsentence then
      table.insert(shortindicies,{(#tokenarr) + 1,fullarr[midcontext],corruptidx})
    end
    
    
    local winarr = {0}
    for j = 1,contextsize - 1 do
      table.insert(winarr, fullarr[j])
    end
    
    for j = contextsize,(#fullarr) do
      --table.insert(corruptindicies,corruptidx)
      --corruptidx = midcontext
      table.remove(winarr,1)
      table.insert(winarr,fullarr[j])
      local winarrclone = deepcopy(winarr)
      table.insert(tokenarr,winarrclone)
    --  local wincorruptarrclone = deepcopy(winarr)
    --  wincorruptarrclone[corruptidx] = tostring((torch.rand(1)*dictsize):ceil()[1])
    --  table.insert(corrupttokenarr,wincorruptarrclone)
    end
    
    
  end
  local correct_data = nil
  local corrupt_data = nil
  if #tokenarr == 0 then
    correct_data = torch.DoubleTensor()
    corrupt_data = torch.DoubleTensor()
  else
  
    correct_data = torch.Tensor(tokenarr)
    corrupt_data = correct_data:clone()
 --   corrupt_data[{{},corruptindicies}] = (torch.rand((#corrupt_data)[1]) * dictsize):ceil()
   -- corruptindicies = torch.Tensor(corruptindicies)
    --corrupt_data = torch.Tensor(corrupttokenarr)
    
    --Using round will lead to appearance of zero idx, which we want to avoid.
   corrupt_data[{{},{midcontext}}] = (torch.rand((#corrupt_data)[1]) * dictsize):ceil()
    
    for _,v in pairs(shortindicies) do
      corrupt_data[v[1]][midcontext] = v[2]
      corrupt_data[v[1]][v[3]] = (torch.rand(1)*dictsize):ceil()[1]
    end
    
    
    
  end
  if(opt.cuda) then
    require 'cutorch' --just in case
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


