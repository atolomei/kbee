package com.novamens.metrics;

public class MeanEstimator {
	
	private final double ALPHA = 0.15;
	private final double BETA = 1.0-ALPHA;

	private String name;
	
	private double alpha = ALPHA;
	private double beta = BETA;
	private double est_minus1 = 0;
	private double est = 0;

	private double sum = 0;
	private double total = 0;
	
	public  MeanEstimator (String name) {
		this.name=name;
	}
	
	public String getName() {
		return name;
	}
	
	public double getMean() {
		return this.est;
	}
	
	
	public double getAverageMean() {
		if (total==0)
			return 0;
		return this.sum / this.total;
	}
	
	public void reset() {
		est_minus1 = 0;
		est = 0;
		total = 0;
	}
	
	public void addValue(double value) {
		estimate(value);
	}
	
	private void estimate(double value) {

		if (value==-1)
			return;
		
		if (est_minus1==0)  
			est_minus1 = value;
		
		est = alpha * value + beta * est_minus1;
		est_minus1 = est;
		
		total+=1;
		sum+=value;
		
	}

}
