package meshIneBits.patterntemplates;

import java.util.Vector;

import meshIneBits.Bit2D;
import meshIneBits.GeneratedPart;
import meshIneBits.Layer;
import meshIneBits.Pattern;
import meshIneBits.config.CraftConfig;
import meshIneBits.config.PatternParameterConfig;
import meshIneBits.util.Vector2;

/**
 * 
 */
public class DiagonalHerringbonePattern extends PatternTemplate {

	private Vector2 patternStart;
	private Vector2 patternEnd;
	private double skirtRadius;
	private double bitsOffset;

	public Pattern createPattern(int layerNumber) {
		// Setup parameters
		bitsOffset = (double) config.get("bitsOffset").getCurrentValue();
		// Start
		Vector<Bit2D> bits = new Vector<Bit2D>();
		double xOffSet = Math.sqrt(2.0) / 2.0 * CraftConfig.bitLength + bitsOffset;
		double yOffSet = Math.sqrt(2.0) / 2.0 * CraftConfig.bitWidth + bitsOffset;
		for (double i = patternStart.x; i <= patternEnd.x; i = i + 2 * xOffSet){
			for (double j = patternStart.y; j <= patternEnd.y; j = j + 2 * yOffSet){
				Vector2 originBit;
				Vector2 orientationBit;
				double layerOffSet = 0; // In this pattern we apply an offset on 1 layer on 2
				if (layerNumber%2 == 0){
					layerOffSet = yOffSet;
				}
				originBit = new Vector2(i, j + layerOffSet);
				orientationBit = new Vector2(1, 1);
				bits.add(new Bit2D(originBit, orientationBit));	
			}
		}
		for (double i = patternStart.x + xOffSet; i <= patternEnd.x; i = i + 2 * xOffSet){
			for (double j = patternStart.y + yOffSet; j <= patternEnd.y; j = j + 2 * yOffSet){
				Vector2 originBit;
				Vector2 orientationBit;
				double layerOffSet = 0; // In this pattern we apply an offset on 1 layer on 2
				if (layerNumber%2 == 0){
					layerOffSet = yOffSet;
				}
				originBit = new Vector2(i, j + layerOffSet);
				orientationBit = new Vector2(-1, 1);
				bits.add(new Bit2D(originBit, orientationBit));	
			}
		}
		return new Pattern(bits, new Vector2(1,0)); //every pattern have no rotation in that template
	}
	@Override
	public int optimize(Layer actualState) {
		// TODO Auto-generated method stub
		return -1;
	}
	@Override
	public Vector2 moveBit(Pattern actualState, Vector2 bitKey, Vector2 localDirection) {
		double distance = 0;
		if (localDirection.x == 0) {// up or down
			distance = CraftConfig.bitWidth / 2;
		} else if (localDirection.y == 0) {// left or right
			distance = CraftConfig.bitLength / 2;
		}
		return this.moveBit(actualState, bitKey, localDirection, distance);
	}
	@Override
	public Vector2 moveBit(Pattern actualState, Vector2 bitKey, Vector2 localDirection, double distance) {
		return actualState.moveBit(bitKey, localDirection, distance);
	}
	
	@Override
	public String getCommonName() {
		return "Diagonal Herringbone";
	}
	
	@Override
	public String getIconName() {
		return "p2.png";
	}
	
	@Override
	public String getDescription() {
		return "A rather usual pattern. No auto-optimization implemented yet.";
	}
	
	@Override
	public String getHowToUse() {
		return "Choose the gap you desired.";
	}

	@Override
	public void initiateConfig() {
		config.add(new PatternParameterConfig("bitsOffset", "Space between bits",
				"The horizontal and vertical gap in mm", 1.0, 100.0, 3.0, 1.0));
	}

	@Override
	public boolean ready(GeneratedPart generatedPart) {
		this.skirtRadius = generatedPart.getSkirtRadius();
		double maxiSide = Math.max(CraftConfig.bitLength, CraftConfig.bitWidth);
		this.patternStart = new Vector2(-skirtRadius - maxiSide, -skirtRadius - maxiSide);
		this.patternEnd = new Vector2(skirtRadius + maxiSide, skirtRadius + maxiSide);
		return true;
	}
}