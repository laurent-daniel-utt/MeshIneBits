package meshIneBits.patterntemplates;

import meshIneBits.Bit2D;
import meshIneBits.IA.genetics.Genetic;
import meshIneBits.Layer;
import meshIneBits.Mesh;
import meshIneBits.Pavement;

import java.awt.geom.Area;
import java.util.Vector;

public class GeneticPavement extends PatternTemplate {

    @Override
    protected void initiateConfig() {
        //Nothing
    }

    @Override
    public boolean ready(Mesh mesh) {
        return true;
    }

    @Override
    public Pavement pave(Layer layer) {
        Vector<Bit2D> bits = new Vector<>();
        if (layer.getLayerNumber() < 1) {
            bits = new Genetic(layer).getSolutions();
        }
        return new Pavement(bits);
    }

    @Override
    public Pavement pave(Layer layer, Area area) { //todo @Etienne@Andre il sert à quoi lui?
        System.out.println("pave layer & area");
       /* Vector<Bit2D> bits = new Vector<>();
        if (layer.getLayerNumber()==0) {
            area.intersect(AreaTool.getAreaFrom(layer.getHorizontalSection()));//do smthg with?
            Genetic genetic = new Genetic();
            bits = genetic.getSolutions();
        }
        Pavement pavement = new Pavement(bits);
        pavement.computeBits(area);
        return pavement;*/
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
        return "pattern-classic-brick.png";
    }

    @Override
    public String getDescription() {
        return "Paves the bounds of the slices with genetic algorithms.";
    }

    @Override
    public String getHowToUse() {
        return "";
    } //todo @Etienne@Andre, on mets quoi

}
