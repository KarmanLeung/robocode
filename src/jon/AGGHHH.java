package jon;

import java.awt.Color;
import java.awt.Point;
import java.awt.geom.Point2D.Double;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import robocode.AdvancedRobot;
import robocode.HitByBulletEvent;
import robocode.RobotDeathEvent;
import robocode.ScannedRobotEvent;
import robocode.TurnCompleteCondition;
import robocode.WinEvent;

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
		setScanColor(Color.CYAN);

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
			List<Tank> enemys = tanks.stream().
					sorted(Comparator.comparing( p -> p.getScore() )).
					collect(Collectors.toList());
			
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
	
	
	private Double sentry = new Double(0, 0);
	private double mapWidth = 1000;
	private double mapHeight = 800;
	private final int rows = 8;
    private final int cols = 10;
    Double destination = null;
	
	private void seletTargetLocation() {
	    //TODO: find sentry and set coordinates
	    Point sentryQuadrant = toQuadrant(sentry);
	    Point ourQuadrant = toQuadrant(new Double(getX(), getY()));
	    
        int nq = rand.nextInt(8);
	    for(int i=0; i<20; ++i) {
	        Point nextQuadrant = relativeQudarantToAbsolute(ourQuadrant, nq);
	        if(isSafe(nextQuadrant, sentryQuadrant)) {
	            destination = pickCoordinatesFromQuadrant(nextQuadrant);
	            break;
	        } else {
	            nq =  rand.nextInt(8);
	        }
	    }
	    
	}
	
	
	private Double pickCoordinatesFromQuadrant(Point quadrant){
	    return new Double((quadrant.x + 0.5)*mapWidth/(double)cols, (quadrant.y + 0.5)*mapHeight/(double)rows);
	}
	
	/**
	 * Checks this quadrant is not too close to sentry and inside the field
	 */
	private boolean isSafe(Point nextQuadrant, Point sentryQuadrant) {
        if (Math.abs(nextQuadrant.x-sentryQuadrant.x)>1 || Math.abs(nextQuadrant.y-sentryQuadrant.y)>1) {
            return (nextQuadrant.x >= 0 && nextQuadrant.y >= 0 && nextQuadrant.x < cols && nextQuadrant.y < rows);  
        }
        return false;
    }

    Random rand = new Random();
	
	private Point toQuadrant(Double coords) {
	    return new Point((int)(coords.x / mapWidth) * cols, (int)(coords.y / mapHeight) * rows);
	}
	
	
	private Point relativeQudarantToAbsolute(Point center, int neigbouringQuadrant) {
	    switch (neigbouringQuadrant) {
        case 0:
            return new Point(center.x-1, center.y-1);
        case 1:
            return new Point(center.x, center.y-1);
        case 2:
            return new Point(center.x+1, center.y-1);
        case 3:
            return new Point(center.x+1, center.y);
        case 4:
            return new Point(center.x+1, center.y+1);
        case 5:
            return new Point(center.x, center.y+1);
        case 6:
            return new Point(center.x-1, center.y+1);
        case 7:
            return new Point(center.x-1, center.y);
        default:
            return center;
        }
	}
}
