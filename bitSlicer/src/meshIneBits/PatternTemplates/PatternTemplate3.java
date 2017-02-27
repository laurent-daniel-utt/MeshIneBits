package meshIneBits.PatternTemplates;

import java.util.Vector;

import meshIneBits.Bit2D;
import meshIneBits.Pattern;
import meshIneBits.Config.CraftConfig;
import meshIneBits.PatternTemplates.PatternTemplate;
import meshIneBits.util.Vector2;

public class PatternTemplate3 extends PatternTemplate {

	public PatternTemplate3(double skirtRadius) {
		super(skirtRadius);
	}

	public Pattern createPattern(double layerNumber) {
		Vector<Bit2D> bits = new Vector<Bit2D>();
		double cooX = patternStart.x;
		double cooY = patternStart.y;
		Vector2 coo;
		int line = 1;
		for(cooX = patternStart.x; cooX <= patternEnd.x; cooX += CraftConfig.bitLength + CraftConfig.bitsOffset){
			for(cooY = patternStart.y; cooY <= patternEnd.y; cooY += CraftConfig.bitWidth + CraftConfig.bitsOffset){
				if(line % 2 == 0)
					coo = new Vector2(cooX, cooY);
				else
					coo = new Vector2(cooX + (CraftConfig.bitLength + CraftConfig.bitsOffset) / 2, cooY);
				bits.add(new Bit2D(coo, new Vector2(1,0))); //every bits have no rotation in that template
				line++;
			}
		}
		
		// in this pattern 1 layer on 2 has a 90° rotation
		Vector2 rotation = new Vector2(1,0);
		if (layerNumber%2 == 0){
			rotation = new Vector2(0,1);
		}
		return new Pattern(bits, rotation, skirtRadius);
	}

}