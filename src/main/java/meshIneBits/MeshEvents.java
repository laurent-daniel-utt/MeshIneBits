package meshIneBits;

public enum MeshEvents {
    READY(false, 0),
    IMPORTING(true, 100),
    IMPORTED(false, 101),
    SLICING(true, 200),
    SLICED(false, 201),
    PAVING_MESH(true, 300),
    PAVED_MESH(false, 301),
    OPTIMIZING_LAYER(true, 400),
    OPTIMIZED_LAYER(false, 401),
    OPTIMIZING_MESH(true, 500),
    OPTIMIZED_MESH(false, 501),
    GLUING(true, 600),
    GLUED(false, 601);

    private boolean working;
    /**
     * Indicate position of state in workflow
     */
    private int code;

    MeshEvents(boolean working, int code) {
        this.working = working;
        this.code = code;
    }

    public boolean isWorking() {
        return working;
    }

    public int getCode() {
        return code;
    }
}
