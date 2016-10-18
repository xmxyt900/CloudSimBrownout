package com.cloudbus.cloudsim.examples.power.brownout;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.cloudbus.cloudsim.Log;

/**
 * A simulation of a heterogeneous power aware data center that only applied DVFS, but no dynamic
 * optimization of the VM allocation. The adjustment of the hosts' power consumption according to
 * their CPU utilization is happening in the PowerDatacenter class.
 * 
 * The remaining configuration parameters are in the Constants and RandomConstants classes.
 * 
 * If you are using any algorithms, policies or workload included in the power package please cite
 * the following paper:
 * 
 * Anton Beloglazov, and Rajkumar Buyya, "Optimal Online Deterministic Algorithms and Adaptive
 * Heuristics for Energy and Performance Efficient Dynamic Consolidation of Virtual Machines in
 * Cloud Data Centers", Concurrency and Computation: Practice and Experience (CCPE), Volume 24,
 * Issue 13, Pages: 1397-1420, John Wiley & Sons, Ltd, New York, USA, 2012
 * 
 * @author Anton Beloglazov
 * @since Jan 5, 2012
 */
public class Brownout {

	/**
	 * The main method.
	 * 
	 * @param args the arguments
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	
	public static long MARKOV_SEED = 1000l;
//	public static ArrayList<Double> ENERGY_EFFICIENCY_COLLECTOR = new ArrayList<Double>();
	public static HashMap<Long, Double> ENERGY_EFFICIENCY_COLLECTOR1 = new HashMap<Long, Double>();
	public static double minimumEnergyEfficiencyResult = 10.0;
	public static long seedOfMinimumEnergyEfficiencyResult = 0l;
	public static int brownoutPolicyId = 0;
	public static int workloadId = 0;
	
	public static void main(String[] args) throws IOException {
		boolean enableOutput = true;
		boolean outputToFile = true;
		String inputFolder = "";
		String outputFolder = "output";
		String workload = "random"; // Random workload
		String vmAllocationPolicy = "thr"; // DVFS policy without VM migrations thr (static threshold) and ubp (utilization based probability)
		String vmSelectionPolicy = "mu";
		String parameter = "0.8";
		
		
        for(workloadId = 0; workloadId < 1; workloadId++){
        	
		//Testing for markov decision process with iterations
		for(brownoutPolicyId = 0; brownoutPolicyId < 4; brownoutPolicyId++) {
			System.out.println("\n");
			System.out.println("\n Workload File ID:" + workloadId + " Policy Id: " + brownoutPolicyId);
//			MARKOV_SEED += (long) i;
		new BrownoutRunner(
				enableOutput,				outputToFile,
				inputFolder,
				outputFolder,
				workload,
				vmAllocationPolicy,
				vmSelectionPolicy,
				parameter);
	}
	}
	}
	

}
