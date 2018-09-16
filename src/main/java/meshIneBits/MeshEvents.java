package meshIneBits;

public enum MeshEvents {
    READY(false),
    IMPORTING(true),
    IMPORTED(false),
    SLICING(true),
    SLICED(false),
    PAVING_MESH(true),
    PAVED_MESH(false),
    OPTIMIZING_LAYER(true),
    OPTIMIZED_LAYER(false),
    OPTIMIZING_MESH(true),
    OPTIMIZED_MESH(false),
    GLUING(true),
    GLUED(false);

    private boolean working;

    MeshEvents(boolean working) {
        this.working = working;
    }

    public boolean isWorking() {
        return working;
    }
}
