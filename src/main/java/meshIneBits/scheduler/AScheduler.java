package meshIneBits.scheduler;

import javafx.util.Pair;
import meshIneBits.Bit3D;
import meshIneBits.Mesh;
import meshIneBits.MeshEvents;
import meshIneBits.util.Logger;
import meshIneBits.util.Vector2;

import java.io.Serializable;
import java.util.Observable;
import java.util.Vector;

public abstract class AScheduler extends Observable implements Serializable, Runnable {
    protected Mesh mesh = null;
    protected Vector<Pair<Bit3D, Vector2>> sortedBits = new Vector<>();
    protected Vector<Bit3D> firstLayerBits = new Vector<>();

    AScheduler() {}
    AScheduler(Mesh m)
    {
        super();
        addObserver(m);
        mesh = m;
    }

    public void setMesh(Mesh m)
    {
        this.mesh = m;
    }

    /**
     * Function used to return bit index in the ordering process
     * @param bit
     * @return
     */
    public abstract int getBitIndex(Bit3D bit);

    /**
     * Return batch index for a bit
     * @param bit
     * @return
     */
    public abstract int getBitBatch(Bit3D bit);

    /**
     * Return plate index for a bit
     * @param bit
     * @return
     */
    public abstract int getBitPlate(Bit3D bit);

    /**
     * Lauch ordering process
     * @return
     */
    public abstract boolean order();

    public boolean isScheduled()
    {
        return !this.sortedBits.isEmpty();
    }

    public Vector<Pair<Bit3D, Vector2>> getSortedBits() { return this.sortedBits; }
    public Vector<Bit3D> getFirstLayerBits() { return this.firstLayerBits; }

    public abstract boolean schedule();

    public void run(){
        notifyObservers(MeshEvents.SCHEDULING);
        Logger.updateStatus("Starting bits cut & place scheduling operation.");
        schedule();
        Logger.updateStatus("Bits cut & place scheduling is over.");
        notifyObservers(MeshEvents.SCHEDULED);
    }


}
