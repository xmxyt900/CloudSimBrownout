package org.cloudbus.cloudsim;

import java.util.Comparator;

/**
 * Sort the component instances with component price 
 * From ascending order
 * @author minxianx
 *
 */
public class CompareByComponentPrice implements Comparator<Object> {
	
	public int compare(Object o1, Object o2){
		CloudletOptionalComponent coc1 = (CloudletOptionalComponent) o1;
		CloudletOptionalComponent coc2 = (CloudletOptionalComponent) o2;
		
		if(coc1.getComponentPrice() < coc2.getComponentPrice()){
			return -1;
		}
		if(coc1.getComponentPrice() > coc2.getComponentPrice()){
			return 1;
		}
		
		return 0;
	}
}

