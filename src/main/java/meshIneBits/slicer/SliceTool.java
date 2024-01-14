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

package meshIneBits.slicer;

import java.util.Observable;
import java.util.Vector;
import meshIneBits.Project;
import meshIneBits.ProjectEvents;
import meshIneBits.Model;
import meshIneBits.config.CraftConfig;
import meshIneBits.util.Logger;
import meshIneBits.util.Segment2D;
import meshIneBits.util.Triangle;
import meshIneBits.util.Vector3;

/**
 * The slice tool slices the model into slices, it does so by going trough all model triangles and
 * slice those into 2D lines. <br>
 * <img src="./doc-files/slices.png" alt=""> <br>
 * For each slice, it go through all the triangles of the model. If the z of the slice is between
 * the z_min and the z_max of the triangle, then it call the method {@link
 * Triangle#project2D(double)}, which returns a {@link Segment2D} (class representing in a plane
 * since it admits only Coordinates in x and y). Each segment is then recorded in the {@link Slice}
 * object. At the end of {@link #sliceModel()} we get a collection of slices composed of segments
 * that form the outline of the project.
 */
public class SliceTool extends Observable implements Runnable {

  private Model model;
  private Vector<Slice> slices = new Vector<>();

  /**
   * SliceTool register itself to a {@link Project}, which is updated when the slicing is finished.
   *
   * @param project {@link Project} that will collect the slices when slicing is finished.
   */
  public SliceTool(Project project) {
    addObserver(project);
    this.model = project.getModel();
  }

  /**
   * @return {@link Vector} of {@link Slice}
   */
  public Vector<Slice> getSlices() {
    return slices;
  }

  /**
   * Slicing algorithm. One slice per layer, and each slice stays right at the middle of layer
   */
  @Override
  public void run() {
    Logger.updateStatus("Slicing project");
    Vector3 modelMax = model.getMax();
    double sliceDistance = CraftConfig.bitThickness + CraftConfig.layersOffset;
    double firstSliceHeight =
        CraftConfig.firstSliceHeightPercent / 100 * CraftConfig.bitThickness; // right at the middle
    int sliceCount = (int) (Math.floor((modelMax.z - firstSliceHeight) / sliceDistance) + 1);

    for (int i = 0; i < sliceCount; i++) {
      Slice s = new Slice();
      s.setAltitude(firstSliceHeight + i * sliceDistance);
      slices.add(s); // holder
    }

    int n = 0;
    int totalProgress = model.getTriangles()
        .size() + sliceCount;
    for (Triangle t : model.getTriangles()) {
      Logger.setProgress(++n, totalProgress);

      // Finding zMin between 3 vertices
      double zMin = t.point[0].z;
      if (t.point[1].z < zMin) {
        zMin = t.point[1].z;
      }
      if (t.point[2].z < zMin) {
        zMin = t.point[2].z;
      }

      // Finding zMax between 3 vertices
      double zMax = t.point[0].z;
      if (t.point[1].z > zMax) {
        zMax = t.point[1].z;
      }
      if (t.point[2].z > zMax) {
        zMax = t.point[2].z;
      }

      // Project each segment on slices
      int inf = (int) Math.floor(
          (zMin - firstSliceHeight) / sliceDistance); // index of lowest floor above zMin
      int sup = (int) Math.floor(
          (zMax - firstSliceHeight) / sliceDistance); // index of highest floor under zMax
      for (int i = Math.max(inf, 0); i <= Math.min(sup, sliceCount - 1); i++) {
        Slice s = slices.get(i);
        double sliceZ = s.getAltitude();
        Segment2D project2D = t.project2D(sliceZ);
        if (project2D != null) {
          s.addModelSegment(project2D);
        }
      }
    }

    Logger.updateStatus("Optimizing slices");
    for (int i = 0; i < sliceCount; i++) {
      Logger.setProgress(++n, totalProgress);
      slices.get(i)
          .optimize();
    }

    Logger.updateStatus("Project sliced");
    setChanged();
    notifyObservers(ProjectEvents.SLICED);
  }

  /**
   * Start the slicing in a thread.
   */
  public void sliceModel() {
//        Thread t = new Thread(this);
//        t.start();
    this.run();
  }
}
