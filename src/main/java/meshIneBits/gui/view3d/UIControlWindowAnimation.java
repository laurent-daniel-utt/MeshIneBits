package meshIneBits.gui.view3d;

import controlP5.*;
import controlP5.Button;
import processing.core.PApplet;

import java.awt.*;

import static meshIneBits.gui.view3d.ButtonsLabel.*;

public class UIControlWindowAnimation extends UIControlWindow implements AnimationIndexIncreasedListener {

    private static final Dimension SCREEN_DIMENSION = Toolkit.getDefaultToolkit().getScreenSize();

    private Button animation;
    private Button export;
    private Toggle toggleBits;
    private Toggle toggleBatch;
    private Toggle toggleLayers;
    private Toggle toggleCurrent;
    private Toggle toggleFull;
    private Slider sliderAnimation;
    private boolean pauseAnimation;
    private Button speedUpButton;
    private Button speedDownButton;
    private Button pauseButton;
    private boolean pausing =false;

//    public void settings() {
//        size(SCREEN_DIMENSION.width/5, SCREEN_DIMENSION.height - 60, P3D);
//    }

    @Override
    public void setup() {
        super.setup();
        surface.setLocation(SCREEN_DIMENSION.width - width, 0);
        System.out.println(width + " - " + height);
    }

    @Override
    public void onOpen() {

    }

    @Override
    public void onClose() {

    }

    @Override
    protected void generateButton() {
        int color = 255;
        animation = getControl().addButton(ANIMATION).setSize(140, 30)
                .setColorLabel(255).setFont(createFont("arial bold", 15));
        export = getControl().addButton(EXPORT).setSize(140, 30)
                .setColorLabel(255).setFont(createFont("arial bold", 15));

        toggleBits = getControl().addToggle(BY_BIT)
                .setSize(20, 20)
                .setColorBackground(color(255, 250))
                .setColorActive(color).setColorForeground(color + 50)
                .setLabel(BY_BIT)
                .setFont(createFont("arial bold", 15))
                .onClick((e) -> {
                    if (e.getController().getValue() == 1.0) {
                        toggleLayers.setState(false);
                        toggleBatch.setState(false);
                    }
                });
//                .setState(false);

        toggleBatch = getControl().addToggle(BY_BATCH)
                .setSize(20, 20)
                .setColorBackground(color(255, 250))
                .setColorActive(color).setColorForeground(color + 50)
                .setLabel(BY_BATCH)
                .setFont(createFont("arial bold", 15))
                .onClick((e) -> {
                    if (e.getController().getValue() == 1.0) {
                        toggleLayers.setState(false);
                        toggleBits.setState(false);
                    }
                });
//                .setState(false);

        toggleLayers = getControl().addToggle(BY_LAYER)
                .setSize(20, 20)
                .setColorBackground(color(255, 250))
                .setColorActive(color).setColorForeground(color + 50)
                .setLabel(BY_LAYER)
                .setFont(createFont("arial bold", 15))
                .onClick((e) -> {
                    if (e.getController().getValue() == 1.0) {
                        toggleBits.setState(false);
                        toggleBatch.setState(false);
                    }
                });
//                .setState(true);
//        toggleBits.setState(false);
//        toggleBatch.setState(false);
        toggleLayers.setState(true);

        toggleCurrent = getControl().addToggle(ONE_BY_ONE)
                .setSize(20, 20)
                .setColorBackground(color(255, 250))
                .setColorActive(color).setColorForeground(color + 50)
                .setLabel(ONE_BY_ONE)
                .setFont(createFont("arial bold", 15))
                .onClick((e) -> {
                    if (e.getController().getValue() == 1.0) {
                        toggleFull.setState(false);
                    }
                })
                .setState(false);
        toggleFull = getControl().addToggle(FULL)
                .setSize(20, 20)
                .setColorBackground(color(255, 250))
                .setColorActive(color).setColorForeground(color + 50)
                .setLabel(FULL)
                .setFont(createFont("arial bold", 15))
                .onClick((e) -> {
                    if (e.getController().getValue() == 1.0) {
                        toggleCurrent.setState(false);
                    }
                })
                .setState(true);

        sliderAnimation = getControl().addSlider(ANIMATION_SLICER);
        sliderAnimation.setVisible(true).setSize(200, 30).getCaptionLabel().setFont(createFont("arial bold", 15));
        sliderAnimation.onChange(e -> {
//            if (pauseAnimation) this.index = (int) sliderAnimation.getValue();
        });

        speedUpButton = getControl().addButton(SPEED_UP).setVisible(true).setSize(30, 30).setColorLabel(255).setFont(createFont("arial bold", 15));
        speedUpButton.getCaptionLabel().setText(">>");
        speedDownButton = getControl().addButton(SPEED_DOWN).setVisible(true).setSize(30, 30).setColorLabel(255).setFont(createFont("arial bold", 15));
        speedDownButton.getCaptionLabel().setText("<<");
        pauseButton = getControl().addButton(PAUSE).setVisible(true).setSize(50, 30).setColorLabel(255);
        pauseButton.getCaptionLabel().setText(PAUSE).setFont(createFont("arial bold", 15));
        pauseButton.onClick(e -> {
//            pauseAnimation = !pauseAnimation;
//            if (pauseAnimation) {
//                executorService.shutdownNow();
//                pauseButton.getCaptionLabel().setText("Start");
//            } else {
//                executorService = Executors.newSingleThreadExecutor();
//                pauseButton.getCaptionLabel().setText("Pause");
//            }
        });
    }

