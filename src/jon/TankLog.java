package jon;

import java.awt.geom.Point2D.Double;
import robocode.ScannedRobotEvent;

public class TankLog {

	private long time;
	private double distance;
	private double velocity;
	private double heading;
	private double energy;
	private double bearing;
	private String name;
	private Double orgin;

	TankLog(ScannedRobotEvent e, Double orgin, double bearing){
		this.time = e.getTime();
		this.distance = e.getDistance();
		this.velocity = e.getVelocity();
		this.energy = e.getEnergy();
		this.heading = e.getHeading();
		this.bearing = bearing;
		this.name = e.getName();
		this.orgin = orgin;

	}
	
	
	public double getBearing() {
		return bearing;
	}


	public void setBearing(double bearing) {
		this.bearing = bearing;
	}


	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}


	public Double getOrgin() {
		return orgin;
	}
	
	public void setOrgin(Double orgin) {
		this.orgin = orgin;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public double getVelocity() {
		return velocity;
	}

	public void setVelocity(double velocity) {
		this.velocity = velocity;
	}

	public double getHeading() {
		return heading;
	}

	public void setHeading(double heading) {
		this.heading = heading;
	}

	public double getEnergy() {
		return energy;
	}

	public void setEnergy(double energy) {
		this.energy = energy;
	}

	public void setDistance(double distance) {
		this.distance = distance;
	}

	public double getDistance(){
		// getting closer or further away?
		return distance;
	}


}
