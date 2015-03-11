# POSTagger

This is a repos for practical of Computational Linguisitics at Oxford.
This is a POS tagger, implemented seperately by HMM and ConvNet.
This work mainly refers to several works as follows.

# Structure
The project structure is as follows

* luaSrc is the src folder for training embeddings and postagger, backended by Neural Network
* src/main/scala is the src folder for training HMM n-gram, namely bigram and trigram model. Furthermore, there are some other utilities used in the project are implemented in scala, such as FilePermutator and CorpusManager.
* doc is the document folder, storing the technical report of the practical.
* res (which is invisible in github repos), is the folder for storing the corpus and the wikipedia xml dump.
* lib (which is invisible in github repos), is the folder for storing the unmanaged libraries.

# Build

Download the 0.13.* SBT and clone the repos. Just enter the following command in the cloned folder

    sbt compile

The command will build the scala part of project automatically


#Dependencies

This project depends on the following libraries, which are not managed by sbt.

1. fbcunn
2. BIDMat
3. torch 7
4. Stanford NLTK

In order to build the whole practical, and also the lua part of it, ensure you install the libraries.


# Reference
1. Collobert, Ronan, et al. "Natural language processing (almost) from scratch." The Journal of Machine Learning Research 12 (2011): 2493-2537.
2. Chen, Stanley F., and Joshua Goodman. "An empirical study of smoothing techniques for language modeling." Proceedings of the 34th annual meeting on Association for Computational Linguistics. Association for Computational Linguistics, 1996.
3. Collobert, Ronan, Koray Kavukcuoglu, and Clément Farabet. "Torch7: A matlab-like environment for machine learning." BigLearn, NIPS Workshop. No. EPFL-CONF-192376. 2011.
4. Canny, John, and Huasha Zhao. "Bidmach: Large-scale learning with zero memory allocation." BigLearn Workshop, NIPS. 2013.
5. Manning, Christopher D., et al. "The Stanford CoreNLP natural language processing toolkit." Proceedings of 52nd Annual Meeting of the Association for Computational Linguistics: System Demonstrations. 2014.
6. Wikipedia Extractor [https://github.com/bwbaugh/wikipedia-extractor]
