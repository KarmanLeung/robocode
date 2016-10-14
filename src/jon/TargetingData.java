package jon;

public class TargetingData {
	
	private double bearing;
	private double firepower;
	private long time;
	private String target;
	private double origBearing;
	
	
	public TargetingData(double bearing, double firepower, long time, String target, double origBearing) {
		this.bearing = bearing;
		this.firepower = firepower;
		this.time = time;
		this.target = target;
		this.origBearing = origBearing;
	}

	

	public double getOrigBearing() {
		return origBearing;
	}



	public void setOrigBearing(double origBearing) {
		this.origBearing = origBearing;
	}



	public String getTarget() {
		return target;
	}



	public void setTarget(String target) {
		this.target = target;
	}



	public double getBearing() {
		return bearing;
	}


	public void setBearing(double bearing) {
		this.bearing = bearing;
	}


	public double getFirepower() {
		return firepower;
	}


	public void setFirepower(double firepower) {
		this.firepower = firepower;
	}


	public long getTime() {
		return time;
	}


	public void setTime(long time) {
		this.time = time;
	}
	
	

}
