package meshIneBits;

import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Path2D;
import java.util.Vector;

import meshIneBits.config.CraftConfig;
import meshIneBits.util.AreaTool;
import meshIneBits.util.Vector2;

/**
 * Bit2D represent a bit in 2D : boundaries and cut path. A {@link Bit3D} is
 * build with multiple Bit2D <br>
 * <img src="./doc-files/bit2d.png" alt="">
 * 
 * @see Bit3D
 */
public class Bit2D implements Cloneable {
	/**
	 * In the pattern coordinate system without rotation or translation of whole
	 * object.
	 */
	private final Vector2 origin;
	private Vector2 orientation;
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
	 *            the model
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
	 * A new full bit with <tt>originBit</tt> and <tt>orientation</tt> in the
	 * coordinate system of the associated pattern
	 * 
	 * @param origin the center of bit's outer bound
	 * @param orientation the rotation of bit
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
	 * Constructor for custom length and width.
	 * 
	 * @param origin the center of bit's outer bound
	 * @param orientation the rotation of bit
	 * @param length length of the bit
	 * @param width width of the bit
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
	 * @param origin center of bit's outer bound
	 * @param orientation rotation of the bit
	 * @param length length of the bit
	 * @param width width of the bit
	 * @param transfoMatrix transformation to be applied
	 * @param inverseTransfoMatrix inversion of <tt>transfoMatrix</tt>
	 * @param cutPaths where to cut this bit
	 * @param areas set of non intersected areas
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
	 * Create the area of the bit and set an initial cut path if necessary. This
	 * is necessary when the bit has been reduced manually. Note: Oy axe points
	 * downward and Ox points to the right. We always take the up right corner
	 * as ({@link CraftConfig#bitLength bitLength} / 2, -
	 * {@link CraftConfig#bitWidth bitWidth} / 2 ). The bit' boundary is a
	 * rectangle.
	 */
	private void buildBoundaries() {
		Vector2 cornerUpRight = new Vector2(+CraftConfig.bitLength / 2.0, -CraftConfig.bitWidth / 2.0);
		Vector2 cornerDownRight = new Vector2(cornerUpRight.x, cornerUpRight.y + width);
		Vector2 cornerUpLeft = new Vector2(cornerUpRight.x - length, cornerUpRight.y);
		Vector2 cornerDownLeft = new Vector2(cornerDownRight.x - length, cornerDownRight.y);

		Path2D path = new Path2D.Double();
		path.moveTo(cornerUpRight.x, cornerUpRight.y);
		path.lineTo(cornerDownRight.x, cornerDownRight.y);
		path.lineTo(cornerDownLeft.x, cornerDownLeft.y);
		path.lineTo(cornerUpLeft.x, cornerUpLeft.y);
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
	 * @return the union of all surfaces making this bit transformed by
	 *         <tt>transfoMatrix</tt>
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
	 * @return clone of all surfaces making this bit transformed by
	 *         <tt>transforMatrix</tt>
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

	/**
	 * @return clone of raw areas
	 */
	public Vector<Area> getClonedAreas() {
		Vector<Area> clonedAreas = new Vector<Area>();
		for (Area a : areas) {
			clonedAreas.add((Area) a.clone());
		}
		return clonedAreas;
	}

	/**
	 * @return clone of raw cut paths
	 */
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
	 * @return the center of the rectangle of this bit, not necessarily the
	 *         {@link #origin origin}
	 */
	public Vector2 getCenter() {
		double verticalDistance = -(CraftConfig.bitWidth - width) / 2,
				horizontalDistance = (CraftConfig.bitLength - length) / 2;
		return origin.add(new Vector2(horizontalDistance, verticalDistance));
	}

	/**
	 * A raw area is an area that has not been transformed to another coordinate
	 * system.
	 * 
	 * @return the union of raw (non intersected) areas
	 */
	public Area getRawArea() {
		Area area = new Area();
		for (Area a : areas) {
			area.add(a);
		}
		return area;
	}

	/**
	 * @return set of raw areas of this bit (not transformed)
	 */
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
	 * @return a set of lift points, each of which is in charge of each
	 *         separated area (in case a bit has many separated areas)
	 */
	public Vector<Vector2> computeLiftPoints() {
		Vector<Vector2> result = new Vector<Vector2>();
		for (Area area : areas) {
			Vector2 localLiftPoint = AreaTool.getLiftPoint(area, CraftConfig.suckerDiameter / 2);
			if (localLiftPoint != null) {
				result.add(localLiftPoint);
			}
		}
		return result;
	}

	/**
	 * To resize a bit, keeping up-right corner as reference.
	 * 
	 * @param newPercentageLength
	 *            100 means retain 100% of old bit's length
	 * @param newPercentageWidth
	 *            100 means retain 100% of old bit's width
	 */
	public void resize(double newPercentageLength, double newPercentageWidth) {
		length = length * newPercentageLength / 100;
		width = width * newPercentageWidth / 100;
		// Rebuild the boundary
		buildBoundaries();
	}

	@Override
	public String toString() {
		return "Bit2D [origin=" + origin + ", length=" + length + ", width=" + width + ", orientation=" + orientation
				+ "]";
	}

	/**
	 * This method only accepts the conservative transformation (no scaling).
	 * The coordinates are rounded by {@link CraftConfig#errorAccepted} to
	 * accelerate calculation.
	 * 
	 * @param transformation
	 *            a combination of affine transformation
	 * @return a new bit with same geometric with initial one transformed by
	 *         <tt>transfoMatrix</tt>
	 */
	public Bit2D createTransformedBit(AffineTransform transformation) {
		Vector2 newOrigin = origin.getTransformed(transformation).getRounded(), newOrientation = origin.add(orientation)
				.getTransformed(transformation).sub(newOrigin).normal().getRounded();
		Bit2D newBit = new Bit2D(newOrigin, newOrientation, length, width);
		newBit.updateBoundaries(this.getArea().createTransformedArea(transformation));
		return newBit;
	}
}