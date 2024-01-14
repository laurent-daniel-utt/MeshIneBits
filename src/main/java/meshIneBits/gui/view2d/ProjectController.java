/*
 * MeshIneBits is a Java software to disintegrate a 3d project (model in .stl)
 * into a network of standard parts (called "Bits").
 *
 * Copyright (C) 2016-2021 DANIEL Laurent.
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
import meshIneBits.borderPaver.artificialIntelligence.Acquisition;
import meshIneBits.borderPaver.debug.drawDebug;
import meshIneBits.config.CraftConfig;
import meshIneBits.config.CraftConfigLoader;
import meshIneBits.config.patternParameter.BooleanParam;
import meshIneBits.config.patternParameter.DoubleParam;
import meshIneBits.gui.view3d.oldversion.ProcessingModelView;
import meshIneBits.gui.view3d.provider.ProjectProvider;
import meshIneBits.patterntemplates.PatternTemplate;
import meshIneBits.scheduler.AScheduler;
import meshIneBits.util.*;
import meshIneBits.util.supportUndoRedo.ActionOfUserMoveBit;
import meshIneBits.util.supportUndoRedo.ActionOfUserScaleBit;
import meshIneBits.util.supportUndoRedo.HandlerRedoUndo;

import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static meshIneBits.gui.view3d.view.BaseVisualization3DView.WindowStatus;

/**
 * Observes the {@link Project} and {@link Layer}s. Observed by {@link ProjectWindowCore} and {@link
 * ProcessingModelView}. Controls {@link ProjectWindow}.
 */
