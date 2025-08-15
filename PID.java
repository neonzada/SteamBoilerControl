public class PID {
	private double Kp, Ki, Kd;
	private double Ts;
	private double integrator = 0.0;
	private double prevError = 0.0;
	private double prevMeasure = Double.NaN;
	private double derivFiltered = 0.0;
	private double alpha = 0.9; //derivative filter

	private double outMin, outMax;
	private boolean enableIntegral = true;

	public PID(double Kp, double Ki, double Kd, double Ts, double outMin, double outMax){
		this.Kp = Kp;
		this.Ki = Ki;
		this.Kd = Kd;
		this.Ts = Ts;

		this.outMin = outMin;
		this.outMax = outMax;
	}

	public synchronized void setOutputLimits(double min, double max){
		this.outMin = min;
		this.outMax = max;
	}
	public synchronized void setEnableIntegral(boolean enable){
		this.enableIntegral = enable;
		if(!enable) integrator = 0.0;
	}

	public synchronized double update(double setpoint, Double measurement){
		if(measurement == null) return Double.NaN;

		double error = setpoint - measurement;

		// proportional control
		double P = Kp * error;

		// integral control via trapezium rule
		if(enableIntegral){
			integrator += Ki * Ts * (error + prevError) * 0.5;
		}

		// derivative control
		double D = 0.0;
		if(!Double.isNaN(prevMeasure) && Kd != 0.0){
			double dRaw = Kd * (prevMeasure - measurement) / Ts;
			derivFiltered = alpha * derivFiltered + (1.0 - alpha) * dRaw;
			D = derivFiltered;
		}

		double u_unbounded = P + integrator + D;

		double u = Math.max(outMin, Math.min(outMax, u_unbounded)); //saturacao

		// anti-windup: saturated -> rollback to 
		if(enableIntegral){
			if(u != u_unbounded){
				integrator -= Ki * Ts * (error + prevError) * 0.5;
			}
		}
		
		prevError = error;
		prevMeasure = measurement;
		return u;
	}
}
