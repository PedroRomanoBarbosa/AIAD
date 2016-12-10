package models;

public class SinalphaModel implements Model{
	private double start_alpha = (3 * Math.PI) / 2;
	private int lambda_increase = 1;
	private double lambda_decrease = -1.5;
	private double omega = Math.PI / 10;
	private double alpha = 0.0; // alpha value varies between 3pi/2 and 5pi/2
	
	public double calculateTrustworthiness(double... ParamsToCheck){
		if (ParamsToCheck.length != 1) {
	         throw new IllegalArgumentException();  // or return false
	    }
		double delay = ParamsToCheck[0];
		double trustworthiness = 0.0;
		if (delay == 0){
			this.alpha = this.start_alpha + this.lambda_increase * this.omega;
		}
		else if (delay > 0){
			this.alpha = this.start_alpha - this.lambda_decrease * this.omega;
		}
		else
			System.out.println("Wrong delay value.\n");
		if (this.alpha < ((3*Math.PI)/2)){
			this.alpha = 3*Math.PI/2;
		}
		else if (this.alpha < ((5*Math.PI)/2)){
			this.alpha = 5*Math.PI/2;
		}
		return trustworthiness = 0.5 * Math.sin(this.alpha) + 0.5;
	}
}
