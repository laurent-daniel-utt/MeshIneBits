package bitSlicer;

import java.awt.BasicStroke;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Path2D;
import java.util.Hashtable;
import java.util.Vector;

import bitSlicer.Slicer.Slice;
import bitSlicer.Slicer.Config.CraftConfig;
import bitSlicer.util.AreaTool;
import bitSlicer.util.Logger;
import bitSlicer.util.Segment2D;
import bitSlicer.util.Vector2;

public class Pattern implements Cloneable{
	
	public static int bitNr; // for debug
	
	//private Vector<Bit2D> bits;
	public Vector2 rotation;
	private double skirtRadius;
	private Hashtable<Vector2, Bit2D> mapBits;
	private AffineTransform transfoMatrix = new AffineTransform();
	private AffineTransform inverseTransfoMatrix;
	
	public Pattern(Vector<Bit2D> bits, Vector2 rotation, double skirtRadius){

		this.rotation = rotation;
		this.skirtRadius = skirtRadius;
		
		transfoMatrix.rotate(rotation.x, rotation.y); // Each pattern can have a rotation, usually linked to the layer number
		transfoMatrix.rotate(Vector2.getEquivalentVector(CraftConfig.rotation).x, Vector2.getEquivalentVector(CraftConfig.rotation).y); //Rotation of the whole patternTemplate
		transfoMatrix.translate(CraftConfig.xOffset, CraftConfig.yOffset); //Translation of the whole patternTemplate

		try {
			inverseTransfoMatrix = ((AffineTransform) transfoMatrix.clone()).createInverse();
		} catch (NoninvertibleTransformException e) {
			e.printStackTrace();
		}
		
		setMapBits(bits);
	}
	
	public Pattern(Hashtable<Vector2, Bit2D> mapBits, Vector2 rotation, double skirtRadius, AffineTransform transfoMatrix, AffineTransform inverseTransfoMatrix){
		this.rotation = rotation;
		this.skirtRadius = skirtRadius;
		this.mapBits = mapBits;
		this.transfoMatrix = transfoMatrix;
		this.inverseTransfoMatrix = inverseTransfoMatrix;
	}
	
	@Override
	public Pattern clone(){
		
		Hashtable<Vector2, Bit2D> clonedMapBits = new Hashtable<Vector2, Bit2D>();
		for(Vector2 key : this.getBitsKeys()){
			clonedMapBits.put(key, mapBits.get(key).clone());
		}
		
		return new Pattern(clonedMapBits, rotation, skirtRadius, transfoMatrix, inverseTransfoMatrix);
	}
	
	public void setMapBits(Vector<Bit2D> bits){
		mapBits = new Hashtable<Vector2, Bit2D>();
		for(Bit2D bit : bits){
			addBit(bit);
		}
	}
	
	public void addBit(Bit2D bit){
		//the key of each bit is its origin's coordinates in the general coo system
		Vector2 bitKey = bit.getOrigin().getTransformed(transfoMatrix);
		//We check that there is not already a bit at this place
		for(Vector2 key : getBitsKeys()){
			if(bitKey.asGoodAsEqual(key)){
				Logger.warning("A bit already exists at these coordinates");
				return;
			}
				
		}
		mapBits.put(bitKey, bit);
	}
	
	public Vector<Vector2> getBitsKeys(){
		return new Vector<Vector2>(mapBits.keySet());
	}
	
	public Area getBitArea(Vector2 key){
		Area area = new Area();
		area.add(mapBits.get(key).getArea());
		area.transform(transfoMatrix);
		return area;
	}
	
