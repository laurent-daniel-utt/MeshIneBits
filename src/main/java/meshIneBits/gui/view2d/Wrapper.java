/*
 * MeshIneBits is a Java software to disintegrate a 3d mesh (model in .stl)
 * into a network of standard parts (called "Bits").
 *
 * Copyright (C) 2016  Thibault Cassard & Nicolas Gouju.
 * Copyright (C) 2017-2018  TRAN Quoc Nhat Han.
 * Copyright (C) 2018 Vallon BENJAMIN.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package meshIneBits.gui.view2d;

import meshIneBits.Layer;
import meshIneBits.Mesh;
import meshIneBits.gui.MainWindow;
import meshIneBits.gui.utilities.ButtonIcon;
import meshIneBits.gui.utilities.OptionsContainer;
import meshIneBits.gui.utilities.RibbonCheckBox;
import meshIneBits.gui.utilities.patternParamRenderer.LabeledSpinner;
import meshIneBits.util.Logger;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Objects;
import java.util.Observable;
import java.util.Observer;

/**
 * The panel wrapping 2D representation with view orienting tools. Exported from
 * {@link MainWindow}. It observes {@link Controller}.
 */
class Wrapper extends JPanel implements Observer {

    private static final long serialVersionUID = 1L;
    private Controller controller;
    private Core coreView;
    private JSlider zoomSlider;
    private JSlider layerSlider;
    private JSpinner layerSpinner;
    private JSlider sliceSlider;
    private JSpinner sliceSpinner;
    private JLabel bg;

    static final double MIN_ZOOM_VALUE = 0.5;
    private final int MIN_ZOOM_SLIDER_VALUE = 1;
    private final int MAX_ZOOM_SLIDER_VALUE = 500;

    // Following attributes are the coefficients from the formula Y = a * EXP(b * x)
    // used for the zoom's log scale
    private double aCoefficient;
    private double bCoefficient;

    Wrapper() {
        this.setLayout(new BorderLayout());

        controller = Controller.getInstance();
        controller.addObserver(this);

        buildBackground();
        this.add(bg, BorderLayout.CENTER);


        // Set up the core of view
        coreView = new Core();
        controller.addObserver(coreView);
    }

    Controller getController() {
        return controller;
    }

    private void buildBackground() {
        bg = new JLabel("", SwingConstants.CENTER);
        ImageIcon icon = new ImageIcon(
                new ImageIcon(Objects.requireNonNull(this.getClass().getClassLoader().getResource("resources/MeshIneBitsAlpha.png"))).getImage()
                        .getScaledInstance(645, 110, Image.SCALE_SMOOTH));
        bg.setIcon(icon);
        bg.setFont(new Font(null, Font.BOLD | Font.ITALIC, 120));
        bg.setForeground(new Color(0, 0, 0, 8));

        bCoefficient = Math.log(MIN_ZOOM_VALUE / 10) / (MIN_ZOOM_SLIDER_VALUE - MAX_ZOOM_SLIDER_VALUE);
        aCoefficient = MIN_ZOOM_VALUE / Math.exp(bCoefficient * MIN_ZOOM_SLIDER_VALUE);
    }

    private int getZoomSliderValue(double zoomValue) {
        return (int) (Math.log(zoomValue / aCoefficient) / bCoefficient);
    }

    private double getZoomValue(int zoomSliderValue) {
        return aCoefficient * Math.exp(bCoefficient * zoomSliderValue);
    }

    private void buildZoomer() {
        zoomSlider = new JSlider(SwingConstants.HORIZONTAL, MIN_ZOOM_SLIDER_VALUE, MAX_ZOOM_SLIDER_VALUE,
                getZoomSliderValue(controller.getZoom()));
        zoomSlider.setMaximumSize(new Dimension(MAX_ZOOM_SLIDER_VALUE, 20));

        ButtonIcon zoomMinusBtn = new ButtonIcon("", "search-minus.png", true);
        ButtonIcon zoomPlusBtn = new ButtonIcon("", "search-plus.png", true);

        JPanel zoomer = new JPanel();
        zoomer.setLayout(new FlowLayout(FlowLayout.CENTER));
        zoomer.add(zoomMinusBtn);
        zoomer.add(zoomSlider);
        zoomer.add(zoomPlusBtn);
        zoomer.setAlignmentX(Component.CENTER_ALIGNMENT);

        this.add(zoomer, BorderLayout.SOUTH);

        zoomSlider.addChangeListener(e -> controller.setZoom(getZoomValue(zoomSlider.getValue())));

        zoomMinusBtn.addActionListener(e -> controller.setZoom(controller.getZoom() / 2));

        zoomPlusBtn.addActionListener(e -> controller.setZoom(controller.getZoom() * 2));
    }

