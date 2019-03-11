import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import com.yahoo.labs.samoa.instances.Instance;

import weka.core.Instances;

public class BenchmarkManager {
	
	private static double createValueMatrix(ArrayList<Double> baseMean, ArrayList<Double> otherMean){
	
		double mean;						
		ArrayList<Double> diff = new ArrayList<Double>();										
			
		for (int i = 0; i < otherMean.size(); i++) {
			diff.add(otherMean.get(i) - baseMean.get(i));
		}
		
		mean = 0;
		for (int i = 0; i < diff.size(); i++) {
			mean += diff.get(i);
		}			
		mean /= diff.size();
		
		return mean;
				
	}
	
	private static void printMatrix(double[][] matrix, String title) {
		
		//PRINT matrix
	    try(FileWriter fw = new FileWriter("grafici/model.txt", true);
	    	BufferedWriter bw = new BufferedWriter(fw)) {	  						 
		  	
		  	bw.write("*** MATRIX " + title + " ***\n");
		  	for (int r = 0; r < matrix.length; r ++) {	    
		    	for (int c = 0; c < matrix[r].length ; c++) {
		    		bw.write(matrix[r][c] + "  ");
		    	}
		    	bw.write("\n");
		    }
		  	bw.write("\n");
	  			    
	    } catch (IOException ex) {
		  ex.printStackTrace();
	    }
		
	}
	
