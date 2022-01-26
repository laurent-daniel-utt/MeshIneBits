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

package meshIneBits.util;

import meshIneBits.Bit3D;
import meshIneBits.Layer;
import meshIneBits.Mesh;
import meshIneBits.config.CraftConfig;

import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * A try to generate an XML file compatible with the BrickRobot machine, but it doesn't work because we can't handle complex path to position the bits
 */
class BrickRobotXmlTool {

    private Mesh part;
    private PrintWriter writer;
    private Path filePath;
    StringBuffer xmlCode;
    private static final double SAFETY_MARGIN = 20; //offset to avoid hitting the other bits of the layer

    public BrickRobotXmlTool(Mesh part, Path fileLocation) {
        this.part = part;
        this.filePath = fileLocation;
        setFileToXml();
    }

    private void setFileToXml() {
        String fileName = filePath.getFileName().toString();
        if (fileName.split("[.]").length >= 2)
            fileName = fileName.split("[.]")[0];
        fileName = fileName + "." + "xml";
        filePath = Paths.get(filePath.getParent().toString() + "\\" + fileName);
    }

    public void writeXmlCode() {
        try {
            writer = new PrintWriter(filePath.toString(), "UTF-8");
            writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            writer.println("<ASM_ORI>");
            writer.println("	<SPEED s=\"4000.0\"/>");
            Logger.updateStatus("Generating XML file");
            for (int i = 0; i < part.getLayers().size(); i++) {
                writeLayer(part.getLayers().get(i));
                Logger.setProgress(i, part.getLayers().size() - 1);
            }
            writer.println("</ASM_ORI>");
            Logger.message("The XML file has been generated and saved in " + filePath);
        } catch (Exception e) {
            Logger.error("The XML file has not been generated");
            e.printStackTrace();
        } finally {
            writer.close();
        }
    }

    private void writeLayer(Layer layer) {
        double zPlateau = layer.getLayerNumber() * CraftConfig.bitThickness - (CraftConfig.bitThickness / 2);
        writer.println("	<PLATEAU z=\"" + zPlateau + "\"/>");
        for (int i = 0; i < layer.getBits3dKeys().size(); i++) {
            writeBit(layer.getBit3D(layer.getBits3dKeys().get(i)), zPlateau);
        }
    }

    private void writeBit(Bit3D bit, double zPlateau) {
        writer.println("	<PIECE p=\"0.0\"/>");
        writer.println("		<PATH config_data=\"N, ,0,0\" p=\"0.0\" r=\"" + (bit.getOrientation().getEquivalentAngle() + 90) + "\" w=\"0.0\""
                + " x=\"" + bit.getOrigin().x + "\""
                + " y=\"" + bit.getOrigin().y + "\""
                + " z=\"" + (zPlateau + CraftConfig.bitThickness + SAFETY_MARGIN) + "\"/>");
        writer.println("		<POSI config_data=\"N, ,0,0\" p=\"0.0\" r=\"" + (bit.getOrientation().getEquivalentAngle() + 90) + "\" w=\"0.0\""
                + " x=\"" + bit.getOrigin().x + "\""
                + " y=\"" + bit.getOrigin().y + "\""
                + " z=\"" + zPlateau + "\"/>");
        writer.println("		<PATH config_data=\"N, ,0,0\" p=\"0.0\" r=\"" + (bit.getOrientation().getEquivalentAngle() + 90) + "\" w=\"0.0\""
                + " x=\"" + bit.getOrigin().x + "\""
                + " y=\"" + bit.getOrigin().y + "\""
                + " z=\"" + (zPlateau + CraftConfig.bitThickness + SAFETY_MARGIN) + "\"/>");
    }
}
