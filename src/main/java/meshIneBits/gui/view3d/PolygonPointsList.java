package meshIneBits.gui.view3d;

import java.util.Vector;

/**
 * 
 * @author Nicolas
 *
 */
public class PolygonPointsList {
	
	private Vector<int[]> points;
	private int curPosition = -1;
	
	/**
	 * 
	 * @param points
	 * @throws Exception 
	 */
	public PolygonPointsList(Vector<int[]> points) throws Exception{
		if(points.size() < 3){
			throw new Exception("Not enough points to build a polygon !");
		}
		this.points = points;
	}
	
	/**
	 * 
	 * @return the next point in the list
	 */
	public int[] getNextPoint(){
		if(curPosition < points.size() - 1)
			curPosition++;
		else
			curPosition = 0;
		return points.get(curPosition);
	}
	
	/**
	 * 
	 * @return Number of points in the list
	 */
	public int getLength(){
		return points.size();
	}
}
