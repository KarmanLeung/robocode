package jon;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import robocode.AdvancedRobot;
import robocode.HitByBulletEvent;
import robocode.HitRobotEvent;
import robocode.HitWallEvent;
import robocode.RobotDeathEvent;
import robocode.ScannedRobotEvent;
import robocode.TurnCompleteCondition;
import robocode.WinEvent;

public class Banana extends AdvancedRobot {

	double BFW = 100;
	double BFH = 100;

	Double previousEnergy = 100d;
	int direction = 1;
	
	private boolean movingForward = true;
	private String target = null;

	ConcurrentHashMap<String, Enemy> scannedRobots = new ConcurrentHashMap<>();

	public void run() {

		 BFW = getBattleFieldWidth();
		 BFH = getBattleFieldHeight();

		
		setAdjustRadarForRobotTurn(true);
		setBodyColor(Color.yellow);
		setGunColor(Color.black);
		setRadarColor(Color.blue);
		setScanColor(Color.yellow);

		// TODO work out where we are to determine which way to turn radar first (same time)
		turnRadarLeft(360);

		while(true) {
			
			setTurnRadarLeftRadians(Double.POSITIVE_INFINITY);
			
			waitFor(new TurnCompleteCondition(this));

		}


	}
	public void reverseDirection() {
		if (movingForward) {
			setBack(40000);
			movingForward = false;
		} else {
			setAhead(40000);
			movingForward = true;
		}
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
		if( scannedRobots.size() == 0 ) {
			setTarget(null);
		} else if (scannedRobots.size() == 1) {
			setTarget(scannedRobots.keys().nextElement());
		} else {
			// get closest 
			Enemy enemy = scannedRobots.values().stream().
					filter( p -> p.isAlive() ).
					sorted(Comparator.comparing( p -> p.getDistance() )).
					collect(Collectors.toList()).iterator().next();
			setTarget(enemy.getName());
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

	private void buildWalls(Vector<GravPoint> gravpoints){
		List<Integer> width  = Stream.iterate(1, n -> n + 1).limit( (int) getBattleFieldWidth() ).collect(Collectors.toList());
		List<Integer> hieght  = Stream.iterate(1, n -> n + 1).limit( (int) getBattleFieldHeight() ).collect(Collectors.toList());
		
		double wall = -0.3;
		
		for(Integer w : width) {
			gravpoints.addElement(new GravPoint(1, w, wall));
			gravpoints.addElement(new GravPoint(getBattleFieldHeight(), w, wall));
		}
		for(Integer h : hieght) {
			gravpoints.addElement(new GravPoint(h, 0, wall));
			gravpoints.addElement(new GravPoint(h, getBattleFieldWidth(), wall));
		}
	}
	
	private void addTanks(Vector<GravPoint> gravpoints) {
		List<Enemy> enemys = scannedRobots.values().stream().
		filter( p -> p.isAlive() ).
		sorted(Comparator.comparing( p -> p.getDistance() )).
		collect(Collectors.toList());
		
		for(Enemy enemy : enemys ) {
			EnemyLog l = enemy.getLog().get(enemy.getLog().size() - 1);
			double force = Math.pow(l.getDistance() ,2);
			
			//if(enemy.getName().equals(getTarget())) {
			//	force = -force;
			//}
			
			gravpoints.addElement(new GravPoint( l.getX() , l.getY(), force));
		}
				
	}
	
	public void onScannedRobot( ScannedRobotEvent e) {
		
		// calc bearing
		double bearing = normalizeBearing(getHeading() + e.getBearing());
		
		if( scannedRobots.containsKey(e.getName())) {
			scannedRobots.get(e.getName()).addScanEvent(e, getX(), getY(),bearing);
		} else {
			scannedRobots.put(e.getName(), new Enemy(e.getName(), e, getX(), getY(),bearing));
		}

		selectTheBestTarget();
		
		if(getTarget() != null) {
			TargetingData data = scannedRobots.get(getTarget()).getTargetData();
		
			Vector<GravPoint> gravpoints = new Vector<>();
			
			
			//antiGravMove(gravpoints);
			
			//if( moveAwayFromWall() ) {
			//	// moving
			//}
			//else 
			if( scannedRobots.values().stream().filter( p -> p.isAlive() ).collect(Collectors.toList()).size() > 2) {
				doMove();
			} else {
				moveTowardsTarget();
			}
			
			
			setTurnGunRight(  normalizeBearing( data.getBearing() - getGunHeading() ));
			
			fireMyBigGun(data.getFirepower());  				
		}
		
	}
	
	public void onHitWall(HitWallEvent e) {
		reverseDirection();
	}
	

	// normalizes a bearing to between +180 and -180
	double normalizeBearing(double angle) {
		while (angle >  180) angle -= 360;
		while (angle < -180) angle += 360;
		return angle;
	}



	private boolean moveAwayFromWall() {

		boolean noWallToAvoid = true;
		
		Double x = getX();
		Double y = getY();

		Double maxY = getBattleFieldHeight();
		Double maxX = getBattleFieldWidth();

		Double heading = getHeading();
		Double vol = getVelocity();

		if( x < maxX * 0.1 && y < maxY * 0.1) { 
			if(getHeading() > 180 & getHeading() < 270) {
				reverse();
			}
		}
		else if ( x < maxX * 0.1 && y > maxY * 0.9  ) {
			if(getHeading() < 180 & getHeading() > 90) {
				reverse();
			}
		}
		else if ( x > maxX * 0.9 && y > maxY * 0.9 ) {
			if(getHeading() >0 & getHeading() < 90) {
				reverse();
			}
		}
		else if ( x > maxX * 0.9 && y < maxY * 0.1  ) {
			if( getHeading() > 270){
				reverse();
			}
		}
		else if ( x > maxX * 0.9  ) {
			if(getHeading()  < 20 && getHeading() > 300) {
				reverse();
			}
		}
		else if ( x < maxX * 0.1 ) {
			if( getHeading() > 130 && getHeading()  < 200) {
				reverse();
			}
		}	
		else if ( y < maxY * 0.1 ) {
			if(getHeading() > 210 && getHeading()  < 300){
				reverse();
			}
		}	
		else if ( y > maxY * 0.9 ) {
			if(getHeading() > 60 && getHeading()  < 120) {
				reverse();
			}
		}
		else {
			noWallToAvoid=false;
		}
		

		return noWallToAvoid;
	}

	private void reverse() {
		setTurnRight(-getHeading()); 
		setAhead(9999);	
		waitFor(new TurnCompleteCondition(this));
	}



	public void fireMyBigGun(double s) {
		if(getGunHeat() == 0 && Math.abs(getGunTurnRemaining()) < 5) {
			setFire(getEnergy() > s ? s : 0.1 );
		}
	}
	
	
	void antiGravMove(Vector<GravPoint> gravpoints) {
	    double xforce = 0;
	    double yforce = 0;
	    double force;
	    double ang;
	    GravPoint p;
	    
	    for(int i = 0;i<gravpoints.size();i++) {
	        p = (GravPoint)gravpoints.elementAt(i);
	        //Calculate the total force from this point on us
	        force = p.power/Math.pow(getRange(getX(),getY(),p.x,p.y),1.5);
	        //Find the bearing from the point to us
	        ang = 
	    normalizeBearing(Math.PI/2 - Math.atan2(getY() - p.y, getX() - p.x)); 
	        //Add the components of this force to the total force in their 
	        //respective directions
	        xforce += Math.sin(ang) * force;
	        yforce += Math.cos(ang) * force;
	    }
	    
	    /**The following four lines add wall avoidance.  They will only 
	    affect us if the bot is close to the walls due to the
	    force from the walls decreasing at a power 3.**/
	    xforce += 5000/Math.pow(getRange(getX(), 
	      getY(), getBattleFieldWidth(), getY()), 3);
	    xforce -= 5000/Math.pow(getRange(getX(), 
	      getY(), 0, getY()), 3);
	    yforce += 5000/Math.pow(getRange(getX(), 
	      getY(), getX(), getBattleFieldHeight()), 3);
	    yforce -= 5000/Math.pow(getRange(getX(), 
	      getY(), getX(), 0), 3);
	    
	    //Move in the direction of our resolved force.
	    goTo(getX()-xforce,getY()-yforce);
	}
	
	void goTo(double x, double y) {
	    double dist = 20; 
	    double angle = Math.toDegrees(absoluteBearing(getX(),getY(),x,y));
	    double r = turnTo(angle);
	    setAhead(dist * r);
	}
	
	

	/**Turns the shortest angle possible to come to a heading, then returns 
	the direction the bot needs to move in.**/
	int turnTo(double angle) {
	    double ang;
	    int dir;
	    ang = normalizeBearing(getHeading() - angle);
	    if (ang > 90) {
	        ang -= 180;
	        dir = -1;
	    }
	    else if (ang < -90) {
	        ang += 180;
	        dir = -1;
	    }
	    else {
	        dir = 1;
	    }
	    setTurnLeft(ang);
	    return dir;
	}

	///**/Returns the distance between two points**/
	double getRange(double x1,double y1, double x2,double y2) {
	    double x = x2-x1;
	    double y = y2-y1;
	    double range = Math.sqrt(x*x + y*y);
	    return range;	
	}
	
	class GravPoint {
	    public double x,y,power;
	    public GravPoint(double pX,double pY,double pPower) {
	        x = pX;
	        y = pY;
	        power = pPower;
	    }
	}
	
	double absoluteBearing(double x1, double y1, double x2, double y2) {
		double xo = x2-x1;
		double yo = y2-y1;
		double hyp = Point2D.distance(x1, y1, x2, y2);
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
