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
            Pavement pavement = new Pavement(bits);
            pavement.computeBits(layer.getHorizontalSection());
            return pavement;
        }
        else
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



}
