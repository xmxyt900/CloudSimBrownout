package org.cloudbus.cloudsim;

import java.util.Comparator;

/**
 * Sort the component instances with component utilization and price ratio
 * From ascending order
 * @author minxianx
 *
 */
public class CompareByComponentUtilizationAndPriceRatio implements Comparator<Object> {
	
	public int compare(Object o1, Object o2){
		CloudletOptionalComponent coc1 = (CloudletOptionalComponent) o1;
		CloudletOptionalComponent coc2 = (CloudletOptionalComponent) o2;
		
		double utilizationAndPriceRatio1 = coc1.getComponentUtilization() / coc1.getComponentPrice();
		double utilizationAndPriceRatio2 = coc2.getComponentUtilization() / coc2.getComponentPrice();
		
		if(utilizationAndPriceRatio1 < utilizationAndPriceRatio2){
			return 1;
		}
		if(utilizationAndPriceRatio1 > utilizationAndPriceRatio2){
			return -1;
		}
		
		return 0;
	}
}
