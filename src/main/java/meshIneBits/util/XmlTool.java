/*
 * MeshIneBits is a Java software to disintegrate a 3d mesh (model in .stl)
 * into a network of standard parts (called "Bits").
 *
 * Copyright (C) 2016  Thibault Cassard & Nicolas Gouju.
 * Copyright (C) 2017-2018  TRAN Quoc Nhat Han.
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

import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.HashMap;
import java.util.Vector;

import javafx.util.Pair;
import meshIneBits.Bit3D;
import meshIneBits.GeneratedPart;
import meshIneBits.Layer;
import meshIneBits.config.CraftConfig;

import javax.annotation.processing.SupportedSourceVersion;

public class XmlTool {

	private GeneratedPart part;
	private PrintWriter writer;
	private Path filePath;
	private double effectiveWidth;
	private int nbBits;
	private int remainingBits;
	private double currentPos;

	public XmlTool(GeneratedPart part, Path fileLocation) {
		this.part = part;
		this.filePath = fileLocation;
		setFileToXml();
		getPrinterParameters();
	}

	public String getNameFromFileLocation() {
		return filePath.getFileName().toString().split("[.]")[0];
	}

	private boolean liftableBit(Bit3D bit) {
		int liftableSubBit = 0;
		for (Vector2 p : bit.getLiftPoints()) {
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

	private void getPrinterParameters(){
		float workingWidth = 0;
		float margin = 0;
		try {
			File filename = new File(this.getClass().getClassLoader().getResource("resources/PrinterConfig.txt").getPath());
			FileInputStream file = new FileInputStream(filename);
			BufferedReader br = new BufferedReader(new InputStreamReader(file));
			String strline;
			while ((strline = br.readLine()) != null){
				if (strline.startsWith("workingWidth")){
					workingWidth = Float.valueOf(strline.substring(14));
				}
				else if (strline.startsWith("margin")){
					margin = Float.valueOf(strline.substring(8));
				}
				else if (strline.startsWith("nbBits")){
					nbBits = Integer.valueOf(strline.substring(8));
				}
			}
			br.close();
			file.close();
		}
		catch(Exception e){
			System.out.println("Error :" + e.getMessage());
		}
		effectiveWidth = workingWidth - margin;
		remainingBits = nbBits;
	}


	private void setFileToXml() {
		String fileName = filePath.getFileName().toString();
		if (fileName.split("[.]").length >= 2) {
			fileName = fileName.split("[.]")[0];
		}
		fileName = fileName + "." + "xml";
		filePath = Paths.get(filePath.getParent().toString() + "\\" + fileName);
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

	private void moveWorkingSpace(Bit3D bit, int id){
		if (remainingBits == 0){
			writer.println("		<return>");
			writer.println("		</return>");
			remainingBits = nbBits;
		}
		for (int i = 0; i < bit.getDepositPoints().size(); i++) {
			if (bit.getDepositPoints().get(i) != null) {
				if (id == 0) {
					writer.println("		<goTo>");
					currentPos = bit.getDepositPoints().get(i).x + effectiveWidth / 2;
					writer.println("			<x>" + currentPos + "</x>");
					writer.println("		</goTo>");
				} else {
					if (Math.abs(bit.getDepositPoints().get(i).x - currentPos) > effectiveWidth / 2) {
						currentPos += effectiveWidth;
						writer.println("		<goTo>");
						writer.println("			<x>" + currentPos + "</x>");
						writer.println("		</goTo>");
					}
				}
			}
		}
	}

	private void writeBit(Bit3D bit, int id) {

		if (!liftableBit(bit)) {
			return;
		}

		writer.println("		<bit>");
		writer.println("			<id>" + id + "</id>");
		writer.println("			<cut>");
		if (bit.getCutPaths() != null) {
			for (Path2D p : bit.getCutPaths()) {
				writeCutPaths(p);
			}
		}
		writer.println("			</cut>");
		writeSubBits(bit);
		writer.println("		</bit>");
	}

	private void writeCutPaths(Path2D p) {

		Vector<double[]> points = new Vector<double[]>();
		for (PathIterator pi = p.getPathIterator(null); !pi.isDone(); pi.next()) {
			double[] coords = new double[6];
			int type = pi.currentSegment(coords);
			double[] point = { type, coords[0], coords[1] };
			points.add(point);
		}

		boolean waitingForMoveTo = true;
		Vector<double[]> pointsToAdd = new Vector<double[]>();
		for (double[] point : points) {
			if ((point[0] == PathIterator.SEG_LINETO) && waitingForMoveTo) {
				pointsToAdd.add(point);
			} else if ((point[0] == PathIterator.SEG_LINETO) && !waitingForMoveTo) {
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
		Vector<Pair<Bit3D,Vector2>> Bits3DKeys = layer.sortBits();
		writer.println("	<layer>");
		writer.println("		<z>" + (layer.getLayerNumber() * (CraftConfig.bitThickness + CraftConfig.layersOffset)) + "</z>");
		for (int i = 0; i < Bits3DKeys.size(); i++) {
			Bit3D bit = Bits3DKeys.get(i).getKey();
			moveWorkingSpace(bit,i);
			writeBit(bit, i);
			remainingBits -= 1;
		}
		writer.println("	</layer>");
	}

	private void writeSubBits(Bit3D bit) {
		for (int id = 0; id < bit.getLiftPoints().size(); id++) {
			if (bit.getLiftPoints().get(id) != null) {
				writer.println("			<subBit>");
				writer.println("				<id>" + id + "</id>");
				writer.println("				<liftPoint>");
				writer.println("					<x>" + bit.getLiftPoints().get(id).x + "</x>");
				writer.println("					<y>" + bit.getLiftPoints().get(id).y + "</y>");
				writer.println("				</liftPoint>");
				writer.println("				<rotation>" + bit.getOrientation().getEquivalentAngle() + "</rotation>");
				writer.println("				<position>");
				writer.println("					<x>" + bit.getDepositPoints().get(id).x + "</x>");
				writer.println("					<y>" + bit.getDepositPoints().get(id).y + "</y>");
				writer.println("				</position>");
				writer.println("			</subBit>");
			}
		}
	}

	public void writeXmlCode() {
		try {
			writer = new PrintWriter(filePath.toString(), "UTF-8");
			writer.println("<part>");
			startFile();
			Logger.updateStatus("Generating XML file");
			for (int i = 0; i < part.getLayers().size(); i++) {
				writeLayer(part.getLayers().get(i));
				Logger.setProgress(i, part.getLayers().size() - 1);
			}
			writer.println("</part>");
			Logger.message("The XML file has been generated and saved in " + filePath);
		} catch (Exception e) {
			Logger.error("The XML file has not been generated");
			e.printStackTrace();
		} finally {
			writer.close();
		}
	}

}
