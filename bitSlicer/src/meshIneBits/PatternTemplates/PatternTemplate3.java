package meshIneBits.PatternTemplates;

import java.util.Vector;

import meshIneBits.Bit2D;
import meshIneBits.Pattern;
import meshIneBits.Config.CraftConfig;
import meshIneBits.util.Vector2;
/**
 * Pattern improved from classic pattern {@link PatternTemplate1}.
 * @author NHATHAN
 *
 */
public class PatternTemplate3 extends PatternTemplate {

	public PatternTemplate3(double skirtRadius) {
		super(skirtRadius);
	}

	public Pattern createPattern(int layerNumber) {
		Vector<Bit2D> bits = new Vector<Bit2D>();
//		double cooX = patternStart.x;
//		double cooY = patternStart.y;
//		Vector2 coo;
//		int line = 1;
//		for(cooX = patternStart.x; cooX <= patternEnd.x; cooX += CraftConfig.bitLength + CraftConfig.bitsOffset){
//			for(cooY = patternStart.y; cooY <= patternEnd.y; cooY += CraftConfig.bitWidth + CraftConfig.bitsOffset){
//				if(line % 2 == 0)
//					coo = new Vector2(cooX, cooY);
//				else
//					coo = new Vector2(cooX + (CraftConfig.bitLength + CraftConfig.bitsOffset) / 2, cooY);
//				bits.add(new Bit2D(coo, new Vector2(1,0))); //every bits have no rotation in that template
//				line++;
//			}
//		}
		
		double f = CraftConfig.bitsWidthSpace; // space between 2 consecutive bits' height's side
		double e = CraftConfig.bitsLengthSpace; // space between 2 consecutive bits' length's side
		double L = CraftConfig.bitLength;
		double H = CraftConfig.bitWidth;
		// The first bit is displaced by diffxOffset and diffyOffset
		Vector2 _1stBit = new Vector2(CraftConfig.diffxOffset, CraftConfig.diffyOffset);  
		// Fill out the square
		int lineNum = 0;// Initialize
		// Vertically downward
		while (_1stBit.y - H/2 + lineNum * (H + e) <= patternEnd.y ){// Check the left-high corner of bit
			// Horizontally
			if (lineNum % 2 == 0){
				fillHorizontally(new Vector2(_1stBit.x, _1stBit.y + lineNum * (H + e)) , bits);
			} else {
				fillHorizontally(new Vector2(_1stBit.x + L/2 + f/2, _1stBit.y + lineNum * (H + e)), bits);
			}
			lineNum++;
		}
		// Vertically upward
		lineNum = 1; // Reinitialize
		while (_1stBit.y + H/2 - lineNum * (H + e) >= patternStart.y ){// Check the left-low corner of bit
			// Horizontally
			if (lineNum % 2 == 0){
				fillHorizontally(new Vector2(_1stBit.x, _1stBit.y - lineNum * (H + e)) , bits);
			} else {
				fillHorizontally(new Vector2(_1stBit.x + L/2 + f/2, _1stBit.y - lineNum * (H + e)), bits);
			}
			lineNum++;
		}
				
		// in this pattern 1 layer on 2 has a 90° rotation
//		Vector2 rotation = new Vector2(1,0);
//		if (layerNumber%2 == 0){
//			rotation = new Vector2(0,1);
//		}
		double alpha = CraftConfig.diffRotation;
		Vector2 customizedRotation = Vector2.getEquivalentVector((alpha * layerNumber) % 360);
		return new Pattern(bits, customizedRotation, skirtRadius);
	}
	/**
	 * Fill a line of bits into set of bits, given the origin of the first bit. 
	 * @param _1stBitOrigin origin of departure
	 * @param bits set of bits of this layer
	 */
	private void fillHorizontally(Vector2 _1stBitOrigin, Vector<Bit2D> bits){
		double L = CraftConfig.bitLength;
		double f = CraftConfig.bitsWidthSpace;
		// To the right
		int colNum = 0; // Initialize
		while (_1stBitOrigin.x - L/2 + colNum * (L + f) <= patternEnd.x){// check the left-high corner of bit
			bits.add(new Bit2D(new Vector2(_1stBitOrigin.x + colNum * (L + f), _1stBitOrigin.y), new Vector2(1, 0)));
			colNum++;
		}
		// To the left
		colNum = 1; // Reinitialize
		while (_1stBitOrigin.x + L/2 - colNum * (L + f) >= patternStart.x){// check the right-high corner of bit
			bits.add(new Bit2D(new Vector2(_1stBitOrigin.x - colNum * (L + f), _1stBitOrigin.y), new Vector2(1, 0)));
			colNum++;
		}
	}
}