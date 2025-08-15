/*
- A valve for evacuation of water. It serves only to empty the steam-boiler in  its initial phase. 

- Its total capacity C (indicated in litres).

- The minimal limit quantity M1 of water (in litres). Below M1 the steam-boiler would be in danger after five seconds,
  if the steam continued to come out at its maximum quantity without supply of water from the pumps. 

- The maximal limit quantity M2 of waters (in litres). Above 1]2 the steam-boiler would be in danger after five seconds,
  if the pumps continued to supply the steam-boiler with water without possibility to evacuate the steam. 

- The minimal normal quantity N1 of water in litres to be maintained in the steam-boiler during regular operation (M1 < N1). 

- The maximal normal quantity N2 of water (in litres) to be maintained in the steam-boiler during regular operation (N2 < M2). 

- The maximum quantity W of steam (in litres/sec) at the exit of the steam-boiler. 

- The maximum gradient U1 of increase of the quantity of steam (in litres/sec/sec). 

- The maximum gradient U2 of decrease of the quantity of steam (in litres/sec/sec).
 */
public class Boiler {
	private double q; //water quant.
	private double v; //steam out

	private final double[] pumpThroughput;
	private final boolean[] pumpFailed;

	private boolean levelSensorFailed = false;
	private boolean steamSensorFailed = false;

	public Boiler(double initialQ){
		this.q = initialQ;
		this.v = Constants.V;
		this.pumpThroughput = new double[Constants.NUM_PUMPS];
		this.pumpFailed = new boolean[Constants.NUM_PUMPS];
	}

	public synchronized void stepOneSecond(){
		double pTotal = 0.0;
		for(int i = 0; i < Constants.NUM_PUMPS; i++){
			if(!pumpFailed[i]) pTotal += pumpThroughput[i];
		}

		q += pTotal - v;
		//bound checking
		if(q < 0.0) q = 0.0;
		if(q > Constants.C) q = Constants.C;
	}

	public synchronized double getQ() { return q; }
	public synchronized double getV() { return v; }

	public synchronized void setPumpThroughput(int idx, double throughput){
		if(idx < 0 || idx >= Constants.NUM_PUMPS) return;
		pumpThroughput[idx] = Math.max(0.0, Math.min(Constants.P, throughput));
	}

	public synchronized double getPumpThroughput(int idx){
		return pumpThroughput[idx];
	}

	public synchronized void failPump(int idx, boolean fail){
		if(idx < 0 || idx >= Constants.NUM_PUMPS) return;
		pumpFailed[idx] = fail;
		if(fail) pumpThroughput[idx] = 0.0;
	}

	public synchronized boolean isPumpFailed(int idx){
		return pumpFailed[idx];
	}

	public synchronized int countOperantPumps(){
		int count = 0;
		for(int i = 0; i < Constants.NUM_PUMPS; i++){
			if(!pumpFailed[i]) count++;
		}
		return count;
	}

	public synchronized void failLevelSensor(boolean fail) { levelSensorFailed = fail; }
    public synchronized void failSteamSensor(boolean fail) { steamSensorFailed = fail; }
    public synchronized boolean isLevelSensorFailed() { return levelSensorFailed; }
    public synchronized boolean isSteamSensorFailed() { return steamSensorFailed; }
}
