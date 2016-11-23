package bitSlicer;

import java.awt.BasicStroke;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.util.Hashtable;
import java.util.Vector;

import bitSlicer.PatternTemplates.PatternTemplate;
import bitSlicer.Slicer.Slice;
import bitSlicer.Slicer.Config.CraftConfig;
import bitSlicer.util.AreaTool;
import bitSlicer.util.Shape2D;
import bitSlicer.util.Vector2;

public class Pattern {
	
	public static int bitNr; // for debug
	
	//private Vector<Bit2D> bits;
	private Vector2 rotation;
	private double skirtRadius;
	private Hashtable<Vector2, Bit2D> mapBits;
	private AffineTransform transfoMatrix = new AffineTransform();
	private AffineTransform inverseTransfoMatrix;
	
	public Pattern(Hashtable<Vector2, Bit2D> mapBits, Vector2 rotation, double skirtRadius){

		//this.bits = bits;
		this.mapBits = mapBits;
		this.rotation = rotation;
		this.skirtRadius = skirtRadius;
		
		transfoMatrix.rotate(rotation.x, rotation.y); // Each pattern can have a rotation, usually linked to the layer number
		transfoMatrix.rotate(CraftConfig.rotation); //Rotation of the whole patternTemplate
		transfoMatrix.translate(CraftConfig.xOffset, CraftConfig.yOffset); //Translation of the whole patternTemplate
		
		try {
			inverseTransfoMatrix = ((AffineTransform) transfoMatrix.clone()).createInverse();
		} catch (NoninvertibleTransformException e) {
			e.printStackTrace();
		}
	}
	
	/*
	 * Remove the bits that are outside the part
	 * Compute the cut line for the bits on the boundaries of the part
	 */
	/*
	public void computeBits(Slice slice){
		Vector<Bit2D> bitsToKeep = new Vector<Bit2D>();

		for(Bit2D bit : bits){
			if(bit.isOnPath(slice) || bit.isInsideShape(slice, skirtRadius)){
				bitsToKeep.add(bit);
			}
		}
		bits = bitsToKeep;
	}
	*/
	
//	public void setToLayerCooSystem(){
//		for (Bit2D bit : bits){
//			bit.setBitInPatternCooSystem(); //Bits' boundaries go from a local coo system to the pattern's one
//			bit.setInThatCooSystem(rotation, new Vector2(0,0)); // Each pattern can have a rotation, usually linked to the layer number
//			bit.setInThatCooSystem(Vector2.getEquivalentVector(CraftConfig.rotation), new Vector2(CraftConfig.xOffset, CraftConfig.yOffset)); //the whole pattern template can have a rotation and an offset regarding the part
//		}
//	}
	
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
		Area sliceArea = AreaTool.getAreaFrom(slice);
        sliceArea.transform(inverseTransfoMatrix);
        Shape str = new BasicStroke(0.1f).createStrokedShape(sliceArea);
        Area sliceLine = new Area(str);
        Vector<Vector2> keys = new Vector<Vector2>(mapBits.keySet());
        for(Vector2 key : keys){
        	Area bitArea = new Area();
    		bitArea.add(mapBits.get(key).getArea());
    		bitArea.intersect(sliceArea);
    		if (bitArea.isEmpty())
    			mapBits.remove(key);
    		else if(!bitArea.equals(mapBits.get(key).getArea())){
    			mapBits.get(key).updateBoundaries(bitArea);
    			setCutPath(sliceLine, key);
    		}	
        }
	}
	
	private void setCutPath(Area sliceLine, Vector2 key){
		
		Vector<double[]> pathPoints = AreaTool.getPathPoints(mapBits.get(key));
		Vector<double[]> cutPathPoints = new Vector<double[]>();
		for(double[] point : pathPoints){
			if(sliceLine.contains(new Point2D.Double(point[1], point[2])))
				cutPathPoints.add(point);
		}
		
	}
	
}
