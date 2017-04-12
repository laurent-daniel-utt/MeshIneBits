/**
 * 
 */
package meshIneBits.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * An utility to rounds a double to specified number of decimal places.
 * @see <a href="http://stackoverflow.com/questions/2808535/round-a-double-to-2-decimal-places">This link</a>.
 */
public class Rounder {
	public static double round(double value, int places) {
	    if (places < 0) throw new IllegalArgumentException();

	    BigDecimal bd = new BigDecimal(value);
	    bd = bd.setScale(places, RoundingMode.HALF_UP);
	    return bd.doubleValue();
	}
}
