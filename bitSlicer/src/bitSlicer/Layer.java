package bitSlicer;

import bitSlicer.util.Shape2D;
import bitSlicer.util.Vector2;
import bitSlicer.Slicer.Slice;
import bitSlicer.Slicer.Config.CraftConfig;

import java.awt.geom.AffineTransform;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import bitSlicer.Pattern;

public class Layer extends Shape2D {
	
	@SuppressWarnings("unused")
	private int layerNumber;
	private Vector<Slice> slices;
	private Pattern modelPattern;
	private Vector<Pattern> patterns = new Vector<Pattern>();
	private Hashtable<Vector2, Bit3D> mapBits3D;
	private int sliceToSelect = 1; //Let the user choose that in the gui for each layer!
	
	public Layer(Vector<Slice> slices, int layerNumber, GeneratedPart generatedPart){
		this.slices = slices;
		this.layerNumber = layerNumber;
		this.modelPattern = generatedPart.getPatternTemplate().createPattern(layerNumber);
		
		for (Slice s : slices){
			patterns.add(modelPattern.clone());
			patterns.lastElement().computeBits(s);
		}
		
		generateBits3D();
		computeLiftPoints();
	}

	public Vector<Slice> getSlices() {
		return this.slices;
	}
	
	public Vector<Pattern> getPatterns(){
		return this.patterns;
	}
	
	public void computeBitsPattern(int sliceNumber){
		patterns.get(sliceNumber).computeBits(slices.get(sliceNumber));
	}
	
	public Vector<Vector2> getBits3dKeys(){
		return new Vector<Vector2>(mapBits3D.keySet());
	}
	
	public Bit3D getBit3D(Vector2 key){
		return mapBits3D.get(key);
	}
	
	public void generateBits3D(){
		mapBits3D = new Hashtable<Vector2, Bit3D>();
		Vector<Vector<Vector2>> keysPattern = new Vector<Vector<Vector2>>();
		for(int i = 0; i < patterns.size(); i++)
			keysPattern.add(patterns.get(i).getBitsKeys());
		for(int i = 0; i < keysPattern.size(); i++){ //dans chaque Vector<Keys>
			for(int j = 0; j < keysPattern.get(i).size(); j++){ //Pour chaque key
				Vector<Bit2D> bitsToInclude = new Vector<Bit2D>();
				bitsToInclude.add(patterns.get(i).getBit(keysPattern.get(i).get(j)));
				for(int k = i + 1; k < keysPattern.size() - 1; k++){ //pour chaque Vector<Keys> d'index superieur
					Iterator<Vector2> itr = keysPattern.get(k).iterator(); //On passe en revue chaque key
					while(itr.hasNext()){
						Vector2 myKey = itr.next();
						if(myKey.asGoodAsEqual(keysPattern.get(i).get(j))){ //Si c'est la même alors on inclut ce bit2D et on le supprimme de la liste
							bitsToInclude.addElement(patterns.get(k).getBit(myKey));
							itr.remove();
						}
					}
				}
				Bit3D newBit;
				try{
					newBit = new Bit3D(bitsToInclude, keysPattern.get(i).get(j), getNewOrientation(bitsToInclude.get(0)), sliceToSelect);
					mapBits3D.put(keysPattern.get(i).get(j), newBit);
				}
				catch(Exception e){
					//new Bit3D() will throw an exception if there is not enough slices allowed to it or if the slice to select doesn't exist in that bit
					if(e.getMessage() != "This bit is too thin" && e.getMessage() != "The slice to select does not exist in that bit")
						e.printStackTrace();
				}
			}
		}
	}
	
	public Vector2 getNewOrientation(Bit2D bit){
		AffineTransform patternAffTrans = (AffineTransform) modelPattern.getAffineTransform().clone();
		patternAffTrans.translate(-CraftConfig.xOffset, -CraftConfig.yOffset);
		return bit.getOrientation().getTransformed(patternAffTrans);
	}
	
	public void removeBit(Vector2 key){
		modelPattern.removeBit(key);
		generateBits3D();
	}
	
	public void addBit(Bit2D bit){
		modelPattern.addBit(bit);
		generateBits3D();
	}
	
	public void computeLiftPoints(){
		for(Vector2 key : getBits3dKeys())
			mapBits3D.get(key).computeLiftPoints();
	}
	
	public int getLayerNumber(){
		return layerNumber;
	}
	
}
