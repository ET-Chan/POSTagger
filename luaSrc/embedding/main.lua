--require("mobdebug").start()

require 'wiki_data'


create_model = require 'create_model'
train = require 'train'



local opt = {
  dictsize = 200000 + 2 + 1, -- ONE FOR PADDING, TWO FOR RARE and RARENUMBER
  paddingidx = 200000 + 3,
  wdvecdim = 50,
  contextsize = 11,
  midcontext = 6,
  hu1sz = 100, 
  margin = 1,
  batchsize = 3000,
  maxepoch = 1,
  saveInterval = 5e2,
  profiling = false,
  profiling_its = 10,
  cuda = true,
  cputhread = 4,
  timinginterval = 1e10,
  filefilter = "wiki",
  
}

torch.setnumthreads(opt.cputhread)

local model, criterion = create_model(opt)


local params, grads = model:getParameters()

local wiki = wiki_data.new("../../res/wiki/idxCorpus/",opt)
local counter = 0

local filename = ("../../res/nnembd.net")
local optimfilename = ("../../res/optim")

local f = io.open(optimfilename,"r")
if f~=nil then
  f:close()
  optim_state = torch.load(optimfilename)
  print ("Optim parms loaded")
end
  
sys.tic()

for _ = 1,opt.maxepoch do
  repeat
    counter = counter + 1
    local data,corrupt_data,epochpass = wiki:read(opt)
    local _,avgloss,avgafterloss = train(opt,data,corrupt_data,model,criterion,params,grads)

    print ("Mini batch passed:" .. counter .. " Losses: " .. avgloss .." Afterloss: " .. avgafterloss)
    if counter % opt.saveInterval == 0 then
      print ("Model snapshot saved!")
      torch.save(filename,model)
      torch.save(optimfilename,optim_state)
    end
    
    collectgarbage()
    
    if opt.profiling then
      if counter > opt.profiling_its then
        break
      end
    end
    
    if counter % opt.timinginterval == 0 then
        print("Time used for " .. opt.timinginterval .. " minibatches: " .. sys.toc())
        sys.tic()
    end
    
  until epochpass
  print("Finished a epoch!")
end

torch.save(filename,model)
torch.save(optimfilename,optim_state)


