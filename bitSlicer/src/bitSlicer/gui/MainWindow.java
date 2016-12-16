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
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
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
import bitSlicer.Slicer.Config.CraftConfig;
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
		if (instance == null) {
			instance = new MainWindow();
		}
		return instance;
	}

	private MainWindow() {
		this.setIconImage(
				new ImageIcon(this.getClass().getClassLoader().getResource("resources/icon.png")).getImage()
				);
		
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
		setSize(1200, 700);
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

	private class PreviewFrame extends JPanel implements MouseWheelListener{
		private PreviewPanel pp;
		private JSlider zoomSlider;
		private JSpinner zoomSpinner;
		private JSlider layerSlider;
		private JSpinner layerSpinner;
		private JLabel bg;


		public PreviewFrame() {
			this.setLayout(new BorderLayout());

			bg = new JLabel("", SwingConstants.CENTER);
			ImageIcon icon = new ImageIcon(new ImageIcon(this.getClass().getClassLoader().getResource("resources/MeshIneBitsAlpha.png")).getImage().getScaledInstance(645, 110, Image.SCALE_SMOOTH));
			bg.setIcon(icon);
			bg.setFont(new Font(null, Font.BOLD|Font.ITALIC, 120));
			bg.setForeground(new Color(0,0,0,8));
			this.add(bg);
		}

		public void init() {
			bg.setVisible(false);
			this.setLayout(new BorderLayout());
			addMouseWheelListener(this);
			pp = new PreviewPanel();

			// Layer slider
			layerSlider = new JSlider(JSlider.VERTICAL, 0, part.getLayers().size() - 1, pp.showLayer);
			layerSpinner = new JSpinner(new SpinnerNumberModel(pp.showLayer, 0, part.getLayers().size() - 1, 1));

			layerSpinner.setFocusable(false);
			layerSpinner.setMaximumSize(new Dimension(40, 40));

			JPanel layerPanel = new JPanel();
			layerPanel.setLayout(new BoxLayout(layerPanel, BoxLayout.PAGE_AXIS));
			layerPanel.add(layerSlider);
			layerPanel.add(layerSpinner);
			layerPanel.setBorder(new EmptyBorder(0, 0, 5, 5));

			// Zoom slider
			System.out.println((int) (pp.drawScale*100.0));
			zoomSlider = new JSlider(JSlider.HORIZONTAL, 20, 2000, (int) (pp.drawScale*100.0));
			zoomSlider.setMaximumSize(new Dimension(500, 20));
			zoomSpinner = new JSpinner(new SpinnerNumberModel(pp.drawScale, 0, 250.0, 1));
			zoomSpinner.setFocusable(false);
			zoomSpinner.setMaximumSize(new Dimension(40, 40));

			JPanel zoomPanel = new JPanel();
			zoomPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
			zoomPanel.add(new JLabel("Zoom :  "));
			zoomPanel.add(zoomSpinner);
			zoomPanel.add(zoomSlider);
			zoomPanel.setAlignmentX(Component.CENTER_ALIGNMENT);;

			this.add(layerPanel, BorderLayout.EAST);
			this.add(zoomPanel, BorderLayout.SOUTH);
			this.add(pp, BorderLayout.CENTER);


			zoomSpinner.addChangeListener(new ChangeListener()
			{
				public void stateChanged(ChangeEvent e)
				{
					updateZoom((Double) zoomSpinner.getValue());
				}
			});

			zoomSlider.addChangeListener(new ChangeListener() {

				@Override
				public void stateChanged(ChangeEvent e) {
					updateZoom((double) zoomSlider.getValue()/100.0);
				}
			});

			layerSpinner.addChangeListener(new ChangeListener()
			{
				public void stateChanged(ChangeEvent e)
				{
					showLayer (((Integer) layerSpinner.getValue()).intValue());
				}	
			});

			layerSlider.addChangeListener(new ChangeListener()
			{
				public void stateChanged(ChangeEvent e)
				{
					showLayer (((Integer) layerSlider.getValue()).intValue());
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
			private Vector<Area> bitControls = new Vector<Area>();

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

						//new Color(109, 138, 192)
						g2d.setColor(new Color(164, 180, 200));
						if(b == selectedBitKey){
							g2d.setColor(new Color(94, 125, 215));
							drawModelArea(g2d, area);
						}
						else
							drawModelArea(g2d, area);
					}
					
					if (selectedBitKey != null && layer.getBit3D(selectedBitKey) != null)
						drawBitControls(g2d, selectedBitKey, layer.getBit3D(selectedBitKey));
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
				g2d.draw(area);
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

			public void drawBitControls(Graphics2D g2d, Vector2 bitKey, Bit3D bit) {
				bitControls.clear();
				TriangleShape triangleShape = new TriangleShape(
						new Point2D.Double(0, 0),
						new Point2D.Double(-7, 10),
						new Point2D.Double(7, 10)
						);
				
				Vector<Area> areas = new Vector<Area>();
				int padding = 15;
				
				Area overlapBit = new Area(new Rectangle2D.Double(-CraftConfig.bitLength/2, -CraftConfig.bitWidth/2, CraftConfig.bitLength, CraftConfig.bitWidth));
				areas.add(overlapBit);
				
				Area topArrow = new Area(triangleShape);
				AffineTransform affTrans = new AffineTransform();	        	
				affTrans.translate(0, - padding - CraftConfig.bitWidth/2);
				affTrans.rotate(0, 0);
				topArrow.transform(affTrans);
				areas.add(topArrow);
				bitControls.add(topArrow);
				
				Area leftArrow = new Area(triangleShape);
				affTrans = new AffineTransform();	        	
				affTrans.translate( padding + CraftConfig.bitLength/2, 0);
				affTrans.rotate(0, 1);
				leftArrow.transform(affTrans);
				areas.add(leftArrow);
				bitControls.add(leftArrow);
				
				Area bottomArrow = new Area(triangleShape);
				affTrans = new AffineTransform();	        	
				affTrans.translate(0, padding + CraftConfig.bitWidth/2);
				affTrans.rotate(-1, 0);
				bottomArrow.transform(affTrans);
				areas.add(bottomArrow);
				bitControls.add(bottomArrow);
				
				Area rightArrow = new Area(triangleShape);
				affTrans = new AffineTransform();	        	
				affTrans.translate(- padding - CraftConfig.bitLength/2, 0);
				affTrans.rotate(0, -1);
				rightArrow.transform(affTrans);
				areas.add(rightArrow);
				bitControls.add(rightArrow);

				for (Area area : areas) {
					affTrans = new AffineTransform();	        	
					affTrans.translate(bitKey.x, bitKey.y);
					affTrans.rotate(bit.getOrientation().x, bit.getOrientation().y);
					area.transform(affTrans);	
					
					affTrans = new AffineTransform();	
					affTrans.translate(viewOffsetX * drawScale + this.getWidth()/2, viewOffsetY * drawScale + this.getHeight()/2);
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
				
				g2d.setColor(new Color(0,114,255,50));
				g2d.fill(overlapBit);
				
			}

			public class TriangleShape extends Path2D.Double {
				public TriangleShape(Point2D... points) {
					moveTo(points[0].getX(), points[0].getY());
					lineTo(points[1].getX(), points[1].getY());
					lineTo(points[2].getX(), points[2].getY());
					closePath();
				}
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
					Point2D clickSpot = new Point2D.Double((((double) e.getX()) - this.getWidth()/2) / drawScale - viewOffsetX, (((double) e.getY()) - this.getHeight()/2) / drawScale - viewOffsetY);
					for(int i = 0; i < bitControls.size(); i++){
						if(bitControls.get(i).contains(oldX, oldY)){
							clickOnBitControl(i);
							bitControls.clear();
							return;
						}
					}
					Layer layer = part.getLayers().get(showLayer);
					Vector<Vector2> bitKeys = layer.getBits3dKeys();
					for(Vector2 bitKey : bitKeys){
						Bit3D bit = layer.getBit3D(bitKey);
						Area area = bit.getRawArea();
						AffineTransform affTrans = new AffineTransform();
						affTrans.translate(bitKey.x, bitKey.y);
						affTrans.rotate(bit.getOrientation().x, bit.getOrientation().y);
						area.transform(affTrans); 
						if(area.contains(clickSpot)){
							if(selectedBitKey == bitKey){ //then it is a click to unselect this bit
								selectedBitKey = null;
								bitControls.clear();
							}
							else if(selectedBitKey == null){ //you need to unselect the bit before being able to select a new one
								selectedBitKey = bitKey;
							}
							break;
						}
					}
					repaint();
				}
			}
			
			public void clickOnBitControl(int id){
				System.out.println("click on " + id);
				Layer layer = part.getLayers().get(showLayer);
				Vector2 direction = null;
				double offSetValue = 0;
				//Every directions are in the bit's local coordinate system
				switch(id){
				case 0: //Top direction
					direction = new Vector2(0, -1);
					offSetValue = CraftConfig.bitWidth/ 2;
					break;
				case 1: //Left direction
					direction = new Vector2(1, 0);
					offSetValue = CraftConfig.bitLength/ 2;
					break;
				case 2: //Bottom direction
					direction = new Vector2(0, 1);
					offSetValue = CraftConfig.bitWidth/ 2;
					break;
				case 3: //Right direction
					direction = new Vector2(-1, 0);
					offSetValue = CraftConfig.bitLength/ 2;
					break;
				}
				layer.moveBit(selectedBitKey, direction, offSetValue);
				selectedBitKey = null;
				repaint();
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

		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {
			int notches = e.getWheelRotation();
			double zoom = (double) zoomSpinner.getValue();
			if (notches > 0)
				zoom -= Math.abs(notches/10.0);
			else 
				zoom += Math.abs(notches/10.0);


			updateZoom(zoom);
		}

		private void updateZoom(double zoom) {
			if(zoom < 0.2)
				zoom = 0.2;

			zoomSpinner.setValue(zoom);
			zoomSlider.setValue((int) (zoom*100));
			pp.drawScale = zoom;
			pp.repaint();
		}

		private void showLayer(int layerNr) {
			layerSpinner.setValue(layerNr);
			layerSlider.setValue(layerNr);
			selectedBitKey = null;
			pp.showLayer = layerNr;
			pp.showSlice = 0;
			pp.repaint();
		}
	}

	public void setPart(GeneratedPart part) {
		this.part = part;
		this.remove(pf);
		pf = new PreviewFrame();
		this.add(pf);
		pf.init();
		pf.revalidate();
		pf.repaint();
	}

	public void closePart() {
		this.part = null;
		pf.revalidate();
		pf.repaint();
		pf.bg.setVisible(true);
	}
}









