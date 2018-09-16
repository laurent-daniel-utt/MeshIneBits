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

package meshIneBits.gui.view2d;

import meshIneBits.Bit3D;
import meshIneBits.Mesh;
import meshIneBits.Layer;
import meshIneBits.config.CraftConfig;
import meshIneBits.gui.MainController;
import meshIneBits.slicer.Slice;
import meshIneBits.util.Polygon;
import meshIneBits.util.Vector2;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A panel resided inside of {@link Wrapper} to show {@link Slice} or {@link
 * Layer} of the {@link Mesh}. It observes {@link Controller}
 */
class Core extends JPanel implements MouseMotionListener, MouseListener, MouseWheelListener, Observer {

	private static final long serialVersionUID = 1L;
	private double viewOffsetX, viewOffsetY;
	private Map<Bit3D, BitControls> bitMovers = new HashMap<>();
	private int oldX, oldY;
	private Controller controller;
	private boolean rightClickPressed = false;
	private double defaultZoom = 1;
	private double drawScale = 1;

	Core() {
		// Setting up for easier use later
		controller = Controller.getInstance();
		controller.setCurrentPart(MainController.getInstance().getCurrentMesh());

		// Actions listener
		addMouseMotionListener(this);
		addMouseListener(this);
		addMouseWheelListener(this);
		setFocusable(true);
		setupKeyBindings();
	}

