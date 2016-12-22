package meshIneBits.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Observable;
import java.util.Observer;
import java.util.Vector;

import javax.swing.JPanel;

import meshIneBits.Bit3D;
import meshIneBits.GeneratedPart;
import meshIneBits.Layer;
import meshIneBits.Slicer.Config.CraftConfig;
import meshIneBits.util.Segment2D;
import meshIneBits.util.Vector2;

public class PreviewPanel extends JPanel implements MouseMotionListener, MouseListener, Observer {
	
	private static final long serialVersionUID = 1L;
	public double viewOffsetX, viewOffsetY;
	private Vector<Area> bitControls = new Vector<Area>();
	private int oldX, oldY;
	ShowedView sv;
	
	public PreviewPanel() {
		addMouseMotionListener(this);
		addMouseListener(this);
	}
	
	@Override
	public void update(Observable sv, Object arg) {
		this.sv = (ShowedView) sv;
		switch((ShowedView.Component) arg){
		case PART:
			//init();
			break;
		case ME:
			break;
		default:
			repaint();
			break;
		}
	}
	
	public class TriangleShape extends Path2D.Double {
		public TriangleShape(Point2D... points) {
			moveTo(points[0].getX(), points[0].getY());
			lineTo(points[1].getX(), points[1].getY());
			lineTo(points[2].getX(), points[2].getY());
			closePath();
		}
	}
	
	public void clickOnBitControl(int id) {
		Layer layer = sv.getCurrentPart().getLayers().get(sv.getCurrentLayerNumber());
		Vector2 direction = null;
		double offSetValue = 0;
		//Every directions are in the bit's local coordinate system
		switch (id) {
		case 0: //Top direction
			direction = new Vector2(0, -1);
			offSetValue = CraftConfig.bitWidth / 2;
			break;
		case 1: //Left direction
			direction = new Vector2(1, 0);
			offSetValue = CraftConfig.bitLength / 2;
			break;
		case 2: //Bottom direction
			direction = new Vector2(0, 1);
			offSetValue = CraftConfig.bitWidth / 2;
			break;
		case 3: //Right direction
			direction = new Vector2(-1, 0);
			offSetValue = CraftConfig.bitLength / 2;
			break;
		}
		layer.moveBit(sv.getSelectedBitKey(), direction, offSetValue);
		sv.setSelectedBitKey(null);
		repaint();
	}

	public void drawBitControls(Graphics2D g2d, Vector2 bitKey, Bit3D bit) {
		bitControls.clear();
		TriangleShape triangleShape = new TriangleShape(new Point2D.Double(0, 0), new Point2D.Double(-7, 10), new Point2D.Double(7, 10));

		Vector<Area> areas = new Vector<Area>();
		int padding = 15;

		Area overlapBit = new Area(new Rectangle2D.Double(-CraftConfig.bitLength / 2, -CraftConfig.bitWidth / 2, CraftConfig.bitLength, CraftConfig.bitWidth));
		areas.add(overlapBit);

		Area topArrow = new Area(triangleShape);
		AffineTransform affTrans = new AffineTransform();
		affTrans.translate(0, -padding - (CraftConfig.bitWidth / 2));
		affTrans.rotate(0, 0);
		topArrow.transform(affTrans);
		areas.add(topArrow);
		bitControls.add(topArrow);

		Area leftArrow = new Area(triangleShape);
		affTrans = new AffineTransform();
		affTrans.translate(padding + (CraftConfig.bitLength / 2), 0);
		affTrans.rotate(0, 1);
		leftArrow.transform(affTrans);
		areas.add(leftArrow);
		bitControls.add(leftArrow);

		Area bottomArrow = new Area(triangleShape);
		affTrans = new AffineTransform();
		affTrans.translate(0, padding + (CraftConfig.bitWidth / 2));
		affTrans.rotate(-1, 0);
		bottomArrow.transform(affTrans);
		areas.add(bottomArrow);
		bitControls.add(bottomArrow);

		Area rightArrow = new Area(triangleShape);
		affTrans = new AffineTransform();
		affTrans.translate(-padding - (CraftConfig.bitLength / 2), 0);
		affTrans.rotate(0, -1);
		rightArrow.transform(affTrans);
		areas.add(rightArrow);
		bitControls.add(rightArrow);

		double drawScale = sv.getZoom();
		
		for (Area area : areas) {
			affTrans = new AffineTransform();
			affTrans.translate(bitKey.x, bitKey.y);
			affTrans.rotate(bit.getOrientation().x, bit.getOrientation().y);
			area.transform(affTrans);

			affTrans = new AffineTransform();
			affTrans.translate((viewOffsetX * drawScale) + (this.getWidth() / 2), (viewOffsetY * drawScale) + (this.getHeight() / 2));
			affTrans.scale(drawScale, drawScale);
			area.transform(affTrans);

			g2d.setColor(new Color(94, 125, 215));
			if (!area.equals(overlapBit)) {
				g2d.draw(area);
				g2d.fill(area);
			}
		}

		g2d.setColor(new Color(94, 125, 215));
		g2d.draw(overlapBit);

		g2d.setColor(new Color(0, 114, 255, 50));
		g2d.fill(overlapBit);

	}

