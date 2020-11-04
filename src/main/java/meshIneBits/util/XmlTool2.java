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

package meshIneBits.util;

import javafx.util.Pair;
import meshIneBits.Bit3D;
import meshIneBits.Layer;
import meshIneBits.Mesh;
import meshIneBits.config.CraftConfig;
import meshIneBits.scheduler.AScheduler;
import meshIneBits.scheduler.BasicScheduler;

import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.io.File;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.Vector;

public class XmlTool2  {

    private Mesh part;
    private PrintWriter writer;
    private Path mFilePath;
    private double effectiveWidth;
    private int nbBits;
    private int remainingBits;
    private double currentPos;

    public XmlTool2(Mesh part, Path filePath) {
        this.part = part;
        this.mFilePath = setFilePathToXML(filePath);
        getPrinterParameters();
    }

    private String getNameFromFileLocation() {
        return mFilePath.getFileName().toString().split("[.]")[0];
    }

    private boolean liftableBit(Bit3D bit) {
        int liftableSubBit = 0;
        for (Vector2 p : bit.getRawLiftPoints()) {
            if (p != null) {
                liftableSubBit++;
            }
        }
        if (liftableSubBit > 0) {
            return true;
        } else {
            return false;
        }
    }

    private void getPrinterParameters() {
        float workingWidth = CraftConfig.workingWidth;
        float margin = CraftConfig.margin;
        nbBits = CraftConfig.nbBits;
        effectiveWidth = workingWidth - margin;
        remainingBits = nbBits;
    }


    private Path setFilePathToXML(Path filePath) {
        String fileName = filePath.getFileName().toString();
        if (fileName.split("[.]").length >= 2) {
            fileName = fileName.split("[.]")[0];
        }
        fileName = fileName + "." + "xml";
        return Paths.get(filePath.getParent().toString() + File.separator + fileName);
    }

    private void startFile() {
        writer.println("	<name>" + getNameFromFileLocation() + "</name>");
        writer.println("	<date>" + (new Date()).toString() + "</date>");
        writer.println("	<bitDimension>");
        writer.println("		<height>" + CraftConfig.bitThickness + "</height>");
        writer.println("		<width>" + CraftConfig.bitWidth + "</width>");
        writer.println("		<length>" + CraftConfig.bitLength + "</length>");
        writer.println("	</bitDimension>");
        writer.println("	<partSkirt>");
        writer.println("		<height>" + (((part.getLayers().size() + CraftConfig.layersOffset) * CraftConfig.bitThickness) - CraftConfig.layersOffset) + "</height>");
        writer.println("		<radius>" + part.getSkirtRadius() + "</radius>");
        writer.println("	</partSkirt>");

    }

    private void moveWorkingSpace(Bit3D bit, int id) {
        if (remainingBits == 0) {
            writer.println("		<return>");
            writer.println("		</return>");
            remainingBits = nbBits;
        }
        for (int i = 0; i < bit.getLiftPoints().size(); i++) {
            if (bit.getLiftPoints().get(i) != null) {
                if (id == 0) {
                    writer.println("		<goTo>");
                    currentPos = bit.getLiftPoints().get(i).x + effectiveWidth / 2;
                    writer.println("			<x>" + currentPos + "</x>");
                    writer.println("		</goTo>");
                } else {
                    if (Math.abs(bit.getLiftPoints().get(i).x - currentPos) > effectiveWidth / 2) {
                        currentPos += effectiveWidth;
                        writer.println("		<goTo>");
                        writer.println("			<x>" + currentPos + "</x>");
                        writer.println("		</goTo>");
                    }
                }
            }
        }
    }

    private void writeBit(Bit3D bit) {

        if (!liftableBit(bit)) {
            return;
        }

        writer.println("		<bit>");
        writer.println("			<id>" + part.getScheduler().getBitIndex(bit) + "</id>");
        writer.println("			<cut>");
        Vector<Path2D> cutPaths = new Vector<>();
//        for(Vector<Path2D> paths : bit.getBaseBit().getCutPathsSeparate()){
//            cutPaths.addAll(paths);
//        }
//        for (Path2D p : cutPaths) {
//            writeCutPaths(p);
//        }
        if (bit.getRawCutPaths() != null) {
            for (Path2D p : bit.getRawCutPaths()) {
                writeCutPaths(p);
            }
        }
        writer.println("			</cut>");
        writeSubBits(bit);
        writer.println("		</bit>");
    }

