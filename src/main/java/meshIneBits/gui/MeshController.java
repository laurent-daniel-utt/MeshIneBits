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
import meshIneBits.util.Logger;
import meshIneBits.util.SimultaneousOperationsException;
import meshIneBits.util.XmlTool;

import java.io.*;
import java.util.Observable;
import java.util.Observer;

/**
 * Observe the {@link Mesh}
 */
public class MeshController implements Observer {

    private final MeshWindow meshWindow;
    private Mesh mesh;

    MeshController(MeshWindow meshWindow) {
        this.meshWindow = meshWindow;
    }

    public void setMesh(Mesh mesh) {
        this.mesh = mesh;
    }

    @Override
    public void update(Observable o, Object arg) {
        if (arg instanceof MeshEvents)
            switch ((MeshEvents) arg) {
                case READY:
                    break;
                case IMPORTING:
                    Logger.updateStatus("Importing model");
                    break;
                case IMPORTED:
                    Logger.updateStatus("Model imported. Mesh ready to slice.");
                    break;
                case SLICING:
                    break;
                case SLICED:
                    break;
                case PAVING_MESH:
                    break;
                case PAVED_MESH:
                    break;
                case OPTIMIZING_LAYER:
                    break;
                case OPTIMIZED_LAYER:
                    break;
                case OPTIMIZING_MESH:
                    break;
                case OPTIMIZED_MESH:
                    break;
                case GLUING:
                    break;
                case GLUED:
                    break;
                case OPENED:
                    meshWindow.getView3DWindow().setCurrentMesh(mesh);
                    break;
                case OPEN_FAILED:
                    Logger.error("Failed to open the mesh");
                    break;
                case SAVED:
                    break;
                case SAVE_FAILED:
                    Logger.error("Failed to save the mesh");
                    break;
                case EXPORTED:
                    break;
            }
    }

    /**
     * Restore a mesh into working space
     *
     * @param file location of saved mesh
     */
    void openMesh(File file) throws SimultaneousOperationsException {
        if (mesh != null && mesh.getState().isWorking())
            throw new SimultaneousOperationsException(mesh);
        MeshOpener meshOpener = new MeshOpener(file);
        meshOpener.addObserver(this);
        (new Thread(meshOpener)).start();
    }

    /**
     * Save the current mesh on disk
     *
     * @param file location to save
     */
    void saveMesh(File file) throws Exception {
        if (mesh == null)
            throw new Exception("Mesh not found");
        if (mesh.getState().isWorking())
            throw new SimultaneousOperationsException(mesh);
        MeshSaver meshSaver = new MeshSaver(file);
        meshSaver.addObserver(this);
        (new Thread(meshSaver)).start();
    }

    public void exportXML(File file) throws Exception {
        if (mesh == null)
            throw new Exception("Mesh not found");
        if (mesh.getState().isWorking())
            throw new SimultaneousOperationsException(mesh);
        if (mesh.getState().getCode() < MeshEvents.PAVED_MESH.getCode())
            throw new Exception("Mesh unpaved");
        MeshXMLExporter meshXMLExporter = new MeshXMLExporter(file);
        meshXMLExporter.addObserver(this);
        (new Thread(meshXMLExporter)).start();
    }

    public void newMesh(File file) throws SimultaneousOperationsException {
        if (mesh != null && mesh.getState().isWorking())
            throw new SimultaneousOperationsException(mesh);
        MeshCreator meshCreator = new MeshCreator(file);
        meshCreator.addObserver(this);
        (new Thread(meshCreator)).start();
    }

    private abstract class MeshOperator extends Observable implements Runnable {
        final File file;

        MeshOperator(File file) {
            this.file = file;
        }
    }

    private class MeshCreator extends MeshOperator {

        MeshCreator(File file) {
            super(file);
        }

        @Override
        public void run() {
            mesh = new Mesh();
            mesh.addObserver(MeshController.this);
            setChanged();
            notifyObservers(MeshEvents.READY);
            String filename = file.toString();
            try {
                mesh.importModel(filename); // sync task
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class MeshSaver extends MeshOperator {
        MeshSaver(File file) {
            super(file);
        }

        @Override
        public void run() {
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
                oos.writeObject(mesh);
                oos.flush();
                // notify main window
                setChanged();
                notifyObservers(MeshEvents.SAVED);
            } catch (IOException e) {
                e.printStackTrace();
                setChanged();
                notifyObservers(MeshEvents.SAVE_FAILED);
            }
        }
    }

    private class MeshOpener extends MeshOperator {

        MeshOpener(File file) {
            super(file);
        }

        @Override
        public void run() {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                setMesh((Mesh) ois.readObject());
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

    private class MeshXMLExporter extends MeshOperator {

        MeshXMLExporter(File file) {
            super(file);
        }

        @Override
        public void run() {
            XmlTool xt = new XmlTool(mesh, file.toPath());
            xt.writeXmlCode();
            setChanged();
            notifyObservers(MeshEvents.EXPORTED);
        }
    }
}