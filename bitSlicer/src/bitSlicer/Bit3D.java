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
	Vector2 origin; //In the general coordinate system
	Vector2 orientation; //Rotation around its origin point
	Bit2D bit2dToExtrude;
	Point2D liftPoint = null;
	Vector<Point2D> liftPoints = new Vector<Point2D>();
	Vector<Vector2> rotations = new Vector<Vector2>();
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
			computeLiftPoint();
		}	
	}
	
	public Area getRawArea(){
		return bit2dToExtrude.getRawArea();
	}
	
	public Vector2 getOrientation(){
		return orientation;
	}
	
	public void computeLiftPoint(){
		if(cutPaths != null)
			liftPoint = AreaTool.getLiftPoint2(getRawArea(), CraftConfig.suckerDiameter / 2);
		else if(CraftConfig.suckerDiameter < CraftConfig.bitWidth && CraftConfig.suckerDiameter < CraftConfig.bitLength)
			liftPoint = new Point2D.Double(0, 0);
		else
			liftPoint = null;
	}
	
	public Point2D getLiftPoint(){
		return liftPoint;
	}
}