	private static void printModelAndSize(int mean, int var, ArrayList<ArrayList<Double>> rebalanceStreamModel, ArrayList<ArrayList<Double>> rebalanceStreamSizeBatch){
		
		ArrayList<Double> stream = new ArrayList<Double>();
		
		try(FileWriter fw = new FileWriter("grafici/model.txt", true);
			BufferedWriter bw = new BufferedWriter(fw)) {
				
				bw.write("*** Mean: " + mean + ", Variance: " + var +" ***\n");				
				for(int k = 0; k < 5; k ++) {					
					bw.write("*** Stream: " + (k+1) + " ***\n");
					
					//PRINT MODEL CHOOSEN
					stream = rebalanceStreamModel.get(k);
					bw.write("*** Model choosen ***\n");
					for (int i = 0; i < stream.size(); i ++) {
						if (stream.get(i) == 0.0) {
							bw.write("Normal, ");
						}
						else if (stream.get(i) == 1.0) {
							bw.write("Balance, ");
						}
						else if (stream.get(i) == 2.0) {
							bw.write("Reset, ");
						}
						else if (stream.get(i) == 3.0) {
							bw.write("ResetBalance, ");
						}
					}
					bw.write("\n");
				
					//PRINT SIZE OF BATCH
					stream = rebalanceStreamSizeBatch.get(k);
					bw.write("*** Size batchReset ***\n");
					for (int i = 0; i < stream.size(); i ++) {						
						bw.write(Double.toString(stream.get(i)) + "  ");						
					}
					bw.write("\n");
				}
				bw.write("\n");
				bw.write("\n");											
			    
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	private static void runBenchmark() {
		
		ArrayList<Instance> dataset = new ArrayList<Instance>();				
		ArrayList<Instances> splittedDataset = new ArrayList<Instances>();
		Instances trainingDataSet;
        Instances validationDataSet;
        
        ArrayList<ArrayList<Double>> rebalanceStreamResults = new ArrayList<ArrayList<Double>>();
        ArrayList<ArrayList<Double>> rebalanceStreamVal = new ArrayList<ArrayList<Double>>();
	    ArrayList<ArrayList<Double>> rebalanceStreamTest = new ArrayList<ArrayList<Double>>();
	    ArrayList<ArrayList<Double>> rebalanceStreamModel = new ArrayList<ArrayList<Double>>();
	    ArrayList<ArrayList<Double>> rebalanceStreamSizeBatch = new ArrayList<ArrayList<Double>>();
	    ArrayList<Double> rebalanceStreamMean = new ArrayList<Double>();
	    ArrayList<Double> rebalanceStreamMin = new ArrayList<Double>();
	    ArrayList<Double> rebalanceStreamMax = new ArrayList<Double>();		   
	    ArrayList<ArrayList<Double>> rebalanceStreamActualResults = new ArrayList<ArrayList<Double>>();
	    
    	ArrayList<ArrayList<Double>> baseResults = new ArrayList<ArrayList<Double>>();
    	ArrayList<ArrayList<Double>> baseVal = new ArrayList<ArrayList<Double>>();
	    ArrayList<ArrayList<Double>> baseTest = new ArrayList<ArrayList<Double>>();
	    ArrayList<Double> baseMean = new ArrayList<Double>();
	    ArrayList<Double> baseMin = new ArrayList<Double>();
	    ArrayList<Double> baseMax = new ArrayList<Double>();	    
	    ArrayList<ArrayList<Double>> baseActualResults = new ArrayList<ArrayList<Double>>();
		
	    ArrayList<ArrayList<Double>> finalResults = new ArrayList<ArrayList<Double>>();
	    ArrayList<Double> finalMean = new ArrayList<Double>();
	    ArrayList<Double> finalMin = new ArrayList<Double>();
	    ArrayList<Double> finalMax = new ArrayList<Double>();	    
	    ArrayList<ArrayList<Double>> finalActualResults = new ArrayList<ArrayList<Double>>();
	    
	    ArrayList<ArrayList<Double>> verticalMean = new ArrayList<ArrayList<Double>>();
	    
	    
	    ArrayList<ArrayList<Double>> levelsImbalancement = new ArrayList<ArrayList<Double>>();
	    ArrayList<Double> levelMean = new ArrayList<Double>();
	    ArrayList<Double> levelMin = new ArrayList<Double>();
	    ArrayList<Double> levelMax = new ArrayList<Double>();
	    ArrayList<ArrayList<Double>> levelActualResults = new ArrayList<ArrayList<Double>>();
	    
	    double[][] RebalanceStreamBaseMatrix = new double[5][4];
		double[][] FinalBaseMatrix = new double[5][4];
	    int row = 0;
	    int col = 0;

	    ConfigurationGenerator cg = new ConfigurationGenerator();
		DatasetGenerator dg = new DatasetGenerator();
		DatasetManagement dm = new DatasetManagement();
		ModelLearning ml = new ModelLearning();
		ResultsAggregator ra = new ResultsAggregator();
		ResultsVisualizator rv = null;		
						
		ArrayList<Integer> configs = cg.createConfiguration();
		
		//foreach configurations
		for (int c = 0; c < configs.size(); c += 2) {
	    	System.out.println("*** Mean: " + configs.get(c) + " Var: " + configs.get(c+1) + " ***");	    		    
	    	
	    	dataset.clear();
	    	
	    	rebalanceStreamVal.clear();
    		rebalanceStreamTest.clear();
    		rebalanceStreamModel.clear();	
    		rebalanceStreamSizeBatch.clear();
    		
    		baseVal.clear();
    		baseTest.clear();
    		
    		levelsImbalancement.clear();
    		    		
    		for (int i = 3; i < 8; i++) {
    			System.out.println("*** stream " + Integer.toString(i - 2) + " ***");
    			
    			//dataset generation
    			dataset = dg.createDataset(configs.get(c), configs.get(c+1),i);
    			levelsImbalancement.add(dg.getLevelImbalancement());
    			
    			//dataset split
    			splittedDataset = dm.splitDataset(dataset);
	    		trainingDataSet = splittedDataset.get(0);	    		
	    		validationDataSet = splittedDataset.get(1);	    			    		
    		
	    		//models training
	    		try {
					ml.learnModels(dg.getStreamHeader(), trainingDataSet, validationDataSet);
					
					rebalanceStreamResults.clear();
					baseResults.clear();
					
					rebalanceStreamResults = ml.getRebalanceStreamResults();
					baseResults = ml.getBaseResults();
					
					rebalanceStreamVal.add(rebalanceStreamResults.get(0));
		    		rebalanceStreamTest.add(rebalanceStreamResults.get(1));
		    		rebalanceStreamModel.add(rebalanceStreamResults.get(2));	
		    		rebalanceStreamSizeBatch.add(rebalanceStreamResults.get(3));
		    		
		    		baseVal.add(baseResults.get(0));
		    		baseTest.add(baseResults.get(1));	
		    		
				} catch (Exception e) {					
					e.printStackTrace();
				}	    	
	    			    		
			}
	    	
	    	finalResults.clear();
	    	//final algorithm
	    	finalResults = ra.createFinalAlgorithm(rebalanceStreamVal, rebalanceStreamTest, baseVal, baseTest);
	    	
	    	//vertical mean RebalanceStream algorithm
	    	verticalMean.clear();
	    	rebalanceStreamMean.clear();
	    	rebalanceStreamMin.clear();
	    	rebalanceStreamMax.clear();
	    	//I use the val
	    	verticalMean = ra.verticalMean(rebalanceStreamVal);
	    	//I use the preq
//	    	verticalMean = ra.verticalMean(rebalanceStreamTest);
	    	rebalanceStreamMean = verticalMean.get(0);
	    	rebalanceStreamMin = verticalMean.get(1);
	    	rebalanceStreamMax = verticalMean.get(2);	    		    	
	    	
	    	//vertical mean base algorithm
	    	verticalMean.clear();
	    	baseMean.clear();
	    	baseMin.clear();
	    	baseMax.clear();
	    	//I use the val
	    	verticalMean = ra.verticalMean(baseVal);
	    	//I use the preq
//	    	verticalMean = ra.verticalMean(baseTest);
	    	baseMean = verticalMean.get(0);
	    	baseMin = verticalMean.get(1);
	    	baseMax = verticalMean.get(2);	    		    	
	    	
	    	//vertical mean final algorithm
	    	verticalMean.clear();
	    	finalMean.clear();
	    	finalMin.clear();
	    	finalMax.clear();	    	
	    	verticalMean = ra.verticalMean(finalResults); 
	    	finalMean = verticalMean.get(0);
	    	finalMin = verticalMean.get(1);
	    	finalMax = verticalMean.get(2);
	    	
	    	//vertical mean imbalancement level
	    	verticalMean.clear();
	    	levelMean.clear();
	    	levelMin.clear();
	    	levelMax.clear();
	    	verticalMean = ra.verticalMean(levelsImbalancement);
	    	levelMean = verticalMean.get(0);
	    	levelMin = verticalMean.get(1);
	    	levelMax = verticalMean.get(2);	        	
	    	
	    	// matrix creation
	    	RebalanceStreamBaseMatrix[row][col] = createValueMatrix(baseMean,rebalanceStreamMean);
	    	FinalBaseMatrix[row][col] = createValueMatrix(baseMean,finalMean);
			
			if (col == 3) {
	    		row ++;
	    		col = 0;
	    	}
	    	else {
	    		col ++;
	    	}	   
	    	
	    	
	    	//create line charts
	    	rebalanceStreamActualResults.clear();
	    	baseActualResults.clear();
	    	finalActualResults.clear();
	    	levelActualResults.clear();
	    	
	    	rebalanceStreamActualResults.add(0, rebalanceStreamMean);
	    	rebalanceStreamActualResults.add(1, rebalanceStreamMin);
	    	rebalanceStreamActualResults.add(2, rebalanceStreamMax);
	    	
	    	baseActualResults.add(0, baseMean);
	    	baseActualResults.add(1, baseMin);
	    	baseActualResults.add(2, baseMax);
	    	
	    	finalActualResults.add(0, finalMean);
	    	finalActualResults.add(1, finalMin);
	    	finalActualResults.add(2, finalMax);
	    	
	    	levelActualResults.add(0, levelMean);
	    	levelActualResults.add(1, levelMin);
	    	levelActualResults.add(2, levelMax);
	    	
	    	rv = new ResultsVisualizator(rebalanceStreamActualResults, baseActualResults, finalActualResults, levelActualResults, configs.get(c), configs.get(c+1));	
	    	
	    	printModelAndSize(configs.get(c), configs.get(c+1), rebalanceStreamModel, rebalanceStreamSizeBatch);
			
		}

		//print and create heatmap base-RebalanceStream
		printMatrix(RebalanceStreamBaseMatrix,"RebalanceStream-Base");
		new HeatmapGenerator(RebalanceStreamBaseMatrix,"RebalanceStream-Base");

		//print and create heatmap base-final
		printMatrix(FinalBaseMatrix,"Final-Base");
		new HeatmapGenerator(FinalBaseMatrix,"Final-Base");
		
	}
	
	public static void main(String[] args) {

		System.out.println("*** BEGIN ***");
		runBenchmark();
		System.out.println("*** END ***");		
	}

}
