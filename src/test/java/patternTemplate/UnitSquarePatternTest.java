/**
 * 
 */
package patternTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import meshIneBits.GeneratedPart;
import meshIneBits.Layer;
import meshIneBits.Model;
import meshIneBits.config.CraftConfig;
import meshIneBits.config.PatternConfig;
import meshIneBits.patterntemplates.UnitSquarePattern;

/**
 * @author Quoc Nhat Han TRAN
 *
 */
class UnitSquarePatternTest {

	private final static Logger LOGGER = meshIneBits.util.Logger.createSimpleInstanceFor(UnitSquarePatternTest.class);

	UnitSquarePattern patternTemplate;
	GeneratedPart part;
	/**
	 * Limit of execution time in seconds
	 */
	final static int TIME_LIMIT = 60;

	@BeforeEach
	void setUp() {
		patternTemplate = new UnitSquarePattern();
		CraftConfig.templateChoice = patternTemplate;
	}

	/**
	 * A complete scenario
	 */
	@Test
	@Tag("slow")
	void testSphereScenario() {
		LOGGER.info("Scenario Sphere");
		setUpPart("Sphere.stl");
		// The slicer runs on a different thread
		// We need to wait until it settles down
		waitSlicerDone();
		// Once the part is sliced
		// We generate layers
		// And test each layer
		testGenerateLayers();
		// Once ensured the layers
		// Run the auto-optimization
		testOptimizeLayers();
		LOGGER.info("Scenario Sphere completed");
	}

	/**
	 * Load up the model and slice it
	 * 
	 * @param modelFilename
	 * @throws Exception
	 */
	private void setUpPart(String modelFilename) {
		try {
			LOGGER.info("Load Sphere.stl");
			Model m = new Model(this.getClass().getResource("/stlModel/" + modelFilename).getPath());
			m.center();
			part = new GeneratedPart(m);
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Cannot properly load up model and slice." + e.getMessage(), e);
			fail("Cannot properly load up model and slice", e);
		}
	}

	/**
	 * Wait till slicing completes
	 * 
	 * @see #TIME_LIMIT
	 */
	private void waitSlicerDone() {
		int timeElapsed = 0;
		while (timeElapsed < TIME_LIMIT) {
			if (part.isSliced())
				break;
			// If part has not been sliced yet
			// Sleep a little
			timeElapsed++;
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				LOGGER.log(Level.SEVERE, e.getMessage(), e);
			}
		}
		if (timeElapsed >= TIME_LIMIT && !part.isSliced())
			fail("Cannot wait till the end of slicing");
	}

	/**
	 * Runs the layers generator
	 */
	private void testGenerateLayers() {
		LOGGER.info("GenerateLayers test starts");

		part.buildBits2D();
		// The generator runs on a different thread
		// We need to wait until its end
		waitGeneratorDone();
		// Part has been generated
		// We check each layer
		for (Layer layer : part.getLayers()) {
			// Assure each layer is empty
			assertEquals(0, layer.getSelectedPattern().getBitsKeys().size(),
					"Layer " + layer.getLayerNumber() + " should be empty");
		}

		LOGGER.info("GenerateLayers test terminated");
	}

	private void waitGeneratorDone() {
		int timeElapsed = 0;
		while (timeElapsed < TIME_LIMIT) {
			if (part.isGenerated())
				break;
			// If generator still not done
			timeElapsed++;
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				LOGGER.log(Level.SEVERE, e.getMessage(), e);
			}
		}
		if (timeElapsed >= TIME_LIMIT && !part.isGenerated())
			fail("Cannot wait until part is completely generated");
	}

	/**
	 * Test the optimization
	 */
	private void testOptimizeLayers() {
		LOGGER.info("Optimizer test starts");

		// For instance, we only test the first layer
		LOGGER.warning("We only try optimize one single layer");
		Layer layerToTest = part.getLayers().get(0);
		int res = patternTemplate.optimize(layerToTest);
		if (res < 0)
			LOGGER.warning("Optimization failed on layer " + layerToTest.getLayerNumber());
		else if (res > 0)
			LOGGER.warning(res + " irregularities on layer " + layerToTest.getLayerNumber() + " have not been resolved");
		else
			LOGGER.info("Optimization succeeded on layer " + layerToTest.getLayerNumber());

		LOGGER.info("Optimizer test terminated");
	}

	/**
	 * Test method for
	 * {@link meshIneBits.patterntemplates.UnitSquarePattern#initiateConfig()}.
	 */
	@Test
	void testInitiateConfig() {
		LOGGER.info("InitiateConfig test starts");

		PatternConfig config = patternTemplate.getPatternConfig();
		// Verify default value
		LOGGER.info("Assert current value of horizontal margin");
		assertEquals((Double) config.get("horizontalMargin").getCurrentValue(), (Double) 2.0,
				"Default horizontal margin should be 2.0 mm");
		LOGGER.info("Assert current value of vertical margin");
		assertEquals((Double) config.get("verticalMargin").getCurrentValue(), (Double) 2.0,
				"Default vertical margin should be 2.0 mm");

		LOGGER.info("InitiatiteConfig test terminated");
	}

	/**
	 * Test method for
	 * {@link meshIneBits.patterntemplates.UnitSquarePattern#moveBit(meshIneBits.Pattern, meshIneBits.util.Vector2, meshIneBits.util.Vector2)}.
	 */
	@Test
	void testMoveBitByDefault() {
		LOGGER.info("MoveBit (by default) test starts");

		LOGGER.info("Assert MoveBit return null (bit movement is prohibited)");
		assertNull(patternTemplate.moveBit(null, null, null), "This template should not allow bit displacement");

		LOGGER.info("MoveBit (by default) test terminated");
	}

	/**
	 * Test method for
	 * {@link meshIneBits.patterntemplates.UnitSquarePattern#moveBit(meshIneBits.Pattern, meshIneBits.util.Vector2, meshIneBits.util.Vector2, double)}.
	 */
	@Test
	void testMoveBitExtended() {
		LOGGER.info("MoveBit (extended) test starts");

		LOGGER.info("Assert MoveBit return null (bit movement is prohibited)");
		assertNull(patternTemplate.moveBit(null, null, null, CraftConfig.bitLength),
				"This template should not allow bit displacement");

		LOGGER.info("MoveBit (extended) test terminated");
	}
}
