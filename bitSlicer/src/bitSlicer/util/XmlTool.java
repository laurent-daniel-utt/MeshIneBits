package bitSlicer.util;

import java.awt.geom.Path2D;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

import bitSlicer.Bit3D;
import bitSlicer.GeneratedPart;
import bitSlicer.Layer;
import bitSlicer.Slicer.Config.CraftConfig;

public class XmlTool {
	
	GeneratedPart part;
	PrintWriter writer;
	String fileLocation;
	StringBuffer xmlCode;
	
	public XmlTool(GeneratedPart part, String fileLocation){
		this.part = part;
		this.fileLocation = fileLocation;
	}
	
	public String getNameFromFileLocation(){
		return fileLocation;
	}
	
	public void writeXmlCode() throws IOException{
		writer = new PrintWriter(fileLocation, "UTF-8");
		
		writer.println("<part>");
		startFile();
		for(Layer l : part.getLayers())
			writeLayer(l);
		writer.println("</part>");
		
		writer.close();
	}
	
	private void startFile(){
		writer.println("<name>" + getNameFromFileLocation() + "</name");
		writer.println("<date>" + (new Date()).toString() + "</date>");
		writer.println("<bitDimension>");
		writer.println("<height>" + CraftConfig.bitThickness + "</height>");
		writer.println("<width>" + CraftConfig.bitWidth + "</width>");
		writer.println("<length>" + CraftConfig.bitLength + "</length>");
		writer.println("</bitDimension>");
		writer.println("<partSkirt>");
		writer.println("<height>" + ((part.getLayers().size() + CraftConfig.layersOffset) * CraftConfig.bitThickness - CraftConfig.layersOffset) + "</height>");
		writer.println("<radius>" + part.getSkirtRadius() + "</radius>");
		writer.println("<partSkirt>");
	}
	
	private void writeLayer(Layer layer){
		writer.println("<layer>");
		writer.println("<z>" + (layer.getLayerNumber() * (CraftConfig.bitThickness + CraftConfig.layersOffset)) + "<z>");
		for(int i = 0; i < layer.getBits3dKeys().size(); i++)
			writeBit(layer.getBit3D(layer.getBits3dKeys().get(i)), i);
		writer.println("</layer>");
	}
	
	private void writeBit(Bit3D bit, int id){
		writer.println("<bit>");
		writer.println("<id>" + id + "</id>");
		writer.println("<cut>");
		for(Path2D p : bit.getCutPaths())
			writeCutPaths(p);
		writer.println("</cut>");
		
		writer.println("</bit>");
	}
	
	private void writeCutPaths(Path2D p){
		
		
	}
	
	
}
