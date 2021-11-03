package meshIneBits.gui.view3d;

public interface IProcessingModel3D {

    void rotationX(float x);
    void rotationY(float y);
    void rotationZ(float z);

    void translateX(float x);
    void translateY(float y);
    void translateZ(float z);

    void apply();
    void applyGravity();
    void centerCamera();
    void reset();

    void displayModel(boolean boo);
    void displayMesh(boolean boo);

    void setAnimationByBit(boolean boo);
    void setAnimationByBatch(boolean boo);
    void setAnimationByLayer(boolean boo);

    void setDisplayOneByOne(boolean boo);
    void setDisplayFull(boolean b);

    void export();

    void animation();

    void stopAnimation();
}

