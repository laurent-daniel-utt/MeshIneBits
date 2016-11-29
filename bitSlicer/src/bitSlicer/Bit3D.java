package bitSlicer;

import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.util.Vector;

import bitSlicer.util.Vector2;
import bitSlicer.util.Vector3;

public class Bit3D {
	
	Vector<Bit2D> bits2D = new Vector<Bit2D>();
	Vector<SubBit3D> subBits3D = null;
	Vector<Path2D> cutPaths = null;
	Vector3 origin; //In the general coordinate system
	Vector2 orientation;
	Area areaToExtrude = null; //In the local coordinate system
	
	public Bit3D(Bit2D bit2D, Vector3 origin){
		this.bits2D.add(bit2D);
		this.origin = origin;
		computeBit3D();
	}
	
	public Bit3D(Vector<Bit2D> bits2D, Vector3 origin){
		this.bits2D = bits2D;
		this.origin = origin;
		computeBit3D();
	}
	
	/*
	 * Generate subBits if there is some.
	 * Otherwise set directly the area from which the extrusion will make the 3D shape of the bit3D.
	 */
	public void computeBit3D(){
		if(bits2D.get(0).getAreas().size() > 1)
			generateSubBits3D();
		else{
			Vector<Area> areas = new Vector<Area>();
			for(Bit2D b : bits2D){
				areas.add(b.getArea());
			}
			areaToExtrude = generateAreaToExtrude(areas);
		}
	}
	
	/*
	 * generate the subBits.
	 */
	private void generateSubBits3D(){
		Vector<SubBit3D> subBits3D = new Vector<SubBit3D>();
		int nbrSubBits = bits2D.get(0).getAreas().size();
		for(int i = 0; i < nbrSubBits; i++){
			Vector<Area> areas = new Vector<Area>();
			for(Bit2D b : bits2D)
				areas.add(b.getAreas().get(i));
			subBits3D.add(new SubBit3D(areas, this));
		}
		
	}
	
	/*
	 * So far in the project we simply use the area of the first bit2D to determine the shape of the bit3D
	 * Later the project is to adjust the shape using several slices in the same layer
	 */
	public static Area generateAreaToExtrude(Vector<Area> areas){
		return areas.get(0);
	}
}
