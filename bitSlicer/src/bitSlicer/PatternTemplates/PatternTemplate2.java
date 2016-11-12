package bitSlicer.PatternTemplates;

import java.util.Vector;
import bitSlicer.Bit2D;
import bitSlicer.Pattern;
import bitSlicer.PatternTemplates.PatternTemplate;
import bitSlicer.Slicer.Config.CraftConfig;
import bitSlicer.util.Vector2;

public class PatternTemplate2 extends PatternTemplate {

	public PatternTemplate2(Vector2 offSet, double skirtRadius) {
		super(offSet, skirtRadius);
	}

	public Pattern createPattern(double layerNumber) {
		Vector<Bit2D> bits = new Vector<Bit2D>();
		boolean evenI = false;
		boolean evenJ = false;
		double iOffSet = Math.sqrt(2) / 2 * CraftConfig.bitLength;
		double jOffSet = Math.sqrt(2) / 2 * CraftConfig.bitWidth;
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
				if (evenI && evenJ){				
					originBit = new Vector2(i, j);
					orientationBit = new Vector2(1, 1);
					bits.add(new Bit2D(originBit, orientationBit));
				}
				if (!evenI && !evenJ){
					originBit = new Vector2(i, j);
					orientationBit = new Vector2(-1, 1);
					bits.add(new Bit2D(originBit, orientationBit));
				}		
			}
		}
		return new Pattern(bits, new Vector2(1,0)); //every pattern have no rotation in that template
	}

}