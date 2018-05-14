package patternTemplate;

import static org.junit.jupiter.api.Assertions.fail;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import meshIneBits.GeneratedPart;
import meshIneBits.Layer;
import meshIneBits.Model;
import meshIneBits.patterntemplates.PatternTemplate;

/**
 * Integrity test. Subclass to test each pattern
 * 
 * @author Quoc Nhat Han TRAN
 *
 */
public abstract class PatternIntegrityTest {
	/**
	 * Initialize this logger when subclass
	 */
	static Logger LOGGER;

	/**
	 * Change type to a subclass of {@link PatternTemplate} when subclass
	 */
	PatternTemplate pattern;

	Model model;

	GeneratedPart part;

	/**
	 * Subclass can change this depending on size of test sample and algorithm
	 */
	static int TIME_LIMIT = 60;

	/**
	 * Init {@link #pattern} and set up its properties via
	 * {@link PatternTemplate#getPatternConfig()} or its open APIs
	 */
	@BeforeEach
	abstract protected void setUp();

	@ParameterizedTest
	@ValueSource(strings = { "Sphere.stl", "CreuxBoite.stl", "Cylindre.stl", "Tour.stl", "Blob.stl" })
	protected void testScenario(String modelFilename) {
		String clname = this.getClass().getSimpleName();
		LOGGER.info("Integrity test of " + clname + " in scenario " + modelFilename + " starts");
		setUpPart(modelFilename);
		slicePart();
		generateLayers();
		optimizeLayer();
		LOGGER.info("Integrity test of " + clname + " in scenario " + modelFilename + " finishes");
	}

	/**
	 * Slice a given model
	 */
	private void slicePart() {
		LOGGER.info("Slicer starts");
		part = new GeneratedPart(model);
		waitSlicerDone();
		checkSlicedPart();
		LOGGER.info("Slicer finishes");
	}

	/**
	 * Subclass should check if the part conforms
	 */
	abstract protected void checkSlicedPart();

	/**
	 * Load up the model and slice it
	 * 
	 * @param modelFilename
	 */
	private void setUpPart(String modelFilename) {
		try {
			LOGGER.info("Load Sphere.stl");
			model = new Model(this.getClass().getResource("/stlModel/" + modelFilename).getPath());
			model.center();
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
	 * Test the layers generator
	 */
	private void generateLayers() {
		LOGGER.info("Generator starts");
		part.buildBits2D();
		waitGeneratorDone();
		checkGeneratedPart();
		LOGGER.info("Generator finishes");
	}

	/**
	 * Subclass to check the state of generated part
	 */
	abstract protected void checkGeneratedPart();

	/**
	 * Wait the thread of generating layers
	 */
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
	private void optimizeLayer() {
		LOGGER.info("Optimizer starts");

		LOGGER.warning("We only try optimize one random layer");
		Layer layerToTest = getRandomLayer();
		int res = pattern.optimize(layerToTest);
		if (res < 0)
			LOGGER.warning("Optimization failed on layer " + layerToTest.getLayerNumber());
		else if (res > 0)
			LOGGER.warning(
					res + " irregularities on layer " + layerToTest.getLayerNumber() + " have not been resolved");
		else
			LOGGER.info("Optimization succeeded on layer " + layerToTest.getLayerNumber());

		LOGGER.info("Optimizer finishes");
	}

	/**
	 * @return a random layer
	 */
	private Layer getRandomLayer() {
		List<Layer> layers = part.getLayers();
		int i = (int) Math.round(Math.random() * layers.size());
		return layers.get(i);
	}
}