package sample;

import java.util.Random;

import robocode.AdvancedRobot;
import robocode.HitRobotEvent;
import robocode.ScannedRobotEvent;
public class Arobot extends AdvancedRobot {
    public void run() {
        Random r = new Random();
        while (true) {
            setTurnRight(r.nextInt(5000) + 5000);
            setMaxVelocity(4);
            ahead(10000);
        }
    }

    /**
     * onScannedRobot: Fire hard!
     */
    public void onScannedRobot(ScannedRobotEvent e) {
        fire(3);
    }

    /**
     * onHitRobot: If it's our fault, we'll stop turning and moving, so we need to turn again to keep spinning.
     */
    public void onHitRobot(HitRobotEvent e) {
        if (e.getBearing() > -10 && e.getBearing() < 10) {
            fire(3);
        }
        if (e.isMyFault()) {
            turnRight(10);
        }
    }

}
