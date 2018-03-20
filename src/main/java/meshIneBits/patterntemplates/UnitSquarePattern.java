/**
 * 
 */
package meshIneBits.patterntemplates;

import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import meshIneBits.Bit2D;
import meshIneBits.GeneratedPart;
import meshIneBits.Layer;
import meshIneBits.Pattern;
import meshIneBits.config.CraftConfig;
import meshIneBits.config.PatternParameterConfig;
import meshIneBits.slicer.Slice;
import meshIneBits.util.AreaTool;
import meshIneBits.util.Logger;
import meshIneBits.util.Shape2D;
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

	/**
	 * Equal to horizontal margin plus lift point diameter
	 */
	private double unitLength;
	/**
	 * Equal to vertical margin plus lift point diameter
	 */
	private double unitHeight;

	/*
	 * (non-Javadoc)
	 * 
	 * @see meshIneBits.patterntemplates.PatternTemplate#initiateConfig()
	 */
	@Override
	public void initiateConfig() {
		config.add(new PatternParameterConfig("horizontalMargin", "Horizontal margin",
				"A little space allowing Lift Point move horizontally", 1.0, 100.0, 2.0, 1.0));
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
	 * 
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
		Logger.updateStatus("Paving layer " + actualState.getLayerNumber());
		// Calculate size of unit square
		this.calcUnitSize();
		// Get the boundary
		Vector<Area> zones = AreaTool.getLevel0AreasFrom(actualState.getSelectedSlice());
		// Sum of pavement
		Vector<Bit2D> overallPavement = new Vector<Bit2D>();
		for (Area zone : zones) {
			// Generate the corresponding matrix
			UnitMatrix matrix = new UnitMatrix(zone);
			matrix.resolve();
			overallPavement.addAll(matrix.exportBits());
		}
		actualState.setReferentialPattern(new Pattern(overallPavement, new Vector2(1, 0)));
		return 0;
	}

	/**
	 * Calculate unit's size
	 */
	private void calcUnitSize() {
		this.unitLength = ((double) config.get("horizontalMargin").getCurrentValue()) + CraftConfig.suckerDiameter;
		this.unitHeight = ((double) config.get("verticalMargin").getCurrentValue()) + CraftConfig.suckerDiameter;
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

	private class UnitSquare extends Rectangle2D.Double {
		public UnitSquare(double x, double y) {
			super(x, y, unitLength, unitHeight);
		}

		public UnitState state;

		/**
		 * Find the state of this unit
		 * 
		 * @param area
		 *            where we will fill out by bits
		 */
		public void determineState(Area area) {
			if (area.contains(this)) {
				this.state = UnitState.ACCEPTED;
			} else if (area.intersects(this)) {
				this.state = UnitState.BORDER;
			} else {
				this.state = UnitState.IGNORED;
			}
		}
	}

	/**
	 * Describe the relative position in respect to predefined area
	 * 
	 * @author Quoc Nhat Han TRAN
	 *
	 */
	enum UnitState {
		/**
		 * Unit totally inside area, possibly touching boundary
		 */
		ACCEPTED,
		/**
		 * Unit partially inside area, not counted if only touching boundary
		 */
		BORDER,
		/**
		 * Unit totally outside area
		 */
		IGNORED
	}

	private class UnitMatrix {
		private UnitSquare[][] matrix;
		private Area area;

		/**
		 * Prepare buckets to hold units
		 * 
		 * @param area
		 *            a level 0 area
		 */
		public UnitMatrix(Area area) {
			this.area = area;
			Rectangle2D.Double outerRect = (Double) this.area.getBounds2D();
			int numOfUnitsInLength = (int) Math.ceil(outerRect.getWidth() / unitLength);
			int numOfUnitsInHeight = (int) Math.ceil(outerRect.getHeight() / unitHeight);
			this.matrix = new UnitSquare[numOfUnitsInHeight][numOfUnitsInLength];
			for (int i = 0; i < matrix.length; i++) {
				for (int j = 0; j < matrix[i].length; j++) {
					this.matrix[i][j] = new UnitSquare(outerRect.getX() + j * unitLength,
							outerRect.getY() + i * unitHeight);
					this.matrix[i][j].determineState(area);
				}
			}
		}

		/**
		 * Construct bits from units
		 * 
		 * @return bits regrouped from units
		 */
		public Vector<Bit2D> exportBits() {
			// TODO Auto-generated method stub
			return null;
		}

		/**
		 * Concatenate {@link UnitState#BORDER border} units with
		 * {@link UnitState#ACCEPTED accepted} one
		 */
		public void resolve() {
			for (int i = 0; i < matrix.length; i++) {
				for (int j = 0; j < matrix[i].length; j++) {
					UnitSquare unit = this.matrix[i][j];
					switch (unit.state) {
					case IGNORED:
						// Do nothing
						break;
					case ACCEPTED:
						// Do nothing
						break;
					case BORDER:
						// Check neighbors
						// TODO Auto-generated method stub
						break;
					default:
						break;
					}
				}
			}
		}
	}
}
