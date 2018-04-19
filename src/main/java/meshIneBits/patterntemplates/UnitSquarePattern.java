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
import java.util.Set;
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
	 * A bit requires at least <tt>n x m</tt> unit squares to cover itself. This is
	 * maximum length of a
	 * {@link meshIneBits.patterntemplates.UnitSquarePattern.UnitMatrix.Polyomino
	 * Polyomino} if it lies horizontally
	 */
	private double maxPLength;
	/**
	 * A bit requires at least <tt>n x m</tt> unit squares to cover itself. This is
	 * the maximum width of a
	 * {@link meshIneBits.patterntemplates.UnitSquarePattern.UnitMatrix.Polyomino
	 * Polyomino} if it lies horizontally
	 */
	private double maxPWidth;

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
		LOGGER.info("Paving layer " + actualState.getLayerNumber());
		// Calculate size of unit square
		this.calcUnitSizeAndLimits();
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
		return "This pattern defines systematically possible position of Lift Points, basing on which we will create a regular bit.";
	}

	@Override
	public String getHowToUse() {
		return "Unit square's size should be a little bit larger than Lift Points, so that we have safe space between bits.";
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
		 * Mark or unmark a puzzle
		 * 
		 * @param b
		 */
		public void setMerged(boolean b);

		/**
		 * @return 1 if a single unit, or actual size if a polyomino
		 */
		public int size();

		/**
		 * @return Top-most and left-most virtual coordination of current puzzle.
		 *         <tt>(x,y) = (j, i)</tt>
		 */
		public Point getTopCoor();
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
		 * 
		 * @return <tt>true</tt> if a solution found
		 */
		public boolean resolve() {
			LOGGER.fine("Resolve this matrix");
			// this.quickRegroup();
			neighbors = new ConnectivityGraph();
			this.findCandidates();
			this.sortCandidates();
			return this.dfsTry();
		}

		/**
		 * A candidate is a puzzle before {@link #dfsTry()}. This stack should be sorted
		 * in descendant by<br>
		 * <ol>
		 * <li>Largest</li>
		 * <li>Top most</li>
		 * <li>Left most</li>
		 * </ol>
		 * <br>
		 * A candidate has to be either a {@link Polyomino} or {@link UnitState#ACCEPTED
		 * ACCEPTED} {@link UnitSquare}.
		 */
		private ArrayList<Puzzle> candidates;

		/**
		 * Each candidate comes with a list of neighbors. That list should be sorted in
		 * descendant by<br>
		 * <ol>
		 * <li>Largest</li>
		 * <li>Top most</li>
		 * <li>Left most</li>
		 * </ol>
		 */
		private Map<Puzzle, List<Puzzle>> possibilites;

		/**
		 * Graph of links between non {@link UnitState#IGNORED} {@link UnitSquare}s and
		 * {@link Polyomino}s
		 */
		private ConnectivityGraph neighbors;

		/**
		 * For each state of matrix, we check {@link #candidates} from the stop point of
		 * last {@link Action} to find a puzzle we can merge. Then we merge it, push the
		 * new {@link Polyomino} into {@link #candidates}.<br>
		 * 
		 * If we cannot find any more candidate, check: <br>
		 * 1. No more isolated border units --> print out (and stop)<br>
		 * 2. Exist an isolated border units --> failed --> revert and try with other
		 * P_i <br>
		 * 
		 * @return
		 */
		private boolean dfsTry() {
			LOGGER.finer("Depth-first Search for solution");
			// Initiate root of actions
			Action rootAction = null;
			try {
				rootAction = new Action(null, null, null);
			} catch (IllegalArgumentException e) {
				return false;
			}
			Action currentAction = rootAction;
			do {
				Action childAction = currentAction.nextChild();
				if (childAction != null) {
					// If there is a way to continue
					childAction.realize();
					registerCandidate(childAction);
					currentAction = childAction;
					LOGGER.finest("Do " + childAction.toString() + "->" + childAction.getResult() + "\r\n"
							+ "Neighbors of trigger=" + neighbors.of(childAction.getTrigger()) + "\r\n"
							+ "Neighbors of target=" + neighbors.of(childAction.getTarget()) + "\r\n" + "Possibilites="
							+ possibilites.get(childAction.getResult()));
				} else {
					// If nothing we can do further
					if (solutionFound())
						return true;
					else {
						// If there is no more child from root
						if (currentAction.equals(rootAction))
							return false;
						// Else
						// Revert
						currentAction.undo();
						LOGGER.finest("Undo " + currentAction.getResult() + "->" + currentAction);
						// Climb up in tree
						currentAction = currentAction.getParent();
					}
				}
			} while (true);
		}

		/**
		 * Check if the current state of matrixP satisfies
		 * 
		 * @return <tt>true</tt> if each non-{@link UnitState#IGNORED IGNORED}
		 *         {@link UnitSquare} ({@link UnitState#ACCEPTED ACCEPTED} or
		 *         {@link UnitState#BORDER BORDER}) belongs to one {@link Polyomino}
		 */
		private boolean solutionFound() {
			for (int i = 0; i < matrixP.length; i++) {
				for (int j = 0; j < matrixP[i].length; j++) {
					// If a non ignored unit belongs to no polyomino
					if (matrixU[i][j].state != UnitState.IGNORED && matrixP[i][j] == null)
						return false;
				}
			}
			return true;
		}

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
			possibilites = new HashMap<Puzzle, List<Puzzle>>(matrixU.length * matrixU[0].length);
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
		 * Sort the list of candidates in descending order <br>
		 * <ol>
		 * <li>Largest</li>
		 * <li>Top most</li>
		 * <li>Left most</li>
		 * </ol>
		 */
		private void sortCandidates() {
			LOGGER.finer("Sort candidates list in largest > top most > left most");
			Comparator<Puzzle> c = (p1, p2) -> {
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
			};
			candidates.sort(c);
		}

		/**
		 * A quick regrouping {@link UnitSquare} at the border with
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
			puzzle.setMerged(false);
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
		 * figure out its possibilities in a descendant order.<br>
		 * <ol>
		 * <li>Largest</li>
		 * <li>Top most</li>
		 * <li>Left most</li>
		 * </ol>
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
			Comparator<Puzzle> c = (p1, p2) -> {
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
			};
			list.sort(c);
			possibilites.put(puzzle, list);
		}

		/**
		 * Register the result from an {@link Action}. Also determine its new neighbors
		 * and register them to {@link #neighbors}
		 * 
		 * @param fromAction
		 *            what we just did
		 */
		private void registerCandidate(Action fromAction) {
			registerNeighborsOfMergedPolyomino(fromAction);
			registerCandidate(fromAction.getResult());
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
			 * Whether this unit has been merged into a {@link Polyomino}
			 */
			private boolean merged = false;;

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

			@Override
			public Polyomino merge(Puzzle other) {
				Polyomino p = new Polyomino();
				p.add(this);
				boolean success = p.add(other);
				if (success) {
					this.setMerged(true);
					other.setMerged(true);
					return p;
				} else
					return null;
			}

			@Override
			public boolean isMerged() {
				return merged;
			}

			@Override
			public void setMerged(boolean b) {
				this.merged = b;
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

			/**
			 * Whether this is merged into an other {@link Polyomino}
			 */
			private boolean merged = false;

			/**
			 * To not let bit float into the sides
			 */
			private final double SAFETY_MARGIN = 1.0;

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

				Area bitArea = this.getLimitArea(bit, floatpos);
				Area polyominoArea = this.getUnitedArea();
				bitArea.intersect(polyominoArea);
				bit.updateBoundaries(bitArea);

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
							this.boundary.getMaxY() - v / 2 + SAFETY_MARGIN);
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
					lim.x += horizontalMarginAroundBit - SAFETY_MARGIN;
				else
					lim.x += SAFETY_MARGIN;

				double verticalMarginAroundBit;
				if (this.boundary.height <= bitVerticalLength) {
					// We will put in a margin whose width is equal to pattern's parameter
					// "vertical margin"
					verticalMarginAroundBit = (double) UnitSquarePattern.this.config.get(VERTICAL_MARGIN)
							.getCurrentValue();
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
				str.append("[");
				for (UnitSquare u : this) {
					str.append(u);
					str.append(",");
				}
				str.append("]");
				return str.toString();
			}

			/**
			 * Connect 2 polyominos
			 * 
			 * @param thatPolyomino
			 * @return
			 */
			public boolean add(Polyomino thatPolyomino) {
				return super.addAll(thatPolyomino);
			}

			/**
			 * General connect
			 * 
			 * @param puzzle
			 * @return
			 * @see {@link #add(Polyomino)}
			 * @see {@link #add(UnitSquare)}
			 */
			public boolean add(Puzzle puzzle) {
				if (puzzle instanceof UnitSquare) {
					return this.add((UnitSquare) puzzle);
				} else if (puzzle instanceof Polyomino) {
					return this.add((Polyomino) puzzle);
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
				p.add(this);
				boolean success = p.add(other);
				if (success) {
					this.setMerged(true);
					other.setMerged(true);
					return p;
				} else
					return null;
			}

			@Override
			public boolean isMerged() {
				return merged;
			}

			@Override
			public void setMerged(boolean b) {
				this.merged = b;
			}

			/**
			 * @param puzzle
			 * 
			 */
			@Override
			public boolean canMergeWith(Puzzle puzzle) {
				if (this.isEmpty())
					return true;
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
			private ArrayList<Action> children;

			/**
			 * Fusion of {@link #target}
			 */
			private Puzzle result;

			/**
			 * @param parent
			 * @param trigger
			 * @param target
			 * @throws IllegalArgumentException
			 *             if this action has been done before (with <tt>target</tt> as
			 *             trigger and <tt>trigger</tt> as target)
			 */
			public Action(Action parent, Puzzle trigger, Puzzle target) throws IllegalArgumentException {
				this.parent = parent;
				this.trigger = trigger;
				this.target = target;
				this.children = new ArrayList<Action>();
				this.result = null;
				if (parent != null) {
					// If this is not the root
					// Only append to parent if this has not been done
					if (!this.parent.getChildren().contains(this))
						this.parent.getChildren().add(this);
					else
						throw new IllegalArgumentException("This action has been tried before");
				}
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
				// Remove the fusion in tracking matrix
				// Re-register the old puzzles
				registerPuzzle(trigger);
				registerPuzzle(target);
				// Remove result from candidates and neighbors
				unregisterCandidate(result);
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
			public ArrayList<Action> getChildren() {
				return children;
			}

			/**
			 * After realizing an {@link Action}, we search what we do next
			 * 
			 * @return <tt>null</tt> if nothing
			 */
			public Action nextChild() {
				if (this.children.isEmpty()) {
					for (Puzzle puzzleTrigger : candidates) {
						// Skip merged puzzle
						if (puzzleTrigger.isMerged())
							continue;
						// Check its possibilities
						for (Puzzle puzzleTarget : possibilites.get(puzzleTrigger)) {
							// If at least a target Puzzle has not been merged
							if (!puzzleTarget.isMerged() && puzzleTrigger.canMergeWith(puzzleTarget))
								try {
									return new Action(this, puzzleTrigger, puzzleTarget);
								} catch (IllegalArgumentException e) {
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
			 */
			public Action nextSibling() {
				int resumePointOfTrigger = candidates.indexOf(trigger);
				List<Puzzle> oldPossibilities = possibilites.get(trigger);
				int resumtPointOfTarget = oldPossibilities.indexOf(target);
				// Resume on old list of possibilities of old trigger
				for (int j = resumtPointOfTarget + 1; j < oldPossibilities.size(); j++) {
					Puzzle newTarget = oldPossibilities.get(j);
					if (!newTarget.isMerged()) {
						try {
							return new Action(parent, trigger, newTarget);
						} catch (IllegalArgumentException e) {
							// The action has been tried
							// We search for others
						}
					}
				}
				// Search on new trigger
				for (int i = resumePointOfTrigger + 1; i < candidates.size(); i++) {
					Puzzle newTrigger = candidates.get(i);
					List<Puzzle> listPossibilities = possibilites.get(newTrigger);
					for (int j = 0; j < listPossibilities.size(); j++) {
						// Check if merged
						Puzzle newTarget = listPossibilities.get(j);
						if (!newTarget.isMerged()) {
							try {
								return new Action(parent, newTrigger, newTarget);
							} catch (IllegalArgumentException e) {
								// This action has been tried
								// We search for others
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
				return ((this.trigger == a.getTrigger() && this.target == a.getTarget())
						|| (this.trigger == a.getTarget() && this.target == a.getTrigger()));
			}

			@Override
			public String toString() {
				if (this.parent == null)
					return "{root}";
				else
					return "{" + trigger.toString() + "+" + target.toString() + "}";
			}
		}
	}
}