package jon;

import java.util.LinkedList;
import java.util.List;

import robocode.ScannedRobotEvent;

public class Enemy {

	private String name;
	private List<EnemyLog> log;
	
	private boolean isDead = false;
	
	Enemy(String name, ScannedRobotEvent e, double myX, double myY, double bearing) {
		this.name = name;
		this.isDead = false;
		
		List<EnemyLog> newLog = new LinkedList<>();
		newLog.add( new EnemyLog(e, myX, myY, bearing) );
		this.log = newLog;
	}
	
	public boolean isUpdated(long time){
		return log.get(log.size() - 1).getTime() >= time - 4 ? true : false;
	}
	
	public String getName() {
		return this.name;
	}
	
	public double getDistance(){
		// TODO calculate if they are coming closer or away
		return log.get(log.size() - 1).getDistance();
	}
	
	public double getBearing(){
		// TODO calculate if they are coming closer or away
		return log.get(log.size() - 1).getOrigBearing();
	}
	
	public void addScanEvent(ScannedRobotEvent e, double myX, double myY, double bearing){
		this.log.add(new EnemyLog(e, myX, myY, bearing));	
	}

	public List<EnemyLog> getLog() {
		return log;
	}

	public void setLog(List<EnemyLog> log) {
		this.log = log;
	}

	public boolean isDead() {
		return isDead;
	}
	
	public boolean isAlive() {
		return isDead() ? false : true;
	}

	public void setDead(boolean isDead) {
		this.isDead = isDead;
	}
	
	public boolean hasFired() {
		if(log.size() >= 2) {
			double changeInEnergy =  log.get(log.size() - 1).getEnergy() - log.get(log.size() - 2).getEnergy();
			if( changeInEnergy >0 && changeInEnergy <=3 ) {
				return true;
			}
		}
		return false;
	}
	
	public TargetingData getTargetData() {
		EnemyLog data = log.get(log.size() - 1);
		
		return data.calculateShot(log);
	}
	
	
}
