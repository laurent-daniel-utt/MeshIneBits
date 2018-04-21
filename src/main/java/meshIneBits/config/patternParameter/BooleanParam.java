/**
 * 
 */
package meshIneBits.config.patternParameter;

/**
 * @author Quoc Nhat Han TRAN
 *
 */
public class BooleanParam implements PatternParameter {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2259784876889236537L;
	private boolean currentValue;
	private boolean defaultValue;
	private final String TITLE;
	private final String CODENAME;
	private final String DESCRIPTION;

	/**
	 * @param title
	 * @param codename
	 * @param description
	 * @param defaultValue
	 * @param currentValue
	 */
	public BooleanParam(String title, String codename, String description, boolean defaultValue, boolean currentValue) {
		TITLE = title;
		CODENAME = codename;
		DESCRIPTION = description;
		this.defaultValue = defaultValue;
		this.currentValue = currentValue;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see meshIneBits.config.patternParameter.PatternParameter#getCurrentValue()
	 */
	@Override
	public Boolean getCurrentValue() {
		return currentValue;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * meshIneBits.config.patternParameter.PatternParameter#setCurrentValue(java.
	 * lang.Object)
	 */
	@Override
	public void setCurrentValue(Object newValue) {
		if (newValue instanceof Boolean)
			this.currentValue = (boolean) newValue;
		else
			this.currentValue = defaultValue;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see meshIneBits.config.patternParameter.PatternParameter#getCodename()
	 */
	@Override
	public String getCodename() {
		return CODENAME;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see meshIneBits.config.patternParameter.PatternParameter#getTitle()
	 */
	@Override
	public String getTitle() {
		return TITLE;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see meshIneBits.config.patternParameter.PatternParameter#getDescription()
	 */
	@Override
	public String getDescription() {
		return DESCRIPTION;
	}

}
