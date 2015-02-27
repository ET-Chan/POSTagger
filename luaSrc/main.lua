require("mobdebug").start()

require 'wiki_data'


create_model = require 'create_model'
train = require 'train'



local opt = {
  dictsize = 200000 + 2 + 1, -- ONE FOR PADDING, TWO FOR RARE and RARENUMBER
  paddingidx = 200000 + 3,
  wdvecdim = 50,
  contextsize = 5,
  midcontext = 3,
  hu1sz = 400, 
  margin = 1,
  batchsize = 1000,
  learningRate = 1e-1,
  maxepoch = 1,
  saveInterval = 5e2,
  profiling = false,
  profiling_its = 10,
  cuda = true,
  cputhread = 4,
  timinginterval = 1e10,
}

torch.setnumthreads(opt.cputhread)

local model, criterion = create_model(opt)

local wiki = wiki_data.new("../res/wiki/idxCorpus/")
local counter = 0

local filename = ("../res/nnembd.net")


sys.tic()

for _ = 1,opt.maxepoch do
  repeat
    counter = counter + 1
    local data,corrupt_data,epochpass = wiki:read(opt)
    local _,loss,afterloss = train(opt,data,corrupt_data,model,criterion)
    local avgloss = loss:sum()/(#loss)[1]
    local avgafterloss = afterloss:sum()/(#afterloss)[1]
    print ("Mini batch passed:" .. counter .. " Losses: " .. avgloss .." Afterloss: " .. avgafterloss)
    if counter % opt.saveInterval == 0 then
      print ("Model snapshot saved!")
      torch.save(filename,model)
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



