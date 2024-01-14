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

package meshIneBits.gui.view3d.oldversion;

import java.util.Observable;
import java.util.Observer;
import meshIneBits.Project;
import meshIneBits.Model;

/**
 * Observe {@link Project} and
 */
 class ControllerView3D extends Observable implements Observer {

  private Project project;

  private static ControllerView3D instance;
  private Model model;

  public static ControllerView3D getInstance() {
    if (instance == null) {
      instance = new ControllerView3D();
    }
    return instance;
  }

  public static void closeInstance() {
    instance = null;
  }

  private ControllerView3D() {
  }

  public Project getCurrentMesh() {
    return project;
  }

  /**
   * @param project <tt>null</tt> to delete the current project
   */
  public void setMesh(Project project) {
    if (project != null) {
      this.project = project;
      project.addObserver(this);
      model = project.getModel();
    } else {
      this.project = null;
    }

    setChanged();
    notifyObservers(ProcessingModelView.IMPORTED_MODEL);
  }

  @Override
  public void update(Observable o, Object arg) {
    //if(ProjectEvents.PAVED_MESH.equals(arg)){
    setChanged();
    notifyObservers();
    //}
  }

  int getCurrentLayerNumber() {
    // TODO Implement toolbox in 3D view
    return project.getLayers()
        .size() - 1;
  }

  public Model getModel() {
    return model;
  }

  public void setModel(Model m) {
    model = m;
    setChanged();
    notifyObservers();
  }

  public boolean isAvailable() {
    return model != null;
  }


}
