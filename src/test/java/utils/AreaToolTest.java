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

package utils;

import meshIneBits.Mesh;
import meshIneBits.Model;
import meshIneBits.slicer.Slice;
import meshIneBits.util.AreaTool;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class AreaToolTest {

	/**
	 * Initialize this logger when subclass
	 */
	private static Logger logger = meshIneBits.util.Logger.createSimpleInstanceFor(AreaToolTest.class);

	private Model model;

	private Mesh part;

	private static int TIME_LIMIT = 60;

	/**
	 * Load up the model and slice it
	 *
	 * @param modelFilename short name of model to load from stlModel folder
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
		part = new Mesh(model);
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

	@ParameterizedTest(name = "{index}. model={0}. slice={1}")
	@MethodSource("areaSampleProvider")
	void testGetContinuousSurfacesFrom(String modelFilename, int sliceNum,
									   List<Point2D.Double> insidePoints,
									   List<Point2D.Double> outsidePoints) {
		setUpPart(modelFilename);
		slicePart();
		Slice s = part.getSlices().get(sliceNum);
		Area unionArea = new Area();
		AreaTool.getContinuousSurfacesFrom(s).forEach(unionArea::add);
		logger.info("Model=" + modelFilename);
		logger.info("SliceNum=" + sliceNum);
		logger.info("UnionAreaBoundary=" + unionArea.getBounds2D());
		s.forEach(polygon -> logger.info("PolygonBoundary=" + AreaTool.getAreaFrom(polygon).getBounds2D()));
		// Check inner
		for (Point2D p : insidePoints) {
			assertTrue(unionArea.contains(p), p + " should be inside");
		}
		// Check outer
		for (Point2D p : outsidePoints) {
			assertFalse(unionArea.contains(p), p + " should be outside");
		}
		logger.info("Success");
	}

	@ParameterizedTest(name = "{index}. model={0}. slice={1}")
	@MethodSource("areaSampleProvider")
	@Disabled
	void testGetLevel0AreasFrom(String modelFilename, int sliceNum,
								List<Point2D.Double> insidePoints,
								List<Point2D.Double> outsidePoints) {
		setUpPart(modelFilename);
		slicePart();
		Slice s = part.getSlices().get(sliceNum);
		Area unionArea = new Area();
		List<Area> a = AreaTool.getLevel0AreasFrom(s);
		if (a != null) a.forEach(unionArea::add);
		logger.info("Model=" + modelFilename);
		logger.info("SliceNum=" + sliceNum);
		logger.info("UnionAreaBoundary=" + unionArea.getBounds2D());
		s.forEach(polygon -> logger.info("PolygonBoundary=" + AreaTool.getAreaFrom(polygon).getBounds2D()));
		// Check inner
		for (Point2D p : insidePoints) {
			assertTrue(unionArea.contains(p), p + " should be inside");
		}
		// Check outer
		for (Point2D p : outsidePoints) {
			assertFalse(unionArea.contains(p), p + " should be outside");
		}
		logger.info("Success");
	}

	/**
	 * Read areaSample.json and parse
	 *
	 * @return model filename, number of slice to check, list of inside points
	 * and list of outside points
	 */
	private static Stream<Arguments> areaSampleProvider() {
		List<Arguments> listArguments = new Vector<>();
		try {
			String content = readJSONFile(AreaToolTest.class.getResource("/areaSample.json").getPath());
			JSONObject object = new JSONObject(content);
			JSONArray samples = object.getJSONArray("samples");
			for (int i = 0; i < samples.length(); i++) {
				JSONObject sample = samples.getJSONObject(i);
				String modelFilename = sample.getString("modelFilename");
				int sliceNum = sample.getInt("sliceNum");
				List<Point2D.Double> insidePoints = jsonArrayToListPoints(sample.getJSONArray("insidePoints"));
				List<Point2D.Double> outsidePoints = jsonArrayToListPoints(sample.getJSONArray("outsidePoints"));
				listArguments.add(Arguments.of(modelFilename, sliceNum, insidePoints, outsidePoints));
			}
		} catch (IOException e) {
			fail("Failed to load sample file. " + e.getMessage());
		}
		return listArguments.stream();
	}

	/**
	 * Read JSON file
	 *
	 * @param path absolute path
	 * @return whole content of file
	 * @throws IOException when file not found or error on reading
	 */
	private static String readJSONFile(String path) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(path));
		StringBuilder content = new StringBuilder();
		String line;
		while ((line = br.readLine()) != null) {
			content.append(line);
		}
		return content.toString();
	}

	/**
	 * Convert json array to list of points {@link Double}
	 *
	 * @param jArray read from file
	 * @return list of points 2D
	 */
	private static List<Point2D.Double> jsonArrayToListPoints(JSONArray jArray) {
		List<Point2D.Double> l = new Vector<>(jArray.length());
		for (int j = 0; j < jArray.length(); j++) {
			JSONObject o = jArray.getJSONObject(j);
			l.add(new Point2D.Double(o.getDouble("x"), o.getDouble("y")));
		}
		return l;
	}
}