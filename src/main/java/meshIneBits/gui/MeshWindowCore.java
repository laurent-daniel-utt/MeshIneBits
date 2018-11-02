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

package meshIneBits.gui;

import meshIneBits.Bit3D;
import meshIneBits.Layer;
import meshIneBits.Mesh;
import meshIneBits.config.CraftConfig;
import meshIneBits.config.WorkspaceConfig;
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
 * Sketch of slice and layer. Observes the {@link MeshController}.
 */
class MeshWindowCore extends JPanel implements MouseMotionListener, MouseListener, MouseWheelListener, KeyListener, Observer {
    private double viewOffsetX, viewOffsetY;
    private Map<Bit3D, BitControls> bitMovers = new HashMap<>();
    private int oldX, oldY;
    private boolean rightClickPressed = false;
    private double defaultZoom = 1;
    private double drawScale = 1;
    private boolean onControl;
    private AffineTransform realToView;
    private AffineTransform viewToReal;
    private MeshController meshController;

    MeshWindowCore(MeshController meshController) {
        this.meshController = meshController;
        this.meshController.addObserver(this);

        this.setLayout(new BorderLayout());
        initBackground();
        // Actions listener
        addMouseMotionListener(this);
        addMouseListener(this);
        addMouseWheelListener(this);
        addKeyListener(this);
        setFocusable(true);
        setupKeyBindings();
    }

    private void initBackground() {
        Mesh mesh = meshController.getMesh();
        if (mesh == null || !mesh.isSliced()) {
            this.removeAll();
            JLabel background = new JLabel("", SwingConstants.CENTER);
            ImageIcon icon = IconLoader.get("MeshIneBitsAlpha.png", 645, 110);
            background.setIcon(icon);
            background.setFont(new Font(null, Font.BOLD | Font.ITALIC, 120));
            background.setForeground(new Color(0, 0, 0, 8));
            this.add(background, BorderLayout.CENTER);
        }
    }

