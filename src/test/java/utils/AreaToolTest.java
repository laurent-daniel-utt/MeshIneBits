package utils;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import meshIneBits.GeneratedPart;
import meshIneBits.Model;
import meshIneBits.slicer.Slice;
import meshIneBits.util.AreaTool;

class AreaToolTest {

	/**
	 * Initialize this logger when subclass
	 */
	protected static Logger logger = meshIneBits.util.Logger.createSimpleInstanceFor(AreaToolTest.class);

	protected Model model;

	protected GeneratedPart part;

	static int TIME_LIMIT = 60;

	/**
	 * Load up the model and slice it
	 * 
	 * @param modelFilename
	 */
	private void setUpPart(String modelFilename) {
		try {
			logger.info("Load " + modelFilename);
			model = new Model(this.getClass().getResource("/stlModel/" + modelFilename).getPath());
			model.center();
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Cannot properly load up model and slice." + e.getMessage(), e);
			fail("Cannot properly load up model and slice", e);
		}
	}

	/**
	 * Slice a given model
	 */
	private void slicePart() {
		logger.info("Slicer starts");
		part = new GeneratedPart(model);
		waitSlicerDone();
		logger.info("Slicer finishes");
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
				logger.log(Level.SEVERE, e.getMessage(), e);
			}
		}
		if (timeElapsed >= TIME_LIMIT && !part.isSliced())
			fail("Cannot wait till the end of slicing");
	}

	@ParameterizedTest(name = "{index} ==> model={0}")
	@MethodSource("areaSampleProvider")
	void testGetAreaFrom(String modelFilename, int sliceNum, List<Point2D> insidePoints, List<Point2D> outsidePoints) {
		setUpPart(modelFilename);
		slicePart();
		Slice s = part.getSlices().get(sliceNum);
		Area a = AreaTool.getAreaFrom(s);
		logger.info("Area to test: boundary=" + a.getBounds2D());
		// Check inner
		for (Point2D p : insidePoints) {
			assertTrue(a.contains(p), p + " should be inside");
		}
		// Check outer
		for (Point2D p : outsidePoints) {
			assertFalse(a.contains(p), p + " should be outside");
		}
	}

	/**
	 * Read {@link areaSample.json} and parse
	 * 
	 * @return
	 */
	Stream<Arguments> areaSampleProvider() {
		return null;
	}
}