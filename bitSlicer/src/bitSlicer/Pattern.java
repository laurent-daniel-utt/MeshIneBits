package bitSlicer;

import java.awt.BasicStroke;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.util.Hashtable;
import java.util.Vector;

import bitSlicer.PatternTemplates.PatternTemplate;
import bitSlicer.Slicer.Slice;
import bitSlicer.Slicer.Config.CraftConfig;
import bitSlicer.util.AreaTool;
import bitSlicer.util.Segment2D;
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
			//System.out.println("----------------------------------");
		}
		//System.out.println("----------------------------------");
		
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
	
	private void setCutPath(Area sliceLine, Vector2 key){
		
		Vector<double[]> pathPoints = AreaTool.getPathPoints(mapBits.get(key).getArea());
		
		//triplet[0=type; 1=x; 2=y][0=prev; 1=point; 2=next]
		Vector<double[][]> triplets = new Vector<double[][]>(); //={prevPoint[], point[], nextPoint[]}
			
		//set triplets (only for the points that are on the cut line)
		double[] prev = new double[3];
		
		for (int i = 0; i < pathPoints.size(); i++) {
			double[] currentPoint = pathPoints.get(i);
			Point2D.Double point2D = new Point2D.Double(currentPoint[1], currentPoint[2]);
			
			if(sliceLine.contains(point2D)){
				double[][] newTriplet = new double[3][3];
				if(i == 0){
					newTriplet[0][0] = pathPoints.get(pathPoints.size() - 1)[0];
					newTriplet[1][0] = pathPoints.get(pathPoints.size() - 1)[1];
					newTriplet[2][0] = pathPoints.get(pathPoints.size() - 1)[2];
				}
				else{
					newTriplet[0][0] = prev[0];
					newTriplet[1][0] = prev[1];
					newTriplet[2][0] = prev[2];
				}
				newTriplet[0][1] = pathPoints.get(i)[0];
				newTriplet[1][1] = pathPoints.get(i)[1];
				newTriplet[2][1] = pathPoints.get(i)[2];
				
				if(i == pathPoints.size() - 1){
					newTriplet[0][1] = pathPoints.get(0)[0];
					newTriplet[1][1] = pathPoints.get(0)[1];
					newTriplet[2][1] = pathPoints.get(0)[2];
				}
				else{
					newTriplet[0][2] = pathPoints.get(i + 1)[0];
					newTriplet[1][2] = pathPoints.get(i + 1)[1];
					newTriplet[2][2] = pathPoints.get(i + 1)[2];
				}
				
				triplets.add(newTriplet);
				
				prev = currentPoint;
			}
		}
		
		if (triplets.isEmpty()) return;
			
		
		Vector<double[]> cutPoints = new Vector<double[]>();
		
		//Every points are converted to LINE_TO points excepts for the ones that are on each side of the cutPath, these are converted to MOVE_TO points
		for(double[][] triplet : triplets){
			if(!(sliceLine.contains(new Point2D.Double(triplet[1][0], triplet[2][0]))) ||
					!(sliceLine.contains(new Point2D.Double(triplet[1][2], triplet[2][2])))){
				triplet[0][1] = PathIterator.SEG_MOVETO;
			}
			else
				triplet[0][1] = PathIterator.SEG_LINETO;
			
			double[] cutPoint = {triplet[0][1], triplet[1][1], triplet[2][1]};
			cutPoints.add(cutPoint);
		}
		
		Vector<Vector<double[]>> cutPaths = new Vector<Vector<double[]>>();
		Vector<double[]> currentCutPath = new Vector<double[]>();
		
		boolean mergeFirstAndLast = false;
		//double[] prevCutPoint = cutPoints.get(cutPoints.size() - 1);
		double[] nextCutPoint = new double[3];
		
		//Separate the different cutPaths if there is more than one
		for(int i = 0; i < cutPoints.size(); i++){
			if(i < cutPoints.size() - 1)
				nextCutPoint = cutPoints.get(i + 1);
			else
				nextCutPoint = cutPoints.get(0);
				
			if((cutPoints.get(i)[0] == PathIterator.SEG_MOVETO) &&
					(nextCutPoint[0] == PathIterator.SEG_LINETO) &&
					(!currentCutPath.isEmpty())){
				
				if(cutPaths.isEmpty()){
					mergeFirstAndLast = true;
				}
				cutPaths.add(currentCutPath);
				currentCutPath = new Vector<double[]>();
			}
			currentCutPath.add(cutPoints.get(i));
		}
		
		if((mergeFirstAndLast) && (cutPaths.size() > 1)){
			cutPaths.get(cutPaths.size() - 1).addAll(cutPaths.get(0));
			cutPaths.remove(0);
		}
		
		//Reset the last point of each cutPath to LINE_TO instead of MOVE_TO
		for(Vector<double[]> cutPath : cutPaths){
			cutPath.get(cutPath.size() - 1)[0] = PathIterator.SEG_LINETO;
		}
		//Every cutPath are now in the format MOVE_TO, LINE_TO, LINE_TO, ..., LINE_TO.
		
		Vector<Path2D> cutPath2DCollection = new Vector<Path2D>();
		
		//Create the path2D for each cutPath
		for(Vector<double[]> cutPath : cutPaths){
			Path2D.Double cutPath2D = new Path2D.Double();
			cutPath2D.moveTo(cutPath.get(0)[1], cutPath.get(0)[2]);
			for(int i = 1; i<cutPath.size(); i++){
				cutPath2D.lineTo(cutPath.get(i)[1], cutPath.get(i)[2]);
			}
			cutPath2DCollection.add(cutPath2D);
		}
		mapBits.get(key).setCutPath(cutPath2DCollection);
		
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
	
}
