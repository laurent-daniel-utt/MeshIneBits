/**
 * 
 */
package meshIneBits.patterntemplates;

import java.util.Vector;

import meshIneBits.Bit2D;
import meshIneBits.GeneratedPart;
import meshIneBits.Layer;
import meshIneBits.Pattern;
import meshIneBits.config.PatternParameterConfig;
import meshIneBits.util.Vector2;

/**
 * 
 * <p>
 * Given a layer, this pattern will divide it into multiple squares called
 * <em>unit square</em>, whose size bases on the diameter (geometry) of suction
 * cup.
 * </p>
 * 
 * <p>
 * Each unit square is:
 * 
 * <ul>
 * <li><b>accepted</b> if it is entirely inside of layer's boundary (touch
 * possible).</li>
 * <li><b>to be considered</b> if a part of it is inside of layer bound (at
 * least a zone, not a point).</li>
 * <li><b>ignored</b> if else.</li>
 * </ul>
 * </p>
 * 
 * <p>
 * Then, we construct bits by grouping a number of unit squares. A bit is
 * <em>regular</em> (craftable) if it contains at least an <b>accepted</b> unit
 * square.
 * </p>
 * 
 * @author Quoc Nhat Han TRAN
 *
 */
public class UnitSquarePattern extends PatternTemplate {

	/*
	 * (non-Javadoc)
	 * 
	 * @see meshIneBits.patterntemplates.PatternTemplate#initiateConfig()
	 */
	@Override
	public void initiateConfig() {
		// TODO Auto-generated method stub
		// Unit square's length
		// = lift point diameter + horizontal margin
		config.add(new PatternParameterConfig("horizontalMargin", "Horizontal margin",
				"A little space allowing Lift Point move horizontally", 1.0, 100.0, 2.0, 1.0));
		// Unit square's width
		// = lift point diameter + vertical margin
		config.add(new PatternParameterConfig("verticalMargin", "Vertical margin",
				"A little space allowing Lift Point move vertically", 1.0, 100.0, 2.0, 1.0));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * meshIneBits.patterntemplates.PatternTemplate#ready(meshIneBits.GeneratedPart)
	 */
	/**
	 * This method does nothing.
	 * @return <tt>false</tt>
	 */
	@Override
	public boolean ready(GeneratedPart generatedPart) {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see meshIneBits.patterntemplates.PatternTemplate#createPattern(int)
	 */
	/**
	 * This constructor will only leave a blank space. The real job is done in
	 * {@link #optimize(Layer)}
	 */
	@Override
	public Pattern createPattern(int layerNumber) {
		return new Pattern(new Vector<Bit2D>(), new Vector2(1, 0));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see meshIneBits.patterntemplates.PatternTemplate#optimize(meshIneBits.Layer)
	 */
	@Override
	public int optimize(Layer actualState) {
		// TODO Auto-generated method stub
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * meshIneBits.patterntemplates.PatternTemplate#moveBit(meshIneBits.Pattern,
	 * meshIneBits.util.Vector2, meshIneBits.util.Vector2)
	 */
	@Override
	public Vector2 moveBit(Pattern actualState, Vector2 bitKey, Vector2 localDirection) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * meshIneBits.patterntemplates.PatternTemplate#moveBit(meshIneBits.Pattern,
	 * meshIneBits.util.Vector2, meshIneBits.util.Vector2, double)
	 */
	@Override
	public Vector2 moveBit(Pattern actualState, Vector2 bitKey, Vector2 localDirection, double distance) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getIconName() {
		// TODO Change icon
		return "p3.png";
	}

	@Override
	public String getCommonName() {
		return "Unit Square Pattern";
	}

	@Override
	public String getDescription() {
		return "This pattern defines systematically possible position of Lift Points, basing on which we will create a regular bit.";
	}

	@Override
	public String getHowToUse() {
		return "Unit square's size should be a little bit larger than Lift Points, so that we have safe space between bits.";
	}

}
