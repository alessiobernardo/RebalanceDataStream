import java.util.ArrayList;

public class ResultsAggregator {
	
	public ArrayList<ArrayList<Double>> createFinalAlgorithm(ArrayList<ArrayList<Double>> rebalanceStreamVal, ArrayList<ArrayList<Double>> rebalanceStreamTest, 
															 ArrayList<ArrayList<Double>> baseVal, ArrayList<ArrayList<Double>> baseTest){
		
		ArrayList<ArrayList<Double>> finalResults = new ArrayList<ArrayList<Double>>();
	    ArrayList<Double> stream = new ArrayList<Double>();
	    
	    ArrayList<Double> resBaseVal = new ArrayList<Double>();
	    ArrayList<Double> resBaseTest = new ArrayList<Double>();
	    
	    ArrayList<Double> resRebalanceStreamVal = new ArrayList<Double>();	    
	    ArrayList<Double> resRebalanceStreamTest = new ArrayList<Double>();
		
		for (int k = 0; k < 5; k++) {
			
			stream.clear();
			
			resRebalanceStreamVal = rebalanceStreamVal.get(k); 
			resRebalanceStreamTest = rebalanceStreamTest.get(k); 
			
			resBaseVal = baseVal.get(k); 
			resBaseTest = baseTest.get(k); 
			
			for (int i = 0; i < resRebalanceStreamVal.size(); i++) {
				if (resRebalanceStreamTest.get(i) >= resBaseTest.get(i)) {
					stream.add(resRebalanceStreamVal.get(i)); //I use the val
//					stream.add(resRebalanceStreamTest.get(i)); //I use the preq
				}
				else {
					stream.add(resBaseVal.get(i)); //I use the val
//					stream.add(resBaseTest.get(i)); //I use the preq
				}
			}
			finalResults.add((ArrayList<Double>) stream.clone()); 
		}
		
		return finalResults;
	}
	
	public ArrayList<ArrayList<Double>> verticalMean(ArrayList<ArrayList<Double>> results){
		
		ArrayList<ArrayList<Double>> valToReturn = new ArrayList<ArrayList<Double>>();
		ArrayList<Double> means = new ArrayList<Double>();
	    ArrayList<Double> mins = new ArrayList<Double>();
	    ArrayList<Double> maxs = new ArrayList<Double>();
	    
	    ArrayList<Double> result = new ArrayList<Double>();	    
	    double mean,max,min;	    	 
		
		for (int i = 0; i < results.get(0).size(); i++) {			
			mean = 0;
			min = 2;
			max = -1;						

			for (int k = 0; k < 5; k++) { 							
			
				result = results.get(k);																
				mean += result.get(i);							 				

				if (result.get(i) < min) {
					min = result.get(i);
				}
				if (result.get(i) > max) {
					max = result.get(i);
				}			
			}
			
			mean /= 5;
			means.add(mean);
			mins.add(min);
			maxs.add(max);					
		}

	    valToReturn.add(0, means);
	    valToReturn.add(1, mins);
	    valToReturn.add(2, maxs);
	    
	    return valToReturn;
		
	}
		
	
}
