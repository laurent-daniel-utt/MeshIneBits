package bitSlicer;

import java.awt.geom.Area;

public class SubBit2D {
	
	Bit2D motherBit;
	Area area = new Area();
	
	public SubBit2D(Bit2D motherBit, Area area){
		this.motherBit = motherBit;
		this.area = area;
	}
}
