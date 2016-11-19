package bitSlicer.util;

import java.awt.geom.Line2D;
import java.util.Vector;

/**
 * Segment2D represents a line in 2D space.
 */
public class Segment2D extends AABBrect
{
	public final static int TYPE_MODEL_SLICE = 0;
	public final static int TYPE_PERIMETER = 1;
	public final static int TYPE_MOVE = 2;
	public final static int TYPE_FILL = 3;
	public final static int TYPE_ERROR = 0xFFFF;
	
	public Vector2 start;
	public Vector2 end;
	private Vector2 normal;
	private Segment2D next, prev;
	
	public double lineWidth;
	public double feedRate;
	private int type;
	
	public Segment2D(int type, Vector2 start, Vector2 end)
	{
		// Make the AABB 1mm larger then the actual segment, to account for inaccuracies and moving
		// around the segment ends a bit.
		super(start, end, 1.0);
		
		this.type = type;
		this.lineWidth = -1;
		update(start, end);
	}
	
	public Segment2D(int type, Segment2D prev, Segment2D next)
	{
		super(prev.end, next.start, 1.0);
		this.type = type;
		this.start = prev.end;
		this.end = next.start;
		
		if (prev.next != null)
			prev.next.prev = null;
		prev.next = this;
		if (next.prev != null)
			next.prev.next = null;
		next.prev = this;
		
		this.prev = prev;
		this.next = next;
		
		update(this.start, this.end);
	}
	
	/**
	 * For large updates we need to fix the normal, and the AABB. Only call this when the segment is
	 * not in a Tree2D
	 */
	public void update(Vector2 start, Vector2 end)
	{
		this.start = start;
		this.end = end;
		this.normal = end.sub(start).crossZ().normal();
		updateAABB(start, end, 1.0);
	}
	
	public void flip()
	{
		this.update(end, start);
	}
	
	public void flip2(){
		Vector2 temp = start;
		start = end;
		end = temp;
	}
	
	public String toString()
	{
		return "Segment:" + start + " " + end;
	}
	
	public Vector2 getIntersectionPoint(Segment2D other)
	{
		double x12 = start.x - end.x;
		double x34 = other.start.x - other.end.x;
		double y12 = start.y - end.y;
		double y34 = other.start.y - other.end.y;
		
		// Calculate the intersection of the 2 segments.
		double c = x12 * y34 - y12 * x34;
		if (Math.abs(c) < 0.0001)
		{
			return null;
		} else
		{
			double a = start.x * end.y - start.y * end.x;
			double b = other.start.x * other.end.y - other.start.y * other.end.x;
			
			return new Vector2((a * x34 - b * x12) / c, (a * y34 - b * y12) / c);
		}
	}
	
	
	/**
	 * Collision detection taken from http://stackoverflow.com/a/1968345
	 */
	public Vector2 getCollisionPoint(Segment2D other){
		double p0_x = this.start.x;
		double p0_y = this.start.y;
		double p1_x = this.end.x;
		double p1_y = this.end.y;
		double p2_x = other.start.x;
		double p2_y = other.start.y;
		double p3_x = other.end.x;
		double p3_y = other.end.y;
		
		double s1_x, s1_y, s2_x, s2_y;
	    s1_x = p1_x - p0_x;
	    s1_y = p1_y - p0_y;
	    s2_x = p3_x - p2_x;
	    s2_y = p3_y - p2_y;

	    double s, t;
	    s = (-s1_y * (p0_x - p2_x) + s1_x * (p0_y - p2_y)) / (-s2_x * s1_y + s1_x * s2_y);
	    t = ( s2_x * (p0_y - p2_y) - s2_y * (p0_x - p2_x)) / (-s2_x * s1_y + s1_x * s2_y);

	    //new Line2D.Float((float) this.start.x, (float) this.start.y, (float) this.end.x, (float) this.end.y).intersectsLine(new Line2D.Float((float) other.start.x, (float) other.start.y, (float) other.end.x, (float) other.end.y))
	    if (s >= 0 && s <= 1 && t >= 0 && t <= 1)
	    {
	        // Collision detected
	        return new Vector2(p0_x + (t * s1_x), p0_y + (t * s1_y));
	    }
	    
	    return null; // No collision
	}
	
