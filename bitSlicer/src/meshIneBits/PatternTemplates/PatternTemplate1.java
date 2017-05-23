package meshIneBits.PatternTemplates;

import java.util.Vector;

import meshIneBits.Bit2D;
import meshIneBits.Layer;
import meshIneBits.Pattern;
import meshIneBits.Config.CraftConfig;
import meshIneBits.util.Vector2;

/*
 * Simplest pattern possible: a grid with a rotation of 90� 1 layer on 2
 */

public class PatternTemplate1 extends PatternTemplate {

	public PatternTemplate1(double skirtRadius) {
		super(skirtRadius);
	}


	/**
	 * TODO
	 * Eliminate this pattern
	 */
	public Pattern createPattern(int layerNumber) {
		Vector<Bit2D> bits = new Vector<Bit2D>();
		Vector2 coo = patternStart;
		int column = 1;
		while (coo.x <= patternEnd.x){
			while(coo.y <= patternEnd.y){
				bits.add(new Bit2D(coo, new Vector2(1,0))); //every bits have no rotation in that template
				coo = coo.add(new Vector2(0, CraftConfig.bitWidth + CraftConfig.yOffset));
			}
			coo = new Vector2(patternStart.x + (CraftConfig.bitLength + CraftConfig.yOffset)*column, patternStart.y);
			column++;
		}
		// in this pattern 1 layer on 2 has a 90� rotation
		Vector2 rotation = new Vector2(1,0);
		if (layerNumber%2 == 0){
			rotation = new Vector2(0,1);
		}
		return new Pattern(bits, rotation, skirtRadius);
	}


	@Override
	public int optimize(Layer actualState) {
		// TODO Auto-generated method stub
		return 0;
	}


	@Override
	public Vector2 moveBit(Pattern actualState, Vector2 bitKey, Vector2 localDirection) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public Vector2 moveBit(Pattern actualState, Vector2 bitKey, Vector2 localDirection, double distance) {
		// TODO Auto-generated method stub
		return null;
	}

}
