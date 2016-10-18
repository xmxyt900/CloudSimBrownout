/*
 *
 */
package com.cloudbus.cloudsim.examples.power.brownout;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.CloudletOptionalComponent;
import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.UtilizationModelDimmer;
import org.cloudbus.cloudsim.UtilizationModelNull;
import org.cloudbus.cloudsim.UtilizationModelStochastic;
import org.cloudbus.cloudsim.examples.power.Constants;

/**
 * The Helper class for the random workload.
 * 
 * If you are using any algorithms, policies or workload included in the power
 * package please cite the following paper:
 * 
 * Anton Beloglazov, and Rajkumar Buyya, "Optimal Online Deterministic
 * Algorithms and Adaptive Heuristics for Energy and Performance Efficient
 * Dynamic Consolidation of Virtual Machines in Cloud Data Centers", Concurrency
 * and Computation: Practice and Experience (CCPE), Volume 24, Issue 13, Pages:
 * 1397-1420, John Wiley & Sons, Ltd, New York, USA, 2012
 * 
 * @author Anton Beloglazov
 * @since Jan 5, 2012
 */
public class BrownoutHelper {

	/**
	 * Creates the cloudlet list.
	 * 
	 * @param brokerId
	 *            the broker id
	 * @param cloudletsNumber
	 *            the cloudlets number
	 * 
	 * @return the list< cloudlet>
	 */
	public static List<Cloudlet> createCloudletList(int brokerId, int cloudletsNumber) {
		List<Cloudlet> list = new ArrayList<Cloudlet>();

		long fileSize = 300;
		long outputSize = 300;
		long seed = BrownoutConstants.CLOUDLET_UTILIZATION_SEED;
		UtilizationModel utilizationModelNull = new UtilizationModelNull();
		List<CloudletOptionalComponent> cloudletOptionalComponentList = new ArrayList<CloudletOptionalComponent>();
		double conpar = 1.0 * (1 - DimmerConstants.DimmerComponentLowerThreshold) / 0.5;


//		CloudletOptionalComponent coc5 = new CloudletOptionalComponent(1, 0.6 * conpar, 0.6 * conpar, true, "tag1");
//		cloudletOptionalComponentList.add(coc5);		
	
//		CloudletOptionalComponent coc5 = new CloudletOptionalComponent(1, 0.1 * conpar, 0.1 * conpar, true, "tag1");
//		cloudletOptionalComponentList.add(coc5);
//		
//		CloudletOptionalComponent coc6 = new CloudletOptionalComponent(2, 0.2 * conpar, 0.3 * conpar, true, "tag2");
//		cloudletOptionalComponentList.add(coc6);
////		
//		CloudletOptionalComponent coc8 = new CloudletOptionalComponent(3, 0.3 * conpar, 0.2 * conpar, true, "tag3");
//		cloudletOptionalComponentList.add(coc8);		
		
//		CloudletOptionalComponent coc5 = new CloudletOptionalComponent(1, 0.04 * conpar, 0.04 * conpar, true, "tag1");
//		cloudletOptionalComponentList.add(coc5);
//		
//		CloudletOptionalComponent coc6 = new CloudletOptionalComponent(2, 0.06 * conpar, 0.06 * conpar, true, "tag2");
//		cloudletOptionalComponentList.add(coc6);
////		
//		CloudletOptionalComponent coc8 = new CloudletOptionalComponent(3, 0.08 * conpar, 0.12 * conpar, true, "tag3");
//		cloudletOptionalComponentList.add(coc8);
//		
//		CloudletOptionalComponent coc1 = new CloudletOptionalComponent(1, 0.12 * conpar, 0.08 * conpar, true, "tag4");
//		cloudletOptionalComponentList.add(coc1);
//		
//		CloudletOptionalComponent coc2 = new CloudletOptionalComponent(2, 0.12 * conpar, 0.08 * conpar, true, "tag5");
//		cloudletOptionalComponentList.add(coc2);
////		
//		CloudletOptionalComponent coc3 = new CloudletOptionalComponent(3, 0.18 * conpar, 0.12 * conpar, true, "tag6");
//		cloudletOptionalComponentList.add(coc3);
		
//		CloudletOptionalComponent coc5 = new CloudletOptionalComponent(1, 0.024 * conpar, 0.024 * conpar, true, "tag1");
//		cloudletOptionalComponentList.add(coc5);
//		
//		CloudletOptionalComponent coc6 = new CloudletOptionalComponent(2, 0.036 * conpar, 0.036 * conpar, true, "tag2");
//		cloudletOptionalComponentList.add(coc6);
////		
//		CloudletOptionalComponent coc8 = new CloudletOptionalComponent(3, 0.016 * conpar, 0.016 * conpar, true, "tag33");
//		cloudletOptionalComponentList.add(coc8);
//		
//		CloudletOptionalComponent coc1 = new CloudletOptionalComponent(4, 0.024 * conpar, 0.024 * conpar, true, "tag4");
//		
//		
//		cloudletOptionalComponentList.add(coc1);
//		
//		CloudletOptionalComponent coc2 = new CloudletOptionalComponent(5, 0.032 * conpar, 0.048 * conpar, true, "tag33");
//		cloudletOptionalComponentList.add(coc2);
////		
//		CloudletOptionalComponent coc3 = new CloudletOptionalComponent(6, 0.048 * conpar, 0.072 * conpar, true, "tag6");
//		cloudletOptionalComponentList.add(coc3);
//		
//		CloudletOptionalComponent coc55 = new CloudletOptionalComponent(7, 0.048 * conpar, 0.072 * conpar, true, "tag55");
//		cloudletOptionalComponentList.add(coc55);
//		
//		CloudletOptionalComponent coc66 = new CloudletOptionalComponent(8, 0.072 * conpar, 0.108 * conpar, true, "tag66");
//		
//		
//		cloudletOptionalComponentList.add(coc66);
		
		CloudletOptionalComponent coc5 = new CloudletOptionalComponent(1, 0.15 * conpar, 0.12 * conpar, true, "tag1");
		cloudletOptionalComponentList.add(coc5);
		
		CloudletOptionalComponent coc6 = new CloudletOptionalComponent(2, 0.12 * conpar, 0.08 * conpar, true, "tag2");
		cloudletOptionalComponentList.add(coc6);
//		
		CloudletOptionalComponent coc8 = new CloudletOptionalComponent(3, 0.1 * conpar, 0.1 * conpar, true, "tag3");
		cloudletOptionalComponentList.add(coc8);
		
		CloudletOptionalComponent coc1 = new CloudletOptionalComponent(4, 0.08 * conpar, 0.09 * conpar, true, "tag4");
		
		
		cloudletOptionalComponentList.add(coc1);
		
		CloudletOptionalComponent coc2 = new CloudletOptionalComponent(5, 0.05 * conpar, 0.11 * conpar, true, "tag5");
		cloudletOptionalComponentList.add(coc2);
//		
//		CloudletOptionalComponent coc3 = new CloudletOptionalComponent(6, 0.1 * conpar, 0.1* conpar, true, "tag6");
//		cloudletOptionalComponentList.add(coc3);
//		
//		CloudletOptionalComponent coc55 = new CloudletOptionalComponent(7, 0.048 * conpar, 0.072 * conpar, true, "tag55");
//		cloudletOptionalComponentList.add(coc55);
//		
//		CloudletOptionalComponent coc66 = new CloudletOptionalComponent(8, 0.072 * conpar, 0.108 * conpar, true, "tag66");
//		
//		
//		cloudletOptionalComponentList.add(coc66);
		
		
//		
//		CloudletOptionalComponent coc88 = new CloudletOptionalComponent(9, 0.048 * conpar, 0.032 * conpar, true, "tag33");
//		cloudletOptionalComponentList.add(coc88);
//		
//		CloudletOptionalComponent coc11 = new CloudletOptionalComponent(1, 0.072 * conpar, 0.048 * conpar, true, "tag11");
//		cloudletOptionalComponentList.add(coc11);
//		
//		CloudletOptionalComponent coc22 = new CloudletOptionalComponent(2, 0.072 * conpar, 0.048 * conpar, true, "tag22");
//		cloudletOptionalComponentList.add(coc22);
////		
//		CloudletOptionalComponent coc33 = new CloudletOptionalComponent(3, 0.108 * conpar, 0.072 * conpar, true, "tag33");
//		cloudletOptionalComponentList.add(coc33);
		
//		CloudletOptionalComponent coc5 = new CloudletOptionalComponent(1, 0.8, 0.8, true, "tag1");
//		cloudletOptionalComponentList.add(coc5);
		
//		CloudletOptionalComponent coc6 = new CloudletOptionalComponent(2, 0.16666, 0.24999, true, "tag2");
//		cloudletOptionalComponentList.add(coc6);
////		
//		CloudletOptionalComponent coc8 = new CloudletOptionalComponent(3, 0.24999, 0.16666, true, "tag3");
//		cloudletOptionalComponentList.add(coc8);
		
	
	/**Test case # */	
//		CloudletOptionalComponent coc5 = new CloudletOptionalComponent(1, 0.02, 0.02, true, "tag1");
//		cloudletOptionalComponentList.add(coc5);
//		
//		CloudletOptionalComponent coc6 = new CloudletOptionalComponent(2, 0.04, 0.06, true, "tag2");
//		cloudletOptionalComponentList.add(coc6);
////		
//		CloudletOptionalComponent coc8 = new CloudletOptionalComponent(3, 0.06, 0.04, true, "tag3");
//		cloudletOptionalComponentList.add(coc8);
//		
//		CloudletOptionalComponent coc1 = new CloudletOptionalComponent(4, 0.08, 0.1, true, "tag4");
//		cloudletOptionalComponentList.add(coc1);
//		
//		CloudletOptionalComponent coc2 = new CloudletOptionalComponent(5, 0.1, 0.08, true, "tag5");
//		cloudletOptionalComponentList.add(coc2);
		
		
		
/** Test Case Type 1*/
//		CloudletOptionalComponent coc5 = new CloudletOptionalComponent(1, 0.06, 0.05, true, "tag1");
//		cloudletOptionalComponentList.add(coc5);
//		
//		CloudletOptionalComponent coc6 = new CloudletOptionalComponent(2, 0.11, 0.14, true, "tag2");
//		cloudletOptionalComponentList.add(coc6);
////		
//		CloudletOptionalComponent coc8 = new CloudletOptionalComponent(3, 0.12, 0.11, true, "tag3");
//		cloudletOptionalComponentList.add(coc8);
//		
//		CloudletOptionalComponent coc1 = new CloudletOptionalComponent(4, 0.13, 0.12, true, "tag4");
//		cloudletOptionalComponentList.add(coc1);
//		
//		CloudletOptionalComponent coc2 = new CloudletOptionalComponent(5, 0.18, 0.18, true, "tag5");
//		cloudletOptionalComponentList.add(coc2);
//		
		
		
		
/** Test Case Type 1*/
		
//		CloudletOptionalComponent coc5 = new CloudletOptionalComponent(1, 0.03, 0.03, true, "tag1");
//		cloudletOptionalComponentList.add(coc5);
//		
//		CloudletOptionalComponent coc6 = new CloudletOptionalComponent(2, 0.05, 0.04, true, "tag2");
//		cloudletOptionalComponentList.add(coc6);
//////		
//		CloudletOptionalComponent coc7 = new CloudletOptionalComponent(3, 0.05, 0.07, true, "tag3");
//		cloudletOptionalComponentList.add(coc7);		
////////		
//////
////////	
//		CloudletOptionalComponent coc1 = new CloudletOptionalComponent(4, 0.05, 0.07, true, "tag4");
//		cloudletOptionalComponentList.add(coc1);
//////////
//		CloudletOptionalComponent coc2 = new CloudletOptionalComponent(5, 0.06, 0.05, true, "tag5");
//		cloudletOptionalComponentList.add(coc2);
//
////////////////		
//////////////
//		CloudletOptionalComponent coc3 = new CloudletOptionalComponent(6, 0.06, 0.07, true, "tag6");
//		cloudletOptionalComponentList.add(coc3); //Delay the add operation to the later codes
////////////		
//		CloudletOptionalComponent coc4 = new CloudletOptionalComponent(7, 0.07, 0.05, true, "tag7");
//		cloudletOptionalComponentList.add(coc4);
////		
////		
//		CloudletOptionalComponent coc8 = new CloudletOptionalComponent(8, 0.07, 0.07, true, "tag8");
//		cloudletOptionalComponentList.add(coc8);
//		
////		
//		CloudletOptionalComponent coc9 = new CloudletOptionalComponent(9, 0.08, 0.06, true, "tag9");
//		cloudletOptionalComponentList.add(coc9);
//////		
//////		
//		CloudletOptionalComponent coc10 = new CloudletOptionalComponent(10, 0.08, 0.09, true, "tag10");
//		cloudletOptionalComponentList.add(coc10);
//////		

/** Test Case Type 2*/
		
//		CloudletOptionalComponent coc9 = new CloudletOptionalComponent(0.06, 0.03, true, "tag1");
//		cloudletOptionalComponentList.add(coc9);
//		
//		CloudletOptionalComponent coc10 = new CloudletOptionalComponent(0.06, 0.03, true, "tag1");
//		cloudletOptionalComponentList.add(coc10);
		
//		CloudletOptionalComponent coc7 = new CloudletOptionalComponent(0.07, 0.04, true, "tag3");
//		cloudletOptionalComponentList.add(coc7);		
//		
//
//		
//		CloudletOptionalComponent coc1 = new CloudletOptionalComponent(0.05, 0.03, true, "tag4");
//		cloudletOptionalComponentList.add(coc1);
//
//		CloudletOptionalComponent coc2 = new CloudletOptionalComponent(0.09, 0.03, true, "tag1");
//		cloudletOptionalComponentList.add(coc2);
//
//		CloudletOptionalComponent coc3 = new CloudletOptionalComponent(0.05, 0.03, true, "tag6");
//		cloudletOptionalComponentList.add(coc3); //Delay the add operation to the later codes
//	
//		CloudletOptionalComponent coc4 = new CloudletOptionalComponent(0.04, 0.03, true, "tag1");
//		cloudletOptionalComponentList.add(coc4);	
//		
//		CloudletOptionalComponent coc0 = new CloudletOptionalComponent(0.08, 0.03, true, "tag1");
//		cloudletOptionalComponentList.add(coc0);	
		

/** Test Case Type 3*/
		
//		CloudletOptionalComponent coc5 = new CloudletOptionalComponent(0.07, 0.04, true, "tag1");
//		cloudletOptionalComponentList.add(coc5);
//		
//		CloudletOptionalComponent coc6 = new CloudletOptionalComponent(0.08, 0.03, true, "tag1");
//		cloudletOptionalComponentList.add(coc6);
//		
//		CloudletOptionalComponent coc7 = new CloudletOptionalComponent(0.08, 0.04, true, "tag1");
//		cloudletOptionalComponentList.add(coc7);		
//		
//
//		
//		CloudletOptionalComponent coc1 = new CloudletOptionalComponent(0.07, 0.03, true, "tag1");
//		cloudletOptionalComponentList.add(coc1);
//
//		CloudletOptionalComponent coc2 = new CloudletOptionalComponent(0.07, 0.04, true, "tag2");
//		cloudletOptionalComponentList.add(coc2);
//
//		CloudletOptionalComponent coc3 = new CloudletOptionalComponent(0.08, 0.04, true, "tag6");
//		cloudletOptionalComponentList.add(coc3); //Delay the add operation to the later codes
//	
//		CloudletOptionalComponent coc4 = new CloudletOptionalComponent(0.08, 0.04, true, "tag1");
//		cloudletOptionalComponentList.add(coc4);	
//		
//		CloudletOptionalComponent coc0 = new CloudletOptionalComponent(0.07, 0.04, true, "tag1");
//		cloudletOptionalComponentList.add(coc0);	
		
		
////
//		CloudletOptionalComponent coc = new CloudletOptionalComponent(0.15, 0.13, true, "tag1");
//		cloudletOptionalComponentList.add(coc);
//		
//		
//		CloudletOptionalComponent coc1 = new CloudletOptionalComponent(0.15, 0.16, true, "tag1");
//		cloudletOptionalComponentList.add(coc1);
//
//		CloudletOptionalComponent coc2 = new CloudletOptionalComponent(0.15, 0.1, true, "tag1");
//		cloudletOptionalComponentList.add(coc2);
////		
//
//		CloudletOptionalComponent coc3 = new CloudletOptionalComponent(0.15, 0.11, true, "tag1");
//		cloudletOptionalComponentList.add(coc3); //Delay the add operation to the later codes
		
		for (int i = 0; i < cloudletsNumber; i++) {
			Cloudlet cloudlet = null;
			if (seed == -1) {
				cloudlet = new Cloudlet(i, Constants.CLOUDLET_LENGTH, Constants.CLOUDLET_PES, fileSize, outputSize,
						new UtilizationModelDimmer(), utilizationModelNull, utilizationModelNull,
						cloudletOptionalComponentList, true);
			} else {
				//Delay to this segment, whether to add another component
				if (isAddMoreComponent(i)) {
//					cloudletOptionalComponentList.add(coc2);
//					cloudletOptionalComponentList.add(coc3);
//					cloudletOptionalComponentList.add(coc4);
				}

				cloudlet = new Cloudlet(i, Constants.CLOUDLET_LENGTH, Constants.CLOUDLET_PES, fileSize, outputSize,
						new UtilizationModelDimmer(seed * i), utilizationModelNull, utilizationModelNull,
						cloudletOptionalComponentList, true);
			}
			cloudlet.setUserId(brokerId);
			cloudlet.setVmId(i);
			list.add(cloudlet);
		}

		return list;
	}

	private static boolean isAddMoreComponent(int seed){
		Random random = new Random(seed);
		double randomProbability = random.nextDouble();
		if(randomProbability > 0.8){
			return true;
		}else{
		return false;
		}

	}

}
