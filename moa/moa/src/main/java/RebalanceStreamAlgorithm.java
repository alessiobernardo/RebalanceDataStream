
import moa.classifiers.meta.TemporallyAugmentedClassifier;
import moa.classifiers.Classifier;
import moa.classifiers.core.driftdetection.ADWIN;
import moa.core.InstanceExample;
import moa.core.SerializeUtils;
import moa.core.TimingUtils;
import moa.options.ClassOption;
import moa.streams.ArffFileStream;
import moa.streams.FilteredStream;
import moa.streams.ImbalancedStream;
import moa.streams.generators.RandomRBFGenerator;
import moa.streams.generators.RandomRBFGeneratorDrift;
import weka.core.Attribute;
import weka.core.Instances;
import weka.core.Utils;
import weka.filters.Filter;
import weka.filters.supervised.instance.SMOTE;

import com.github.javacliparser.IntOption;
import com.yahoo.labs.samoa.instances.Instance;
import com.yahoo.labs.samoa.instances.InstancesHeader;
import com.yahoo.labs.samoa.instances.SamoaToWekaInstanceConverter;
import com.yahoo.labs.samoa.instances.WekaToSamoaInstanceConverter;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.stream.IntStream;

import org.apache.commons.lang3.ArrayUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;


public class RebalanceStreamAlgorithm {
	
	public int[][] confusionMatrixLearner = new int[2][2];
	double accLearner = 0;
	double kStatLearner = 0;
	
	public int[][] confusionMatrixResetBal = new int[2][2];
	double accResetBal = 0;
	double kStatResetBal = 0;
	
	public int[][] confusionMatrixReset = new int[2][2];
	double accReset = 0;
	double kStatReset = 0;
	
	public int[][] confusionMatrixBal = new int[2][2];
	double accBal = 0;
	double kStatBal = 0;
		
	public static ArrayList<Double> acc = new ArrayList<Double>();

	public int[][] confusionMatrixEval = new int[2][2];
	public static ArrayList<Double> accEval = new ArrayList<Double>();

	
	public RebalanceStreamAlgorithm() {}
	
	public void clean() {
		for (int r = 0; r < 2; r++) {
			for (int c = 0; c < 2; c++) {
				confusionMatrixLearner[r][c] = 0;
				confusionMatrixResetBal[r][c] = 0;
				confusionMatrixReset[r][c] = 0;
				confusionMatrixBal[r][c] = 0;
			}	
		}
		
		acc.clear();
		
		accResetBal = 0;
		kStatResetBal = 0;
		
		accLearner = 0;
		kStatLearner = 0;
		
		accReset = 0;
		kStatReset = 0;
		
		accBal = 0;
		kStatBal = 0;
		
	}
	
	public void cleanLearner() {
		for (int r = 0; r < 2; r++) {
			for (int c = 0; c < 2; c++) {
				confusionMatrixLearner[r][c] = 0;				
			}	
		}
				
		accLearner = 0;
		kStatLearner = 0;
		
	}
	
	public void cleanResetBal() {
		for (int r = 0; r < 2; r++) {
			for (int c = 0; c < 2; c++) {				
				confusionMatrixResetBal[r][c] = 0;
			}	
		}
				
		accResetBal = 0;
		kStatResetBal = 0;		
	}
	
	public void cleanReset() {
		for (int r = 0; r < 2; r++) {
			for (int c = 0; c < 2; c++) {				
				confusionMatrixReset[r][c] = 0;
			}	
		}
				
		accReset = 0;
		kStatReset = 0;		
	}
	
	public void cleanBal() {
		for (int r = 0; r < 2; r++) {
			for (int c = 0; c < 2; c++) {				
				confusionMatrixBal[r][c] = 0;
			}	
		}
				
		accBal = 0;
		kStatBal = 0;		
	}
	
	
	public void cleanEval() {
		for (int r = 0; r < 2; r++) {
			for (int c = 0; c < 2; c++) {
				confusionMatrixEval[r][c] = 0;
			}	
		}
	}
	
