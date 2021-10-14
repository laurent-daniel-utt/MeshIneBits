/*
 * MeshIneBits is a Java software to disintegrate a 3d mesh (model in .stl)
 * into a network of standard parts (called "Bits").
 *
 * Copyright (C) 2016-2021 DANIEL Laurent.
 * Copyright (C) 2016  CASSARD Thibault & GOUJU Nicolas.
 * Copyright (C) 2017-2018  TRAN Quoc Nhat Han.
 * Copyright (C) 2018 VALLON Benjamin.
 * Copyright (C) 2018 LORIMER Campbell.
 * Copyright (C) 2018 D'AUTUME Christian.
 * Copyright (C) 2019 DURINGER Nathan (Tests).
 * Copyright (C) 2020 CLARIS Etienne & RUSSO André.
 * Copyright (C) 2020-2021 DO Quang Bao.
 * Copyright (C) 2021 VANNIYASINGAM Mithulan.
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

import meshIneBits.Bit2D;
import meshIneBits.Bit3D;
import meshIneBits.artificialIntelligence.DebugTools;
import meshIneBits.Layer;
import meshIneBits.Mesh;
import meshIneBits.config.CraftConfig;
import meshIneBits.config.WorkspaceConfig;
import meshIneBits.gui.utilities.IconLoader;
import meshIneBits.slicer.Slice;
import meshIneBits.util.*;
import meshIneBits.util.Polygon;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;

/**
 * Sketch of slice and layer. Observes the {@link MeshController}.
 */
class MeshWindowCore extends JPanel implements MouseMotionListener, MouseListener, MouseWheelListener, KeyListener, Observer {
    private double viewOffsetX, viewOffsetY;
    private final Map<Bit3D, BitControls> bitMovers = new HashMap<>();
    private int oldX, oldY;
    private boolean rightClickPressed = false;
    private double defaultZoom = 1;
    private double drawScale = 1;
    private boolean onControl;
    private AffineTransform realToView;
    private AffineTransform viewToReal;
    private MeshController meshController;
    private boolean onShift;

    private CustomLogger logger = new CustomLogger(this.getClass());

    MeshWindowCore(MeshController meshController) {

        this.meshController = meshController;
        this.meshController.addObserver(this);

        setOpaque(false);
        this.setLayout(new BorderLayout());
        initBackground();
        // Actions listener
        addMouseMotionListener(this);
        addMouseListener(this);
        addMouseWheelListener(this);
        addKeyListener(this);
        setupKeyBindings();
        setFocusable(true);



    }

    void initBackground() {
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
        getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "ACCEPT");
        getActionMap().put("ACCEPT", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                meshController.closeSelectedRegion();
            }
        });
        getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_Z,InputEvent.CTRL_MASK),"UNDO");
        getActionMap().put("UNDO", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                meshController.undo();
            }
        });
        getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_Y,InputEvent.CTRL_MASK),"REDO");
        getActionMap().put("REDO", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                meshController.redo();
            }
        });
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
//        if ((e.getKeyCode() == KeyEvent.VK_Z) && ((e.getModifiers() & KeyEvent.CTRL_MASK) != 0)) {
//            meshController.undo();
//        }
//        if((e.getKeyCode() == KeyEvent.VK_Y) && ((e.getModifiers() & KeyEvent.CTRL_MASK) != 0)){
//            meshController.redo();
//        }
        onControl = e.isControlDown();
