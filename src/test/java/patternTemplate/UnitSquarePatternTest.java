/**
 * 
 */
package patternTemplate;

import static org.junit.jupiter.api.Assertions.*;

import java.util.logging.Logger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import meshIneBits.Pattern;
import meshIneBits.config.PatternConfig;
import meshIneBits.patterntemplates.UnitSquarePattern;

/**
 * @author Quoc Nhat Han TRAN
 *
 */
class UnitSquarePatternTest {

	private static final Logger logger = Logger.getLogger(UnitSquarePatternTest.class.getName());

	UnitSquarePattern pattern;

	@BeforeEach
	void setUp() {
		pattern = new UnitSquarePattern();
	}

	/**
	 * Test method for
	 * {@link meshIneBits.patterntemplates.UnitSquarePattern#initiateConfig()}.
	 */
	@Test
	void testInitiateConfig() {
		logger.info("InitiateConfig test starts");

		pattern.initiateConfig();
		PatternConfig config = pattern.getPatternConfig();
		// Verify default value
		logger.info("Assert current value of margins");
		logger.info("Assert current value of horizontal margin");
		assertEquals((Double) config.get("horizontalMargin").getCurrentValue(), (Double) 2.0,
				"Default horizontal margin should be 2.0 mm");
		logger.info("Assert current value of vertical margin");
		assertEquals((Double) config.get("verticalMargin").getCurrentValue(), (Double) 2.0,
				"Default vertical margin should be 2.0 mm");

		logger.info("InitiatiteConfig test terminated");
	}

	/**
	 * Test method for
	 * {@link meshIneBits.patterntemplates.UnitSquarePattern#ready(meshIneBits.GeneratedPart)}.
	 */
	@Test
	void testReady() {
		logger.info("Ready test starts");

		logger.info("Assert ready() do nothing");
		assertFalse(pattern.ready(null), "Ready() has done something true");

		logger.info("Ready test terminated");
	}

	/**
	 * Test method for
	 * {@link meshIneBits.patterntemplates.UnitSquarePattern#createPattern(int)}.
	 */
	@Test
	void testCreatePattern() {
		logger.info("CreatePattern test starts");

		logger.info("Assert CreatePattern return empty set");
		Pattern createdPattern = pattern.createPattern(0);
		assertEquals(createdPattern.getBitsKeys().size(), 0, "Created pattern should be an empty set");

		logger.info("CreatePattern test terminated");
	}

	/**
	 * Test method for
	 * {@link meshIneBits.patterntemplates.UnitSquarePattern#optimize(meshIneBits.Layer)}.
	 */
	@Test
	void testOptimize() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for
	 * {@link meshIneBits.patterntemplates.UnitSquarePattern#moveBit(meshIneBits.Pattern, meshIneBits.util.Vector2, meshIneBits.util.Vector2)}.
	 */
	@Test
	void testMoveBitByDefault() {
		logger.info("MoveBit (by default) test starts");

		logger.info("Assert MoveBit return null (bit movement is prohibited)");
		fail("Not yet implementd");

		logger.info("MoveBit (by default) test terminated");
	}

	/**
	 * Test method for
	 * {@link meshIneBits.patterntemplates.UnitSquarePattern#moveBit(meshIneBits.Pattern, meshIneBits.util.Vector2, meshIneBits.util.Vector2, double)}.
	 */
	@Test
	void testMoveBitExtended() {
		fail("Not yet implemented");
	}
}
