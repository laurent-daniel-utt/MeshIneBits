package bitSlicer;

import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.Vector;

import bitSlicer.Slicer.Config.CraftConfig;
import bitSlicer.util.AreaTool;
import bitSlicer.util.Vector2;

public class Bit3D {
	
	Vector<Path2D> cutPaths = null; //In the local coordinate system
	Vector2 origin; //Position of the center of the full bit in the general coordinate system
	Vector2 orientation; //Rotation around its local origin point
	Bit2D bit2dToExtrude;
	Vector<Point2D> liftPoints = new Vector<Point2D>();
	Vector<Vector2> depositPoints = new Vector<Vector2>();
	
	
	public Bit3D(Vector<Bit2D> bits2D, Vector2 origin, Vector2 orientation, int sliceToSelect) throws Exception{
		
		double maxNumberOfSlices = CraftConfig.bitThickness / CraftConfig.sliceHeight;
		double percentage = (bits2D.size() / maxNumberOfSlices) * 100;
		
		if(percentage < CraftConfig.minPercentageOfSlices){	
			throw new Exception(){
				private static final long serialVersionUID = 1L;
				public String getMessage(){
					return "This bit is too thin";
				}
			};
		}
		else if(sliceToSelect >= bits2D.size()){
			throw new Exception(){
				private static final long serialVersionUID = 1L;
				public String getMessage(){
					return "The slice to select does not exist in that bit";
				}
			};
		}
		else{
			this.origin = origin;
			this.orientation = orientation;
			bit2dToExtrude = bits2D.get(sliceToSelect);
			cutPaths = bit2dToExtrude.getRawCutPaths();
		}	
	}
	
	public Area getRawArea(){
		return bit2dToExtrude.getRawArea();
	}
	
	public Vector2 getOrientation(){
		return orientation;
	}
	
	public void computeLiftPoints(){
		for(Area subBit : bit2dToExtrude.getRawAreas()){
			Point2D liftPoint = computeLiftPoint(subBit);
			liftPoints.add(liftPoint);
			if(liftPoint != null){
				//A new lift point means a new deposit point which is the addition of the origin point of the bit and the lift point (which is in the local coordinate system of the bit)
				depositPoints.add(origin.add(new Vector2(liftPoints.lastElement().getX(), liftPoints.lastElement().getY())));
			}
			else
				depositPoints.addElement(null);
		}
	}
	
	public Point2D computeLiftPoint(Area subBit){
		if(cutPaths != null)
			return AreaTool.getLiftPoint(subBit, CraftConfig.suckerDiameter / 2);
		else if(CraftConfig.suckerDiameter < CraftConfig.bitWidth && CraftConfig.suckerDiameter < CraftConfig.bitLength)
			return new Point2D.Double(0, 0);
		else
			return null;
	}
	
	public Vector<Point2D> getLiftPoints(){
		return liftPoints;
	}
	
	public Vector<Path2D> getCutPaths(){
		return cutPaths;
	}
}