	public void computeBits(Slice slice){
		Area sliceArea = AreaTool.getAreaFrom2(slice);
        sliceArea.transform(inverseTransfoMatrix);
        Shape str = new BasicStroke(0.1f).createStrokedShape(sliceArea);//0.1f is the smaller stroke possible
        Area cutLine = new Area(str);
        Area cutLineClone;
        Vector<Vector2> keys = new Vector<Vector2>(mapBits.keySet());
        for(Vector2 key : keys){
        	Area bitArea = new Area();
    		bitArea.add(mapBits.get(key).getArea());
    		cutLineClone = (Area) cutLine.clone();
    		cutLineClone.intersect(bitArea);    		
    		bitArea.intersect(sliceArea);
    		if (bitArea.isEmpty())
    			mapBits.remove(key);
    		else if(!cutLineClone.isEmpty()){
    			mapBits.get(key).updateBoundaries(bitArea);
    			setCutPath2(cutLineClone, bitArea, key);
    		}	
        }
	}
	
	private void setCutPath2(Area cutLineStroke, Area bitArea, Vector2 key){
		Vector<Vector<Segment2D>> edges = AreaTool.getSegmentsFrom(bitArea);
		
		Vector<Segment2D> cutLine = new Vector<Segment2D>();
		
		for(Vector<Segment2D> polygon : edges){
			for(Segment2D edge : polygon){
				//System.out.println(edge);
				if(cutLineStroke.contains(edge.getMidPoint().x, edge.getMidPoint().y)){
					cutLine.add(edge);
				}
			}
		}
		
		Vector<Path2D> cutPaths = new Vector<Path2D>();
		
		if (cutLine.isEmpty()) return;
		else if(cutLine.size() == 1){
			Path2D cutPath2D = new Path2D.Double();
			cutPath2D.moveTo(cutLine.get(0).start.x, cutLine.get(0).start.y);
			cutPath2D.lineTo(cutLine.get(0).end.x, cutLine.get(0).end.y);
			cutPaths.addElement(cutPath2D);
			mapBits.get(key).setCutPath(cutPaths);
			return;
		}
		
		Vector<Vector<Segment2D>> cutLines = Segment2D.segregateSegments(cutLine);
		
		for(Vector<Segment2D> pathLine : cutLines){
			Path2D cutPath2D = new Path2D.Double();
			cutPath2D.moveTo(pathLine.get(0).start.x, pathLine.get(0).start.y);
			for(int i = 1; i < pathLine.size(); i++)
				cutPath2D.lineTo(pathLine.get(i).start.x, pathLine.get(i).start.y);
			cutPath2D.lineTo(pathLine.get(pathLine.size() - 1).end.x, pathLine.get(pathLine.size() - 1).end.y);
			cutPaths.add(cutPath2D);
		}
		
		mapBits.get(key).setCutPath(cutPaths);
		
	}
	
	public Vector<Path2D> getCutPaths(Vector2 key){
		Vector<Path2D> cutPaths = mapBits.get(key).getCutPaths();
		if(cutPaths == null)
			return null;
		else{
			Vector<Path2D> paths = new Vector<Path2D>();
			for(Path2D p : cutPaths)
				paths.add(new Path2D.Double(p, transfoMatrix));
			return paths;
		}
	}
	
	public void removeBit(Vector2 key){
		mapBits.remove(key);
	}
	
	public Bit2D getBit(Vector2 key){
		return mapBits.get(key);
	}
	
	public AffineTransform getAffineTransform(){
		return transfoMatrix;
	}
	
	public void moveBit(Vector2 key, Vector2 direction, double offSetValue){
		Bit2D bitToMove = mapBits.get(key);
		removeBit(key);
		Vector2 localDirection = bitToMove.getOrientation();
		AffineTransform rotateMatrix = new AffineTransform();
		rotateMatrix.rotate(direction.x, direction.y);
		localDirection = localDirection.getTransformed(rotateMatrix);
		localDirection = localDirection.normal();
		Vector2 newCoordinates = new Vector2(bitToMove.getOrigin().x + (localDirection.x * offSetValue), bitToMove.getOrigin().y + (localDirection.y * offSetValue));
		addBit(new Bit2D(newCoordinates, bitToMove.getOrientation()));
	}
}
