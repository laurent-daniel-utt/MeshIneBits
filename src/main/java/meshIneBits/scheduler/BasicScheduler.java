package meshIneBits.scheduler;

import javafx.scene.layout.CornerRadii;
import javafx.util.Pair;
import meshIneBits.Bit2D;
import meshIneBits.Bit3D;
import meshIneBits.Layer;
import meshIneBits.Mesh;
import meshIneBits.config.CraftConfig;
import meshIneBits.util.Logger;
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
        Logger.setProgress(0, this.mesh.getLayers().size());
        double xMin;
        this.sortedBits.clear();
        Logger.message("Size of layer "+ this.mesh.getLayers().size());
        int i=0;
        for (Layer curLayer: this.mesh.getLayers())
        {
            Vector<Pair<Bit3D, Vector2>> bits = curLayer.sortBits();
            bits=this.filterBits(bits);
            if (bits.size()>0) {
                System.out.println(bits.size());
                this.sortedBits.addAll(bits);
                this.firstLayerBits.put(i,bits.firstElement().getKey());
            }
//                this.firstLayerBits.add(bits.firstElement().getKey());
            Logger.setProgress(curLayer.getLayerNumber()+1, this.mesh.getLayers().size());
            i++;
        }
        System.out.println("size of firstlayerBits: "+firstLayerBits.size()+", first value: "+firstLayerBits.get(0));
        System.out.println("Size of SOrtedBits: "+sortedBits.size());
        System.out.println("Basic scheduler end scheduling");
        return true;
    }
    public Vector<Pair<Bit3D, Vector2>> filterBits(Vector<Pair<Bit3D, Vector2>> bits){
        double xMin;
        if(bits.size()>0){
            xMin = bits.get(0).getValue().x;
            bits = this.sortBits(bits, Math.abs(xMin));
            System.out.println(bits.size());
//                this.firstLayerBits.add(bits.firstElement().getKey());
        }
        return bits;
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
