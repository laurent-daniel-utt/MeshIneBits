package meshIneBits.patterntemplates;

import meshIneBits.Bit2D;
import meshIneBits.Layer;
import meshIneBits.Mesh;
import meshIneBits.Pavement;
import meshIneBits.artificialIntelligence.AI_Tool;
import meshIneBits.artificialIntelligence.deepLearning.BorderedPatternAlgorithm;
import meshIneBits.config.CraftConfig;
import meshIneBits.config.patternParameter.DoubleParam;
import meshIneBits.util.AreaTool;

import java.awt.geom.Area;
import java.util.Collection;
import java.util.Vector;

public class BorderedPattern extends PatternTemplate {
    @Override
    protected void initiateConfig() {
        config.add(new DoubleParam(
                "minWidth",
                "min width to keep",
                "Minimum bit width kept after cut",
                0.0,
                CraftConfig.bitWidth / 2,
                3.0,
                5.0));
        config.add(AI_Tool.paramSafeguardSpace);//TODO @Etienne faire mieux

    }

    @Override
    public boolean ready(Mesh mesh) {
        return true;
    }

    @Override
    public Pavement pave(Layer layer) {
        try {
            BorderedPatternAlgorithm borderedPatternAlgorithm = new BorderedPatternAlgorithm();
            Collection<Bit2D> bits = borderedPatternAlgorithm.getBits(layer.getHorizontalSection(),
                    (double) config.get("minWidth").getCurrentValue());
            updateBitAreasWithSpaceAround(bits);
            return new Pavement(bits);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new Pavement(new Vector<>());
    }

    @Override
    public Pavement pave(Layer layer, Area area) {
        System.out.println("Pave layer & area with algorithms... Not implemented yet.");
        return null;
    }

    @Override
    public int optimize(Layer actualState) {
        return -2;
    }

    @Override
    public String getCommonName() {
        return "Bordered Pattern";
    }

    @Override
    public String getIconName() {
        return "pattern-AI.png";
    }

    @Override
    public String getDescription() {
        return "Paves the bounds of the slices with a neural network";
    }

    @Override
    public String getHowToUse() {
        return "";
    }

    /**
     * Cut the bits with the others according to the safeguardSpace
     *
     * @param bits the collection of bits to cut
     */
    //(Modified function for border algorithms, the other doesn't work)
    private void updateBitAreasWithSpaceAround(Collection<Bit2D> bits) {
        double safeguardSpace = (double) config.get("safeguardSpace").getCurrentValue();
        for (Bit2D bit2DToCut : bits) {
            Area bit2DToCutArea = bit2DToCut.getArea();
            Area nonAvailableArea = new Area();
            for (Bit2D bit2D : bits) {
                if (!bit2D.equals(bit2DToCut)) {
                    Area expand = AreaTool.expand(bit2D.getArea(), safeguardSpace);
                    nonAvailableArea.add(expand);
                }
            }
            bit2DToCutArea.subtract(nonAvailableArea);
            bit2DToCut.updateBoundaries(bit2DToCutArea);
        }
    }
}