    private void buildLayerSelector() {
        Mesh part = controller.getCurrentMesh();

        layerSlider = new JSlider(SwingConstants.VERTICAL, 0, part.getLayers().size() - 1, 0);
        layerSpinner = new JSpinner(new SpinnerNumberModel(0, 0, part.getLayers().size() - 1, 1));

        layerSpinner.setFocusable(false);
        layerSpinner.setMaximumSize(new Dimension(40, 40));

        JPanel layerPanel = new JPanel();
        layerPanel.setLayout(new BoxLayout(layerPanel, BoxLayout.PAGE_AXIS));
        layerPanel.add(layerSlider);
        layerPanel.add(layerSpinner);
        layerPanel.setBorder(new EmptyBorder(0, 0, 5, 5));

        this.add(layerPanel, BorderLayout.EAST);

        layerSpinner.addChangeListener(e -> controller.setLayer((Integer) layerSpinner.getValue()));

        layerSlider.addChangeListener(e -> controller.setLayer(layerSlider.getValue()));
    }

    private void buildSliceSelector() {
        Mesh part = controller.getCurrentMesh();

        sliceSlider = new JSlider(SwingConstants.VERTICAL, 0, part.getSlices().size() - 1, 0);
        sliceSpinner = new JSpinner(new SpinnerNumberModel(0, 0, part.getSlices().size() - 1, 1));

        sliceSpinner.setFocusable(false);
        sliceSpinner.setMaximumSize(new Dimension(40, 40));

        JPanel slicePanel = new JPanel();
        slicePanel.setLayout(new BoxLayout(slicePanel, BoxLayout.PAGE_AXIS));
        slicePanel.add(sliceSlider);
        slicePanel.add(sliceSpinner);
        slicePanel.setBorder(new EmptyBorder(0, 5, 5, 0));

        this.add(slicePanel, BorderLayout.EAST);

        sliceSpinner.addChangeListener(e -> controller.setSlice((Integer) sliceSpinner.getValue()));

        sliceSlider.addChangeListener(e -> controller.setSlice(sliceSlider.getValue()));
    }

    private void init() {
        Mesh mesh = controller.getCurrentMesh();
        if (mesh != null && (mesh.isPaved() || mesh.isSliced())) {
            // remove old components
            this.removeAll();

            // Repaint
            this.add(coreView, BorderLayout.CENTER);

            if (controller.getCurrentMesh().isPaved()) {
                buildLayerSelector();
            } else {
                buildSliceSelector();
            }

            buildZoomer();
            buildToolbox();
        } else {
            noPart();
        }
    }

    private void buildToolbox() {
        JPanel toolbox = new JPanel();
        toolbox.setLayout(new BoxLayout(toolbox, BoxLayout.PAGE_AXIS));

        // Build internal components
        toolbox.add(new DisplayOptionsPane());
//        toolbox.add(new SliceSelectorPane());
        toolbox.add(new BitModifierPane());
        toolbox.add(new BitAdderPane());
        toolbox.add(new AutoOptimizerPane());

        add(new JScrollPane(toolbox), BorderLayout.WEST);
    }


    private void noPart() {
        this.removeAll();
        this.add(bg);
        repaint();
        revalidate();
    }

    @Override
    public void update(Observable o, Object arg) {
        if (arg != null) {
            switch ((Controller.Component) arg) {
                case MESH:
                    init();
                    break;
                case LAYER:
                    updateLayerChoice(controller.getCurrentLayerNumber());
                    break;
                case ZOOM:
                    updateZoom(controller.getZoom());
                    break;
                case SLICE:
                    updateSliceChoice(controller.getCurrentSliceNumber());
                    break;
                case SELECTED_BIT:
                    // TODO show bit properties
                    break;
                default:
                    break;
            }
        }
        repaint();
        revalidate();
    }

    private void updateLayerChoice(int layerNr) {
        try {
            layerSpinner.setValue(layerNr);
            layerSlider.setValue(layerNr);
        } catch (Exception e) {
            // If layer spinner and slider don't exist
        }
    }

    private void updateSliceChoice(int sliceNr) {
        try {
            sliceSpinner.setValue(sliceNr);
            sliceSlider.setValue(sliceNr);
        } catch (Exception e) {
            // If slice spinner and slider don't exist
        }
    }

    private void updateZoom(double zoom) {
        try {
            if (getZoomSliderValue(zoom) <= MAX_ZOOM_SLIDER_VALUE)
                zoomSlider.setValue(getZoomSliderValue(zoom));
            else if (getZoomSliderValue(zoom) > MAX_ZOOM_SLIDER_VALUE && zoomSlider.getValue() < MAX_ZOOM_SLIDER_VALUE - 1) {
                zoomSlider.setValue(MAX_ZOOM_SLIDER_VALUE - 1);
            }

        } catch (Exception e) {
            // If the slider doesn't exist
        }
    }

