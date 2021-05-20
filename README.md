# RebalanceDataStream

Every data stream has the concept drift phenomenon that is the cause of the class imbalancement. Moreover, this can be responsible for bad performances and all the rebalancing existing techniques are not suitable for data stream. These are the reasons why I decided to set up this project, in order to create a possible approach to rebalance a data stream. It is developed in **Java** with the **MOA** library.

# RebalanceStream Algorithm

The general idea is to use **ADWIN**, as **Adaptive Random Forest algorithm** does, in order to detect when there is a concept drift in the data stream and be able to adapt the model under construction. When it happens, the aim is to use **SMOTE** to rebalance the data arrived up to that point and to use them to train other models. The best one is chosen in order to continue the execution.<br>
More specifically, while a new sample arrives, I do the prequential evaluation, I train the model called *learner* and I save the data in a *batch*. When *ADWIN* detects a warning, I start collecting the data also in a new batch called *resetBatch*, while when *ADWIN* detects a change, I create 3 other models:
- *LearnerBal*: I apply *SMOTE* on the *batch* and I use it to train the model, in the same way as before.
- *LearnerReset*: I use the *resetBatch* to train the model.
- *LearnerResetBal*: I apply *SMOTE* on the *resetBatch* and I use it to train the model.

In order to compare the 4 learners, I calculate the prequential evaluation k-statistic for all of them and I choose the best one. This will be used when a new sample will arrive, while the others are resetted together to the *batch* and the *resetBatch*.  

This algorithm is compared to its **Base version**. Both use the *SWT classifier with ARF as base learner*. For each algorithm, every 2,000 data, I save the prequential evaluation k-statistic and I use the validation set to validate the model, saving at the end its k-statistic results. At the and I have 2 sets of results for each algorithm.

# RebalanceStream+ Algorithm

Starting from these results I also propose a new synthetic algorithm. I create a new set of results: for each element of the prequential evaluation results, if prequential evaluation of the base version algorithm is better than the prequential evaluation of proposed one, I take the result on validation set of the base version algorithm, otherwise I take the other one. In this way I should grant the best result every time.

# Results

The dataset used in the experiments is a randomly generated one. I generate *100,000* samples with *50 centroids*, a speed change of *0.0000001*, *10* attributes and *2* classes. In order to change the imbalancement level, the class ratio is randomly chosen from values between *(0.6;0.4)* and *(0.9;0.1)* and it is changed every some random numbers of rows, decided by a *Gaussian model* with a certain mean and variance. In order to do some tests, I use different combinations of mean and variance. The mean values used are *20,000, 22,500, 25,000, 27,500 and 30,000*. The variance values used are *50, 100, 200 and 400*. For each one of them, 5 different seeds *(3, 4, 5, 6, 7)* are used to randomly generate 5 datasets. From each one, a training and a validation set is created (90\% - 10\%).<br>
After the execution of the 3 algorithms, I have 10 sets of results for each one of them, 5 sets for each typology. I plot only the validation k-statistic set, because it gives the possibility to know the performances on data that the algorithm will never use to train its model. In this way I have only one set *s* of results for each dataset, for each algorithm, for a total of 5 sets of results for algorithm. In order to aggregate and plot them, I make a "vertical" mean  to obtain a mean, minimum and maximum set of results for each algorithm. For all the elements in each set, I make the mean among the *i* values of sets *s1, s2, s3, s4, s5* and I find the minimum and maximum value. Finally, I have 3 sets of results for each algorithm: the mean of k-statistic *mean*, the minimum values *min* and the maximum values *max*. These are shown in a unique line in a line chart.

I also create a heatmap that, for each combination of mean and variance, allows to easily compare the results of a pair of algorithms. It is created starting from a matrix of results. Between the end of one combination and the beginning of another, I update the corresponding matrix with the result of the actual combination. I start calculating it from the *mean1* and *mean2* sets of results of the 2 algorithms to compare. For all their elements, I take the element *i* from *mean1* and *mean2* and I calculate the difference. At the end I make the mean of all the differences and I save the result in the matrix in correspondence of the right combination of row and column, where the row states the mean of the actual combination and the column states the variance of the same. If the result is positive, it means that the first algorithm is better than the second. In the heatmap, it will be green.

# Set up

In order to run the code, after having cloned the repository, you must import the code in a java editor. I used **Eclipse**. After installing the eclipse **m2e plugin**, go to **File**, **Import**, and select the **Existing Maven Projects** option. Set **Root Directory** to your MOA code folder (Repository\moa\moa). Follow all the steps of the wizard. It is possible that you get this error, but just ignore it: "Plugin execution not covered by lifecycle configuration: org.codehaus.mojo:license-maven-plugin:1.1:add-third-party (execution: add-third-party, phase: generate-resources)". It is necessary to add to the build path the **jfreechart** and **weka** jar files. 

Search the *BenchmarkManager* class in moa/src/defaulpackage and run it. All the plots will be saved in the *plots* folder.

# Citation

Currently, the [RebalanceDataStream](https://github.com/Waikato/moa/blob/master/moa/src/main/java/moa/classifiers/meta/imbalanced/RebalanceStream.java) is included in the official MOA repository. If this work was useful for you, please feel free to cite us with the format below.

```
@INPROCEEDINGS{9346536,
  author={Bernardo, Alessio and {Della Valle}, Emanuele and Bifet, Albert},
  booktitle={2020 International Conference on Data Mining Workshops (ICDMW)}, 
  title={Incremental Rebalancing Learning on Evolving Data Streams}, 
  year={2020},
  volume={},
  number={},
  pages={844-850},
  doi={10.1109/ICDMW51313.2020.00121}}
```


For more information, please refer to my [thesis](https://www.politesi.polimi.it/bitstream/10589/145564/3/2019_04_Bernardo.pdf)

