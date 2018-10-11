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

import meshIneBits.Bit3D;
import meshIneBits.Layer;
import meshIneBits.Mesh;
import meshIneBits.config.CraftConfig;
import meshIneBits.config.WorkspaceConfig;
import meshIneBits.gui.MainController;
import meshIneBits.slicer.Slice;
import meshIneBits.util.DetectorTool;
import meshIneBits.util.Polygon;
import meshIneBits.util.Vector2;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;

/**
 * A panel resided inside of {@link Wrapper} to show {@link Slice} or {@link
 * Layer} of the {@link Mesh}. It observes {@link Controller}
 */
class Core extends JPanel implements MouseMotionListener, MouseListener, MouseWheelListener, KeyListener, Observer {

    private static final long serialVersionUID = 1L;
    private double viewOffsetX, viewOffsetY;
    private Map<Bit3D, BitControls> bitMovers = new HashMap<>();
    private int oldX, oldY;
    private Controller controller;
    private boolean rightClickPressed = false;
    private double defaultZoom = 1;
    private double drawScale = 1;
    private boolean onControl;
    private AffineTransform realToView;
    private AffineTransform viewToReal;

    Core() {
        // Setting up for easier use later
        controller = Controller.getInstance();
        controller.setCurrentMesh(MainController.getInstance().getCurrentMesh());

        // Actions listener
        addMouseMotionListener(this);
        addMouseListener(this);
        addMouseWheelListener(this);
        addKeyListener(this);
        setFocusable(true);
        setupKeyBindings();
    }

