--equire("mobdebug").start()

require 'wsj_reader'

create_model = require 'create_model'
train_model = require 'train'
test_model = require 'test'


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

local opt = {
   dictsize = 200000 + 2 + 1, -- ONE FOR PADDING, TWO FOR RARE and RARENUMBER
  paddingidx = 200000 + 3,
  wdvecdim = 50,
  contextsize = 5,
  hu1sz = 300,
  tagsize = 46 + 1,
  paddingtagidx = 47,
  pad_while_read = true,
  ratio = 0.9,
  batchsize = 100,
  learningRate = 1e-1,
  maxepoch = 10,
  saveInterval = 5e2,
  cuda = true , -- IN THIS PROBLEM, I DOUBT IF IT WILL BE FASTER
  cputhread = 4,
  timinginterval = 1e10,
  profiling = false,
  profiling_its = 10,
}


torch.setnumthreads(opt.cputhread)


local model,criterion = create_model(opt)


  local params, grads = model:getParameters()
  

local optimfilename = ("../../res/posoptim")
local f = io.open(optimfilename,"r")
if f~=nil then
  f:close()
  optim_state = torch.load(optimfilename)
  print ("Optim parms loaded")
end
  
sys.tic()

local trainp = 1
local testp = 1

local wsj_reader = wsj_reader.new("../../res/outcorplua")
wsj_reader:load(opt)

--NO CHEATING.
local trainD,testD = wsj_reader:sample(opt)

for _ = 1,opt.maxepoch do
 -- local train,test = wsj_reader:sample(opt) <- THIS IS CHEATING
 local train = deepcopy(trainD)
 local test = deepcopy(testD)
  local its = torch.ceil(#train.sentences / opt.batchsize)
  local test_batchsz = #test.sentences / its
  
  for _ = 1,#train.sentences,opt.batchsize do
    local train_mini_words = {}
    local train_mini_tags = {}
    local test_mini_words = {}
     local test_mini_tags = {}
     
    local jm = math.min(opt.batchsize, #train.sentences)
    for j = 1,jm do 
      --In this stage, no captitals considered.
      --Should be added later on
      table.insert(train_mini_words,table.remove(train.sentences))
      table.insert(train_mini_tags,table.remove(train.tags))
    end
    
    for j = 1,test_batchsz do
     table.insert(test_mini_words,table.remove(test.sentences))
      table.insert(test_mini_tags,table.remove(test.tags))
    end
    
    local _,trainloss = train_model(opt,train_mini_words,train_mini_tags,model,criterion,params,grads)
    local _,testloss,misclasserr = test_model(opt,test_mini_words,test_mini_tags,model,criterion)
    
    print("Train loss: "..trainloss .. " ,Test loss: " .. testloss .." Mis' Error: " .. misclasserr .. " Left: ".. #train.sentences)
  end
  
end



