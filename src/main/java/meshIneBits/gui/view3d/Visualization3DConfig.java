package meshIneBits.gui.view3d;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import meshIneBits.config.CraftConfig;
import meshIneBits.gui.view3d.animation.AnimationProcessor.AnimationMode;
import meshIneBits.gui.view3d.animation.AnimationProcessor.AnimationOption;
import remixlab.dandelion.geom.Vec;

public class Visualization3DConfig {

  private static Dimension SCREEN_SIZE = Toolkit.getDefaultToolkit().getScreenSize();

  //V3D config
  public static final String VISUALIZATION_3D_WINDOW_TITLE = "MeshIneBits - Model view";
  public static final int V3D_WINDOW_WIDTH = SCREEN_SIZE.width * 3 / 5;
  public static final int V3D_WINDOW_HEIGHT = SCREEN_SIZE.height;
  public static final int V3D_WINDOW_LOCATION_X = SCREEN_SIZE.width/5;
  public static final int V3D_WINDOW_LOCATION_Y = 0;
  public static final Vec V3D_EYE_POSITION = new Vec(0, 1, 1);
  public static final int V3D_RADIUS = 2500;
  public static final int V3D_FRAMERATE = 60;
  public static final Color V3D_BACKGROUND = new Color(150, 150, 150);
  public static final Color V3D_AMBIENT_LIGHT= new Color(255, 255, 255);

  //AssemblingProcessView config
  public static final String ASSEMBLING_PROCESS_VIEW_TITLE = "MeshIneBits - Model view";
  public static final int ASSEMBLING_PROCESS_VIEW_WIDTH = SCREEN_SIZE.width * 3 / 5;
  public static final int ASSEMBLING_PROCESS_VIEW_HEIGHT = SCREEN_SIZE.height;
  public static final int ASSEMBLING_PROCESS_VIEW_LOCATION_X = SCREEN_SIZE.width/5;
  public static final int ASSEMBLING_PROCESS_VIEW_LOCATION_Y = 0;

  //UI parameter window config
  public static final int UIP_WINDOW_WIDTH = SCREEN_SIZE.width / 5;
  public static final int UIP_WINDOW_HEIGHT = SCREEN_SIZE.height;
  public static final int UIPW_FRAMERATE = 24;
  public static final Point UIPW_ANIMATION = new Point(SCREEN_SIZE.width-UIP_WINDOW_WIDTH,0);
  public static final Point UIPW_VIEW = new Point(0,0);
  public static final Color UIPW_BACKGROUND = new Color(150, 150, 150);
  public static final int EYE_POSITION_Y = 3000;
  public static final int EYE_POSITION_Z = 3000;
  public static final String EXPORT_3D_RENDERER = "meshIneBits.util.supportExportFile.obj.OBJExport";

  //other
  public static final float PRINTER_X = CraftConfig.printerX;
  public static final float PRINTER_Y = CraftConfig.printerY;
  public static final float PRINTER_Z = CraftConfig.printerZ;
  public static final float ASSEMBLY_WORKING_SPACE_WIDTH = CraftConfig.workingWidth;
  public static final float ASSEMBLY_WORKING_SPACE_HEIGHT = CraftConfig.printerY;


  //Shape config
  public static final Color MODEL_COLOR = new Color(219, 100, 50);
  public static final Color MESH_COLOR = new Color(112, 66, 20);
  public static final Color CUTTING_BIT_VIEW = new Color(112, 66, 20);

  public static final float BIT_THICKNESS = (float) CraftConfig.bitThickness;

  //Animation config
  public static final AnimationOption defaultAnimationOption = AnimationOption.BY_BATCH;
  public static final AnimationMode defaultAnimationMode = AnimationMode.FULL;
  public static final double speed_coefficient_default = 1.0; //=coefficient*time
  public static final double speed_coefficient_min = 2;
  public static final double speed_coefficient_max = 0.05;
  public static final double speed_level_number = 10;
  public static final long SECOND = 1000; //second

  //millisecond
  public static final long REFRESH_TIME = 3000;



}
