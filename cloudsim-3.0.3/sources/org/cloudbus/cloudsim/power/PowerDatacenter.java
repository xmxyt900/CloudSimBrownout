/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.power;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.CloudletOptionalComponent;
import org.cloudbus.cloudsim.CompareByComponentUtilizationAndPriceRatio;
import org.cloudbus.cloudsim.CompareByComponentPrice;
import org.cloudbus.cloudsim.CompareByComponentUtilization;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.ResCloudlet;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicy;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.core.predicates.PredicateType;

import com.cloudbus.cloudsim.examples.power.brownout.Brownout;
import com.cloudbus.cloudsim.examples.power.brownout.BrownoutConstants;
import com.cloudbus.cloudsim.examples.power.brownout.DimmerConstants;

/**
 * PowerDatacenter is a class that enables simulation of power-aware data
 * centers.
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
 * @since CloudSim Toolkit 2.0
 */
public class PowerDatacenter extends Datacenter {

	/** The power. */
	private double power;

	/** The disable migrations. */
	private boolean disableMigrations;

	/** The cloudlet submited. */
	private double cloudletSubmitted;

	/** The migration count. */
	private int migrationCount;

	/** The number of times that the dimmer is triggered */
	private int dimmerTimes = 0;

	/** A set that records how many times dimmer maybe triggered */
	private Set<Double> timeFrameMayTriggeredDimmer = new HashSet<Double>();

	/** A lined hashmap records how many active hosts at each time interval */
	private LinkedHashMap<Double, Integer> numberOfActiveHostMap = new LinkedHashMap<Double, Integer>();
	
	
	double highestDimmerValue = 0.0;
	double lowestDimmerValue = 1.0;
	
	private Random randomGenerator;
	
	long seedFactor = 0l;
	
	double mdpProb; 
	
	/**
	 * Instantiates a new datacenter.
	 * 
	 * @param name
	 *            the name
	 * @param characteristics
	 *            the res config
	 * @param schedulingInterval
	 *            the scheduling interval
	 * @param utilizationBound
	 *            the utilization bound
	 * @param vmAllocationPolicy
	 *            the vm provisioner
	 * @param storageList
	 *            the storage list
	 * @throws Exception
	 *             the exception
	 */
	public PowerDatacenter(String name, DatacenterCharacteristics characteristics,
			VmAllocationPolicy vmAllocationPolicy, List<Storage> storageList, double schedulingInterval)
					throws Exception {
		super(name, characteristics, vmAllocationPolicy, storageList, schedulingInterval);

		setPower(0.0);
		setDisableMigrations(false);
		setCloudletSubmitted(-1);
		setMigrationCount(0);
	}

	/**
	 * Updates processing of each cloudlet running in this PowerDatacenter. It
	 * is necessary because Hosts and VirtualMachines are simple objects, not
	 * entities. So, they don't receive events and updating cloudlets inside
	 * them must be called from the outside.
	 * 
	 * @pre $none
	 * @post $none
	 */
	@Override
	protected void updateCloudletProcessing() {
		if (getCloudletSubmitted() == -1 || getCloudletSubmitted() == CloudSim.clock()) {
			CloudSim.cancelAll(getId(), new PredicateType(CloudSimTags.VM_DATACENTER_EVENT));
			schedule(getId(), getSchedulingInterval(), CloudSimTags.VM_DATACENTER_EVENT);
			return;
		}
		double currentTime = CloudSim.clock();

		// if some time passed since last processing
		if (currentTime > getLastProcessTime()) {
			System.out.print(currentTime + " ");

			double minTime = updateCloudetProcessingWithoutSchedulingFutureEventsForce();

			if (!isDisableMigrations()) {
				List<Map<String, Object>> migrationMap = getVmAllocationPolicy().optimizeAllocation(getVmList());

				if (migrationMap != null) {
					for (Map<String, Object> migrate : migrationMap) {
						Vm vm = (Vm) migrate.get("vm");
						PowerHost targetHost = (PowerHost) migrate.get("host");
						PowerHost oldHost = (PowerHost) vm.getHost();

						if (oldHost == null) {
							Log.formatLine("%.2f: Migration of VM #%d to Host #%d is started", currentTime, vm.getId(),
									targetHost.getId());
						} else {
							Log.formatLine("%.2f: Migration of VM #%d from Host #%d to Host #%d is started",
									currentTime, vm.getId(), oldHost.getId(), targetHost.getId());
						}

						targetHost.addMigratingInVm(vm);
						incrementMigrationCount();

						/** VM migration delay = RAM / bandwidth **/
						// we use BW / 2 to model BW available for migration
						// purposes, the other
						// half of BW is for VM communication
						// around 16 seconds for 1024 MB using 1 Gbit/s network
						send(getId(), vm.getRam() / ((double) targetHost.getBw() / (2 * 8000)), CloudSimTags.VM_MIGRATE,
								migrate);
					}
				}
			}

			// schedules an event to the next time
			if (minTime != Double.MAX_VALUE) {
				CloudSim.cancelAll(getId(), new PredicateType(CloudSimTags.VM_DATACENTER_EVENT));
				send(getId(), getSchedulingInterval(), CloudSimTags.VM_DATACENTER_EVENT);
			}

			setLastProcessTime(currentTime);
		}
	}

	/**
	 * Update cloudlet processing without scheduling future events.
	 * 
	 * @return the double
	 */
	protected double updateCloudetProcessingWithoutSchedulingFutureEvents() {
		if (CloudSim.clock() > getLastProcessTime()) {
			return updateCloudetProcessingWithoutSchedulingFutureEventsForce();
		}
		return 0;
	}

