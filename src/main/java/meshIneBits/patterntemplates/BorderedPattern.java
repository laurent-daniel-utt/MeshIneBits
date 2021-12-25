package meshIneBits.patterntemplates;

import meshIneBits.Bit2D;
import meshIneBits.Layer;
import meshIneBits.Mesh;
import meshIneBits.Pavement;
import meshIneBits.artificialIntelligence.AI_Tool;
import meshIneBits.artificialIntelligence.deepLearning.MagicAlgorithm;
import meshIneBits.util.AreaTool;
import meshIneBits.util.Vector2;

import java.awt.geom.Area;
import java.util.Collection;
import java.util.Vector;

public class BorderedPattern extends PatternTemplate {
    @Override
    protected void initiateConfig() {
    }

    @Override
    public boolean ready(Mesh mesh) {
        return true;
    }

    @Override
    public Pavement pave(Layer layer) {
        try {
            MagicAlgorithm magicAlgorithm = new MagicAlgorithm();
            Collection<Bit2D> bits = magicAlgorithm.getBits(layer.getHorizontalSection());
            //updateBitAreasWithSpaceAround(bits);
            return new Pavement(bits);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new Pavement(new Vector<>());
    }

    @Override
    public Pavement pave(Layer layer, Area area) {
        System.out.println("Pave layer & area with AI... Not implemented yet.");
        return null;
    }

    @Override
    public int optimize(Layer actualState) {
        // TODO: 2021-01-17 implement optimization for last bit placement as in GeneticPavement.
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
