package meshIneBits.util;

import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;

import meshIneBits.Bit3D;
import meshIneBits.GeneratedPart;
import meshIneBits.Layer;
import meshIneBits.Config.CraftConfig;

public class BrickRobotXmlTool {
	
	GeneratedPart part;
	PrintWriter writer;
	Path filePath;
	StringBuffer xmlCode;
	static final double SAFETY_MARGIN = 20; //offset to avoid hitting the other bits of the layer

	public BrickRobotXmlTool(GeneratedPart part, Path fileLocation) {
		this.part = part;
		this.filePath = fileLocation;
		setFileToXml();
	}
	
	private void setFileToXml(){
		String fileName = filePath.getFileName().toString();
		if(fileName.split("[.]").length >= 2)
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
		double zPlateau = layer.getLayerNumber() * CraftConfig.bitThickness - (CraftConfig.bitThickness / 2) ;
		writer.println("	<PLATEAU z=\"" + zPlateau + "\"/>");
		for (int i = 0; i < layer.getBits3dKeys().size(); i++) {
			writeBit(layer.getBit3D(layer.getBits3dKeys().get(i)), zPlateau);
		}
	}
	
	private void writeBit(Bit3D bit, double zPlateau) {
		writer.println("	<PIECE p=\"0.0\"/>");
		writer.println("		<PATH config_data=\"N, ,0,0\" p=\"0.0\" r=\"" + bit.getOrientation().getEquivalentAngle() + "\" w=\"0.0\""
				+ " x=\"" + bit.getOrigin().x + "\""
				+ " y=\"" + bit.getOrigin().y + "\""
				+ " z=\"" + (zPlateau + CraftConfig.bitThickness + SAFETY_MARGIN) + "\"/>");
		writer.println("		<POSI config_data=\"N, ,0,0\" p=\"0.0\" r=\"" + bit.getOrientation().getEquivalentAngle() + "\" w=\"0.0\""
				+ " x=\"" + bit.getOrigin().x + "\""
				+ " y=\"" + bit.getOrigin().y + "\""
				+ " z=\"" + zPlateau + "\"/>");
		writer.println("		<PATH config_data=\"N, ,0,0\" p=\"0.0\" r=\"" + bit.getOrientation().getEquivalentAngle() + "\" w=\"0.0\""
				+ " x=\"" + bit.getOrigin().x + "\""
				+ " y=\"" + bit.getOrigin().y + "\""
				+ " z=\"" + (zPlateau + CraftConfig.bitThickness + SAFETY_MARGIN) + "\"/>");
	}
}