	private void setupKeyBindings() {
		// Reset state when pressing Esc
		getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "RESET");
		getActionMap().put("RESET", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				controller.reset();
			}
		});
	}

	private void setDefaultZoom() {
		int width = this.getWidth();
		int height = this.getHeight();
		if (width > height)
			defaultZoom = height / (controller.getCurrentMesh().getSkirtRadius() * 2);
		else
			defaultZoom = width / (controller.getCurrentMesh().getSkirtRadius() * 2);
	}

	private void updateDrawScale() {
		drawScale = controller.getZoom() * defaultZoom;
	}

	private void clickOnBitControl(int id) {
		Layer layer = controller.getCurrentMesh().getLayers().get(controller.getCurrentLayerNumber());
		Vector2 direction = null;

		// Every directions are in the bit's local coordinate system
		switch (id) {
			case 0: // Top direction
				direction = new Vector2(0, -1);
				break;
			case 1: // Left direction
				direction = new Vector2(1, 0);
				break;
			case 2: // Bottom direction
				direction = new Vector2(0, 1);
				break;
			case 3: // Right direction
				direction = new Vector2(-1, 0);
				break;
		}
		// Move all selected bits
		final Vector2 finalDirection = direction;
		Set<Vector2> newCoordinates = controller.getSelectedBits().stream()
				.map(bit2D -> layer.moveBit(bit2D.getOrigin(), finalDirection))
				.collect(Collectors.toSet());
		controller.setSelectedBitKeys(newCoordinates);
	}

	private void paintBitControls(Bit3D bit, Graphics2D g2d) {
		bitMovers.put(bit, new BitControls(bit, g2d));
	}

	private void drawModelArea(Graphics2D g2d, Area area) {
		AffineTransform tx1 = new AffineTransform();

		tx1.translate((viewOffsetX * drawScale) + (this.getWidth() / 2),
				(viewOffsetY * drawScale) + (this.getHeight() / 2));
		tx1.scale(drawScale, drawScale);

		area.transform(tx1);

		g2d.fill(area);
		g2d.draw(area);
	}

	private void drawModelCircle(Graphics g, Vector2 center, int radius) {

		g.drawOval(
				((int) ((center.x + viewOffsetX) * drawScale) + (this.getWidth() / 2))
						- (int) ((radius * drawScale) / 2),
				((int) ((center.y + viewOffsetY) * drawScale) + (this.getHeight() / 2))
						- (int) ((radius * drawScale) / 2),
				(int) (radius * drawScale), (int) (radius * drawScale));
	}

	private void drawModelPath2D(Graphics2D g2d, Path2D path) {
		AffineTransform tx1 = new AffineTransform();

		tx1.translate((viewOffsetX * drawScale) + ((double) this.getWidth() / 2),
				(viewOffsetY * drawScale) + ((double) this.getHeight() / 2));
		tx1.scale(drawScale, drawScale);
		g2d.draw(path.createTransformedShape(tx1));
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (SwingUtilities.isLeftMouseButton(e)) {
			Mesh part = controller.getCurrentMesh();

			if ((part != null) && part.isPaved()) {
				// Get the clicked point in the right coordinate system
				Point2D.Double clickSpot = new Point2D.Double(
						((((double) e.getX()) - ((double) this.getWidth() / 2)) / drawScale) - viewOffsetX,
						((((double) e.getY()) - ((double) this.getHeight() / 2)) / drawScale) - viewOffsetY);

				if (controller.isOnAddingBits()) {
					controller.addNewBits(clickSpot);
					return;
				}

				Layer layer = controller.getCurrentMesh().getLayers().get(controller.getCurrentLayerNumber());
				Vector<Vector2> bitKeys = layer.getBits3dKeys();

				// Look if we hit a bit control (arrows)
				for (BitControls controls : bitMovers.values()) {
					for (int i = 0; i < controls.size(); i++) {
						if (controls.get(i).contains(oldX, oldY)) {
							bitMovers.clear();
							clickOnBitControl(i);
							return;
						}
					}
				}

				// Look for a bit which contains the clicked spot
				for (Vector2 bitKey : bitKeys) {
					Bit3D bit = layer.getBit3D(bitKey);
					Area area = bit.getRawArea();
					AffineTransform affTrans = new AffineTransform();
					affTrans.translate(bitKey.x, bitKey.y);
					affTrans.rotate(bit.getOrientation().x, bit.getOrientation().y);
					area.transform(affTrans);
					if (area.contains(clickSpot)) {
						controller.addOrRemoveSelectedBitKeys(bitKey);
						break;
					}
				}
			}
		}
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		if (SwingUtilities.isMiddleMouseButton(e) || SwingUtilities.isLeftMouseButton(e)) {

			viewOffsetX += (e.getX() - oldX) / drawScale;
			viewOffsetY += (e.getY() - oldY) / drawScale;
			repaint();
		}
		oldX = e.getX();
		oldY = e.getY();
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		oldX = e.getX();
		oldY = e.getY();
		if (controller.isOnAddingBits())
			repaint();
	}

	@Override
	public void mousePressed(MouseEvent e) {
		if (SwingUtilities.isRightMouseButton(e)) {
			rightClickPressed = true;
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if (SwingUtilities.isRightMouseButton(e)) {
			rightClickPressed = false;
		}
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		if (!rightClickPressed) {
			// Zoom on the view
			int notches = e.getWheelRotation();
			double zoom = controller.getZoom();
			if (notches > 0) {
				zoom = zoom / 1.25;
			} else {
				zoom = zoom * 1.25;
			}
			controller.setZoom(zoom);
		} else {
			// Navigate through layers when right click pressed
			int notches = e.getWheelRotation();
			int layer = controller.getCurrentLayerNumber();
			if (notches > 0) {
				layer -= Math.abs(notches);
			} else {
				layer += Math.abs(notches);
			}
			controller.setLayer(layer);
		}
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		requestFocusInWindow();

		setDefaultZoom();
		updateDrawScale();

		Graphics2D g2d = (Graphics2D) g;
		Mesh currentPart = controller.getCurrentMesh();

		// If part is only sliced (layers not generated yet), draw the slices
		if ((currentPart != null) && !currentPart.isPaved()) {
			paintSlices(currentPart, g2d);
		}

		// If layers are generated, draw the patterns
		else if ((currentPart != null) && currentPart.isPaved()) {

			// Draw previous layer
			if (controller.showPreviousLayer() && (controller.getCurrentLayerNumber() > 0)) {
				paintPreviousLayer(g2d);
			}

			// Draw bits
			Layer currentLayer = currentPart.getLayers().get(controller.getCurrentLayerNumber());
			paintBits(currentPart, currentLayer, g2d);

			// Draw the slices contained in the layer
			if (controller.showSlices()) {
				paintSlicesInTheSameLayer(currentPart, currentLayer, g2d);
			}

			// Draw the controls of the selected bit
			bitMovers.clear();
			if (!controller.getSelectedBitKeys().isEmpty())
				controller.getSelectedBits()
						.forEach(bit -> this.paintBitControls(bit, g2d));

			// Draw the preview of adding bits
			if (controller.isOnAddingBits()) {
				paintBitPreview(g2d);
			}
		}
	}

	private void paintBitPreview(Graphics2D g2d) {
		Rectangle2D.Double r = new Rectangle2D.Double(
				-CraftConfig.bitLength / 2, -CraftConfig.bitWidth / 2,
				controller.newBitsLengthParam.getCurrentValue(),
				controller.newBitsWidthParam.getCurrentValue());
		Area a = new Area(r);
		AffineTransform affineTransform = new AffineTransform();
		affineTransform.translate(oldX, oldY);
		Vector2 lOrientation = Vector2.getEquivalentVector(
				controller.newBitsOrientationParam.getCurrentValue());
		affineTransform.rotate(lOrientation.x, lOrientation.y);
		affineTransform.scale(drawScale, drawScale);
		a.transform(affineTransform);

		g2d.setColor(Color.BLUE);
		g2d.setStroke(new BasicStroke(1.0f));
		g2d.draw(a);
	}

	private void paintSlices(Mesh currentPart, Graphics2D g2d) {
		Slice slice = currentPart.getSlices().get(controller.getCurrentSliceNumber());
		for (Polygon p : slice) {
			drawModelPath2D(g2d, p.toPath2D());
		}
	}

	private void paintSlicesInTheSameLayer(Mesh currentPart, Layer layer, Graphics2D g2d) {
		g2d.setStroke(new BasicStroke(0.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
		g2d.setColor(new Color(100 + (155 / layer.getSlices().size()), 50, 0));
		for (int i = 0; i < layer.getSlices().size(); i++) {
			if (i == layer.getSliceToSelect()) {
				// Set the selected slice of the layer in blue
				Stroke dashed = new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{2},
						0);
				g2d.setStroke(dashed);
				g2d.setColor(Color.blue);
			} else {
				// Set the other slices in different red to differentiate them from each other
				g2d.setStroke(new BasicStroke(0.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
				g2d.setColor(new Color(100 + ((i + 1) * (155 / layer.getSlices().size())), 50, 0));
			}

			for (Polygon p : layer.getSlices().get(i)) {
				drawModelPath2D(g2d, p.toPath2D());
			}
		}
	}

    private void paintBits(Mesh mesh, Layer layer, Graphics2D g2d) {
		Vector<Vector2> bitKeys = layer.getBits3dKeys();
		// Get all the irregular bits (bitKey in fact) in this layer
        List<Vector2> irregularBitsOfThisLayer = mesh.getIrregularBitKeysOf(layer);

		for (Vector2 b : bitKeys) { // Draw each bits
			Bit3D bit = layer.getBit3D(b);
			Area area = bit.getRawArea(); // Get the area of the bit
			AffineTransform affTrans = new AffineTransform();
			affTrans.translate(b.x, b.y);
			affTrans.rotate(bit.getOrientation().x, bit.getOrientation().y);
			area.transform(affTrans); // Put the bit's area at the right place

			// Color irregular bits
			g2d.setColor(new Color(164, 180, 200, 200));
			if (controller.showIrregularBits() && irregularBitsOfThisLayer.contains(b)) {
				g2d.setColor(new Color(255, 0, 0, 100));
			}
			// Draw the bit's area
			drawModelArea(g2d, area);

			// Draw the cut path
			if (controller.showCutPaths() && (bit.getCutPaths() != null)) {
				g2d.setColor(Color.blue.darker());
				g2d.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
				for (Path2D p : bit.getCutPaths()) {
					Path2D path = (Path2D) p.clone();
					path.transform(affTrans);
					drawModelPath2D(g2d, path);
				}
			}

			// Draw the lift points path if checkbox is checked
			if (controller.showLiftPoints()) {
				g2d.setColor(Color.red);
				g2d.setStroke(new BasicStroke(0.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
				for (Vector2 liftPoint : bit.getLiftPoints()) {
					if (liftPoint != null) {
						Point2D point = new Point2D.Double();
						affTrans.transform(new Point2D.Double(liftPoint.x, liftPoint.y), point);
						drawModelCircle(g2d, new Vector2(point.getX(), point.getY()), (int) CraftConfig.suckerDiameter);
					}
				}
			}

		}
	}

	/**
	 * Draw the outline of the layer below the current showing one
	 *
	 * @param g2d
	 */
	private void paintPreviousLayer(Graphics2D g2d) {

		Layer previousLayer = controller.getCurrentMesh().getLayers()
				.get(controller.getCurrentLayerNumber() - 1);
		Vector<Vector2> previousBitKeys = previousLayer.getBits3dKeys();

		g2d.setColor(Color.DARK_GRAY);
		g2d.setStroke(new BasicStroke(0.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));

		for (Vector2 b : previousBitKeys) {

			Bit3D bit = previousLayer.getBit3D(b);
			Area area = bit.getRawArea();
			AffineTransform affTrans = new AffineTransform();
			affTrans.translate(b.x, b.y);
			affTrans.rotate(bit.getOrientation().x, bit.getOrientation().y);

			area.transform(affTrans);

			AffineTransform tx1 = new AffineTransform();

			tx1.translate((viewOffsetX * drawScale) + (this.getWidth() / 2),
					(viewOffsetY * drawScale) + (this.getHeight() / 2));
			tx1.scale(drawScale, drawScale);

			area.transform(tx1);

			g2d.draw(area);
		}
	}

	@Override
	public void update(Observable o, Object arg) {
		revalidate();
		repaint();
	}

	private class TriangleShape extends Path2D.Double {
		private static final long serialVersionUID = -147647250831261196L;

		private TriangleShape(Point2D... points) {
			moveTo(points[0].getX(), points[0].getY());
			lineTo(points[1].getX(), points[1].getY());
			lineTo(points[2].getX(), points[2].getY());
			closePath();
		}
	}

	private class BitControls extends Vector<Area> {
		BitControls(Bit3D bit, Graphics2D g2d) {
			// Defining the shape of the arrows
			TriangleShape triangleShape = new TriangleShape(new Point2D.Double(0, 0),
					new Point2D.Double(-7, 10),
					new Point2D.Double(7, 10));

			int padding = 15; // Space between bit and arrows

			Area overlapBit = new Area(new Rectangle2D.Double(-CraftConfig.bitLength / 2, -CraftConfig.bitWidth / 2,
					CraftConfig.bitLength, CraftConfig.bitWidth));
			Vector<Area> areas = new Vector<>();
			areas.add(overlapBit);

			AffineTransform affTrans = new AffineTransform();

			Area topArrow = new Area(triangleShape);
			affTrans.translate(0, -padding - (CraftConfig.bitWidth / 2));
			affTrans.rotate(0, 0);
			topArrow.transform(affTrans);
			areas.add(topArrow);
			this.add(topArrow);

			Area leftArrow = new Area(triangleShape);
			affTrans = new AffineTransform();
			affTrans.translate(padding + (CraftConfig.bitLength / 2), 0);
			affTrans.rotate(0, 1);
			leftArrow.transform(affTrans);
			areas.add(leftArrow);
			this.add(leftArrow);

			Area bottomArrow = new Area(triangleShape);
			affTrans = new AffineTransform();
			affTrans.translate(0, padding + (CraftConfig.bitWidth / 2));
			affTrans.rotate(-1, 0);
			bottomArrow.transform(affTrans);
			areas.add(bottomArrow);
			this.add(bottomArrow);

			Area rightArrow = new Area(triangleShape);
			affTrans = new AffineTransform();
			affTrans.translate(-padding - (CraftConfig.bitLength / 2), 0);
			affTrans.rotate(0, -1);
			rightArrow.transform(affTrans);
			areas.add(rightArrow);
			this.add(rightArrow);

			double landscapeWidth = Core.this.getWidth();
			double landscapeHeight = Core.this.getHeight();

			for (Area area : areas) {
				affTrans = new AffineTransform();
				affTrans.translate(bit.getOrigin().x, bit.getOrigin().y);
				affTrans.rotate(bit.getOrientation().x, bit.getOrientation().y);
				area.transform(affTrans);

				affTrans = new AffineTransform();
				affTrans.translate(viewOffsetX * drawScale + landscapeWidth / 2,
						viewOffsetY * drawScale + landscapeHeight / 2);
				affTrans.scale(drawScale, drawScale);
				area.transform(affTrans);

				g2d.setColor(new Color(94, 125, 215));
				if (!area.equals(overlapBit)) {
					g2d.draw(area);
					g2d.fill(area);
				}
			}

			g2d.setStroke(new BasicStroke(0.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
			g2d.setColor(new Color(94, 125, 215));
			g2d.draw(overlapBit);

			g2d.setColor(new Color(0, 114, 255, 50));
			g2d.fill(overlapBit);
		}
	}
}