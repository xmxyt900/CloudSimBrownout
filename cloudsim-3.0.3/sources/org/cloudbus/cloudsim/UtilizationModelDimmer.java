/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import workload.planetlab.OverloadedTraceGenerator;

/**
 * The UtilizationModelDimmer class implements a model, according to which a Cloudlet increase or 
 * decrease cpu utilization .
 * 
 * @author Anton Beloglazov
 * @since CloudSim Toolkit 2.0
 */
public class UtilizationModelDimmer implements UtilizationModel {

	/** The random generator. */
	private Random randomGenerator;

	/** The history. */
	private Map<Double, Double> history;
	
	private boolean isDimmerTriggered = false;
	
	private double dimmerUtilization = 0.0;
	
	OverloadedTraceGenerator overloadTraceGenerator = new OverloadedTraceGenerator();
	
	int traceIndex =0;

	/**
	 * Instantiates a new utilization model stochastic.
	 */
	public UtilizationModelDimmer() {
		setHistory(new HashMap<Double, Double>());
		setRandomGenerator(new Random());
	}

	/**
	 * Instantiates a new utilization model stochastic.
	 * 
	 * @param seed the seed
	 */
	public UtilizationModelDimmer(long seed) {
		setHistory(new HashMap<Double, Double>());
		setRandomGenerator(new Random(seed));
	}

	/*
	 * (non-Javadoc)
	 * @see cloudsim.power.UtilizationModel#getUtilization(double)
	 */
	@Override
	public double getUtilization(double time) {
		if (getHistory().containsKey(time)) {
			return getHistory().get(time);
		}
		double slot = time / 300;
//		double utilization = 0;
//		if( (slot >= 0 && slot < 48))    utilization = (0.25 * getRandomGenerator().nextDouble() + 0.75);
//		if( (slot >= 48 && slot < 96))	utilization = (0.15 * getRandomGenerator().nextDouble() + 0.85);
//		if( (slot >= 144 && slot < 192)) utilization = (0.05 * getRandomGenerator().nextDouble() + 0.95);
//		if( (slot >= 192 && slot < 240)) utilization = (0.05 * getRandomGenerator().nextDouble() + 0.95);
//		if( (slot >= 96 && slot < 144) || (slot >= 240 && slot < 288))	 utilization = (0.15 * getRandomGenerator().nextDouble() + 0.85);
//		

	   double randomUtilization = getRandomGenerator().nextDouble();
		double utilization = 0.95 + 0.05 * randomUtilization ;
		
		utilization = overloadTraceGenerator.getOverloadedTrace().get(traceIndex++ % 287);
		if(isDimmerTriggered && (time - 0.1) % 300 == 0 ){
			isDimmerTriggered = false;
//			System.out.println("Regenerate Utilization " + dimmerUtilization);
			utilization = dimmerUtilization * utilization;
			Log.format("Time: %f, Regenerate Utilization %f.2 \n", time, utilization);
			getHistory().put(time, utilization);
			return utilization;
			
		}else{
//			Log.format("Is Contained %b \n", getHistory().containsKey(time));
			if(((time - 0.1) % 300 == 0 || time == 0) && !getHistory().containsKey(time)){
		getHistory().put(time, utilization);
		Log.format("Time: %f, Generate Utilization %f.2 \n", time, utilization);
		return utilization;
		}else{
			double time1 = (int) time / 300 * 300 + 0.1;
			return getHistory().get(time1);
		}
		}
		
	}

	public void setUtilization(double utilization, double time){
		isDimmerTriggered = true;
		this.dimmerUtilization = utilization;
//		double utilizationAfterDimmer= getHistory().get(time) * dimmerUtilization; 
		getHistory().remove(time);
//		Log.format("Update Utilization \n");
//		getHistory().put(time, utilizationAfterDimmer);
//		getUtilization(time);
		
	}
	
	
	/**
	 * Gets the history.
	 * 
	 * @return the history
	 */
	protected Map<Double, Double> getHistory() {
		return history;
	}

	/**
	 * Sets the history.
	 * 
	 * @param history the history
	 */
	protected void setHistory(Map<Double, Double> history) {
		this.history = history;
	}

	/**
	 * Save history.
	 * 
	 * @param filename the filename
	 * @throws Exception the exception
	 */
	public void saveHistory(String filename) throws Exception {
		FileOutputStream fos = new FileOutputStream(filename);
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		oos.writeObject(getHistory());
		oos.close();
	}

	/**
	 * Load history.
	 * 
	 * @param filename the filename
	 * @throws Exception the exception
	 */
	@SuppressWarnings("unchecked")
	public void loadHistory(String filename) throws Exception {
		FileInputStream fis = new FileInputStream(filename);
		ObjectInputStream ois = new ObjectInputStream(fis);
		setHistory((Map<Double, Double>) ois.readObject());
		ois.close();
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
