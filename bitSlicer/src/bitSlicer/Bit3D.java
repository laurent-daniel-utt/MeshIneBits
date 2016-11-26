package bitSlicer;

import java.awt.geom.Path2D;
import java.util.Vector;

import bitSlicer.util.Vector3;

public class Bit3D {
	
	Vector<Bit2D> bits2D;
	Vector<SubBit3D> subBits3D = null;
	Vector<Path2D> cutPaths = null;
	Vector3 origin;
	
	public Bit3D(Vector<Bit2D> bits2D, Vector3 origin){
		this.bits2D = bits2D;
		this.origin = origin;
	}
	
	public Bit3D(Bit2D bit2D, Vector3 origin){
		this.bits2D = new Vector<Bit2D>();
		this.bits2D.add(bit2D);
		this.origin = origin;
	}
	
	public Bit3D(Vector<Bit2D> bits2D, Vector3 origin, Vector<Path2D> cutPaths){
		this.bits2D = bits2D;
		this.origin = origin;
		this.cutPaths = cutPaths;
	}
	
	public Bit3D(Vector3 origin, Vector<SubBit3D> subBits3D, Vector<Path2D> cutPaths){
		this.origin = origin;
		this.cutPaths = cutPaths;
		this.subBits3D = subBits3D;
	}

}
