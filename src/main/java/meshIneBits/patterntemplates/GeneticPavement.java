package meshIneBits.patterntemplates;

import meshIneBits.Bit2D;
import meshIneBits.artificialIntelligence.genetics.Genetic;
import meshIneBits.Layer;
import meshIneBits.Mesh;
import meshIneBits.Pavement;
import meshIneBits.config.patternParameter.DoubleParam;
import meshIneBits.util.AreaTool;

import java.awt.geom.Area;
import java.util.Collection;

public class GeneticPavement extends PatternTemplate {

    private final DoubleParam safeguardSpaceParam = new DoubleParam(
            "safeguardSpace",
            "Space around bit",
            "In order to keep bits not overlapping or grazing each other",
            1.0, 10.0, 3.0, 0.01);

    @Override
    protected void initiateConfig() {
        config.add(new DoubleParam(
                "genNumber",
                "Number of generations",
                "The number of generations",
                1.0,
                50.0,
                10.0,
                1.0));

        config.add(new DoubleParam(
                "popSize",
                "Size of the population",
                "The size of population to generate",
                100.0,
                Double.POSITIVE_INFINITY,
                150.0,
                50.0));

        config.add(new DoubleParam(
                "lengthCoefficient",
                "Length Coefficient %",
                "Balance between area and length to calculate solutions' score",
                2.0,
                100.0,
                80.0,
                2.0));
        config.add(new DoubleParam(
                "earlyStopping",
                "Maximum number of bits before stopping",
                "Set a max number of bits to avoid infinite loop",
                0.0,
                Double.POSITIVE_INFINITY,
                50.0,
                5.0
        ));
    }

    @Override
    public boolean ready(Mesh mesh) {
        return true;
    }

    @Override
    public Pavement pave(Layer layer) {
        Collection<Bit2D> bits = new Genetic(
                layer,
                (double) config.get("genNumber").getCurrentValue(),
                (double) config.get("popSize").getCurrentValue(),
                (double) config.get("lengthCoefficient").getCurrentValue(),
                (double) config.get("earlyStopping").getCurrentValue())
                .getSolutions();
        updateBitAreasWithSpaceAround(bits);
        return new Pavement(bits);
    }

    @Override
    public Pavement pave(Layer layer, Area area) {
        System.out.println("pave layer & area with genetics... Not implemented yet.");
        return null;
    }

    @Override
    public int optimize(Layer actualState) {
        return -2;
    }

    @Override
    public String getCommonName() {
        return "Genetic pavement";
    }

    @Override
    public String getIconName() {
        return "pattern-genetic.png";
    }

    @Override
    public String getDescription() {
        return "Paves the bounds of the slices with genetic algorithms.";
    }

    @Override
    public String getHowToUse() {
        return "";
    }

    private void updateBitAreasWithSpaceAround(Collection<Bit2D> bits) {
        Area availableArea = new Area();
        for (Bit2D bit : bits) {
            availableArea.add(bit.getArea());
        }
        for (Bit2D bit : bits) {
            if (bit.getArea() == null) continue;
            Area bitArea = bit.getArea();
            bitArea.intersect(availableArea);
            if (!bitArea.isEmpty()) {
                bit.updateBoundaries(bitArea);
                availableArea.subtract(AreaTool.expand(
                        bitArea, // in real
                        safeguardSpaceParam.getCurrentValue()));
            }
        }

    }

}