    private void setupKeyBindings() {
        // Reset state when pressing Esc
        getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "RESET");
        getActionMap().put("RESET", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.reset();
            }
        });
    }

    private void setDefaultZoom() {
        int width = this.getWidth();
        int height = this.getHeight();
        if (width > height)
            defaultZoom = height / (controller.getCurrentMesh().getSkirtRadius() * 2);
        else
            defaultZoom = width / (controller.getCurrentMesh().getSkirtRadius() * 2);
    }

    private void updateDrawScale() {
        drawScale = controller.getZoom() * defaultZoom;
    }

    private void clickOnBitControl(int id) {
        Layer layer = controller.getLayer();
        Vector2 direction = null;

        // Every directions are in the bit's local coordinate system
        switch (id) {
            case 0: // Top direction
                direction = new Vector2(0, -1);
                break;
            case 1: // Left direction
                direction = new Vector2(1, 0);
                break;
            case 2: // Bottom direction
                direction = new Vector2(0, 1);
                break;
            case 3: // Right direction
                direction = new Vector2(-1, 0);
                break;
        }
        // Move all selected bits
        controller.setSelectedBitKeys(layer.moveBits(controller.getSelectedBits(), direction));
    }

    private void paintBitControls(Bit3D bit, Graphics2D g2d) {
        bitMovers.put(bit, new BitControls(bit, g2d));
    }

    private void drawModelArea(Graphics2D g2d, Area area) {
        area.transform(realToView);

        g2d.fill(area);
        g2d.draw(area);
    }

    private void drawModelCircle(Graphics2D g2d, Vector2 center, int radius) {
        Ellipse2D liftPoint = new Ellipse2D.Double(
                center.x - (radius >> 1),
                center.y - (radius >> 1),
                radius, radius);
        g2d.draw(realToView.createTransformedShape(liftPoint));
    }

    private void drawModelPath2D(Graphics2D g2d, Path2D path) {
        g2d.draw(path.createTransformedShape(realToView));
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e)) {
            Mesh mesh = controller.getCurrentMesh();

            if ((mesh != null) && mesh.isPaved()) {
                // Get the clicked point in the right coordinate system
                Point2D.Double clickSpot = new Point2D.Double(e.getX(), e.getY());
                viewToReal.transform(clickSpot, clickSpot);
                if (controller.isOnAddingBits()) {
                    controller.addNewBitAt(clickSpot);
                    return;
                }

                // Look if we hit a bit control (arrows)
                for (BitControls controls : bitMovers.values()) {
                    for (int i = 0; i < controls.size(); i++) {
                        if (controls.get(i).contains(oldX, oldY)) {
                            bitMovers.clear();
                            clickOnBitControl(i);
                            return;
                        }
                    }
                }

                // Look for a bit which contains the clicked spot
                controller.addOrRemoveSelectedBitKeys(controller.findBitAt(clickSpot));
            }
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (SwingUtilities.isMiddleMouseButton(e) || SwingUtilities.isLeftMouseButton(e)) {

            viewOffsetX += (e.getX() - oldX) / drawScale;
            viewOffsetY += (e.getY() - oldY) / drawScale;
            repaint();
        }
        oldX = e.getX();
        oldY = e.getY();
    }

    @Override
    public void mouseEntered(MouseEvent arg0) {
    }

    @Override
    public void mouseExited(MouseEvent arg0) {
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        oldX = e.getX();
        oldY = e.getY();
        if (controller.isOnAddingBits())
            repaint();
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (SwingUtilities.isRightMouseButton(e)) {
            rightClickPressed = true;
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (SwingUtilities.isRightMouseButton(e)) {
            rightClickPressed = false;
        }
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        if (!rightClickPressed) {
            double notches = e.getPreciseWheelRotation();
            if (!onControl) {
                // Zoom on the view
                double zoom = controller.getZoom();
                if (notches > 0) {
                    zoom = zoom / 1.25;
                } else {
                    zoom = zoom * 1.25;
                }
                controller.setZoom(zoom);
            } else {
                // Rotate the bit preview
                if (controller.isOnAddingBits()) {
                    controller.incrementBitsOrientationParamBy(notches * WorkspaceConfig.rotationSpeed);
                }
                // Rotate the selected bits
                if (controller.getSelectedBitKeys().size() > 0) {
                    controller.rotateSelectedBitsBy(notches * WorkspaceConfig.rotationSpeed);
                }
            }
        } else {
            // Navigate through layers when right click pressed
            controller.setLayer(controller.getCurrentLayerNumber() + e.getWheelRotation());
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        requestFocusInWindow();

        setDefaultZoom();
        updateDrawScale();

        realToView = calculateAffineTransformRealToView();
        viewToReal = calculateAffineTransformViewToReal();

        Graphics2D g2d = (Graphics2D) g;
        Mesh currentMesh = controller.getCurrentMesh();
        if (currentMesh == null) return;

        // If mesh is only sliced (layers not generated yet), draw the slices
        if (!currentMesh.isPaved()) {
            g2d.setColor(Color.BLACK);
            g2d.setStroke(new BasicStroke(1.0f));
            paintSlice(currentMesh, g2d);
        }

        // If layers are generated, draw the pavements
        else {

            // Draw previous layer
            if (controller.showPreviousLayer() && (controller.getCurrentLayerNumber() > 0)) {
                paintPreviousLayer(g2d);
            }

            // Draw bits
            Layer currentLayer = controller.getLayer();
            paintBits(currentMesh, currentLayer, g2d);

            // Draw the slices contained in the layer
/*
			if (controller.showSlices()) {
				paintSlicesInTheSameLayer(currentLayer, g2d);
			}
*/

            // Draw the border of layer
            if (controller.showSlice()) {
                g2d.setColor(Color.RED);
                g2d.setStroke(new BasicStroke(1.0f));
                paintLayerBorder(g2d);
            }

            // Draw the controls of the selected bit
            bitMovers.clear();
            if (!controller.getSelectedBitKeys().isEmpty())
                controller.getSelectedBits()
                        .forEach(bit -> this.paintBitControls(bit, g2d));

            // Draw the preview of adding bits
            if (controller.isOnAddingBits()) {
                paintBitPreview(g2d);
            }
        }
    }

    private void paintBitPreview(Graphics2D g2d) {
        Rectangle2D.Double r = new Rectangle2D.Double(
                -CraftConfig.bitLength / 2, -CraftConfig.bitWidth / 2,
                controller.newBitsLengthParam.getCurrentValue(),
                controller.newBitsWidthParam.getCurrentValue());
        Area bitBorder = new Area(r);
        // Transform into current view
        AffineTransform affineTransform = new AffineTransform();
        affineTransform.translate(oldX, oldY);
        Vector2 lOrientation = Vector2.getEquivalentVector(
                controller.newBitsOrientationParam.getCurrentValue());
        affineTransform.rotate(lOrientation.x, lOrientation.y);
        affineTransform.scale(drawScale, drawScale);
        bitBorder.transform(affineTransform);

        Point2D.Double realSpot = new Point2D.Double(oldX, oldY);
        viewToReal.transform(realSpot, realSpot);
        Area availableBitArea = controller.getAvailableBitAreaAt(realSpot); // in real pos
        boolean irregular = DetectorTool.checkIrregular(availableBitArea);
        // Fit into view
        availableBitArea.transform(realToView);
        // Change color based on irregularity
        if (!irregular) {
            // Draw border
            g2d.setColor(Color.BLUE.darker());
            g2d.setStroke(new BasicStroke(1.1f));
            g2d.draw(bitBorder);
            // Draw internal area
            g2d.setColor(new Color(164, 180, 200, 100));
            g2d.fill(availableBitArea);
        } else {
            // Draw border
            g2d.setColor(Color.RED.darker());
            g2d.setStroke(new BasicStroke(1.1f));
            g2d.draw(bitBorder);
            // Draw internal area
            g2d.setColor(new Color(250, 0, 100, 100));
            g2d.fill(availableBitArea);
        }
    }

    private AffineTransform calculateAffineTransformViewToReal() {
        AffineTransform a = new AffineTransform();
        a.translate(-viewOffsetX, -viewOffsetY);
        a.scale(1 / drawScale, 1 / drawScale);
        a.translate(-this.getWidth() >> 1, -this.getHeight() >> 1);
        return a;
    }

    private AffineTransform calculateAffineTransformRealToView() {
        AffineTransform a = new AffineTransform();
        a.translate(this.getWidth() >> 1, this.getHeight() >> 1);
        a.scale(drawScale, drawScale);
        a.translate(viewOffsetX, viewOffsetY);
        return a;
    }

    private void paintSlice(Mesh mesh, Graphics2D g2d) {
        Slice slice = mesh.getSlices().get(controller.getCurrentSliceNumber());
        for (Polygon p : slice) {
            drawModelPath2D(g2d, p.toPath2D());
        }
    }

    private void paintLayerBorder(Graphics2D g2d) {
        Slice slice = controller.getLayer().getHorizontalSection();
        for (Polygon p : slice) {
            drawModelPath2D(g2d, p.toPath2D());
        }
    }

    @SuppressWarnings("unused")
    @Deprecated
    private void paintSlicesInTheSameLayer(Layer layer, Graphics2D g2d) {
        g2d.setStroke(new BasicStroke(0.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
        g2d.setColor(new Color(100 + (155 / layer.getSlices().size()), 50, 0));
        for (int i = 0; i < layer.getSlices().size(); i++) {
            if (i == layer.getSliceToSelect()) {
                // Set the selected slice of the layer in blue
                Stroke dashed = new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{2},
                        0);
                g2d.setStroke(dashed);
                g2d.setColor(Color.blue);
            } else {
                // Set the other slices in different red to differentiate them from each other
                g2d.setStroke(new BasicStroke(0.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
                g2d.setColor(new Color(100 + ((i + 1) * (155 / layer.getSlices().size())), 50, 0));
            }

            for (Polygon p : layer.getSlices().get(i)) {
                drawModelPath2D(g2d, p.toPath2D());
            }
        }
    }

    private void paintBits(Mesh mesh, Layer layer, Graphics2D g2d) {
        Vector<Vector2> bitKeys = layer.getBits3dKeys();
        // Get all the irregular bits (bitKey in fact) in this layer
        List<Vector2> irregularBitsOfThisLayer = mesh.getIrregularBitKeysOf(layer);

        for (Vector2 b : bitKeys) { // Draw each bits
            Bit3D bit = layer.getBit3D(b);
            Area area = bit.getRawArea(); // Get the area of the bit
            AffineTransform affTrans = new AffineTransform();
            affTrans.translate(b.x, b.y);
            affTrans.rotate(bit.getOrientation().x, bit.getOrientation().y);
            area.transform(affTrans); // Put the bit's area at the right place

            // Color irregular bits
            g2d.setColor(new Color(164, 180, 200, 200));
            if (controller.showIrregularBits() && irregularBitsOfThisLayer.contains(b)) {
                g2d.setColor(new Color(255, 0, 0, 100));
            }
            // Draw the bit's area
            drawModelArea(g2d, area);

            // Draw the cut path
            if (controller.showCutPaths() && (bit.getCutPaths() != null)) {
                g2d.setColor(Color.blue.darker());
                g2d.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
                for (Path2D p : bit.getCutPaths()) {
                    Path2D path = (Path2D) p.clone();
                    path.transform(affTrans);
                    drawModelPath2D(g2d, path);
                }
            }

            // Draw the lift points path if checkbox is checked
            if (controller.showLiftPoints()) {
                g2d.setColor(Color.red);
                g2d.setStroke(new BasicStroke(0.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
                for (Vector2 liftPoint : bit.getLiftPoints()) {
                    if (liftPoint != null) {
                        Point2D point = new Point2D.Double();
                        affTrans.transform(new Point2D.Double(liftPoint.x, liftPoint.y), point);
                        drawModelCircle(g2d, new Vector2(point.getX(), point.getY()), (int) CraftConfig.suckerDiameter);
                    }
                }
            }

        }
    }

    /**
     * Draw the outline of the layer below the current showing one
     *
     * @param g2d graphic
     */
    private void paintPreviousLayer(Graphics2D g2d) {

        Layer previousLayer = controller.getCurrentMesh().getLayers()
                .get(controller.getCurrentLayerNumber() - 1);
        Vector<Vector2> previousBitKeys = previousLayer.getBits3dKeys();

        g2d.setColor(Color.DARK_GRAY);
        g2d.setStroke(new BasicStroke(0.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));

        for (Vector2 b : previousBitKeys) {

            Bit3D bit = previousLayer.getBit3D(b);
            Area area = bit.getRawArea();
            AffineTransform affTrans = new AffineTransform();
            affTrans.translate(b.x, b.y);
            affTrans.rotate(bit.getOrientation().x, bit.getOrientation().y);

            area.transform(affTrans);

            area.transform(realToView);

            g2d.draw(area);
        }
    }

    @Override
    public void update(Observable o, Object arg) {
        revalidate();
        repaint();
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        onControl = e.isControlDown();
    }

    @Override
    public void keyReleased(KeyEvent e) {
        onControl = false;
    }

    private class TriangleShape extends Path2D.Double {
        private static final long serialVersionUID = -147647250831261196L;

        private TriangleShape(Point2D... points) {
            moveTo(points[0].getX(), points[0].getY());
            lineTo(points[1].getX(), points[1].getY());
            lineTo(points[2].getX(), points[2].getY());
            closePath();
        }
    }

    private class BitControls extends Vector<Area> {
        BitControls(Bit3D bit, Graphics2D g2d) {
            // Defining the shape of the arrows
            TriangleShape triangleShape = new TriangleShape(new Point2D.Double(0, 0),
                    new Point2D.Double(-7, 10),
                    new Point2D.Double(7, 10));

            int padding = 15; // Space between bit and arrows

            Area overlapBit = new Area(new Rectangle2D.Double(-CraftConfig.bitLength / 2, -CraftConfig.bitWidth / 2,
                    CraftConfig.bitLength, CraftConfig.bitWidth));
            Vector<Area> areas = new Vector<>();
            areas.add(overlapBit);

            AffineTransform affTrans = new AffineTransform();

            Area topArrow = new Area(triangleShape);
            affTrans.translate(0, -padding - (CraftConfig.bitWidth / 2));
            affTrans.rotate(0, 0);
            topArrow.transform(affTrans);
            areas.add(topArrow);
            this.add(topArrow);

            Area leftArrow = new Area(triangleShape);
            affTrans = new AffineTransform();
            affTrans.translate(padding + (CraftConfig.bitLength / 2), 0);
            affTrans.rotate(0, 1);
            leftArrow.transform(affTrans);
            areas.add(leftArrow);
            this.add(leftArrow);

            Area bottomArrow = new Area(triangleShape);
            affTrans = new AffineTransform();
            affTrans.translate(0, padding + (CraftConfig.bitWidth / 2));
            affTrans.rotate(-1, 0);
            bottomArrow.transform(affTrans);
            areas.add(bottomArrow);
            this.add(bottomArrow);

            Area rightArrow = new Area(triangleShape);
            affTrans = new AffineTransform();
            affTrans.translate(-padding - (CraftConfig.bitLength / 2), 0);
            affTrans.rotate(0, -1);
            rightArrow.transform(affTrans);
            areas.add(rightArrow);
            this.add(rightArrow);

            for (Area area : areas) {
                affTrans = new AffineTransform();
                affTrans.translate(bit.getOrigin().x, bit.getOrigin().y);
                affTrans.rotate(bit.getOrientation().x, bit.getOrientation().y);
                area.transform(affTrans);

                area.transform(realToView);

                g2d.setColor(new Color(94, 125, 215));
                if (!area.equals(overlapBit)) {
                    g2d.draw(area);
                    g2d.fill(area);
                }
            }

            g2d.setStroke(new BasicStroke(0.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
            g2d.setColor(new Color(94, 125, 215));
            g2d.draw(overlapBit);

            g2d.setColor(new Color(0, 114, 255, 50));
            g2d.fill(overlapBit);
        }
    }
}