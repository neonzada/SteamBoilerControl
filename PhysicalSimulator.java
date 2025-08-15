import javax.realtime.*;

public class PhysicalSimulator extends RealtimeThread{
	private Boiler boiler;
	private long startTime;
	public PhysicalSimulator(Boiler b, PeriodicParameters pp, long startTime){
		super(new PriorityParameters(PriorityScheduler.instance().getMaxPriority() - 5), pp, null, null, null, null);
		this.boiler = b;
		this.startTime = startTime;
	}
	public void run(){
		while(true){
			waitForNextPeriod();
			boiler.stepOneSecond();
			long elapsedSec = (System.nanoTime() - startTime) / 1_000_000_000L;

			System.out.printf(
				"[PHYS] t=%ds | Level=%.1f L | Steam=%.1f L/s\n",
				elapsedSec,
				boiler.getQ(),
				boiler.getV()
			);
		}
	}
}
