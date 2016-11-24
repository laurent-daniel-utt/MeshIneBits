package bitSlicer.PatternTemplates;

import java.util.Hashtable;
import bitSlicer.Bit2D;
import bitSlicer.Pattern;
import bitSlicer.PatternTemplates.PatternTemplate;
import bitSlicer.Slicer.Config.CraftConfig;
import bitSlicer.util.Vector2;

public class PatternTemplate2 extends PatternTemplate {

	public PatternTemplate2(double skirtRadius) {
		super(skirtRadius);
	}

	public Pattern createPattern(double layerNumber) {
		Hashtable<Vector2, Bit2D> mapBits = new Hashtable<Vector2, Bit2D>();
		boolean evenI = false;
		boolean evenJ = false;
		double iOffSet = Math.sqrt(2) / 2 * CraftConfig.bitLength;// + CraftConfig.xBitsOffset;
		double jOffSet = Math.sqrt(2) / 2 * CraftConfig.bitWidth;// + CraftConfig.yBitsOffset;
		for (double i = patternStart.x; i <= patternEnd.x; i = i + iOffSet){		
			if (evenI){
				evenI = false;
			}
			else{
				evenI = true;
			}
			for (double j = patternStart.y; j <= patternEnd.y; j = j + jOffSet){
				if (evenJ){
					evenJ = false;
				}
				else{
					evenJ = true;
				}
				Vector2 originBit;
				Vector2 orientationBit;
				double layerOffSet = 0; // In this pattern we apply an offset on 1 layer on 2
				if (layerNumber%2 == 0){
					layerOffSet = jOffSet;
				}
				if (evenI && evenJ){				
					originBit = new Vector2(i, j + layerOffSet);
					orientationBit = new Vector2(1, 1);
					mapBits.put(originBit, new Bit2D(originBit, orientationBit));
				}
				else if (!evenI && !evenJ){
					originBit = new Vector2(i, j + layerOffSet);
					orientationBit = new Vector2(-1, 1);
					mapBits.put(originBit, new Bit2D(originBit, orientationBit));
				}		
			}
		}
		return new Pattern(mapBits, new Vector2(1,0), skirtRadius); //every pattern have no rotation in that template
	}

}