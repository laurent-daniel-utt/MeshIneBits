package meshIneBits.scheduler;

import javafx.scene.layout.CornerRadii;
import javafx.util.Pair;
import meshIneBits.Bit2D;
import meshIneBits.Bit3D;
import meshIneBits.Layer;
import meshIneBits.Mesh;
import meshIneBits.config.CraftConfig;
import meshIneBits.util.Vector2;

import java.util.Vector;

public class BasicScheduler extends AScheduler {

    public BasicScheduler(){}

    public BasicScheduler(Mesh m) {
        super(m);
    }

    @Override
    public int getBitIndex(Bit3D bit) {
        if(sortedBits.isEmpty()) {
            return 0;
        }
        for(Pair<Bit3D, Vector2> pair: this.sortedBits)
        {
            if (pair.getKey() == bit)
            {
                return this.sortedBits.indexOf(pair);
            }
        }
        return 0;
    }

    @Override
    public int getBitBatch(Bit3D bit) {
        if(sortedBits.isEmpty()) {
            return 0;
        }
        return this.getBitIndex(bit)/CraftConfig.nbBitesBatch;
    }

    @Override
    public int getBitPlate(Bit3D bit) {
        if(sortedBits.isEmpty()) {
            return 0;
        }
        return this.getBitIndex(bit)/CraftConfig.nbBitesByPlat;
    }

    @Override
    public boolean order() {
        return false;
    }

    @Override
    public boolean schedule() {
        System.out.println("Basic scheduler schedule");
        double xMin;
        this.sortedBits.clear();

        for (Layer curLayer: this.mesh.getLayers())
        {
            Vector<Pair<Bit3D, Vector2>> bits = curLayer.sortBits();
            xMin = bits.get(0).getValue().x;
            bits = this.sortBits(bits, Math.abs(xMin));
            this.sortedBits.addAll(bits);
            this.firstLayerBits.add(bits.firstElement().getKey());
        }
        System.out.println("Basic scheduler end scheduling");
        return true;
    }

    public Vector<Pair<Bit3D, Vector2>> sortBits(Vector<Pair<Bit3D, Vector2>> keySet, double offsetX) {
        double xInterval = CraftConfig.workingWidth;
        keySet.sort((v1, v2) -> {
            int v1XColumn = (int)(v1.getValue().x + mesh.getModel().getPos().x + offsetX) / (int)xInterval;
            int v2XColumn = (int)(v2.getValue().x + mesh.getModel().getPos().x + offsetX) / (int)xInterval;

            if(v1XColumn == v2XColumn) {
                if (Double.compare(v1.getValue().y, v2.getValue().y) == 0) {
                    return Double.compare(v1.getValue().x, v2.getValue().x);
                } else {
                    return Double.compare(v1.getValue().y, v2.getValue().y);
                }
            } else if( v1XColumn < v2XColumn )
            {
                return -1;
            } else {
                return 1;
            }
        });
        return keySet;
    }

    public String toString()
    {
        return "Basic";
    }
}
