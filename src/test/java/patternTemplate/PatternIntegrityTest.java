/*
 * MeshIneBits is a Java software to disintegrate a 3d mesh (model in .stl)
 * into a network of standard parts (called "Bits").
 *
 * Copyright (C) 2016  Thibault Cassard & Nicolas Gouju.
 * Copyright (C) 2017-2018  TRAN Quoc Nhat Han.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package patternTemplate;

import meshIneBits.GeneratedPart;
import meshIneBits.Layer;
import meshIneBits.Model;
import meshIneBits.patterntemplates.PatternTemplate;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.fail;

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
	static Logger logger;

	PatternTemplate pattern;

	private Model model;

	GeneratedPart part;

	/**
	 * Subclass can change this depending on size of test sample and algorithm
	 */
	private static int TIME_LIMIT = 60;

	@ParameterizedTest
	@ValueSource(strings = { "Sphere.stl", "HoledBox.stl", "Tour.stl", "Blob.stl" })
	void testScenario(String modelFilename) {
		String clname = this.getClass().getSimpleName();
		logger.info("Integrity test of " + clname + " in scenario " + modelFilename + " starts");
		setUpPart(modelFilename);
		slicePart();
		generateLayers();
		optimizeLayer();
		logger.info("Integrity test of " + clname + " in scenario " + modelFilename + " finishes");
	}

	/**
	 * Slice a given model
	 */
	private void slicePart() {
		logger.info("Slicer starts");
		part = new GeneratedPart(model);
		waitSlicerDone();
		checkSlicedPart();
		logger.info("Slicer finishes");
	}

	/**
	 * Subclass should check if the part conforms
	 */
	abstract protected void checkSlicedPart();

	/**
	 * Load up the model and slice it
	 * 
	 * @param modelFilename in test resource
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

	/**
	 * Test the layers generator
	 */
	private void generateLayers() {
		logger.info("Generator starts");
		part.buildBits2D();
		waitGeneratorDone();
		checkGeneratedPart();
		logger.info("Generator finishes");
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
				logger.log(Level.SEVERE, e.getMessage(), e);
			}
		}
		if (timeElapsed >= TIME_LIMIT && !part.isGenerated())
			fail("Cannot wait until part is completely generated");
	}

	/**
	 * Test the optimization
	 */
	private void optimizeLayer() {
		logger.info("Optimizer starts");

		logger.warning("We only try optimize one random layer");
		Layer layerToTest = getRandomLayer();
		if (layerToTest == null)
			fail("No layer to test");
		int res = pattern.optimize(layerToTest);
		if (res < 0)
			logger.warning("Optimization failed on layer " + layerToTest.getLayerNumber());
		else if (res > 0)
			logger.warning(
					res + " irregularities on layer " + layerToTest.getLayerNumber() + " have not been resolved");
		else
			logger.info("Optimization succeeded on layer " + layerToTest.getLayerNumber());

		logger.info("Optimizer finishes");
	}

	/**
	 * 
	 * @return a random layer. <tt>null</tt> if {@link #part} is empty
	 */
	private Layer getRandomLayer() {
		List<Layer> layers = part.getLayers();
		if (layers.isEmpty())
			return null;
		int i = (int) Math.round(Math.random() * (layers.size() - 1));
		return layers.get(i);
	}
}