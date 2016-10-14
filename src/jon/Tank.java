package jon;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import jon.Utils;
import java.awt.geom.Point2D.Double;
import robocode.ScannedRobotEvent;

public class Tank {

	private String name;
	private List<TankLog> log;
	
	private boolean isDead = false;
	
	Tank(String name, ScannedRobotEvent e, Double orgin, double bearing) {
		this.name = name;
		this.isDead = false;
		
		List<TankLog> newLog = new LinkedList<>();
		newLog.add( new TankLog(e, orgin, bearing) );
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
		return log.get(log.size() - 1).getBearing();
	}
	
	public void addScanEvent(ScannedRobotEvent e, Double orgin, double bearing){
		this.log.add(new TankLog(e, orgin, bearing));	
	}

	public List<TankLog> getLog() {
		return log;
	}

	public void setLog(List<TankLog> log) {
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
		TankLog data = log.get(log.size() - 1);
		
		return new Utils().getTargetData(data);
	}
	
	public TargetingData getTargetDataCommon(long time) {
		List<TankLog> data = log.stream().filter(p -> p.getTime() >= time).collect(Collectors.toList());
		
		return new Utils().getTargetData(data);
	}
	
}