    private class DisplayOptionsPane extends OptionsContainer {
        DisplayOptionsPane() {
            super("Display options");
//			JCheckBox slicesCheckBox = new RibbonCheckBox("Show slices");
//			slicesCheckBox.addActionListener(e -> controller.toggleShowSlice(slicesCheckBox.isSelected()));

            JCheckBox sliceCheckBox = new RibbonCheckBox("Show slice");
            sliceCheckBox.addActionListener(e -> controller.toggleShowSlice(sliceCheckBox.isSelected()));

            JCheckBox liftPointsCheckBox = new RibbonCheckBox("Show lift points");
            liftPointsCheckBox.addActionListener(e -> controller.toggleShowLiftPoints(liftPointsCheckBox.isSelected()));

            JCheckBox previousLayerCheckBox = new RibbonCheckBox("Show previous layer");
            previousLayerCheckBox.addActionListener(e -> controller.toggleShowPreviousLayer(previousLayerCheckBox.isSelected()));

            JCheckBox cutPathsCheckBox = new RibbonCheckBox("Show cut paths");
            cutPathsCheckBox.addActionListener(e -> controller.toggleShowCutPaths(cutPathsCheckBox.isSelected()));

            JCheckBox irregularBitsCheckBox = new RibbonCheckBox("Show irregular bits");
            irregularBitsCheckBox.addActionListener(e -> controller.toggleShowIrregularBits(irregularBitsCheckBox.isSelected()));

//			add(slicesCheckBox);
            add(sliceCheckBox);
            add(liftPointsCheckBox);
            add(previousLayerCheckBox);
            add(cutPathsCheckBox);
            add(irregularBitsCheckBox);
        }
    }

    @SuppressWarnings("unused")
    @Deprecated
    private class SliceSelectorPane extends OptionsContainer {
        SliceSelectorPane() {
            super("Select slice");
            // To be deleted in future version
        }
    }

    private class BitModifierPane extends OptionsContainer {
        BitModifierPane() {
            super("Modify bit");
            JButton cutLengthBtn1 = new ButtonIcon("", "cut-length.png", true, 80, 25);
            JButton cutWidthBtn2 = new ButtonIcon("", "cut-width.png", true, 80, 25);
            JButton cutQuartBtn3 = new ButtonIcon("", "cut-quart.png", true, 80, 25);
            JButton deleteBitBtn = new ButtonIcon("", "delete-bit.png", true, 80, 25);
            JButton scaleFullBitBtn = new ButtonIcon("", "full-bit.png", true, 80, 25);
            add(cutLengthBtn1);
            add(cutWidthBtn2);
            add(cutQuartBtn3);
            add(deleteBitBtn);
            add(scaleFullBitBtn);

            // Action listener
            cutLengthBtn1.addActionListener(e -> controller.scaleSelectedBit(100, 50));
            cutWidthBtn2.addActionListener(e -> controller.scaleSelectedBit(50, 100));
            cutQuartBtn3.addActionListener(e -> controller.scaleSelectedBit(50, 50));
            deleteBitBtn.addActionListener(e -> controller.scaleSelectedBit(0, 0));
            scaleFullBitBtn.addActionListener(e -> controller.scaleSelectedBit(100, 100));
        }
    }

    private class BitAdderPane extends OptionsContainer {
        BitAdderPane() {
            super("Add bit");
            LabeledSpinner newBitsLengthSpinner = new LabeledSpinner(controller.newBitsLengthParam);
            add(newBitsLengthSpinner);
            LabeledSpinner newBitsWidthSpinner = new LabeledSpinner(controller.newBitsWidthParam);
            add(newBitsWidthSpinner);
            LabeledSpinner newBitsOrientationSpinner = new LabeledSpinner(controller.newBitsOrientationParam);
            add(newBitsOrientationSpinner);
            add(controller.safeguardSpaceParam.getRenderer());
            JButton chooseOriginsBtn = new JButton("Start");
            add(chooseOriginsBtn);
            JButton cancelChoosingOriginsBtn = new JButton("Cancel");
            add(cancelChoosingOriginsBtn);

            chooseOriginsBtn.addActionListener(e ->
                    controller.setOnAddingBits(true));
            cancelChoosingOriginsBtn.addActionListener(e ->
                    controller.setOnAddingBits(false));
        }
    }

    private class AutoOptimizerPane extends OptionsContainer {
        AutoOptimizerPane() {
            super("Auto-optimize");
            JButton currentLayerBtn = new ButtonIcon("This layer", "cog.png");
            JButton currentMeshBtn = new ButtonIcon("The whole mesh", "cog.png");
            currentLayerBtn.setToolTipText(
                    "Try the best to eliminate all irregular bits in the currently selected layer");
            currentMeshBtn.setToolTipText(
                    "Try the best to eliminate all irregular bits in the current mesh");
            add(currentLayerBtn);
            add(currentMeshBtn);

            currentLayerBtn.addActionListener(e -> {
                currentLayerBtn.setEnabled(false);
                currentMeshBtn.setEnabled(false);
                Mesh mesh = controller.getCurrentMesh();
                Layer currentLayer = controller.getLayer();
                try {
                    mesh.optimize(currentLayer);
                } catch (Exception e1) {
                    Logger.error(e1.getMessage());
                }
                currentLayerBtn.setEnabled(true);
                currentMeshBtn.setEnabled(true);
            });

            currentMeshBtn.addActionListener(e -> {
                currentLayerBtn.setEnabled(false);
                currentMeshBtn.setEnabled(false);
                try {
                    controller.getCurrentMesh().optimize();
                } catch (Exception e1) {
                    Logger.error(e1.getMessage());
                }
                currentLayerBtn.setEnabled(true);
                currentMeshBtn.setEnabled(true);
            });
        }
    }
}