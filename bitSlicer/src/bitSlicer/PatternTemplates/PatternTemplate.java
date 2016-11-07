package bitSlicer.PatternTemplates;

import java.util.Vector;

import bitSlicer.Bit2D;
import bitSlicer.util.Vector2;

abstract class PatternTemplate {
	
	protected double bitWidth, bitLength;
	protected Vector2 rotation, offSet, patternStart, patternEnd;
	
	public PatternTemplate(double bitWidth, double bitLength, Vector2 rotation, Vector2 offSet, double skirtRadius){
		this.bitWidth = bitWidth;
		this.bitLength = bitLength;
		this.rotation = rotation;
		this.offSet = offSet;
		setBoundaries(skirtRadius);
	}

	public void setBoundaries(double skirtRadius){
		//Set patternStart et patternEnd en fonction de la rotation, de l'offset et du skirtRadius
		//SkirtRadius est le rayon du cylindre qui contient entierement la pièce
		//patternEnd et patternStart sont 2 points qui forment le rectangle que va recouvrir la méthode createPattern
	}
	
	abstract Vector<Bit2D> createPattern(double layerNumber);
}

