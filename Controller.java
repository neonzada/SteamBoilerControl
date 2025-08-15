import javax.realtime.*;

public class Controller extends RealtimeThread{
	public enum Mode { INITIALIZATION, NORMAL, DEGRADED, SALVAGE, EMERGENCY_STOP }
	private long startTime;
	private Boiler boiler;
	private Sensors sensors;
	private PID pid;
	private double setpoint = (Constants.N1 + Constants.N2)/2.0;
	private Mode mode = Mode.INITIALIZATION;

	public Controller(Boiler b, Sensors s, PeriodicParameters params, long startTime){
		super(new PriorityParameters(PriorityScheduler.instance().getMaxPriority() - 10), params, null, null, null, null);
		this.boiler = b;
		this.sensors = s;
		this.pid = new PID(0.25, 0.02, 0.0, 5.0, 0.0, Constants.NUM_PUMPS * Constants.P);
		this.startTime = startTime;
	}

	@Override
	public void run(){
		while(true){
			Double qMeasure = sensors.readLevel();
			Double vMeasure = sensors.readSteam();
			int operant = boiler.countOperantPumps();

			if(qMeasure == null){
				mode = Mode.SALVAGE;
			}else if(operant == 0){
				mode = Mode.EMERGENCY_STOP;
			}else if(operant < Constants.NUM_PUMPS){
				mode = Mode.DEGRADED;
			}else{
				mode = (mode == Mode.INITIALIZATION) ? Mode.NORMAL : mode; //preserves previous state
			}
			if(qMeasure != null && (qMeasure < Constants.M1 || qMeasure > Constants.M2)){
				mode = Mode.EMERGENCY_STOP;
			}

			switch(mode){
				case NORMAL:
					pidControl(qMeasure, vMeasure);
					break;
				case DEGRADED:
					pidControl(qMeasure, vMeasure);
					break;
				case SALVAGE:
					salvageControl(vMeasure);
					break;
				case EMERGENCY_STOP:
					emergencyStop();
					break;
				default:
					break;
			}

			//log status
			long elapsedSec = (System.nanoTime() - startTime) / 1_000_000_000L;
			System.out.println("============================================================================================");
			System.out.printf(
				"[CTRL] t=%ds | Mode=%-15s | Level=%.1f L | Steam=%.1f L/s | Pumps:",
				elapsedSec,
				mode,
				(qMeasure != null ? qMeasure : Double.NaN),
				(vMeasure != null ? vMeasure : Double.NaN)
			);
			for(int i = 0; i < Constants.NUM_PUMPS; i++){
				double pVal = boiler.getPumpThroughput(i);
				System.out.printf(" #%d=%.1f", i+1, pVal);
			}
			System.out.println("\n============================================================================================");

			waitForNextPeriod();
		}
	}

	private void pidControl(Double q, Double vMeasure){
		int operant = boiler.countOperantPumps();
		if(operant == 0){ mode = Mode.EMERGENCY_STOP; return; }

		//feedforward - current output (nominal)
		double ff = (vMeasure != null ? vMeasure : Constants.V);
		double uCap = operant * Constants.P;

		pid.setOutputLimits(0.0 - ff, uCap - ff);

		if(q == null){
			pid.setEnableIntegral(false);
			return;
		}else{
			pid.setEnableIntegral(true);
		}

		double u_pid = pid.update(setpoint, q);
		double u = ff + u_pid;
		if(Double.isNaN(u)) { return; }

		if(u > uCap) u = uCap;
		if(u < 0.0) u = 0.0;

		double perPump = u / operant;
		for(int i = 0; i < Constants.NUM_PUMPS; i++){
			if(!boiler.isPumpFailed(i)){
				boiler.setPumpThroughput(i, perPump);
			}else{
				boiler.setPumpThroughput(i, 0.0);
			}
		}
	}

	private void salvageControl(Double vMeasure){
		double estimatedQ = boiler.getQ();
		if(vMeasure != null){
			if(vMeasure > Constants.V * 0.9){
				// high steam output -> decreasing q
				boiler.setPumpThroughput(1, Constants.P);
				boiler.setPumpThroughput(2, Constants.P/2.0);
			}else{
				boiler.setPumpThroughput(1, 0.0);
				boiler.setPumpThroughput(2, 0.0);
			}
		}else{
			mode = Mode.EMERGENCY_STOP;
		}
	}

	private void emergencyStop(){
		for(int i = 0; i < Constants.NUM_PUMPS; i++){
			boiler.setPumpThroughput(i, 0.0);
		}
	}
}