    private void setupKeyBindings() {
        // Reset state when pressing Esc
        getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "RESET");
        getActionMap().put("RESET", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                meshController.reset();
            }
        });
        // Delete selected bits when pressing Del
        getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "DEL");
        getActionMap().put("DEL", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                meshController.deleteSelectedBits();
            }
        });
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

    @Override
    public void mouseClicked(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e)) {
            Mesh mesh = meshController.getMesh();

            if ((mesh != null) && mesh.isPaved()) {
                // Get the clicked point in the right coordinate system
                Point2D.Double clickSpot = new Point2D.Double(e.getX(), e.getY());
                viewToReal.transform(clickSpot, clickSpot);
                if (meshController.isAddingBits()) {
                    meshController.addNewBitAt(clickSpot);
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
                meshController.addOrRemoveSelectedBitKeys(meshController.findBitAt(clickSpot));
            }
        }
    }

    private void clickOnBitControl(int id) {
        Layer layer = meshController.getCurrentLayer();
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
        meshController.setSelectedBitKeys(layer.moveBits(meshController.getSelectedBits(), direction));
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
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

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
    public void mouseMoved(MouseEvent e) {
        oldX = e.getX();
        oldY = e.getY();
        if (meshController.isAddingBits())
            repaint();
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        if (!rightClickPressed) {
            double notches = e.getPreciseWheelRotation();
            if (!onControl) {
                // Zoom on the view
                double zoom = meshController.getZoom();
                if (notches > 0) {
                    zoom /= WorkspaceConfig.zoomSpeed;
                } else {
                    zoom *= WorkspaceConfig.zoomSpeed;
                }
                meshController.setZoom(zoom);
            } else {
                // Rotate the bit preview
                if (meshController.isAddingBits()) {
                    meshController.incrementBitsOrientationParamBy(notches * WorkspaceConfig.rotationSpeed);
                }
            }
        } else {
            // Navigate through layers when right click pressed
            meshController.setLayer(meshController.getLayerNumber() + e.getWheelRotation());
        }
    }

    @Override
    public void update(Observable o, Object arg) {
        if (meshController.getMesh() != null) {
            removeAll();
            revalidate();
            repaint();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Mesh mesh = meshController.getMesh();
        if (mesh == null) return;

        requestFocusInWindow();

        setDefaultZoom();
        updateDrawScale();

        realToView = calculateAffineTransformRealToView();
        viewToReal = calculateAffineTransformViewToReal();

        Graphics2D g2d = (Graphics2D) g;

        // If current layer is only sliced (not paved yet), draw the slice
        if (!meshController.getCurrentLayer().isPaved()) {
            paintLayerBorder(g2d);
        }

        // If layers are paved, draw the pavements
        else {

            // Draw previous layer
            if (meshController.showingPreviousLayer() && (meshController.getLayerNumber() > 0)) {
                paintPreviousLayer(g2d);
            }

            // Draw bits
            paintBits(g2d);

            // Draw the slices contained in the layer
/*
			if (meshController.showSlices()) {
				paintSlicesInTheSameLayer(currentLayer, g2d);
			}
*/

            // Draw the border of layer
            if (meshController.showingSlice()) {
                paintLayerBorder(g2d);
            }

            // Draw the controls of the selected bit
            bitMovers.clear();
            if (!meshController.getSelectedBitKeys().isEmpty())
                meshController.getSelectedBits()
                        .forEach(bit -> paintBitControls(bit, g2d));

            // Draw the preview of adding bits
            if (meshController.isAddingBits()) {
                paintBitPreview(g2d);
            }
        }
    }

    private void setDefaultZoom() {
        int width = this.getWidth();
        int height = this.getHeight();
        if (width > height)
            defaultZoom = height / (meshController.getMesh().getSkirtRadius() * 2);
        else
            defaultZoom = width / (meshController.getMesh().getSkirtRadius() * 2);
    }

    private void updateDrawScale() {
        drawScale = meshController.getZoom() * defaultZoom;
    }

    private AffineTransform calculateAffineTransformRealToView() {
        AffineTransform a = new AffineTransform();
        a.translate(this.getWidth() >> 1, this.getHeight() >> 1);
        a.scale(drawScale, drawScale);
        a.translate(viewOffsetX, viewOffsetY);
        return a;
    }

    private AffineTransform calculateAffineTransformViewToReal() {
        AffineTransform a = new AffineTransform();
        a.translate(-viewOffsetX, -viewOffsetY);
        a.scale(1 / drawScale, 1 / drawScale);
        a.translate(-this.getWidth() >> 1, -this.getHeight() >> 1);
        return a;
    }

    /**
     * Draw the outline of the layer below the current showing one
     *
     * @param g2d graphic
     */
    private void paintPreviousLayer(Graphics2D g2d) {
        Layer previousLayer = meshController
                .getMesh()
                .getLayers()
                .get(meshController.getLayerNumber() - 1);
        Vector<Vector2> previousLayerBitKeys = previousLayer.getBits3dKeys();

        g2d.setColor(WorkspaceConfig.previousLayerColor);
        g2d.setStroke(new BasicStroke(0.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));

        for (Vector2 b : previousLayerBitKeys) {

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

    private void paintBits(Graphics2D g2d) {
        Layer layer = meshController.getCurrentLayer();
        Vector<Vector2> bitKeys = layer.getBits3dKeys();
        // Get all the irregular bits (bitKey in fact) in this layer
        List<Vector2> irregularBitsOfThisLayer = layer.getIrregularBits();

        for (Vector2 b : bitKeys) { // Draw each bits
            Bit3D bit = layer.getBit3D(b);
            Area area = bit.getRawArea(); // Get the area of the bit
            AffineTransform affTrans = new AffineTransform();
            affTrans.translate(b.x, b.y);
            affTrans.rotate(bit.getOrientation().x, bit.getOrientation().y);
            area.transform(affTrans); // Put the bit's area at the right place

            // Color irregular bits
            g2d.setColor(WorkspaceConfig.regularBitColor);
            if (meshController.showingIrregularBits() && irregularBitsOfThisLayer.contains(b)) {
                g2d.setColor(WorkspaceConfig.irregularBitColor);
            }
            // Draw the bit's area
            drawModelArea(g2d, area);

            // Draw the cut path
            if (meshController.showingCutPaths() && (bit.getCutPaths() != null)) {
                g2d.setColor(WorkspaceConfig.cutpathColor);
                g2d.setStroke(WorkspaceConfig.cutpathStroke);
                for (Path2D p : bit.getCutPaths()) {
                    Path2D path = (Path2D) p.clone();
                    path.transform(affTrans);
                    drawModelPath2D(g2d, path);
                }
            }

            // Draw the lift points path if checkbox is checked
            if (meshController.showingLiftPoints()) {
                g2d.setColor(WorkspaceConfig.liftpointColor);
                g2d.setStroke(WorkspaceConfig.liftpointStroke);
                for (Vector2 liftPoint : bit.getLiftPoints()) {
                    if (liftPoint != null) {
                        Point2D point = new Point2D.Double();
                        affTrans.transform(new Point2D.Double(liftPoint.x, liftPoint.y), point);
                        drawModelCircle(g2d,
                                new Vector2(point.getX(), point.getY()),
                                (int) CraftConfig.suckerDiameter);
                    }
                }
            }
        }
    }

    private void paintLayerBorder(Graphics2D g2d) {
        g2d.setColor(WorkspaceConfig.layerBorderColor);
        g2d.setStroke(WorkspaceConfig.layerBorderStroke);
        Slice slice = meshController.getCurrentLayer().getHorizontalSection();
        for (Polygon p : slice) {
            drawModelPath2D(g2d, p.toPath2D());
        }
    }

    private void paintBitControls(Bit3D bit, Graphics2D g2d) {
        bitMovers.put(bit, new BitControls(bit, g2d));
    }

    private void paintBitPreview(Graphics2D g2d) {
        Rectangle2D.Double r = new Rectangle2D.Double(
                -CraftConfig.bitLength / 2,
                -CraftConfig.bitWidth / 2,
                meshController.getNewBitsLengthParam().getCurrentValue(),
                meshController.getNewBitsWidthParam().getCurrentValue());
        Area bitBorder = new Area(r);
        // Transform into current view
        AffineTransform affineTransform = new AffineTransform();
        affineTransform.translate(oldX, oldY);
        Vector2 lOrientation = Vector2.getEquivalentVector(
                meshController.getNewBitsOrientationParam().getCurrentValue());
        affineTransform.rotate(lOrientation.x, lOrientation.y);
        affineTransform.scale(drawScale, drawScale);
        bitBorder.transform(affineTransform);

        Point2D.Double realSpot = new Point2D.Double(oldX, oldY);
        viewToReal.transform(realSpot, realSpot);
        Area availableBitArea = meshController.getAvailableBitAreaAt(realSpot); // in real pos
        boolean irregular = DetectorTool.checkIrregular(availableBitArea);
        // Fit into view
        availableBitArea.transform(realToView);
        // Change color based on irregularity
        if (!irregular) {
            // Draw border
            g2d.setColor(WorkspaceConfig.bitPreviewBorderColor);
            g2d.setStroke(WorkspaceConfig.bitPreviewBorderStroke);
            g2d.draw(bitBorder);
            // Draw internal area
            g2d.setColor(WorkspaceConfig.bitPreviewColor);
            g2d.fill(availableBitArea);
        } else {
            // Draw border
            g2d.setColor(WorkspaceConfig.irregularBitPreviewBorderColor);
            g2d.setStroke(WorkspaceConfig.irregularBitPreviewBorderStroke);
            g2d.draw(bitBorder);
            // Draw internal area
            g2d.setColor(WorkspaceConfig.irregularBitPreviewColor);
            g2d.fill(availableBitArea);
        }
    }

    private void drawModelPath2D(Graphics2D g2d, Path2D path2D) {
        g2d.draw(path2D.createTransformedShape(realToView));
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

    @SuppressWarnings("unused")
    @Deprecated
    private void paintSlicesInTheSameLayer(Graphics2D g2d) {
        Layer layer = meshController.getCurrentLayer();
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

    private class TriangleShape extends Path2D.Double {

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
            TriangleShape triangleShape = new TriangleShape(
                    new Point2D.Double(0, 0),
                    new Point2D.Double(-7, 10),
                    new Point2D.Double(7, 10));

            int padding = WorkspaceConfig.paddingBitControl; // Space between bit and arrows

            Area overlapBit = new Area(
                    new Rectangle2D.Double(
                            -CraftConfig.bitLength / 2,
                            -CraftConfig.bitWidth / 2,
                            CraftConfig.bitLength,
                            CraftConfig.bitWidth));
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

                g2d.setColor(WorkspaceConfig.bitControlColor);
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
