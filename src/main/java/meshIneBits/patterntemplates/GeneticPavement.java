package meshIneBits.patterntemplates;

import meshIneBits.Bit2D;
import meshIneBits.artificialIntelligence.AI_Tool;
import meshIneBits.artificialIntelligence.genetics.Genetic;
import meshIneBits.Layer;
import meshIneBits.Mesh;
import meshIneBits.Pavement;
import meshIneBits.config.patternParameter.DoubleParam;
import meshIneBits.util.AreaTool;

import java.awt.geom.Area;
import java.util.Collection;

public class GeneticPavement extends PatternTemplate {

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
                "ratio",
                "Length/Area ratio %",
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
                5.0));
        config.add(AI_Tool.paramSafeguardSpace);
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
                (double) config.get("ratio").getCurrentValue(),
                (double) config.get("earlyStopping").getCurrentValue())
                .getSolutions();
        updateBitAreasWithSpaceAround(bits);
        return new Pavement(bits);
    }

    @Override
    public Pavement pave(Layer layer, Area area) {
        System.out.println("Pave layer & area with genetics... Not implemented yet.");
        return null;
    }

    @Override
    public int optimize(Layer actualState) {
        // TODO: 2021-01-17 implement optimization for last bit placement as in classic brick pattern.
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
        return "Choose your length covered/area ratio and your params. Choose the gap you desire.";
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
                        (double) config.get("safeguardSpace").getCurrentValue()));
            }
        }

    }

}
