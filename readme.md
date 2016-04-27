Dageva
======

 Dageva is a tool for automatic construction of machine learning workflows/ensembles by evolving them using genetic programming.
 Here we explain how to use it.


Overview
--------

 Evolution and evaluation take place in separate programs communicating over network (using XML-RPC).
 Evolution "client" (dageva) is implemented in Java, whereas evaluation "server" (dag-evaluate) is implemented in Python.


 __TODO__  : základní přehled toho co to zvládne, tzn: 
  * typy ML metod a jaký maj typy
  * asi obrázek ABCDE asi rozdělenej a okomentovanej
  * příklady výsledků



  
Installing and running the program
----------------------------------

 You can download the program dageva.jar and the default config file config.json from the release directory in this repository. 
 In order to run the program __Java8__ JRE must be installed.

 You must provide two program arguments: 
  * JSON config filename specifying all the input options.
  * Path to the directory, where output in the form of logs will be stored. (Which will be created if it does not exist.)

For example running the following command will use config.json file located in the same directory as the dageva.jar file
and it will create a new directory called logs for the logs. 

```
java -jar dageva.jar config.json logs
```

Output format
-------------

Output is in the form of log files. 
For each run is created its own directory in the log directory.
Each run directory contains copy of the used config file for the 

__TODO__

Both client and server have their own logging.

Config file
-----------

 Dageva is controlled by a config file represented as a simple json file.
 On the top level there is a object with several properties which specify the settings.
 Let us go through the properties:

  * __seed__ : Optional option for setting seed of the pseudo random number generator (only for the evolution client). (long int)   
  * __serverUrl__ : URL on which the evaluator runs. (string) 
  * __killServer__ : Whether to close the server program after the evolution finishes. (bool)
  * __dataset__ : Filename containing the dataset as CSV. (string)
  * __numGenerations__ : Number of evolution generations. (int)
  * __populationSize__ : Number of individuals in each generation. (int)
  * __generatingMaxTreeSize__ : Maximal permitted size (number of symbols) in generated program trees. (int)
  * __tournamentBetterWinsProbability__ : parameter controlling randomness of the tournament individual selection method. (float from [0,1])
  * __saveBest__ : Whether the best individual is automatically preserved to the next generation. (bool)  
  * __basicTypedXover__ : Options for the tree-swapping crossover operation. (object)
    * __probability__ : Probability of using this genetic operation. (float from [0,1])
    * __maxTreeSize__ : Maximal size of an individual produced by this operation. (int)
  * __sameSizeSubtreeMutation__ : Options for the subtree-generating  mutation operation. (object)
    * __probability__ : Probability of using this genetic operation. (float from [0,1])
    * __maxSubtreeSize__ : Maximal allowed size of selected subtree to be replaced by newly generated subtree with the same size. (float from [0,1]) 
  * __oneParamMutation__ : Options for the mutation operation that changes one numerical parameter. (object)
    * __probability__ : Probability of using this genetic operation. (float from [0,1])
    * __shiftsWithProbabilities__  : Each numerical parameter is associated with an ordered list of possible values.
                                     This option specifies possible changes of values given as an array of possible
                                     "shifts" of values. It is an array of pairs (i.e. 2-elemt arrays) of a form [shift,probability].
                                     For example if the option is set to [[-2, 0.1], [-1, 0.4], [1, 0.4], [2, 0.1]] 
                                     and a parameter has the list of possible values [0.01, 0.05, 0.1, 0.2, 0.5, 1]
                                     and the current value is 0.1, the mutation with probability 0.4 changes the value to 0.05,
                                     with probability 0.4 to 0.2, with probability 0.1 to 0.01 and with probability 0.1 to 0.5. 
                                     (array of [int,float] pairs)
  * __copyOp__ : Options for the genetic operation "reproduction" that makes an exact copy of the selected individual. (object)
    * __probability__ : Probability of using this genetic operation. (float from [0,1])
    
  * __goalType__ : The type of the program tree individual representing the generated workflow.  (string)
  * __lib__ : Symbols (functions and terminals) from which are the individual program trees build. 
              Those symbols stand for the machine learning methods and for the  functions combining smaller forkflows into
              bigger ones (aka higher-order combinators).  (array of strings)

