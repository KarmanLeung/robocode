package jon;

import static org.junit.Assert.*;

import java.awt.geom.Point2D.Double;

import org.junit.Test;

public class UtilsTest {
	

	@Test
	public void testDistanceBetweenPoints() {
	
		Double point1 = new Double(0,0); 
		Double point2 = new Double(3,4);
		
		Utils utils = new Utils();
		assertEquals(5, utils.distanceBetweenPoints(point1, point2), 0.1);
		
	}
	
	@Test
	public void testCalculateXY(){
		Double orgin = new Double(0,0);
		double distance = 5;
		double bearing = 36.8699;
		
		Utils utils = new Utils();
		assertEquals(Math.round(new Double(3,4).getX()), Math.round(utils.calculateXY(orgin, distance, bearing).getX()) );
		assertEquals(Math.round(new Double(3,4).getY()), Math.round(utils.calculateXY(orgin, distance, bearing).getY()) );
	}

	@Test
	public void testCalculateFutureXY1() {
		Double orgin = new Double(1,1);
		double velocity = 1;
		double heading = 90;
		long time = 1;
		
		Utils utils = new Utils();
		assertEquals(new Double(2,1), utils.calculateFutureXY(orgin, velocity, heading, time) );
	}
	
	@Test
	public void testCalculateFutureXY2() {
		Double orgin = new Double(10,10);
		double velocity = 2;
		double heading = 180;
		long time = 2;
		
		Utils utils = new Utils();
		assertEquals(new Double(10,6), utils.calculateFutureXY(orgin, velocity, heading, time) );
	}
	
	@Test
	public void testCalculateBearing(){
		Double orgin  = new Double(10,10);
		Double target = new Double(10,10);
		
		Utils utils = new Utils();
		assertEquals(0, utils.calculateBearing(orgin, target), 0.1 );
	}
	
	@Test
	public void testCalculateBearing2(){
		Double orgin  = new Double(1,1);
		Double target = new Double(2,2);
		
		Utils utils = new Utils();
		assertEquals(45, utils.calculateBearing(orgin, target), 0.1 );
	}
	
	
}
