package bitSlicer.PatternTemplates;

import java.util.Hashtable;
import bitSlicer.PatternTemplates.PatternTemplate;
import bitSlicer.Slicer.Config.CraftConfig;
import bitSlicer.util.Vector2;
import bitSlicer.Bit2D;
import bitSlicer.Pattern;

/*
 * Simplest pattern possible: a grid with a rotation of 90° 1 layer on 2
 */

public class PatternTemplate1 extends PatternTemplate {

	public PatternTemplate1(double skirtRadius) {
		super(skirtRadius);
	}

	public Pattern createPattern(double layerNumber) {
		Hashtable<Vector2, Bit2D> mapBits = new Hashtable<Vector2, Bit2D>();
		Vector2 coo = patternStart;
		int column = 1;
		while (coo.x <= patternEnd.x){
			while(coo.y <= patternEnd.y){
				mapBits.put(coo, new Bit2D(coo, new Vector2(1,0))); //every bits have no rotation in that template
				coo = coo.add(new Vector2(0, CraftConfig.bitWidth + CraftConfig.yBitsOffset));
			}
			coo = new Vector2(patternStart.x + (CraftConfig.bitLength + CraftConfig.xBitsOffset)*column, patternStart.y);
			column++;
		}
		// in this pattern 1 layer on 2 has a 90° rotation
		Vector2 rotation = new Vector2(1,0);
		if (layerNumber%2 == 0){
			rotation = new Vector2(0,1);
		}
		return new Pattern(mapBits, rotation, skirtRadius);
	}

}