	public ArrayList<ArrayList<Double>> run(InstancesHeader header, Instances trainingDataSet, Instances validationDataSet) throws Exception{
		
		ArrayList<ArrayList<Double>> valToReturn = new ArrayList<ArrayList<Double>>();
		ArrayList<Double> kStatEval = new ArrayList<Double>();
		ArrayList<Double> kStat = new ArrayList<Double>();
		ArrayList<Double> modelChosen = new ArrayList<Double>();
				
		ArrayList<Double> sizeBatchReset = new ArrayList<Double>();	
		
		//servono solo in benchmarking
		WekaToSamoaInstanceConverter wekaToSamoa = new WekaToSamoaInstanceConverter();
		SamoaToWekaInstanceConverter samoaToWeka = new SamoaToWekaInstanceConverter();
		Instance testInst;
		int nModel = 1;
		int nElementsTest = 0;
		double percMin = 0;
		ArrayList<Instance> batch = new ArrayList<Instance>();
		ArrayList<Instance> resetBatch = new ArrayList<Instance>();
		ArrayList<Double> compare = new ArrayList<Double>();
		
		clean();
		                     
        TemporallyAugmentedClassifier learner = new TemporallyAugmentedClassifier();
        learner.baseLearnerOption = new ClassOption("baseLearner", 'l', "Classifier to train.", Classifier.class, "meta.AdaptiveRandomForest");                
        learner.setModelContext(header);        
        learner.prepareForUse();
        
        TemporallyAugmentedClassifier learnerResetBal = new TemporallyAugmentedClassifier();
        learnerResetBal.baseLearnerOption = new ClassOption("baseLearner", 'l', "Classifier to train.", Classifier.class, "meta.AdaptiveRandomForest"); 
        learnerResetBal.setModelContext(header);        
        
        TemporallyAugmentedClassifier learnerReset = new TemporallyAugmentedClassifier();
        learnerReset.baseLearnerOption = new ClassOption("baseLearner", 'l', "Classifier to train.", Classifier.class, "meta.AdaptiveRandomForest"); 
        learnerReset.setModelContext(header);        
        
        TemporallyAugmentedClassifier learnerBal = new TemporallyAugmentedClassifier();
        learnerBal.baseLearnerOption = new ClassOption("baseLearner", 'l', "Classifier to train.", Classifier.class, "meta.AdaptiveRandomForest"); 
        learnerBal.setModelContext(header);     
       
        int numberSamples = 0;
        int numberSamplesCorrect = 0;
        float nMin = 0;
		float nMaj = 0;
		float resetNMin = 0;
		float resetNMaj = 0;
		int neighbors = 0;
		Instances resetBatchBal;
		Instances batchBal;
        SMOTE smote = new SMOTE();
        ADWIN drift = new ADWIN();
        Instance trainInst;        
        Instance trainInstBal;
        
        double p0,p1,pc,k,accuracy;
        double modelInUse = 0.0;
        boolean warning = false;
        long evaluateStartTime = TimingUtils.getNanoCPUTimeOfCurrentThread();
        
        for (int row = 0; row < trainingDataSet.numInstances(); row ++) {       
        	trainInst = wekaToSamoa.samoaInstance(trainingDataSet.instance(row));
                //trainInst = (Instance) stream.nextInstance().getData();
                	
            /*					learner.correctlyClassifies(trainInst)
                	 				pred 0.0	pred 1.0
            trainInst.classValue()
            		correct 0.0
                	correct 1.0
            */
              
            if (learner.correctlyClassifies(trainInst) && trainInst.classValue() == 0.0){
                confusionMatrixLearner[0][0] ++;
                drift.setInput(0);                    	
            }
            else if (learner.correctlyClassifies(trainInst) && trainInst.classValue() == 1.0){
                confusionMatrixLearner[1][1] ++;
                drift.setInput(0);                    	
            }
            else if (!learner.correctlyClassifies(trainInst) && trainInst.classValue() == 0.0){
                confusionMatrixLearner[0][1] ++;
                drift.setInput(1);                    
            }
            else if (!learner.correctlyClassifies(trainInst) && trainInst.classValue() == 1.0){
            	confusionMatrixLearner[1][0] ++;
                drift.setInput(1);                    	
            }
                  
            learner.trainOnInstance(trainInst);                    
                    
            if (row == (nModel * 2000)) {            
                    	
            	numberSamplesCorrect = confusionMatrixLearner[0][0] + confusionMatrixLearner[1][1];
            	numberSamples = confusionMatrixLearner[0][0] + confusionMatrixLearner[1][1] + confusionMatrixLearner[0][1] + confusionMatrixLearner[1][0];
            	accuracy = (double) numberSamplesCorrect/ (double) numberSamples;
            	acc.add(accuracy);
                        
            	p0 = (((double)confusionMatrixLearner[0][0] + (double)confusionMatrixLearner[0][1]) / (double) numberSamples) * (((double)confusionMatrixLearner[0][0] + (double)confusionMatrixLearner[1][0]) / (double) numberSamples);
            	p1 = (((double)confusionMatrixLearner[1][0] + (double)confusionMatrixLearner[1][1]) / (double) numberSamples) * (((double)confusionMatrixLearner[0][1] + (double)confusionMatrixLearner[1][1]) / (double) numberSamples);
            	pc = p0 + p1;
            	k = (double)(accuracy - pc) / (double)(1 - pc);
            	kStat.add(k);
                    	                   	
            	nElementsTest = 0;
            	numberSamples = 0;
            	numberSamplesCorrect = 0;
            	cleanEval();
                                                           	
            	for (int s = 0; s < validationDataSet.size(); s ++) {
                        	            		
            		testInst = wekaToSamoa.samoaInstance(validationDataSet.instance(s));
                       	                        	
            		if (learner.correctlyClassifies(testInst) && testInst.classValue() == 0.0){
                        confusionMatrixEval[0][0] ++;
            		}
            		else if (learner.correctlyClassifies(testInst) && testInst.classValue() == 1.0){
                        confusionMatrixEval[1][1] ++;
            		}
            		else if (!learner.correctlyClassifies(testInst) && testInst.classValue() == 0.0){
                        confusionMatrixEval[0][1] ++;
            		}
            		else if (!learner.correctlyClassifies(testInst) && testInst.classValue() == 1.0){
            			confusionMatrixEval[1][0] ++;
            		}	
                       	
            		numberSamples ++;
            		nElementsTest ++;
                        	
            	}            	                     
                        
            	numberSamplesCorrect = confusionMatrixEval[0][0] + confusionMatrixEval[1][1];
            	accuracy = (double) numberSamplesCorrect/ (double) numberSamples;
            	accEval.add(accuracy);
                        
            	p0 = (((double)confusionMatrixEval[0][0] + (double)confusionMatrixEval[0][1]) / (double) numberSamples) * (((double)confusionMatrixEval[0][0] + (double)confusionMatrixEval[1][0]) / (double) numberSamples);
            	p1 = (((double)confusionMatrixEval[1][0] + (double)confusionMatrixEval[1][1]) / (double) numberSamples) * (((double)confusionMatrixEval[0][1] + (double)confusionMatrixEval[1][1]) / (double) numberSamples);
            	pc = p0 + p1;
            	k = (double)(accuracy - pc) / (double)(1 - pc);
            	kStatEval.add(k);
                    	
            	modelChosen.add(modelInUse);
                                                
            	nModel ++;
            }
                
            batch.add(trainInst);
            if (warning == true) {
            	resetBatch.add(trainInst);
            }
            if (trainInst.classValue() == 1.0) {
            	nMaj ++;
                if (warning == true) {
                	resetNMaj ++;
                }
            } else {
                nMin ++;
                if (warning == true) {
                	resetNMin ++;
                }
            }
                               
            //I already learned everything
            if (drift.getChange()) {   
            	
            	sizeBatchReset.add((double) resetBatch.size());            	
            	
            	numberSamplesCorrect = confusionMatrixLearner[0][0] + confusionMatrixLearner[1][1];
        		numberSamples = confusionMatrixLearner[0][0] + confusionMatrixLearner[1][1] + confusionMatrixLearner[0][1] + confusionMatrixLearner[1][0];
                accuracy = (double) numberSamplesCorrect/ (double) numberSamples;
                accLearner = accuracy;
                
                p0 = (((double)confusionMatrixLearner[0][0] + (double)confusionMatrixLearner[0][1]) / (double) numberSamples) * (((double)confusionMatrixLearner[0][0] + (double)confusionMatrixLearner[1][0]) / (double) numberSamples);
                p1 = (((double)confusionMatrixLearner[1][0] + (double)confusionMatrixLearner[1][1]) / (double) numberSamples) * (((double)confusionMatrixLearner[0][1] + (double)confusionMatrixLearner[1][1]) / (double) numberSamples);
                pc = p0 + p1;
                k = (double)(accuracy - pc) / (double)(1 - pc);
                kStatLearner = k;                                
                
                //batchBal from batch               
                batchBal = samoaToWeka.wekaInstancesInformation(batch.get(0).dataset());
                for (int l = 0; l < batch.size(); l ++) {
                	batchBal.add(samoaToWeka.wekaInstance(batch.get(l)));                    	
                }
                batchBal.setClassIndex(batchBal.numAttributes() - 1);
                
                //learnerBal on batchBal with smote                   
            	if (nMaj > 1 && nMin > 1) {  
            		
            		percMin = nMin / (nMin + nMaj);
                    if (percMin > 0.1) {

	                    float perc = 0;
	        			if (nMaj > nMin) {
	        				perc = ((nMaj / nMin)-1)*100;
	        				if (nMin > 5) {
	        					neighbors = 5;
	        				}
	        				else  {
	        					neighbors = (int) (nMin - 1);
	        				}
	        			}else {
	        				perc = ((nMin / nMaj)-1)*100;
	        				if (nMaj > 5) {
	        					neighbors = 5;
	        				}
	        				else  {
	        					neighbors = (int) (nMaj - 1);
	        				}
	        			}
	        			
	        			
	        			            			
	        			smote.setInputFormat(batchBal);
	        			smote.setPercentage(perc);
	        			smote.setNearestNeighbors(neighbors);
	        			batchBal = Filter.useFilter(batchBal, smote);
	        			
	        			learnerBal.prepareForUse();
	        			
	        			for (int r = 0; r < batchBal.numInstances(); r ++) {
	        				trainInstBal = wekaToSamoa.samoaInstance(batchBal.instance(r));
	        				
	        				if (learnerBal.correctlyClassifies(trainInstBal) && trainInstBal.classValue() == 0.0){
	        					confusionMatrixBal[0][0] ++;                            	
	                        }
	                        else if (learnerBal.correctlyClassifies(trainInstBal) && trainInstBal.classValue() == 1.0){
	                        	confusionMatrixBal[1][1] ++;                            	
	                        }
	                        else if (!learnerBal.correctlyClassifies(trainInstBal) && trainInstBal.classValue() == 0.0){
	                        	confusionMatrixBal[0][1] ++;                            
	                        }
	                        else if (!learnerBal.correctlyClassifies(trainInstBal) && trainInstBal.classValue() == 1.0){
	                        	confusionMatrixBal[1][0] ++;                            	
	                        }
	        				
	        				learnerBal.trainOnInstance(trainInstBal);
	        			}
	        			
	        			numberSamplesCorrect = confusionMatrixBal[0][0] + confusionMatrixBal[1][1];
	        			numberSamples = confusionMatrixBal[0][0] + confusionMatrixBal[1][1] + confusionMatrixBal[0][1] + confusionMatrixBal[1][0];
	                    accuracy = (double) numberSamplesCorrect/ (double) numberSamples;
	                    accBal = accuracy;
	                    
	                    p0 = (((double)confusionMatrixBal[0][0] + (double)confusionMatrixBal[0][1]) / (double) numberSamples) * (((double)confusionMatrixBal[0][0] + (double)confusionMatrixBal[1][0]) / (double) numberSamples);
	                    p1 = (((double)confusionMatrixBal[1][0] + (double)confusionMatrixBal[1][1]) / (double) numberSamples) * (((double)confusionMatrixBal[0][1] + (double)confusionMatrixBal[1][1]) / (double) numberSamples);
	                    pc = p0 + p1;
	                    k = (double)(accuracy - pc) / (double)(1 - pc);
	                    kStatBal = k;   
	                    
                    } else {
                    	kStatBal = -1;
                    }                     
            	} else {
            		kStatBal = -1;
            	}

                //resetBatchBal from resetBatch                
                resetBatchBal = samoaToWeka.wekaInstancesInformation(resetBatch.get(0).dataset());
                for (int l = 0; l < resetBatch.size(); l ++) {
                	resetBatchBal.add(samoaToWeka.wekaInstance(resetBatch.get(l)));                    	
                }
                resetBatchBal.setClassIndex(resetBatchBal.numAttributes() - 1);

                
                //learnerReset on resetBatchBal without smote
                learnerReset.prepareForUse();
    			
    			for (int r = 0; r < resetBatchBal.numInstances(); r ++) {
    				trainInstBal = wekaToSamoa.samoaInstance(resetBatchBal.instance(r));
    				
    				if (learnerReset.correctlyClassifies(trainInstBal) && trainInstBal.classValue() == 0.0){
    					confusionMatrixReset[0][0] ++;                            	
                    }
                    else if (learnerReset.correctlyClassifies(trainInstBal) && trainInstBal.classValue() == 1.0){
                    	confusionMatrixReset[1][1] ++;                            	
                    }
                    else if (!learnerReset.correctlyClassifies(trainInstBal) && trainInstBal.classValue() == 0.0){
                    	confusionMatrixReset[0][1] ++;                            
                    }
                    else if (!learnerReset.correctlyClassifies(trainInstBal) && trainInstBal.classValue() == 1.0){
                    	confusionMatrixReset[1][0] ++;                            	
                    }
    				
    				learnerReset.trainOnInstance(trainInstBal);
    			}
    			
    			numberSamplesCorrect = confusionMatrixReset[0][0] + confusionMatrixReset[1][1];
    			numberSamples = confusionMatrixReset[0][0] + confusionMatrixReset[1][1] + confusionMatrixReset[0][1] + confusionMatrixReset[1][0];
                accuracy = (double) numberSamplesCorrect/ (double) numberSamples;
                accReset = accuracy;
                
                p0 = (((double)confusionMatrixReset[0][0] + (double)confusionMatrixReset[0][1]) / (double) numberSamples) * (((double)confusionMatrixReset[0][0] + (double)confusionMatrixReset[1][0]) / (double) numberSamples);
                p1 = (((double)confusionMatrixReset[1][0] + (double)confusionMatrixReset[1][1]) / (double) numberSamples) * (((double)confusionMatrixReset[0][1] + (double)confusionMatrixReset[1][1]) / (double) numberSamples);
                pc = p0 + p1;
                k = (double)(accuracy - pc) / (double)(1 - pc);
                kStatReset = k;
                
                
                //learnerResetBal on resetBatchBal with smote                   
            	if (resetNMaj > 1 && resetNMin > 1) {    
            		
            		percMin = resetNMin / (resetNMin + resetNMaj);
                    if (percMin > 0.1) {
            		
	                    float perc = 0;
	        			if (resetNMaj > resetNMin) {
	        				perc = ((resetNMaj / resetNMin)-1)*100;
	        				if (resetNMin > 5) {
	        					neighbors = 5;
	        				}
	        				else  {
	        					neighbors = (int) (resetNMin - 1);
	        				}
	        			}else {
	        				perc = ((resetNMin / resetNMaj)-1)*100;
	        				if (resetNMaj > 5) {
	        					neighbors = 5;
	        				}
	        				else  {
	        					neighbors = (int) (resetNMaj - 1);
	        				}
	        			}
	        		            			
	        			smote.setInputFormat(resetBatchBal);
	        			smote.setPercentage(perc);
	        			smote.setNearestNeighbors(neighbors);
	        			resetBatchBal = Filter.useFilter(resetBatchBal, smote);
	        			
	        			learnerResetBal.prepareForUse();
	        			
	        			for (int r = 0; r < resetBatchBal.numInstances(); r ++) {
	        				trainInstBal = wekaToSamoa.samoaInstance(resetBatchBal.instance(r));
	        				
	        				if (learnerResetBal.correctlyClassifies(trainInstBal) && trainInstBal.classValue() == 0.0){
	        					confusionMatrixResetBal[0][0] ++;                            	
	                        }
	                        else if (learnerResetBal.correctlyClassifies(trainInstBal) && trainInstBal.classValue() == 1.0){
	                        	confusionMatrixResetBal[1][1] ++;                            	
	                        }
	                        else if (!learnerResetBal.correctlyClassifies(trainInstBal) && trainInstBal.classValue() == 0.0){
	                        	confusionMatrixResetBal[0][1] ++;                            
	                        }
	                        else if (!learnerResetBal.correctlyClassifies(trainInstBal) && trainInstBal.classValue() == 1.0){
	                        	confusionMatrixResetBal[1][0] ++;                            	
	                        }
	        				
	        				learnerResetBal.trainOnInstance(trainInstBal);
	        			}
	        			
	        			numberSamplesCorrect = confusionMatrixResetBal[0][0] + confusionMatrixResetBal[1][1];
	        			numberSamples = confusionMatrixResetBal[0][0] + confusionMatrixResetBal[1][1] + confusionMatrixResetBal[0][1] + confusionMatrixResetBal[1][0];
	                    accuracy = (double) numberSamplesCorrect/ (double) numberSamples;
	                    accResetBal = accuracy;
	                    
	                    p0 = (((double)confusionMatrixResetBal[0][0] + (double)confusionMatrixResetBal[0][1]) / (double) numberSamples) * (((double)confusionMatrixResetBal[0][0] + (double)confusionMatrixResetBal[1][0]) / (double) numberSamples);
	                    p1 = (((double)confusionMatrixResetBal[1][0] + (double)confusionMatrixResetBal[1][1]) / (double) numberSamples) * (((double)confusionMatrixResetBal[0][1] + (double)confusionMatrixResetBal[1][1]) / (double) numberSamples);
	                    pc = p0 + p1;
	                    k = (double)(accuracy - pc) / (double)(1 - pc);
	                    kStatResetBal = k;
	                    
                    } else {
                    	kStatResetBal = -1;
                    }                                                                 		
            	} else {
            		kStatResetBal = -1;
            	}
                 
            	compare.clear();
            	compare.add(0,kStatLearner);
            	compare.add(1,kStatBal);
            	compare.add(2,kStatReset);
            	compare.add(3,kStatResetBal);
            	
            	double max = compare.get(0);
            	int maxPos = 0;
            	for (int pos = 0; pos < compare.size(); pos ++) {
            		if (compare.get(pos) > max) {
            			max = compare.get(pos);
            			maxPos = pos;
            		}
            	}
            
            	if (maxPos == 0) { //learner is the best
            		modelInUse = (double) maxPos;
            		
            		learnerBal.resetLearning();
                    learnerReset.resetLearning();
                    learnerResetBal.resetLearning();
                    
                    cleanResetBal();
                	cleanReset();
                	cleanBal();
                	
            	}
            	else if (maxPos == 1) { //learnerBal is the best
            		modelInUse = (double) maxPos;

            		learner = (TemporallyAugmentedClassifier) learnerBal.copy();
                    learnerBal.resetLearning();
                    learnerReset.resetLearning();
                    learnerResetBal.resetLearning();
                    
                    for(int r = 0; r < 2; r ++){
                	    for(int c = 0; c < 2 ; c ++){
                	    	confusionMatrixLearner[r][c] = confusionMatrixBal[r][c];
                	    }
                	} 
                    
                    cleanResetBal();
                	cleanReset();
                	cleanBal();
                	
            	}
            	else if (maxPos == 2) { //learnerReset is the best
            		modelInUse = (double) maxPos;
                    
                    learner = (TemporallyAugmentedClassifier) learnerReset.copy();
                    learnerBal.resetLearning();
                    learnerReset.resetLearning();
                    learnerResetBal.resetLearning();
                    
                    for(int r = 0; r < 2; r ++){
                	    for(int c = 0; c < 2 ; c ++){
                	    	confusionMatrixLearner[r][c] = confusionMatrixReset[r][c];
                	    }
                	}
                    
                    cleanResetBal();
                	cleanReset();
                	cleanBal();
                	
            	}
            	else { //learnerResetBal is the best
            		modelInUse = (double) maxPos;
                    
                    learner = (TemporallyAugmentedClassifier) learnerResetBal.copy();
                    learnerBal.resetLearning();
                    learnerReset.resetLearning();
                    learnerResetBal.resetLearning();
                    
                    for(int r = 0; r < 2; r ++){
                	    for(int c = 0; c < 2 ; c ++){
                	    	confusionMatrixLearner[r][c] = confusionMatrixResetBal[r][c];
                	    }
                	} 
                    
                    cleanResetBal();
                	cleanReset();
                	cleanBal();
                	
            	}              	
            	 
            	batch.clear();
            	resetBatch.clear();
                drift.resetChange();
                warning = false;
                nMaj = 0;
                nMin = 0;
                resetNMaj = 0;
            	resetNMin = 0;
                	
                    
            }
            else if (drift.getWarning() && warning == false) {          	
                	
                resetBatch.add(trainInst);
                if (trainInst.classValue() == 1.0) {
                	resetNMaj ++;                       
                } else {                    	
                	resetNMin ++;                        
                }
                warning = true;
                   
            }  
                                               
        }	
        
        
        numberSamplesCorrect = confusionMatrixLearner[0][0] + confusionMatrixLearner[1][1];
    	numberSamples = confusionMatrixLearner[0][0] + confusionMatrixLearner[1][1] + confusionMatrixLearner[0][1] + confusionMatrixLearner[1][0];
        accuracy = (double) numberSamplesCorrect/ (double) numberSamples;
        acc.add(accuracy);
        
        p0 = (((double)confusionMatrixLearner[0][0] + (double)confusionMatrixLearner[0][1]) / (double) numberSamples) * (((double)confusionMatrixLearner[0][0] + (double)confusionMatrixLearner[1][0]) / (double) numberSamples);
        p1 = (((double)confusionMatrixLearner[1][0] + (double)confusionMatrixLearner[1][1]) / (double) numberSamples) * (((double)confusionMatrixLearner[0][1] + (double)confusionMatrixLearner[1][1]) / (double) numberSamples);
        pc = p0 + p1;
        k = (double)(accuracy - pc) / (double)(1 - pc);
        kStat.add(k);
        
        nElementsTest = 0;
        numberSamples = 0;
        numberSamplesCorrect = 0;
        cleanEval();
                
        for (int s = 0; s < validationDataSet.size(); s ++) {
        	        	
        	testInst = wekaToSamoa.samoaInstance(validationDataSet.instance(s));
        	
        	if (learner.correctlyClassifies(testInst) && testInst.classValue() == 0.0){
        		confusionMatrixEval[0][0] ++;
            }
            else if (learner.correctlyClassifies(testInst) && testInst.classValue() == 1.0){
                confusionMatrixEval[1][1] ++;
            }
            else if (!learner.correctlyClassifies(testInst) && testInst.classValue() == 0.0){
                confusionMatrixEval[0][1] ++;
            }
            else if (!learner.correctlyClassifies(testInst) && testInst.classValue() == 1.0){
                confusionMatrixEval[1][0] ++;
            }	
        	
        	nElementsTest ++;
        	numberSamples ++;
        	
        }    
        
        numberSamplesCorrect = confusionMatrixEval[0][0] + confusionMatrixEval[1][1];
        accuracy = (double) numberSamplesCorrect/ (double) numberSamples;
        accEval.add(accuracy);
        
        p0 = (((double)confusionMatrixEval[0][0] + (double)confusionMatrixEval[0][1]) / (double) numberSamples) * (((double)confusionMatrixEval[0][0] + (double)confusionMatrixEval[1][0]) / (double) numberSamples);
        p1 = (((double)confusionMatrixEval[1][0] + (double)confusionMatrixEval[1][1]) / (double) numberSamples) * (((double)confusionMatrixEval[0][1] + (double)confusionMatrixEval[1][1]) / (double) numberSamples);
        pc = p0 + p1;
        k = (double)(accuracy - pc) / (double)(1 - pc);
        kStatEval.add(k);
        
        modelChosen.add(modelInUse);
    	
        
        
        double time = TimingUtils.nanoTimeToSeconds(TimingUtils.getNanoCPUTimeOfCurrentThread()- evaluateStartTime);       
        System.out.println("Time train: " + time + "s");

        valToReturn.add(0, kStatEval);
        valToReturn.add(1, kStat);
        valToReturn.add(2, modelChosen);        
        valToReturn.add(3, sizeBatchReset);
        
        return valToReturn;
        
    }

	
}
