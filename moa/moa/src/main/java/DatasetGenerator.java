import java.util.ArrayList;
import java.util.Random;
import com.yahoo.labs.samoa.instances.Instance;
import com.yahoo.labs.samoa.instances.InstancesHeader;

import moa.streams.ImbalancedStream;

public class DatasetGenerator {

	public InstancesHeader streamHeader = new InstancesHeader();
	public ArrayList<Double> levelImbalancement = new ArrayList<Double>();
	
	public ArrayList<Double> getLevelImbalancement() {
		return levelImbalancement;
	}

	public void setLevelImbalancement(ArrayList<Double> levelImbalancement) {
		this.levelImbalancement = levelImbalancement;
	}

	public InstancesHeader getStreamHeader() {
		return streamHeader;
	}

	public void setStreamHeader(InstancesHeader streamHeader) {		
		this.streamHeader = streamHeader;
	}

	public ArrayList<Instance> createDataset(int mean, int var, int seed) {
				
		ArrayList<Instance> d = new ArrayList<Instance>();	
		ArrayList<Double> level = new ArrayList<Double>();
		
		ArrayList<String> balance = new ArrayList<>();
		balance.add("0.9;0.1");
		balance.add("0.8;0.2");
		balance.add("0.7;0.3");
		balance.add("0.6;0.4");
		
		int nElements = 1;
		int nEl;
        int nElPrec = 0;
        int pos;
		Instance inst;						
							
		ImbalancedStream stream = new ImbalancedStream();
		stream.streamOption.setValueViaCLIString("generators.RandomRBFGeneratorDrift -n 50 -c 2 -a 10 -s 0.0000001 -i " + seed);	        
		
		Random ran = new Random();
        ran.setSeed(10);
        pos = ran.nextInt(4);
        stream.classRatioOption.setValue(balance.get(pos));
        nEl = (int) Math.round(ran.nextGaussian()*var+mean);                		
        stream.prepareForUse();
        
        while (stream.hasMoreInstances() && nElements < 100001) {
        	inst = (Instance) stream.nextInstance().getData();
        	d.add(inst);
        	
        	if (pos == 0) {
        		level.add(0.1);
        	} 
        	else if (pos == 1) {
        		level.add(0.2);
        	}
        	else if (pos == 2) {
        		level.add(0.3);
        	}
        	else {
        		level.add(0.4);
        	}
        	
        	if ( (nElements - nElPrec) == nEl) {
            	nElPrec += nEl;
            	pos = ran.nextInt(4);
                stream.classRatioOption.setValue(balance.get(pos));
                stream.prepareForUse();
                nEl = (int) Math.round(ran.nextGaussian()*var+mean);                                                     
            }
        	
        	nElements ++;	       	
        }
	        		
		setStreamHeader(stream.getHeader());
		setLevelImbalancement(level);
		return d;
	}
}
