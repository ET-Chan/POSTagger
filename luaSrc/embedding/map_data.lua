local cjson = require "cjson"

--require('mobdebug').start()


function readjson(path)
  
  --return a table with index
  local file = io.open(path,r)
  local text = file:read("*all")
  local value = cjson.decode(text)
  file:close()
  return value
end

local mapname = "../../res/wordIdx.json"
local rmapname = "../../res/rWordIdx.json"

local map = readjson(mapname)
local rmap = readjson(rmapname)

local RAREIDX = 200001

--i really hate to reimplement things
--but finding how to call lua code from java
-- result in nothing. FUXK
function idx2str(idx)
  idx = tostring(idx)
  if(rmap[idx]==nil) then
    print("Not found in dictionary!")
  end
  
  return rmap[idx]
end

function str2idx(str)
  str = string.lower(str)
  if(map[str] == nil) then
    return RAREIDX
  else
    return map[str]
  end
  
end
