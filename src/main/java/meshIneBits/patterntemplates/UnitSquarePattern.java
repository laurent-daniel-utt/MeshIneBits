/**
 * 
 */
package meshIneBits.patterntemplates;

import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
		this.calcUnitSizeAndLimits();
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
	 * Calculate unit's size and minimum required units to cover a bit
	 */
	private void calcUnitSizeAndLimits() {
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
		double distance = 0;
		if (localDirection.x == 0) {// up or down
			distance = CraftConfig.bitWidth / 2;
		} else if (localDirection.y == 0) {// left or right
			distance = CraftConfig.bitLength / 2;
		}
		return this.moveBit(actualState, bitKey, localDirection, distance);
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
		return actualState.moveBit(bitKey, localDirection, distance);
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
		/**
		 * 
		 */
		private static final long serialVersionUID = 4316086827801379838L;

		/**
		 * Relative position to the area containing this square
		 */
		public UnitState state;

		/**
		 * A part of zone's area which is contained by this unit
		 */
		private Area containedArea;

		/**
		 * Create a unit square staying inside <tt>zone</tt> with top-left corner at
		 * <tt>(x, y)</tt>
		 * 
		 * @param x
		 * @param y
		 * @param area
		 */
		public UnitSquare(double x, double y, Area area) {
			super(x, y, unitLength, unitHeight);
			this.determineState(area);
			this.calculateContainedArea(area);
		}

		/**
		 * Find the state of this unit
		 * 
		 * @param area
		 *            the place in which this unit stays
		 */
		private void determineState(Area area) {
			if (area.contains(this)) {
				this.state = UnitState.ACCEPTED;
			} else if (area.intersects(this)) {
				this.state = UnitState.BORDER;
			} else {
				this.state = UnitState.IGNORED;
			}
		}

		/**
		 * Given a zone in which we place this unit, calculate how much space it holds.
		 * This method should be called at least once before converting
		 * {@link Polyomino} into {@link Bit2D}
		 * 
		 * @param zone
		 */
		private void calculateContainedArea(Area zone) {
			this.containedArea = new Area(this);
			this.containedArea.intersect(zone);
		}

		/**
		 * Check if this touches other unit
		 * 
		 * @param that
		 * @return
		 */
		public boolean touch(UnitSquare that) {
			if (((Math.abs(this.getX() - that.getX()) == unitLength) && (this.getY() == that.getY()))
					|| ((Math.abs(this.getY() - that.getY()) == unitHeight) && (this.getX() == that.getX()))) {
				return true;
			}
			return false;
		}

		/**
		 * @return intersection of this unit and the zone in which it stays
		 */
		public Area getArea() {
			return this.containedArea;
		}

	}

	/**
	 * A sequence of adjacent unit squares. A polyomino could not be too larger than
	 * a bit
	 * 
	 * @author Quoc Nhat Han TRAN
	 *
	 */
	private class Polyomino extends HashSet<UnitSquare> {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1974861227965075981L;

		/**
		 * Only accept unit which touches at lease an other in the polyomino
		 * 
		 * @param thatUnit
		 *            a unit not {@link UnitState#IGNORED ignored}
		 * @return <tt>false</tt> if
		 *         <ul>
		 *         <li>already having that unit</li>
		 *         <li>or that unit does not touch anyone</li>
		 *         <li>or concatenation violates the size of a bit (exceeding height or
		 *         length)</li>
		 *         </ul>
		 */
		@Override
		public boolean add(UnitSquare thatUnit) {
			if (this.isEmpty()) {
				// Quick check
				return super.add(thatUnit);
			}
			if (!this.contains(thatUnit) && this.isStillValidIfAdding(thatUnit) && this.havingUnitTouching(thatUnit)) {
				super.add(thatUnit);
				this.updateBoundaryAfterAdding(thatUnit);
				return true;
			} else
				return false;
		}

		private Rectangle2D.Double boundary;

		/**
		 * Update the coordinate of boundary
		 * 
		 * @param thatUnit
		 */
		private void updateBoundaryAfterAdding(UnitSquare thatUnit) {
			this.boundary = (Double) this.boundary.createUnion(thatUnit);
		}

		/**
		 * Verify connectivity
		 * 
		 * @param thatUnit
		 * @return <tt>true</tt> if at least one internal unit is directly adjacent to
		 *         that
		 */
		private boolean havingUnitTouching(UnitSquare thatUnit) {
			Predicate<UnitSquare> directNeighbor = thisUnit -> thisUnit.touch(thatUnit);
			if (this.stream().anyMatch(directNeighbor))
				return true;
			else
				return false;
		}

		/**
		 * Verify size
		 * 
		 * @param thatUnit
		 * @return <tt>false</tt> if concatenation violates the size of a bit (exceeding
		 *         height or length)
		 */
		private boolean isStillValidIfAdding(UnitSquare thatUnit) {
			Rectangle2D.Double newBoundary = (Double) this.boundary.createUnion(thatUnit);
			if (newBoundary.getWidth() > CraftConfig.bitLength || newBoundary.getHeight() > CraftConfig.bitWidth)
				return false;
			else
				return true;
		}

		/**
		 * Create a bit from surface of this polyomino. By default, the generated bit
		 * will float in the top-left corner. But if an {@link UnitState#ACCEPTED
		 * accepted} unit lying on either side of boundary, the generated bit will float
		 * toward that side.
		 * 
		 * @return a regular bit if this polyomino has at least one
		 *         {@link UnitState#ACCEPTED accepted} unit
		 */
		public Bit2D convertToBit2D() {
			Vector2 bitOrientation = null;
			if (this.boundary.getWidth() >= this.boundary.getHeight()) {
				bitOrientation = new Vector2(1, 0); // Horizontal
			} else {
				bitOrientation = new Vector2(0, 1); // Vertical
			}

			Vector2 bitOrigin = this.calculateBitOrigin(bitOrientation);

			Bit2D bit = new Bit2D(bitOrigin, bitOrientation);
			bit.updateBoundaries(this.getUnitedArea());
			return bit;
		}

		/**
		 * By calculating which corner / side of boundary the bit should float to, we
		 * deduct the origin point of the bit covering this polyomino
		 * 
		 * @param orientation
		 *            horizontal (1, 0) or vertical (0, 1)
		 * @return origin of the bit covering this polyomino
		 */
		private Vector2 calculateBitOrigin(Vector2 orientation) {
			// Direction to float
			boolean top = false, left = false, bottom = false, right = false;

			Predicate<UnitSquare> isAcceptedUnit = unit -> unit.state == UnitState.ACCEPTED;
			Predicate<UnitSquare> onTopSide = unit -> unit.y == this.boundary.y;
			Predicate<UnitSquare> onLeftSide = unit -> unit.x == this.boundary.x;
			Predicate<UnitSquare> onBottomSide = unit -> unit.getMaxY() == this.boundary.getMaxY();
			Predicate<UnitSquare> onRightSide = unit -> unit.getMaxX() == this.boundary.getMaxX();

			Stream<UnitSquare> acceptedUnits = this.stream().filter(isAcceptedUnit)
					.collect(Collectors.toCollection(HashSet::new)).stream();
			if (acceptedUnits.anyMatch(onTopSide))
				top = true;
			if (acceptedUnits.anyMatch(onLeftSide))
				left = true;
			if (acceptedUnits.anyMatch(onBottomSide))
				bottom = true;
			if (acceptedUnits.anyMatch(onRightSide))
				right = true;

			// Default float
			if (!top && !bottom)
				top = true;
			if (!left && !right)
				left = true;

			double h = CraftConfig.bitLength / 2, v = CraftConfig.bitWidth / 2;// horizontal and vertical length
			if (orientation.x == 0 && orientation.y == 1) {// vertical orientation
				h = CraftConfig.bitWidth / 2;
				v = CraftConfig.bitLength / 2;
			}

			Vector2 origin = null;
			if (top) {
				if (left) {
					origin = new Vector2(this.boundary.getMinX() + h / 2, this.boundary.getMinY() + v / 2);
				} else {
					origin = new Vector2(this.boundary.getMaxX() - h / 2, this.boundary.getMinY() + v / 2);
				}
			} else {
				if (left) {
					origin = new Vector2(this.boundary.getMinX() + h / 2, this.boundary.getMaxY() - v / 2);
				} else {
					origin = new Vector2(this.boundary.getMaxX() - h / 2, this.boundary.getMaxY() - v / 2);
				}
			}
			// By default, origin floats to top-left corner

			return origin;
		}

		/**
		 * Union of all units' area (each unit's area is a part of the whole layer's)
		 * 
		 * @return <tt>null</tt> if this polyomino is empty
		 */
		private Area getUnitedArea() {
			Area union = new Area();
			this.stream().forEach(unit -> union.add(unit.getArea()));
			return union;
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

	/**
	 * Flat description of units
	 * 
	 * @author Quoc Nhat Han TRAN
	 *
	 */
	private class UnitMatrix {
		/**
		 * All units created
		 */
		private UnitSquare[][] matrix;
		/**
		 * Predefine zone on which this matrix is put
		 */
		private Area area;
		/**
		 * A set of polyominos used to fill the whole matrix
		 */
		private Set<Polyomino> pavage;

		/**
		 * Prepare buckets to hold units
		 * 
		 * @param area
		 *            a level 0 area
		 */
		public UnitMatrix(Area area) {
			this.area = area;
			Rectangle2D.Double outerRect = (Double) this.area.getBounds2D();
			int numOfColumns = (int) Math.ceil(outerRect.getWidth() / unitLength);
			int numOfLines = (int) Math.ceil(outerRect.getHeight() / unitHeight);
			this.matrix = new UnitSquare[numOfLines][numOfColumns];
			for (int i = 0; i < matrix.length; i++) {
				for (int j = 0; j < matrix[i].length; j++) {
					this.matrix[i][j] = new UnitSquare(outerRect.getX() + j * unitLength,
							outerRect.getY() + i * unitHeight, area);
				}
			}
			this.pavage = new HashSet<Polyomino>();
		}

		/**
		 * Get bits from polyominos
		 * 
		 * @return bits regrouped from polyominos
		 */
		public Vector<Bit2D> exportBits() {
			Vector<Bit2D> v = new Vector<Bit2D>();
			this.pavage.forEach(piece -> v.add(piece.convertToBit2D()));
			return v;
		}

		/**
		 * Concatenate {@link UnitState#BORDER border} units with
		 * {@link UnitState#ACCEPTED accepted} one to create polyominos
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
						// TODO Resolution
						// Test
						Polyomino p = new Polyomino();
						p.add(unit);
						this.pavage.add(p);
						break;
					case BORDER:
						// TODO Resolution
						// Check neighbors
						// Test
						Polyomino p2 = new Polyomino();
						p2.add(unit);
						this.pavage.add(p2);
						break;
					default:
						break;
					}
				}
			}
		}
	}
}
