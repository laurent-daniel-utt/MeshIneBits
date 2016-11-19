package bitSlicer.util;

import java.util.Vector;

/**
 * Vector2 represent a point in a 2D space
 */
public class Vector2
{
	public final double x, y;
	
	public Vector2(double x, double y)
	{
		this.x = x;
		this.y = y;
		if (Double.isNaN(x) || Double.isNaN(y))
			throw new RuntimeException("Vector has NaN component...");
	}
	
	public Vector2 add(Vector2 v)
	{
		return new Vector2(x + v.x, y + v.y);
	}
	
	public Vector2 sub(Vector2 v)
	{
		return new Vector2(x - v.x, y - v.y);
	}
	
	/**
	 * Return the vector divided by a double
	 */
	public Vector2 div(double f)
	{
		return new Vector2(x / f, y / f);
	}
	
	/**
	 * Return the vector multiplied by a double
	 */
	public Vector2 mul(double f)
	{
		return new Vector2(x * f, y * f);
	}
	
	/**
	 * Return the vector mirrored on the Z axis
	 */
	public Vector2 crossZ()
	{
		return new Vector2(y, -x);
	}
	
	/**
	 * Return the scalar of two vector
	 */
	public double dot(Vector2 v)
	{
		return x * v.x + y * v.y;
	}
	
	public boolean asGoodAsEqual(Vector2 v)
	{
		return (Math.abs(x - v.x) + Math.abs(y - v.y)) < 0.00001;
	}
	
	public String toString()
	{
		return x + "," + y;
	}
	
	/**
	 * Returns a normalized vector with a length of 1, having the same direction as the origonal vector.
	 */
	public Vector2 normal()
	{
		double d = vSize();
		if (d < 0.0000001)
			return new Vector2(0, 0);
		return new Vector2(x / d, y / d);
	}
	
	/**
	 * Returns the length of the vector.
	 */
	public double vSize()
	{
		return Math.sqrt(x * x + y * y);
	}
	
	/**
	 * Returns the squared length of the vector (faster then vSize())
	 */
	public double vSize2()
	{
		return x * x + y * y;
	}
	
	public boolean isOnSegment(Segment2D s) {
		double x1 = s.start.x;
		double y1 = s.start.y;
		double x2 = s.end.x;
		double y2 = s.end.y;
		
		double AB = Math.sqrt((x2-x1)*(x2-x1)+(y2-y1)*(y2-y1));
		double AP = Math.sqrt((x-x1)*(x-x1)+(y-y1)*(y-y1));
		double PB = Math.sqrt((x2-x)*(x2-x)+(y2-y)*(y2-y));
		
		if(AB == AP + PB)
		    return true;
		else 
			return false;
	}
	
	/* 
	 * Taken from http://stackoverflow.com/a/17590923
	 */
	public boolean isOnPath(Vector<Segment2D> segmentList) {
		for (Segment2D s : segmentList) {
			if(this.isOnSegment(s))
			    return true;
		}
		return false;
	}
	

	public boolean isOnPath(Polygon poly) {
		for (Segment2D s : poly) {
			if (this.isOnSegment(s))
					return true;
		}
		
		return false;
	}
	
	// Give the orientation vector equivalent to an angle in degrees
	public static Vector2 getEquivalentVector(double angleDegrees){
		double angleRadian = Math.PI*angleDegrees/180;
		return new Vector2(Math.cos(angleRadian), Math.sin(angleRadian));
	}
	
	public Vector2 getInLowerCooSystem(Vector2 myOrientation, Vector2 myOrigin){
		Vector2 orientation = myOrientation.normal();
		double angleRotation = Math.acos(orientation.x);
		double computedX = x*Math.cos(angleRotation)-y*Math.sin(angleRotation)+myOrigin.x;
		double computedY = x*Math.sin(angleRotation)+y*Math.cos(angleRotation)+myOrigin.y;
		return new Vector2(computedX, computedY);
	}

}
