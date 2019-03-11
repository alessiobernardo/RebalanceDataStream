import java.util.ArrayList;

import com.yahoo.labs.samoa.instances.InstancesHeader;

import weka.core.Instances;

public class ModelLearning {
	
	public ArrayList<ArrayList<Double>> rebalanceStreamResults = new ArrayList<ArrayList<Double>>();
	public ArrayList<ArrayList<Double>> baseResults = new ArrayList<ArrayList<Double>>();

	public ArrayList<ArrayList<Double>> getRebalanceStreamResults() {
		return rebalanceStreamResults;
	}

	public void setRebalanceStreamResults(ArrayList<ArrayList<Double>> rebalanceStreamResults) {
		this.rebalanceStreamResults = rebalanceStreamResults;
	}

	public ArrayList<ArrayList<Double>> getBaseResults() {
		return baseResults;
	}

	public void setBaseResults(ArrayList<ArrayList<Double>> baseResults) {
		this.baseResults = baseResults;
	}
	
	public void learnModels(InstancesHeader header, Instances train, Instances val) throws Exception {
		
		RebalanceStreamAlgorithm rebalanceStream = new RebalanceStreamAlgorithm();	    	    	    
	    BaseAlgorithm base = new BaseAlgorithm();
	    
	    rebalanceStreamResults.clear();
	    baseResults.clear();
	    
	    setRebalanceStreamResults(rebalanceStream.run(header,train,val));
	    setBaseResults(base.run(header,train,val));
	    	    
	}
}
