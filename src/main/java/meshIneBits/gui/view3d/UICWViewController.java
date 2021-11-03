package meshIneBits.gui.view3d;

import meshIneBits.Mesh;
import meshIneBits.util.CustomLogger;
import meshIneBits.util.MultiThreadServiceExecutor;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static meshIneBits.gui.view3d.ButtonsLabel.*;
import static meshIneBits.gui.view3d.ButtonsLabel.VIEW_MESH;

public class UICWViewController implements UIControllerListener, ProcessingModelView.ModelChangesListener {

    private static final long time = 1000; //second
    private static final double speed_coefficient_default = 1.0; //=coefficient*time
    private static final double speed_coefficient_min = 2;
    private static final double speed_coefficient_max = 0.05;
    private static final double speed_level_number = 10;
    private final Mesh mesh;
    private IProcessingModel3D view;
    private CustomLogger logger = new CustomLogger(UICWViewController.class);

    private double animationSpeed = speed_coefficient_default;
    private AtomicBoolean isAnimating = new AtomicBoolean(false);
    private AtomicInteger index = new AtomicInteger();
    private MultiThreadServiceExecutor executor = MultiThreadServiceExecutor.instance;
    private int indexMax;
    private final AtomicBoolean pausing = new AtomicBoolean(false);
    private AnimationIndexIncreasedListener[] indexListeners;


    public UICWViewController(IProcessingModel3D view, Mesh mesh) {
        this.mesh = mesh;
        this.view = view;
    }

    @Override
    public void onActionListener(String event, Object value) {
        switch (event) {
            case ROTATION_X:
                view.rotationX((float) value);
                break;
            case ROTATION_Y:
                view.rotationY((float) value);
                break;
            case ROTATION_Z:
                view.rotationZ((float) value);
                break;
            case POSITION_X:
                view.translateX((float) value);
                break;
            case POSITION_Y:
                view.translateY((float) value);
                break;
            case POSITION_Z:
                view.translateZ((float) value);
                break;
            case VIEW_MESH:
                boolean boo = (boolean) value;
                view.displayMesh(boo);
                view.displayModel(!boo);
                break;
            case APPLY:
                view.apply();
                break;
            case GRAVITY:
                view.applyGravity();
                break;
            case CENTER_CAMERA:
                view.centerCamera();
                break;
            case RESET:
                view.reset();
                break;
            case BY_BIT:
                view.setAnimationByBit((boolean) value);
                setAnimationIndex(0);
                break;
            case BY_BATCH:
                view.setAnimationByBatch((boolean) value);
                setAnimationIndex(0);
                break;
            case BY_LAYER:
                view.setAnimationByLayer((boolean) value);
                setAnimationIndex(0);
                break;
            case ONE_BY_ONE:
                view.setDisplayOneByOne((boolean) value);
                break;
            case FULL:
                view.setDisplayFull((boolean) value);
                break;
            case EXPORT:
                view.export();
            case ANIMATION:
                startAnimation();
                break;
            case SPEED_UP:
                updateAnimationSpeedCoefficient(true);
                break;
            case SPEED_DOWN:
                updateAnimationSpeedCoefficient(false);
                break;
            case PAUSE:
                pauseAnimation();
                break;
            case ANIMATION_SLICER:
                setAnimationIndex(Math.round((float) value));
                break;
            default:
                logger.logERRORMessage("The event invoked is not handled by UserControllerListener object: " + this.getClass());
                break;
        }
    }

    private void startAnimation() {
        isAnimating.set(!isAnimating.get());
        if (isAnimating.get()) {
            animationSpeed = speed_coefficient_default;
            view.animation();
            increaseAnimationIndex();
        } else {
            view.stopAnimation();
            resetAnimation();
        }
    }

    private void increaseAnimationIndex() {
        executor.execute(() -> {
            try {
                while (isAnimating.get()) {
                    if (pausing.get()) {
                        synchronized (pausing) {
                            pausing.wait();
                        }
                    } else {
                        Thread.sleep((long) (animationSpeed * time));
                        if (index.get() == indexMax - 1) {
                            setAnimationIndex(0);
                        } else {
                            setAnimationIndex(index.get() + 1);
                        }
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    private void setAnimationIndex(int i) {
        index.set(i);
        if (indexListeners == null) return;
        for (AnimationIndexIncreasedListener listener : indexListeners) {
            if (pausing.get() && (listener instanceof UIControlWindowAnimation)) continue;
            listener.onIndexIncreasedListener(index.get());
        }
    }

    private void updateAnimationSpeedCoefficient(boolean inscreasing) {
        animationSpeed += (inscreasing ? -1 : 1) * (speed_coefficient_min - speed_coefficient_max) / speed_level_number;
        if (animationSpeed >= speed_coefficient_min) {
            animationSpeed = speed_coefficient_min;
        }
        if (animationSpeed <= speed_coefficient_max) {
            animationSpeed = speed_coefficient_max;
        }
    }

    private void resetAnimation() {
        isAnimating.set(false);
        synchronized (pausing) {
            pausing.set(false);
            pausing.notify();
        }
        setAnimationIndex(0);
        animationSpeed = speed_coefficient_default;
    }

    public void setAnimationIndexListener(AnimationIndexIncreasedListener... listeners) {
        this.indexListeners = listeners;
    }

    public void setAnimationRange(int indexMax) {
        this.indexMax = indexMax;
        if (indexListeners == null) return;
        for (AnimationIndexIncreasedListener listener : indexListeners) {
            listener.updateIndexRange(0, indexMax);
        }
    }

    public void pauseAnimation() {
        synchronized (pausing) {
            pausing.set(!pausing.get());
            if (!pausing.get()) {
                pausing.notify();
            }
        }
    }

    @Override
    public void onSizeChange(double scale, double dept, double width, double height) {

    }

    @Override
    public void onPositionChange(double x, double y, double z) {

    }

    @Override
    public void onRotationChange(double x, double y, double z) {

    }

    public void close() {
        view = null;
        indexListeners=null;
    }


}
