local cjson = require "cjson"

--require('mobdebug').start()

DictReader = {}

DictReader.__index = DictReader

function DictReader.read(path)
  
  --return a table with index
  local file = io.open(path,r)
  local text = file:read("*all")
  local value = cjson.decode(text)
  file:close()
  return value
end