//        meshController.setAddingBits(e.isShiftDown());
    }

    @Override
    public void keyReleased(KeyEvent e) {
        onControl = false;
        if(e.getKeyChar()==KeyEvent.SHIFT_MASK) {
//            meshController.setAddingBits(false);
//            repaint();
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e)) {
            Layer layer = meshController.getCurrentLayer();
            if (layer == null) return;

            // Get the clicked point in the Mesh coordinate system
            Point2D.Double clickSpot = new Point2D.Double(e.getX(), e.getY());
            viewToReal.transform(clickSpot, clickSpot);

            if (meshController.isSelectingRegion()) {
                meshController.addNewRegionVertex(clickSpot);
                return;
            }

            if (layer.isPaved()) {
                if (meshController.isAddingBits()) {
                    meshController.addNewBitAt(clickSpot);
                    return;
                }

                // Look if we hit a bit control (arrows)
                for (BitControls controls : bitMovers.values()) {
                    for (int i = 0; i < controls.size(); i++) {
                        if (controls.get(i).contains(oldX, oldY)) {
                            bitMovers.clear();
                            onClickedBitControl(i);
                            return;
                        }
                    }
                }

                // Look for a bit which contains the clicked spot
                meshController.toggleInclusionOfBitHaving(clickSpot);
            }
        }
    }

    private void onClickedBitControl(int id) {
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
        meshController.moveSelectedBits(direction);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (SwingUtilities.isRightMouseButton(e)) {
            rightClickPressed = true;
            if (!meshController.isAddingBits()
                    || !meshController.isSelectingRegion()) {
                // We can only bulk select when not adding bits or selecting region
                meshController.startBulkSelect(viewToReal.transform(e.getPoint(), null));
            }
        }
//        if(SwingUtilities.isLeftMouseButton(e)){
//            Point2D.Double clickSpot = new Point2D.Double(e.getX(), e.getY());
//            viewToReal.transform(clickSpot, clickSpot);
//            meshController.toggleInclusionOfBitHaving(clickSpot);
//        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (SwingUtilities.isRightMouseButton(e)) {
            rightClickPressed = false;
            if (!meshController.getBulkSelectZone().isEmpty())
                meshController.retrieveBulkSelectedBits();
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
        if(onShift&&SwingUtilities.isLeftMouseButton(e)){

        }
        else if (SwingUtilities.isLeftMouseButton(e)) {
            // Move the clip around
            viewOffsetX += (e.getX() - oldX) / drawScale;
            viewOffsetY += (e.getY() - oldY) / drawScale;
            repaint();
        } else if (rightClickPressed
                && meshController.isBulkSelecting()) {
            meshController.updateBulkSelect(viewToReal.transform(e.getPoint(), null));
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

                // Get the clicked point in the Mesh coordinate system
                Point2D.Double clickSpot = new Point2D.Double(e.getX(), e.getY());
                viewToReal.transform(clickSpot, clickSpot);

                double zoom = meshController.getZoom();
                double oldZoom = meshController.getZoom();

                //When reducing the size
                if (notches > 0) {
                    zoom /= WorkspaceConfig.zoomSpeed;
                    meshController.setZoom(zoom);

                    if (zoom > 1){
                        viewOffsetX = (clickSpot.x -clickSpot.x  * zoom)/zoom;
                        viewOffsetY = (clickSpot.y -clickSpot.y  * zoom )/zoom;
                    }
                    else {
                        //centre the view.
                        viewOffsetX= 0;
                        viewOffsetY= 0;
                    }


                }
                //When increasing the size
                else {
                    zoom *= WorkspaceConfig.zoomSpeed;
                    meshController.setZoom(zoom);

                    if (zoom > 1){
                        viewOffsetX = (clickSpot.x - clickSpot.x * zoom)/zoom;
                        viewOffsetY = (clickSpot.y - clickSpot.y * zoom)/zoom;
                    }
                    else {
                        //centre the view.
                        viewOffsetX= 0;
                        viewOffsetY= 0;
                    }
                }
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
//        SwingUtilities.invokeLater(()->{
            if (meshController.getMesh() != null) {
                removeAll();
                revalidate();
                repaint();
            }
//        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        logger.logDEBUGMessage("start");
        super.paintComponent(g);
        Mesh mesh = meshController.getMesh();
        if (mesh == null) return;
        if (meshController.getCurrentLayer() == null) return;

        requestFocusInWindow();

        // Change cursor on paving region
        if (meshController.isSelectingRegion()) {
            if (getCursor().getType() != Cursor.CROSSHAIR_CURSOR)
                setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
        } else {
            if (getCursor().getType() != Cursor.DEFAULT_CURSOR)
                setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        }

        setDefaultZoom();
        updateDrawScale();

        realToView = calculateAffineTransformRealToView();
        viewToReal = calculateAffineTransformViewToReal();

        Graphics2D g2d = (Graphics2D) g;

        // If current layer is only sliced (not paved yet), draw the slice
        if (!meshController.getCurrentLayer().isPaved()) {
            paintLayerBorder(g2d);
        } else {
            // Draw the border of layer
            if (meshController.showingSlice()) {
                paintLayerBorder(g2d);
            }

            // Draw bits
            paintBits(g2d);

            // Draw the controls of the selected bit
            bitMovers.clear();
            if (!meshController.getSelectedBitKeys().isEmpty())
                meshController.getSelectedBits()
                        .forEach(bit -> paintBitControls(bit, g2d));

            // Draw the preview of adding bits
            if (meshController.isAddingBits()) {
                paintBitPreview(g2d);
            }

            if (meshController.isBulkSelecting()) {
                paintBulkSelectZone(g2d);
            }
        }
        if (meshController.AI_NeedPaint) {
            AIpaintForDebug(g2d);
        }
        // Draw selected region
        if (meshController.isSelectingRegion()
                || meshController.hasSelectedRegion())
            paintSelectedRegion(g2d);

        // Draw previous layer
        if (meshController.showingPreviousLayer() && (meshController.getLayerNumber() > 0)) {
            paintPreviousLayer(g2d);
        }
        logger.logDEBUGMessage("End");
    }


    private void paintBulkSelectZone(Graphics2D g2d) {
        g2d.setColor(WorkspaceConfig.bulkSelectZoneColor);
        g2d.setStroke(WorkspaceConfig.bulkSelectZoneStroke);
        Shape bulkSelectZoneInView = realToView.createTransformedShape(meshController.getBulkSelectZone());
        g2d.fill(bulkSelectZoneInView);
    }

    private void paintSelectedRegion(Graphics2D g2d) {
        // Paint vertices
        g2d.setColor(WorkspaceConfig.vertexColor);
        meshController.getRegionVertices().forEach(p -> {

            Point2D.Double pInView = new Point2D.Double(); // Init with real
            realToView.transform(p, pInView); // Transform to view
            g2d.fill(new Rectangle2D.Double(
                    pInView.x - (WorkspaceConfig.vertexRadius >> 1),
                    pInView.y - (WorkspaceConfig.vertexRadius >> 1),
                    WorkspaceConfig.vertexRadius,
                    WorkspaceConfig.vertexRadius
            ));
        });
        // Paint polygon
        g2d.setColor(WorkspaceConfig.regionColor);
        g2d.setStroke(WorkspaceConfig.regionStroke);
        g2d.draw(realToView.createTransformedShape(meshController.getCurrentSelectedRegion()));
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
        if (!previousLayer.isPaved()) return;
        Vector<Vector2> previousLayerBitKeys = previousLayer.getBits3dKeys();

        g2d.setColor(WorkspaceConfig.previousLayerColor);
        g2d.setStroke(new BasicStroke(0.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));

        for (Vector2 b : previousLayerBitKeys) {

            Bit3D bit = previousLayer.getBit3D(b);
            Area area = bit.getBaseBit().getArea();
            area.transform(realToView);

            g2d.draw(area);
        }
    }

    private void paintBits(Graphics2D g2d) {
        Layer layer = meshController.getCurrentLayer();
        if (layer == null
                || layer.getFlatPavement() == null)
            return;
        Vector<Vector2> bitKeys = layer.getBits3dKeys();

        for (Vector2 bitKey : bitKeys) {
            Bit3D bit3D = layer.getBit3D(bitKey);
            Bit2D bit2D = bit3D.getBaseBit();
            // Draw each bits

            // Area
            if (meshController.showingIrregularBits()
                    && bit3D.isIrregular())
                g2d.setColor(WorkspaceConfig.irregularBitColor);
            else if(meshController.showingBitNotFull()&&!bit2D.isFullLength()){
                g2d.setColor(WorkspaceConfig.bitNotFullLength);
            }else  g2d.setColor(WorkspaceConfig.regularBitColor);
            if (bit2D.isUsedForNN())
                g2d.setColor(WorkspaceConfig.forAI_BitColor);
            drawModelArea(g2d, bit2D.getArea());

            // Cut paths
            Vector<Path2D> cutpaths = bit2D.getCutPaths();
            if (meshController.showingCutPaths()
                    && (cutpaths != null)) {
                g2d.setColor(WorkspaceConfig.cutpathColor);
                g2d.setStroke(WorkspaceConfig.cutpathStroke);
                cutpaths.forEach(path2D -> drawModelPath2D(g2d, path2D));
            }

            // Lift points
            if (meshController.showingLiftPoints()) {
                g2d.setColor(WorkspaceConfig.liftpointColor);
                g2d.setStroke(WorkspaceConfig.liftpointStroke);
                if (!bit3D.getLiftPoints().isEmpty())
                    bit3D.getLiftPoints()
                            .forEach(liftPoint ->
                                    drawModelCircle(g2d,
                                            liftPoint.x,
                                            liftPoint.y,
                                            (int) CraftConfig.suckerDiameter));
                g2d.setColor(Color.black);
                if(!bit3D.getTwoDistantPointsInMeshCoordinate().isEmpty()){
                    for(Vector2 point : bit3D.getTwoDistantPointsInMeshCoordinate()){
                        drawModelCircle(g2d,point.x,point.y,(int) CraftConfig.suckerDiameter/4);
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

    private void AIpaintForDebug(Graphics2D g2d) {
        //STROKE COMMANDS
        g2d.setStroke(new BasicStroke(2f));
        g2d.setColor(Color.RED);

        //Draw Polygon
        /*
        drawModelPath2D(g2d,meshController.ai_Tool.dataPrep.poly.toPath2D());
         */

        //Draw bits Areas
        Vector<Bit2D> bits = DebugTools.Bits;
        for (Bit2D bit : bits) {
            Area area = bit.getArea();
            area.transform(realToView);
            g2d.setColor(Color.RED);
            g2d.fill(area);
            g2d.setColor(Color.DARK_GRAY);
            g2d.draw(area);
        }

        //Draw an area
        if(DebugTools.areaToDraw!=null){
            Area area = DebugTools.areaToDraw;
            area.transform(realToView);
            g2d.setColor(Color.BLUE);
            g2d.fill(area);
            g2d.setColor(Color.DARK_GRAY);
            g2d.draw(area);
        }

        //Draw points
        for (Vector2 point : DebugTools.pointsToDrawRED) {
            g2d.setColor(Color.red);
            drawModelCircle(g2d, point.x, point.y, 4);
        }
        for (Vector2 point : DebugTools.pointsToDrawGREEN) {
            g2d.setColor(Color.green);
            drawModelCircle(g2d, point.x, point.y, 5);
        }
        for (Vector2 point : DebugTools.pointsToDrawBLUE) {
            g2d.setColor(Color.blue);
            drawModelCircle(g2d, point.x, point.y, 3);
        }


        //Draw Segment2D
        Path2D path = new GeneralPath();
        Segment2D seg = DebugTools.currentSegToDraw;
        Shape shape = new Line2D.Double(seg.start.x, seg.start.y, seg.end.x, seg.end.y);
        path.append(shape, false);
        drawModelPath2D(g2d, path);

        seg = DebugTools.currentSegToDraw2;
        shape = new Line2D.Double(seg.start.x, seg.start.y, seg.end.x, seg.end.y);
        path.append(shape, false);
        drawModelPath2D(g2d, path);

        //Draw a list of Segment2D
        for (Segment2D segment : DebugTools.segmentsToDraw) {
            shape = new Line2D.Double(segment.start.x, segment.start.y, segment.end.x, segment.end.y);
            path.append(shape, false);
            drawModelPath2D(g2d, path);
        }

        //Draw Text
        /*
        g2d.drawString("text",posX,posY);
         */
    }

    private void paintBitControls(Bit3D bit, Graphics2D g2d) {
        bitMovers.put(bit, new BitControls(bit, g2d));
    }

    private void paintBitPreview(Graphics2D g2d) {
        // Bit boundary
        Rectangle2D.Double r = new Rectangle2D.Double(
                -CraftConfig.lengthFull / 2,
                -CraftConfig.bitWidth / 2,
                meshController.getNewBitsLengthParam().getCurrentValue(),
                meshController.getNewBitsWidthParam().getCurrentValue());
        // Current position of cursor
        Point2D.Double currentSpot = new Point2D.Double(oldX, oldY); // In view
        viewToReal.transform(currentSpot, currentSpot); // In real
        meshController.setCurrentPoint(currentSpot);
        // Transform into current view
        AffineTransform originToCurrentSpot = new AffineTransform();
        originToCurrentSpot.translate(currentSpot.x, currentSpot.y);
        Vector2 lOrientation = Vector2.getEquivalentVector(
                meshController.getNewBitsOrientationParam().getCurrentValue());
        originToCurrentSpot.rotate(lOrientation.x, lOrientation.y);

        Shape bitPreviewInReal = originToCurrentSpot.createTransformedShape(r);
        Shape bitPreviewInView = realToView.createTransformedShape(bitPreviewInReal);
        Area sectionHolding = new Area(new Rectangle2D.Double(CraftConfig.lengthFull /2-CraftConfig.sectionHoldingToCut
                ,-CraftConfig.bitWidth/2
                ,CraftConfig.sectionHoldingToCut
                ,CraftConfig.bitWidth));
        sectionHolding.transform(originToCurrentSpot);
        sectionHolding.transform(realToView);

        Area availableBitArea = meshController.getAvailableBitAreaFrom(bitPreviewInReal); // in real pos
        boolean irregular = DetectorTool.checkIrregular(availableBitArea);
        // Fit into view
        availableBitArea.transform(realToView);
        // Change color based on irregularity
        if (!irregular) {
            if (!meshController.isFullLength()) {
                g2d.setColor(Color.DARK_GRAY);
                g2d.fill(sectionHolding);
            }
            // Draw border
            g2d.setColor(WorkspaceConfig.bitPreviewBorderColor);
            g2d.setStroke(WorkspaceConfig.bitPreviewBorderStroke);
            g2d.draw(bitPreviewInView);
            // Draw internal area
            g2d.setColor(WorkspaceConfig.bitPreviewColor);
            g2d.fill(availableBitArea);


        } else {
            // Draw border
            g2d.setColor(WorkspaceConfig.irregularBitPreviewBorderColor);
            g2d.setStroke(WorkspaceConfig.irregularBitPreviewBorderStroke);
            g2d.draw(bitPreviewInView);
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

    private void drawModelCircle(Graphics2D g2d, double x, double y, int radius) {
        Ellipse2D liftPoint = new Ellipse2D.Double(
                x - (radius >> 1),
                y - (radius >> 1),
                radius, radius);
        g2d.draw(realToView.createTransformedShape(liftPoint));
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
                            -CraftConfig.lengthFull / 2,
                            -CraftConfig.bitWidth / 2,
                            CraftConfig.lengthFull,
                            CraftConfig.bitWidth));
            overlapBit.transform(bit.getBaseBit().getTransfoMatrix());

            Vector<Area> arrows = new Vector<>();
            AffineTransform affTrans;

            Area topArrow = new Area(triangleShape);
            affTrans = new AffineTransform();
            affTrans.translate(0, -padding - (CraftConfig.bitWidth / 2));
            affTrans.rotate(0, 0);
            topArrow.transform(affTrans);
            arrows.add(topArrow);
            this.add(topArrow);

            Area leftArrow = new Area(triangleShape);
            affTrans = new AffineTransform();
            affTrans.translate(padding + (CraftConfig.lengthFull / 2), 0);
            affTrans.rotate(0, 1);
            leftArrow.transform(affTrans);
            arrows.add(leftArrow);
            this.add(leftArrow);

            Area bottomArrow = new Area(triangleShape);
            affTrans = new AffineTransform();
            affTrans.translate(0, padding + (CraftConfig.bitWidth / 2));
            affTrans.rotate(-1, 0);
            bottomArrow.transform(affTrans);
            arrows.add(bottomArrow);
            this.add(bottomArrow);

            Area rightArrow = new Area(triangleShape);
            affTrans = new AffineTransform();
            affTrans.translate(-padding - (CraftConfig.lengthFull / 2), 0);
            affTrans.rotate(0, -1);
            rightArrow.transform(affTrans);
            arrows.add(rightArrow);
            this.add(rightArrow);


            g2d.setColor(WorkspaceConfig.bitControlColor);
            affTrans = bit.getBaseBit().getTransfoMatrix();
            for (Area area : arrows) {
                area.transform(affTrans);
                area.transform(realToView);
                g2d.draw(area);
                g2d.fill(area);
            }

            g2d.setStroke(new BasicStroke(0.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
            g2d.setColor(new Color(94, 125, 215));
            overlapBit.transform(realToView);
            g2d.draw(overlapBit);

            g2d.setColor(new Color(0, 114, 255, 50));
            g2d.fill(overlapBit);
        }
    }


    public void setViewOffset(){
        viewOffsetX=0;
        viewOffsetY=0;
    }
}
