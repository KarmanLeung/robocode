package jon;

import java.awt.geom.Point2D.Double;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Utils {
	
	public TargetingData getTargetData(List<TankLog> logs) {
		
		TankLog rLog = logs.get(logs.size() - 1);
		Double orgin = rLog.getOrgin();
		Map<Double, Integer> previous = new HashMap<>();
		
		for(TankLog log : logs) {
			Double target = calculateXY(log.getOrgin(), log.getDistance(), log.getBearing());
			if(previous.putIfAbsent(target, 1) == null) {
				Integer val = previous.get(target);
				val++;
			}
		}
		Map<Double, Integer> commonPoints = new HashMap<>();
		
		
		for(Map.Entry<Double, Integer> entry : previous.entrySet() ) {
			if(entry.getValue() > 1) {
				commonPoints.put(entry.getKey(), entry.getValue());
			}
		}
		
		if( commonPoints.size() > 0 ) {
			// TODO which to pick
			Double target = previous.entrySet().iterator().next().getKey();
			return getTargetData(orgin, rLog.getName(), distanceBetweenPoints(orgin, target), calculateBearing( orgin,  target), 0, 0);
		}
		
		
		return getTargetData( rLog.getOrgin(), rLog.getName(), rLog.getDistance(), rLog.getBearing(), rLog.getVelocity(), rLog.getHeading());
	}
	
	
	public TargetingData getTargetData(TankLog log) {
		return getTargetData( log.getOrgin(), log.getName(), log.getDistance(), log.getBearing(), log.getVelocity(), log.getHeading());
	}
	
	
	public TargetingData getTargetData(Double orgin, String targetName, double distance, double bearing, double velocity, double heading) {
		
		double firePower = distance < 200 ? 3 : distance < 350 ? 2 : distance < 500 ? 1 : 0.5;
		double bulletSpeed = 20 - firePower * 3;
		
		
		Double target = calculateXY( orgin, distance, bearing);
		Double targetNew = calculateFutureXY( target,  velocity,  heading,  (long)( distance / bulletSpeed ) );
		
		double distanceNew = distanceBetweenPoints(orgin, targetNew);
		long bulletTimeNew = (long)(distanceNew / bulletSpeed);
		
		return new TargetingData(calculateBearing( orgin,  targetNew), firePower, bulletTimeNew, targetName, bearing);
	}
	
	
	public double distanceBetweenPoints(Double point1, Double point2) {
		return Math.hypot(point2.getX() - point1.getX(), point2.getY() - point1.getY()) ;
	}

	public Double calculateXY(Double orgin, double distance, double bearing){
		
		double xOffset = distance * Math.sin(Math.toRadians(bearing)) ;
		double yOffset = distance * Math.cos(Math.toRadians(bearing)) ;

		return new Double(xOffset + orgin.getX(), yOffset + orgin.getY());
	}
	
	
	public Double calculateFutureXY(Double orgin, double velocity, double heading, long time){

		double xNew = orgin.getX() + (velocity * time * Math.sin(Math.toRadians(heading)));
		double yNew = orgin.getY() + (velocity * time * Math.cos(Math.toRadians(heading)));
		
		return new Double(xNew, yNew);
	}
	
	
	public double calculateBearing(Double orgin, Double target){
		
		double x = target.getX() - orgin.getX();
		double y = target.getY() - orgin.getY();
		
		return Math.toDegrees(Math.atan2(x,y));
	}
	
	public double normalizeBearing(double angle) {
		while (angle >  180) angle -= 360;
		while (angle < -180) angle += 360;
		return angle;
	}
	

}
