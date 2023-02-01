/*
 * MeshIneBits is a Java software to disintegrate a 3d mesh (model in .stl)
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

package meshIneBits;

import meshIneBits.config.CraftConfig;
import meshIneBits.patterntemplates.PatternTemplate;
import meshIneBits.scheduler.AScheduler;
import meshIneBits.slicer.Slice;
import meshIneBits.slicer.SliceTool;
import meshIneBits.util.Logger;
import meshIneBits.util.MultiThreadServiceExecutor;
import meshIneBits.util.Segment2D;
import meshIneBits.util.SimultaneousOperationsException;
import meshIneBits.util.supportExportFile.MeshXMLTool;

import java.awt.geom.Area;
import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This object is the equivalent of the piece which will be printed
 */
public class Mesh extends Observable implements Observer, Serializable {

  private static final long serialVersionUID = 20180000400L;

  private Vector<Layer> layers = new Vector<>();
  private Vector<Slice> slices = new Vector<>();
  private double skirtRadius;
  private transient SliceTool slicer;
  private Model model;
  private MeshEvents state;
  private AScheduler scheduler = CraftConfig.schedulerPreloaded[0];
  private String modelFile;
  private ArrayList<ArrayList<Strip>> stripes=new ArrayList<>();


  /**
   * Set the new mesh to ready
   */
  public Mesh() {
    setState(MeshEvents.READY);
    this.scheduler.setMesh(this);
  }
  public Object clone()throws CloneNotSupportedException{
    return super.clone();
  }
  public static Mesh open(File file) throws IOException, ClassNotFoundException {
    Logger.message("open starts");
    try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
      Mesh mesh = (Mesh) ois.readObject();
      return mesh;
    }
  }

  /**
   * Register a model given a path
   *
   * @param filepath model file to load
   * @throws Exception when another action is currently executing
   */
  public void importModel(String filepath) throws Exception {
    if (state.isWorking()) {
      throw new SimultaneousOperationsException(this);
    }

    setState(MeshEvents.IMPORTING);
    Logger.updateStatus("Importing model from " + filepath);
    try {
      this.model = new Model(filepath);
      this.model.center();
      this.modelFile = filepath;
    } catch (Exception e) {
      e.printStackTrace();
      setState(MeshEvents.IMPORT_FAILED);
      return;
    }

    // Crash all slices and layers
    slices.clear();
    layers.clear();

    Logger.updateStatus("Model from " + filepath + " imported. Ready to slice");
    // Signal to update
    setState(MeshEvents.IMPORTED);
  }