    private void writeCutPaths(Path2D p) {

        Vector<double[]> points = new Vector<>();
        for (PathIterator pi = p.getPathIterator(null); !pi.isDone(); pi.next()) {
            double[] coords = new double[6];
            int type = pi.currentSegment(coords);
            double[] point = {type, coords[0], coords[1]};
            points.add(point);
        }

        boolean waitingForMoveTo = true;
        Vector<double[]> pointsToAdd = new Vector<>();
        for (double[] point : points) {
            if ((point[0] == PathIterator.SEG_LINETO) && waitingForMoveTo) {
                pointsToAdd.add(point);
            } else if (point[0] == PathIterator.SEG_LINETO) {
                writer.println("				<lineTo>");
                writer.println("					<x>" + point[1] + "</x>");
                writer.println("					<y>" + point[2] + "</y>");
                writer.println("				</lineTo>");
            } else {
                writer.println("				<moveTo>");
                writer.println("					<x>" + point[1] + "</x>");
                writer.println("					<y>" + point[2] + "</y>");
                writer.println("				</moveTo>");
                waitingForMoveTo = false;
            }
        }

        for (double[] point : pointsToAdd) {
            writer.println("					<lineTo>");
            writer.println("						<x>" + point[1] + "</x>");
            writer.println("						<y>" + point[2] + "</y>");
            writer.println("					</lineTo>");
        }
    }

    private void writeLayer(Layer layer) {
        AScheduler scheduler = part.getScheduler();
        if(scheduler.getFirstLayerBits().get(layer.getLayerNumber())!=null){
            Bit3D startBit = scheduler.getFirstLayerBits().get(layer.getLayerNumber());
            int startIndex = scheduler.getBitIndex(startBit);
            System.out.println(layer.getLayerNumber());
            int endIndex=((BasicScheduler)scheduler).filterBits(layer.sortBits()).size();
            System.out.println(startIndex+"-"+endIndex);
            System.out.println("size of sortedBit"+scheduler.getSortedBits().size());
            List<Pair<Bit3D, Vector2>> Bits3DKeys = scheduler.getSortedBits().subList(startIndex,startIndex+endIndex);
            System.out.println("size of Bits3Dkeys: "+Bits3DKeys.size());
            Vector3 modelTranslation = part.getModel().getPos();
            writer.println("	<layer>");
            writer.println("		<z>" + (layer.getLayerNumber() * (CraftConfig.bitThickness + CraftConfig.layersOffset)) + "</z>");
            for (int i = 0; i < Bits3DKeys.size(); i++) {
                Bit3D bit = Bits3DKeys.get(i).getKey();
// translating the bits - they are generated at the origin of the world coordinate system;
                for (int j = 0; j < bit.getRawLiftPoints().size(); j++) {
                    if (bit.getRawLiftPoints().get(j) != null) {
                        double oldX = bit.getLiftPoints().get(j).x;
                        double oldY = bit.getLiftPoints().get(j).y;
                        bit.getLiftPoints().set(j, new Vector2(oldX + modelTranslation.x, oldY + modelTranslation.y));
                    }
                }
                moveWorkingSpace(bit, i);
                writeBit(bit);
                remainingBits -= 1;
            }
            writer.println("	</layer>");
        }

    }

    private void writeSubBits(Bit3D bit) {
        for (int id = 0; id < bit.getRawLiftPoints().size(); id++) {
            if (bit.getRawLiftPoints().get(id) != null) {
                writer.println("			<subBit>");
                writer.println("			    <id>" + part.getScheduler().getBitIndex(bit) + "</id>");
                writer.println("			    <batch>" + part.getScheduler().getBitBatch(bit) + "</batch>");
                writer.println("			    <plate>" + part.getScheduler().getBitPlate(bit) + "</plate>");
                writer.println("				<liftPoint>");
                writer.println("					<x>" + bit.getRawLiftPoints().get(id).x + "</x>");
                writer.println("					<y>" + bit.getRawLiftPoints().get(id).y + "</y>");
                writer.println("				</liftPoint>");
                writer.println("				<rotation>" + bit.getOrientation().getEquivalentAngle() + "</rotation>");
                writer.println("				<position>");
                writer.println("					<x>" + bit.getLiftPoints().get(id).x + "</x>");
                writer.println("					<y>" + bit.getLiftPoints().get(id).y + "</y>");
                writer.println("				</position>");
                writer.println("			</subBit>");
            }
        }
    }

    public void writeXmlCode() {
        try {
            writer = new PrintWriter(mFilePath.toString(), "UTF-8");
            writer.println("<part>");
            startFile();
            Logger.updateStatus("Generating XML file");
            for (int i = 0; i < part.getLayers().size(); i++) {
                writeLayer(part.getLayers().get(i));
                Logger.setProgress(i, part.getLayers().size() - 1);
            }
            writer.println("</part>");
            Logger.message("The XML file has been generated and saved in " + mFilePath);
        } catch (Exception e) {
            Logger.error("The XML file has not been generated");
            e.printStackTrace();
        } finally {
            writer.close();
        }
    }

}
