package jon;

import java.awt.Color;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import robocode.AdvancedRobot;
import robocode.HitByBulletEvent;
import robocode.HitRobotEvent;
import robocode.HitWallEvent;
import robocode.Robot;
import robocode.RobotDeathEvent;
import robocode.ScannedRobotEvent;
import robocode.TurnCompleteCondition;
import robocode.WinEvent;

public class Tank1 extends AdvancedRobot {
	
	
	double BFW = 100;
	double BFH = 100;

	Double previousEnergy = 100d;
	int movementDirection = 1;
	String target = "-";

	ConcurrentHashMap<String, EnemyTank> scannedRobotsEnergy = new ConcurrentHashMap<>();

	
	
	
	public void run() {

		 BFW = getBattleFieldWidth();
		 BFH = getBattleFieldHeight();

		
		setAdjustRadarForRobotTurn(true);
		setBodyColor(Color.yellow);
		setGunColor(Color.black);
		setRadarColor(Color.blue);
		setScanColor(Color.yellow);
		//setAdjustGunForRobotTurn(true);
		//setAdjustRadarForGunTurn(true);

		//turnRadarLeft(360);
		
		

		while(true) {
			//turnRight(getGunHeading());
			setAhead(9999999);
			//if(target.equals("-")) {

			//}
			//else {
			//		movementDirection = -movementDirection;				
			//		turnRadarLeft(20*movementDirection);
			//	}
			setTurnRadarRightRadians(Double.POSITIVE_INFINITY);

			setTurnLeft(  wallSmoothing(getX(), getY(), getHeading(),1, 1)); 
			waitFor(new TurnCompleteCondition(this));

		}


	}

	public void onRobotDeath(RobotDeathEvent e) {
		if ( e.getName().equals(target) ) {
			target = "-";
		}
	}

	public void onWin(WinEvent e) {
		// Victory dance
		turnRight(36000);
	}



	public void onScannedRobot( ScannedRobotEvent e) {
		// Stay at right angles to the opponent

		setTurnRight( e.getBearing() + 90 - 30 * movementDirection);

		target = e.getName();



		//store energy for robot
		scannedRobotsEnergy.putIfAbsent(e.getName(), new EnemyTank(e, getHeading(),getX(), getY()) );
		EnemyTank enemy = scannedRobotsEnergy.get(e.getName());

		//double changeInEnergy = scannedRobotsEnergy.get(e.getName()) -e.getEnergy();
		//scannedRobotsEnergy.put(e.getName(), e.getEnergy());



		//if (changeInEnergy>0 && changeInEnergy<=3) {
		//	// Dodge! 
		movementDirection = -movementDirection;

		//setTurnRadarLeft(20*movementDirection);

		//	setAhead((e.getDistance()/ 4 + 25 ) * movementDirection );
		//}
		// When a bot is spotted,
		// sweep the gun and radar
		//gunDirection = -gunDirection;
		//setTurnGunRight(99999*gunDirection);

		double firePower = Math.min(500 / e.getDistance(), 3);
		// calculate speed of bullet
		double bulletSpeed = 20 - firePower * 3;
		// distance = rate * time, solved for time
		long bulletTime = (long)(e.getDistance() / bulletSpeed);



		//setTurnGunRight( normalizeBearing(getHeading() + enemy.futureAbsBearing(bulletTime) - getGunHeading()));

		// Fire directly at target

		setTurnGunRight(normalizeBearing( getHeading() - getGunHeading() + e.getBearing()) );

		if( getGunHeat() == 0 && Math.abs(getGunTurnRemaining() ) < 8 ) {
			fireMyBigGun(firePower );
		}

		//		movementDirection = -movementDirection;
		//		if (enemy.changeInEnergy(e) >0 && enemy.changeInEnergy(e)<=3) {
		//			// it's fired at me
		//			// move
		//				turnLeft(3);
		//				ahead(50 * movementDirection);
		//		    }

		enemy.update(e, getHeading(), getX(), getY());
		// Track the energy level

	}

	// normalizes a bearing to between +180 and -180
	double normalizeBearing(double angle) {
		while (angle >  180) angle -= 360;
		while (angle < -180) angle += 360;
		return angle;
	}

	public void onHitWall(HitWallEvent e){
		double bearing = e.getBearing(); //get the bearing of the wall
		setTurnRight(-bearing + 15); //This isn't accurate but release your robot.
		setAhead(9999); //The robot goes away from the enemy.

	}