Example of a config file:
    
```json
{
  "serverUrl" : "http://127.0.0.1:8080",
  "killServer": true,

  "dataset" : "winequality-white.csv",

  "numGenerations": 10,
  "populationSize": 3,

  "generatingMaxTreeSize":  15,

  "tournamentBetterWinsProbability" : 0.8,
  "saveBest" : true,

  "basicTypedXover" : {"probability": 0.3 , "maxTreeSize": 50},
  "sameSizeSubtreeMutation" : {"probability": 0.3, "maxSubtreeSize": 10},
  "oneParamMutation" : {"probability" : 0.3, "shiftsWithProbabilities": [[-2, 0.1], [-1, 0.4], [1, 0.4], [2, 0.1]]},
  "copyOp" : {"probability": 0.1},


  "goalType" : "D => LD",

  "lib" : [
    "TypedDag.dia( TypedDag: D => D , TypedDag: D => (V LD n) , TypedDag: (V LD n) => LD ) : D => LD",
    "TypedDag.dia0( TypedDag: D => (V LD n) , TypedDag: (V LD n) => LD ) : D => LD",
    "TypedDag.split( TypedDag: D => (V D n) , MyList: V (D => LD) n ) : D => (V LD n)",
    "MyList.cons( Object: a , MyList: V a n ) : V a (S n)",
    "MyList.nil : V a 0",
    "PCA : D => D",
    "kBest : D => D",
    "kMeans : D => (V D (S(S n)))",
    "copy : D => (V D (S(S n)))",
    "SVC        : D => LD",
    "logR       : D => LD",
    "gaussianNB : D => LD",
    "DT         : D => LD",
    "vote : (V LD (S(S n))) => LD"
  ]
}
```
    
    



Clent server communication
--------------------------

__todo__ Zlehka o technikalitách 
 * jak se posílaj parametry 
   *  allParamsInfo = {"kBest": {"feat_frac": [0.01, 0.05, 0.1, 0.25, 0.5, 0.75, 1]}, "copy": {}, "DT": {"max_features": [0.05, 0.1, 0.25, 0.5, 0.75, 1], "max_depth": [1, 2, 5, 10, 15, 25, 50, 100], "min_samples_leaf": [1, 2, 5, 10, 20], "criterion": ["gini", "entropy"], "min_samples_split": [1, 2, 5, 10, 20]}, "logR": {"penalty": ["l1", "l2"], "C": [0.1, 0.5, 1.0, 2, 5, 10, 15], "tol": [0.0001, 0.001, 0.01]}, "PCA": {"whiten": [false, true], "feat_frac": [0.01, 0.05, 0.1, 0.25, 0.5, 0.75, 1]}, "union": {}, "kMeans": {}, "vote": {}, "gaussianNB": {}, "SVC": {"C": [0.1, 0.5, 1.0, 2, 5, 10, 15], "tol": [0.0001, 0.001, 0.01], "gamma": [0.0, 0.0001, 0.001, 0.01, 0.1, 0.5]}}
 * jak se posílaj dagy


Algorithm overview
------------------

__todo__ těžko říct esli je potřeba ale zas neni tak těžký přepsat pseudocod z článku a vypadá hustě imho



TODO - ruzný věci co pak někam připsat
--------------------------------------

 * Intro do evoluce?
 * Napsat tam, že je to work in progress.
 * Odůvodnit nějak rozumě, že to probíhá v separovanejch programech
   * real důvod - GP máme v Javě, ML se dělá v Pajtnu
   * modulární (haluzní) důvod - pak to pude napojovat na různý vyhodnocovače (dá se opřít realnym příkladem, pač toho mravence mám  v Haskellu :))