    @Override
    protected void updateButton() {

        animation.setPosition(30, height / 4);
//        tooltipExport.setPosition(displayWidth - 220, displayHeight / 4 + 347);

        toggleBits.setPosition(30, height / 4 + 50);
//        tooltipBits.setPosition(displayWidth - 220, displayHeight / 4 + 217);
        toggleBatch.setPosition(30, height / 4 + 100);
//        tooltipBatch.setPosition(displayWidth - 220, displayHeight / 4 + 262);
        toggleLayers.setPosition(30, height / 4 + 150);
//        tooltipLayers.setPosition(displayWidth - 220, displayHeight / 4 + 307);
        toggleCurrent.setPosition(width - 100, height / 4 + 50);
//        tooltipCurrent.setPosition(displayWidth - 100, displayHeight / 4 + 255);
        toggleFull.setPosition(width - 100, height / 4 + 150);
//        tooltipFull.setPosition(displayWidth - 100, displayHeight / 4 + 300);
        speedDownButton.setPosition(30, height / 4 + 250);
        speedUpButton.setPosition(150, height / 4 + 250);
        pauseButton.setPosition(80, height / 4 + 250);
        sliderAnimation.setPosition(30, height / 4 + 200);
        export.setPosition(30, height / 4 + 300);
    }

    @Override
    public void controlEvent(ControlEvent theEvent) {
        switch (theEvent.getName()) {
            case BY_BIT:
            case BY_BATCH:
            case BY_LAYER:
            case FULL:
            case ONE_BY_ONE:
                if (theEvent.getValue() == 1.0) {
                    System.out.println(theEvent.getName() + ": " + theEvent.getValue());
                    if (getListener() != null) {
                        getListener().onActionListener(theEvent.getName(), (float) 1.0 == theEvent.getValue());
                    }
                }
                break;
            case PAUSE:
                pausing = !pausing;
                if (getListener() != null) {
                    getListener().onActionListener(theEvent.getName(), theEvent.getValue());
                }
                break;
            case ANIMATION:
                pausing = false;
                if (getListener() != null) {
                    getListener().onActionListener(theEvent.getName(), theEvent.getValue());
                }
                break;
            case ANIMATION_SLICER:
                if (pausing && getListener() != null) {
                    getListener().onActionListener(theEvent.getName(), theEvent.getValue());
                }
                break;
            case EXPORT:
            case SPEED_DOWN:
            case SPEED_UP:
                if (getListener() != null) {
                    getListener().onActionListener(theEvent.getName(), theEvent.getValue());
                }
                break;
            default:
//                logger.logWARNMessage("The event invoked is not handled by method ControlEvent");
                break;
        }


    }

    public static void main(String[] args) {
        PApplet.main(UIControlWindowAnimation.class.getCanonicalName());
    }

    @Override
    public void updateIndexRange(int min, int max) {
        if (sliderAnimation != null)
            sliderAnimation.setRange(min, max);
    }

    @Override
    public void onIndexIncreasedListener(int index) {
        if (sliderAnimation != null)
            sliderAnimation.setValue(index);
    }
}