@SuppressWarnings("WeakerAccess")
public class ProjectController extends Observable implements Observer ,
    HandlerRedoUndo.UndoRedoListener {

  public static final String SHOW_SLICE = "showSlice";
  public static final String SHOW_LIFT_POINTS = "showLiftPoints";
  public static final String SHOW_BITS_NOT_FULL_LENGTH = "showBitsNotFull";
  public static final String SHOW_PREVIOUS_LAYER = "showPreviousLayer";
  public static final String SHOW_CUT_PATHS = "showCutPaths";
  public static final String SHOW_IRREGULAR_BITS = "showIrregularBits";
  public static final String ADDING_BITS = "addingBits";
  public static final String SELECTING_REGION = "selectingRegion";
  public static final String SETTING_LAYER = "settingLayer";

  public static final String MANIPULATNG_BIT   = "manipulateBit";

  public static final String MESH_SLICED = "meshSliced";
  public static final String MESH_OPENED = "meshOpened";
  public static final String LAYER_CHOSEN = "layerChosen";
  public static final String MESH_PAVED = "meshPaved";
  public static final String LAYER_PAVED = "layerPaved";
  public static final String LAYER_OPTIMIZED = "layerOptimized";
  public static final String MESH_OPTIMIZED = "meshOptimized";
  public static final String BIT_UNSELECTED = "bitUnselected";
  public static final String BIT_SELECTED = "bitSelected";
  public static final String BITS_SELECTED = "bitsSelected";
  public static final String DELETING_BITS = "deletingBits";
  public static final String DELETING_SUBBITS="deletingSubBits";

  public boolean manipulating=false;
  public static final String SUBBITS_DELETED="deletedSubBits";
  public static final String BITS_DELETED = "deletedBits";

  public static final String SubBIT_UNSELECTED="subBitUnSelected";
  public static final String SubBIT_SELECTED="subBitSelected";
  // New bit config
  private final DoubleParam newBitsLengthParam = new DoubleParam(
      "newBitLength",
      "Bit length",
      "Length of bits to add",
      1.0, CraftConfig.lengthFull,
      CraftConfig.lengthFull, 1.0);
  private final DoubleParam newBitsWidthParam = new DoubleParam(
      "newBitWidth",
      "Bit width",
      "Length of bits to add",
      1.0, CraftConfig.bitWidth,
      CraftConfig.bitWidth, 1.0);
  private final DoubleParam newBitsOrientationParam = new DoubleParam(
      "newBitOrientation",
      "Bit orientation",
      "Angle of bits in respect to that of layer",
      -180.0, 180.0, 0.0, 0.01);
  private final DoubleParam bitsrotater = new DoubleParam(
          "newBitsOrientation",
          "SelectedBits orientation",
          "Angle of Rotatedbits in respect to that of layer",
          -180.0, 180.0, 0.0, 0.01);

  private final DoubleParam bitsInverser = new DoubleParam(
          "bit inverser",
          "bitInversion",
          "inversing reversed bits",
          -180.0, 180.0, 0.0, 0.01);

  private final DoubleParam safeguardSpaceParam = new DoubleParam(
      "safeguardSpace",
      "Space around bit",
      "In order to keep bits not overlapping or grazing each other",
      1.0, 10.0, CraftConfig.safeguardSpaceParam, 0.01);

  private final BooleanParam autocropParam = new BooleanParam(
      "autocrop",
      "Auto crop",
      "Cut the new bit while preserving space around bits",
      true
  );
  private final BooleanParam prohibitAddingIrregularBitParam = new BooleanParam(
      "prohibitAddingIrregularBit",
      "Keep regularity",
      "Prohibit adding irregular bit",
      true
  );
  private final ProjectWindow projectWindow;
  private final List<Point2D.Double> regionVertices = new ArrayList<>();
  private final PropertyChangeSupport changes = new PropertyChangeSupport(this);
  /**
   * For debug, let displaying segments, points and areas via drawDebug.
   *
   * @see drawDebug
   */
  public boolean AI_NeedPaint = false;
  private Project project;
  private int layerNumber = -1;
  private Set<Vector2> selectedBitKeys = new HashSet<>();
  private Set<Vector2> selectedBitKeysOfSubbit = new HashSet<>();

  private Set<SubBit2D> selectedsubBit = new HashSet<>();
  private Set<SubBit2D> selectedsubBitMemory = new HashSet<>();

  private Layer currentLayer = null;
  private double zoom = 1;
  private boolean showSlice = true;
  private boolean showLiftPoints = false;
  private boolean showPreviousLayer = false;
  private boolean showCutPaths = false;
  private boolean showIrregularBits = false;
  private boolean addingBits = false;
  private boolean showBitNotFull = false;
  public static ProjectController thecontroller=null;


  /**
   * In {@link Project}'s coordinate system
   */
  private Area availableArea;
  /**
   * In {@link Project}'s coordinate system
   */
  private Area bitAreaPreview;
  private boolean selectingRegion;
  private boolean selectedRegion;
  private Path2D.Double currentSelectedRegion = new Path2D.Double();
  private Point2D currentPoint;
  private Area areaHoldingCut;
  //this variable is used to calc bit full length when paint preview
  private boolean fullLength = true;
  private CustomLogger logger = new CustomLogger(this.getClass());
  /**
   * In real coordinate system
   */
  private Point2D bulkSelectZoneUpperLeft;
  private Point2D bulkSelectZoneBottomRight;
  private Rectangle2D.Double bulkSelectZone;
  /**
   * used to register action of user for redo/undo function
   */
  private HandlerRedoUndo handlerRedoUndo = new HandlerRedoUndo(this);
  /**
   *
   */
  private ITheardServiceExecutor serviceExecutor = MultiThreadServiceExecutor.instance;
public static AtomicBoolean Paved=new AtomicBoolean(false);
public static CountDownLatch r=new CountDownLatch(1);
  ProjectController(ProjectWindow projectWindow) {
    this.projectWindow = projectWindow;
    thecontroller=this;
  }

  public boolean isFullLength() {
    return fullLength;
  }

  public void setCurrentPoint(Point2D currentPoint) {
    this.currentPoint = currentPoint;
  }

  public Project getMesh() {
    return project;
  }


  public void setMesh(Project project) {
    this.project = project;
    project.addObserver(this);
  }

  public void resetMesh() {
    project = null;
    this.setChanged();
    this.notifyObservers(ProjectEvents.READY);
  }

  public int getLayerNumber() {
    return layerNumber;
  }

  public Area getAvailableArea() {
    return availableArea;
  }

  @Override
  public void update(Observable o, Object arg) {
    if (o instanceof Layer) {
      updateAvailableArea();
    }
    if (arg == null || arg instanceof M) {
      setChanged();
      notifyObservers();
      return;
    }
    if (arg instanceof ProjectEvents) {
      switch ((ProjectEvents) arg) {
        case READY:
          projectWindow.reset();
          break;
        case IMPORT_FAILED:

          break;
        case IMPORTING:
          break;
        case IMPORTED:
          ProjectProvider.getInstance()
              .setMesh(project);
          break;
        case SLICING:
          break;
        case SLICED:
          Logger.updateStatus("Project sliced");
          setLayer(0);
          projectWindow.initGadgets();
          // Notify the core to draw
          setChanged();
          notifyObservers(ProjectEvents.SLICED);
          // Notify property panel
          changes.firePropertyChange(MESH_SLICED, null, project);
          changes.firePropertyChange(LAYER_CHOSEN, null, getCurrentLayer());
          break;
        case PAVING_MESH:
          Logger.updateStatus("Paving project");
          break;
        case PAVED_MESH:
          Logger.updateStatus("Project paved");
          checkEmptyLayers();
          setChanged();
          notifyObservers(ProjectEvents.PAVED_MESH);

          if (WindowStatus==2)r.countDown();
          Paved.set(true);
          // Notify property panel
          changes.firePropertyChange(MESH_PAVED, null, project);
          changes.firePropertyChange(LAYER_PAVED, null, getCurrentLayer());
          break;
        case PAVING_LAYER:
          break;
        case PAVED_LAYER:
          // Notify property panel
          changes.firePropertyChange(LAYER_PAVED, null, getCurrentLayer());
          setChanged();
          notifyObservers(ProjectEvents.PAVED_LAYER);
          break;
        case OPTIMIZING_LAYER:
          break;
        case OPTIMIZED_LAYER:
          // Notify property panel
          changes.firePropertyChange(LAYER_OPTIMIZED, null, getCurrentLayer());
          // Notify the core to draw
          setChanged();
          notifyObservers(ProjectEvents.SLICED);
          break;
        case OPTIMIZING_MESH:
          break;
        case OPTIMIZED_MESH:
          // Notify property panel
          changes.firePropertyChange(MESH_OPTIMIZED, null, project);
          // Notify the core to draw
          setChanged();
          notifyObservers(ProjectEvents.SLICED);
          break;
        case GLUING:
          break;
        case GLUED:
          break;
        case SCHEDULING:
          break;
        case SCHEDULED:
          break;
        case OPENED:
          //projectWindow.getView3DWindow().setCurrentMesh(project);
          ProjectProvider.getInstance()
              .setMesh(project);
          setLayer(0);
          projectWindow.initGadgets();
          setChanged();
          notifyObservers(ProjectEvents.OPENED);
          changes.firePropertyChange(MESH_OPENED, null, project);
          changes.firePropertyChange(LAYER_CHOSEN, null, getCurrentLayer());
          break;
        case OPEN_FAILED:
          break;
        case SAVED:
          break;
        case SAVE_FAILED:
          break;
        case EXPORTING:
          break;
        case EXPORTED:
          break;
      }
    }
  }

 /* private void updateAvailableArea() {
    availableArea = AreaTool.getAreaFrom(getCurrentLayer().getHorizontalSection());
    Pavement pavement = getCurrentLayer().getFlatPavement();
    if (pavement == null) {
      return; // Empty layer
    }
    pavement.getBitsKeys()
        .forEach(key -> availableArea.subtract(
            AreaTool.expand(
                pavement.getBit(key)
                    .getAreaCS(), // in real
                safeguardSpaceParam.getCurrentValue())
        ));
  }
*/

  public void callupdate(){
    if(availableArea!=null)updateAvailableArea();
  }
 private  void updateAvailableArea() {
   availableArea = AreaTool.getAreaFrom(getCurrentLayer().getHorizontalSection());
   Pavement pavement = getCurrentLayer().getFlatPavement();
   if (pavement == null) {
     return; // Empty layer
   }
   getCurrentLayer().getSubBits()
           .forEach(sub -> availableArea.subtract(
                   AreaTool.expand(
                          sub.getAreaCS(),CraftConfig.safeguardSpaceParam)
           ));
 }


  public void setLayer(int layerNum) {
    if (project == null) {
      return;
    }
    if ((layerNum >= project.getLayers()
        .size())
        || (layerNum < 0)) {
      return;
    }
    layerNumber = layerNum;
    getCurrentLayer().addObserver(this);
    updateAvailableArea();
    reset();
    // Notify selector
    changes.firePropertyChange(SETTING_LAYER, 0, layerNum); // no need of old value
    // Notify property panel
    changes.firePropertyChange(LAYER_CHOSEN, null, getCurrentLayer());
    // Notify the core
    setChanged();
    notifyObservers();
  }

  public void checkEmptyLayers() {
    // Check empty layers
    List<Integer> indexesEmptyLayers = project.getEmptyLayers();
    if (indexesEmptyLayers.size() > 0) {
      StringBuilder str = new StringBuilder();
      indexesEmptyLayers.forEach(integer -> str.append(integer)
          .append(" "));
      Logger.updateStatus("Some layers are empty: " + str.toString());
    }
  }

  public void reset() {
    setSelectedBitKeys(null);
    setAddingBits(false);
    clearSelectingRegion(true);
  }

  private void clearSelectingRegion(boolean fireChangesSelectingRegion) {
    regionVertices.clear();
    currentSelectedRegion = new Path2D.Double();
    selectedRegion = false;
    selectingRegion = false;
    if (fireChangesSelectingRegion) {
      changes.firePropertyChange(SELECTING_REGION, true, false);
    }
    setChanged();
    notifyObservers();
  }

  public void scaleSelectedBit(double percentageLength, double percentageWidth) {
    final Map<Vector2, Double[]> map = new HashMap<>();
    this.getSelectedBits()
        .forEach(bitKey -> map.put(bitKey.getOrigin(),
            new Double[]{bitKey.getBaseBit().getLength(), bitKey.getBaseBit().getWidth()}));
//        LinkedList<Double> listsLengthBefore= this.getSelectedBits().stream().map(bit->bit.getBaseBit().getLength()).collect(Collectors.toCollection(LinkedList::new));
//        LinkedList<Double> listsWidthBefore= this.getSelectedBits().stream().map(bit->bit.getBaseBit().getWidth()).collect(Collectors.toCollection(LinkedList::new));
    ActionOfUserScaleBit actionOfUserScaleBit = new ActionOfUserScaleBit(map, percentageLength,
        percentageWidth);
    this.handlerRedoUndo.addActionBit(actionOfUserScaleBit);
    if (this.getSelectedBitKeys()
        .isEmpty()) {
      Logger.warning("There is no bit selected");
    } else {
      setSelectedBitKeys(getSelectedBits().stream()
          .map(bit -> getCurrentLayer().scaleBit(bit, percentageLength, percentageWidth))
          .collect(Collectors.toSet()));
    }
  }

  public Set<Vector2> getSelectedBitKeys() {
    return selectedBitKeys;
  }

  /**
   * Bulk reset
   *
   * @param newSelectedBitKeys <tt>null</tt> to reset to empty
   */
  public void setSelectedBitKeys(Set<Vector2> newSelectedBitKeys) {
    selectedBitKeys.clear();
    if (newSelectedBitKeys != null) {
      selectedBitKeys.addAll(newSelectedBitKeys);
      selectedBitKeys.removeIf(Objects::isNull);
    }
    // Notify property panel
    changes.firePropertyChange(BITS_SELECTED, null, getSelectedBits());
    // Notify the core to repaint
    setChanged();
    notifyObservers();
  }
  public void setSelectedSubBit(Set<SubBit2D> newSelectedSubs) {
    selectedsubBit.clear();
    if (newSelectedSubs != null) {
      selectedsubBit.addAll(newSelectedSubs);
      selectedsubBit.removeIf(Objects::isNull);
    }
    // Notify property panel
    //changes.firePropertyChange(BITS_SELECTED, null, getSelectedBits());
    // Notify the core to repaint
    setChanged();
    notifyObservers();
  }
  public Set<Bit3D> getSelectedBits() {
    return selectedBitKeys.stream()
        .map(getCurrentLayer()::getBit3D)
        .collect(Collectors.toSet());
  }
  public Set<Bit3D> getSelectedBitsforSubBits() {
    return selectedBitKeysOfSubbit.stream()
            .map(getCurrentLayer()::getBit3D)
            .collect(Collectors.toSet());
  }
  public Set<SubBit2D> getSelectedSubBits() {
    return selectedsubBit;
  }

  /**
   * Restore a project into working space
   *
   * @param file location of saved project
   */
  public void openMesh(File file) throws SimultaneousOperationsException {
    resetAll();
    if (project != null && project.getState()
        .isWorking()) {
      throw new SimultaneousOperationsException(project);
    }
    // Save last opened file
    CraftConfig.lastMesh = file.getPath();
    CraftConfigLoader.saveConfig(null);

    ProjectOpener projectOpener = new ProjectOpener(file);
    projectOpener.addObserver(this);
    serviceExecutor.execute(projectOpener);
  }

  /**
   * Save the current project on disk
   *
   * @param file location to save
   */
  public void saveMesh(File file) throws Exception {
    if (project == null) {
      throw new Exception("Project not found");
    }
    if (project.getState()
        .isWorking()) {
      throw new SimultaneousOperationsException(project);
    }
    // Save last opened file
    CraftConfig.lastMesh = file.getPath();
    CraftConfigLoader.saveConfig(null);

    ProjectSaver projectSaver = new ProjectSaver(file);
    serviceExecutor.execute(projectSaver);
  }

  public void exportXML(File file) throws Exception {
    if (project == null) {
      throw new Exception("Project not found");
    }
    serviceExecutor.execute(() -> {
      try {
        project.export(file);
      } catch (Exception e) {
        this.handleException(e);
      }
    });
  }

  public void newMesh(File file) throws SimultaneousOperationsException {
    resetAll();
    if (project != null && project.getState()
        .isWorking()) {
      throw new SimultaneousOperationsException(project);
    }
    // Save last opened file
    CraftConfig.lastModel = file.getPath();
    CraftConfigLoader.saveConfig(null);

    ProjectCreator projectCreator = new ProjectCreator(file);
    projectCreator.addObserver(this);
    serviceExecutor.execute(projectCreator);
  }

  public void sliceMesh() throws Exception {
    if (project == null) {
      throw new Exception("Project not found");
    }
    if (project.getState()
        .isWorking()) {
      throw new SimultaneousOperationsException(project);
    }
    this.serviceExecutor.execute(() -> {
      logger.logDEBUGMessage("SliceMesh start");
      try {
        project.slice();
      } catch (Exception e) {
        this.handleException(e);
      }

    });
  }

  public void paveMesh(PatternTemplate patternTemplate) throws Exception {
    if (project == null) {
      throw new Exception("Project not found");
    }
    if (project.getState()
        .isWorking()) {
      throw new SimultaneousOperationsException(project);
    }
    if (!project.isSliced()) {
      throw new Exception("Project not sliced");
    }
    CraftConfigLoader.saveConfig(null);
    serviceExecutor.execute(() -> {
      try {

        project.pave(patternTemplate);
      } catch (Exception e) {
        e.printStackTrace();
      }
    });
//        project.pave(patternTemplate);
  }



  public PropertyChangeSupport getChanges(){
    return changes;
  }
  public void deleteSelectedBits() {
    //save action before doing
    if (Acquisition.isStoringNewBits()) { //if AI is storing new examples bits, we send the bit to it
      Acquisition.deleteLastPlacedBits(selectedBitKeys.size());
    }
    Set<Vector2> previousKeys = new HashSet<>(this.getSelectedBitKeys());
    Set<Bit3D> bit3DSet = this.getSelectedBits();
    handlerRedoUndo.addActionBit(new ActionOfUserMoveBit(null,bit3DSet, previousKeys, null, null,
        this.getCurrentLayer().getLayerNumber()));

    changes.firePropertyChange(DELETING_BITS, null, getSelectedBits());
    getCurrentLayer().removeBits(selectedBitKeys, true);
    selectedBitKeys.clear();

    //TODO verify if putting this rebuild in comment will affect anything
   // getCurrentLayer().rebuild();
    changes.firePropertyChange(BITS_DELETED, null, getCurrentLayer());
  }






  public void deleteSelectedSubBits() {
    //save action before doing
    Set<Vector2> previousKeys = new HashSet<>(this.selectedBitKeysOfSubbit);
    Set<Bit3D> bit3DSet = this.getSelectedBitsforSubBits();

   // handlerRedoUndo.addActionBit(new ActionOfUserMoveBit(bit3DSet, previousKeys, null, null,
     //       this.getCurrentLayer().getLayerNumber()));

    handlerRedoUndo.addActionBit(new ActionOfUserMoveBit(selectedsubBit,null, null, null, null,
            this.getCurrentLayer().getLayerNumber()));
    changes.firePropertyChange(DELETING_SUBBITS, null, getSelectedBits());
    selectedsubBit.forEach(subBit2D -> subBit2D.setRemoved(true));
    selectedsubBitMemory.addAll(selectedsubBit);
    getCurrentLayer().setRemovedSubBits((HashSet<SubBit2D>) selectedsubBitMemory);
    selectedsubBit.forEach(subBit2D -> getCurrentLayer().removeSubBit(getCurrentLayer().getKey(subBit2D.getParentBit()),subBit2D,true));
    selectedBitKeysOfSubbit.clear();
    selectedsubBit.clear();
    //getCurrentLayer().rebuild();
    setChanged();
    notifyObservers();
    changes.firePropertyChange(SUBBITS_DELETED, null, getCurrentLayer());
  }



public void deleteSubbits(Set<SubBit2D> subs){
    getCurrentLayer().removeSubbits(subs);
  selectedsubBitMemory.addAll(subs);
  getCurrentLayer().getRemovedSubBits().addAll(subs);
  }





  public void deleteBitsByBitsAndKeys(Set<Bit3D> bit3DSet, Set<Vector2> keys) {
    setSelectedBitKeys(keys);
    changes.firePropertyChange(DELETING_BITS, null, getSelectedBits());
    getCurrentLayer().removeBits(getSelectedBitKeys(), true);
    selectedBitKeys.clear();
    //TODO verify if putting this rebuild in comment will affect anything
    // getCurrentLayer().rebuild();
    changes.firePropertyChange(BITS_DELETED, null, getCurrentLayer());
  }
  public void deleteBitsByKeys( Set<Vector2> keys) {
    getCurrentLayer().removeBits(keys, true);
    keys.clear();
  }

  public void incrementBitsOrientationParamBy(double v) {
    newBitsOrientationParam.incrementBy(v, true);
    setChanged();
    notifyObservers();
  }

  public void incrementSelectedBitsOrientationParamBy(double v) {
    bitsrotater.incrementBy(v, true);
    setChanged();
    notifyObservers();
  }
  public void inverserIncrementation(double v) {
    bitsInverser.incrementBy(v, true);
    setChanged();
    notifyObservers();
  }


  public void updateCore(){
    setChanged();
    notifyObservers();
  }

  public Layer getCurrentLayer() {
    return getMesh() == null || getMesh().getLayers() == null || getMesh().getLayers()
        .isEmpty()
        || layerNumber < 0 || getMesh().getLayers()
        .size() < layerNumber
        ? null : getMesh().getLayers()
        .get(layerNumber);
  }

  public Area getAvailableBitAreaFrom(Shape bitPreviewInReal) {
    Area a = new Area(bitPreviewInReal);

    // Intersect
    a.intersect(availableArea);
    calcBitFullLengthOrNormal(a);
    // Cache
    bitAreaPreview = (Area) a.clone();
    return a;
  }




  private void calcBitFullLengthOrNormal(Area a) {
    fullLength = (Bit2D.checkSectionHoldingToCut(
        new Vector2(currentPoint.getX(), currentPoint.getY()),
        Vector2.getEquivalentVector(newBitsOrientationParam.getCurrentValue()), a));
  }

  public DoubleParam getBitsInverser() {
    return bitsInverser;
  }

  /**
   * this method is used in 4 cases to add a new bit, with manual pattern, when rotating a bit, when moving a bit and when manipulating
   * a bit (using the manipulating button), all these four functionalities use the same algorithm but with few differences.
   * @param position in {@link Project}'s coordinate system
   * @param neworigin the new origin of the moved bit (using the blue arrows)
   * @param orientation the new orientation of the rotating bit (using ctrl+mouse wheel)
   * @param rotating true when we rotated a bit (ctrl+mouse wheel), false if not
   */
  public void addNewBitAt(Point2D.Double position,boolean rotating,Vector2 orientation,Vector2 neworigin) {
    if (project == null
        || getCurrentLayer().getFlatPavement() == null
        || position == null
        || bitAreaPreview.isEmpty()
    ) {
      return;
    }
    // Do not add new irregular bit
    if (DetectorTool.checkIrregular(bitAreaPreview)
        && prohibitAddingIrregularBitParam.getCurrentValue()) {
      return;
    }
    Vector2 lOrientation;
    // when this method is used by the manual pattern
    if(!rotating && neworigin==null){  lOrientation = Vector2.getEquivalentVector(newBitsOrientationParam.getCurrentValue());}
   // when this method is used to add a bit after rotating a bit
    else if(rotating){ lOrientation = Vector2.getEquivalentVector(bitsrotater.getCurrentValue()); }
   // when this method is used after moving a bit
    else { lOrientation=orientation;}
    Vector2 origin;
    origin= new Vector2(position.x, position.y);
    //save origin of new bit
    Set<Vector2> resultKey = new HashSet<>();
    resultKey.add(origin);
    //add new bit
    Bit2D newBit = new NewBit2D(origin, lOrientation,
        newBitsLengthParam.getCurrentValue(),
        newBitsWidthParam.getCurrentValue());
    newBit.setCheckFullLength(true);
    if (autocropParam.getCurrentValue()) {
      newBit.updateBoundaries(bitAreaPreview);
    }
    getCurrentLayer().addBit(newBit, true);
    //add new action into HandlerRedoUndo
    setSelectedBitKeys(resultKey);

    if (Acquisition.isStoringNewBits()) { //if AI is storing new examples bits, we send the bit to it
      try {
        Acquisition.addNewExampleBit(newBit, currentLayer.getHorizontalSection());
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    this.handlerRedoUndo.addActionBit(new ActionOfUserMoveBit(resultKey, this.getSelectedBits(),
        getCurrentLayer().getLayerNumber()));
    selectedBitKeys.clear();
  }




  public void addBit3Ds(Collection<Bit3D> bits3d) {
    for (Bit3D bit3d : bits3d) {
      Bit2D bit2d = bit3d.getBaseBit();
      bit2d.setUsedForNN(bit3d.isUsedForNN());
      getCurrentLayer().addBit(bit2d, true);
    }
  }

  public void addSubBit3Ds(Collection<SubBit2D> subs) {
    for (SubBit2D sub : subs) {
      sub.setRemoved(false);
      getCurrentLayer().addSubBit(sub.getParentBit(), sub);
    HashSet<SubBit2D> newRemovedsubs=new HashSet<>();
    }
    selectedsubBitMemory.removeAll(subs);
    getCurrentLayer().getRemovedSubBits().removeAll(subs);
  }
  /**
   * @param position in {@link Project} coordinate system
   * @return key of bit containing <tt>position</tt>. <tt>null</tt> if not found
   */
  private Vector2 findBitAt(Point2D.Double position) {
    Pavement flatPavement = getCurrentLayer().getFlatPavement();
    for (Vector2 key : flatPavement.getBitsKeys()) {
      if (flatPavement.getBit(key)
          .getAreaCS()
          .contains(position)) {
        return key;
      }
    }
    return null;
  }

  private SubBit2D findSubBitAt(Point2D.Double position){
  HashSet<SubBit2D>subbits=new HashSet<>(getCurrentLayer().getSubBits());
         for(SubBit2D sub:subbits){
           if(sub.getAreaCS().contains(position)){
             return sub;
           }
         }
    return null;
  }

  /**
   * Add new bit key to {@link #selectedBitKeys} and remove if already present
   *
   * @param bitKey in layer's coordinate system
   */
  public void addOrRemoveSelectedBitKeys(Vector2 bitKey) {
    if (project == null || !project.isPaved() || bitKey == null) {
      return;
    }
    if (!selectedBitKeys.add(bitKey)) {
      selectedBitKeys.remove(bitKey);
      changes.firePropertyChange(BIT_UNSELECTED, null, getCurrentLayer().getBit3D(bitKey));
    } else {
      changes.firePropertyChange(BIT_SELECTED, null, getCurrentLayer().getBit3D(bitKey));
    }
    // Notify the core
    setChanged();
    notifyObservers();
  }

  public double getZoom() {
    return zoom;
  }

  public void setZoom(double zoom) {
    this.zoom = zoom;
    setChanged();
    notifyObservers();
  }

  public DoubleParam getNewBitsLengthParam() {
    return newBitsLengthParam;
  }

  public DoubleParam getNewBitsWidthParam() {
    return newBitsWidthParam;
  }

  public DoubleParam getNewBitsOrientationParam() {
    return newBitsOrientationParam;
  }

  public  DoubleParam getBitsrotater(){

    return  bitsrotater;
  }

  public DoubleParam getSafeguardSpaceParam() {
    return safeguardSpaceParam;
  }

  public BooleanParam getAutocropParam() {
    return autocropParam;
  }

  public BooleanParam getProhibitAddingIrregularBitParam() {
    return prohibitAddingIrregularBitParam;
  }

  public void addPropertyChangeListener(String property, PropertyChangeListener l) {
    if (property.equals(""))
    // Listen all changes
    {
      changes.addPropertyChangeListener(l);
    } else
    // Listen to specific changes
    {
      changes.addPropertyChangeListener(property, l);
    }
  }

  public void removePropertyChangeListener(PropertyChangeListener l) {
    changes.removePropertyChangeListener(l);
  }

  public void retrieveBulkSelectedBits() {
    if (project == null
        || !project.isPaved()
        || bulkSelectZone.isEmpty()) {
      return;
    }
    Pavement flatPavement = getCurrentLayer().getFlatPavement();
    for (Vector2 key : flatPavement.getBitsKeys()) {NewBit2D bit=(NewBit2D)flatPavement.getBit(key);
     for(SubBit2D sub:bit.getSubBits()){
       if (bulkSelectZone.contains(sub
               .getAreaCS()
               .getBounds2D()) && !sub.isRemoved()) {
         selectedBitKeys.add(key);
       }

     }


    /*  if (bulkSelectZone.contains(flatPavement.getBit(key)
          .getAreaCS()
          .getBounds2D())) {
        selectedBitKeys.add(key);
      }*/
    }
    clearBulkSelect();
    changes.firePropertyChange(BITS_SELECTED, null, getSelectedBits());
    setChanged();
    notifyObservers();
  }

  public void clearBulkSelect() {
    bulkSelectZone = null;
    bulkSelectZoneUpperLeft = null;
    bulkSelectZoneBottomRight = null;
  }

  public void startBulkSelect(Point2D realSpot) {
    bulkSelectZoneBottomRight = realSpot;
    bulkSelectZoneUpperLeft = realSpot;
    bulkSelectZone = new Rectangle2D.Double();
    bulkSelectZone.setFrameFromDiagonal(bulkSelectZoneUpperLeft, bulkSelectZoneBottomRight);
  }

  public void updateBulkSelect(Point2D realSpot) {
    bulkSelectZoneBottomRight = realSpot;
    bulkSelectZone.setFrameFromDiagonal(bulkSelectZoneUpperLeft, bulkSelectZoneBottomRight);
  }

  public Rectangle2D getBulkSelectZone() {
    return bulkSelectZone;
  }

  public boolean isBulkSelecting() {
    return bulkSelectZone != null;
  }

  /**
   * Centralized handler of exceptions
   *
   * @param e raised from any project execution
   */
  public void handleException(Exception e) {
    e.printStackTrace();
    Logger.error(e.getMessage());
  }

  public void optimizeMesh() throws Exception {
    if (project == null) {
      throw new Exception("Project not found");
    }
    if (!project.isPaved()) {
      throw new Exception("Project not paved");
    }
    if (project.getState()
        .isWorking()) {
      throw new SimultaneousOperationsException(project);
    }
//        project.optimize();
    OptimizedProject optimizedProject = new OptimizedProject();
    optimizedProject.updateMesh(project)
        .optimize();
  }

  public void optimizeLayer() throws Exception {
    if (project == null) {
      throw new Exception("Project not found");
    }
    if (project.getState()
        .isWorking()) {
      throw new SimultaneousOperationsException(project);
    }
    if (!getCurrentLayer().isPaved()) {
      throw new Exception("Layer not paved");
    }
    project.optimize(getCurrentLayer());
  }

  public void paveLayer(PatternTemplate patternTemplate) throws Exception {
    if (project == null) {
      throw new Exception("Project not found");
    }
    if (project.getState()
        .isWorking()) {
      throw new SimultaneousOperationsException(project);
    }
    serviceExecutor.execute(() -> {
      try {
        project.pave(patternTemplate, getCurrentLayer());
      } catch (Exception e) {
        this.handleException(e);
      }
    });
  }

  public void setNewBitSize(int lengthPercentage, int widthPercentage) {
    newBitsLengthParam.setCurrentValue(CraftConfig.lengthFull * lengthPercentage / 100);
    newBitsWidthParam.setCurrentValue(CraftConfig.bitWidth * widthPercentage / 100);
    setChanged();
    notifyObservers();
  }

  public void scheduleMesh() throws Exception {
    if (project == null) {
      throw new Exception("Project not found");
    }
    if (!project.isPaved()) {
      throw new Exception("Project not paved");
    }
    if (project.getScheduler() == null) {
      throw new Exception("Scheduler not defined");
    }
    serviceExecutor.execute(() -> {
      try {
        project.runScheduler();
      } catch (Exception e) {
        this.handleException(e);
      }
    });
  }

  public void setScheduler(AScheduler scheduler) throws Exception {
    if (project == null) {
      throw new Exception("Project not found");
    }
    if (!project.isPaved()) {
      throw new Exception("Project not paved");
    }
    project.setScheduler(scheduler);
  }

  public void paveSelectedRegion(PatternTemplate patternTemplate) throws Exception {
    if (project == null) {
      throw new Exception("Project not found");
    }
    if (getCurrentLayer() == null) {
      throw new Exception("Layer not found");
    }
    if (regionVertices.isEmpty()) {
      throw new Exception("No region vertex found");
    }
    if (!selectedRegion) {
      throw new Exception("Region not closed");
    }
    serviceExecutor.execute(() -> {
      try {
        project.pave(patternTemplate, getCurrentLayer(), new Area(currentSelectedRegion));
        clearSelectingRegion(true);
      } catch (Exception e) {
        this.handleException(e);
      }
    });
  }

  public boolean hasSelectedRegion() {
    return selectedRegion;
  }

  public Path2D.Double getCurrentSelectedRegion() {
    return currentSelectedRegion;
  }

  public List<Point2D.Double> getRegionVertices() {
    return regionVertices;
  }

  public void addNewRegionVertex(Point2D.Double clickSpotInReal) {
    regionVertices.add(clickSpotInReal);
    if (regionVertices.size() == 1) // the first vertex
    {
      currentSelectedRegion.moveTo(clickSpotInReal.x, clickSpotInReal.y);
    } else {
      currentSelectedRegion.lineTo(clickSpotInReal.x, clickSpotInReal.y);
    }
    setChanged();
    notifyObservers();
  }

  public void closeSelectedRegion() {
    currentSelectedRegion.closePath();
    selectedRegion = true;
    selectingRegion = false;
    setChanged();
    notifyObservers();
  }

  public void paveFill(PatternTemplate patternTemplate) throws Exception {
    if (project == null) {
      throw new Exception("Project not found");
    }
    if (getCurrentLayer() == null) {
      throw new Exception("Layer not found");
    }
    if (!getCurrentLayer().isPaved()) {
      throw new Exception("Layer not paved");
    }
    project.pave(patternTemplate, getCurrentLayer(), availableArea);
  }

  public void toggle(String property) {
    switch (property) {
      case SHOW_SLICE:
        setShowSlice(!showingSlice());
        break;
      case SHOW_LIFT_POINTS:
        setShowLiftPoints(!showingLiftPoints());
        break;
      case SHOW_BITS_NOT_FULL_LENGTH:
        setShowBitsNotFullLength(!showBitNotFull);
        break;
      case SHOW_PREVIOUS_LAYER:
        setShowPreviousLayer(!showingPreviousLayer());
        break;
      case SHOW_CUT_PATHS:
        setShowCutPaths(!showingCutPaths());
        break;
      case SHOW_IRREGULAR_BITS:
        setShowIrregularBits(!showingIrregularBits());
        break;
      case ADDING_BITS:
        setAddingBits(!isAddingBits());
        break;
      case MANIPULATNG_BIT:
        if(selectedBitKeys.size()==1){
          manipulating=true;
          deleteSelectedBits();
          setAddingBits(true);
        }
        else {if(!manipulating)Logger.error("you have to select a bit");
          manipulating=false;
          setAddingBits(false);
          if(selectedBitKeys.size()>1)Logger.error("you have to select only one bit");
          Thread t=new Thread(() -> {
            try {
              Thread.sleep(3000);
            } catch (InterruptedException e) {
              throw new RuntimeException(e);
            }
            Logger.updateStatus(null);
          });
          t.start();
        }
        break;
      case SELECTING_REGION:
        setSelectingRegion(!isSelectingRegion());
        break;
    }
  }

  public void setShowSlice(boolean b) {
    showSlice = b;
    System.out.println("showSlice = " + showSlice);

    changes.firePropertyChange(SHOW_SLICE, !showSlice, showSlice);

    setChanged();
    notifyObservers();
  }

  public boolean showingSlice() {
    return showSlice;
  }

  public void setShowLiftPoints(boolean b) {
    showLiftPoints = b;

    changes.firePropertyChange(SHOW_LIFT_POINTS, !showLiftPoints, showLiftPoints);

    setChanged();
    notifyObservers();
  }

  public void setShowBitsNotFullLength(Boolean b) {
    showBitNotFull = b;
    changes.firePropertyChange(SHOW_BITS_NOT_FULL_LENGTH, !showBitNotFull, showBitNotFull);

    setChanged();
    notifyObservers();
  }

  public boolean showingLiftPoints() {
    return showLiftPoints;
  }

  public boolean showingBitNotFull() {
    return showBitNotFull;
  }

  ;


  public void setShowPreviousLayer(boolean b) {
    showPreviousLayer = b;

    changes.firePropertyChange(SHOW_PREVIOUS_LAYER, !showPreviousLayer, showPreviousLayer);

    setChanged();
    notifyObservers();
  }

  public boolean showingPreviousLayer() {
    return showPreviousLayer;
  }

  public void setShowCutPaths(boolean b) {
    showCutPaths = b;

    changes.firePropertyChange(SHOW_CUT_PATHS, !showCutPaths, showCutPaths);

    setChanged();
    notifyObservers();
  }

  public boolean showingCutPaths() {
    return showCutPaths;
  }

  public void setShowIrregularBits(boolean b) {
    showIrregularBits = b;

    changes.firePropertyChange(SHOW_IRREGULAR_BITS, !showIrregularBits, showIrregularBits);

    setChanged();
    notifyObservers();
  }

  public boolean showingIrregularBits() {
    return showIrregularBits;
  }

  boolean isAddingBits() {
    return addingBits;
  }

  public void setAddingBits(boolean b) {
    this.addingBits = b;

    changes.firePropertyChange(ADDING_BITS, !addingBits, addingBits);

    setChanged();
    notifyObservers();
  }

  public boolean isSelectingRegion() {
    return selectingRegion;
  }

  public void setSelectingRegion(boolean b) {
    this.selectingRegion = b;
    changes.firePropertyChange(SELECTING_REGION, !selectingRegion, selectingRegion);

    if (!selectingRegion) {
      clearSelectingRegion(false);
    }

    setChanged();
    notifyObservers();
  }

  public boolean get(String property) {
    switch (property) {
      case SHOW_SLICE:
        return showingSlice();
      case SHOW_LIFT_POINTS:
        return showingLiftPoints();
      case SHOW_PREVIOUS_LAYER:
        return showingPreviousLayer();
      case SHOW_CUT_PATHS:
        return showingCutPaths();
      case SHOW_IRREGULAR_BITS:
        return showingIrregularBits();
      case ADDING_BITS:
        return isAddingBits();
      case SELECTING_REGION:
        return isSelectingRegion();
    }
    return false;
  }

  public void moveSelectedBits(Vector2 direction) {
   /* // Save before doing
    Set<Bit3D> cloned = getSelectedBits();
    Set<Vector2> previousSelectedBits = new HashSet<>(this.getSelectedBitKeys());
    //move bits
   */
 /*Iterator<Bit3D> it=getSelectedBits().iterator();
    Bit3D bitToMove=it.next();
     getCurrentLayer().moveBit(bitToMove, direction);

    double distance = 0;
    if (direction.x == 0) {// up or down
      distance = CraftConfig.bitWidth / 2;
    } else if (direction.y == 0) {// left or right
      distance = CraftConfig.lengthFull / 2;
    }
Bit2D bitToMove2D=bitToMove.getBaseBit();

    Vector2 translationInMesh =
            direction.rotate(bitToMove2D.getOrientation())
                    .normal()
                    .mul(distance);
    Vector2 newOrigin = bitToMove2D.getOriginCS()
            .add(translationInMesh);*/


  /*  Set<Bit3D> bits3D = this.getSelectedBits();
    //Save after moved
    Set<Vector2> resultKeys = new HashSet<>(getSelectedBitKeys());
    //create new ActionMoveBit for save action
    this.handlerRedoUndo.addActionBit(
        new ActionOfUserMoveBit(cloned, previousSelectedBits, resultKeys, bits3D,
            getCurrentLayer().getLayerNumber()));
*/  }




  /**
   * Add new bit key to {@link #selectedBitKeys} and remove if already present
   *
   * @param clickSpot in {@link Project} coordinate system
   */
  public void toggleInclusionOfBitHaving(Point2D.Double clickSpot) {
    Vector2 bitKey = findBitAt(clickSpot);
    SubBit2D sub=findSubBitAt(clickSpot);
    if (project == null || !project.isPaved() || bitKey == null || sub==null) {
      return;
    }
    if (!selectedBitKeys.add(bitKey)) {
      selectedBitKeys.remove(bitKey);
      changes.firePropertyChange(BIT_UNSELECTED, null, getCurrentLayer().getBit3D(bitKey));
    } else {
      changes.firePropertyChange(BIT_SELECTED, null, getCurrentLayer().getBit3D(bitKey));
    }
    // Notify the core
    setChanged();
    notifyObservers();
  }

  public void toggleInclusionOfSubBitHaving(Point2D.Double clickSpot) {
    SubBit2D subBit=findSubBitAt(clickSpot);
    Vector2 bitKey = findBitAt(clickSpot);
    if (project == null || !project.isPaved() || subBit == null) {
      return;
    }
    if (selectedsubBit.contains(subBit)) {
       selectedBitKeysOfSubbit .remove(bitKey);
      selectedsubBit.remove(subBit);
      changes.firePropertyChange(SubBIT_UNSELECTED, null, subBit);
    } else {
      selectedBitKeysOfSubbit.add(bitKey);
      selectedsubBit.add(subBit);
      changes.firePropertyChange(SubBIT_SELECTED, null, subBit);
    }
    // Notify the core
    setChanged();
    notifyObservers();
  }


  /**
   * Call to back to step previous
   */
  public void undo() {
    if (handlerRedoUndo.getPreviousActionOfUserBits() != null
        && !handlerRedoUndo.getPreviousActionOfUserBits()
        .isEmpty()) {
      handlerRedoUndo.undo(this);
    }
  }

  public void redo() {
    if (handlerRedoUndo.getPreviousActionOfUserBits() != null
        && handlerRedoUndo.getAfterActionOfUserBits()
        .size() != 0) {
      handlerRedoUndo.redo(this);
    }
  }

  @Override
  public void onUndoListener(HandlerRedoUndo.ActionOfUser a) {
    a.runUndo(this);
  }

  @Override
  public void onRedoListener(HandlerRedoUndo.ActionOfUser a) {
    a.runRedo(this);
  }

  public void resetAll() {
    resetMesh();
    layerNumber = -1;
    selectedBitKeys.clear();
    selectedsubBit.clear();
    selectedsubBitMemory.clear();
    zoom = 1;
    showSlice = true;
    showLiftPoints = false;
    showPreviousLayer = false;
    showCutPaths = false;
    showIrregularBits = false;
    addingBits = false;
    regionVertices.clear();
    currentSelectedRegion = new Path2D.Double();
    availableArea = null;
    bitAreaPreview = null;
    layerNumber = -1;
    currentPoint = null;
    areaHoldingCut = null;
    fullLength = true;
    handlerRedoUndo.reset();
    setChanged();
    notifyObservers();
  }

  /**
   * Convenient class to run async tasks
   */
  private abstract class ProjectOperator extends Observable implements Runnable {

    final File file;

    ProjectOperator(File file) {
      this.file = file;
    }
  }

  private class ProjectCreator extends ProjectOperator {

    ProjectCreator(File file) {
      super(file);
    }

    @Override
    public void run() {
      setMesh(new Project());
      setChanged();
      notifyObservers(ProjectEvents.READY);
      String filename = file.toString();
      try {
        project.importModel(filename); // sync task
      } catch (Exception e) {
        handleException(e);
        setChanged();
        notifyObservers(ProjectEvents.IMPORT_FAILED);
      }
    }
  }

  private class ProjectSaver extends ProjectOperator {

    ProjectSaver(File file) {
      super(file);
    }

    @Override
    public void run() {
      try {
        project.saveAs(file);
        Logger.updateStatus("Project saved at " + file.getPath());
      } catch (IOException e) {
        handleException(e);
      }
    }
  }

  private class ProjectOpener extends ProjectOperator {

    ProjectOpener(File file) {
      super(file);
    }

    @Override
    public void run() {
      try {
        setMesh(Project.open(file));
        Logger.updateStatus("Project opened from " + file.getPath());
        // notify main window
        setChanged();
        notifyObservers(ProjectEvents.OPENED);
      } catch (ClassNotFoundException | IOException e) {
        handleException(e);
        setChanged();
        notifyObservers(ProjectEvents.OPEN_FAILED);
      }
    }
  }

}