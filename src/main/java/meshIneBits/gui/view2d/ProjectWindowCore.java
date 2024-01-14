/*
 * MeshIneBits is a Java software to disintegrate a 3d project (model in .stl)
 * into a network of standard parts (called "Bits").
 *
 * Copyright (C) 2016-2022 DANIEL Laurent.
 * Copyright (C) 2016  CASSARD Thibault & GOUJU Nicolas.
 * Copyright (C) 2017-2018  TRAN Quoc Nhat Han.
 * Copyright (C) 2018 VALLON Benjamin.
 * Copyright (C) 2018 LORIMER Campbell.
 * Copyright (C) 2018 D'AUTUME Christian.
 * Copyright (C) 2019 DURINGER Nathan (Tests).
 * Copyright (C) 2020-2021 CLAIRIS Etienne & RUSSO André.
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
 *
 */

package meshIneBits.gui.view2d;

import meshIneBits.*;
import meshIneBits.borderPaver.debug.drawDebug;
import meshIneBits.config.CraftConfig;
import meshIneBits.config.WorkspaceConfig;
import meshIneBits.gui.utilities.IconLoader;
import meshIneBits.slicer.Slice;
import meshIneBits.util.Polygon;
import meshIneBits.util.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;

import static meshIneBits.gui.view2d.ProjectWindowPropertyPanel.MousePropertyPanel;

/**
 * Sketch of slice and layer. Observes the {@link ProjectController}.
 */
