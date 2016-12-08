package bitSlicer;

import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.util.Vector;

import bitSlicer.util.Vector2;

public class Bit3D {
	
	Vector<Path2D> cutPaths = null; //In the local coordinate system
	Vector2 origin; //In the general coordinate system
	Vector2 orientation; //Rotation around its origin point
	Bit2D bit2dToExtrude;
	
	public Bit3D(Vector<Bit2D> bits2D, Vector2 origin, Vector2 orientation){
		this.origin = origin;
		this.orientation = orientation;
		selectBit2dToExtrude(bits2D);
	}

	public void selectBit2dToExtrude(Vector<Bit2D> bits2D){
		
		Bit2D selectedBit;
		
		//Compute the average area
		double averageAreaValue = 0;
		Vector<Integer> customAreaValue = new Vector<Integer>();
		for(Bit2D b : bits2D){
			customAreaValue.add(b.getCustomAreaValue());
			averageAreaValue += customAreaValue.lastElement();
		}
		averageAreaValue /= (bits2D.size());
		
		//Select the bit2D with the area the closer to the average area value
		double deltaAreaValue = Math.abs(customAreaValue.get(0) - averageAreaValue);
		selectedBit = bits2D.get(0);
		for(int i = 1; i < bits2D.size(); i++){
			double delta = Math.abs(customAreaValue.get(i) - averageAreaValue);
			if(delta < deltaAreaValue){
				deltaAreaValue = delta;
				selectedBit = bits2D.get(i);
			}
		}
		
		bit2dToExtrude = selectedBit;
		cutPaths = selectedBit.getCutPaths();
	}
	
	public Area getRawArea(){
		return bit2dToExtrude.getRawArea();
	}
	
	public Vector2 getOrientation(){
		return orientation;
	}
}
