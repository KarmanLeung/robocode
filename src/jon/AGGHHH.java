package jon;

import java.awt.geom.Point2D.Double;
import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import robocode.AdvancedRobot;
import robocode.CustomEvent;
import robocode.HitByBulletEvent;
import robocode.HitRobotEvent;
import robocode.HitWallEvent;
import robocode.RadarTurnCompleteCondition;
import robocode.RobotDeathEvent;
import robocode.ScannedRobotEvent;
import robocode.TurnCompleteCondition;
import robocode.WinEvent;
import robocode.util.Utils;

public class AGGHHH extends AdvancedRobot {

	double BFW = 100;
	double BFH = 100;

	double previousEnergy = 100d;
	int direction = 1;
	boolean avoidInProgress = false;
	
	private boolean movingForward = true;
	private String target = null;

	ConcurrentHashMap<String, Tank> scannedRobots = new ConcurrentHashMap<>();

	
	public int sign(double n){
		return n >= 0 ? 1 : -1;
	}
	
	
	
	
	public void onHitByBullet(HitByBulletEvent e){
		double bearing = e.getBearing(); //Get the direction which is arrived the bullet.
		//if(getEnergy() < 50){ // if the energy is low, the robot go away from the enemy
			setTurnRight( e.getBearing() + 90); 
			setAhead(75); 
		//}
	}
	
	
	public void run() {

		 BFW = getBattleFieldWidth();
		 BFH = getBattleFieldHeight();

		
		setAdjustRadarForRobotTurn(true);
		setBodyColor(Color.green);
		setGunColor(Color.black);
		setRadarColor(Color.yellow);
		setScanColor(Color.yellow);

		// TODO work out where we are to determine which way to turn radar first (same time)
		
		setAdjustRadarForGunTurn(true);
		setTurnRadarRight(360);

		
		Utils utils = new Utils();
		
		

		while(true) {
			
			setTurnRadarLeftRadians(999999999);
			getScannedRobotEvents();
			
//			if(avoid()) {
//				// adjusting for wall, or tank
//			}
//			else {
//				avoidInProgress = false;
//				setTurnLeft(360 * randomFactor()); 
//				waitFor(new TurnCompleteCondition(this));
//				setAhead(200);
//			}
			
			doIt();
			
			waitFor(new TurnCompleteCondition(this));
		}
	}
	
	public double randomFactor() {
		return Math.random();
	}
	
	public boolean avoid(){
		Utils utils = new Utils();
		long time = 2;
		
		if( avoidInProgress ) {
			return true;
		}
		Double future = utils.calculateFutureXY(new Double(getX(),  getY()), getVelocity(), getHeading(), time );
		
		if( future.getX() > getBattleFieldWidth() || future.getY() > getBattleFieldHeight() || future.getX() < 0 || future.getY() < 0) {
			
			setTurnLeft(180);
			
			setAhead(50);
			avoidInProgress = true;
			return true;
		}
			
		return false;
	}
	

	
	private void setTarget(String target) {
		this.target = target;
	}
	private String getTarget(){
		return target;
	}
	
	public boolean haveFired() {
		return scannedRobots.values().stream().
		filter( p -> p.hasFired() ).
		collect(Collectors.toList()).size() > 0;
	}
	
	public void selectTheBestTarget() {
		
		List<Tank> tanks = scannedRobots.values().stream().filter( p -> ! p.isSentry() ).filter( p -> p.isAlive() ).collect(Collectors.toList());
		if( tanks.size() == 0 ) {
			setTarget(null);
		} else if (tanks.size() == 1) {
			setTarget(tanks.get(0).getName());
		} else {
			// get closest 
			List<Tank> enemys = tanks.values().stream().
					sorted(Comparator.comparing( p -> p.getScore() )).
					collect(Collectors.toList()).iterator().next();
			
				setTarget(enemys.get(0).getName());
			
			
		}
		
	}

	public void onRobotDeath(RobotDeathEvent e) {
		if ( scannedRobots.containsKey(e.getName()) ) {
			scannedRobots.get(e.getName()).setDead(true);
			scannedRobots.remove(e.getName());
		}
		selectTheBestTarget();
	}

	public void onWin(WinEvent e) {
	  while(true){	
		turnRight(360);
		turnLeft(360);
	  }
	}

	public void doMove() {
		setTurnRight(scannedRobots.get(getTarget()).getBearing() + 90);
		// strafe by changing direction every 20 ticks
		if (getTime() % 20 == 0) {
			direction *= -1;
			setAhead(150 * direction);
		}
	}
	
	public void moveTowardsTarget() {
		if( scannedRobots.get(getTarget()).getDistance() - 60 > 0 ) {
 		   setTurnRight(scannedRobots.get(getTarget()).getBearing() );
		    setAhead(scannedRobots.get(getTarget()).getDistance() - 50);
		}
		if ( scannedRobots.get(getTarget()).getBearing() < 80 || scannedRobots.get(getTarget()).getBearing() > 100) {
			setTurnRight(scannedRobots.get(getTarget()).getBearing() + 90);
		}
		// strafe by changing direction every 20 ticks
		if( haveFired() ) {
			direction = -direction;
			setAhead(60 * direction);
		}
	}

	public void doIt(){
		if(getTarget() != null) {
			TargetingData data = scannedRobots.get(getTarget()).getTargetDataCommon(getTime() - 20);
			
			setTurnGunRight(  normalizeBearing( data.getBearing() - getGunHeading() ));
			fireMyBigGun(data.getFirepower());  				
		}
	}
	
	
	public void onScannedRobot( ScannedRobotEvent e) {
		
		// calc bearing
		double bearing = normalizeBearing(getHeading() + e.getBearing());
		
		if( scannedRobots.containsKey(e.getName())) {
			scannedRobots.get(e.getName()).addScanEvent(e, new Double(getX(), getY()), bearing);
		} else {
			scannedRobots.put(e.getName(), new Tank(e.getName(), e, new Double(getX(), getY()), bearing));
		}

		selectTheBestTarget();
		doIt();
		
	}
	
//	public void onHitWall(HitWallEvent e) {
//		reverseDirection();
//	}
	

	// normalizes a bearing to between +180 and -180
	double normalizeBearing(double angle) {
		while (angle >  180) angle -= 360;
		while (angle < -180) angle += 360;
		return angle;
	}




	private void reverse() {
		setTurnRight(-getHeading()); 
		setAhead(9999);	
		waitFor(new TurnCompleteCondition(this));
	}



	public void fireMyBigGun(double s) {
		if(getGunHeat() == 0 && Math.abs(getGunTurnRemaining()) < 5) {
			fire(getEnergy() > s ? s : 0.1 );
		}
	}
	
	
}