public class ProjectWindowCore extends JPanel implements MouseMotionListener,
        MouseWheelListener, KeyListener,MouseListener, Observer  {

  private double viewOffsetX, viewOffsetY;
  private final Map<Bit3D, BitControls> bitMovers = new HashMap<>();
  private int oldX, oldY;
  private boolean rightClickPressed = false;
  private double defaultZoom = 1;
  private double drawScale = 1;
  private boolean onControl;
  public static AffineTransform realToView;
  public static AffineTransform viewToReal;

  public static ProjectController projectController;
  public static final String RESTORED="restored";

  private boolean controlPressed=false;
  private boolean onShift;

  private CustomLogger logger = new CustomLogger(this.getClass());
  private boolean rotating;
  private boolean Moving;
  private Bit3D rotatedBit;

  private Vector2 newOrigin=null;
  private Bit3D movedBit;



  private Vector2 translationInMesh;
  ProjectWindowCore(ProjectController projectController) {

    this.projectController = projectController;
    this.projectController.addObserver(this);

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
    Project project = projectController.getMesh();
    if (project == null || !project.isSliced()) {
      this.removeAll();
      JLabel background = new JLabel("", SwingConstants.CENTER);
      ImageIcon icon = IconLoader.get("MeshIneBitsAlpha.png", 645, 110);
      background.setIcon(icon);
      background.setFont(new Font(null, Font.BOLD | Font.ITALIC, 120));
      background.setForeground(new Color(0, 0, 0, 8));
      this.add(background, BorderLayout.CENTER);
    }
    this.validate();
    this.repaint();
  }

  private void setupKeyBindings() {
    // Reset state when pressing Esc
    getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "RESET");
    getActionMap().put("RESET", new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent e) {
        projectController.reset();
      }
    });
    // Delete selected bits when pressing Del
    getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "DEL");
    getActionMap().put("DEL", new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent e) {
        projectController.deleteSelectedBits();

        // projectController.deleteSelectedSubBits();
      }
    });
    getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "ACCEPT");
    getActionMap().put("ACCEPT", new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent e) {
        projectController.closeSelectedRegion();
      }
    });
    getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_MASK), "UNDO");
    getActionMap().put("UNDO", new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent e) {
        projectController.undo();
        projectController.getChanges().firePropertyChange(RESTORED, null, projectController.getCurrentLayer());
      }
    });

    getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, InputEvent.CTRL_MASK), "Del");
    getActionMap().put("Del", new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent e) {
        projectController.deleteSelectedSubBits();
      }
    });

    getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_MASK), "REDO");
    getActionMap().put("REDO", new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent e) {
        projectController.redo();
      }
    });
  }

  @Override
  public void keyTyped(KeyEvent e) {

  }

  public Vector2 getNewOrigin(){
    return  newOrigin;
  }

  @Override
  public void keyPressed(KeyEvent e) {
//        if ((e.getKeyCode() == KeyEvent.VK_Z) && ((e.getModifiers() & KeyEvent.CTRL_MASK) != 0)) {
//            projectController.undo();
//        }
//        if((e.getKeyCode() == KeyEvent.VK_Y) && ((e.getModifiers() & KeyEvent.CTRL_MASK) != 0)){
//            projectController.redo();
//        }
    if(e.getKeyCode()==KeyEvent.VK_ESCAPE && rotating){
      rotating=false;
      Point2D.Double position;
      if(newOrigin==null)position=new Point2D.Double(rotatedBit.getOrigin().x,rotatedBit.getOrigin().y);
      else {position=new Point2D.Double(newOrigin.x,newOrigin.y);}
      projectController.addNewBitAt(position,true,null,null);
      newOrigin=null;
    }
    if(e.getKeyCode()==KeyEvent.VK_ESCAPE && Moving){
      Moving=false;
      Point2D.Double position=new Point2D.Double(newOrigin.x,newOrigin.y);
      projectController.addNewBitAt(position,false,movedBit.getBaseBit().getOrientation(),newOrigin);
      newOrigin=null;
    }

    if (e.getKeyCode()==KeyEvent.VK_CONTROL)controlPressed=true;
    onControl = e.isControlDown();
//        projectController.setAddingBits(e.isShiftDown());
  }

  @Override
  public void keyReleased(KeyEvent e) {
    onControl = false;
    if (e.getKeyCode()==KeyEvent.VK_CONTROL)controlPressed=false;
    if (e.getKeyChar() == KeyEvent.SHIFT_MASK) {
//            projectController.setAddingBits(false);
//            repaint();
    }
  }


  public void mouseClicked(MouseEvent e) {
    if (SwingUtilities.isLeftMouseButton(e) && controlPressed==false) {
      Layer layer = projectController.getCurrentLayer();
      if (layer == null) {
        return;
      }

      // Get the clicked point in the Project coordinate system
      Point2D.Double clickSpot = new Point2D.Double(e.getX(), e.getY());
      viewToReal.transform(clickSpot, clickSpot);

      if (projectController.isSelectingRegion()) {
        projectController.addNewRegionVertex(clickSpot);
        return;
      }

      if (layer.isPaved()) {
        if (projectController.isAddingBits() && !projectController.manipulating ) {
          projectController.addNewBitAt(clickSpot,false,null,null);
          return;
        } else if (projectController.isAddingBits() && projectController.manipulating) {
          projectController.addNewBitAt(clickSpot,false,null,null);
          projectController.manipulating=false;
          projectController.setAddingBits(false);
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
        projectController.toggleInclusionOfBitHaving(clickSpot);
      }
    } else if (SwingUtilities.isLeftMouseButton(e) && controlPressed == true) {
      Layer layer = projectController.getCurrentLayer();
      if (layer == null) {
        return;
      }
      Point2D.Double clickSpot = new Point2D.Double(e.getX(), e.getY());
      viewToReal.transform(clickSpot, clickSpot);
      if (layer.isPaved()){
        projectController.toggleInclusionOfSubBitHaving(clickSpot);

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

    if(projectController.getSelectedBits().size()==1 && !Moving) {
      Iterator<Bit3D> it= projectController.getSelectedBits().iterator();
      movedBit=it.next();
      projectController.deleteSelectedBits();
      Moving=true;
      double distance = 0;
      if (direction.x == 0) {// up or down
        distance = CraftConfig.widthmover;
      } else if (direction.y == 0) {// left or right
        distance = CraftConfig.lengthmover;
      }
      Bit2D bitToMove2D=movedBit.getBaseBit();

      translationInMesh =
              direction.rotate(bitToMove2D.getOrientation())
                      .normal()
                      .mul(distance);
      double angle=Math.toRadians(movedBit.getBaseBit().getOrientation().getEquivalentAngle2());


      newOrigin = bitToMove2D.getOriginCS()
              .add(translationInMesh);
      //movedBit.getBaseBit().setOriginCS(newOrigin);
      projectController.updateCore();


    }
    else if(Moving==true && projectController.getSelectedBits().isEmpty()) {
      double distance = 0;
      if (direction.x == 0) {// up or down
        distance = CraftConfig.widthmover;
      } else if (direction.y == 0) {// left or right
        distance = CraftConfig.lengthmover;
      }
      Bit2D bitToMove2D=movedBit.getBaseBit();

      translationInMesh =
              direction.rotate(bitToMove2D.getOrientation())
                      .normal()
                      .mul(distance);

      newOrigin=newOrigin.add(translationInMesh);
      // movedBit.getBaseBit().setOriginCS(newOrigin);
      projectController.updateCore();

    }

  }


  public void mousePressed(MouseEvent e) {
    if (SwingUtilities.isRightMouseButton(e)) {
      rightClickPressed = true;
      if (!projectController.isAddingBits()
              || !projectController.isSelectingRegion()) {
        // We can only bulk select when not adding bits or selecting region
        projectController.startBulkSelect(viewToReal.transform(e.getPoint(), null));
      }
    }
//        if(SwingUtilities.isLeftMouseButton(e)){
//            Point2D.Double clickSpot = new Point2D.Double(e.getX(), e.getY());
//            viewToReal.transform(clickSpot, clickSpot);
//            projectController.toggleInclusionOfBitHaving(clickSpot);
//        }
  }


  public void mouseReleased(MouseEvent e) {
    if (SwingUtilities.isRightMouseButton(e)) {
      rightClickPressed = false;
      if (!projectController.getBulkSelectZone()
              .isEmpty()) {
        projectController.retrieveBulkSelectedBits();
      }
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
    if (onShift && SwingUtilities.isLeftMouseButton(e)) {

    } else if (SwingUtilities.isLeftMouseButton(e)) {
      // Move the clip around
      viewOffsetX += (e.getX() - oldX) / drawScale;
      viewOffsetY += (e.getY() - oldY) / drawScale;
      repaint();
    } else if (rightClickPressed
            && projectController.isBulkSelecting()) {
      projectController.updateBulkSelect(viewToReal.transform(e.getPoint(), null));
      repaint();
    }
    oldX = e.getX();
    oldY = e.getY();
  }

  @Override
  public void mouseMoved(MouseEvent e) {
    oldX = e.getX();
    oldY = e.getY();
    if (projectController.isAddingBits()) {
      repaint();
    }
    // getPropertiesOf();

    Ellipse2D liftPoint2 = new Ellipse2D.Double(
            e.getX() ,
            e.getY() ,
            1, 1);
    if(viewToReal!=null) {Shape s2=viewToReal.createTransformedShape(liftPoint2);
      MousePropertyPanel.updateProperties(s2.getBounds2D().getCenterX(),s2.getBounds2D().getCenterY());}

  }

  @Override
  public void mouseWheelMoved(MouseWheelEvent e) {
    if (!rightClickPressed) {
      double notches = e.getPreciseWheelRotation();
      if (!onControl) {

        // Get the clicked point in the Project coordinate system
        Point2D.Double clickSpot = new Point2D.Double(e.getX(), e.getY());
        viewToReal.transform(clickSpot, clickSpot);

        double zoom = projectController.getZoom();
        double oldZoom = projectController.getZoom();

        //When reducing the size
        if (notches > 0) {
          zoom /= WorkspaceConfig.zoomSpeed;
          projectController.setZoom(zoom);

          if (zoom > 1) {
            viewOffsetX = (clickSpot.x - clickSpot.x * zoom) / zoom;
            viewOffsetY = (clickSpot.y - clickSpot.y * zoom) / zoom;
          } else {
            //centre the view.
            viewOffsetX = 0;
            viewOffsetY = 0;
          }


        }
        //When increasing the size
        else {
          zoom *= WorkspaceConfig.zoomSpeed;
          projectController.setZoom(zoom);

          if (zoom > 1) {
            viewOffsetX = (clickSpot.x - clickSpot.x * zoom) / zoom;
            viewOffsetY = (clickSpot.y - clickSpot.y * zoom) / zoom;
          } else {
            //centre the view.
            viewOffsetX = 0;
            viewOffsetY = 0;
          }
        }
      } else {
        // Rotate the bit preview
        if (projectController.isAddingBits() ) {
          projectController.incrementBitsOrientationParamBy(notches * WorkspaceConfig.rotationSpeed);
        }
      }
    } else if(rightClickPressed) {
      // Navigate through layers when right click pressed
      projectController.setLayer(projectController.getLayerNumber() + e.getWheelRotation());
    }
    if(!projectController.getSelectedBits().isEmpty() && controlPressed) {
      if (projectController.getSelectedBits().size()==1){
        Iterator<Bit3D>it= projectController.getSelectedBits().iterator();
        //projectController.getCurrentLayer().removeBit(it.next(),true);
        rotatedBit=it.next();
        projectController.deleteSelectedBits();
        rotating=true;
        double notches = e.getPreciseWheelRotation();

        projectController.incrementSelectedBitsOrientationParamBy(notches*WorkspaceConfig.rotationSpeed);
      }
    }
    if(rotating || Moving) { double notches = e.getPreciseWheelRotation();
      rotating=true;
      if(Moving) rotatedBit=movedBit;
      Moving=false;

      projectController.incrementSelectedBitsOrientationParamBy(notches*WorkspaceConfig.rotationSpeed);
    }
  }

  @Override
  public void update(Observable o, Object arg) {
    SwingUtilities.invokeLater(() -> {
      if (projectController.getMesh() != null) {
        removeAll();
        revalidate();
        repaint();
      }
    });
  }

  @Override
  protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    Project project = projectController.getMesh();

    if (project == null) {
      return;
    }
    if (projectController.getCurrentLayer() == null) {
      return;
    }

    requestFocusInWindow();

    // Change cursor on paving region
    if (projectController.isSelectingRegion()) {
      if (getCursor().getType() != Cursor.CROSSHAIR_CURSOR) {
        setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
      }
    } else {
      if (getCursor().getType() != Cursor.DEFAULT_CURSOR) {
        setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
      }
    }

    setDefaultZoom();
    updateDrawScale();

    realToView = calculateAffineTransformRealToView();
    viewToReal = calculateAffineTransformViewToReal();

    Graphics2D g2d = (Graphics2D) g;
    RenderingHints rh=new RenderingHints(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
    g2d.setRenderingHints(rh);
    RenderingHints rs=new RenderingHints(RenderingHints.KEY_STROKE_CONTROL,RenderingHints.VALUE_STROKE_NORMALIZE);
    g2d.setRenderingHints(rs);
    // If current layer is only sliced (not paved yet), draw the slice
    if (!projectController.getCurrentLayer()
            .isPaved()) {
      paintLayerBorder(g2d);
    } else {
      // Draw the border of layer
      if (projectController.showingSlice()) {
        paintLayerBorder(g2d);
      }

      // Draw bits
      paintBits(g2d);

      // Draw the controls of the selected bit
      bitMovers.clear();
      if (!projectController.getSelectedBitKeys()
              .isEmpty() && !Moving ) {
        projectController.getSelectedBits()
                .forEach(bit -> {
                  paintBitControls(bit, g2d,null); });
      }
      if ( Moving && !rotating) {
        paintBitControls(movedBit, g2d,null);
      }
      if (!projectController.getSelectedSubBits()
              .isEmpty()) {
        projectController.getSelectedSubBits()
                .forEach(subbit -> {
                  if(!subbit.isRemoved()) {
                    paintBitControls(null, g2d,subbit);}
                });
      }
      // Draw the preview of adding bits
      if (projectController.isAddingBits()) {
        paintBitPreview(g2d);
      }
      if(rotating){
        if(newOrigin==null && rotatedBit!=null){
          paintBitPreviewRotation(g2d,rotatedBit.getOrigin());}
        else if (newOrigin!=null && rotatedBit!=null){  paintBitPreviewRotation(g2d,newOrigin);}
      }
      if (Moving){
        paintBitPreviewMoving(g2d,newOrigin,movedBit.getBaseBit());
      }
      if (projectController.isBulkSelecting()) {
        paintBulkSelectZone(g2d);
      }
      if (projectController.AI_NeedPaint) {
        AIpaintForDebug(g2d);
      }
    }
    // Draw selected region
    if (projectController.isSelectingRegion()
            || projectController.hasSelectedRegion()) {
      paintSelectedRegion(g2d);
    }
    // Draw previous layer
    if (projectController.showingPreviousLayer() && (projectController.getLayerNumber() > 0)) {
      paintPreviousLayer(g2d);
    }
  }


  private void paintBulkSelectZone(Graphics2D g2d) {
    g2d.setColor(WorkspaceConfig.bulkSelectZoneColor);
    g2d.setStroke(WorkspaceConfig.bulkSelectZoneStroke);
    Shape bulkSelectZoneInView = realToView.createTransformedShape(
            projectController.getBulkSelectZone());
    g2d.fill(bulkSelectZoneInView);
  }

  private void paintSelectedRegion(Graphics2D g2d) {
    // Paint vertices
    g2d.setColor(WorkspaceConfig.vertexColor);
    projectController.getRegionVertices()
            .forEach(p -> {

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
    g2d.draw(realToView.createTransformedShape(projectController.getCurrentSelectedRegion()));
  }

  private void setDefaultZoom() {
    int width = this.getWidth();
    int height = this.getHeight();
    if (width > height) {
      defaultZoom = height / (projectController.getMesh()
              .getSkirtRadius() * 2);
    } else {
      defaultZoom = width / (projectController.getMesh()
              .getSkirtRadius() * 2);
    }
  }

  private void updateDrawScale() {
    drawScale = projectController.getZoom() * defaultZoom;
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
    Layer previousLayer = projectController
            .getMesh()
            .getLayers()
            .get(projectController.getLayerNumber() - 1);
    if (!previousLayer.isPaved()) {
      return;
    }
    Vector<Vector2> previousLayerBitKeys = previousLayer.getBits3dKeys();

    g2d.setColor(WorkspaceConfig.previousLayerColor);
    g2d.setStroke(new BasicStroke(0.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));

    for (Vector2 b : previousLayerBitKeys) {

      Bit3D bit = previousLayer.getBit3D(b);
      Area area = bit.getBaseBit()
              .getAreaCS();
      area.transform(realToView);

      g2d.draw(area);
    }
  }

  private void paintBits(Graphics2D g2d) {
    Layer layer = projectController.getCurrentLayer();
    if (layer == null
            || layer.getFlatPavement() == null) {
      return;
    }
    Vector<Vector2> bitKeys = layer.getBits3dKeys();

    for (Vector2 bitKey : bitKeys) {
      Bit3D bit3D = layer.getBit3D(bitKey);
      Bit2D bit2D = bit3D.getBaseBit();

      // Draw each bits
      NewBit2D newBit2D = ((NewBit3D) bit3D).getBaseBit();
      Vector<SubBit2D> validSubBits = newBit2D.getValidSubBits();
      // Area
      if (projectController.showingIrregularBits()
        // && bit3D.isIrregular()
      )

      {showirregular(bit3D,layer,g2d);
        // g2d.setColor(WorkspaceConfig.irregularBitColor);
      }else if (projectController.showingBitNotFull() && !bit2D.isFullLength()) {
        g2d.setColor(WorkspaceConfig.bitNotFullLength);
      } else {
        g2d.setColor(WorkspaceConfig.regularBitColor);
      }
      if (bit2D.isUsedForNN()) {
        g2d.setColor(WorkspaceConfig.forAI_BitColor);
      }
      if(!projectController.showingIrregularBits()) {
        newBit2D.getSubBits().forEach(subBit2D ->{
            drawModelArea(g2d, subBit2D.getAreaCS());}
        );
      }

      // Cut paths
      Vector<Path2D> cutpaths = bit2D.getCutPathsCS();
      if (projectController.showingCutPaths()
              && (cutpaths != null)) {
        g2d.setColor(WorkspaceConfig.cutpathColor);
        g2d.setStroke(WorkspaceConfig.cutpathStroke);
        cutpaths.forEach(path2D -> drawModelPath2D(g2d, path2D));
      }

      // Lift points
      if (projectController.showingLiftPoints()) {
        g2d.setColor(WorkspaceConfig.liftpointColor);
        g2d.setStroke(WorkspaceConfig.liftpointStroke);
        if (!bit3D.getLiftPointsCS()
                .isEmpty()) {
          bit3D.getLiftPointsCS()
                  .forEach(liftPoint ->
                          drawModelCircle(g2d,
                                  liftPoint.x,
                                  liftPoint.y,
                                  (int) CraftConfig.suckerDiameter));
        }
        g2d.setColor(Color.black);
        if (!bit3D.getTwoDistantPointsCS()
                .isEmpty()) {
          for (Vector2 point : bit3D.getTwoDistantPointsCS()) {
            drawModelCircle(g2d, point.x, point.y, (int) CraftConfig.suckerDiameter / 4);
          }
        }
      }
    }

  }






  private void showirregular(Bit3D bit3D,Layer layer,Graphics2D g2d){
    Bit2D bit2D = bit3D.getBaseBit();
    NewBit2D newBit2D = ((NewBit3D) bit3D).getBaseBit();
    Vector<SubBit2D> SubBits = newBit2D.getSubBits();
    for(SubBit2D subBits:SubBits){
      if( !subBits.isregular()){
        g2d.setColor(Color.RED);
      }
      else {
        g2d.setColor(WorkspaceConfig.regularBitColor);
      }
      if(!subBits.isRemoved()) drawModelArea(g2d,subBits.getAreaCS());
    }
  }



  private void paintLayerBorder(Graphics2D g2d) {
    g2d.setColor(WorkspaceConfig.layerBorderColor);
    g2d.setStroke(WorkspaceConfig.layerBorderStroke);
    Slice slice = projectController.getCurrentLayer()
            .getHorizontalSection();
    for (Polygon p : slice) {
      drawModelPath2D(g2d, p.toPath2D());
    }
  }

  private void AIpaintForDebug(Graphics2D g2d) {
    //STROKE COMMANDS
//        g2d.setStroke(new BasicStroke(2f));
//        g2d.setColor(Color.RED);

    //Draw Polygon
//        drawModelPath2D(g2d,poly.toPath2D());

    //Draw bits Areas
    Vector<Bit2D> bits = drawDebug.Bits;
    for (Bit2D bit : bits) {
      Area area = bit.getAreaCS();
      area.transform(realToView);
      g2d.setColor(Color.RED);
      g2d.fill(area);
      g2d.setColor(Color.DARK_GRAY);
      g2d.draw(area);
    }

    //Draw an area
    if (drawDebug.areaToDraw != null) {
      Area area = drawDebug.areaToDraw;
      area.transform(realToView);
      g2d.setColor(Color.BLUE);
      g2d.fill(area);
      g2d.setColor(Color.DARK_GRAY);
      g2d.draw(area);
    }

    //Draw points
    for (Vector2 point : drawDebug.pointsToDrawRED) {
      g2d.setColor(Color.red);
      drawModelCircle(g2d, point.x, point.y, 3);
    }
    for (Vector2 point : drawDebug.pointsToDrawGREEN) {
      g2d.setColor(Color.green);
      drawModelCircle(g2d, point.x, point.y, 5);
    }
    for (Vector2 point : drawDebug.pointsToDrawORANGE) {
      g2d.setColor(Color.orange);
      drawModelCircle(g2d, point.x, point.y, 7);
    }
    for (Vector2 point : drawDebug.pointsToDrawBLACK) {
      g2d.setColor(Color.black);
      drawModelCircle(g2d, point.x, point.y, 7);
    }
    //Draw a list of Segment2D
    Path2D path = new GeneralPath();
    for (Segment2D segment : drawDebug.segmentsToDrawRed) {
      Shape shape = new Line2D.Double(segment.start.x, segment.start.y, segment.end.x, segment.end.y);
      path.append(shape, false);
      g2d.setColor(Color.red);
      drawModelPath2D(g2d, path);
    }

    //Draw a list of Segment2D
    Path2D path2 = new GeneralPath();
    for (Segment2D segment : drawDebug.segmentsToDrawBlue) {
      Shape shape = new Line2D.Double(segment.start.x, segment.start.y, segment.end.x, segment.end.y);
      path2.append(shape, false);
      g2d.setColor(Color.blue);
      drawModelPath2D(g2d, path2);
    }

    //Draw polygons
    int i = 0;
    Color[] colors = {Color.blue, Color.red, Color.GREEN, Color.ORANGE};
    for (Polygon poly : drawDebug.polys) {
      g2d.setColor(colors[i]);
      drawModelPath2D(g2d, poly.toPath2D());
      i++;
    }

    i = 0;
    for (Vector2 point : drawDebug.pointsToDrawBLUE) {
      g2d.setColor(Color.blue);
      drawModelCircle(g2d, point.x, point.y, 1);
      g2d.drawString(Integer.toString(i), (int) (2 * point.x + 100), (int) (2 * point.y + 10));
      i++;
    }

    //Draw Text
        /*
        g2d.drawString("text",posX,posY);
         */
  }

  private void paintBitControls(Bit3D bit, Graphics2D g2d,SubBit2D subbit) {
    bitMovers.put(bit, new BitControls(bit, g2d,subbit));
  }

  /**
   * This method is used the show the preview of the bit when using the manual pattern to add bits
   * @param g2d
   */
  private void paintBitPreview(Graphics2D g2d) {
    // Bit boundary
    Rectangle2D.Double r = new Rectangle2D.Double(
            -CraftConfig.lengthFull / 2,
            -CraftConfig.bitWidth / 2,
            projectController.getNewBitsLengthParam()
                    .getCurrentValue(),
            projectController.getNewBitsWidthParam()
                    .getCurrentValue());
    // Current position of cursor
    Point2D.Double currentSpot = new Point2D.Double(oldX, oldY); // In view
    viewToReal.transform(currentSpot, currentSpot); // In real
    projectController.setCurrentPoint(currentSpot);
    // Transform into current view
    AffineTransform originToCurrentSpot = new AffineTransform();
    originToCurrentSpot.translate(currentSpot.x, currentSpot.y);
    Vector2 lOrientation = Vector2.getEquivalentVector(
            projectController.getNewBitsOrientationParam()
                    .getCurrentValue());
    originToCurrentSpot.rotate(lOrientation.x, lOrientation.y);

    Shape bitPreviewInReal = originToCurrentSpot.createTransformedShape(r);
    Shape bitPreviewInView = realToView.createTransformedShape(bitPreviewInReal);
    Area sectionHolding = new Area(
            new Rectangle2D.Double(CraftConfig.lengthFull / 2 - CraftConfig.sectionHoldingToCut
                    , -CraftConfig.bitWidth / 2
                    , CraftConfig.sectionHoldingToCut
                    , CraftConfig.bitWidth));
    sectionHolding.transform(originToCurrentSpot);
    sectionHolding.transform(realToView);

    Area availableBitArea = projectController.getAvailableBitAreaFrom(bitPreviewInReal); // in real pos
    boolean irregular = DetectorTool.checkIrregular(availableBitArea);
    // Fit into view
    availableBitArea.transform(realToView);
    // Change color based on irregularity
    if (!irregular) {
      if (!projectController.isFullLength()) {
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


  /**
   * A method merging the manual bit adding algorithm with bit (already created) rotating algorithm, that shows the preview of bit
   * while being rotated (rotation preview state)
   * @param g2d
   * @param origin the origin of the rotated bit
   *  similarly to the moving bit algorithm, once a bit is rotated, its actually suppressed and what is shown is only its preview
   *  with the new orientation, and only by pressing on escape a new bit would be added similar to the one shown in the preview.
   *  note: when a bit is in the rotation preview state, it can not be moved and the arrows would disappear,but when it's in
   *  the moving preview state, it can be rotated
   */

  private void paintBitPreviewRotation(Graphics2D g2d, Vector2 origin) {
    // Bit boundary
    Rectangle2D.Double r = new Rectangle2D.Double(
            -CraftConfig.lengthFull / 2,
            -CraftConfig.bitWidth / 2,
            projectController.getNewBitsLengthParam()
                    .getCurrentValue(),
            projectController.getNewBitsWidthParam()
                    .getCurrentValue());
    // Current position of cursor
    Point2D.Double currentSpot = new Point2D.Double(origin.x, origin.y); // In view
    //viewToReal.transform(currentSpot, currentSpot); // In real
    projectController.setCurrentPoint(currentSpot);
    // Transform into current view
    AffineTransform originToCurrentSpot = new AffineTransform();
    originToCurrentSpot.translate(currentSpot.x, currentSpot.y);
    Vector2 lOrientation = Vector2.getEquivalentVector(
            projectController.getBitsrotater()
                    .getCurrentValue());
    originToCurrentSpot.rotate(lOrientation.x, lOrientation.y);

    Shape bitPreviewInReal = originToCurrentSpot.createTransformedShape(r);
    Shape bitPreviewInView = realToView.createTransformedShape(bitPreviewInReal);
    Area sectionHolding = new Area(
            new Rectangle2D.Double(CraftConfig.lengthFull / 2 - CraftConfig.sectionHoldingToCut
                    , -CraftConfig.bitWidth / 2
                    , CraftConfig.sectionHoldingToCut
                    , CraftConfig.bitWidth));
    sectionHolding.transform(originToCurrentSpot);
    sectionHolding.transform(realToView);

    Area availableBitArea = projectController.getAvailableBitAreaFrom(bitPreviewInReal); // in real pos
    boolean irregular = DetectorTool.checkIrregular(availableBitArea);
    // Fit into view
    availableBitArea.transform(realToView);
    // Change color based on irregularity
    if (!irregular) {
      if (!projectController.isFullLength()) {
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

  /**
   * A method merging the manual bit adding algorithm with bit (already created) moving algorithm,that shows the preview of bit
   *   while being moved (moving preview state)
   * @param g2d
   * @param newOrigin the new origin of the bit after moving the bit
   * @param oldBit the old bit before the moving, this object is used to retain the old orientation since moving doesnt affect the orientation
   * so we need to keep the same orientation.
   * note: once a bit is moved by clicking on one of the arrows the bit is actually suppressed, and what is shown is only the preview
   * of the bit in the new position, and only by pressing on the escape button a new bit would be added at the new position as the bit
   * shown in the preview
   */
  private void paintBitPreviewMoving(Graphics2D g2d,Vector2 newOrigin,Bit2D oldBit) {
    // Bit boundary
    Rectangle2D.Double r = new Rectangle2D.Double(
            -CraftConfig.lengthFull / 2,
            -CraftConfig.bitWidth / 2,
            projectController.getNewBitsLengthParam()
                    .getCurrentValue(),
            projectController.getNewBitsWidthParam()
                    .getCurrentValue());
    // Current position of cursor
    Point2D.Double currentSpot = new Point2D.Double(newOrigin.x, newOrigin.y); // In view
    // viewToReal.transform(currentSpot, currentSpot); // In real
    projectController.setCurrentPoint(currentSpot);

    // Transform into current view
    AffineTransform originToCurrentSpot = new AffineTransform();
    // originToCurrentSpot=oldBit.getTransfoMatrixToCS();
    originToCurrentSpot.translate(currentSpot.x, currentSpot.y);
    Vector2 lOrientation = oldBit.getOrientation();
    originToCurrentSpot.rotate(lOrientation.x, lOrientation.y);

    Shape bitPreviewInReal = originToCurrentSpot.createTransformedShape(r);

    Shape bitPreviewInView = realToView.createTransformedShape(bitPreviewInReal);
    Area sectionHolding = new Area(
            new Rectangle2D.Double(CraftConfig.lengthFull / 2 - CraftConfig.sectionHoldingToCut
                    , -CraftConfig.bitWidth / 2
                    , CraftConfig.sectionHoldingToCut
                    , CraftConfig.bitWidth));
    sectionHolding.transform(originToCurrentSpot);
    sectionHolding.transform(realToView);

    Area availableBitArea = projectController.getAvailableBitAreaFrom(bitPreviewInReal); // in real pos
    boolean irregular = DetectorTool.checkIrregular(availableBitArea);
    // Fit into view
    availableBitArea.transform(realToView);
    // Change color based on irregularity
    if (!irregular) {
      if (!projectController.isFullLength()) {
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
    BitControls(Bit3D bit, Graphics2D g2d,SubBit2D subBit) {
      // Defining the shape of the arrows
      if(subBit==null){
        TriangleShape triangleShape = new TriangleShape(
                new Point2D.Double(0, 0),
                new Point2D.Double(-7, 10),
                new Point2D.Double(7, 10));

        int padding = WorkspaceConfig.paddingBitControl; // Space between bit and arrows
        Area overlapBit;
        Vector2 lOrientationr=null ;
        if(!Moving) {overlapBit = new Area(
                new Rectangle2D.Double(
                        -CraftConfig.lengthFull / 2,
                        -CraftConfig.bitWidth / 2,
                        CraftConfig.lengthFull,
                        CraftConfig.bitWidth));
          overlapBit.transform(bit.getBaseBit()
                  .getTransfoMatrixToCS());}

        else {
          Rectangle2D.Double r = new Rectangle2D.Double(
                  -CraftConfig.lengthFull / 2,
                  -CraftConfig.bitWidth / 2,
                  projectController.getNewBitsLengthParam()
                          .getCurrentValue(),
                  projectController.getNewBitsWidthParam()
                          .getCurrentValue());
          Point2D.Double currentSpot = new Point2D.Double(newOrigin.x, newOrigin.y); // In view
          // viewToReal.transform(currentSpot, currentSpot); // In real
          projectController.setCurrentPoint(currentSpot);

          // Transform into current view
          AffineTransform originToCurrentSpot = new AffineTransform();
          // originToCurrentSpot=oldBit.getTransfoMatrixToCS();
          originToCurrentSpot.translate(currentSpot.x, currentSpot.y);
          Vector2 lOrientation = movedBit.getBaseBit().getOrientation();
          originToCurrentSpot.rotate(lOrientation.x, lOrientation.y);

          Shape bitPreviewInReal = originToCurrentSpot.createTransformedShape(r);
          overlapBit=new Area(bitPreviewInReal);
          lOrientationr= movedBit.getBaseBit().getOrientation();
        }

//padding=15
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

        Bit2D newBit=null;
        if (Moving) newBit = new NewBit2D(newOrigin, lOrientationr, projectController.getNewBitsLengthParam().getCurrentValue(), projectController.getNewBitsWidthParam().getCurrentValue());

        g2d.setColor(WorkspaceConfig.bitControlColor);
        if(!Moving) affTrans = bit.getBaseBit().getTransfoMatrixToCS();
        else { affTrans = newBit.getTransfoMatrixToCS();  }
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
      else { //to mark subbits when selected
        g2d.setColor(new Color(255, 0, 255, 250));
        drawModelArea(g2d,subBit.getAreaCS());

      }




    }
  }


  public void setViewOffset() {
    viewOffsetX = 0;
    viewOffsetY = 0;
  }
}
