--this is to interfacing with scala
--jnlua is totally crap, hence I gave up using it
--just use old tranditional piping would be suffice

require 'torch'

if((#arg) == 0) then
  print("You must enter an argument for selecting interface")
  return -1
end

local t = {
  rank = require 'rank',
  
}



local f = t[arg[1]]
if(f==nil) then
  print("Unknown interface, quit!")
  return -1
end

while true do
  local input = io.read()
  io.write(f(input))
end
