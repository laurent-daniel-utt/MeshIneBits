/*
 * MeshIneBits is a Java software to disintegrate a 3d mesh (model in .stl)
 * into a network of standard parts (called "Bits").
 *
 * Copyright (C) 2016  Thibault Cassard & Nicolas Gouju.
 * Copyright (C) 2017-2018  TRAN Quoc Nhat Han.
 * Copyright (C) 2018 Vallon BENJAMIN.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package meshIneBits.patterntemplates;

import meshIneBits.Bit2D;
import meshIneBits.Mesh;
import meshIneBits.Layer;
import meshIneBits.Pavement;
import meshIneBits.config.CraftConfig;
import meshIneBits.config.patternParameter.BooleanParam;
import meshIneBits.config.patternParameter.DoubleParam;
import meshIneBits.util.AreaTool;
import meshIneBits.util.Logger;
import meshIneBits.util.Vector2;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;
import java.util.*;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
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
 *
 * <p>
 * Then, we construct bits by grouping a number of unit squares. A bit is
 * <em>regular</em> (craftable) if it contains at least an <b>accepted</b> unit
 * square.
 * </p>
 *
 * @author Quoc Nhat Han TRAN
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
     * Maximum length of a {@link meshIneBits.patterntemplates.UnitSquarePattern.UnitMatrix.Polyomino
     * Polyomino} if it lies horizontally
     */
    private double maxPLength;
    /**
     * Maximum width of a {@link meshIneBits.patterntemplates.UnitSquarePattern.UnitMatrix.Polyomino
     * Polyomino} if it lies horizontally
     */
    private double maxPWidth;

    /**
     * To not let bit graze each other
     */
    private final double SAFETY_MARGIN = 0.5;

    private static final String HORIZONTAL_MARGIN = "horizontalMargin";
    private static final String VERTICAL_MARGIN = "verticalMargin";
    private static final String APPLY_QUICK_REGROUP = "applyQuickRegroup";
    private static final String LIMIT_ACTIONS = "limitActions";
    private static final String INCREMENTAL_ROTATION = "incrementalRotation";
    private static final String CUT_DETAILS = "cutDetails";

    private static final java.util.logging.Logger LOGGER = Logger.createSimpleInstanceFor(UnitSquarePattern.class);

    @Override
    public void initiateConfig() {
        config.add(new DoubleParam(HORIZONTAL_MARGIN, "Horizontal margin",
                "A little space allowing Lift Point move horizontally",
                1.0, 100.0, 2.0, 1.0));
        config.add(new DoubleParam(VERTICAL_MARGIN, "Vertical margin",
                "A little space allowing Lift Point move vertically",
                1.0, 100.0, 2.0, 1.0));
        config.add(new BooleanParam(APPLY_QUICK_REGROUP, "Use quick regroup",
                "Allow pattern to regroup some border units before actually resolving",
                true));
        config.add(new DoubleParam(LIMIT_ACTIONS, "Depth of search",
                "Number of actions to take before giving up",
                1.0, 1000000.0, 10000.0, 1.0));
        config.add(new DoubleParam(INCREMENTAL_ROTATION, "Incremental rotation (deg)",
                "Positive offset means to rotate the pavement in clockwise order an angle calculated by the ordinal number of the layer. Negative means otherwise.",
                -180.0, 180.0, 0.0, 1.0));
        config.add(new BooleanParam(CUT_DETAILS, "Cut details",
                "Allow to drop off some minor details",
                true));
    }

    /**
     * This method does nothing.
     *
     * @return <tt>true</tt>
     */
    @Override
    public boolean ready(Mesh mesh) {
        return true;
    }

    /**
     * @param layer target to fill {@link meshIneBits.Bit2D} in
     * @return empty pavement if algorithm fails
     */
    @Override
    public Pavement pave(Layer layer) {
        Logger.updateStatus("Paving layer " + layer.getLayerNumber());
        LOGGER.info("Paving layer " + layer.getLayerNumber());
        // Calculate size of unit square
        this.calcUnitSizeAndLimits();
        // Update the choice on applying quick regroup
        applyQuickRegroup = (boolean) config.get(APPLY_QUICK_REGROUP).getCurrentValue();
        // Limit depth of search
        limitActions = (int) Math.round((double) config.get(LIMIT_ACTIONS).getCurrentValue());
        // Cut off minor details or not
        cutDetails = (boolean) config.get(CUT_DETAILS).getCurrentValue();
        // Get the boundaries
        List<Area> zones = AreaTool.getContinuousSurfacesFrom(layer.getHorizontalSection());
        // Rotation
        double rotation = Math.toRadians(((double) config.get(INCREMENTAL_ROTATION).getCurrentValue()) * layer.getLayerNumber());
        // Matrix of transformation
        AffineTransform atmatrix = AffineTransform.getRotateInstance(-rotation);
        AffineTransform inv = AffineTransform.getRotateInstance(rotation);
        // Sum of pavement
        Vector<Bit2D> overallPavement = new Vector<>();
        for (Area zone : zones) {
            // Transform the zone beforehand
            zone.transform(atmatrix);
            // Generate the corresponding matrix
            UnitMatrix matrix = new UnitMatrix(zone);
            if (matrix.resolve()) {
                LOGGER.info("Solution found for " + zone);
                // Re-transform bits
                Set<Bit2D> biasedBits = matrix.exportBits();
                Set<Bit2D> trueBits = biasedBits.stream().map(bit -> bit.createTransformedBit(inv)).collect(Collectors.toSet());
                overallPavement.addAll(trueBits);
                LOGGER.info(matrix.pToString());
            } else {
                LOGGER.warning("Pavement of layer " + layer.getLayerNumber() + " failed.");
                Logger.updateStatus("Pavement of layer " + layer.getLayerNumber() + " failed.");
                return new Pavement(new Vector<>(), new Vector2(1, 0));
            }
        }
        return new Pavement(overallPavement, new Vector2(1, 0));
    }

    @Override
    public int optimize(Layer actualState) {
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

    @Override
    public Vector2 moveBit(Pavement actualState, Vector2 bitKey, Vector2 localDirection) {
        double distance = 0;
        if (localDirection.x == 0) {// up or down
            distance = CraftConfig.bitWidth / 2;
        } else if (localDirection.y == 0) {// left or right
            distance = CraftConfig.bitLength / 2;
        }
        return this.moveBit(actualState, bitKey, localDirection, distance);
    }

    @Override
    public Vector2 moveBit(Pavement actualState, Vector2 bitKey, Vector2 localDirection, double distance) {
        return actualState.moveBit(bitKey, localDirection, distance);
    }

    @Override
    public String getIconName() {
        return "p5.png";
    }

    @Override
    public String getCommonName() {
        return "Unit Square Pavement";
    }

    @Override
    public String getDescription() {
        return "This pattern transforms the to-be-filled zone into a grid of unit squares, which we will regroup into regular bits later.";
    }

    @Override
    public String getHowToUse() {
        return "Unit square's size should be a little bit larger than Lift Points, so that we have safe space between bits.";
    }

    private boolean applyQuickRegroup = true;

    /**
     * Set the pattern to use quick regroup some border units before actually
     * resolving
     *
     * @param b <tt>true</tt> to set to apply
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

    private boolean cutDetails = true;

    /**
     * Allow to drop off some minor details that we cannot resolve.
     *
     * @param b <tt>true</tt> to stop checking continuous surfaces and to round
     *          up bit area as we export
     */
    public void setCutDetails(boolean b) {
        cutDetails = b;
    }

    /**
     * Describe the relative position of a {@link UnitMatrix.UnitSquare} in
     * respect to predefined area
     *
     * @author Quoc Nhat Han TRAN
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

        UnitState(String codename) {
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
     * @param name "NATURAL" or "DUTY_FIRST". If not found, will set NATURAL by
     *             default
     * @see Strategy
     */
    private void setCandidatesSorter(String name) {
        switch (name.toUpperCase()) {
            case "DUTY_FIRST":
                this.candidateSorter = Strategy.DUTY_FIRST;
                break;
            default:
                this.candidateSorter = Strategy.NATURAL;
                break;
        }
    }

    /**
     * @param name "NATURAL" or "BORDER_FIRST". If not found, will set NATURAL
     *             by default
     * @see Strategy
     */
    private void setPossibilitiesSorter(String name) {
        switch (name.toUpperCase()) {
            case "BORDER_FIRST":
                this.candidateSorter = Strategy.BORDER_FIRST;
                break;
            default:
                this.candidateSorter = Strategy.NATURAL;
                break;
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
                Point p1p = p1.getTopCoordinate();
                Point p2p = p2.getTopCoordinate();
                if (p1p.y < p2p.y)
                    return -1;
                else if (p1p.y > p2p.y)
                    return 1;
                else {
                    // Left most
                    return Integer.compare(p1p.x, p2p.x);
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

        Strategy(String description, Comparator<Puzzle> comparator) {
            this.description = description;
            this.comparator = comparator;
        }

        /**
         * @return comparator under this strategy
         */
        Comparator<Puzzle> _c() {
            return comparator;
        }

        /**
         * @return description
         */
        String _d() {
            return description;
        }
    }

    /**
     * Represents a combination of {@link UnitMatrix.UnitSquare} on matrix
     *
     * @author Quoc Nhat Han TRAN
     */
    private interface Puzzle {

        /**
         * Check if 2 puzzle can be merged. Should check this before {@link
         * #merge(Puzzle)}
         *
         * @param puzzle a {@link UnitMatrix.UnitSquare} or {@link
         *               UnitMatrix.Polyomino}
         */
        boolean canMergeWith(Puzzle puzzle);

        /**
         * Put together two pieces of puzzles
         *
         * @param other target to fuse
         * @return A new {@link UnitMatrix.Polyomino} containing all {@link
         * UnitMatrix.UnitSquare} making up these 2 puzzles. <tt>null</tt> if
         * there is no contact between them
         */
        Puzzle merge(Puzzle other);

        /**
         * Whether this puzzle has been merged with another, basing on their
         * presence on {@link UnitMatrix#matrixP}
         *
         * @return <tt>true</tt> if its corresponding case in {@link
         * UnitMatrix#matrixP} is filled with {@link UnitMatrix.Polyomino}
         */
        boolean isMerged();

        /**
         * @return 1 if a single unit, or actual size if a polyomino
         */
        int size();

        /**
         * @return Top-most and left-most virtual coordination of current
         * puzzle.
         * <tt>(x,y) = (j, i)</tt>
         */
        Point getTopCoordinate();

        /**
         * @return all neighbors (direct or semi-direct) this puzzle needs to
         * save
         */
        Set<Puzzle> getDuty();

        /**
         * @return 3 (+0.1 -> 0.8) for border unit, 0 for accepted unit or
         * polyomino
         */
        double getIsolatedLevel();

        /**
         * @return surface area contained by this piece
         */
        Area getRawArea();
    }

    /**
     * Flat description of multiple {@link UnitSquare}s
     *
     * @author Quoc Nhat Han TRAN
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
         * @param area a level 0 area
         */
        UnitMatrix(Area area) {
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
        Set<Bit2D> exportBits() {
            Set<Polyomino> setPolyominos = this.collectPolyominos();
            Set<Bit2D> setBits = new HashSet<>();
            setPolyominos.stream()
                    .map(Polyomino::getBit2D)
                    .filter(Objects::nonNull)
                    .forEach(setBits::add);
            return setBits;
        }

        /**
         * @return all polyominos filling {@link #matrixP}
         */
        private Set<Polyomino> collectPolyominos() {
            Set<Polyomino> setPolyominos = new HashSet<>();
            for (Polyomino[] matrixPLine : matrixP) {
                for (Polyomino matrixPCase : matrixPLine) {
                    if (matrixPCase != null)
                        setPolyominos.add(matrixPCase);// Not adding if already had
                }
            }
            return setPolyominos;
        }

        /**
         * Concatenate {@link UnitState#BORDER border} units with {@link
         * UnitState#ACCEPTED accepted} one to create polyominos
         *
         * @return <tt>true</tt> if a solution found
         */
        boolean resolve() {
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
            if (!this.dfsTry()) // cannot save all border units
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
         * A candidate is a puzzle to try as trigger of concatenation in {@link
         * #dfsTry()}. It has to be either a {@link UnitMatrix.Polyomino} or
         * {@link UnitState#ACCEPTED ACCEPTED} {@link UnitSquare}.
         */
        private List<Puzzle> candidates;

        /**
         * Each candidate comes with a list of possibilities, which are in fact
         * puzzles that can couple with that candidate
         */
        private Map<Puzzle, List<Puzzle>> possibilities;

        /**
         * Graph of direct contacts between non {@link UnitState#IGNORED} {@link
         * UnitSquare}s and {@link UnitMatrix.Polyomino}s
         */
        private ConnectivityGraph neighbors;

        /**
         * Graph of {@link UnitSquare}s to save of each {@link
         * UnitState#ACCEPTED ACCEPTED} {@link UnitSquare} and {@link
         * Polyomino}
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
         * Try to cover the border by trying in order each candidate with each
         * its possibility
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
            Action childAction;
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
                            + possibilities.get(childAction.getResult()));
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
        // * {@link UnitState#BORDER BORDER}) belongs to one {@link UnitMatrix.Polyomino}
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
         * Given current state of {@link #matrixP} and {@link #matrixU}, we find
         * all candidates and register them
         *
         * @see #candidates
         * @see #registerCandidate(Puzzle)
         */
        private void findCandidates() {
            LOGGER.finer("Find candidates and possibilities for each candidate");
            candidates = new ArrayList<>();
            possibilities = new HashMap<>();
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
         * Sort the list of candidates using {@link Strategy} defined by {@link
         * UnitSquarePattern#candidateSorter}
         */
        private void sortCandidates() {
            LOGGER.finer("Sort candidates by " + candidateSorter.toString() + "(" + candidateSorter._d() + ")");
            candidates.sort(candidateSorter._c());
        }

        /**
         * A quick regrouping {@link UnitSquare}s at the border with {@link
         * UnitState#ACCEPTED accepted} one. Not deterministic
         */
        private void quickRegroup() {
            LOGGER.finer("Quick regrouping border units");
            // Make each border unit propose to an accepted one
            proposersRegistry = new HashMap<>();
            for (int i = 0; i < matrixU.length; i++) {
                for (int j = 0; j < matrixU[i].length; j++) {
                    UnitSquare unit = matrixU[i][j];
                    if (unit.state == UnitState.BORDER) {
                        // Check each direct contact unit
                        // if it is accepted
                        // then propose

                        // Above
                        if (i > 0 && unit.canMergeWith(matrixU[i - 1][j]))
                            this.registerProposal(unit, matrixU[i - 1][j]);
                        // Below
                        if (i + 1 < matrixU.length && unit.canMergeWith(matrixU[i + 1][j]))
                            this.registerProposal(unit, matrixU[i + 1][j]);
                        // Left
                        if (j > 0 && unit.canMergeWith(matrixU[i][j - 1]))
                            this.registerProposal(unit, matrixU[i][j - 1]);
                        // Right
                        if (j + 1 < matrixU[i].length && unit.canMergeWith(matrixU[i][j + 1]))
                            this.registerProposal(unit, matrixU[i][j + 1]);
                    }
                }
            }

            Comparator<UnitSquare> comparingFamousLevel = (u1, u2) ->
                    // Compare between number of followers
                    // in descendant order
                    Integer.compare(proposersRegistry.get(u2).size(), proposersRegistry.get(u1).size());
            List<UnitSquare> targetList = new ArrayList<>(proposersRegistry.keySet());
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
                Polyomino p = new Polyomino(target);
                Set<UnitSquare> rejectedProposers = new HashSet<>();
                for (UnitSquare proposer : proposers) {
                    if (p.canMergeWith(proposer))
                        p = p.merge(proposer);
                    else
                        rejectedProposers.add(proposer);
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
         * @param puzzle if a {@link UnitMatrix.Polyomino}, each corresponding
         *               tile in {@link #matrixP} will be it. If a {@link
         *               UnitSquare}, the corresponding tile will be
         *               <tt>null</tt>
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
         * @param proposer who want to marry
         * @param target   who will be the bride
         */
        private void registerProposal(UnitSquare proposer, UnitSquare target) {
            proposersRegistry
                    .computeIfAbsent(target, k -> new HashSet<>())
                    .add(proposer);
        }

        /**
         * Add a new {@link Puzzle} to list of {@link #candidates} for further
         * try. Also figure out its possibilities and sort them by {@link
         * UnitSquarePattern#possibilitySorter}
         *
         * @param puzzle Its connectivities should be already saved in {@link
         *               #neighbors}
         * @see #candidates
         */
        private void registerCandidate(Puzzle puzzle) {
            // Push it in #candidates
            if (candidates.contains(puzzle))
                return;
            candidates.add(0, puzzle); // Push to the first
            // Calculate its possibilities
            List<Puzzle> list = new ArrayList<>(neighbors.of(puzzle));
            // Remove not border units
            list.removeIf(p -> {
                if (!(p instanceof UnitSquare))
                    return true;
                UnitSquare u = (UnitSquare) p;
                return u.state != UnitState.BORDER;
                // Only retain border unit
            });
            // Remove unmergeable puzzles
            list.removeIf(p -> !p.canMergeWith(puzzle));
            // Sort possibilities
            list.sort(possibilitySorter._c());
            // Register
            possibilities.put(puzzle, list);
        }

        /**
         * Register level of duty of a newly created polymoino
         *
         * @param action a fusion
         */
        private void registerDutyOfMergedPolyomino(Action action) {
            Puzzle result = action.getResult();
            Puzzle trigger = action.getTrigger();
            Puzzle target = action.getTarget();
            Set<Puzzle> dutyOfResult = new HashSet<>();
            // Intersection
            dutyOfResult.addAll(duty.of(trigger));
            dutyOfResult.addAll(duty.of(target));
            // Filter
            dutyOfResult.removeIf(Puzzle::isMerged);
            // Register
            duty.put(result, dutyOfResult);
        }

        /**
         * Find neighbors of {@link Puzzle} resulted by <tt>action</tt> and
         * register them
         *
         * @param action a fusion
         */
        private void registerNeighborsOfMergedPolyomino(Action action) {
            Puzzle result = action.getResult();
            Puzzle trigger = action.getTrigger();
            Puzzle target = action.getTarget();
            Set<Puzzle> neighborsOfResult = new HashSet<>();
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
         * candidates and its possibilities. Also remove from {@link
         * #neighbors}
         *
         * @param puzzle a piece
         */
        private void unregisterCandidate(Puzzle puzzle) {
            candidates.remove(puzzle);
            possibilities.remove(puzzle);
            neighbors.remove(puzzle);
        }

        @Override
        public String toString() {
            StringBuilder str = new StringBuilder();
            str.append("[\r\n");
            for (int i = 0; i < matrixU.length; i++) {
                str.append(i).append("[");
                for (int j = 0; j < matrixU[i].length; j++) {
                    str.append(matrixU[i][j].state);
                    str.append(",");
                }
                str.append("]\r\n");
            }
            str.append("]");
            return str.toString();
        }

        String pToString() {
            Set<Polyomino> setPolyominos = this.collectPolyominos();
            StringBuilder str = new StringBuilder("Representations of polyominos in matrix\n");
            str.append("[\r\n");
            for (int i = 0; i < matrixP.length; i++) {
                str.append(i).append("[");
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
                str.append(p.toString()).append("\r\n");
            }
            return str.toString();
        }

        /**
         * Represents a small square (or rectangle) on surface of {@link Layer}
         *
         * @author Quoc Nhat Han TRAN
         */
        private class UnitSquare extends Rectangle2D.Double implements Puzzle {
            /**
             *
             */
            private static final long serialVersionUID = 4316086827801379838L;

            /**
             * Relative position to the area containing this square
             */
            UnitState state;

            /**
             * A part of zone's area which is contained by this unit
             */
            private Area rawArea;

            /**
             * The matrix' line in which this unit resides
             */
            final int _i;

            /**
             * The matrix' column in which this unit resides
             */
            final int _j;

            /**
             * Create a unit square staying inside <tt>zone</tt> with top-left
             * corner at
             * <tt>(x, y)</tt>
             *
             * @param x    coordinate in double
             * @param y    coordinate in double
             * @param area target to pave
             * @param i    virtual abscissa. Non-negative, otherwise 0
             * @param j    virtual coordinate. Non-negative, otherwise 0
             */
            UnitSquare(double x, double y, Area area, int i, int j) {
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
             * @param area the place in which this unit stays
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
             * Given a zone in which we place this unit, calculate how much
             * space it holds. This method should be called at least once before
             * converting {@link UnitMatrix.Polyomino} into {@link Bit2D}
             *
             * @param zone a part of slice's area
             */
            private void calculateContainedArea(Area zone) {
                this.rawArea = new Area(this);
                this.rawArea.intersect(zone);
            }

            /**
             * Check if this touches other unit
             *
             * @param that relating directly to the same area of this unit
             * @return <tt>true</tt> if they sit next to each other and their
             * united area is continuous
             */
            boolean touch(UnitSquare that) {
                // Sit next to each other
                if (Math.abs(this._i - that._i) + Math.abs(this._j - that._j) != 1)
                    return false;
                if (!cutDetails)
                    return checkContinuousSurface(that);
                else return true;
            }

            /**
             * @param that next to this
             * @return <tt>true</tt> if union surface is in one piece
             */
            boolean checkContinuousSurface(UnitSquare that) {
                Area union = new Area(this.getRawArea());
                union.add(that.getRawArea());
                return AreaTool.getContinuousSurfacesFrom(union).size() == 1;
            }

            @Override
            public String toString() {
                return state + "(" + _i + "," + _j + ")";
            }

            @Override
            public Polyomino merge(Puzzle other) {
                Polyomino p = new Polyomino(other);
                p = p.merge(this);
                return p;
            }

            @Override
            public boolean isMerged() {
                return matrixP[_i][_j] != null;
            }

            /**
             * Two {@link UnitSquare} are equal if their virtual coordinates are
             * identical
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
                if (puzzle == null) return false;
                if (puzzle instanceof UnitSquare) {
                    UnitSquare u = (UnitSquare) puzzle;
                    return (this.state == UnitState.ACCEPTED || u.state == UnitState.ACCEPTED) && this.touch(u);
                } else if (puzzle instanceof Polyomino)
                    return puzzle.canMergeWith(this);
                else
                    return false;
            }

            @Override
            public int size() {
                return 1;
            }

            @Override
            public Point getTopCoordinate() {
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

            @Override
            public Area getRawArea() {
                return rawArea;
            }

            /**
             * @return the number of non {@link UnitState#ACCEPTED ACCEPTED}
             * {@link UnitSquare}s around, divided by 10
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
                return (double) count / 10;
            }

            /**
             * Only use this method after resolving completely
             *
             * @return this area minus a safe space. Empty if {@link #state} is
             * {@link UnitState#IGNORED IGNORED}
             */
            Area getSafeArea() {
                if (state == UnitState.IGNORED)
                    return new Area();
                Rectangle2D.Double r = (Double) super.clone();
                // Reduce border
                // Top
                if (_i > 0
                        && matrixP[_i - 1][_j] != null
                        && matrixP[_i - 1][_j] != matrixP[_i][_j]) {
                    r.y += SAFETY_MARGIN;
                    r.height -= SAFETY_MARGIN;
                }
                // Bottom
                if (_i + 1 < matrixP.length
                        && matrixP[_i + 1][_j] != null
                        && matrixP[_i + 1][_j] != matrixP[_i][_j]) {
                    r.height -= SAFETY_MARGIN;
                }
                // Left
                if (_j > 0
                        && matrixP[_i][_j - 1] != null
                        && matrixP[_i][_j - 1] != matrixP[_i][_j]) {
                    r.x += SAFETY_MARGIN;
                    r.width -= SAFETY_MARGIN;
                }
                // Right
                if (_j + 1 < matrixP[_i].length
                        && matrixP[_i][_j + 1] != null
                        && matrixP[_i][_j + 1] != matrixP[_i][_j]) {
                    r.width -= SAFETY_MARGIN;
                }
                // Intersect area
                Area a = (Area) rawArea.clone();
                a.intersect(new Area(r));
                // Whittle corners
                // Reset r
                r = new Rectangle2D.Double();
                // Top-right
                if (_i > 0 && _j + 1 < matrixP[_i].length
                        && matrixP[_i - 1][_j + 1] != null
                        && matrixP[_i - 1][_j + 1] != matrixP[_i][_j]) {
                    r.setRect(this.getMaxX() - SAFETY_MARGIN, this.getMinY(),
                            SAFETY_MARGIN, SAFETY_MARGIN);
                    a.subtract(new Area(r));
                }
                // Top-left
                if (_i > 0 && _j > 0
                        && matrixP[_i - 1][_j - 1] != null
                        && matrixP[_i - 1][_j - 1] != matrixP[_i][_j]) {
                    r.setRect(this.getMinX(), this.getMinY(),
                            SAFETY_MARGIN, SAFETY_MARGIN);
                    a.subtract(new Area(r));
                }
                // Bottom-left
                if (_i + 1 < matrixP.length && _j > 0
                        && matrixP[_i + 1][_j - 1] != null
                        && matrixP[_i + 1][_j - 1] != matrixP[_i][_j]) {
                    r.setRect(this.getMinX(), this.getMaxY() - SAFETY_MARGIN,
                            SAFETY_MARGIN, SAFETY_MARGIN);
                    a.subtract(new Area(r));
                }
                // Bottom-right
                if (_i + 1 < matrixP.length && _j + 1 < matrixP[_i].length
                        && matrixP[_i + 1][_j + 1] != null
                        && matrixP[_i + 1][_j + 1] != matrixP[_i][_j]) {
                    r.setRect(this.getMaxX() - SAFETY_MARGIN, this.getMaxY() - SAFETY_MARGIN,
                            SAFETY_MARGIN, SAFETY_MARGIN);
                    a.subtract(new Area(r));
                }

                return a;
            }
        }

        /**
         * A sequence of adjacent {@link UnitSquare}s. A polyomino could not be
         * too larger than a {@link Bit2D}. It contains at least one {@link
         * UnitState#ACCEPTED ACCEPTED} {@link UnitSquare}
         *
         * @author Quoc Nhat Han TRAN
         */
        private class Polyomino extends HashSet<UnitSquare> implements Puzzle {
            /**
             *
             */
            private static final long serialVersionUID = 1974861227965075981L;

            private Area rawArea = new Area();

            final int id;

            Polyomino() {
                super();
                this.id = countPolyomino++;
            }

            Polyomino(Puzzle puzzle) {
                this();
                if (puzzle instanceof UnitSquare)
                    add((UnitSquare) puzzle);
                else if (puzzle instanceof Polyomino)
                    addAll((Polyomino) puzzle);
            }

            /**
             * Always check {@link #canMergeWith(Puzzle)} before hand
             *
             * @param thatUnit a unit not {@link UnitState#IGNORED ignored}
             * @return <tt>false</tt> if
             * <ul>
             * <li>already having that unit</li>
             * <li>resulting an irregular bit</li>
             * <li>or that unit does not touch anyone</li>
             * <li>or concatenation violates the size of a bit (exceeding
             * height or length)</li>
             * </ul>
             */
            @Override
            public boolean add(UnitSquare thatUnit) {
                super.add(thatUnit);
//				this.updateBoundaryAfterAdding(thatUnit);
                this.updateRawAreaAfterAdding(thatUnit);
                return true;
            }

            /**
             * Concatenate thatUnit's raw area into this
             *
             * @param thatUnit a piece of {@link #matrixU}
             */
            private void updateRawAreaAfterAdding(UnitSquare thatUnit) {
                this.rawArea.add(thatUnit.getRawArea());
            }
//
//			/**
//			 * Outer border of this polyomino
//			 */
//			private Rectangle2D.Double boundary = new Rectangle2D.Double();
//
//			/**
//			 * @return boundary in {@link Double} precision
//			 */
//			Rectangle2D.Double getBoundary() {
//				return boundary;
//			}
//
//			/**
//			 * Update the coordinate of boundary. Should be only called after
//			 * adding, if else, it does nothing.
//			 *
//			 * @param thatUnit unit to be concatenated
//			 */
//			private void updateBoundaryAfterAdding(UnitSquare thatUnit) {
//				if (this.size() == 1)
//					// If this is the first unit
//					// The boundary of this polyomino should be that of the unit
//					this.boundary = thatUnit;
//				else
//					// If already having other unit
//					// We find their union
//					this.boundary = (Double) this.boundary.createUnion(thatUnit);
//			}

            /**
             * Verify connectivity
             *
             * @param that an other piece
             * @return <tt>true</tt> if at least one internal unit is directly
             * adjacent to that
             */
            boolean touch(Puzzle that) {
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
             * @param that an other piece
             * @return <tt>false</tt> if concatenation violates the size of a
             * bit (exceeding height or length)
             */
            private boolean isStillValidIfAdding(Puzzle that) {
                return checkUnionWith(that.getRawArea());
            }

            /**
             * Check if the union is still valid for producing a regular {@link
             * Bit2D}
             *
             * @param newArea raw area of {@link Polyomino} or {@link
             *                UnitSquare}
             * @return <tt>true</tt> if fusion does not break limits
             */
            private boolean checkUnionWith(Area newArea) {
                Area union = (Area) this.getRawArea().clone();
                union.add(newArea);
                if (!cutDetails)
                    if (AreaTool.getContinuousSurfacesFrom(union).size() != 1)
                        return false;
                Vector2 orientation = this.getBitOrientation();
                if (orientation.x == 1 && orientation.y == 0) {
                    // Horizontal
                    return !(union.getBounds2D().getWidth() > maxPLength)
                            && !(union.getBounds2D().getHeight() > maxPWidth);
                } else {
                    // Vertical
                    return !(union.getBounds2D().getWidth() > maxPWidth)
                            && !(union.getBounds2D().getHeight() > maxPLength);
                }
            }

            /**
             * Create a bit from surface of this polyomino. By default, the
             * generated bit will float in the top-left corner. But if an {@link
             * UnitState#ACCEPTED accepted} unit lying on either side of
             * boundary, the generated bit will float toward that side.
             *
             * @return a regular bit if this polyomino has at least one {@link
             * UnitState#ACCEPTED accepted} unit. <tt>null</tt> if bit contains
             * no valid surface
             */
            Bit2D getBit2D() {
                if (this.isEmpty())
                    return null;

                Vector2 bitOrientation = this.getBitOrientation();
                String floatingPosition = this.getBitFloatingPosition();
                Vector2 bitOrigin = this.getBitOrigin(bitOrientation, floatingPosition);
                Bit2D bit = new Bit2D(bitOrigin, bitOrientation);

                Area bitLimArea = this.getLimArea(floatingPosition, bitOrientation);
                Area polyominoArea = this.getUnitedArea();
                bitLimArea.intersect(polyominoArea);
                if (cutDetails) {
                    bitLimArea = this.roundUp(bitLimArea);
                    if (bitLimArea == null || bitLimArea.isEmpty())
                        return null;
                }
                bit.updateBoundaries(bitLimArea);

                return bit;
            }

            Comparator<Area> boundary = ((Area a1, Area a2) -> {
                Rectangle2D.Double b1 = (Double) a1.getBounds2D(),
                        b2 = (Double) a2.getBounds2D();
                double s = b1.width * b1.height - b2.width * b2.height;
                if (s > 0) return 1;
                else if (s == 0) return 0;
                else return -1;
            });

            /**
             * Remove tiny areas to assure each bit contain only one area
             *
             * @param bitArea after all other calculations
             * @return <tt>null</tt> if not containing a valid separated area
             */

            Area roundUp(Area bitArea) {
                return AreaTool.getContinuousSurfacesFrom(bitArea).stream()
                        .filter(Objects::nonNull)
                        .filter(area
                                -> AreaTool.getLiftPoint(area, CraftConfig.suckerDiameter / 2) != null)
                        .max(boundary)
                        .orElse(null);
            }

            /**
             * Check if the bit should lie horizontally or vertically
             *
             * @return <tt>(1, 0)</tt> if horizontal, otherwise <tt>(0, 1)</tt>
             */
            private Vector2 getBitOrientation() {
                if (rawArea.getBounds2D().getWidth() >= rawArea.getBounds2D().getHeight()) {
                    return new Vector2(1, 0); // Horizontal
                } else {
                    return new Vector2(0, 1); // Vertical
                }
            }

            /**
             * Which corner / side of boundary the bit should float to
             *
             * @return either "top-left", "top-right", "bottom-left" or
             * "bottom-right"
             */
            private String getBitFloatingPosition() {
                // Direction to float
                // Top-left / Top-right / Bottom-left / Bottom-right
                boolean top = false, left = false, bottom = false, right = false;

                Rectangle2D.Double boundary = (Double) rawArea.getBounds2D();
                for (UnitSquare unit : this) {
                    if (unit.state == UnitState.ACCEPTED) {
                        if (unit.getMinY() == boundary.getMinY()) {
                            // On top side
                            top = true;
                        } else if (unit.getMaxY() == boundary.getMaxY()) {
                            // On bottom side
                            bottom = true;
                        }

                        if (unit.getMinX() == boundary.getMinX()) {
                            // On left side
                            left = true;
                        } else if (unit.getMaxX() == boundary.getMaxX()) {
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
             * @param orientation horizontal (1, 0) or vertical (0, 1)
             * @param floatpos    either "top-left", "top-right", "bottom-left"
             *                    or "bottom-right"
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

                Vector2 origin;
                Rectangle2D.Double boundary = (Double) rawArea.getBounds2D();
                switch (floatpos) {
                    case "top-left":
                        origin = new Vector2(boundary.getMinX() + h / 2 + SAFETY_MARGIN,
                                boundary.getMinY() + v / 2 + SAFETY_MARGIN);
                        break;
                    case "top-right":
                        origin = new Vector2(boundary.getMaxX() - h / 2 - SAFETY_MARGIN,
                                boundary.getMinY() + v / 2 + SAFETY_MARGIN);
                        break;
                    case "bottom-left":
                        origin = new Vector2(boundary.getMinX() + h / 2 + SAFETY_MARGIN,
                                boundary.getMaxY() - v / 2 - SAFETY_MARGIN);
                        break;
                    case "bottom-right":
                        origin = new Vector2(boundary.getMaxX() - h / 2 - SAFETY_MARGIN,
                                boundary.getMaxY() - v / 2 - SAFETY_MARGIN);
                        break;
                    default:
                        origin = new Vector2(boundary.getMinX() + h / 2 + SAFETY_MARGIN,
                                boundary.getMinY() + v / 2 + SAFETY_MARGIN);
                        break;
                }

                return origin;
            }

            /**
             * Union of all units' area (each unit's area is a part of the whole
             * layer's)
             *
             * @return empty area if this polyomino is empty
             */
            Area getUnitedArea() {
                Area union = new Area();
                this.forEach(unit -> union.add(unit.getSafeArea()));
                return union;
            }

            /**
             * The bit surface should be limited inside boundary of this
             * polyomino minus a minimum margin. The margin should run along
             * opposite border of floating position. If the bit is bigger than
             * the polyomino, the margin's width will be equal to pattern's
             * parameter. Otherwise, the margin's width would be the difference
             * between bit's and polyomino's size.
             *
             * @param floatingPosition either "top-left", "top-right",
             *                         "bottom-left" or "bottom-right"
             * @param bitOrientation   (0;1) or (1;0)
             * @return a rectangular area smaller than boundary
             */
            private Area getLimArea(String floatingPosition, Vector2 bitOrientation) {
                Rectangle2D.Double boundary = (Double) rawArea.getBounds2D();
                Rectangle2D.Double lim = (Double) boundary.clone();

                // Determine the float position of bit
                String[] pos = floatingPosition.split("-");
                // pos[0] would be "top" or "bottom"
                // pos[1] would be "left" or "right"

                double bitHorizontalLength = CraftConfig.bitLength,
                        bitVerticalLength = CraftConfig.bitWidth;
                if (bitOrientation.x == 0 && bitOrientation.y == 1) {// If vertical
                    bitHorizontalLength = CraftConfig.bitWidth;
                    bitVerticalLength = CraftConfig.bitLength;
                }

                double horizontalMarginAroundBit;
                if (boundary.width <= bitHorizontalLength) {
                    // We will put in a margin whose width is equal to pattern's parameter
                    // "horizontal margin"
                    horizontalMarginAroundBit = (double) config.get(HORIZONTAL_MARGIN).getCurrentValue();
                } else {
                    // Margin will be difference between boundary's size and bit's
                    horizontalMarginAroundBit = boundary.width - bitHorizontalLength;
                }
                lim.width -= horizontalMarginAroundBit;
                if (pos[1].equals("right"))
                    // Move top-left corner of boundary to right
                    lim.x += horizontalMarginAroundBit - SAFETY_MARGIN;
                else
                    lim.x += SAFETY_MARGIN;

                double verticalMarginAroundBit;
                if (boundary.height <= bitVerticalLength) {
                    // We will put in a margin whose width is equal to pattern's parameter
                    // "vertical margin"
                    verticalMarginAroundBit = (double) config.get(VERTICAL_MARGIN).getCurrentValue();
                } else {
                    // Margin will be difference between boundary's size and bit's
                    verticalMarginAroundBit = boundary.height - bitVerticalLength;
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
                str.append(this.id).append("[");
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
             * @param puzzle an other piece
             * @see #add(UnitSquare)
             */
            private void add(Puzzle puzzle) {
                if (puzzle instanceof UnitSquare) {
                    this.add((UnitSquare) puzzle);
                } else if (puzzle instanceof Polyomino) {
                    this.addAll((Polyomino) puzzle);
                }
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
                Polyomino p = new Polyomino(this);
                p.add(other);
                return p;
            }

            @Override
            public boolean isMerged() {
                if (this.isEmpty())
                    return false;
                UnitSquare u = this.iterator().next();
                Polyomino p = matrixP[u._i][u._j];
                return p == null || !this.equals(matrixP[u._i][u._j]);
            }

            @Override
            public boolean canMergeWith(Puzzle puzzle) {
                if (puzzle == null) // Cannot merge with null
                    return false;
                if (this.isEmpty())
                    return true;
                if (this.equals(puzzle)) // Cannot merge with itself
                    return false;
                return !this.contains(puzzle) && this.touch(puzzle) && this.isStillValidIfAdding(puzzle);
            }

            @Override
            public Point getTopCoordinate() {
                Point maxP = new Point(matrixU.length, matrixU[0].length);
                for (UnitSquare u : this) {
                    Point coordinate = u.getTopCoordinate();
                    if (maxP.y > coordinate.y)
                        maxP = coordinate;
                    else if (maxP.y == coordinate.y) {
                        if (maxP.x > coordinate.x)
                            maxP = coordinate;
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

            @Override
            public Area getRawArea() {
                return rawArea;
            }
        }

        /**
         * All direct links between {@link UnitSquare}s and {@link
         * UnitMatrix.Polyomino}s
         *
         * @author Quoc Nhat Han TRAN
         */
        private class ConnectivityGraph extends HashMap<Puzzle, Set<Puzzle>> {
            /**
             *
             */
            private static final long serialVersionUID = 5057068857746901001L;

            ConnectivityGraph() {
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
             * @param polyomino other piece
             * @return sum of neighborhood of each its unit
             */
            private Set<Puzzle> neighborsOf(Polyomino polyomino) {
                Set<Puzzle> _neighbors = new HashSet<>();
                for (UnitSquare unit : polyomino) {
                    _neighbors.addAll(neighborsOf(unit));
                }
                _neighbors.removeAll(polyomino);
                return _neighbors;
            }

            /**
             * @param unit a non {@link UnitState#IGNORED} {@link UnitSquare}
             * @return a sublist from 4 direct neighbors
             */
            private Set<Puzzle> neighborsOf(UnitSquare unit) {
                int i = unit._i;
                int j = unit._j;
                Set<Puzzle> _neighbors = new HashSet<>(4);
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
             * @param puzzle a piece
             * @return neighborhood
             */
            Set<Puzzle> of(Puzzle puzzle) {
                return this.get(puzzle);
            }
        }

        /**
         * Duty to save all {@link UnitState#BORDER BORDER} {@link UnitSquare}s
         *
         * @author Quoc Nhat Han TRAN
         */
        private class DutyGraph extends HashMap<Puzzle, Set<Puzzle>> {

            /**
             *
             */
            private static final long serialVersionUID = 4729535080154862879L;

            DutyGraph() {
                LOGGER.finer("Init duty graph");
                for (UnitSquare[] matrixULine : matrixU) {
                    for (UnitSquare matrixUCase : matrixULine) {
                        this.put(matrixUCase, dutyOf(matrixUCase));
                    }
                }
                for (Polyomino[] matrixPLine : matrixP) {
                    for (Polyomino matrixPCase : matrixPLine) {
                        if (matrixPCase != null && !this.containsKey(matrixPCase))
                            this.put(matrixPCase, dutyOf(matrixPCase));
                    }
                }
            }

            /**
             * What this puzzle needs to save
             *
             * @param p a piece
             * @return whom to save
             */
            private Set<Puzzle> of(Puzzle p) {
                return this.get(p);
            }

            /**
             * Check 8 corners around
             *
             * @param u should be {@link UnitState#ACCEPTED ACCEPTED}
             * @return empty list if not {@link UnitState#ACCEPTED} {@link
             * UnitSquare}
             */
            private Set<Puzzle> dutyOf(UnitSquare u) {
                Set<Puzzle> s = new HashSet<>(8);
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
             * @param p a piece
             * @return whom to save
             */
            private Set<Puzzle> dutyOf(Polyomino p) {
                Set<Puzzle> s = new HashSet<>();
                for (UnitSquare u : p) {
                    s.addAll(this.get(u));
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
             * @param parent  previous action
             * @param trigger a piece
             * @param target  a piece
             * @throws TooDeepSearchException   if we had done more than
             *                                  demanded
             * @throws IllegalArgumentException if this action has been done
             *                                  before (with <tt>target</tt> as
             *                                  trigger and <tt>trigger</tt> as
             *                                  target)
             */
            Action(Action parent, Puzzle trigger, Puzzle target)
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
                this.children = new ArrayList<>();
                this.result = null;
            }

            /**
             * @return <tt>true</tt> if no parent, no trigger, no target
             */
            boolean isRoot() {
                return parent == null && trigger == null && target == null;
            }

            /**
             * @param p1 a piece
             * @param p2 a piece
             * @return <tt>true</tt> if {p1, p2} is equal to {{@link #trigger},
             * {@link #target}}
             */
            boolean hasTriedToMerge(Puzzle p1, Puzzle p2) {
                return (this.trigger.equals(p1) && this.target.equals(p2))
                        || (this.trigger.equals(p2) && this.target.equals(p2));
            }

            /**
             * Apply this action to the current state of {@link
             * UnitMatrix#matrixP}. Register the result into {@link
             * UnitMatrix#matrixP}
             *
             * @see UnitMatrix#registerPuzzle(Puzzle)
             */
            void realize() {
                result = trigger.merge(target);
                registerPuzzle(result);
            }

            /**
             * Revert this action. Unregister the merged polyominos. Remove
             * result from {@link UnitMatrix#candidates} and {@link
             * UnitMatrix#neighbors}
             */
            void undo() {
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
            Puzzle getResult() {
                return result;
            }

            /**
             * @return what we did before
             */
            Action getParent() {
                return parent;
            }

            /**
             * @return who called this
             */
            Puzzle getTrigger() {
                return trigger;
            }

            /**
             * @return whom we merged with
             */
            Puzzle getTarget() {
                return target;
            }

            /**
             * @return what we did after
             */
            List<Action> getChildren() {
                return children;
            }

            /**
             * After realizing an {@link Action}, we search what we do next
             *
             * @return <tt>null</tt> if nothing
             * @throws TooDeepSearchException when breaking the {@link
             *                                #limitActions}
             */
            Action nextChild() throws TooDeepSearchException {
                if (this.children.isEmpty()) {
                    for (Puzzle puzzleTrigger : candidates) {
                        // Skip merged puzzle
                        if (puzzleTrigger.isMerged())
                            continue;
                        // Check its possibilities
                        for (Puzzle puzzleTarget : possibilities.get(puzzleTrigger)) {
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
             * @return what we should do after undoing an {@link Action}.
             * <tt>null</tt> if
             * nothing
             * @throws TooDeepSearchException when breaking {@link #limitActions}
             */
            Action nextSibling() throws TooDeepSearchException {
                int resumePointOfTrigger = candidates.indexOf(trigger);
                List<Puzzle> oldPossibilities = possibilities.get(trigger);
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
                        }
                    }
                }
                // Search on new trigger
                for (int i = resumePointOfTrigger + 1; i < candidates.size(); i++) {
                    Puzzle newTrigger = candidates.get(i);
                    if (newTrigger.isMerged())
                        continue; // skip merged candidates
                    List<Puzzle> listPossibilities = possibilities.get(newTrigger);
                    for (Puzzle newTarget : listPossibilities) {
                        // Check if merged
                        if (!newTarget.isMerged()) {
                            try {
                                return new Action(parent, newTrigger, newTarget);
                            } catch (DuplicateActionException e) {
                                LOGGER.finest(e.getMessage());
                                // This action has been tried
                                // We search for others
                            }
                        }
                    }
                }
                return null;
            }

            /**
             * Two {@link Action} are equal if their targets and triggers are
             * asymetrically identical
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
         */
        class DuplicateActionException extends Exception {

            /**
             *
             */
            private static final long serialVersionUID = 5125661697708952752L;

            DuplicateActionException(Puzzle p1, Puzzle p2) {
                super("Has tried {" + p1 + "+" + p2 + "}");
            }
        }

        /**
         * Indicate that the number actions we realized has been too much
         *
         * @author Quoc Nhat Han TRAN
         */
        class TooDeepSearchException extends Exception {

            /**
             *
             */
            private static final long serialVersionUID = -3388495688242904101L;

            TooDeepSearchException() {
                super(limitActions + " actions have been realized but no solution is found");
            }
        }
    }
}