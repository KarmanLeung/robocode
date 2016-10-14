package jon;

import java.util.List;
import java.util.stream.Collectors;

import robocode.ScannedRobotEvent;

public class EnemyLog {

	private double x;
	private double y;
	private long time;
	private double distance;
	private double velocity;
	private double heading;
	private double energy;
	private double bearing;
	private double myX;
	private double myY;
	private String name;
	private double origBearing;

	EnemyLog(ScannedRobotEvent e, double myX, double myY, double bearing){
		this.time = e.getTime();
		this.distance = e.getDistance();
		this.velocity = e.getVelocity();
		this.energy = e.getEnergy();
		this.heading = e.getHeading();
		this.bearing = bearing;
		this.origBearing = e.getBearing();
		this.myX = myX;
		this.myY = myY;
		this.name = e.getName();

		calculateXY();

	}
	
	public TargetingData calculateShot(List<EnemyLog> logs ){
		// TODO handle distance changes caused by target moment / my momement
		double firePower = distance < 200 ? 3 : distance < 350 ? 2 : distance < 500 ? 1 : 0.5;
		double bulletSpeed = 20 - firePower * 3;
		long bulletTime = (long)(distance / bulletSpeed);
		
		double expectedBearing = calculateFutureXYBearing(logs, bulletTime);
		
		return new TargetingData(expectedBearing, firePower, bulletTime, name, getOrigBearing());
		
	}
	
	
	
	public double getX() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
	}

	public double getY() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
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

	public double getOrigBearing() {
		return origBearing;
	}

	public void setOrigBearing(double origBearing) {
		this.origBearing = origBearing;
	}

	public double getDistance(){
		// getting closer or further away?
		return distance;
	}

	private void calculateXY(){
		double xOffset = distance * Math.sin(Math.toRadians(bearing)) ;
		double yOffset = distance * Math.cos(Math.toRadians(bearing)) ;

		this.x = myX + xOffset;
		this.y = myY + yOffset;
	}
	
	
	private double averageVol(List<EnemyLog> logs, long time){
		double howFarBack = getTime() - time;
		double totalVol = 0;
		List<EnemyLog> vLogs = logs.stream().filter( l -> l.getTime() >= howFarBack ).collect(Collectors.toList());
		
		for( EnemyLog vLog : vLogs ) {
			totalVol += vLog.getVelocity();
		}
		return totalVol / vLogs.size();
	}
	
	private double calculateFutureXYBearing(List<EnemyLog> logs, long time){
		//double avgVol = averageVol(logs, time);
		double xOffset = x + (velocity * time * Math.sin(Math.toRadians(heading)));
		double yOffset = y + (velocity * time * Math.cos(Math.toRadians(heading)));
		
		double futureX = xOffset - myX;
		double futureY = yOffset - myY;
		
		double bearing = Math.toDegrees(Math.atan2(futureX,futureY));
		
		return bearing;
	}

}
