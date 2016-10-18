package com.cloudbus.cloudsim.examples.power.brownout;

import java.io.BufferedReader;
import java.io.FileReader;

public class OverloadedTraceGenerator {
	
    public static void main(String[] args){	
 
		
	try {  
		FileReader fileReader;  
    	BufferedReader bufferedReader;
	    fileReader = new FileReader("./src/examples/workload/planetlab");  
	    bufferedReader = new BufferedReader(fileReader);  
	    String line = "";  
	    while((line = bufferedReader.readLine()) != null){    
	        System.out.println(line);  
	        }  
	    
		bufferedReader.close();  
		fileReader.close(); 
	    } catch (Exception e) {  
	        e.printStackTrace();  
	}  
 
}
}

