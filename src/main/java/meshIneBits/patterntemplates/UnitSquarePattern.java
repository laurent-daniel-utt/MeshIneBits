/**
 * 
 */
package meshIneBits.patterntemplates;

import java.awt.Point;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Level;

import meshIneBits.Bit2D;
import meshIneBits.GeneratedPart;
import meshIneBits.Layer;
import meshIneBits.Pattern;
import meshIneBits.config.CraftConfig;
import meshIneBits.config.patternParameter.BooleanParam;
import meshIneBits.config.patternParameter.DoubleParam;
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
	 * Maximum length of a
	 * {@link meshIneBits.patterntemplates.UnitSquarePattern.UnitMatrix.Polyomino
	 * Polyomino} if it lies horizontally
	 */
	private double maxPLength;
	/**
	 * Maximum width of a
	 * {@link meshIneBits.patterntemplates.UnitSquarePattern.UnitMatrix.Polyomino
	 * Polyomino} if it lies horizontally
	 */
	private double maxPWidth;

	/**
	 * To not let bit graze each other
	 */
	private final double SAFETY_MARGIN = 1.0;

	private static final String HORIZONTAL_MARGIN = "horizontalMargin";
	private static final String VERTICAL_MARGIN = "verticalMargin";

	private static final java.util.logging.Logger LOGGER = Logger.createSimpleInstanceFor(UnitSquarePattern.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see meshIneBits.patterntemplates.PatternTemplate#initiateConfig()
	 */
	@Override
	public void initiateConfig() {
		config.add(new DoubleParam(HORIZONTAL_MARGIN, "Horizontal margin",
				"A little space allowing Lift Point move horizontally", 1.0, 100.0, 2.0, 1.0));
		config.add(new DoubleParam(VERTICAL_MARGIN, "Vertical margin",
				"A little space allowing Lift Point move vertically", 1.0, 100.0, 2.0, 1.0));
		config.add(new BooleanParam("applyQuickRegroup", "Use quick regroup",
				"Allow pattern to regroup some border units before actually resolving", true));
		config.add(new DoubleParam("limitActions", "Depth of search", "Number of actions to take before giving up", 1.0,
				1000000.0, 10000.0, 1.0));
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
		return true;
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
		LOGGER.info("Paving layer " + actualState.getLayerNumber());
		// Calculate size of unit square
		this.calcUnitSizeAndLimits();
		// Update the choice on applying quick regroup
		applyQuickRegroup = (boolean) config.get("applyQuickRegroup").getCurrentValue();
		// Limit depth of search
		limitActions = (int) Math.round((double) config.get("limitActions").getCurrentValue());
		// Get the boundary
		Vector<Area> zones = AreaTool.getLevel0AreasFrom(actualState.getSelectedSlice());
		// Sum of pavement
		Vector<Bit2D> overallPavement = new Vector<Bit2D>();
		for (Area zone : zones) {
			// Generate the corresponding matrix
			UnitMatrix matrix = new UnitMatrix(zone);
			if (matrix.resolve()) {
				LOGGER.info("Solution found for " + zone);
				overallPavement.addAll(matrix.exportBits());
				LOGGER.info(matrix.pToString());
			} else {
				LOGGER.warning("Pavement of layer " + actualState.getLayerNumber() + " failed.");
				Logger.updateStatus("Pavement of layer " + actualState.getLayerNumber() + " failed.");
				return -1;
			}
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
		this.maxPLength = ((int) Math.ceil(CraftConfig.bitLength / this.unitLength)) * this.unitLength;
		this.maxPWidth = ((int) Math.ceil(CraftConfig.bitWidth / this.unitWidth)) * this.unitWidth;
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
		return "p5.png";
	}

	@Override
	public String getCommonName() {
		return "Unit Square Pattern";
	}

	@Override
	public String getDescription() {
		return "This pattern transforms the to-be-filled zone into a grid of unit squares, which we will regroup into regular bits later.";
	}

	@Override
	public String getHowToUse() {
		return "Unit square's size should be a little bit larger than Lift Points, so that we have safe space between bits.";
	}

	private boolean applyQuickRegroup = false;

	/**
	 * Set the pattern to use quick regroup some border units before actually
	 * resolving
	 * 
	 * @param b
	 */
	public void setApplyQuickRegroup(boolean b) {
		applyQuickRegroup = b;
	}

	/**
	 * Limit the depth of search
	 */
	private int limitActions = 10000;

	public void setLimitAction(int l) {
		limitActions = l;
	}

	/**
	 * Describe the relative position of a {@link UnitSquare} in respect to
	 * predefined area
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
	 * To decide which {@link Puzzle} will be the first target to begin the dfs
	 * search
	 * 
	 * @see #setCandidatesSorter(String)
	 */
	private Strategy candidateSorter = Strategy.NATURAL;

	/**
	 * To decide which {@link Puzzle} will be the first to try
	 * 
	 * @see #setPossibilitiesSorter(String)
	 */
	private Strategy possibilitySorter = Strategy.NATURAL;

	/**
	 * @param name
	 *            "NATURAL" or "DUTY_FIRST". If not found, will set NATURAL by
	 *            default
	 * @see Strategy
	 */
	private void setCandidatesSorter(String name) {
		try {
			this.candidateSorter = Strategy.valueOf(name.toUpperCase());
		} catch (Exception e) {
			this.candidateSorter = Strategy.NATURAL;
		}
	}

	/**
	 * @param name
	 *            "NATURAL" or "BORDER_FIRST". If not found, will set NATURAL by
	 *            default
	 * @see Strategy
	 */
	private void setPossibilitiesSorter(String name) {
		try {
			this.possibilitySorter = Strategy.valueOf(name.toUpperCase());
		} catch (Exception e) {
			this.possibilitySorter = Strategy.NATURAL;
		}
	}

	/**
	 * Useful comparators for puzzles
	 */
	private enum Strategy {
		NATURAL("Largest > Top-most > Left-most", (p1, p2) -> {
			// Largest
			if (p1.size() > p2.size())
				return -1;
			else if (p1.size() < p2.size())
				return 1;
			else {
				// Top most
				Point p1p = p1.getTopCoor();
				Point p2p = p2.getTopCoor();
				if (p1p.y < p2p.y)
					return -1;
				else if (p1p.y > p2p.y)
					return 1;
				else {
					// Left most
					if (p1p.x < p2p.x)
						return -1;
					else if (p1p.x > p2p.x)
						return 1;
					else
						return 0;
				}
			}
		}),

		DUTY_FIRST(
				"(Only for accepted units and polyominos) Having most contact (direct or semi-direct) with border units",
				(p1, p2) -> -(p1.getDuty().size() - p2.getDuty().size())),

		BORDER_FIRST("Prioritize the border units", (p1, p2) -> {
			double i = p1.getIsolatedLevel() - p2.getIsolatedLevel();
			if (i > 0)
				return -1;
			else if (i < 0)
				return 1;
			else
				return 0;
		});

		private String description;
		private Comparator<Puzzle> comparator;

		private Strategy(String description, Comparator<Puzzle> comparator) {
			this.description = description;
			this.comparator = comparator;
		}

		/**
		 * @return comparator under this strategy
		 */
		public Comparator<Puzzle> _c() {
			return comparator;
		}

		/**
		 * @return description
		 */
		public String _d() {
			return description;
		}
	}

	/**
	 * Represents a combination of {@link UnitSquare} on matrix
	 * 
	 * @author Quoc Nhat Han TRAN
	 *
	 */
	private interface Puzzle {

		/**
		 * Check if 2 puzzle can be merged. Should check this before
		 * {@link #merge(Puzzle)}
		 * 
		 * @param puzzle
		 *            a {@link UnitSquare} or {@link Polyomino}
		 */
		public boolean canMergeWith(Puzzle puzzle);

		/**
		 * Put together two pieces of puzzles
		 * 
		 * @param other
		 * 
		 * @return A new {@link Polyomino} containing all {@link UnitSquare} making up
		 *         these 2 puzzles. <tt>null</tt> if there is no contact between them
		 */
		public Puzzle merge(Puzzle other);

		/**
		 * Whether this puzzle has been merged with another, basing on their presence on
		 * {@link UnitMatrix#matrixP}
		 * 
		 * @return
		 */
		public boolean isMerged();

		/**
		 * @return 1 if a single unit, or actual size if a polyomino
		 */
		public int size();

		/**
		 * @return Top-most and left-most virtual coordination of current puzzle.
		 *         <tt>(x,y) = (j, i)</tt>
		 */
		public Point getTopCoor();

		/**
		 * @return all neighbors (direct or semi-direct) this puzzle needs to save
		 */
		public Set<Puzzle> getDuty();

		/**
		 * @return 3 (+0.1 -> 0.8) for border unit, 0 for accepted unit or polyomino
		 */
		public double getIsolatedLevel();
	}

	/**
	 * Flat description of multiple {@link UnitSquare}s
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

		private int countPolyomino = 1;
		private int countAction = 1;
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
			LOGGER.fine("Init a matrix for " + area);
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
			LOGGER.fine("Simple representation:\r\n" + this);
		}

		/**
		 * Get bits from polyominos
		 * 
		 * @return bits regrouped from polyominos
		 */
		public Set<Bit2D> exportBits() {
			Set<Polyomino> setPolyominos = this.collectPolyominos();
			Set<Bit2D> setBits = new HashSet<Bit2D>();
			for (Polyomino p : setPolyominos) {
				setBits.add(p.getBit2D());
			}
			return setBits;
		}

		/**
		 * @return all polyominos filling {@link #matrixP}
		 */
		private Set<Polyomino> collectPolyominos() {
			Set<Polyomino> setPolyominos = new HashSet<Polyomino>();
			for (int i = 0; i < matrixP.length; i++) {
				for (int j = 0; j < matrixP[i].length; j++) {
					if (matrixP[i][j] != null)
						setPolyominos.add(matrixP[i][j]);// Not adding if already had
				}
			}
			return setPolyominos;
		}

		/**
		 * Concatenate {@link UnitState#BORDER border} units with
		 * {@link UnitState#ACCEPTED accepted} one to create polyominos
		 * 
		 * @return <tt>true</tt> if a solution found
		 */
		public boolean resolve() {
			LOGGER.fine("Resolve this matrix");
			if (applyQuickRegroup) {
				LOGGER.finer("Apply quick regroup strategy");
				this.quickRegroup();
			}
			// Init graph of neighbors
			neighbors = new ConnectivityGraph();
			// Init duty graph on demand
			duty = new DutyGraph();
			// Choose strategy of sorting candidates
			setCandidatesSorter("DUTY_FIRST");
			// Choose strategy of sorting possibilities
			setPossibilitiesSorter("BORDER_FIRST");
			// Establish matrix of candidates and possibilities
			this.findCandidates();
			this.sortCandidates();
			// Try to cover the border first
			if (this.dfsTry() == false) // cannot save all border units
				return false;
			// Else
			// We pave as quickly as possibile
			// We might optimize later
			this.quickPave();
			return true;
		}

		/**
		 * Quickly pave the rest of zone without using dfs
		 */
		private void quickPave() {
			for (int i = 0; i < matrixP.length; i++) {
				for (int j = 0; j < matrixP[i].length; j++) {
					Puzzle p = (matrixP[i][j] != null ? matrixP[i][j]
							: matrixU[i][j].state == UnitState.ACCEPTED ? matrixU[i][j] : null);
					if (p == null)
						continue;

					// Check concat to the left
					if (j > 0) {
						Puzzle pLeft = (matrixP[i][j - 1] != null ? matrixP[i][j - 1]
								: matrixU[i][j - 1].state == UnitState.ACCEPTED ? matrixU[i][j - 1] : null);
						if (p.canMergeWith(pLeft)) {
							p = p.merge(pLeft);
							registerPuzzle(p);
						}
					}

					// Check concat to the top
					if (i > 0) {
						Puzzle pTop = (matrixP[i - 1][j] != null ? matrixP[i - 1][j]
								: matrixU[i - 1][j].state == UnitState.ACCEPTED ? matrixU[i - 1][j] : null);
						if (p.canMergeWith(pTop)) {
							p = p.merge(pTop);
							registerPuzzle(p);
						}
					}
				}
			}
		}

		/**
		 * A candidate is a puzzle to try as trigger of concatenation in
		 * {@link #dfsTry()}. It has to be either a {@link Polyomino} or
		 * {@link UnitState#ACCEPTED ACCEPTED} {@link UnitSquare}.
		 */
		private List<Puzzle> candidates;

		/**
		 * Each candidate comes with a list of possibilities, which are in fact puzzles
		 * that can couple with that candidate
		 */
		private Map<Puzzle, List<Puzzle>> possibilites;

		/**
		 * Graph of direct contacts between non {@link UnitState#IGNORED}
		 * {@link UnitSquare}s and {@link Polyomino}s
		 */
		private ConnectivityGraph neighbors;

		/**
		 * Graph of {@link UnitSquare}s to save of each {@link UnitState#ACCEPTED
		 * ACCEPTED} {@link UnitSquare} and {@link Polyomino}
		 */
		private DutyGraph duty;

		/**
		 * Check if no more {@link UnitState#BORDER BORDER} {@link UnitSquare}
		 * 
		 * @return <tt>false</tt> if a border unit is not merged into polyominos
		 */
		private boolean noMoreBorderUnits() {
			for (int i = 0; i < matrixU.length; i++) {
				for (int j = 0; j < matrixU[i].length; j++) {
					if (matrixU[i][j].state == UnitState.BORDER && matrixP[i][j] == null) {
						LOGGER.finest(matrixU[i][j] + " is not rescued");
						return false;
					}
				}
			}
			return true;
		}

		/**
		 * Try to cover the border by trying in order each candidate with each its
		 * possibility
		 */
		private boolean dfsTry() {
			LOGGER.finer("Depth-first search to resolve border units");
			// Initiate root of actions
			Action currentAction;
			try {
				currentAction = new Action(null, null, null);
			} catch (Exception e) {
				LOGGER.log(Level.SEVERE, "Cannot create root of actions", e);
				return false;
			}
			Action childAction = null;
			do {
				try {
					childAction = currentAction.nextChild();
				} catch (TooDeepSearchException e) {
					LOGGER.severe(e.getMessage());
					return false;
				}
				if (childAction != null) {
					// If there is a way to continue
					childAction.realize();
					// Register candidate and its neighbors
					if (candidateSorter == Strategy.DUTY_FIRST)
						registerDutyOfMergedPolyomino(childAction);
					registerNeighborsOfMergedPolyomino(childAction);
					registerCandidate(childAction.getResult());
					// Descend
					currentAction = childAction;
					LOGGER.finest("Do " + childAction.toString() + "->" + childAction.getResult() + "\r\n"
							+ "Neighbors of trigger=" + neighbors.of(childAction.getTrigger()) + "\r\n"
							+ "Neighbors of target=" + neighbors.of(childAction.getTarget()) + "\r\n" + "Possibilites="
							+ possibilites.get(childAction.getResult()));
				} else {
					if (noMoreBorderUnits())
						return true;
					// In case of condition not being fulfilled yet
					// If there is no more child from root
					if (currentAction.isRoot())
						return false;
					// Else
					// Revert
					currentAction.undo();
					LOGGER.finest("Undo " + currentAction.getResult() + "->" + currentAction);
					// Climb up in tree
					currentAction = currentAction.getParent();
				}
			} while (true);
		}

		// /**
		// * Check if the current state of matrixP satisfies
		// *
		// * @return <tt>true</tt> if each non-{@link UnitState#IGNORED IGNORED}
		// * {@link UnitSquare} ({@link UnitState#ACCEPTED ACCEPTED} or
		// * {@link UnitState#BORDER BORDER}) belongs to one {@link Polyomino}
		// */
		// private boolean solutionFound() {
		// for (int i = 0; i < matrixP.length; i++) {
		// for (int j = 0; j < matrixP[i].length; j++) {
		// // If a non ignored unit belongs to no polyomino
		// if (matrixU[i][j].state != UnitState.IGNORED && matrixP[i][j] == null)
		// return false;
		// }
		// }
		// return true;
		// }

		/**
		 * Given current state of {@link #matrixP} and {@link #matrixU}, we find all
		 * candidates and register them
		 * 
		 * @see #candidates
		 * @see #registerCandidate(Puzzle)
		 */
		private void findCandidates() {
			LOGGER.finer("Find candidates and possibilities for each candidate");
			candidates = new ArrayList<Puzzle>();
			possibilites = new HashMap<Puzzle, List<Puzzle>>();
			for (int i = 0; i < matrixU.length; i++) {
				for (int j = 0; j < matrixU[i].length; j++) {
					if (matrixP[i][j] != null)
						// Register a polyomino
						registerCandidate(matrixP[i][j]);
					else if (matrixU[i][j].state == UnitState.ACCEPTED)
						// Only register an accepted unit
						registerCandidate(matrixU[i][j]);
				}
			}
		}

		/**
		 * Sort the list of candidates using {@link Strategy} defined by
		 * {@link UnitSquarePattern#candidateSorter}
		 */
		private void sortCandidates() {
			LOGGER.finer("Sort candidates by " + candidateSorter.toString() + "(" + candidateSorter._d() + ")");
			candidates.sort(candidateSorter._c());
		}

		/**
		 * A quick regrouping {@link UnitSquare}s at the border with
		 * {@link UnitState#ACCEPTED accepted} one. Not deterministic
		 */
		private void quickRegroup() {
			LOGGER.finer("Quick regrouping border units");
			// Make each border unit propose to an accepted one
			proposersRegistry = new HashMap<UnitSquare, Set<UnitSquare>>();
			for (int i = 0; i < matrixU.length; i++) {
				for (int j = 0; j < matrixU[i].length; j++) {
					proposersRegistry.put(matrixU[i][j], new HashSet<UnitSquare>());
				}
			}
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
						// this.registerProposal(unit, unit);
					} else if (unit.state == UnitState.IGNORED) {
						// No follower
						// Do nothing
					}
				}
			}

			Comparator<UnitSquare> comparingFamousLevel = (u1, u2) -> {
				// Compare between number of followers
				// in descendant order
				if (proposersRegistry.get(u1).size() > proposersRegistry.get(u2).size())
					return -1;
				else if (proposersRegistry.get(u1).size() < proposersRegistry.get(u2).size())
					return 1;
				else
					return 0;
			};
			List<UnitSquare> targetList = new ArrayList<UnitSquare>(proposersRegistry.keySet());
			targetList.sort(comparingFamousLevel);

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
				p.add(target);
				Set<UnitSquare> rejectedProposers = new HashSet<UnitSquare>();
				for (UnitSquare proposer : proposers) {
					if (!p.canMergeWith(proposer)) {
						rejectedProposers.add(proposer);
					} else {
						p = p.merge(proposer);
					}
				}
				// Register p into matrixP
				this.registerPuzzle(p);
				// Successful proposers
				proposers.removeAll(rejectedProposers);

				// Remove married units in proposers list to remain faithful
				targetList.removeAll(proposers);
				for (UnitSquare u : targetList) {
					// Cancel proposal of married proposers
					proposersRegistry.get(u).removeAll(proposers);
				}

				// Resort to ensure the ascendant order
				targetList.sort(comparingFamousLevel);
			}
		}

		/**
		 * Register a puzzle into tracking matrix
		 * 
		 * @param puzzle
		 *            if a {@link Polyomino}, each corresponding tile in
		 *            {@link #matrixP} will be it. If a {@link UnitSquare}, the
		 *            corresponding tile will be <tt>null</tt>
		 */
		private void registerPuzzle(Puzzle puzzle) {
			if (puzzle instanceof Polyomino) {
				Polyomino p = (Polyomino) puzzle;
				for (UnitSquare u : p) {
					matrixP[u._i][u._j] = p;
				}
			} else if (puzzle instanceof UnitSquare) {
				UnitSquare u = (UnitSquare) puzzle;
				matrixP[u._i][u._j] = null;
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

		/**
		 * Add a new {@link Puzzle} to list of {@link #candidates} for further try. Also
		 * figure out its possibilities and sort them by
		 * {@link UnitSquarePattern#possibilitySorter}
		 * 
		 * @param puzzle
		 *            Its connectivities should be already saved in {@link #neighbors}
		 * @see #candidates
		 */
		private void registerCandidate(Puzzle puzzle) {
			// Push it in #candidates
			if (candidates.contains(puzzle))
				return;
			candidates.add(0, puzzle); // Push to the first
			// Calculate its possibilities
			List<Puzzle> list = new ArrayList<Puzzle>(neighbors.of(puzzle));
			// Remove not border units
			list.removeIf(p -> {
				if (!(p instanceof UnitSquare))
					return true;
				UnitSquare u = (UnitSquare) p;
				if (u.state != UnitState.BORDER)
					return true;
				// Only retain border unit
				return false;
			});
			// Remove unmergeable puzzles
			list.removeIf(p -> !p.canMergeWith(puzzle));
			// Sort possibilities
			list.sort(possibilitySorter._c());
			// Register
			possibilites.put(puzzle, list);
		}

		/**
		 * Register level of duty of a newly created polymoino
		 * 
		 * @param action
		 */
		private void registerDutyOfMergedPolyomino(Action action) {
			Puzzle result = action.getResult();
			Puzzle trigger = action.getTrigger();
			Puzzle target = action.getTarget();
			Set<Puzzle> dutyOfResult = new HashSet<Puzzle>();
			// Intersection
			dutyOfResult.addAll(duty.of(trigger));
			dutyOfResult.addAll(duty.of(target));
			// Filter
			dutyOfResult.removeIf(Puzzle::isMerged);
			// Register
			duty.put(result, dutyOfResult);
		}

		/**
		 * Find neighbors of {@link Puzzle} resulted by <tt>action</tt> and register
		 * them
		 * 
		 * @param action
		 */
		private void registerNeighborsOfMergedPolyomino(Action action) {
			Puzzle result = action.getResult();
			Puzzle trigger = action.getTrigger();
			Puzzle target = action.getTarget();
			Set<Puzzle> neighborsOfResult = new HashSet<Puzzle>();
			// Intersection of 2 neighborhoods
			neighborsOfResult.addAll(neighbors.of(trigger));
			neighborsOfResult.addAll(neighbors.of(target));
			// Remove merged ones
			neighborsOfResult.removeIf(Puzzle::isMerged);
			// Register neighbors
			neighbors.put(result, neighborsOfResult);
		}

		/**
		 * Remove the puzzle (a merged one resulted by an {@link Action}) from
		 * candidates and its possibilities. Also remove from {@link #neighbors}
		 * 
		 * @param puzzle
		 */
		private void unregisterCandidate(Puzzle puzzle) {
			candidates.remove(puzzle);
			possibilites.remove(puzzle);
			neighbors.remove(puzzle);
		}

		@Override
		public String toString() {
			StringBuilder str = new StringBuilder();
			str.append("[\r\n");
			for (int i = 0; i < matrixU.length; i++) {
				str.append(i + "[");
				for (int j = 0; j < matrixU[i].length; j++) {
					str.append(matrixU[i][j].state);
					str.append(",");
				}
				str.append("]\r\n");
			}
			str.append("]");
			return str.toString();
		}

		public String pToString() {
			Set<Polyomino> setPolyominos = this.collectPolyominos();
			StringBuilder str = new StringBuilder("Representations of polyominos in matrix\n");
			str.append("[\r\n");
			for (int i = 0; i < matrixP.length; i++) {
				str.append(i + "[");
				for (int j = 0; j < matrixP[i].length; j++) {
					if (matrixP[i][j] != null) {
						str.append(matrixP[i][j].id);
						str.append(",");
					} else {
						str.append("0,");
					}
				}
				str.append("]\r\n");
			}
			str.append("]\r\n");
			for (Polyomino p : setPolyominos) {
				str.append(p.toString() + "\r\n");
			}
			return str.toString();
		}

		/**
		 * Represents a small square (or rectangle) on surface of {@link Layer}
		 * 
		 * @author Quoc Nhat Han TRAN
		 *
		 */
		private class UnitSquare extends Rectangle2D.Double implements Puzzle {
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
			public final int _i;

			/**
			 * The matrix' column in which this unit resides
			 */
			public final int _j;

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
				if (i < 0 || j < 0)
					throw new IllegalArgumentException("Virtual coordinates of a Unit Square must not be negative.");
				this._i = i;
				this._j = j;
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
				if (Math.abs(this._i - that._i) + Math.abs(this._j - that._j) == 1)
					return true;
				else
					return false;
			}

			@Override
			public String toString() {
				return state + "(" + _i + "," + _j + ")";
			}

			@Override
			public Polyomino merge(Puzzle other) {
				Polyomino p = new Polyomino();
				p.add(this);
				boolean success = p.add(other);
				if (success) {
					return p;
				} else
					return null;
			}

			@Override
			public boolean isMerged() {
				return matrixP[_i][_j] != null;
			}

			/**
			 * Two {@link UnitSquare} are equal if their virtual coordinates are identical
			 */
			@Override
			public boolean equals(Object arg0) {
				if (!(arg0 instanceof UnitSquare))
					return false;
				UnitSquare u = (UnitSquare) arg0;
				return u._i == this._i && u._j == this._j;
			}

			@Override
			public int hashCode() {
				return Objects.hash(_i, _j);
			}

			@Override
			public boolean canMergeWith(Puzzle puzzle) {
				if (puzzle instanceof UnitSquare) {
					UnitSquare u = (UnitSquare) puzzle;
					return (this.state == UnitState.ACCEPTED || u.state == UnitState.ACCEPTED) && this.touch(u);
				} else if (puzzle instanceof Polyomino)
					return ((Polyomino) puzzle).canMergeWith(this);
				else
					return false;
			}

			@Override
			public int size() {
				return 1;
			}

			@Override
			public Point getTopCoor() {
				return new Point(this._j, this._i);
			}

			@Override
			public Set<Puzzle> getDuty() {
				return duty.get(this);
			}

			@Override
			public double getIsolatedLevel() {
				switch (this.state) {
				case BORDER:
					return 3 + this.getOrphanLevel();
				default:
					return 0;
				}
			}

			/**
			 * @return the number of non {@link UnitState#ACCEPTED ACCEPTED}
			 *         {@link UnitSquare}s around
			 */
			private double getOrphanLevel() {
				int count = 0;
				// Top
				if (_i > 0 && matrixU[_i - 1][_j].state != UnitState.ACCEPTED)
					count++;
				// Bottom
				if (_i + 1 < matrixU.length && matrixU[_i + 1][_j].state != UnitState.ACCEPTED)
					count++;
				// Left
				if (_j > 0 && matrixU[_i][_j - 1].state != UnitState.ACCEPTED)
					count++;
				// Right
				if (_j + 1 < matrixU[_i].length && matrixU[_i][_j + 1].state != UnitState.ACCEPTED)
					count++;
				// Top-left
				if (_i > 0 && _j > 0 && matrixU[_i - 1][_j - 1].state != UnitState.ACCEPTED)
					count++;
				// Top-right
				if (_i > 0 && _j + 1 < matrixU[_i].length && matrixU[_i - 1][_j + 1].state != UnitState.ACCEPTED)
					count++;
				// Bottom-right
				if (_i + 1 < matrixU.length && _j + 1 < matrixU[_i + 1].length
						&& matrixU[_i + 1][_j + 1].state != UnitState.ACCEPTED)
					count++;
				// Bottom-left
				if (_i + 1 < matrixU.length && _j > 0 && matrixU[_i + 1][_j - 1].state != UnitState.ACCEPTED)
					count++;
				return count / 10;
			}

			/**
			 * Only use this method after resolving completely
			 * 
			 * @return this area minus a safe space. <tt>null</tt> if {@link #state} is
			 *         {@link UnitState#IGNORED IGNORED}
			 */
			public Area getSafeArea() {
				if (state == UnitState.IGNORED)
					return null;
				Rectangle2D.Double r = (Double) super.clone();
				// Reduce border
				// Top
				if (_i > 0 && matrixP[_i - 1][_j] != null && matrixP[_i - 1][_j] != matrixP[_i][_j]) {
					r.y += SAFETY_MARGIN;
					r.height -= SAFETY_MARGIN;
				}
				// Bottom
				if (_i + 1 < matrixP.length && matrixP[_i + 1][_j] != null && matrixP[_i + 1][_j] != matrixP[_i][_j]) {
					r.height -= SAFETY_MARGIN;
				}
				// Left
				if (_j > 0 && matrixP[_i][_j - 1] != null && matrixP[_i][_j - 1] != matrixP[_i][_j]) {
					r.x += SAFETY_MARGIN;
					r.width -= SAFETY_MARGIN;
				}
				// Right
				if (_j + 1 < matrixP[_i].length && matrixP[_i][_j + 1] != null
						&& matrixP[_i][_j + 1] != matrixP[_i][_j]) {
					r.width -= SAFETY_MARGIN;
				}
				// Intersect area
				Area a = (Area) this.containedArea.clone();
				a.intersect(new Area(r));
				// Whittle corners
				// Reset r
				r = new Rectangle2D.Double();
				// Top-right
				if (_i > 0 && _j + 1 < matrixP[_i].length && matrixP[_i - 1][_j + 1] != null
						&& matrixP[_i - 1][_j + 1] != matrixP[_i][_j]) {
					r.setRect(this.getMaxX() - SAFETY_MARGIN, this.getMinY(), SAFETY_MARGIN, SAFETY_MARGIN);
					a.subtract(new Area(r));
				}
				// Top-left
				if (_i > 0 && _j > 0 && matrixP[_i - 1][_j - 1] != null && matrixP[_i - 1][_j - 1] != matrixP[_i][_j]) {
					r.setRect(this.getMinX(), this.getMinY(), SAFETY_MARGIN, SAFETY_MARGIN);
					a.subtract(new Area(r));
				}
				// Bottom-left
				if (_i + 1 < matrixP.length && _j > 0 && matrixP[_i + 1][_j - 1] != null
						&& matrixP[_i + 1][_j - 1] != matrixP[_i][_j]) {
					r.setRect(this.getMinX(), this.getMaxY() - SAFETY_MARGIN, SAFETY_MARGIN, SAFETY_MARGIN);
					a.subtract(new Area(r));
				}
				// Bottom-right
				if (_i + 1 < matrixP.length && _j + 1 < matrixP[_i].length && matrixP[_i + 1][_j + 1] != null
						&& matrixP[_i + 1][_j + 1] != matrixP[_i][_j]) {
					r.setRect(this.getMaxX() - SAFETY_MARGIN, this.getMaxY() - SAFETY_MARGIN, SAFETY_MARGIN,
							SAFETY_MARGIN);
					a.subtract(new Area(r));
				}

				return a;
			}
		}

		/**
		 * A sequence of adjacent {@link UnitSquare}s. A polyomino could not be too
		 * larger than a {@link Bit2D}. It contains at least one
		 * {@link UnitState#ACCEPTED ACCEPTED} {@link UnitSquare}
		 * 
		 * @author Quoc Nhat Han TRAN
		 *
		 */
		private class Polyomino extends HashSet<UnitSquare> implements Puzzle {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1974861227965075981L;

			public final int id;

			public Polyomino() {
				super();
				this.id = countPolyomino++;
			}

			/**
			 * Always check {@link #canMergeWith(Puzzle)} before hand
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
				super.add(thatUnit);
				this.updateBoundaryAfterAdding(thatUnit);
				return true;
			}

			/**
			 * Outer border of this polyomino
			 */
			private Rectangle2D.Double boundary = new Rectangle2D.Double();

			/**
			 * @return boundary in {@link Double} precision
			 */
			public Rectangle2D.Double getBoundary() {
				return boundary;
			}

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
			 * @param that
			 * @return <tt>true</tt> if at least one internal unit is directly adjacent to
			 *         that
			 */
			public boolean touch(Puzzle that) {
				if (that instanceof UnitSquare) {
					UnitSquare thatU = (UnitSquare) that;
					for (UnitSquare u : this) {
						if (u.touch(thatU))
							return true;
					}
				} else if (that instanceof Polyomino) {
					Polyomino thatP = (Polyomino) that;
					for (UnitSquare u : this) {
						for (UnitSquare u2 : thatP) {
							if (u.touch(u2))
								return true;
						}
					}
				}
				return false;
			}

			/**
			 * Verify size
			 * 
			 * @param that
			 * @return <tt>false</tt> if concatenation violates the size of a bit (exceeding
			 *         height or length)
			 */
			private boolean isStillValidIfAdding(Puzzle that) {
				if (that instanceof UnitSquare)
					return checkBoundaryUnionWith((UnitSquare) that);
				else if (that instanceof Polyomino)
					return checkBoundaryUnionWith(((Polyomino) that).getBoundary());
				return false;
			}

			/**
			 * Check if the union of boundary is still valid for producing a {@link Bit2D}
			 * 
			 * @param r
			 * @return
			 */
			private boolean checkBoundaryUnionWith(Rectangle2D r) {
				Rectangle2D.Double newBoundary = (Double) this.boundary.createUnion(r);
				Vector2 orientation = this.getBitOrientation();
				if (orientation.x == 1 && orientation.y == 0) {
					// Horizontal
					if (newBoundary.getWidth() > maxPLength || newBoundary.getHeight() > maxPWidth)
						return false;
					else
						return true;
				} else {
					// Vertical
					if (newBoundary.getWidth() > maxPWidth || newBoundary.getHeight() > maxPLength)
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

				Area bitLimArea = this.getLimArea(floatpos, bitOrientation);
				Area polyominoArea = this.getUnitedArea();
				bitLimArea.intersect(polyominoArea);
				bit.updateBoundaries(bitLimArea);

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
					origin = new Vector2(this.boundary.getMinX() + h / 2 + SAFETY_MARGIN,
							this.boundary.getMinY() + v / 2 + SAFETY_MARGIN);
					break;
				case "top-right":
					origin = new Vector2(this.boundary.getMaxX() - h / 2 - SAFETY_MARGIN,
							this.boundary.getMinY() + v / 2 + SAFETY_MARGIN);
					break;
				case "bottom-left":
					origin = new Vector2(this.boundary.getMinX() + h / 2 + SAFETY_MARGIN,
							this.boundary.getMaxY() - v / 2 - SAFETY_MARGIN);
					break;
				case "bottom-right":
					origin = new Vector2(this.boundary.getMaxX() - h / 2 - SAFETY_MARGIN,
							this.boundary.getMaxY() - v / 2 - SAFETY_MARGIN);
					break;
				default:
					origin = new Vector2(this.boundary.getMinX() + h / 2 + SAFETY_MARGIN,
							this.boundary.getMinY() + v / 2 + SAFETY_MARGIN);
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
				this.stream().forEach(unit -> union.add(unit.getSafeArea()));
				return union;
			}

			/**
			 * The bit surface should be limited inside boundary of this polyomino minus a
			 * minimum margin. The margin should run along opposite border of floating
			 * position. If the bit is bigger than the polyomino, the margin's width will be
			 * equal to pattern's parameter. Otherwise, the margin's width would be the
			 * difference between bit's and polyomino's size.
			 * 
			 * @param floatpos
			 *            either "top-left", "top-right", "bottom-left" or "bottom-right"
			 * @param bitOrientation
			 *            (0;1) or (1;0)
			 * @return a rectangular area smaller than boundary
			 */
			private Area getLimArea(String floatpos, Vector2 bitOrientation) {
				Rectangle2D.Double lim = (Double) this.boundary.clone();

				// Determine the float position of bit
				String[] pos = floatpos.split("-");
				// pos[0] would be "top" or "bottom"
				// pos[1] would be "left" or "right"

				double bitHorizontalLength = CraftConfig.bitLength, bitVerticalLength = CraftConfig.bitWidth;
				if (bitOrientation.x == 0 && bitOrientation.y == 1) {// If vertical
					bitHorizontalLength = CraftConfig.bitWidth;
					bitVerticalLength = CraftConfig.bitLength;
				}

				double horizontalMarginAroundBit;
				if (this.boundary.width <= bitHorizontalLength) {
					// We will put in a margin whose width is equal to pattern's parameter
					// "horizontal margin"
					horizontalMarginAroundBit = (double) config.get(HORIZONTAL_MARGIN).getCurrentValue();
				} else {
					// Margin will be difference between boundary's size and bit's
					horizontalMarginAroundBit = this.boundary.width - bitHorizontalLength;
				}
				lim.width -= horizontalMarginAroundBit;
				if (pos[1].equals("right"))
					// Move top-left corner of boundary to right
					lim.x += horizontalMarginAroundBit - SAFETY_MARGIN;
				else
					lim.x += SAFETY_MARGIN;

				double verticalMarginAroundBit;
				if (this.boundary.height <= bitVerticalLength) {
					// We will put in a margin whose width is equal to pattern's parameter
					// "vertical margin"
					verticalMarginAroundBit = (double) config.get(VERTICAL_MARGIN).getCurrentValue();
				} else {
					// Margin will be difference between boundary's size and bit's
					verticalMarginAroundBit = this.boundary.height - bitVerticalLength;
				}
				lim.height -= verticalMarginAroundBit; // equal to bit's vertical length in fact
				if (pos[0].equals("bottom"))
					// Move down top-left corner of boundary
					lim.y += verticalMarginAroundBit - SAFETY_MARGIN;
				else
					lim.y += SAFETY_MARGIN;

				return new Area(lim);
			}

			@Override
			public String toString() {
				StringBuilder str = new StringBuilder();
				str.append(this.id + "[");
				for (UnitSquare u : this) {
					str.append(u);
					str.append(",");
				}
				str.append("]");
				return str.toString();
			}

			/**
			 * General connect
			 * 
			 * @param puzzle
			 * @return
			 * @see {@link #add(UnitSquare)}
			 */
			public boolean add(Puzzle puzzle) {
				if (puzzle instanceof UnitSquare) {
					return this.add((UnitSquare) puzzle);
				} else if (puzzle instanceof Polyomino) {
					return this.addAll((Polyomino) puzzle);
				}
				return false;
			}

			/**
			 * Prohibit the removal
			 * 
			 * @return always <tt>false</tt>
			 */
			@Override
			public boolean remove(Object o) {
				return false;
			}

			@Override
			public Polyomino merge(Puzzle other) {
				Polyomino p = new Polyomino();
				p.addAll(this);
				boolean success = p.add(other);
				if (success) {
					return p;
				} else
					return null;
			}

			@Override
			public boolean isMerged() {
				if (this.isEmpty())
					return false;
				UnitSquare u = this.iterator().next();
				Polyomino p = matrixP[u._i][u._j];
				return p == null ? true : !this.equals(matrixP[u._i][u._j]);
			}

			@Override
			public boolean canMergeWith(Puzzle puzzle) {
				if (puzzle == null) // Cannot merge with null
					return false;
				if (this.isEmpty())
					return true;
				if (this.equals(puzzle)) // Cannot merge with itself
					return false;
				if (!this.contains(puzzle) && this.touch(puzzle) && this.isStillValidIfAdding(puzzle))
					return true;
				else
					return false;
			}

			@Override
			public Point getTopCoor() {
				Point maxP = new Point(matrixU.length, matrixU[0].length);
				for (UnitSquare u : this) {
					Point coor = u.getTopCoor();
					if (maxP.y > coor.y)
						maxP = coor;
					else if (maxP.y == coor.y) {
						if (maxP.x > coor.x)
							maxP = coor;
					}
				}
				return maxP;
			}

			@Override
			public Set<Puzzle> getDuty() {
				return duty.get(this);
			}

			@Override
			public double getIsolatedLevel() {
				return 0;
			}
		}

		/**
		 * All direct links between {@link UnitSquare}s and {@link Polyomino}s
		 * 
		 * @author Quoc Nhat Han TRAN
		 *
		 */
		private class ConnectivityGraph extends HashMap<Puzzle, Set<Puzzle>> {
			/**
			 * 
			 */
			private static final long serialVersionUID = 5057068857746901001L;

			public ConnectivityGraph() {
				LOGGER.finer("Init neighborhood graph");
				for (int i = 0; i < matrixP.length; i++) {
					for (int j = 0; j < matrixP[i].length; j++) {
						if (matrixP[i][j] == null && matrixU[i][j].state != UnitState.IGNORED)
							// This is a non ignored unit
							this.put(matrixU[i][j], neighborsOf(matrixU[i][j]));

						if (matrixP[i][j] != null && !this.containsKey(matrixP[i][j]))
							// This is a Polyomino
							this.put(matrixP[i][j], neighborsOf(matrixP[i][j]));
					}
				}
			}

			/**
			 * Calculate neighbors by searching around each unit square
			 * 
			 * @param unit
			 * @return
			 */
			private Set<Puzzle> neighborsOf(Polyomino polyomino) {
				Set<Puzzle> _neighbors = new HashSet<Puzzle>();
				for (UnitSquare unit : polyomino) {
					_neighbors.addAll(neighborsOf(unit));
				}
				return _neighbors;
			}

			/**
			 * @param unit
			 *            a non {@link UnitState#IGNORED} {@link UnitSquare}
			 * @return a sublist from 4 direct neighbors
			 */
			private Set<Puzzle> neighborsOf(UnitSquare unit) {
				int i = unit._i;
				int j = unit._j;
				Set<Puzzle> _neighbors = new HashSet<Puzzle>(4);
				// Top
				if (i > 0) {
					if (matrixP[i - 1][j] != null && !matrixP[i - 1][j].contains(unit)) {
						_neighbors.add(matrixP[i - 1][j]);
					}

					if (matrixP[i - 1][j] == null && matrixU[i - 1][j].state != UnitState.IGNORED) {
						_neighbors.add(matrixU[i - 1][j]);
					}
				}
				// Bottom
				if (i + 1 < matrixP.length) {
					if (matrixP[i + 1][j] != null && !matrixP[i + 1][j].contains(unit)) {
						_neighbors.add(matrixP[i + 1][j]);
					}

					if (matrixP[i + 1][j] == null && matrixU[i + 1][j].state != UnitState.IGNORED) {
						_neighbors.add(matrixU[i + 1][j]);
					}
				}
				// Left
				if (j > 0) {
					if (matrixP[i][j - 1] != null && !matrixP[i][j - 1].contains(unit)) {
						_neighbors.add(matrixP[i][j - 1]);
					}
					if (matrixP[i][j - 1] == null && matrixU[i][j - 1].state != UnitState.IGNORED) {
						_neighbors.add(matrixU[i][j - 1]);
					}
				}
				// Right
				if (j + 1 < matrixP[0].length) {
					if (matrixP[i][j + 1] != null && !matrixP[i][j + 1].contains(unit)) {
						_neighbors.add(matrixP[i][j + 1]);
					}
					if (matrixP[i][j + 1] == null && matrixU[i][j + 1].state != UnitState.IGNORED) {
						_neighbors.add(matrixU[i][j + 1]);
					}
				}
				return _neighbors;
			}

			/**
			 * Instead of {@link #get(Object)}
			 * 
			 * @param puzzle
			 * @return
			 */
			public Set<Puzzle> of(Puzzle puzzle) {
				return this.get(puzzle);
			}
		}

		/**
		 * Duty to save all {@link UnitState#BORDER BORDER} {@link UnitSquare}s
		 * 
		 * @author Quoc Nhat Han TRAN
		 *
		 */
		private class DutyGraph extends HashMap<Puzzle, Set<Puzzle>> {

			/**
			 * 
			 */
			private static final long serialVersionUID = 4729535080154862879L;

			public DutyGraph() {
				LOGGER.finer("Init duty graph");
				for (int i = 0; i < matrixU.length; i++) {
					for (int j = 0; j < matrixU[i].length; j++) {
						this.put(matrixU[i][j], dutyOf(matrixU[i][j]));
					}
				}
				for (int i = 0; i < matrixP.length; i++) {
					for (int j = 0; j < matrixP[i].length; j++) {
						if (matrixP[i][j] != null && !this.containsKey(matrixP[i][j]))
							this.put(matrixP[i][j], dutyOf(matrixP[i][j]));
					}
				}
			}

			/**
			 * What this puzzle needs to save
			 * 
			 * @param p
			 * @return
			 */
			private Set<Puzzle> of(Puzzle p) {
				return this.get(p);
			}

			/**
			 * Check 8 corners around
			 * 
			 * @param u
			 *            should be {@link UnitState#ACCEPTED ACCEPTED}
			 * @return empty list if not {@link UnitState#ACCEPTED} {@link UnitSquare}
			 */
			private Set<Puzzle> dutyOf(UnitSquare u) {
				Set<Puzzle> s = new HashSet<Puzzle>(8);
				if (u.state != UnitState.ACCEPTED)
					return s;
				int i = u._i;
				int j = u._j;
				// Top
				if (i > 0) {
					if (matrixP[i - 1][j] == null && matrixU[i - 1][j].state == UnitState.BORDER) {
						s.add(matrixU[i - 1][j]);
					}
				}
				// Top-left
				if (i > 0 && j > 0) {
					if (matrixP[i - 1][j - 1] == null && matrixU[i - 1][j - 1].state == UnitState.BORDER) {
						s.add(matrixU[i - 1][j - 1]);
					}
				}
				// Top-right
				if (i > 0 && j < matrixU[i].length) {
					if (matrixP[i - 1][j + 1] == null && matrixU[i - 1][j + 1].state == UnitState.BORDER) {
						s.add(matrixU[i - 1][j + 1]);
					}
				}
				// Bottom
				if (i + 1 < matrixU.length) {
					if (matrixP[i + 1][j] == null && matrixU[i + 1][j].state == UnitState.BORDER) {
						s.add(matrixU[i + 1][j]);
					}
				}
				// Bottom-left
				if (i + 1 < matrixU.length && j > 0) {
					if (matrixP[i + 1][j - 1] == null && matrixU[i + 1][j - 1].state == UnitState.BORDER) {
						s.add(matrixU[i + 1][j - 1]);
					}
				}
				// Bottom-right
				if (i + 1 < matrixU.length && j < matrixU[i].length) {
					if (matrixP[i + 1][j + 1] == null && matrixU[i + 1][j + 1].state == UnitState.BORDER) {
						s.add(matrixU[i + 1][j + 1]);
					}
				}
				// Left
				if (j > 0) {
					if (matrixP[i][j - 1] == null && matrixU[i][j - 1].state == UnitState.BORDER) {
						s.add(matrixU[i][j - 1]);
					}
				}
				// Right
				if (j + 1 < matrixU[i].length) {
					if (matrixP[i][j + 1] == null && matrixU[i][j + 1].state == UnitState.BORDER) {
						s.add(matrixU[i][j + 1]);
					}
				}
				return s;
			}

			/**
			 * Combine duties of all components
			 * 
			 * @param p
			 * @return
			 */
			private Set<Puzzle> dutyOf(Polyomino p) {
				Set<Puzzle> s = new HashSet<Puzzle>();
				for (UnitSquare u : p) {
					s.addAll((Set<Puzzle>) this.get(u));
				}
				// Remove all units absorbed into p
				s.removeAll(p);
				return s;
			}
		}

		/**
		 * A transformation of current state of matrix
		 * 
		 * @author Quoc Nhat Han TRAN
		 *
		 */
		private class Action {
			/**
			 * Initiator of this action
			 */
			private Puzzle trigger;

			/**
			 * What we chose to merge with trigger
			 */
			private Puzzle target;

			/**
			 * What we did right before this
			 */
			private Action parent;

			/**
			 * What we did in following this
			 */
			private List<Action> children;

			/**
			 * Fusion of {@link #target}
			 */
			private Puzzle result;

			/**
			 * @param parent
			 * @param trigger
			 * @param target
			 * @throws TooDeepSearchException
			 *             if we had done more than demanded
			 * @throws IllegalArgumentException
			 *             if this action has been done before (with <tt>target</tt> as
			 *             trigger and <tt>trigger</tt> as target)
			 */
			public Action(Action parent, Puzzle trigger, Puzzle target)
					throws DuplicateActionException, TooDeepSearchException {
				// If this is not the root
				if (parent != null) {
					if (parent.getChildren().parallelStream().anyMatch(c -> c.hasTriedToMerge(trigger, target)))
						throw new DuplicateActionException(trigger, target);
					else
						// Only append to parent if this has not been done
						parent.getChildren().add(this);
				}
				countAction++;
				if (countAction > limitActions)
					// Stop the search
					throw new TooDeepSearchException();
				this.parent = parent;
				this.trigger = trigger;
				this.target = target;
				this.children = new ArrayList<Action>();
				this.result = null;
			}

			/**
			 * @return <tt>true</tt> if no parent, no trigger, no target
			 */
			public boolean isRoot() {
				return parent == null && trigger == null && target == null;
			}

			/**
			 * @param p1
			 * @param p2
			 * @return <tt>true</tt> if {p1, p2} is equal to {{@link #trigger},
			 *         {@link #target}}
			 */
			public boolean hasTriedToMerge(Puzzle p1, Puzzle p2) {
				return (this.trigger.equals(p1) && this.target.equals(p2))
						|| (this.trigger.equals(p2) && this.target.equals(p2));
			}

			/**
			 * Apply this action to the current state of {@link UnitMatrix#matrixP}.
			 * Register the result into {@link UnitMatrix#matrixP}
			 * 
			 * @see UnitMatrix#registerPuzzle(Puzzle)
			 */
			public void realize() {
				result = trigger.merge(target);
				registerPuzzle(result);
			}

			/**
			 * Revert this action. Unregister the merged polyominos. Remove result from
			 * {@link UnitMatrix#candidates} and {@link UnitMatrix#neighbors}
			 */
			public void undo() {
				// Remove result from candidates and neighbors
				unregisterCandidate(result);
				// Remove the fusion in tracking matrix
				// Re-register the old puzzles
				registerPuzzle(trigger);
				registerPuzzle(target);
			}

			/**
			 * @return what we obtained after merging
			 */
			public Puzzle getResult() {
				return result;
			}

			/**
			 * @return what we did before
			 */
			public Action getParent() {
				return parent;
			}

			/**
			 * @return who called this
			 */
			public Puzzle getTrigger() {
				return trigger;
			}

			/**
			 * @return whom we merged with
			 */
			public Puzzle getTarget() {
				return target;
			}

			/**
			 * @return what we did after
			 */
			public List<Action> getChildren() {
				return children;
			}

			/**
			 * After realizing an {@link Action}, we search what we do next
			 * 
			 * @return <tt>null</tt> if nothing
			 * @throws TooDeepSearchException
			 */
			public Action nextChild() throws TooDeepSearchException {
				if (this.children.isEmpty()) {
					for (Puzzle puzzleTrigger : candidates) {
						// Skip merged puzzle
						if (puzzleTrigger.isMerged())
							continue;
						// Check its possibilities
						for (Puzzle puzzleTarget : possibilites.get(puzzleTrigger)) {
							// If at least a target Puzzle has not been merged
							// Each puzzle in possibilities list has been confirmed mergeable with trigger
							if (!puzzleTarget.isMerged())
								try {
									return new Action(this, puzzleTrigger, puzzleTarget);
								} catch (DuplicateActionException e) {
									LOGGER.finest(e.getMessage());
									return null;
								}
						}
					}
					return null;
				} else
					return children.get(children.size() - 1).nextSibling();
			}

			/**
			 * @return what we should do after undoing an {@link Action}. <tt>null</tt> if
			 *         nothing
			 * @throws TooDeepSearchException
			 */
			public Action nextSibling() throws TooDeepSearchException {
				int resumePointOfTrigger = candidates.indexOf(trigger);
				List<Puzzle> oldPossibilities = possibilites.get(trigger);
				int resumtPointOfTarget = oldPossibilities.indexOf(target);
				// Resume on old list of possibilities of old trigger
				for (int j = resumtPointOfTarget + 1; j < oldPossibilities.size(); j++) {
					Puzzle newTarget = oldPossibilities.get(j);
					if (!newTarget.isMerged()) {
						try {
							return new Action(parent, trigger, newTarget);
						} catch (DuplicateActionException e) {
							LOGGER.finest(e.getMessage());
							// The action has been tried
							// We search for others
							continue;
						}
					}
				}
				// Search on new trigger
				for (int i = resumePointOfTrigger + 1; i < candidates.size(); i++) {
					Puzzle newTrigger = candidates.get(i);
					if (newTrigger.isMerged())
						continue; // skip merged candidates
					List<Puzzle> listPossibilities = possibilites.get(newTrigger);
					for (int j = 0; j < listPossibilities.size(); j++) {
						// Check if merged
						Puzzle newTarget = listPossibilities.get(j);
						if (!newTarget.isMerged()) {
							try {
								return new Action(parent, newTrigger, newTarget);
							} catch (DuplicateActionException e) {
								LOGGER.finest(e.getMessage());
								// This action has been tried
								// We search for others
								continue;
							}
						}
					}
				}
				return null;
			}

			/**
			 * Two {@link Action} are equal if their targets and triggers are asymetrically
			 * identical
			 */
			@Override
			public boolean equals(Object arg0) {
				if (!(arg0 instanceof Action))
					return false;
				Action a = (Action) arg0;
				return ((this.trigger.equals(a.getTrigger()) && this.target.equals(a.getTarget()))
						|| (this.trigger.equals(a.getTarget()) && this.target.equals(a.getTrigger())));
			}

			@Override
			public String toString() {
				if (this.parent == null)
					return "{root}";
				else
					return "{" + trigger.toString() + "+" + target.toString() + "}";
			}
		}

		/**
		 * Indicate an action has been realized previously
		 * 
		 * @author Quoc Nhat Han TRAN
		 *
		 */
		public class DuplicateActionException extends Exception {

			/**
			 * 
			 */
			private static final long serialVersionUID = 5125661697708952752L;

			public DuplicateActionException(Puzzle p1, Puzzle p2) {
				super("Has tried {" + p1 + "+" + p2 + "}");
			}
		}

		/**
		 * Indicate that the number actions we realized has been too much
		 * 
		 * @author Quoc Nhat Han TRAN
		 *
		 */
		public class TooDeepSearchException extends Exception {

			/**
			 * 
			 */
			private static final long serialVersionUID = -3388495688242904101L;

			public TooDeepSearchException() {
				super(limitActions + " actions have been realized but no solution is found");
			}
		}
	}
}