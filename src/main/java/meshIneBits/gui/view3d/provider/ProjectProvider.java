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

package meshIneBits.gui.view3d.provider;

import meshIneBits.Project;
import meshIneBits.Model;
import meshIneBits.gui.view3d.oldversion.ProcessingModelView;

import java.util.Observable;
import java.util.Observer;

/**
 * Observe {@link Project} and
 */
public class ProjectProvider extends Observable implements Observer  {

  private Project project;

  private static ProjectProvider instance;
  private Model model;

  public static ProjectProvider getInstance() {
    if (instance == null) {
      instance = new ProjectProvider();
    }
    return instance;
  }

  public static void closeInstance() {
    instance = null;
  }

  private ProjectProvider() {
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

  public Model getModel() {
    return model;
  }


  public boolean isAvailable() {
    return model != null;
  }


}
