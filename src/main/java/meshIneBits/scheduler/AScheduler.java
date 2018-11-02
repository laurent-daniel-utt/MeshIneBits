package meshIneBits.scheduler;

import meshIneBits.Bit2D;
import meshIneBits.Mesh;
import meshIneBits.MeshEvents;
import meshIneBits.util.Logger;

import java.io.Serializable;
import java.util.Observable;

public abstract class AScheduler extends Observable implements Serializable, Runnable {
    private Mesh mesh = null;

    AScheduler() {}
    AScheduler(Mesh m)
    {
        super();
        addObserver(m);
        mesh = m;
    }

    /**
     * Function used to return bit index in the ordering process
     * @param bit
     * @return
     */
    public abstract int getBitIndex(Bit2D bit);

    /**
     * Return batch index for a bit
     * @param bit
     * @return
     */
    public abstract int getBitBatch(Bit2D bit);

    /**
     * Lauch ordering process
     * @return
     */
    public abstract boolean order();

    public abstract boolean schedule();

    public void run(){
        notifyObservers(MeshEvents.SCHEDULING);
        Logger.updateStatus("Starting scheduling operation");
        schedule();
        notifyObservers(MeshEvents.SCHEDULED);
    }


}
