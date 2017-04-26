package meshIneBits;

import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Path2D;
import java.util.HashMap;
import java.util.Vector;

import meshIneBits.Config.CraftConfig;
import meshIneBits.util.AreaTool;
import meshIneBits.util.Direction;
import meshIneBits.util.Vector2;

/**
 * Bit2D represent a bit in 2D : boundaries and cut path. A {@link Bit3D} is
 * build with multiple Bit2D <br>
 * <img src="./doc-files/bit2d.png">
 * 
 * @see Bit3D
 */
public class Bit2D implements Cloneable {
	private final Vector2 origin; // In the pattern coordinate system
	private Vector2 orientation; // Around the bit origin
	private double length;
	private double width;
	private AffineTransform transfoMatrix = new AffineTransform();
	private AffineTransform inverseTransfoMatrix;
	private Vector<Path2D> cutPaths = null;
	private Vector<Area> areas = new Vector<Area>();

	/**
	 * Constructor to clone an existing bit into a smaller one.
	 * 
	 * All the other parameters remain unchanged in comparison to model bit.
	 * 
	 * @param modelBit
	 * @param percentageLength
	 *            from 0 to 100
	 * @param percentageWidth
	 *            from 0 to 100
	 */
	public Bit2D(Bit2D modelBit, double percentageLength, double percentageWidth) {
		this.origin = modelBit.origin;
		this.orientation = modelBit.orientation;
		 length = (CraftConfig.bitLength * percentageLength) / 100;
		 width = (CraftConfig.bitWidth * percentageWidth) / 100;

		setTransfoMatrix();
		buildBoundaries();
	}

	/**
	 * originBit and orientation are in the coordinate system of the associated
	 * pattern
	 */
	public Bit2D(Vector2 origin, Vector2 orientation) {
		this.origin = origin;
		this.orientation = orientation;
		length = CraftConfig.bitLength;
		width = CraftConfig.bitWidth;

		setTransfoMatrix();
		buildBoundaries();
	}

	/**
	 * Constructor for custom length & width.
	 * 
	 * @param origin
	 * @param orientation
	 * @param length
	 * @param width
	 */
	public Bit2D(Vector2 origin, Vector2 orientation, double length, double width) {
		this.origin = origin;
		this.orientation = orientation;
		this.length = length;
		this.width = width;

		setTransfoMatrix();
		buildBoundaries();
	}

	/**
	 * Constructor for cloning
	 * 
	 * @param origin
	 * @param orientation
	 * @param length
	 * @param width
	 * @param transfoMatrix
	 * @param inverseTransfoMatrix
	 * @param cutPaths
	 * @param areas
	 */
	public Bit2D(Vector2 origin, Vector2 orientation, double length, double width, AffineTransform transfoMatrix,
			AffineTransform inverseTransfoMatrix, Vector<Path2D> cutPaths, Vector<Area> areas) {
		this.origin = origin;
		this.orientation = orientation;
		this.length = length;
		this.width = width;
		this.transfoMatrix = transfoMatrix;
		this.inverseTransfoMatrix = inverseTransfoMatrix;
		this.cutPaths = cutPaths;
		this.areas = areas;
	}

	/**
	 * Create the four corners as in a normal bit. Note: Oy axe points downward
	 * and Ox points to the right. We always take the up right corner as the
	 * point of "depart". The bit' boundary is a rectangle.
	 * 
	 * @return list consisting of UpRight, DownRight, UpLeft, DownLeft in the
	 *         own coordinate system of this bit
	 */
	public HashMap<Direction, Vector2> getRawNormalCorners() {
		HashMap<Direction, Vector2> corners = new HashMap<>();
		Vector2 cornerUpRight = new Vector2(+CraftConfig.bitLength / 2.0, -CraftConfig.bitWidth / 2.0);
		Vector2 cornerDownRight = new Vector2(cornerUpRight.x, cornerUpRight.y + width);
		Vector2 cornerUpLeft = new Vector2(cornerUpRight.x - length, cornerUpRight.y);
		Vector2 cornerDownLeft = new Vector2(cornerDownRight.x - length, cornerDownRight.y);
		corners.put(Direction.UPRIGHT, cornerUpRight);
		corners.put(Direction.DOWNRIGHT, cornerDownRight);
		corners.put(Direction.UPLEFT, cornerUpLeft);
		corners.put(Direction.DOWNLEFT, cornerDownLeft);
		return corners;
	}

	/**
	 * Create the four corners as in a normal bit. Note: Oy axe points downward
	 * and Ox points to the right. We always take the up right corner as the
	 * point of "depart". The bit' boundary is a rectangle.
	 * 
	 * @return list consisting of UpRight, DownRight, UpLeft, DownLeft in the
	 *         coordinate system of the layer containing this bit
	 */
	public HashMap<Direction, Vector2> getTransfoNormalCorners() {
		HashMap<Direction, Vector2> rawCorners = getRawNormalCorners();
		HashMap<Direction, Vector2> transfoCorners = new HashMap<>();
		for (Direction key : rawCorners.keySet()) {
			transfoCorners.put(key, rawCorners.get(key).getTransformed(transfoMatrix));
		}
		return transfoCorners;
	}

