package workload.planetlab;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

import com.cloudbus.cloudsim.examples.power.brownout.Brownout;

public class OverloadedTraceGenerator {
	
//    public static void main(String[] args){	
	 ArrayList<Double> overloadedArray = new ArrayList<Double>();
	 
	 {
	 
	try {
	   

		FileReader fileReader;  
    	BufferedReader bufferedReader;

	    fileReader = new FileReader("./cloudsim-3.0.3/examples/workload/planetlab/overloadedTrace" + Brownout.workloadId); 
	    bufferedReader = new BufferedReader(fileReader);  
	    String line = "";  
	    while((line = bufferedReader.readLine()) != null){    
	        overloadedArray.add(Double.parseDouble(line));
	        }  
	    
		bufferedReader.close();  
		fileReader.close(); 
	    } catch (Exception e) {  
	        e.printStackTrace();  
	    }  
//	System.out.println(overloadedArray);

}
	 
	 public ArrayList<Double> getOverloadedTrace(){
		 return overloadedArray;
	 }
}