public ArrayList<ArrayList<Strip>> getStripes(){

    return stripes;
}

  public void setStripes(ArrayList<ArrayList<Strip>> stripes) {
    if(!this.stripes.isEmpty()) this.stripes.clear();
    this.stripes = new ArrayList<>(stripes);
  }

  /**
   * Start slicing the registered model and generating layers
   *
   * @throws Exception when an other action is currently executing
   */
  public void slice() throws Exception {
    if (state.isWorking()) {
      throw new SimultaneousOperationsException(this);
    }

    setState(MeshEvents.SLICING);
    // clean before executing
    slices.clear();
    layers.clear();
    // start
    double zMin = this.model.getMin().z;
    if (zMin != 0) {
      this.model.center(); // recenter before slicing
    }
    slicer = new SliceTool(this);
    slicer.sliceModel();
    // MeshEvents.SLICED will be sent in update() after receiving
    // signal from slicer
  }

  /**
   * Given a certain template, pave the whole mesh sequentially
   *
   * @param template an automatic builder
   * @throws Exception when an other action is currently executing or {@link Mesh} is not sliced
   *                   yet
   */
  public void pave(PatternTemplate template) throws Exception {
    getLayers().forEach(layer -> layer.getRemovedSubBitsPositions().clear());
    pavementSafetyCheck();
    setState(MeshEvents.PAVING_MESH);
    // MeshEvents.PAVED_MESH will be sent in update() after receiving
    // enough signals from layers
    Logger.updateStatus("Ready to generate bits");
    template.ready(this);
    // New worker
    if (template.isInterdependent()) {
      SequentialPavingWorker sequentialPavingWorker = new SequentialPavingWorker(template);
      sequentialPavingWorker.addObserver(this);
//            (new Thread(sequentialPavingWorker)).start();
      sequentialPavingWorker.run();
    } else {
      PavingWorkerMaster pavingWorkerMaster = new PavingWorkerMaster(template);
      pavingWorkerMaster.addObserver(this);
//            (new Thread(pavingWorkerMaster)).start();
      pavingWorkerMaster.run();
    }
  }

  private void pavementSafetyCheck() throws Exception {
    if (state.isWorking()) {
      throw new SimultaneousOperationsException(this);
    }
    if (!isSliced()) {Logger.updateStatus("make sure the Mesh is Sliced");
      throw new Exception("The mesh cannot be paved until it is sliced");

    }
  }

  public boolean isSliced() {
    return state.getCode() >= MeshEvents.SLICED.getCode();
  }

  /**
   * Start the auto optimizer embedded in each template of each layer if presenting
   *
   * @throws Exception when an other action is currently executing
   */
  public void optimize() throws Exception {
    optimizationSafetyCheck();

    setState(MeshEvents.OPTIMIZING_MESH);
    // MeshEvents.OPTIMIZED_MESH will be sent in update() after receiving
    // enough signals from layers
    MeshOptimizerMaster meshOptimizerMaster = new MeshOptimizerMaster();
    meshOptimizerMaster.addObserver(this);
    Thread t = new Thread(meshOptimizerMaster);
    t.start();
  }

  private void optimizationSafetyCheck() throws Exception {
    if (state.isWorking()) {
      throw new SimultaneousOperationsException(this);
    }
    if (!isPaved()) {
      throw new Exception("The mesh cannot be auto optimized until it is fully paved.");
    }
  }

  /**
   * @return <tt>true</tt> if all {@link Layer} is paved
   */
  public  boolean isPaved() {
    if (state.getCode() >= MeshEvents.PAVED_MESH.getCode()) {
      return true;
    } else {
      if (layers.isEmpty()) {
        return false;
      }
      if (layers.stream()
          .allMatch(Layer::isPaved)) {
        state = MeshEvents.PAVED_MESH;
        return true;
      } else {
        return false;
      }
    }
  }

  /**
   * Run the optimizing algorithm of the layer
   *
   * @param layer layer of optimization
   * @throws Exception when an other action is currently executing
   */
  public void optimize(Layer layer) throws Exception {
    optimizationSafetyCheck();

    setState(MeshEvents.OPTIMIZING_LAYER);
    LayerOptimizer layerOptimizer = new LayerOptimizer(layer);
    layerOptimizer.addObserver(this);
    // MeshEvents.OPTIMIZED_LAYER will be sent after completed the task
    Thread t = new Thread(layerOptimizer);
    t.start();
  }

  /**
   * Calculate glue points and/or areas between layers
   */
  public void glue() {
    setState(MeshEvents.GLUING);
    // TODO run glue inserting in each layer
    // MeshEvents.GLUED will be sent in update() after receiving
    // enough signals from layers
  }

  /**
   * Export paving instructions
   *
   * @param file location to save instructions
   * @throws Exception when in working state or not paved
   */
  public void export(File file) throws Exception {
    exportationSafetyCheck();
    setState(MeshEvents.EXPORTING);
    MeshXMLExporter meshXMLExporter = new MeshXMLExporter(file);
//        Thread t = new Thread(meshXMLExporter);
//        t.start();
    meshXMLExporter.run();
    setState(MeshEvents.EXPORTED);
  }

  private void exportationSafetyCheck() throws Exception {
    if (state.isWorking()) {
      throw new SimultaneousOperationsException(this);
    }
    if (!isPaved()) {
      throw new Exception("Mesh in unpaved");
    }
  }

  public Vector<Layer> getLayers() {
    return this.layers;
  }

  public double getSkirtRadius() {
    return skirtRadius;
  }

  public Vector<Slice> getSlices() {
    return slices;
  }

  public Model getModel() {
    return model;
  }

  @Override
  public void update(Observable o, Object arg) {
    if (arg instanceof MeshEvents) {
      switch ((MeshEvents) arg) {
        case READY:
          break;
        case IMPORT_FAILED:
          break;
        case IMPORTING:
          break;
        case IMPORTED:
          break;
        case SLICING:
          break;
        case SLICED:
          // Slice job has been done
          // Slicer sends messages
          this.slices = slicer.getSlices();
          // sliced = true;
          setSkirtRadius();
          initLayers();
          setState(MeshEvents.SLICED);
          break;
        case PAVING_MESH:
          break;
        case PAVED_MESH:
          setState(MeshEvents.PAVED_MESH);
          break;
        case PAVING_LAYER:
          break;
        case PAVED_LAYER:
          Logger.updateStatus("Layer paved");
          setState(MeshEvents.PAVED_LAYER);
          break;
        case OPTIMIZING_LAYER:
          break;
        case OPTIMIZED_LAYER:
          setState(MeshEvents.OPTIMIZED_LAYER);
          break;
        case OPTIMIZING_MESH:
          break;
        case OPTIMIZED_MESH:
          setState(MeshEvents.OPTIMIZED_MESH);
          break;
        case GLUING:
          break;
        case GLUED:
          break;
        case OPENED:
          break;
        case OPEN_FAILED:
          break;
        case SAVED:
          break;
        case SAVE_FAILED:
          break;
        case EXPORTING:
          Logger.updateStatus("Exporting XML");
          break;
        case EXPORTED:
          Logger.updateStatus("XML exported");
          break;
        case SCHEDULING:
          setState(MeshEvents.SCHEDULING);
          break;
        case SCHEDULED:
          Logger.updateStatus("Mesh scheduled");
          setState(MeshEvents.SCHEDULED);
          break;
      }
    }
  }

  /**
   * skirtRadius is the radius of the cylinder that fully contains the part.
   */
  private void setSkirtRadius() {

    double radius = 0;

    for (Slice s : slices) {
      for (Segment2D segment : s.getSegmentList()) {
        if (segment.start.vSize2() > radius) {
          radius = segment.start.vSize2();
        }
        if (segment.end.vSize2() > radius) {
          radius = segment.end.vSize2();
        }
      }
    }
    skirtRadius = Math.sqrt(radius);
    Logger.updateStatus("Skirt's radius: " + ((int) skirtRadius + 1) + " mm");
  }

  /**
   * Generate empty layers
   */
  private void initLayers() {
    Logger.updateStatus("Generating layers");
    int jobsize = slices.size();
    for (int i = 0; i < jobsize; i++) {
      Layer layer = new Layer(i, slices.get(i));
      layers.add(layer);
      Logger.setProgress(i + 1, jobsize);

    }
  }

  public AScheduler getScheduler() {
    if (scheduler != null) {
      return scheduler;
    }

    return null;
  }

  /**
   * Scheduling Part
   */

  public void setScheduler(AScheduler s) {
    Logger.message("Set scheduler to: " + s.toString());
    scheduler = s;
    s.setMesh(this);
    scheduler.addObserver(this);
  }

  public void runScheduler() throws Exception {
    if (state.isWorking()) {
      throw new SimultaneousOperationsException(this);
    }
    if (scheduler == null) {
      throw new SchedulerNotDefinedException();
    }
    Logger.updateStatus("Scheduling mesh");
    // Scheduler will send a signal MeshEvents.SCHEDULED to Mesh.update()
//        (new Thread(scheduler)).start();
    scheduler.run();
  }

  public MeshEvents getState() {
    return state;
  }

  /**
   * Update the current state of mesh and notify observers with that state
   *
   * @param state a value from predefined list
   */
  public void setState(MeshEvents state) {
    this.state = state;
    setChanged();
    notifyObservers(state);
  }

  /**
   * Determine all empty or null layers to notify users
   */
  public List<Integer> getEmptyLayers() {
    List<Integer> indexes = new ArrayList<>();
    for (int i = 0; i < slices.size(); i++) {
      if (layers.get(i) == null
          || layers.get(i)
          .getFlatPavement()
          .getBitsKeys()
          .size() == 0) {
        indexes.add(i);
      }
    }
    return indexes;
  }

  /**
   * Pave certain layer
   *
   * @param patternTemplate maybe different from global choice
   * @param layer           target
   */
  public void pave(PatternTemplate patternTemplate, Layer layer) throws Exception {
    pavementSafetyCheck();

    setState(MeshEvents.PAVING_LAYER);
    // MeshEvents.PAVED_LAYER will be sent to update()
    Logger.updateStatus("Paving layer " + layer.getLayerNumber());
    // New worker
    LayerPaver layerPaver = new LayerPaver(layer, patternTemplate);
    layerPaver.addObserver(this);
//        (new Thread(layerPaver)).start();
    layerPaver.run();
  }

  /**
   * Pave a certain region on layer
   *
   * @param patternTemplate maybe different from pattern of layer
   * @param layer           target
   * @param region          should be closed. Expressed in layer's coordinate system
   */
  public void pave(PatternTemplate patternTemplate, Layer layer, Area region) throws Exception {
    pavementSafetyCheck();

    setState(MeshEvents.PAVING_LAYER);
    // MeshEvents.PAVED_LAYER will be sent to update()
    Logger.updateStatus("Paving region on layer " + layer.getLayerNumber());
    // New worker
    RegionPaver regionPaver = new RegionPaver(layer, region, patternTemplate);
    regionPaver.addObserver(this);
//        (new Thread(regionPaver)).start();
    regionPaver.run();

  }

  public void saveAs(File file) throws IOException {
    //this.convertAllLayerHorizontalAreaToPath2D();
    try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
      System.out.println(file.getPath());
      oos.writeObject(this);

      oos.flush();
      setChanged();
      notifyObservers(MeshEvents.SAVED);
    } catch (IOException e) {
      setChanged();
      notifyObservers(MeshEvents.SAVE_FAILED);
      throw e;
    }
  }

  public String getModelFile() {
    return modelFile;
  }

  public int countIrregularities() {
    return layers.stream()
        .mapToInt(layer -> layer.getKeysOfIrregularBits()
            .size())
        .sum();
  }
  public int countBits() {
    return layers.stream()
            .mapToInt(layer -> layer.getBitsNb())
            .sum();
  }
  /**
   * In charge of paving layers sequentially
   */
  private class SequentialPavingWorker extends Observable implements Runnable {

    private PatternTemplate patternTemplate;

    /**
     * Pave the layer following a certain template
     *
     * @param patternTemplate how we want to pave a layer
     */
    SequentialPavingWorker(PatternTemplate patternTemplate) {
      this.patternTemplate = patternTemplate;
    }

    @Override
    public void run() {
      buildLayers();
      setChanged();
      notifyObservers(MeshEvents.PAVED_MESH);
    }

    /**
     * Construct layers from slices then pave them
     */
    private void buildLayers() {
      Logger.updateStatus("Paving layers sequentially with " + patternTemplate.getCommonName());
      int jobsize = slices.size();
      for (int i = 0; i < jobsize; i++) {
        layers.get(i)
            .setPatternTemplate(patternTemplate);

        layers.get(i)
            .startPaver();

        Logger.setProgress(i + 1, jobsize);

      }
      Logger.updateStatus(layers.size() + " layers have been paved");
    }


  }

  /**
   * Simultaneously pave all layers
   */
  private class PavingWorkerMaster extends Observable implements Observer, Runnable {

    private Map<Layer, PavingWorkerSlave> jobsMap = new ConcurrentHashMap<>();
    private int jobsTotalCount = 0;
    private int finishedJobsCount = 0;
    private PatternTemplate originalPatternTemplate;

    PavingWorkerMaster(PatternTemplate patternTemplate) {
      originalPatternTemplate = patternTemplate;
      layers.forEach(layer -> {
        try {
          PavingWorkerSlave pavingWorkerSlave = new PavingWorkerSlave(
              (PatternTemplate) patternTemplate.clone(),
              layer
          );
          pavingWorkerSlave.addObserver(this);
          jobsMap.put(layer, pavingWorkerSlave);
          jobsTotalCount++;
        } catch (CloneNotSupportedException e) {
          e.printStackTrace();
        }
      });
    }

    @Override
    public void run() {
      Logger.updateStatus("Paving mesh parallelly with " + originalPatternTemplate.getCommonName());
      jobsMap.forEach((layer, pavingWorkerSlave)
          -> MultiThreadServiceExecutor.instance.execute(pavingWorkerSlave));
    }

    @Override
    public synchronized void update(Observable o, Object arg) {
      if (o instanceof PavingWorkerSlave) {
        finishedJobsCount++;
        Logger.setProgress(finishedJobsCount, jobsTotalCount);
      }
      if (finishedJobsCount == jobsTotalCount) {
        // Finished all
        Logger.updateStatus(layers.size() + " layers have been paved");
        // Notify
        setChanged();
        notifyObservers(MeshEvents.PAVED_MESH);
      /*if(WindowStatus==true) {
        processor.onTerminated();
        uipwAnimation.closeWindow();
        uipwView.closeWindow();
        uipwController.close();

      }*/
      }

    }
  }

  /**
   * Separated thread to pave a certain layer
   */
  private class PavingWorkerSlave extends Observable implements Runnable {

    private PatternTemplate patternTemplate;
    private Layer layer;

    PavingWorkerSlave(PatternTemplate patternTemplate, Layer layer) {
      this.patternTemplate = patternTemplate;
      this.layer = layer;
    }

    @Override
    public void run() {
      layer.setPatternTemplate(patternTemplate);
      layer.startPaver();
      setChanged();
      notifyObservers();
    }
  }

  /**
   * Managing optimization of a layer, then reporting to {@link MeshOptimizerMaster} or {@link
   * Mesh}
   */
  private class LayerOptimizer extends Observable implements Runnable {

    private Layer layer;
    private int irregularitiesRest;

    LayerOptimizer(Layer layer) {
      this.layer = layer;
    }

    @Override
    public void run() {
      Logger.updateStatus("Auto-optimizing layer " + layer.getLayerNumber());
      irregularitiesRest = layer.getPatternTemplate()
          .optimize(layer);
      if (irregularitiesRest <= 0) {
        switch (irregularitiesRest) {
          case 0:
            Logger.updateStatus("Auto-optimization succeeded on layer " + layer.getLayerNumber());
            break;
          case -1:
            Logger.updateStatus("Auto-optimization failed on layer " + layer.getLayerNumber());
            break;
          case -2:
            Logger.updateStatus(
                "No optimizing algorithm implemented on layer " + layer.getLayerNumber());
            break;
        }
      } else {
        Logger.updateStatus(
            "Auto-optimization for layer " + layer.getLayerNumber() + " done. " + irregularitiesRest
                + " unsolved irregularities.");
      }
      setChanged();
      notifyObservers(MeshEvents.OPTIMIZED_LAYER);
    }
  }

  /**
   * Managing process of optimizing consequently all layers, then reporting to {@link Mesh}
   */
  private class MeshOptimizerMaster extends Observable implements Runnable, Observer {

    private int irregularitiesRest = 0;
    private int finishedJob = 0;
    private List<Layer> uncleanLayers = new ArrayList<>();
    private List<Layer> unsolvedLayers = new ArrayList<>();
    private List<MeshOptimizerSlave> slaves = new ArrayList<>();

    MeshOptimizerMaster() {
      for (Layer layer : layers) {
        MeshOptimizerSlave slave = new MeshOptimizerSlave(layer);
        slave.addObserver(this);
        slaves.add(slave);
      }
    }

    @Override
    public void run() {
      slaves.forEach(meshOptimizerSlave -> (new Thread(meshOptimizerSlave)).start());
    }

    @Override
    public synchronized void update(Observable o, Object arg) {
      if (o instanceof MeshOptimizerSlave) {
        finishedJob++;
        Logger.setProgress(finishedJob, layers.size());
        int ir = (int) arg;
        Layer layer = ((MeshOptimizerSlave) o).getLayer();
        if (ir > 0) {
          irregularitiesRest += ir;
          uncleanLayers.add(layer);
          Logger.updateStatus("Optimized layer " + layer.getLayerNumber() + ". Still has " + ir
              + " irregular bits unsolved");
        } else {
          switch (ir) {
            case 0:
              Logger.updateStatus(
                  "Optimized layer " + layer.getLayerNumber() + ". No irregular bits left.");
              break;
            case -1:
              Logger.updateStatus("Auto-optimization failed on layer " + layer.getLayerNumber());
              unsolvedLayers.add(layer);
              break;
            case -2:
              Logger.updateStatus(
                  "No optimizing algorithm implemented on layer " + layer.getLayerNumber());
              unsolvedLayers.add(layer);
              break;
          }
        }
      }
      if (finishedJob == layers.size()) {
        // Finished
        StringBuilder str = new StringBuilder();
        StringBuilder str2 = new StringBuilder();
        unsolvedLayers.sort(Comparator.comparingInt(Layer::getLayerNumber));
        uncleanLayers.sort(Comparator.comparingInt(Layer::getLayerNumber));
        for (Layer unsolvedLayer : unsolvedLayers) {
          str.append(unsolvedLayer.getLayerNumber())
              .append(" ");
        }
        for (Layer uncleanLayer : uncleanLayers) {
          str2.append(uncleanLayer.getLayerNumber())
              .append(" ");
        }
        Logger.updateStatus("Optimization completed. "
            + (irregularitiesRest == 0 ? ""
            : irregularitiesRest + " irregularities left on layers " + str2.toString() + ". ")
            + (str.toString()
            .equals("") ? ""
            : "Some layers are not optimizable: " + str.toString() + ". ")
        );
        setChanged();
        notifyObservers(MeshEvents.OPTIMIZED_MESH);
      }
    }
  }

  private class MeshOptimizerSlave extends Observable implements Runnable {

    private Layer layer;

    MeshOptimizerSlave(Layer layer) {
      this.layer = layer;
    }

    public Layer getLayer() {
      return layer;
    }

    @Override
    public void run() {
      int irregularitiesLeft = layer.getPatternTemplate()
          .optimize(layer);
      setChanged();
      notifyObservers(irregularitiesLeft);
    }
  }

  private class MeshXMLExporter extends Observable implements Runnable {

    private final File file;

    MeshXMLExporter(File file) {
      this.file = file;
    }

    @Override
    public void run() {
//            XmlTool2 xt = new XmlTool2(Mesh.this, file.toPath());
//            xt.writeXmlCode();
      MeshXMLTool xt = new MeshXMLTool(file.toPath());
      xt.writeMeshToXML(Mesh.this);
      setChanged();
      notifyObservers(MeshEvents.EXPORTED);
    }
  }

  private class LayerPaver extends Observable implements Runnable {

    private final Layer layer;
    private final PatternTemplate patternTemplate;

    LayerPaver(Layer layer, PatternTemplate patternTemplate) {
      this.layer = layer;
      this.patternTemplate = patternTemplate;
      patternTemplate.ready(Mesh.this);
    }

    @Override
    public void run() {
      layer.setPatternTemplate(patternTemplate);
      layer.startPaver();
      setChanged();
      notifyObservers(MeshEvents.PAVED_LAYER);
    }
  }

  private class SchedulerNotDefinedException extends Exception {

    SchedulerNotDefinedException() {
      super("The scheduling method is not defined in the mesh object");
    }
  }

  private class RegionPaver extends Observable implements Runnable {

    private final Layer layer;
    private final transient Area region;
    private final PatternTemplate patternTemplate;

    RegionPaver(Layer layer, Area region, PatternTemplate patternTemplate) {
      this.layer = layer;
      this.region = region;
      this.patternTemplate = patternTemplate;
    }

    @Override
    public void run() {
      patternTemplate.ready(Mesh.this);
      layer.paveRegion(region, patternTemplate);
      setChanged();
      notifyObservers(MeshEvents.PAVED_LAYER);
    }
  }

}