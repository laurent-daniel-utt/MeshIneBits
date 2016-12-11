package bitSlicer.gui;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.Vector;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.ColorUIResource;

import bitSlicer.Bit3D;
import bitSlicer.GeneratedPart;
import bitSlicer.Layer;
import bitSlicer.Pattern;
import bitSlicer.util.Segment2D;
import bitSlicer.util.Vector2;

public class MainWindow extends JFrame {
//	public static void main(String[] args)
//	{
//		new MainWindow();
//	}

	private GeneratedPart part;
	public Vector2 selectedBitKey = null;
	private static MainWindow instance = null;
	private PreviewFrame pf;
	
	public static MainWindow getInstance(){
		System.out.println("Zbraa");
		if (instance == null) {
			System.out.println("Zbra");
			instance = new MainWindow();
		}
		return instance;
	}

	private MainWindow() {
		System.out.println("Nouvelle fenetre");
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			UIManager.put("TabbedPane.focus", new Color(0, 0, 0, 0));
			UIManager.put("Separator.foreground", new Color(10, 10, 10, 50));
			UIManager.put("Button.focus", new ColorUIResource(new Color(0, 0, 0, 0)));
			UIManager.put("Slider.focus", new ColorUIResource(new Color(0, 0, 0, 0)));
			UIManager.put("ScrollPane.border", BorderFactory.createEmptyBorder());
		} catch (Exception e) {
			e.printStackTrace();
		}

		setTitle("MeshIneBits");
		setSize(1200, 800);
		setResizable(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		Ribbon ribbon = new Ribbon();
		pf = new PreviewFrame();

		Container content = getContentPane();
		content.setLayout(new BoxLayout(content, BoxLayout.PAGE_AXIS));
		content.add(ribbon);
		content.add(pf);

		setVisible(true);
	}

	private class PreviewFrame extends JPanel {
		private PreviewPanel pp;
		public PreviewFrame() {
			this.setLayout(new BorderLayout());

			// Layer slider
			JSlider layerSlider = new JSlider(JSlider.VERTICAL, 0, 250, 0);
			JSpinner layerSpinner = new JSpinner(new SpinnerNumberModel(2, 0, 10, 1));
			layerSpinner.setFocusable(false);
			layerSpinner.setMaximumSize(new Dimension(40, 40));

			JPanel layerPanel = new JPanel();
			layerPanel.setLayout(new BoxLayout(layerPanel, BoxLayout.PAGE_AXIS));
			layerPanel.add(layerSlider);
			layerPanel.add(layerSpinner);
			layerPanel.setBorder(new EmptyBorder(0, 0, 5, 5));

			// Zoom slider
			JSlider zoomSlider = new JSlider(JSlider.HORIZONTAL, 100, 2000, 100);
			zoomSlider.setMaximumSize(new Dimension(500, 20));

			JSpinner zoomSpinner = new JSpinner(new SpinnerNumberModel(1.0, 1.0, 250.0, 1.0));
			zoomSpinner.setFocusable(false);
			zoomSpinner.setMaximumSize(new Dimension(40, 40));

			JPanel zoomPanel = new JPanel();
			zoomPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
			zoomPanel.add(new JLabel("Zoom :  "));
			zoomPanel.add(zoomSpinner);
			zoomPanel.add(zoomSlider);
			zoomPanel.setAlignmentX(Component.CENTER_ALIGNMENT);;
			
			pp = new PreviewPanel();
			
			this.add(layerPanel, BorderLayout.EAST);
			this.add(zoomPanel, BorderLayout.SOUTH);
			this.add(pp, BorderLayout.CENTER);
			
			
			zoomSpinner.addChangeListener(new ChangeListener()
			{
				public void stateChanged(ChangeEvent e)
				{
					pp.drawScale = (Double) zoomSpinner.getValue();
					pp.repaint();
				}
			});
			
			zoomSlider.addChangeListener(new ChangeListener() {
				
				@Override
				public void stateChanged(ChangeEvent e) {
					zoomSpinner.setValue((double) zoomSlider.getValue()/100.0);
					pp.drawScale = (Double) zoomSpinner.getValue();
					pp.repaint();
				}
			});
		}

		public class PreviewPanel extends JPanel implements MouseMotionListener, MouseListener
		{
			private static final long serialVersionUID = 1L;

			public int showSlice = 0;
			public int showLayer = 0;
			public double drawScale = 1.0;
			public double viewOffsetX, viewOffsetY;

			private int oldX, oldY;

			public PreviewPanel()
			{
				addMouseMotionListener(this);
				addMouseListener(this);
			}

