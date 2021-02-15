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

package meshIneBits.gui.view3d;

import meshIneBits.Mesh;
import meshIneBits.MeshEvents;
import meshIneBits.Model;

import java.util.Observable;
import java.util.Observer;

/**
 * Observe {@link Mesh} and
 */
public class ControllerView3D extends Observable implements Observer {

    private Mesh mesh;

    private static ControllerView3D instance;
    private Model model;

    public static ControllerView3D getInstance() {
        if (instance == null)
            instance = new ControllerView3D();
        return instance;
    }

    private ControllerView3D() {
    }

    Mesh getCurrentMesh() {
        return mesh;
    }

    /**
     * @param mesh <tt>null</tt> to delete the current mesh
     */
    public void setMesh(Mesh mesh) {
        if (mesh != null) {
            this.mesh = mesh;
            mesh.addObserver(this);
            model = mesh.getModel();
        } else
            this.mesh = null;

        setChanged();
        notifyObservers(ProcessingModelView.IMPORTED_MODEL);
    }

    @Override
    public void update(Observable o, Object arg) {
        //if(MeshEvents.PAVED_MESH.equals(arg)){
            setChanged();
            notifyObservers();
        //}
    }

    int getCurrentLayerNumber() {
        // TODO Implement toolbox in 3D view
        return mesh.getLayers().size() - 1;
    }

    Model getModel() {
        return model;
    }

    void setModel(Model m) {
        model = m;
        setChanged();
        notifyObservers();
    }
    boolean isAvailable(){
        return model!=null;
    }

}
