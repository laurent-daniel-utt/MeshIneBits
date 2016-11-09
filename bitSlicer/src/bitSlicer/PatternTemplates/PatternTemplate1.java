package bitSlicer.PatternTemplates;

import java.util.Vector;
import bitSlicer.PatternTemplates.PatternTemplate;
import bitSlicer.util.Vector2;
import bitSlicer.Bit2D;
import bitSlicer.Pattern;

/*
 * Simplest pattern possible: a grid with no offset between each layers
 */

public class PatternTemplate1 extends PatternTemplate {

	public PatternTemplate1(Vector2 rotation, Vector2 offSet, double skirtRadius) {
		super(rotation, offSet, skirtRadius);
	}

	public Pattern createPattern(double layerNumber) {
		Vector<Bit2D> bits = new Vector<Bit2D>();
		Vector2 coo = patternStart;
		while (coo.x <= patternEnd.x){
			while(coo.y <= patternEnd.y){
				bits.add(new Bit2D(coo, new Vector2(0,0))); //every bits have no rotation in that template
				coo.add(new Vector2(0,1));
			}
			coo.add(new Vector2(1,0));
		}
		return new Pattern(bits, rotation.add(new Vector2(1,0))); //every pattern have no rotation in that template
	}

}
