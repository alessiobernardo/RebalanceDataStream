import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import com.yahoo.labs.samoa.instances.Instance;
import com.yahoo.labs.samoa.instances.SamoaToWekaInstanceConverter;
import com.yahoo.labs.samoa.instances.WekaToSamoaInstanceConverter;

import weka.core.Attribute;
import weka.core.Instances;

public class DatasetManagement {
	
	private static Instances createRandomInstances() {
		ArrayList<Attribute> atts = new ArrayList<Attribute>();
		ArrayList<String> label = new ArrayList<String>();

		for (int i = 1; i < 11; i ++) {
			atts.add(new Attribute("att" + i));
	
			if (i < 3) {
				label.add(Integer.toString(i-1));
			}		
	
		}
		atts.add(new Attribute("label",label));

		Instances data = new Instances("Dataset",atts,0);
		return data;
	}	
	
	public ArrayList<Instances> splitDataset(ArrayList<Instance> d) {
		
		ArrayList<Instances> splittedDataset = new ArrayList<Instances>();
		
		ArrayList<Integer> alreadyUsed = new ArrayList<Integer>();             
        ArrayList<Instance> validation = new ArrayList<Instance>();
        HashMap<Integer, Instance> posInst = new HashMap<Integer, Instance>();
        
        int pos;
        Random ran = new Random();
        ran.setSeed(10);
        
        Instances train;
        Instances val;
        SamoaToWekaInstanceConverter samoaToWeka = new SamoaToWekaInstanceConverter();        
        
        for (int a = 0; a < 10000; a ++) {
        	pos = ran.nextInt(d.size());
        	while (alreadyUsed.contains(pos)) {
        		pos = ran.nextInt(d.size());
        	}
        	alreadyUsed.add(pos);
        	posInst.put(pos, d.get(pos));        	        	
        }
        
        Map<Integer, Instance> treeMap = new TreeMap<>(posInst);         
        for (Integer i : treeMap.keySet()) {
            validation.add(treeMap.get(i));
        }                
        
        // sort the list
        Collections.sort(alreadyUsed);
            
        for (int a = alreadyUsed.size()-1 ; a >= 0; a--) {
        	d.remove( (int) alreadyUsed.get(a));
        }                
        
        train = createRandomInstances();
        val = createRandomInstances();
        
        for (int l = 0; l < d.size(); l ++) {
        	train.add(samoaToWeka.wekaInstance(d.get(l)));                    	
        }
        
        for (int l = 0; l < validation.size(); l ++) {
        	val.add(samoaToWeka.wekaInstance(validation.get(l)));                    	
        }
        
        train.setClassIndex(train.numAttributes() - 1);
      	val.setClassIndex(val.numAttributes() - 1);      	      	
      	
      	splittedDataset.add(0, train);
      	splittedDataset.add(1, val);
      	
      	return splittedDataset;
      	
		
	}

}
