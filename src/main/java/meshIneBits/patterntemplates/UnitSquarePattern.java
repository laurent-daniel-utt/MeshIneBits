/**
 * 
 */
package meshIneBits.patterntemplates;

import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Vector;

import meshIneBits.Bit2D;
import meshIneBits.GeneratedPart;
import meshIneBits.Layer;
import meshIneBits.Pattern;
import meshIneBits.config.CraftConfig;
import meshIneBits.config.PatternParameterConfig;
import meshIneBits.util.AreaTool;
import meshIneBits.util.Logger;
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
	private double unitWidth;

	/**
	 * A bit requires at least <tt>n x m</tt> unit squares to cover itself.
	 * <tt>n</tt> is the number of unit squares in horizontal to cover a bit's
	 * length.
	 */
	private int n = 0;
	/**
	 * A bit requires at least <tt>n x m</tt> unit squares to cover itself.
	 * <tt>m</tt> is the number of unit squares in vertical to cover a bit's width.
	 */
	private int m = 0;

	private static String HORIZONTAL_MARGIN = "horizontalMargin";
	private static String VERTICAL_MARGIN = "verticalMargin";

	/*
	 * (non-Javadoc)
	 * 
	 * @see meshIneBits.patterntemplates.PatternTemplate#initiateConfig()
	 */
	@Override
	public void initiateConfig() {
		config.add(new PatternParameterConfig(HORIZONTAL_MARGIN, "Horizontal margin",
				"A little space allowing Lift Point move horizontally", 1.0, 100.0, 2.0, 1.0));
		config.add(new PatternParameterConfig(VERTICAL_MARGIN, "Vertical margin",
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
		Pattern thisPattern = new Pattern(overallPavement, new Vector2(1, 0));
		actualState.setReferentialPattern(thisPattern);
		actualState.rebuild();
		return 0;
	}

	/**
	 * Calculate unit's size and minimum required units to cover a bit
	 */
	private void calcUnitSizeAndLimits() {
		this.unitLength = ((double) config.get("horizontalMargin").getCurrentValue()) + CraftConfig.suckerDiameter;
		this.unitWidth = ((double) config.get("verticalMargin").getCurrentValue()) + CraftConfig.suckerDiameter;
		this.n = (int) Math.ceil(CraftConfig.bitLength / this.unitLength);
		this.m = (int) Math.ceil(CraftConfig.bitWidth / this.unitWidth);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * meshIneBits.patterntemplates.PatternTemplate#moveBit(meshIneBits.Pattern,
	 * meshIneBits.util.Vector2, meshIneBits.util.Vector2)
	 */
	/**
	 * No bit displacement is allowed in this pattern
	 */
	@Override
	public Vector2 moveBit(Pattern actualState, Vector2 bitKey, Vector2 localDirection) {
		// double distance = 0;
		// if (localDirection.x == 0) {// up or down
		// distance = CraftConfig.bitWidth / 2;
		// } else if (localDirection.y == 0) {// left or right
		// distance = CraftConfig.bitLength / 2;
		// }
		// return this.moveBit(actualState, bitKey, localDirection, distance);
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * meshIneBits.patterntemplates.PatternTemplate#moveBit(meshIneBits.Pattern,
	 * meshIneBits.util.Vector2, meshIneBits.util.Vector2, double)
	 */
	/**
	 * No bit displacement is allowed in this pattern
	 */
	@Override
	public Vector2 moveBit(Pattern actualState, Vector2 bitKey, Vector2 localDirection, double distance) {
		// return actualState.moveBit(bitKey, localDirection, distance);
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
		 * The matrix' line in which this unit resides
		 */
		public int _i;

		/**
		 * The matrix' column in which this unit resides
		 */
		public int _j;

		/**
		 * Create a unit square staying inside <tt>zone</tt> with top-left corner at
		 * <tt>(x, y)</tt>
		 * 
		 * @param x
		 * @param y
		 * @param area
		 *            target to pave
		 * @param i
		 *            virtual abscissa. Non-negative, otherwise 0
		 * @param j
		 *            virtual coordinate. Non-negative, otherwise 0
		 */
		public UnitSquare(double x, double y, Area area, int i, int j) {
			super(x, y, unitLength, unitWidth);
			this.determineState(area);
			this.calculateContainedArea(area);
			this._i = (i >= 0 ? i : 0);
			this._j = (j >= 0 ? j : 0);
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
		 *            relating directly to the same area of this unit
		 * @return
		 */
		public boolean touch(UnitSquare that) {
			if (Math.abs(this._i - that._i) + Math.abs(this._j - that._j) == 1) {
				return true;
			}
			// if (((Math.abs(this.getX() - that.getX()) == unitLength) && (this.getY() ==
			// that.getY()))
			// || ((Math.abs(this.getY() - that.getY()) == unitWidth) && (this.getX() ==
			// that.getX()))) {
			// return true;
			// }
			return false;
		}

		/**
		 * @return intersection of this unit and the zone in which it stays
		 */
		public Area getArea() {
			return this.containedArea;
		}

		@Override
		public String toString() {
			return state + "(" + _i + "," + _j + ")";
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
				super.add(thatUnit);
				this.updateBoundaryAfterAdding(thatUnit);
				return true;
			}
			if (!this.contains(thatUnit) && this.isStillValidIfAdding(thatUnit) && this.havingUnitTouching(thatUnit)) {
				super.add(thatUnit);
				this.updateBoundaryAfterAdding(thatUnit);
				return true;
			} else
				return false;
		}

		/**
		 * Outer border of this polyomino
		 */
		private Rectangle2D.Double boundary = new Rectangle2D.Double();

		/**
		 * Update the coordinate of boundary. Should be only called after adding, if
		 * else, it does nothing.
		 * 
		 * @param thatUnit
		 */
		private void updateBoundaryAfterAdding(UnitSquare thatUnit) {
			if (!this.contains(thatUnit)) // If the unit has not been added
				return;

			if (this.size() == 1)
				// If this is the first unit
				// The boundary of this polyomino should be that of the unit
				this.boundary = thatUnit;
			else
				// If already having other unit
				// We find their union
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
			for (UnitSquare u : this) {
				if (u.touch(thatUnit))
					return true;
			}
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
			Vector2 orientation = this.getBitOrientation();
			if (orientation.x == 1 && orientation.y == 0) {
				// Horizontal
				if (newBoundary.getWidth() > CraftConfig.bitLength || newBoundary.getHeight() > CraftConfig.bitWidth)
					return false;
				else
					return true;
			} else {
				// Vertical
				if (newBoundary.getWidth() > CraftConfig.bitWidth || newBoundary.getHeight() > CraftConfig.bitLength)
					return false;
				else
					return true;
			}
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
		public Bit2D getBit2D() {
			if (this.isEmpty())
				return null;

			Vector2 bitOrientation = this.getBitOrientation();
			String floatpos = this.getBitFloatingPosition();
			Vector2 bitOrigin = this.getBitOrigin(bitOrientation, floatpos);
			Bit2D bit = new Bit2D(bitOrigin, bitOrientation);

			Area bitArea = this.getLimitArea(bit, floatpos);
			Area polyominoArea = this.getUnitedArea();
			bitArea.intersect(polyominoArea);
			bit.updateBoundaries(bitArea);

			if (this.size() > 1) {
				// For test
				System.out.println(this.toString() + "\nfloatpos=" + floatpos);
			}
			return bit;
		}

		/**
		 * Check if the bit should lie horizontally or vertically
		 * 
		 * @return <tt>(1, 0)</tt> if horizontal, otherwise <tt>(0, 1)</tt>
		 */
		private Vector2 getBitOrientation() {
			if (this.boundary.getWidth() >= this.boundary.getHeight()) {
				return new Vector2(1, 0); // Horizontal
			} else {
				return new Vector2(0, 1); // Vertical
			}
		}

		/**
		 * Which corner / side of boundary the bit should float to
		 * 
		 * @return either "top-left", "top-right", "bottom-left" or "bottom-right"
		 */
		private String getBitFloatingPosition() {
			// Direction to float
			// Top-left / Top-right / Bottom-left / Bottom-right
			boolean top = false, left = false, bottom = false, right = false;

			for (UnitSquare unit : this) {
				if (unit.state == UnitState.ACCEPTED) {
					if (unit.getMinY() == this.boundary.getMinY()) {
						// On top side
						top = true;
					} else if (unit.getMaxY() == this.boundary.getMaxY()) {
						// On bottom side
						bottom = true;
					}

					if (unit.getMinX() == this.boundary.getMinX()) {
						// On left side
						left = true;
					} else if (unit.getMaxX() == this.boundary.getMaxX()) {
						// On right side
						right = true;
					}
				}
			}

			// Default float
			// By default, origin floats to top-left corner
			if (!top && !bottom)
				top = true;
			if (!left && !right)
				left = true;

			if (top) {
				if (left)
					return "top-left";
				else
					return "top-right";
			} else {
				if (left)
					return "bottom-left";
				else
					return "bottom-right";
			}
		}

		/**
		 * Deduct the origin point of the bit covering this polyomino
		 * 
		 * @param orientation
		 *            horizontal (1, 0) or vertical (0, 1)
		 * @param floatpos
		 *            either "top-left", "top-right", "bottom-left" or "bottom-right"
		 * @return origin of the bit covering this polyomino
		 */
		private Vector2 getBitOrigin(Vector2 orientation, String floatpos) {

			double h = CraftConfig.bitLength, v = CraftConfig.bitWidth;// horizontal and vertical length in
																		// horizontal orientation
			if (orientation.x == 0 && orientation.y == 1) {
				// if the bit is in vertical orientation
				h = CraftConfig.bitWidth;
				v = CraftConfig.bitLength;
			}

			Vector2 origin = null;
			switch (floatpos) {
			case "top-left":
				origin = new Vector2(this.boundary.getMinX() + h / 2, this.boundary.getMinY() + v / 2);
				break;
			case "top-right":
				origin = new Vector2(this.boundary.getMaxX() - h / 2, this.boundary.getMinY() + v / 2);
				break;
			case "bottom-left":
				origin = new Vector2(this.boundary.getMinX() + h / 2, this.boundary.getMaxY() - v / 2);
				break;
			case "bottom-right":
				origin = new Vector2(this.boundary.getMaxX() - h / 2, this.boundary.getMaxY() - v / 2);
				break;
			default:
				origin = new Vector2(this.boundary.getMinX() + h / 2, this.boundary.getMinY() + v / 2);
				break;
			}

			return origin;
		}

		/**
		 * Union of all units' area (each unit's area is a part of the whole layer's)
		 * 
		 * @return empty area if this polyomino is empty
		 */
		private Area getUnitedArea() {
			Area union = new Area();
			this.stream().forEach(unit -> union.add(unit.getArea()));
			return union;
		}

		/**
		 * The bit surface should be limited inside boundary of this polyomino minus a
		 * minimum margin. The margin should run along opposite border of floating
		 * position. If the bit is bigger than the polyomino, the margin's width will be
		 * equal to pattern's parameter. Otherwise, the margin's width would be the
		 * difference between bit's and polyomino's size.
		 * 
		 * @param bit
		 *            whose origin and orientation are determined
		 * @param floatpos
		 *            either "top-left", "top-right", "bottom-left" or "bottom-right"
		 * @return a rectangular area smaller than boundary
		 */
		private Area getLimitArea(Bit2D bit, String floatpos) {
			Rectangle2D.Double lim = (Double) this.boundary.clone();

			// Determine the float position of bit
			String[] pos = floatpos.split("-");
			// pos[0] would be "top" or "bottom"
			// pos[1] would be "left" or "right"

			double bitHorizontalLength = bit.getLength(), bitVerticalLength = bit.getWidth();
			Vector2 bitOrientation = bit.getOrientation();
			if (bitOrientation.x == 0 && bitOrientation.y == 1) {// If vertical
				bitHorizontalLength = bit.getWidth();
				bitVerticalLength = bit.getLength();
			}

			double horizontalMarginAroundBit;
			if (this.boundary.width <= bitHorizontalLength) {
				// We will put in a margin whose width is equal to pattern's parameter
				// "horizontal margin"
				horizontalMarginAroundBit = (double) UnitSquarePattern.this.config.get(HORIZONTAL_MARGIN)
						.getCurrentValue();
			} else {
				// Margin will be difference between boundary's size and bit's
				horizontalMarginAroundBit = this.boundary.width - bitHorizontalLength;
			}
			lim.width -= horizontalMarginAroundBit;
			if (pos[1].equals("right"))
				// Move top-left corner of boundary to right
				lim.x += horizontalMarginAroundBit;

			double verticalMarginAroundBit;
			if (this.boundary.height <= bitVerticalLength) {
				// We will put in a margin whose width is equal to pattern's parameter
				// "vertical margin"
				verticalMarginAroundBit = (double) UnitSquarePattern.this.config.get(VERTICAL_MARGIN).getCurrentValue();
			} else {
				// Margin will be difference between boundary's size and bit's
				verticalMarginAroundBit = this.boundary.height - bitVerticalLength;
			}
			lim.height -= verticalMarginAroundBit; // equal to bit's vertical length in fact
			if (pos[0].equals("bottom"))
				// Move down top-left corner of boundary
				lim.y += verticalMarginAroundBit;

			return new Area(lim);
		}

		@Override
		public String toString() {
			StringBuilder str = new StringBuilder();
			str.append("[");
			for (UnitSquare u : this) {
				str.append(u);
				str.append(" ");
			}
			str.append("]");
			return str.toString();
		}
	}

	/**
	 * Describe the relative position in respect to predefined area
	 * 
	 * @author Quoc Nhat Han TRAN
	 *
	 */
	private enum UnitState {
		/**
		 * Unit totally inside area, possibly touching boundary
		 */
		ACCEPTED("A"),
		/**
		 * Unit partially inside area, not counted if only touching boundary
		 */
		BORDER("B"),
		/**
		 * Unit totally outside area
		 */
		IGNORED("I");

		private String codename;

		private UnitState(String codename) {
			this.codename = codename;
		}

		@Override
		public String toString() {
			return codename;
		}
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
		private UnitSquare[][] matrixU;
		/**
		 * Indicate each unit belongs which polyomino
		 */
		private Polyomino[][] matrixP;
		/**
		 * Who proposes to the unit <tt>(i, j)</tt>
		 */
		private Map<UnitSquare, Set<UnitSquare>> proposersRegistry;
		/**
		 * Predefine zone on which this matrix is put
		 */
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
			int numOfColumns = (int) Math.ceil(outerRect.getWidth() / unitLength);
			int numOfLines = (int) Math.ceil(outerRect.getHeight() / unitWidth);
			matrixU = new UnitSquare[numOfLines][numOfColumns];
			for (int i = 0; i < matrixU.length; i++) {
				for (int j = 0; j < matrixU[i].length; j++) {
					matrixU[i][j] = new UnitSquare(outerRect.getX() + j * unitLength, outerRect.getY() + i * unitWidth,
							area, i, j);
				}
			}
			matrixP = new Polyomino[numOfLines][numOfColumns];
			proposersRegistry = new HashMap<UnitSquare, Set<UnitSquare>>();
			for (int i = 0; i < matrixU.length; i++) {
				for (int j = 0; j < matrixU[i].length; j++) {
					proposersRegistry.put(matrixU[i][j], new HashSet<UnitSquare>());
				}
			}
		}

		/**
		 * Get bits from polyominos
		 * 
		 * @return bits regrouped from polyominos
		 */
		public Set<Bit2D> exportBits() {
			Set<Polyomino> setPolyominos = new HashSet<Polyomino>();
			for (int i = 0; i < matrixP.length; i++) {
				for (int j = 0; j < matrixP[i].length; j++) {
					if (matrixP[i][j] != null)
						setPolyominos.add(matrixP[i][j]);// Not adding if already had
				}
			}
			Set<Bit2D> setBits = new HashSet<Bit2D>();
			for (Polyomino p : setPolyominos) {
				setBits.add(p.getBit2D());
			}
			return setBits;
		}

		/**
		 * Concatenate {@link UnitState#BORDER border} units with
		 * {@link UnitState#ACCEPTED accepted} one to create polyominos
		 */
		public void resolve() {
			// Make each border unit propose to an accepted one
			for (int i = 0; i < matrixU.length; i++) {
				for (int j = 0; j < matrixU[i].length; j++) {
					UnitSquare unit = matrixU[i][j];
					if (unit.state == UnitState.BORDER) {
						// Check each direct contact unit
						// if it is accepted
						// then propose

						// Above
						if (i > 0) {
							if (matrixU[i - 1][j].state == UnitState.ACCEPTED)
								this.registerProposal(unit, matrixU[i - 1][j]);
						}
						// Below
						if (i + 1 < matrixU.length) {
							if (matrixU[i + 1][j].state == UnitState.ACCEPTED)
								this.registerProposal(unit, matrixU[i + 1][j]);
						}
						// Left
						if (j > 0) {
							if (matrixU[i][j - 1].state == UnitState.ACCEPTED)
								this.registerProposal(unit, matrixU[i][j - 1]);
						}
						// Right
						if (j + 1 < matrixU[i].length) {
							if (matrixU[i][j + 1].state == UnitState.ACCEPTED)
								this.registerProposal(unit, matrixU[i][j + 1]);
						}
					} else if (unit.state == UnitState.ACCEPTED) {
						// Propose to itself
						this.registerProposal(unit, unit);
					} else if (unit.state == UnitState.IGNORED) {
						// No follower
						// Do nothing
					}
				}
			}

			Comparator<UnitSquare> comparingFamousLevel = (u1, u2) -> {
				// Compare between number of followers
				// in descendant order
				return -(proposersRegistry.get(u1).size() - proposersRegistry.get(u2).size());
			};
			List<UnitSquare> targetList = new ArrayList<UnitSquare>(proposersRegistry.keySet());
			Collections.sort(targetList, comparingFamousLevel);

			// TODO Approve marriage
			// A minimal solution
			// Consider the proposals list in descendant order,
			// we approve the concatenation of the most wanted unit first (the first in
			// list)
			// Once approving, remove all its proposals to others (to remain faithful)
			while (!targetList.isEmpty()) {
				UnitSquare target = targetList.get(0);
				targetList.remove(target);
				Set<UnitSquare> proposers = proposersRegistry.get(target);
				if (proposers.isEmpty())
					break; // No more proposals to approve

				// Create a polyomino containing target and all proposers
				Polyomino p = new Polyomino();
				// TODO check validity
				p.add(target);
				p.addAll(proposers);
				// Register p into matrixP
				this.registerPolyomino(p);

				// Remove married units in proposers list to remain faithful
				targetList.removeAll(proposers);
				for (UnitSquare u : targetList) {
					// Cancel proposal of married proposers
					proposersRegistry.get(u).removeAll(proposers);
				}

				// Resort to ensure the ascendant order
				Collections.sort(targetList, comparingFamousLevel);
			}
		}

		/**
		 * Register the polyomino into tracking matrix
		 * 
		 * @param polyomino
		 */
		private void registerPolyomino(Polyomino polyomino) {
			for (UnitSquare u : polyomino) {
				matrixP[u._i][u._j] = polyomino;
			}
		}

		/**
		 * Register the proposal of <tt>proposer</tt> to <tt>target</tt>
		 * 
		 * @param proposer
		 * @param target
		 */
		private void registerProposal(UnitSquare proposer, UnitSquare target) {
			this.proposersRegistry.get(target).add(proposer);
		}

		@Override
		public String toString() {
			StringBuilder str = new StringBuilder();
			str.append("[\n");
			for (int i = 0; i < matrixU.length; i++) {
				str.append("[");
				for (int j = 0; j < matrixU[i].length; j++) {
					str.append(matrixU[i][j].state);
					str.append(",");
				}
				str.append("]\n");
			}
			str.append("]\n");
			return str.toString();
		}
	}
}
