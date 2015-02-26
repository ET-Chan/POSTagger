


create_model = require 'create_model'

local opt = {
  dictsize = 200000 + 2 + 1, -- ONE FOR PADDING, TWO FOR RARE and RARENUMBER
  wdvecdim = 50,
  contextsize = 5,
  hu1sz = 300, 
  margin = 1,
  batchsize = 100,
  learningRate = 1e-1,
}

local model, criterion = create_model(opt)
