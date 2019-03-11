import java.util.ArrayList;

public class ConfigurationGenerator {
	  
	public ArrayList<Integer> createConfiguration() {
		
		int[] m = {20000,22500,25000,27500,30000};
	    int[] v = {50,100,200,400};
	    
		//CONFIGURATION
	    ArrayList<Integer> configs = new ArrayList<Integer>();	   	   
	    
	    for (int r = 0; r < m.length; r ++) {
	    	for (int c = 0; c < v.length; c ++) {	    	
	    		configs.add(m[r]);
	    		configs.add(v[c]);	    		
	    	}
	    }
	    
	    return configs;
	}

}
