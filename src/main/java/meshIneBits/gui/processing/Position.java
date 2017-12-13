package meshIneBits.gui.processing;

/**
 * 
 * @author Nicolas
 *
 */
public class Position {
	
	private float[] translation;
	private float rotation;
	
	/**
	 * 
	 * @param translation
	 * @param rotation
	 */
	public Position(float[] translation, float rotation){
		this.translation = translation;
		this.rotation = rotation;
	}
	
	/**
	 * 
	 * @return
	 */
	public float[] getTranslation(){
		return translation;
	}
	
	/**
	 * 
	 * @return
	 */
	public float getRotation(){
		return rotation;
	}

}
