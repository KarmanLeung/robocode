package jon;

import java.awt.geom.Point2D;


import robocode.ScannedRobotEvent;

public class EnemyTank {
	
	private ScannedRobotEvent enemy;
	private String name;
	private double currentX;
	private double currentY;
	private long currentGameTime;
	private double energy;
	
	
	EnemyTank(ScannedRobotEvent e, double myHeading, double myX, double myY) {
		update( e , myHeading,  myX,  myY);
	}
	
	public void update(ScannedRobotEvent e , double myHeading, double myX, double myY) {
		this.enemy = e;
		this.name = e.getName();
		
		double absBearingDeg = (myHeading + e.getBearing());
		if (absBearingDeg < 0) {
			absBearingDeg += 360;
		}
		
		this.currentX = myX + Math.sin(Math.toRadians(absBearingDeg)) * e.getDistance();
		this.currentY = myY + Math.cos(Math.toRadians(absBearingDeg)) * e.getDistance();
		this.currentGameTime = e.getTime();
		this.energy = e.getEnergy();
	}
	
	private double toBeX(long bulletTime) {
		return currentX + Math.sin(Math.toRadians(enemy.getHeading())) * enemy.getVelocity() * bulletTime;
	}
	
	private double toBeY(long bulletTime) {
		return currentY + Math.cos(Math.toRadians(enemy.getHeading())) * enemy.getVelocity() * bulletTime;
	}
	
	public double changeInEnergy(ScannedRobotEvent e) {
		return this.energy - e.getEnergy();
	}
	
	// computes the absolute bearing between two points
	public double futureAbsBearing(long bulletTime) {
		double xo = toBeX(bulletTime) - currentX;
		double yo = toBeY(bulletTime)- currentY;
		double hyp = Point2D.distance(currentX, currentY, toBeX(bulletTime), toBeY(bulletTime));
		double arcSin = Math.toDegrees(Math.asin(xo / hyp));
		double bearing = 0;

		if (xo > 0 && yo > 0) { // both pos: lower-Left
			bearing = arcSin;
		} else if (xo < 0 && yo > 0) { // x neg, y pos: lower-right
			bearing = 360 + arcSin; // arcsin is negative here, actuall 360 - ang
		} else if (xo > 0 && yo < 0) { // x pos, y neg: upper-left
			bearing = 180 - arcSin;
		} else if (xo < 0 && yo < 0) { // both neg: upper-right
			bearing = 180 - arcSin; // arcsin is negative here, actually 180 + ang
		}

		return bearing;
	}

}
