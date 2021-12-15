package meshIneBits.gui.view3d.util;

import java.util.Arrays;
import meshIneBits.gui.view3d.Visualization3DConfig;
import meshIneBits.util.Vector3;
import remixlab.dandelion.geom.Vec;

public class ModelInWorkspaceChecker {

  private final float printerX = Visualization3DConfig.PRINTER_X;
  private final float printerY = Visualization3DConfig.PRINTER_Y;
  private final float printerZ = Visualization3DConfig.PRINTER_Z;

  public static class WorkspaceCheckerResponse {

    private Boolean res;
    private final boolean[] borders = new boolean[6];

    public WorkspaceCheckerResponse() {
      Arrays.fill(borders, false);
    }

//    public boolean[] getBorders() {
//      return Arrays.copyOf(borders, 6);
//    }

    public boolean isExceedMinX() {
      return borders[0];
    }

    public boolean isExceedMaxX() {
      return borders[1];
    }

    public boolean isExceedMinY() {
      return borders[2];
    }

    public boolean isExceedMaxY() {
      return borders[3];
    }

    public boolean isExceedMinZ() {
      return borders[4];
    }

    public boolean isExceedMaxZ() {
      return borders[5];
    }


    public Boolean isInWorkspace() {
      if (res != null) {
        return res;
      } else {
        res = true;
        for (boolean border : borders) {
          if (border) {
            res = false;
            break;
          }
        }
      }
      return res;
    }
  }

  public WorkspaceCheckerResponse checkInWorkspace(Vector3 minShape, Vector3 maxShape) {
    WorkspaceCheckerResponse response = new WorkspaceCheckerResponse();
    float minX = -printerX / 2;
    float maxX = printerX / 2;
    float minY = -printerY / 2;
    float maxY = printerY / 2;
    float minZ = 0;
    @SuppressWarnings("all")
    float maxZ = printerZ;
    Vec minPos = new Vec((float) minShape.x, (float) minShape.y, (float) minShape.z);
    Vec maxPos = new Vec((float) maxShape.x, (float) maxShape.y, (float) maxShape.z);
    boolean inWorkspace = true;
    if (minPos.x() < minX) {
      inWorkspace = false;
      response.borders[0] = true;
    }
    if (maxPos.x() >= maxX) {
      inWorkspace = false;
      response.borders[1] = true;
    }
    if (minPos.y() < minY) {
      inWorkspace = false;
      response.borders[2] = true;
    }
    if (maxPos.y() >= maxY) {
      inWorkspace = false;
      response.borders[3] = true;
    }
    if (minPos.z() < minZ) {
      inWorkspace = false;
      response.borders[4] = true;
    }
    if (maxPos.z() >= maxZ) {
      inWorkspace = false;
      response.borders[5] = true;
    }
    response.res = inWorkspace;
    return response;
  }
}