	private void drawModelArea(Graphics2D g2d, Area area) {
		AffineTransform tx1 = new AffineTransform();
		double drawScale = sv.getZoom();
		tx1.translate((viewOffsetX * drawScale) + (this.getWidth() / 2), (viewOffsetY * drawScale) + (this.getHeight() / 2));
		tx1.scale(drawScale, drawScale);

		area.transform(tx1);

		g2d.fill(area);
		g2d.draw(area);
	}

	private void drawModelArea2(Graphics2D g2d, Area area) {
		AffineTransform tx1 = new AffineTransform();
		double drawScale = sv.getZoom();
		tx1.translate((viewOffsetX * drawScale) + (this.getWidth() / 2), (viewOffsetY * drawScale) + (this.getHeight() / 2));
		tx1.scale(drawScale, drawScale);

		area.transform(tx1);
		//g2d.setColor( Color.green );
		//g2d.draw(area);
		g2d.setColor(Color.red);

		g2d.setStroke(new BasicStroke(0.0f, // Line width
				BasicStroke.CAP_BUTT, // End-cap style
				BasicStroke.JOIN_BEVEL)); // Vertex join style

		g2d.fill(area);
	}

	private void drawModelCircle(Graphics g, Vector2 center, int radius) {
		double drawScale = sv.getZoom();
		g.drawOval(((int) ((center.x + viewOffsetX) * drawScale) + (this.getWidth() / 2)) - (int) (radius * drawScale / 2),
				((int) ((center.y + viewOffsetY) * drawScale) + (this.getHeight() / 2)) - (int) (radius * drawScale / 2), (int) (radius * drawScale), (int) (radius * drawScale));
	}

	private void drawModelLine(Graphics g, Vector2 start, Vector2 end) {
		double drawScale = sv.getZoom();
		g.drawLine((int) ((start.x + viewOffsetX) * drawScale) + (this.getWidth() / 2), (int) ((start.y + viewOffsetY) * drawScale) + (this.getHeight() / 2),
				(int) ((end.x + viewOffsetX) * drawScale) + (this.getWidth() / 2), (int) ((end.y + viewOffsetY) * drawScale) + (this.getHeight() / 2));
	}

	private void drawModelPath2D(Graphics2D g2d, Path2D path) {
		AffineTransform tx1 = new AffineTransform();
		double drawScale = sv.getZoom();
		tx1.translate((viewOffsetX * drawScale) + (this.getWidth() / 2), (viewOffsetY * drawScale) + (this.getHeight() / 2));
		tx1.scale(drawScale, drawScale);
		//			        g2d.setColor( Color.red);
		g2d.draw(path.createTransformedShape(tx1));
	}

	private void drawSegment(Graphics g, Segment2D s) {
		((Graphics2D) g).setStroke(new BasicStroke(1));
		g.setColor(Color.RED);
		drawModelLine(g, s.start, s.end);

		// Show discontinuity
		//					if (s.getPrev() == null)
		//						drawModelCircle(g, s.start, 10);
		//					if (s.getNext() == null)
		//						drawModelCircle(g, s.end, 10);

	}

