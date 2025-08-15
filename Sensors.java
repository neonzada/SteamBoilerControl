import java.util.Random;

public class Sensors{
	private Boiler boiler;
	private Random rng = new Random();

	public Sensors(Boiler b){ this.boiler = b; }

	public synchronized Double readLevel(){
		if(boiler.isLevelSensorFailed()) return null;
		return boiler.getQ() + rng.nextGaussian()*0.5;
	}
	public synchronized Double readSteam(){
		if(boiler.isSteamSensorFailed()) return null;
		return boiler.getV() + rng.nextGaussian()*0.1;
	}
}