	/**
	 * Return all the point of collision between this segment and a list of segments
	 */
	public Vector<Vector2> getCollisionPoints(Vector<Segment2D> segmentList){
		Vector<Vector2> collisiontPoints = new Vector<Vector2>();
		for (Segment2D s : segmentList) {
			Vector2 collisionPoint = this.getCollisionPoint(s);
			if (collisionPoint != null)
				collisiontPoints.add(collisionPoint);
		}
		
		return collisiontPoints;
	}
	
	/**
	 * Check if this segment contain a point. Taken from http://stackoverflow.com/a/11908158
	 */
	public boolean contains(Vector2 point)
	{
		double dxc = point.x - start.x;
		double dyc = point.y - start.y;

		double dxl = end.x - start.x;
		double dyl = end.y - start.y;

		double cross = dxc * dyl - dyc * dxl;
		if (cross != 0)
			  return false;
		
		if (Math.abs(dxl) >= Math.abs(dyl))
			return (dxl > 0) ? start.x <= point.x && point.x <= end.x :	end.x <= point.x && point.x <= start.x;
		else
			return (dyl > 0) ? start.y <= point.y && point.y <= end.y :	end.y <= point.y && point.y <= start.y;
	}
	
	public Vector2 getNormal()
	{
		return normal;
	}
	
	public int getType()
	{
		return type;
	}
	
	public void setType(int type)
	{
		this.type = type;
	}
	
	public void setNext(Segment2D newNext)
	{
		if (newNext == null)
		{
			if (next == null)
				throw new UnsupportedOperationException();
			next.prev = null;
			next = null;
		} else
		{
			if (next != null)
				throw new UnsupportedOperationException();
			if (newNext.prev != null)
				throw new UnsupportedOperationException();
			next = newNext;
			next.prev = this;
		}
	}
	
	public Segment2D getNext()
	{
		return next;
	}
	
	public Segment2D getPrev()
	{
		return prev;
	}
	
	/**
	 * Split a segment into two segments using a point as splitting tool.
	 */
	public Vector<Segment2D> split(Vector2 point) {
		
		Vector<Segment2D> segments = new Vector<Segment2D>();
		if (point.asGoodAsEqual(this.start)){ //The split point will always be kept as segments.get(0).end
			Vector2 temp = this.start;
			this.start = this.end;
			this.end = temp;
			segments.add(this); //Does this change can bring issues? prev is now in the end side of the segment
		}
		else if(point.asGoodAsEqual(this.end)){
			segments.add(this);
		}
		else{
			Segment2D s2 = new Segment2D(this.type, point, this.end);
			s2.next = this.next;
			
			this.end = point;

			segments.add(this); // in that case we keep the original segment which is shortened to be the first of the 2 new segments
			segments.add(s2);
		}
		return segments; //The split point will always be kept as segments.get(0).end
	}
	
	/**
	 * Split a segment into segments using a list of point as splitting tool.
	 */
	/* old version:*/
	public Vector<Segment2D> split1(Vector<Vector2> points) {
		
		
		Vector<Segment2D> segments = new Vector<Segment2D>();
		
		points.add(this.start);
		points.add(this.end);
		
		// Convert Vector<Vector2> to Vector2[] since we can't swipe item in Vector<Vector2>
		Vector2[] pointsArray = new Vector2[points.size()];
		for (int i=0; i < points.size(); i++)
			pointsArray[i] = points.get(i);
		
		// Sort the points by x and y
		for (int i = 0; i < pointsArray.length-1; i++) {
			int min = i;
			for (int j = i+1; j < pointsArray.length; j++) {
				if((pointsArray[j].x < pointsArray[min].x))
					min = j;
				else if ((pointsArray[j].x == pointsArray[min].x)) {
					if((pointsArray[j].y < pointsArray[min].y))
						min = j;
				}
			}
			if (min != i) {
				Vector2 temp = pointsArray[min];
				pointsArray[min] = pointsArray[i];
				pointsArray[i] = temp;
			}
		}
		
		Vector<Vector2> sortedPoints = new Vector<Vector2>();
		for (int i = 0; i < pointsArray.length; i++) {
			sortedPoints.add(pointsArray[i]);
		}
		
		// Create segments from ordered points
		for (int i = 0; i < sortedPoints.size()-1; i += 1) {
			segments.add(new Segment2D(this.type, sortedPoints.get(i), sortedPoints.get(i+1)));
		}
		
		return segments;
	}
	