			public void paint(Graphics g)
			{
				super.paint(g);
				if (part != null) {

					Graphics2D g2d = (Graphics2D) g;

					Layer layer = part.getLayers().get(showLayer);
					Vector<Vector2> bitKeys = layer.getBits3dKeys();

					boolean blue = false;

					for(Vector2 b : bitKeys){

						Bit3D bit = layer.getBit3D(b);
						Area area = bit.getRawArea();
						AffineTransform affTrans = new AffineTransform();	        	
						affTrans.translate(b.x, b.y);
						affTrans.rotate(bit.getOrientation().x, bit.getOrientation().y);
						area.transform(affTrans);      	

						g2d.setColor( Color.green );
						if(b == selectedBitKey){
							g2d.setColor( Color.blue );
							drawModelArea(g2d, area);
						}
						else
							drawModelArea(g2d, area);
					}
				}
			}

			private void drawString(Graphics g, String text, double x, double y){
				//((x + viewOffsetX) * drawScale)
				g.setColor(Color.BLACK);
				g.drawString(text, (int) ((x + viewOffsetX) * drawScale)  + this.getWidth() / 2, (int) ((y + viewOffsetY) * drawScale) + this.getHeight() / 2);		}

			private void drawSegment(Graphics g, Segment2D s)
			{		
				((Graphics2D) g).setStroke(new BasicStroke(1));
				g.setColor(Color.RED);
				drawModelLine(g, s.start, s.end);

				// Show discontinuity
				//					if (s.getPrev() == null)
				//						drawModelCircle(g, s.start, 10);
				//					if (s.getNext() == null)
				//						drawModelCircle(g, s.end, 10);

			}

			private void drawModelLine(Graphics g, Vector2 start, Vector2 end)
			{
				g.drawLine((int) ((start.x + viewOffsetX) * drawScale) + this.getWidth() / 2, (int) ((start.y + viewOffsetY) * drawScale) + this.getHeight() / 2, (int) ((end.x + viewOffsetX) * drawScale) + this.getWidth() / 2, (int) ((end.y + viewOffsetY) * drawScale) + this.getHeight() / 2);
			}

			private void drawModelCircle(Graphics g, Vector2 center, int radius)
			{
				g.drawOval((int) ((center.x + viewOffsetX) * drawScale) + this.getWidth() / 2 - radius / 2, (int) ((center.y + viewOffsetY) * drawScale) + this.getHeight() / 2 - radius / 2, radius, radius);
			}

			private void drawModelPath2D(Graphics2D g2d, Path2D path){
				AffineTransform tx1 = new AffineTransform();
				tx1.translate(viewOffsetX * drawScale + this.getWidth()/2, viewOffsetY * drawScale + this.getHeight()/2);
				tx1.scale(drawScale, drawScale);
				//			        g2d.setColor( Color.red);
				g2d.draw(path.createTransformedShape(tx1));
			}

			private void drawModelArea(Graphics2D g2d, Area area){
				AffineTransform tx1 = new AffineTransform();
				tx1.translate(viewOffsetX * drawScale + this.getWidth()/2, viewOffsetY * drawScale + this.getHeight()/2);
				tx1.scale(drawScale, drawScale);

				area.transform(tx1);

				g2d.fill(area);
			}

			private void drawModelArea2(Graphics2D g2d, Area area){
				AffineTransform tx1 = new AffineTransform();
				tx1.translate(viewOffsetX * drawScale + this.getWidth()/2, viewOffsetY * drawScale + this.getHeight()/2);
				tx1.scale(drawScale, drawScale);

				area.transform(tx1);
				//g2d.setColor( Color.green );
				//g2d.draw(area);
				g2d.setColor( Color.red );

				g2d.setStroke(new BasicStroke(0.0f,                     // Line width
						BasicStroke.CAP_BUTT,    // End-cap style
						BasicStroke.JOIN_BEVEL)); // Vertex join style


				g2d.fill(area);
			}

			public void mouseDragged(MouseEvent e)
			{
				viewOffsetX += (double) (e.getX() - oldX) / drawScale;
				viewOffsetY += (double) (e.getY() - oldY) / drawScale;
				repaint();
				oldX = e.getX();
				oldY = e.getY();
			}

			public void mouseMoved(MouseEvent e)
			{
				oldX = e.getX();
				oldY = e.getY();
			}

			public void mouseClicked(MouseEvent e) {
				if (part != null) {
					Pattern pattern = part.getLayers().get(showLayer).getPatterns().get(showSlice);
					Vector<Vector2> bitKeys = pattern.getBitsKeys();
					Point2D clickSpot = new Point2D.Double((((double) e.getX()) - this.getWidth()/2) / drawScale - viewOffsetX, (((double) e.getY()) - this.getHeight()/2) / drawScale - viewOffsetY);
					for(Vector2 bitKey : bitKeys){
						if(pattern.getBitArea(bitKey).contains(clickSpot)){
							if(selectedBitKey == bitKey) //then it is a click to unselect this bit
								selectedBitKey = null;
							else{
								selectedBitKey = bitKey;
							}
							break;
						}
					}
					repaint();
				}
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
			public void mousePressed(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void mouseReleased(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}
		}

		public void update() {
			pp.repaint();			
		}
	}

	public void setPart(GeneratedPart part) {
		this.part = part;
		pf.update();
	}

	public void closePart() {
		this.part = null;
		pf.update();
	}
}









