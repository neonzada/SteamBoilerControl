import javax.realtime.*;

public class App {
	public static void main(String[] args){
		long startTime = System.nanoTime();
		Boiler boiler = new Boiler(500.0);
		Sensors sensors = new Sensors(boiler);

		PeriodicParameters physParams = new PeriodicParameters(null, new RelativeTime(1000, 0));
		PeriodicParameters ctrlParams = new PeriodicParameters(null, new RelativeTime(5000, 0));

		PhysicalSimulator phys = new PhysicalSimulator(boiler, physParams, startTime);
		Controller ctrl = new Controller(boiler, sensors, ctrlParams, startTime);

		phys.start();
		ctrl.start();

		boolean faultInjection = false;

		if(faultInjection){
			new Thread(() -> {
				try{
					Thread.sleep(15000);
					System.out.println(">>> FAILING pump 1");
					boiler.failPump(0, true);
	
					Thread.sleep(10000);
					System.out.println(">>> FAILING level sensor");
					boiler.failLevelSensor(true);
	
					Thread.sleep(15000);
					System.out.println(">>> REPAIRING pump 1 and level sensor");
					boiler.failPump(0, false);
					boiler.failLevelSensor(false);
	
					Thread.sleep(10000);
					System.out.println(">>> FAILING ALL pumps");
					for (int i = 0; i < Constants.NUM_PUMPS; i++) boiler.failPump(i, true);
				}catch(InterruptedException e){
					e.printStackTrace();
				}
			}).start();
		}
	}
}
