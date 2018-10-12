/*
 * MeshIneBits is a Java software to disintegrate a 3d mesh (model in .stl)
 * into a network of standard parts (called "Bits").
 *
 * Copyright (C) 2016  Thibault Cassard & Nicolas Gouju.
 * Copyright (C) 2017-2018  TRAN Quoc Nhat Han.
 * Copyright (C) 2018 Vallon BENJAMIN.
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

package meshIneBits.gui;

import meshIneBits.Mesh;
import meshIneBits.MeshEvents;
import meshIneBits.Model;
import meshIneBits.gui.view3d.ProcessingModelView;
import meshIneBits.util.Logger;

import java.io.*;
import java.util.Observable;
import java.util.Observer;

/**
 * Observe {@link Mesh} and observed by {@link Toolbar}
 */

public class MainController extends Observable implements Observer {

    static private MainController instance;
    private Mesh currentMesh;
    private Model model;
    private MeshOpener meshOpener;

    private MainController() {

    }

    public static MainController getInstance() {
        if (instance == null)
            instance = new MainController();
        return instance;
    }

    /**
     * @param newMesh <tt>null</tt> to clear the current mesh
     */
    void setCurrentMesh(Mesh newMesh) {
        currentMesh = newMesh;
        if (newMesh != null)
            currentMesh.addObserver(this);
    }

    private void setMeshForWindows() {
        MainWindow.getInstance().get2DView().setCurrentMesh(currentMesh);
        MainWindow.getInstance().get3DView().setCurrentMesh(currentMesh);
        MainWindow.getInstance().getDemoView().setCurrentMesh(currentMesh);
    }

    @Override
    public void update(Observable o, Object arg) {
        if (arg == null) return;
        if (o == currentMesh) {
            setMeshForWindows();
        } else if (o == meshOpener) {
            MeshEvents ev = (MeshEvents) arg;
            if (ev == MeshEvents.OPENED) {
                // Reopened from save
                setMeshForWindows();
                setModel();
//                MainWindow.getInstance().getModelView().toggle();
                Logger.updateStatus("Mesh has been loaded into workspace.");
            } else if (ev == MeshEvents.OPEN_FAILED) {
                Logger.error("Unable to open the mesh.");
            }
        }
    }

    public Mesh getCurrentMesh() {
        return currentMesh;
    }

    /**
     * Call this method to attach model to Model View
     * after {@link #currentMesh} has loaded the model successfully
     */
    void setModel() {
        model = currentMesh.getModel();
        ((ProcessingModelView) MainWindow.getInstance().getModelView())
                .setModel(model);
    }

    Model getModel() {
        return model;
    }

    /**
     * Save the current mesh on disk
     *
     * @param f indicate location to save
     * @throws IOException when file not found or error on writing
     */
    void saveMesh(File f) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(f))) {
            oos.writeObject(currentMesh);
            oos.flush();
        }
    }

    /**
     * Restore a mesh into working space
     *
     * @param file location of saved mesh
     */
    void openMesh(File file) {
        if (meshOpener != null) {
            Logger.error("Currently opening another mesh. Please wait until the task finishes.");
            return;
        }
        meshOpener = new MeshOpener(file);
        meshOpener.addObserver(this);
        (new Thread(meshOpener)).start();
    }

    private class MeshOpener extends Observable implements Runnable {

        private final File file;

        MeshOpener(File file) {
            this.file = file;
        }

        @Override
        public void run() {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                setCurrentMesh((Mesh) ois.readObject());
                // notify main window
                setChanged();
                notifyObservers(MeshEvents.OPENED);
            } catch (ClassNotFoundException | IOException e) {
                e.printStackTrace();
                setChanged();
                notifyObservers(MeshEvents.OPEN_FAILED);
            }
        }
    }
}
