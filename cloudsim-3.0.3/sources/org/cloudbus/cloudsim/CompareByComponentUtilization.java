package org.cloudbus.cloudsim;

import java.util.Comparator;

/**
 * Sort the component instances with component utilization 
 * From ascending order
 * @author minxianx
 *
 */

public class CompareByComponentUtilization implements Comparator<Object> {

	public int compare(Object o1, Object o2){
		CloudletOptionalComponent coc1 = (CloudletOptionalComponent) o1;
		CloudletOptionalComponent coc2 = (CloudletOptionalComponent) o2;
		
		if(coc1.getComponentUtilization() < coc2.getComponentUtilization()){
			return -1;
		}
		if(coc1.getComponentUtilization() > coc2.getComponentUtilization()){
			return 1;
		}
		
		return 0;
	}
}
