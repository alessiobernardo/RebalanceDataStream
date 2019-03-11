
import moa.classifiers.meta.TemporallyAugmentedClassifier;
import moa.classifiers.Classifier;
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
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;


public class BaseAlgorithm {	
		
	public BaseAlgorithm() {}
	
	public int[][] cleanEval(int[][] confusionMatrixEval) {
		for (int r = 0; r < 2; r++) {
			for (int c = 0; c < 2; c++) {
				confusionMatrixEval[r][c] = 0;
			}	
		}
		return confusionMatrixEval;
	}
		
	public ArrayList<ArrayList<Double>> run(InstancesHeader header, Instances train, Instances val){
		
		ArrayList<ArrayList<Double>> valToReturn = new ArrayList<ArrayList<Double>>();
		ArrayList<Double> kv = new ArrayList<Double>();
		ArrayList<Double> kp = new ArrayList<Double>();	
		ArrayList<Double> ap = new ArrayList<Double>();		
		ArrayList<Double> av = new ArrayList<Double>();
		
		int[][] confusionMatrix = new int[2][2];
		int[][] confusionMatrixEval = new int[2][2];
		
		int nModel = 1;
		int nElementsTest = 0;
		Instance testInst;
		
		WekaToSamoaInstanceConverter wekaToSamoa = new WekaToSamoaInstanceConverter();
		
        TemporallyAugmentedClassifier learner = new TemporallyAugmentedClassifier();
        learner.baseLearnerOption = new ClassOption("baseLearner", 'l', "Classifier to train.", Classifier.class, "meta.AdaptiveRandomForest");         
        learner.setModelContext(header);        
        learner.prepareForUse();
       
        int numberSamples = 0;
        int numberSamplesTest = 0;
        int numberSamplesCorrect = 0;
        Instance trainInst;
        
        double p0,p1,pc,k,accuracy;       
        long evaluateStartTime = TimingUtils.getNanoCPUTimeOfCurrentThread();
        
        for (int row = 0; row < train.numInstances(); row ++) {
    		trainInst = wekaToSamoa.samoaInstance(train.instance(row));
                           
        	/*						learner.correctlyClassifies(trainInst)
        	 						pred 0.0	pred 1.0
        	 trainInst.classValue()
        	 			correct 0.0
        	 			correct 1.0
        	 */
        	
            if (learner.correctlyClassifies(trainInst) && trainInst.classValue() == 0.0){
            	confusionMatrix[0][0] ++;
            }
            else if (learner.correctlyClassifies(trainInst) && trainInst.classValue() == 1.0){
            	confusionMatrix[1][1] ++;
            }
            else if (!learner.correctlyClassifies(trainInst) && trainInst.classValue() == 0.0){
            	confusionMatrix[0][1] ++;
            }
            else if (!learner.correctlyClassifies(trainInst) && trainInst.classValue() == 1.0){
            	confusionMatrix[1][0] ++;
            }
        
    
            learner.trainOnInstance(trainInst);
            numberSamples ++;
                           
            if (row == (nModel * 2000)) {                	
            	
            	numberSamplesCorrect = confusionMatrix[0][0] + confusionMatrix[1][1];
                accuracy = (double) numberSamplesCorrect/ (double) numberSamples;
                ap.add(accuracy);
                
                p0 = (((double)confusionMatrix[0][0] + (double)confusionMatrix[0][1]) / (double) numberSamples) * (((double)confusionMatrix[0][0] + (double)confusionMatrix[1][0]) / (double) numberSamples);
                p1 = (((double)confusionMatrix[1][0] + (double)confusionMatrix[1][1]) / (double) numberSamples) * (((double)confusionMatrix[0][1] + (double)confusionMatrix[1][1]) / (double) numberSamples);
                pc = p0 + p1;
                k = (double)(accuracy - pc) / (double)(1 - pc);
                kp.add(k);                
                
                numberSamplesTest = 0;
                numberSamplesCorrect = 0;
                confusionMatrixEval = cleanEval(confusionMatrixEval);
                                
                for (int s = 0; s < val.size(); s ++) {
                	                    	
                	testInst = wekaToSamoa.samoaInstance(val.instance(s));
                	
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
                	numberSamplesTest ++;
                	
                }
                
                numberSamplesCorrect = confusionMatrixEval[0][0] + confusionMatrixEval[1][1];
                accuracy = (double) numberSamplesCorrect/ (double) numberSamplesTest;
                av.add(accuracy);
                
                p0 = (((double)confusionMatrixEval[0][0] + (double)confusionMatrixEval[0][1]) / (double) numberSamplesTest) * (((double)confusionMatrixEval[0][0] + (double)confusionMatrixEval[1][0]) / (double) numberSamplesTest);
                p1 = (((double)confusionMatrixEval[1][0] + (double)confusionMatrixEval[1][1]) / (double) numberSamplesTest) * (((double)confusionMatrixEval[0][1] + (double)confusionMatrixEval[1][1]) / (double) numberSamplesTest);
                pc = p0 + p1;
                k = (double)(accuracy - pc) / (double)(1 - pc);
                kv.add(k);
            	
                nModel ++;                                       
                
            }

        }       
        
        numberSamplesCorrect = confusionMatrix[0][0] + confusionMatrix[1][1];
        accuracy = (double) numberSamplesCorrect/ (double) numberSamples;
        ap.add(accuracy);
        
        p0 = (((double)confusionMatrix[0][0] + (double)confusionMatrix[0][1]) / (double) numberSamples) * (((double)confusionMatrix[0][0] + (double)confusionMatrix[1][0]) / (double) numberSamples);
        p1 = (((double)confusionMatrix[1][0] + (double)confusionMatrix[1][1]) / (double) numberSamples) * (((double)confusionMatrix[0][1] + (double)confusionMatrix[1][1]) / (double) numberSamples);
        pc = p0 + p1;
        k = (double)(accuracy - pc) / (double)(1 - pc);
        kp.add(k);
        
        numberSamplesTest = 0;
        numberSamplesCorrect = 0;
        confusionMatrixEval = cleanEval(confusionMatrixEval);
                
        for (int s = 0; s < val.size(); s ++) {
        	        
        	testInst = wekaToSamoa.samoaInstance(val.instance(s));
        	
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
        	
        	numberSamplesTest ++;       	
        }
                
        numberSamplesCorrect = confusionMatrixEval[0][0] + confusionMatrixEval[1][1];
        accuracy = (double) numberSamplesCorrect/ (double) numberSamplesTest;
        av.add(accuracy);
        
        p0 = (((double)confusionMatrixEval[0][0] + (double)confusionMatrixEval[0][1]) / (double) numberSamplesTest) * (((double)confusionMatrixEval[0][0] + (double)confusionMatrixEval[1][0]) / (double) numberSamplesTest);
        p1 = (((double)confusionMatrixEval[1][0] + (double)confusionMatrixEval[1][1]) / (double) numberSamplesTest) * (((double)confusionMatrixEval[0][1] + (double)confusionMatrixEval[1][1]) / (double) numberSamplesTest);
        pc = p0 + p1;
        k = (double)(accuracy - pc) / (double)(1 - pc);
        kv.add(k);
        
        double time = TimingUtils.nanoTimeToSeconds(TimingUtils.getNanoCPUTimeOfCurrentThread()- evaluateStartTime);       
        System.out.println("Time: " + time + "s");
        
        valToReturn.add(0, kv);
        valToReturn.add(1, kp);             
        
        return valToReturn;
       
    }

}