	/**
	 * Update cloudet processing without scheduling future events.
	 * 
	 * @return the double
	 */
	protected double updateCloudetProcessingWithoutSchedulingFutureEventsForce() {
		double currentTime = CloudSim.clock();
		double minTime = Double.MAX_VALUE;
		double timeDiff = currentTime - getLastProcessTime();
		double timeFrameDatacenterEnergy = 0.0;
		int numberOfActiveHost = 0;
		double dimmerValue = 0.0;

		Log.printLine("\n\n--------------------------------------------------------------\n\n");
		Log.formatLine("New resource usage for the time frame starting at %.2f:", currentTime);
		

		dimmerValue = getDimmerValue(currentTime);
		if((currentTime - 0.1) % 300 ==0)
		Log.formatLine("Dimmer is triggered at %.2f, The dimmer value is %.2f", currentTime, dimmerValue);

		for (PowerHost host : this.<PowerHost> getHostList()) {
			Log.printLine();

			// Dimmer is triggered


//			double time = host.updateVmsProcessing(currentTime); // inform VMs
																	// to update
																	// processing
	
			for(Vm vm : host.getVmList()){
				Log.formatLine(
						"%.2f: [Host #" + host.getId() + "] Total allocated MIPS for VM #" + vm.getId()
								+ " (Host #" + vm.getHost().getId()
								+ ") is %.2f, was requested %.2f out of total %.2f (%.2f%%)",
						CloudSim.clock(),
						host.getVmScheduler().getTotalAllocatedMipsForVm(vm),
						vm.getCurrentRequestedTotalMips(),
						vm.getMips(),
						vm.getCurrentRequestedTotalMips() / vm.getMips() * 100);
			}
			
			
			Log.formatLine("%.2f: [Host #%d] utilization is %.2f%%", currentTime, host.getId(),
					host.getUtilizationOfCpu() * 100);
			


//			if(host.getPreviousUtilizationOfCpu() > DimmerConstants.DimmerUpThreshold){
//				Log.formatLine( "Tests: %.2f: [Host #%d] previous utilization is %.2f%%", currentTime, host.getId(),
//						host.getPreviousUtilizationOfCpu() * 100);
			triggerDimmer(host, currentTime, dimmerValue);
			double time = host.updateVmsProcessing(currentTime); 
			
//			}
			

			
			if (time < minTime) {
				minTime = time;
			}

			Log.formatLine("%.2f: [Host #%d] utilization is %.2f%%", currentTime, host.getId(),
					host.getUtilizationOfCpu() * 100);
			
			
			

		}

		if (timeDiff > 0) {
			Log.formatLine("\nEnergy consumption for the last time frame from %.2f to %.2f:", getLastProcessTime(),
					currentTime);

			// Added the code that recored time frame that may trigger dimmer
//			if((currentTime - 0.1) % 300 ==0){
			timeFrameMayTriggeredDimmer.add(currentTime);
//			}

			for (PowerHost host : this.<PowerHost> getHostList()) {
				double previousUtilizationOfCpu = host.getPreviousUtilizationOfCpu();
				double utilizationOfCpu = host.getUtilizationOfCpu();
				double timeFrameHostEnergy = host.getEnergyLinearInterpolation(previousUtilizationOfCpu,
						utilizationOfCpu, timeDiff);
				timeFrameDatacenterEnergy += timeFrameHostEnergy;

				Log.printLine();
				Log.formatLine("%.2f: [Host #%d] utilization at %.2f was %.2f%%, now is %.2f%%", currentTime,
						host.getId(), getLastProcessTime(), previousUtilizationOfCpu * 100, utilizationOfCpu * 100);
//				System.out.println("Host #" + host.getId() + " Time:" + currentTime + " At:" + getLastProcessTime() + " Previous:" + previousUtilizationOfCpu * 100 + " Now:" + utilizationOfCpu * 100);
				
				Log.formatLine("%.2f: [Host #%d] energy is %.2f W*sec", currentTime, host.getId(), timeFrameHostEnergy);

				if (host.getUtilizationOfCpu() == 0) { // Compute the total
														// active number of host
														// at each time interval
					numberOfActiveHost++;
				}

			}

			Log.formatLine("\n%.2f: Data center's energy is %.2f W*sec\n", currentTime, timeFrameDatacenterEnergy);
		}

		if (0 == (currentTime - 0.1) % 300) {
			numberOfActiveHostMap.put(currentTime - 0.1, numberOfActiveHost); // Record
																				// the
																				// active
																				// number
																				// of
																				// host
																				// into
																				// a
																				// map
		}

		setPower(getPower() + timeFrameDatacenterEnergy);

		checkCloudletCompletion();

		/** Remove completed VMs **/
		for (PowerHost host : this.<PowerHost> getHostList()) {
			for (Vm vm : host.getCompletedVms()) {
				getVmAllocationPolicy().deallocateHostForVm(vm);
				getVmList().remove(vm);
				Log.printLine("VM #" + vm.getId() + " has been deallocated from host #" + host.getId());
			}
		}

		Log.printLine();

		setLastProcessTime(currentTime);
		return minTime;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.cloudbus.cloudsim.Datacenter#processVmMigrate(org.cloudbus.cloudsim.
	 * core.SimEvent, boolean)
	 */
	@Override
	protected void processVmMigrate(SimEvent ev, boolean ack) {
		updateCloudetProcessingWithoutSchedulingFutureEvents();
		super.processVmMigrate(ev, ack);
		SimEvent event = CloudSim.findFirstDeferred(getId(), new PredicateType(CloudSimTags.VM_MIGRATE));
		if (event == null || event.eventTime() > CloudSim.clock()) {
			updateCloudetProcessingWithoutSchedulingFutureEventsForce();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cloudsim.Datacenter#processCloudletSubmit(cloudsim.core.SimEvent,
	 * boolean)
	 */
	@Override
	protected void processCloudletSubmit(SimEvent ev, boolean ack) {
		super.processCloudletSubmit(ev, ack);
		setCloudletSubmitted(CloudSim.clock());
	}

	/**
	 * Gets the power.
	 * 
	 * @return the power
	 */
	public double getPower() {
		return power;
	}

	/**
	 * Sets the power.
	 * 
	 * @param power
	 *            the new power
	 */
	protected void setPower(double power) {
		this.power = power;
	}

	/**
	 * Checks if PowerDatacenter is in migration.
	 * 
	 * @return true, if PowerDatacenter is in migration
	 */
	protected boolean isInMigration() {
		boolean result = false;
		for (Vm vm : getVmList()) {
			if (vm.isInMigration()) {
				result = true;
				break;
			}
		}
		return result;
	}

	/**
	 * Checks if is disable migrations.
	 * 
	 * @return true, if is disable migrations
	 */
	public boolean isDisableMigrations() {
		return disableMigrations;
	}

	/**
	 * Sets the disable migrations.
	 * 
	 * @param disableMigrations
	 *            the new disable migrations
	 */
	public void setDisableMigrations(boolean disableMigrations) {
		this.disableMigrations = disableMigrations;
	}

	/**
	 * Checks if is cloudlet submited.
	 * 
	 * @return true, if is cloudlet submited
	 */
	protected double getCloudletSubmitted() {
		return cloudletSubmitted;
	}

	/**
	 * Sets the cloudlet submited.
	 * 
	 * @param cloudletSubmitted
	 *            the new cloudlet submited
	 */
	protected void setCloudletSubmitted(double cloudletSubmitted) {
		this.cloudletSubmitted = cloudletSubmitted;
	}

	/**
	 * Gets the migration count.
	 * 
	 * @return the migration count
	 */
	public int getMigrationCount() {
		return migrationCount;
	}

	/**
	 * Sets the migration count.
	 * 
	 * @param migrationCount
	 *            the new migration count
	 */
	protected void setMigrationCount(int migrationCount) {
		this.migrationCount = migrationCount;
	}

	/**
	 * Increment migration count.
	 */
	protected void incrementMigrationCount() {
		setMigrationCount(getMigrationCount() + 1);
	}

	/**
	 * Defines when to trigger the dimmer to configure utilization of host
	 * 
	 * @param host
	 * @author minxianx
	 */
	public void triggerDimmer(PowerHost host, double currentTime, double dimmerValue) {
		double utilizationAfterDimmer = 0.0;
		double previousUtilizationOfCpu = host.getPreviousUtilizationOfCpu();
		double vmPreviousUtilizationOfcpu = 0.0;
		
		int stateSize = 0;
		if((currentTime - 0.1) % 300 == 0) {
		updataHostObtainedRevenue(host);
		}

		if (previousUtilizationOfCpu >= DimmerConstants.DimmerUpThreshold &&(currentTime - 0.1) % 300 == 0) {
//			Log.formatLine("Host Utilzation %.2f:", previousUtilizationOfCpu);
//			System.out.println("\n"  + "#" + host.getId() + " Host Utilization Before:" + previousUtilizationOfCpu);
			dimmerTimes++;
			
			dimmerValue = dimmerValue * previousUtilizationOfCpu / DimmerConstants.DimmerUpThreshold;
			Log.formatLine("Dimmer is triggered at %.2f, The dimmer value is %.2f", currentTime, dimmerValue);
			
//			host.setDimmerValue(dimmerValue);


//			Log.formatLine("Dimmer is triggered at %.2f, The dimmer value is %.2f", currentTime, dimmerValue);

			for (Vm vm : host.getVmList()) {
				stateSize = vm.getStateHistory().size();
				vmPreviousUtilizationOfcpu = vm.getStateHistory().get(stateSize - 1).getRequestedMips() / vm.getMips();
				

				for (ResCloudlet rcl : vm.getCloudletScheduler().getCloudletExecList()) {
					updateOptionalComponents(rcl.getCloudlet());
					// rcl.getCloudlet().getUtilizationModelCpu().setUtilization(utilizaiton,
					// CloudSim.clock());
					// configureOptionalComponentsByNearestUtilization(host,
					// rcl.getCloudlet(),
					// vmPreviousUtilizationOfcpu * (1 - dimmerValue));
					if(Brownout.brownoutPolicyId == 0){
					configureOptionalComponentsByNearestUtilization(host, rcl.getCloudlet(),
							vmPreviousUtilizationOfcpu * (dimmerValue));
					}
//////					
					else if(Brownout.brownoutPolicyId == 1){
					configureOptionalComponentsByLowestUtilization(host, rcl.getCloudlet(),
							vmPreviousUtilizationOfcpu * (dimmerValue));
			}
					else if(Brownout.brownoutPolicyId ==2 ){
					configureOptionalComponentsByLowestPrice(host, rcl.getCloudlet(),
							vmPreviousUtilizationOfcpu * (dimmerValue));
					}
					else{
					configureOptionalComponentsByHighestUtilizationAndPriceRatio(host, rcl.getCloudlet(),
							vmPreviousUtilizationOfcpu * (dimmerValue));
					}
					
					//Revision needed

					
//					configureOptionalComponentsByMDP(host, rcl.getCloudlet(), vmPreviousUtilizationOfcpu * (dimmerValue));
		

//					System.out.println("The dimmer value is" + dimmerValue);
					utilizationAfterDimmer = getUtilizaitonAfterDimmer(vmPreviousUtilizationOfcpu, rcl.getCloudlet());
					Log.formatLine("VM #%d utilizaiton after deactivation %.2f:", vm.getId(), vmPreviousUtilizationOfcpu * utilizationAfterDimmer);
//					System.out.println("utilization After Dimmer" + utilizationAfterDimmer);
					rcl.getCloudlet().getUtilizationModelCpu().setUtilization(utilizationAfterDimmer, currentTime);
					
			
			
					// host.getDisabaledTagsSet().removeAll(host.getDisabaledTagsSet());
					// System.out.println("DisabledTagSet is cleaned......");
					

				}
			}
	
		}
	}

	/**
	 * Get the dimmer value. If there are k hosts are overloads, the dimmer
	 * value is 1 - k/n, in which n is the total number of host
	 * 
	 * @return
	 */
	public double getDimmerValue(double currentTime) {
		double dimmerValue = 0.0f;
		double overloadedNumberOfHost = 0;
		for (PowerHost host : this.<PowerHost> getHostList()) {
//			System.out.println("Host Utilization: " + host.getPreviousUtilizationOfCpu());
			if (host.getPreviousUtilizationOfCpu() > DimmerConstants.DimmerUpThreshold) {
				
				overloadedNumberOfHost++;
			}
		}

		int slot = (int) currentTime / 300;
		double dimpar = (slot % 10 + 1) * 0.1 * 0.9;
//		dimmerValue = Math.sqrt(overloadedNumberOfHost / (double) this.<PowerHost> getHostList().size()) * (1 - DimmerConstants.DimmerComponentLowerThreshold);
//		dimmerValue = Math.sqrt(overloadedNumberOfHost / (double) this.<PowerHost> getHostList().size() ) ;
		dimmerValue = dimpar;
		
//		double slot = currentTime / 300;
//		if( (slot >= 0 && slot < 48))   dimmerValue = 0.1;
//		if( (slot >= 48 && slot < 96))	dimmerValue = 0.3;
//		if( (slot >= 144 && slot < 192)) dimmerValue = 0.6;
//		if( (slot >= 192 && slot < 240))  dimmerValue = 0.4;
//		if( (slot >= 96 && slot < 144) || (slot >= 240 && slot < 288))	dimmerValue = 0.5; 
//		

//		dimmerValue = overloadedNumberOfHost / (double) this.<PowerHost> getHostList().size() * (1 - DimmerConstants.DimmerComponentLowerThreshold) ;

//		if(dimmerValue > (1 - DimmerConstants.DimmerComponentLowerThreshold)){
//			dimmerValue = 1 - DimmerConstants.DimmerComponentLowerThreshold;
//		}
		
		if(dimmerValue < lowestDimmerValue && dimmerValue > 0) lowestDimmerValue = dimmerValue;
		if(dimmerValue > highestDimmerValue) highestDimmerValue = dimmerValue;
		
		return dimmerValue ;
	}

	/**
	 * Set the specific component of cloudlet as disabled, the
	 * configuredUtilization is the goal utilization that would be reduced. And
	 * this function aims to find the component with utilization nearest to goal
	 * utilization.
	 *
	 * To be optimized......
	 * 
	 * @param cloudlet
	 * @param configuredUtilization
	 * @author minxianx
	 */
	public void configureOptionalComponentsByNearestUtilization(PowerHost host, Cloudlet cloudlet,
			double configuredUtilization) {
		
		int vmsize = host.getVmList().size();
		int nearestUtilizationComponentNumber = 0;
		double minimulUtilizationDifference = 1.0;
		double utilizationDifference = 0.0;
		int index = 0;
		for (CloudletOptionalComponent coc : cloudlet.getCloudletOptionalComponentList()) {			
			utilizationDifference = Math.abs(configuredUtilization - coc.getComponentUtilization());
			if (utilizationDifference < minimulUtilizationDifference) {
				minimulUtilizationDifference = utilizationDifference;
				nearestUtilizationComponentNumber = index;
			}
			index++;
		}
		cloudlet.getCloudletOptionalComponentList().get(nearestUtilizationComponentNumber).setEnabled(false);
		host.getDisabaledTagsSet().add(
				cloudlet.getCloudletOptionalComponentList().get(nearestUtilizationComponentNumber).getComponentTag());
		for (CloudletOptionalComponent coc : cloudlet.getCloudletOptionalComponentList()) {
			if (isTagInSet(host.getDisabaledTagsSet(), coc.getComponentTag())) {
				coc.setEnabled(false);
			}
		}
//		System.out.println(host.getDisabaledTagsSet().toString());
		
		updateHostDeactivatedComponentRatio(host, cloudlet, vmsize);
		updateHostRevenueLoss(host, cloudlet);
		host.getDisabaledTagsSet().removeAll(host.getDisabaledTagsSet());
//		System.out.println("DisabledTagSet is cleaned......");

	}

	/**
	 * Set the specific component of cloudlet as disabled, the
	 * configuredUtilization is the goal utilization that would be reduced. And
	 * this function aims to find the components that with lower utilization and
	 * less than the configuredUtillization component and disable it.
	 *
	 * Policy Updated as version 2 on 23/05/2016
	 * 
	 * @param cloudlet
	 * @param configuredUtilization
	 * @author minxianx
	 */
	public void configureOptionalComponentsByLowestUtilization(PowerHost host, Cloudlet cloudlet,
			double configuredUtilization) {

		int vmsize = host.getVmList().size(); // should be above 0, needs check
		Collections.sort(cloudlet.getCloudletOptionalComponentList(), new CompareByComponentUtilization());

		if (cloudlet.getCloudletOptionalComponentList().get(0).getComponentUtilization() >= configuredUtilization) {
			cloudlet.getCloudletOptionalComponentList().get(0).setEnabled(false);
			host.getDisabaledTagsSet().add(cloudlet.getCloudletOptionalComponentList().get(0).getComponentTag());
			for (CloudletOptionalComponent coc : cloudlet.getCloudletOptionalComponentList()) {
				if (isTagInSet(host.getDisabaledTagsSet(), coc.getComponentTag())) {
					coc.setEnabled(false);
				}

			}
			updateHostDeactivatedComponentRatio(host, cloudlet, vmsize);
			updateHostRevenueLoss(host, cloudlet);
			
			
		} else {

			int partitionIndex = cloudlet.getCloudletOptionalComponentList().size() - 1;
			double tempUtilization1 = 0.0;
			double tempUtilization2 = 0.0;

			for (int index = 1; index < cloudlet.getCloudletOptionalComponentList().size(); index++) {
				tempUtilization1 = 0.0;
				for (int i = 0; i <= index - 1; i++) {
					tempUtilization1 += cloudlet.getCloudletOptionalComponentList().get(i).getComponentUtilization();
				}

				// tempUtilization2 = 0.0;
				// for (int j = 0; j <= index; j++) {
				// tempUtilization2 +=
				// cloudlet.getCloudletOptionalComponentList().get(j).getComponentUtilization();
				// }
				tempUtilization2 = tempUtilization1
						+ cloudlet.getCloudletOptionalComponentList().get(index).getComponentUtilization();

				if ((tempUtilization1 <= configuredUtilization) && (configuredUtilization < tempUtilization2)) {
					if((configuredUtilization - tempUtilization1) <= (tempUtilization2 - configuredUtilization)){
					partitionIndex = index - 1;
					break;
					}else{
						partitionIndex= index;
						break;
					}
				}

			}
			for (int k = 0; k <= partitionIndex; k++) {
				cloudlet.getCloudletOptionalComponentList().get(k).setEnabled(false);
				host.getDisabaledTagsSet().add(cloudlet.getCloudletOptionalComponentList().get(k).getComponentTag());
			}
			

			for (int index = partitionIndex; index < cloudlet.getCloudletOptionalComponentList().size(); index++) {
				if (isTagInSet(host.getDisabaledTagsSet(),
						cloudlet.getCloudletOptionalComponentList().get(index).getComponentTag())) {
					cloudlet.getCloudletOptionalComponentList().get(index).setEnabled(false);
				}

			}
			
			 
			updateHostDeactivatedComponentRatio(host, cloudlet, vmsize);

			updateHostRevenueLoss(host, cloudlet);
			
		}
//		System.out.println(host.getDisabaledTagsSet().toString());
		host.getDisabaledTagsSet().removeAll(host.getDisabaledTagsSet());
//		System.out.println("DisabledTagSet is cleaned......");
	}

	/**
	 * Set the specific component of cloudlet as disabled, the
	 * configuredUtilization is the goal utilization that would be reduced. And
	 * this function aims to find the component with highest utilization / price
	 * component and disable it.
	 *
	 * To be optimized......
	 * 
	 * @param cloudlet
	 * @param configuredUtilization
	 * @author minxianx
	 */
	public void configureOptionalComponentsByHighestUtilizationAndPriceRatio(PowerHost host, Cloudlet cloudlet,
			double configuredUtilization) {
		
		int vmsize = host.getVmList().size(); // should be above 0, needs check
		Collections.sort(cloudlet.getCloudletOptionalComponentList(), new CompareByComponentUtilizationAndPriceRatio());

		if (cloudlet.getCloudletOptionalComponentList().get(0).getComponentUtilization() >= configuredUtilization) {
			cloudlet.getCloudletOptionalComponentList().get(0).setEnabled(false);
			host.getDisabaledTagsSet().add(cloudlet.getCloudletOptionalComponentList().get(0).getComponentTag());
			for (CloudletOptionalComponent coc : cloudlet.getCloudletOptionalComponentList()) {
				if (isTagInSet(host.getDisabaledTagsSet(), coc.getComponentTag())) {
					coc.setEnabled(false);
				}

			}
			
			updateHostDeactivatedComponentRatio(host, cloudlet, vmsize);
			updateHostRevenueLoss(host, cloudlet);
		} else {

			int partitionIndex = cloudlet.getCloudletOptionalComponentList().size() - 1;
			double tempUtilization1 = 0.0;
			double tempUtilization2 = 0.0;

			for (int index = 1; index < cloudlet.getCloudletOptionalComponentList().size(); index++) {
				tempUtilization1 = 0.0;
				for (int i = 0; i <= index - 1; i++) {
					tempUtilization1 += cloudlet.getCloudletOptionalComponentList().get(i).getComponentUtilization();
				}
				tempUtilization2 = tempUtilization1
						+ cloudlet.getCloudletOptionalComponentList().get(index).getComponentUtilization();

				if ((tempUtilization1 <= configuredUtilization) && (configuredUtilization < tempUtilization2)) {
					if((configuredUtilization - tempUtilization1) <= (tempUtilization2 - configuredUtilization)){
						partitionIndex = index - 1;
						break;
						}else{
							partitionIndex= index;
							break;
						}
				}

			}
			for (int k = 0; k <= partitionIndex; k++) {
				cloudlet.getCloudletOptionalComponentList().get(k).setEnabled(false);
				host.getDisabaledTagsSet().add(cloudlet.getCloudletOptionalComponentList().get(k).getComponentTag());
			}
			 

			for (int index = partitionIndex; index < cloudlet.getCloudletOptionalComponentList().size(); index++) {
				if (isTagInSet(host.getDisabaledTagsSet(),
						cloudlet.getCloudletOptionalComponentList().get(index).getComponentTag())) {
					cloudlet.getCloudletOptionalComponentList().get(index).setEnabled(false);
				}

			}
			updateHostDeactivatedComponentRatio(host, cloudlet, vmsize);
			updateHostRevenueLoss(host, cloudlet);
			
		}
//		System.out.println(host.getDisabaledTagsSet().toString());
		host.getDisabaledTagsSet().removeAll(host.getDisabaledTagsSet());
//		System.out.println("DisabledTagSet is cleaned......");
	}

	
	public void configureOptionalComponentsByMDP(PowerHost host, Cloudlet cloudlet,
			double configuredUtilization) {		
		
		
		setRandomGenerator(new Random(Brownout.MARKOV_SEED + seedFactor++));
		mdpProb = getRandomGenerator().nextInt(1000) / 1000.0;
		
		System.out.println("MDP_PROB: " + mdpProb);
		Log.printLine("MDP_PROB: " + mdpProb);
		
		
		int vmsize = host.getVmList().size(); // should be above 0, needs check
		Collections.sort(cloudlet.getCloudletOptionalComponentList(), new CompareByComponentUtilizationAndPriceRatio());
		
		int optionalComponentListSize = cloudlet.getCloudletOptionalComponentList().size();
		
//		double prob = 1.0 / optionalComponentListSize;
		
		double probLow = configuredUtilization / ((optionalComponentListSize + 1) /2);   //round up
		double probHigh = (1 - configuredUtilization) / (optionalComponentListSize - optionalComponentListSize /2);
		
		
		
		
		
		System.out.println("ProbLow: " + probLow);
		System.out.println("ProbHigh: " + probHigh);
		
		
		//Set the probability 
		for (int index = 0; index < optionalComponentListSize / 2; index++) {
			cloudlet.getCloudletOptionalComponentList().get(index).setProbability(configuredUtilization + probHigh * (optionalComponentListSize / 2 - index));
			
		}
		
		for (int index = optionalComponentListSize / 2; index < optionalComponentListSize; index++) {
			cloudlet.getCloudletOptionalComponentList().get(index).setProbability(configuredUtilization -  probLow * (index - optionalComponentListSize / 2));
			
		}

		
		
		
//		double prob = 0.0f;
//		double utilization = 0.0f;
//		double price = 0.0f;
//		double upratio = 0.0f;
//		int compId;
//		for (int index = 0; index < optionalComponentListSize; index++) {
//			compId = cloudlet.getCloudletOptionalComponentList().get(index).getId();
//			prob = cloudlet.getCloudletOptionalComponentList().get(index).getProbability();
//			utilization = cloudlet.getCloudletOptionalComponentList().get(index).getComponentUtilization();
//			price = cloudlet.getCloudletOptionalComponentList().get(index).getComponentPrice();
//			upratio = utilization / price;
//			System.out.println("Id: " + compId + " Prob: " + prob + " utilization: " + utilization + " price: " + price + " upratio: " + upratio);
//			
//			if(cloudlet.getCloudletOptionalComponentList().get(index).getProbability() < mdpProb){
//				cloudlet.getCloudletOptionalComponentList().get(index).setEnabled(false);
//			}
//			
//		}
		//Set the component with lower probability than mdp as 
		for (CloudletOptionalComponent coc : cloudlet.getCloudletOptionalComponentList()) {
			if (coc.getProbability() < mdpProb) {
				coc.setEnabled(false);
				host.getDisabaledTagsSet().add(coc.getComponentTag());
			}
			
		}
//		updateHostDeactivatedComponentRatio(host, cloudlet, vmsize);
//		updateHostRevenueLoss(host, cloudlet);
		
		//update status of connected components
		for (CloudletOptionalComponent coc : cloudlet.getCloudletOptionalComponentList()) {
			if (isTagInSet(host.getDisabaledTagsSet(), coc.getComponentTag())) {
				coc.setEnabled(false);
			}
		}
		updateHostDeactivatedComponentRatio(host, cloudlet, vmsize);
		updateHostRevenueLoss(host, cloudlet);

		System.out.println(host.getDisabaledTagsSet().toString());
		host.getDisabaledTagsSet().removeAll(host.getDisabaledTagsSet());
		System.out.println("DisabledTagSet is cleaned......");
	}
	
	/**
	 * Set the specific component of cloudlet as disabled, the
	 * configuredUtilization is the goal utilization that would be reduced. And
	 * this function aims to find the component with lowest price component and
	 * disable it.
	 *
	 * To be optimized......
	 * 
	 * @param cloudlet
	 * @param configuredUtilization
	 * @author minxianx
	 */
	public void configureOptionalComponentsByLowestPrice(PowerHost host, Cloudlet cloudlet,
			double configuredUtilization) {

		int vmsize = host.getVmList().size(); // should be above 0, needs check

		Collections.sort(cloudlet.getCloudletOptionalComponentList(), new CompareByComponentPrice());

		// The outputs are as expected
		// for (CloudletOptionalComponent coc :
		// cloudlet.getCloudletOptionalComponentList()) {
		// System.out.println(coc.getComponentTag() + " " +
		// coc.getComponentUtilization() + " " + coc.getComponentPrice());
		// }
		if (cloudlet.getCloudletOptionalComponentList().get(0).getComponentUtilization() >= configuredUtilization) {
			cloudlet.getCloudletOptionalComponentList().get(0).setEnabled(false);
			host.getDisabaledTagsSet().add(cloudlet.getCloudletOptionalComponentList().get(0).getComponentTag());
			for (CloudletOptionalComponent coc : cloudlet.getCloudletOptionalComponentList()) {
				if (isTagInSet(host.getDisabaledTagsSet(), coc.getComponentTag())) {
					coc.setEnabled(false);
				}

			}
			
			updateHostDeactivatedComponentRatio(host, cloudlet, vmsize);
			updateHostRevenueLoss(host, cloudlet);
		} else {
			// partionIndex is used to find the boundary that divides the
			// utilization
			int partitionIndex = cloudlet.getCloudletOptionalComponentList().size() - 1;
			double tempUtilization1 = 0.0;
			double tempUtilization2 = 0.0;
			// System.out.println("Configured Utilization: " +
			// configuredUtilization);
			for (int index = 1; index < cloudlet.getCloudletOptionalComponentList().size(); index++) {
				tempUtilization1 = 0.0;
				for (int i = 0; i <= index - 1; i++) {
					tempUtilization1 += cloudlet.getCloudletOptionalComponentList().get(i).getComponentUtilization();
				}
				tempUtilization2 = tempUtilization1
						+ cloudlet.getCloudletOptionalComponentList().get(index).getComponentUtilization();
				// for (int j = 0; j <= index; j++) {
				// tempUtilization2 +=
				// cloudlet.getCloudletOptionalComponentList().get(j).getComponentUtilization();
				// }
				// System.out.println(tempUtilization1 + " " +
				// configuredUtilization + " " + tempUtilization2);
				if ((tempUtilization1 <= configuredUtilization) && (configuredUtilization < tempUtilization2)) {
					if((configuredUtilization - tempUtilization1) <= (tempUtilization2 - configuredUtilization)){
						partitionIndex = index - 1;
						break;
						}else{
							partitionIndex= index;
							break;
						}
				}

			}
			for (int k = 0; k <= partitionIndex; k++) {
				cloudlet.getCloudletOptionalComponentList().get(k).setEnabled(false);
				host.getDisabaledTagsSet().add(cloudlet.getCloudletOptionalComponentList().get(k).getComponentTag());
			}

			for (int index = partitionIndex; index < cloudlet.getCloudletOptionalComponentList().size(); index++) {
				if (isTagInSet(host.getDisabaledTagsSet(),
						cloudlet.getCloudletOptionalComponentList().get(index).getComponentTag())) {
					cloudlet.getCloudletOptionalComponentList().get(index).setEnabled(false);
				}

			}
			// System.out.println(host.getDisabaledTagsSet().toString());
			// System.out.println("Partition Index: " + partitionIndex);
			updateHostDeactivatedComponentRatio(host, cloudlet, vmsize);
			updateHostRevenueLoss(host, cloudlet);
			

		}
//		System.out.println(host.getDisabaledTagsSet().toString());
		host.getDisabaledTagsSet().removeAll(host.getDisabaledTagsSet());
//		System.out.println("DisabledTagSet is cleaned......");
	}

	/**
	 * Update the revenue loss of host
	 * 
	 * @param host
	 * @param coc
	 * @author minxianx
	 */
	public void updateHostRevenueLoss(PowerHost host, CloudletOptionalComponent coc) {
		double hostRevenueLoss = host.getRevenueLoss() + coc.getComponentPrice();
		host.setRevenueLoss(hostRevenueLoss);

	}

	/**
	 * Update the revenue loss of host
	 * 
	 * @param host
	 * @param coc
	 * @author minxianx
	 */
	public void updateHostRevenueLoss(PowerHost host, Cloudlet cloudlet) {
		double hostRevenueLoss = 0.0;
		for (CloudletOptionalComponent coc : cloudlet.getCloudletOptionalComponentList()) {
			if (coc.isEnabled() == false) {
				hostRevenueLoss += coc.getComponentPrice();
				
			}
		}
//		System.out.println("Host id:" + host.getId() + " Increased Revenue Loss: " +
//				 hostRevenueLoss);
		hostRevenueLoss += host.getRevenueLoss();
		host.setRevenueLoss(hostRevenueLoss);
//		 System.out.println("Host id:" + host.getId() + " Revenue Loss: " +
//		 hostRevenueLoss);
		// System.out.println();
	}

	/**
	 * Update the host total deactivated component ratio
	 * @param host
	 * @param cloudlet
	 * @param vmSize
	 */
	public void updateHostDeactivatedComponentRatio(PowerHost host, Cloudlet cloudlet, int vmSize) {
		double deacticatedComponentRatio = 0.0;
		double deactivatedComponentNumber = 0.0;
		for (CloudletOptionalComponent coc : cloudlet.getCloudletOptionalComponentList()) {
			if (coc.isEnabled() == false) {
//				deactivatedComponentNumber++;
				deactivatedComponentNumber += coc.getComponentUtilization();
			}
		}
		if (vmSize == 0 || cloudlet.getCloudletOptionalComponentList().size() == 0) {
			deacticatedComponentRatio = 0;
		} else {
			deacticatedComponentRatio = deactivatedComponentNumber / cloudlet.getCloudletOptionalComponentList().size()
					/ vmSize;
			
			
			deacticatedComponentRatio = deactivatedComponentNumber;
//			System.out.println("Deactivated Component Ratio in Loop: " + deacticatedComponentRatio);
		}
//        System.out.println("Deactivated Component Ratio: " + host.getTotalDeactivatedComponentRatio());
        
		host.setTotalDeactivatedComponentRatio(deacticatedComponentRatio + host.getTotalDeactivatedComponentRatio());
	}

	/**
	 * Update the ability (enabled or disabled) of component
	 * 
	 * @param cloudlet
	 * @author minxianx
	 */
	public void updateOptionalComponents(Cloudlet cloudlet) {
		for (CloudletOptionalComponent coc : cloudlet.getCloudletOptionalComponentList()) {
			coc.setEnabled(true);
		}
	}

	/**
	 * Get the utilization of Cloudlet after dimmer is triggered
	 * 
	 * @param cloudlet
	 * @return
	 * @author minxianx
	 */
	public double getUtilizaitonAfterDimmer(double previousUtilizationOfCpu, Cloudlet cloudlet) {
		double dimmerUtilization = 0.0;
		Log.format("Components are:  ");

		for (CloudletOptionalComponent coc : cloudlet.getCloudletOptionalComponentList()) {
			if (coc.isEnabled() == true) {
				dimmerUtilization += coc.getComponentUtilization();
				Log.format(" %s ", coc.getComponentTag());
			}
		}
		
		Log.format(" are working, ");
		// if(previousUtilizationOfCpu - dimmierUtilization <= 0){
		// return DimmerConstants.DimmerComponentLowerThreshold;
		// }

		double UtilizationAfterDimmer = (dimmerUtilization + DimmerConstants.DimmerComponentLowerThreshold) * previousUtilizationOfCpu;
		Log.format(" wokring utilization is %f.2 \n", dimmerUtilization + DimmerConstants.DimmerComponentLowerThreshold );
		return UtilizationAfterDimmer;
	}

	/**
	 * Get the revenue loss of data center
	 * 
	 * @return
	 * @author minxianx
	 */
	public double getDataCenterRevenueLoss() {
		double dataCenterRevenueLoss = 0.0;
		for (PowerHost host : this.<PowerHost> getHostList()) {
			dataCenterRevenueLoss += host.getRevenueLoss();
			
		}
		return dataCenterRevenueLoss;
	}

	public double getDataCenterObainedRevenue() {
		double dataCenterObtainedRevenue = 0.0;
		for (PowerHost host : this.<PowerHost> getHostList()) {
			dataCenterObtainedRevenue += host.getObtainedRevenue();
		}
		return dataCenterObtainedRevenue;
	}

	public double getDataCenterTotalDeactivatedComponentRatio() {
		double dataCenterTotalDeactivatedComponentRatio = 0.0;
		for (PowerHost host : this.<PowerHost> getHostList()) {
			dataCenterTotalDeactivatedComponentRatio += host.getTotalDeactivatedComponentRatio();
		}
		return dataCenterTotalDeactivatedComponentRatio;
	}
	/**
	 * Check whether the tags are the set
	 * 
	 * @param hashSet
	 * @param tag
	 * @author minxianx
	 * @return
	 */
	public boolean isTagInSet(Set<String> hashSet, String tag) {
		boolean tagInSet = false;
		for (String string : hashSet) {
			if (string.equals(tag)) {
				tagInSet = true;
			}
		}
		return tagInSet;
	}

	/**
	 * Get the number of how many time the dimmer has been triggered
	 * 
	 * @return
	 * @author minxianx
	 */
	public int getDimmerTime() {

		return dimmerTimes;
	}

	public int getTimesMayTriggerDimmer() {
		return timeFrameMayTriggeredDimmer.size() * BrownoutConstants.NUMBER_OF_HOSTS;
	}

	/**
	 * Get the linked hash map that records the number of active hosts at each
	 * interval
	 * 
	 * @return
	 * @author minxianx
	 */
	public LinkedHashMap<Double, Integer> getNumberOfActiveHostsMap() {
		return numberOfActiveHostMap;
	}

	public void updataHostObtainedRevenue(PowerHost host) {
		double hostObtainedRevenue = 0.0;
		for (Vm vm : host.getVmList()) {
			for (ResCloudlet rcl : vm.getCloudletScheduler().getCloudletExecList()) {
				hostObtainedRevenue += 1;
			}
		}
		host.setObainedRevenue(host.getObtainedRevenue() + hostObtainedRevenue);
	}
	
	public double getHighestDimmerValue(){
		return highestDimmerValue;
		
	}
	
	public double getLowestDimmerValue(){
		return lowestDimmerValue;
	}
	
	
	/**
	 * Sets the random generator.
	 * 
	 * @param randomGenerator the new random generator
	 */
	public void setRandomGenerator(Random randomGenerator) {
		this.randomGenerator = randomGenerator;
	}

	/**
	 * Gets the random generator.
	 * 
	 * @return the random generator
	 */
	public Random getRandomGenerator() {
		return randomGenerator;
	}
}