	private void drawString(Graphics g, String text, double x, double y) {
		//((x + viewOffsetX) * drawScale)
		g.setColor(Color.BLACK);
		double drawScale = sv.getZoom();
		g.drawString(text, (int) ((x + viewOffsetX) * drawScale) + (this.getWidth() / 2), (int) ((y + viewOffsetY) * drawScale) + (this.getHeight() / 2));
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		GeneratedPart part = sv.getCurrentPart();
		double drawScale = sv.getZoom();
		if (part != null) {
			Point2D clickSpot = new Point2D.Double(((((double) e.getX()) - (this.getWidth() / 2)) / drawScale) - viewOffsetX,
					((((double) e.getY()) - (this.getHeight() / 2)) / drawScale) - viewOffsetY);
			for (int i = 0; i < bitControls.size(); i++) {
				if (bitControls.get(i).contains(oldX, oldY)) {
					clickOnBitControl(i);
					bitControls.clear();
					return;
				}
			}
			Layer layer = sv.getCurrentPart().getLayers().get(sv.getCurrentLayerNumber());
			Vector<Vector2> bitKeys = layer.getBits3dKeys();
			for (Vector2 bitKey : bitKeys) {
				Bit3D bit = layer.getBit3D(bitKey);
				Area area = bit.getRawArea();
				AffineTransform affTrans = new AffineTransform();
				affTrans.translate(bitKey.x, bitKey.y);
				affTrans.rotate(bit.getOrientation().x, bit.getOrientation().y);
				area.transform(affTrans);
				if (area.contains(clickSpot)) {
					if (sv.getSelectedBitKey() == bitKey) { //then it is a click to unselect this bit
						sv.setSelectedBitKey(null);
						bitControls.clear();
					} else if (sv.getSelectedBitKey() == null) { //you need to unselect the bit before being able to select a new one
						sv.setSelectedBitKey(bitKey);
					}
					break;
				}
			}
			repaint();
		}
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		double drawScale = sv.getZoom();
		viewOffsetX += (e.getX() - oldX) / drawScale;
		viewOffsetY += (e.getY() - oldY) / drawScale;
		repaint();
		oldX = e.getX();
		oldY = e.getY();
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseMoved(MouseEvent e) {
		oldX = e.getX();
		oldY = e.getY();
	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);
		if (sv.getCurrentPart() != null) {

			Graphics2D g2d = (Graphics2D) g;

			Layer layer = sv.getCurrentPart().getLayers().get(sv.getCurrentLayerNumber());
			Vector<Vector2> bitKeys = layer.getBits3dKeys();

			boolean blue = false;

			for (Vector2 b : bitKeys) {

				Bit3D bit = layer.getBit3D(b);
				Area area = bit.getRawArea();
				AffineTransform affTrans = new AffineTransform();
				affTrans.translate(b.x, b.y);
				affTrans.rotate(bit.getOrientation().x, bit.getOrientation().y);

				area.transform(affTrans);

				//new Color(109, 138, 192)
				g2d.setColor(new Color(164, 180, 200));
				if (b == sv.getSelectedBitKey()) {
					g2d.setColor(new Color(94, 125, 215));
					drawModelArea(g2d, area);
				} else {
					drawModelArea(g2d, area);
				}

				for (Vector2 liftPoint : bit.getLiftPoints()) {
					if (liftPoint != null) {
						g.setColor(Color.red);
						Point2D point = new Point2D.Double();
						affTrans.transform(new Point2D.Double(liftPoint.x, liftPoint.y), point);
						drawModelCircle(g, new Vector2(point.getX(), point.getY()), (int) CraftConfig.suckerDiameter);
					}

				}

			}

			if ((sv.getSelectedBitKey() != null) && (layer.getBit3D(sv.getSelectedBitKey()) != null)) {
				drawBitControls(g2d, sv.getSelectedBitKey(), layer.getBit3D(sv.getSelectedBitKey()));
			}
		}
	}

}