	public void onHitByBullet(HitByBulletEvent e){
		double bearing = e.getBearing(); //Get the direction which is arrived the bullet.
		if(getEnergy() < 50){ // if the energy is low, the robot go away from the enemy
			setTurnRight( 15); //This isn't accurate but release your robot.
			setAhead(9999); //The robot goes away from the enemy.
		}

	}

	public void onHitRobot(HitRobotEvent e) {
		// If we're moving the other robot, reverse!
		if (e.isMyFault()) {
			setTurnRight(-e.getBearing() + 15); 
			setAhead(9999);
		}
	}


	private boolean noWallToAvoid() {

		boolean noWallToAvoid = false;
		
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
			if(getHeading()  < 30 && getHeading() > 330) {
				reverse();
			}
		}
		else if ( x < maxX * 0.1 ) {
			if( getHeading() > 150 && getHeading()  < 210) {
				reverse();
			}
		}	
		else if ( y < maxY * 0.1 ) {
			if(getHeading() > 230 && getHeading()  < 300){
				reverse();
			}
		}	
		else if ( y > maxY * 0.9 ) {
			if(getHeading() > 60 && getHeading()  < 120) {
				reverse();
			}
		}
		else {
			noWallToAvoid=true;
		}
		


		return noWallToAvoid;

	}

	private void reverse() {
		setTurnRight(-getHeading()); 
		setAhead(9999);	
		waitFor(new TurnCompleteCondition(this));
	}



	public void fireMyBigGun(double s) {


		fire(getEnergy() > s ? s : 0.1 );



	}
	
	
	
	// _bfWidth and _bfHeight set to battle field width and height
	private static double WALL_STICK = 140;
	private java.awt.geom.Rectangle2D.Double _fieldRect = new java.awt.geom.Rectangle2D.Double(18, 18, BFW-36, BFH-36);
	 
	// ...
	/**
	 * x/y = current coordinates
	 * startAngle = absolute angle that tank starts off moving - this is the angle
	 *   they will be moving at if there is no wall smoothing taking place.
	 * orientation = 1 if orbiting enemy clockwise, -1 if orbiting counter-clockwise
	 * smoothTowardEnemy = 1 if smooth towards enemy, -1 if smooth away
	 * NOTE: this method is designed based on an orbital movement system; these
	 *   last 2 arguments could be simplified in any other movement system.
	 */
	public double wallSmoothing(double x, double y, double startAngle,
	    int orientation, int smoothTowardEnemy) {
	 
	    double angle = startAngle;
	 
	    // in Java, (-3 MOD 4) is not 1, so make sure we have some excess
	    // positivity here
	    angle += (4*Math.PI);
	 
	    double testX = x + (Math.sin(angle)*WALL_STICK);
	    double testY = y + (Math.cos(angle)*WALL_STICK);
	    double wallDistanceX = Math.min(x - 18, BFW - x - 18);
	    double wallDistanceY = Math.min(y - 18, BFH - y - 18);
	    double testDistanceX = Math.min(testX - 18, BFW - testX - 18);
	    double testDistanceY = Math.min(testY - 18, BFH - testY - 18);
	 
	    double adjacent = 0;
	    int g = 0; // because I'm paranoid about potential infinite loops
	 
	    while (!_fieldRect.contains(testX, testY) && g++ < 25) {
	        if (testDistanceY < 0 && testDistanceY < testDistanceX) {
	            // wall smooth North or South wall
	            angle = ((int)((angle + (Math.PI/2)) / Math.PI)) * Math.PI;
	            adjacent = Math.abs(wallDistanceY);
	        } else if (testDistanceX < 0 && testDistanceX <= testDistanceY) {
	            // wall smooth East or West wall
	            angle = (((int)(angle / Math.PI)) * Math.PI) + (Math.PI/2);
	            adjacent = Math.abs(wallDistanceX);
	        }
	 
	        // use your own equivalent of (1 / POSITIVE_INFINITY) instead of 0.005
	        // if you want to stay closer to the wall ;)
	        angle += smoothTowardEnemy*orientation*
	            (Math.abs(Math.acos(adjacent/WALL_STICK)) + 0.005);
	 
	        testX = x + (Math.sin(angle)*WALL_STICK);
	        testY = y + (Math.cos(angle)*WALL_STICK);
	        testDistanceX = Math.min(testX - 18, BFW - testX - 18);
	        testDistanceY = Math.min(testY - 18, BFH - testY - 18);
	 
	        if (smoothTowardEnemy == -1) {
	            // this method ended with tank smoothing away from enemy... you may
	            // need to note that globally, or maybe you don't care.
	        }
	    }
	 
	    return angle; // you may want to normalize this
	}

}