	public Vector<Segment2D> split2(Vector<Vector2> points){
		Vector<Segment2D> segments = new Vector<Segment2D>();
		
		if(points.isEmpty()){
			segments.add(this);
			return segments;
		} else if (points.size() == 1){
			segments.add(new Segment2D(this.type, this.start, points.get(0)));
			segments.add(new Segment2D(this.type, points.get(0), this.end));
			return segments;
		}
		
		Vector<Double> pointDistances = new Vector<Double>();
		
		//Build a vector with the directors vectors of each point (We take the start point as a reference point)
		//Check in the same time if any point is the same as start or end
		int loopEnd = points.size();
		for(int i = 0; i < loopEnd; i++){
			if (points.get(i).asGoodAsEqual(this.start) || points.get(i).asGoodAsEqual(this.end)){
				points.remove(i);
				loopEnd--;
			}
			else{
				pointDistances.add((new Vector2((points.get(i).x - this.start.x), (points.get(i).y - this.start.y)).vSize2()));
			}
		}
		
		//Sort the points by sorting the director vectors using their size
//		for(int i = 0; i < points.size() - 1; i++){
//			for(int j = i + 1; j < points.size() - 1; j++){
//				System.out.println(j);
//				System.out.println(points.size());
//				if (pointDistances.get(j) > pointDistances.get(i)){
//					pointDistances.insertElementAt(pointDistances.get(j), i);
//					pointDistances.remove(j+1);
//					points.insertElementAt(points.get(j), i);
//					points.remove(j+1);
//				}
//			}
//		}
		
		Vector2 sortedPoints[] = new Vector2[points.size()];
		
		for(int i = 0; i < points.size(); i++) {
			int pos = 0;
			for(int j = 0; j < points.size(); j++) {
				if ((pointDistances.get(i) > pointDistances.get(j)) || (i < j && pointDistances.get(i) == pointDistances.get(j))) // Check if points is farther or doublon
					pos++;
			}
			sortedPoints[pos] = points.get(i);
		}
		
		points = new Vector<Vector2>();
		
		for(int i = 0; i<sortedPoints.length; i++) {
			points.add(sortedPoints[i]);
		}
		
		//add the start and end points at the beginning and the end of the vector
		points.insertElementAt(this.start, 0);
		points.add(this.end);		
		
		// Create segments from ordered points
		for (int i = 0; i < points.size()-1; i++) {
			segments.add(new Segment2D(this.type, points.get(i), points.get(i+1)));
		}
		
		return segments;
	}
	
	public Vector2 getMidPoint() {
		return this.getPointAtRatio(0.5);
	}
	
	/**
	 * Return the position of the point at a given ratio
	 */
	public Vector2 getPointAtRatio(double ratio) {
		return new Vector2(this.start.x+(this.end.x - this.start.x)*ratio, this.start.y+(this.end.y - this.start.y)*ratio);
	}
	
	public void resetLinks() {
		this.next = null;
		this.prev = null;
	}
	
	/**
	 * Remove the part of the segment outside of the shape. It will mostly return a vector with only 1 segment.
	 */
	public Vector<Segment2D> trim(Shape2D shape) {
		Vector<Segment2D> trimmedSegment = new Vector<Segment2D>();
		
		Vector<Vector2> collisionPoints = this.getCollisionPoints(shape.getSegmentList());
		
		if(!collisionPoints.isEmpty()){
			Vector<Segment2D> splittedSegment = this.split2(collisionPoints);
			for(Segment2D s : splittedSegment) {
				if(shape.contains(s)) {
					trimmedSegment.add(s);
				}
			}
		}
		else if(shape.contains(this.getMidPoint()))
			trimmedSegment.add(this);
		
		return trimmedSegment;
	}
	
	public Segment2D getInLowerCooSystem(Vector2 myOrientation, Vector2 myOrigin){
		Vector2 computedStart = start.getInLowerCooSystem(myOrientation, myOrigin);
		Vector2 computedEnd = end.getInLowerCooSystem(myOrientation, myOrigin);
		return new Segment2D(1, computedStart, computedEnd);
	}
}

