package meshIneBits.patterntemplates;

import meshIneBits.Bit2D;
import meshIneBits.IA.genetics.Genetic;
import meshIneBits.Layer;
import meshIneBits.Mesh;
import meshIneBits.Pavement;
import meshIneBits.config.patternParameter.DoubleParam;
import meshIneBits.util.AreaTool;

import java.awt.geom.Area;
import java.util.Collection;
import java.util.Vector;

public class GeneticPavement extends PatternTemplate {
    private int toPave = 0; //debugOnly

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
                1.0,
                100000.0,
                150.0,
                1.0));

        config.add(new DoubleParam(
                "lengthPenalty",
                "Length Penalty %",
                "The greater the penalty, the greater the distance covered by the bit will be",
                1.0,
                100.0,
                90.0,
                1.0));
    }

    @Override
    public boolean ready(Mesh mesh) {
        return true;
    }

    @Override
    public Pavement pave(Layer layer) {
        Collection<Bit2D> bits;
        if (layer.getLayerNumber() == toPave) {//todo paver tout
            bits = new Genetic(
                    layer,
                    (double) config.get("genNumber").getCurrentValue(),
                    (double) config.get("popSize").getCurrentValue(),
                    (double) config.get("lengthPenalty").getCurrentValue())
                    .getSolutions();
            toPave++;
            updateBitAreasWithSpaceAround(bits);
            Pavement pavement = new Pavement(bits);
            return pavement;
        } else
            return new Pavement(new Vector<>());
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
        //todo @Etienne@Andre change icon
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
