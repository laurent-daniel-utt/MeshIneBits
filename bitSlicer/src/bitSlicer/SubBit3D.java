package bitSlicer;

import java.awt.geom.Area;
import java.util.Vector;

public class SubBit3D {
	Bit3D motherBit;
	Area areaToExtrude;
	
	public SubBit3D(Vector<Area> areas, Bit3D motherBit){
		this.motherBit = motherBit;
		areaToExtrude = Bit3D.generateAreaToExtrude(areas);
	}
	
	public Area getAreaToExtrude(){
		return areaToExtrude;
	}

}