	/**
	 * Create the area of the bit and set an initial cut path if necessary. This
	 * is necessary when the bit has been reduced manually. Note: Oy axe points
	 * downward and Ox points to the right. We always take the up right corner
	 * as the point of "depart". The bit' boundary is a rectangle.
	 */
	private void buildBoundaries() {
		HashMap<Direction, Vector2> corners = getRawNormalCorners();
		Vector2 cornerUpRight = corners.get(Direction.UPRIGHT);
		Vector2 cornerDownRight = corners.get(Direction.DOWNRIGHT);
		Vector2 cornerUpLeft = corners.get(Direction.UPLEFT);
		Vector2 cornerDownLeft = corners.get(Direction.DOWNLEFT);

		Path2D path = new Path2D.Double();
		path.moveTo(cornerUpLeft.x, cornerUpLeft.y);
		path.lineTo(cornerUpRight.x, cornerUpRight.y);
		path.lineTo(cornerDownRight.x, cornerDownRight.y);
		path.lineTo(cornerDownLeft.x, cornerDownLeft.y);
		path.closePath();

		this.areas.add(new Area(path));

		// Set a cut path if necessary
		if ((length != CraftConfig.bitLength) || (width != CraftConfig.bitWidth)) {
			cutPaths = new Vector<Path2D>();
			Path2D.Double cutPath = new Path2D.Double();
			if ((length != CraftConfig.bitLength) && (width != CraftConfig.bitWidth)) {
				cutPath.moveTo(cornerDownLeft.x, cornerDownLeft.y);
				cutPath.lineTo(cornerUpLeft.x, cornerUpLeft.y);
				cutPath.lineTo(cornerUpRight.x, cornerUpRight.y);
			} else if (length != CraftConfig.bitLength) {
				cutPath.moveTo(cornerDownLeft.x, cornerDownLeft.y);
				cutPath.lineTo(cornerUpLeft.x, cornerUpLeft.y);
			} else if (width != CraftConfig.bitWidth) {
				cutPath.moveTo(cornerUpLeft.x, cornerUpLeft.y);
				cutPath.lineTo(cornerUpRight.x, cornerUpRight.y);
			}
			this.cutPaths.add(cutPath);
		}
	}

	@Override
	public Bit2D clone() {
		return new Bit2D(origin, orientation, length, width, (AffineTransform) transfoMatrix.clone(),
				(AffineTransform) inverseTransfoMatrix.clone(), getClonedCutPaths(), getClonedAreas());
	}

	/**
	 * @return the union of all surfaces making this bit
	 */
	public Area getArea() {
		Area transformedArea = new Area();
		for (Area a : areas) {
			transformedArea.add(a);
		}
		transformedArea.transform(transfoMatrix);
		return transformedArea;
	}

	/**
	 * @return all surfaces making this bit
	 */
	public Vector<Area> getAreas() {
		Vector<Area> result = new Vector<Area>();
		for (Area a : areas) {
			Area transformedArea = new Area(a);
			transformedArea.transform(transfoMatrix);
			result.add(transformedArea);
		}
		return result;
	}

	public Vector<Area> getClonedAreas() {
		Vector<Area> clonedAreas = new Vector<Area>();
		for (Area a : areas) {
			clonedAreas.add((Area) a.clone());
		}
		return clonedAreas;
	}

	public Vector<Path2D> getClonedCutPaths() {
		if (cutPaths != null) {
			Vector<Path2D> clonedCutPaths = new Vector<Path2D>();
			for (Path2D p : cutPaths) {
				clonedCutPaths.add((Path2D) p.clone());
			}
			return clonedCutPaths;
		} else {
			return null;
		}
	}

	public Vector<Path2D> getCutPaths() {
		if (this.cutPaths == null) {
			return null;
		} else {
			Vector<Path2D> paths = new Vector<Path2D>();
			for (Path2D p : this.cutPaths) {
				paths.add(new Path2D.Double(p, transfoMatrix));
			}
			return paths;
		}
	}

	public double getLength() {
		return length;
	}

	public Vector2 getOrientation() {
		return orientation;
	}

	/**
	 * @return the origin in the pattern coordinate system
	 */
	public Vector2 getOrigin() {
		return origin;
	}

	/**
	 * A raw area is an area that has not been transformed to another coordinate
	 * system.
	 * 
	 * @return
	 */
	public Area getRawArea() {
		Area area = new Area();
		for (Area a : areas) {
			area.add(a);
		}
		return area;
	}

	public Vector<Area> getRawAreas() {
		return areas;
	}

	public Vector<Path2D> getRawCutPaths() {
		return cutPaths;
	}

	public double getWidth() {
		return width;
	}

	public void setCutPath(Vector<Path2D> paths) {
		if (this.cutPaths == null) {
			this.cutPaths = new Vector<Path2D>();
		}

		for (Path2D p : paths) {
			this.cutPaths.add(new Path2D.Double(p, inverseTransfoMatrix));
		}
	}

	private void setTransfoMatrix() {

		transfoMatrix.translate(origin.x, origin.y);
		transfoMatrix.rotate(orientation.x, orientation.y);

		try {
			inverseTransfoMatrix = ((AffineTransform) transfoMatrix.clone()).createInverse();
		} catch (NoninvertibleTransformException e) {
			e.printStackTrace();
		}
	}

	public void updateBoundaries(Area transformedArea) {
		areas.clear();
		Area newArea = (Area) transformedArea.clone();
		newArea.transform(inverseTransfoMatrix);
		for (Area a : AreaTool.segregateArea(newArea)) {
			areas.add(a);
		}
	}

	/**
	 * @return the lift point on this bit
	 */
	public Vector2 computeLiftPoint() {
		return AreaTool.getLiftPoint(this.getArea(), CraftConfig.suckerDiameter / 2);
	}
	
	/**
	 * Used to resized a bit
	 * @param percentageLength 100 means retain 100% of old bit's length
	 * @param percentageWidth 100 means retain 100% of old bit's width
	 */
	public void resize(double percentageLength, double percentageWidth){
		length = length * percentageLength / 100;
		width = length * percentageWidth /100;
		// Rebuild the boundary
		buildBoundaries();
	}
}
