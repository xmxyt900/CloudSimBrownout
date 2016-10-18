package org.cloudbus.cloudsim;

import java.util.Comparator;

/**
 * This class is defining the optional components of Cloudlet, which occupies part of utilization, price of the
 * fully Cloudlet. The status of optional components could be set as enabled or disabled.
 * @author minxianx
 *
 */
public class CloudletOptionalComponent {

	int id;
	double componentUtilization;
	double componentPrice;
	double mdpProb;
	boolean isEnabled;
	String tag;
	public CloudletOptionalComponent(double componentUtilization, double componentPrice, boolean isEnable){
		this.componentUtilization = componentUtilization;
		this.componentPrice = componentPrice;
		this.isEnabled = isEnable;
	}
	
	public CloudletOptionalComponent(int id, double componentUtilization, double componentPrice, boolean isEnable, String tag){
		this.id = id;
		this.componentUtilization = componentUtilization;
		this.componentPrice = componentPrice;
		this.isEnabled = isEnable;
		this.tag = tag;
	}
	
	public int getId(){
		return id;
	}
	
	public boolean isEnabled() {
		return isEnabled;
	}
	public void setEnabled(boolean isEnabled) {
		this.isEnabled = isEnabled;
	}
	public double getComponentUtilization() {
		return componentUtilization;
	}
	public double getComponentPrice() {
		return componentPrice;
	}
	
	public String getComponentTag(){
		return tag;
	}
	
	public void setProbability(double prob){
		this.mdpProb = prob;
	}
	
	public double getProbability(){
		return mdpProb;
	}
	
}



