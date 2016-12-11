package bitSlicer.gui;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.MouseInputListener;

import bitSlicer.Bit2D;
import bitSlicer.Bit3D;
import bitSlicer.GeneratedPart;
import bitSlicer.Layer;
import bitSlicer.Pattern;
import bitSlicer.Slicer.Slice;
import bitSlicer.Slicer.Config.CraftConfig;
import bitSlicer.util.AreaTool;
import bitSlicer.util.Polygon;
import bitSlicer.util.Segment2D;
import bitSlicer.util.Shape2D;
import bitSlicer.util.Vector2;


public class PreviewFrame extends JFrame
{
	private static final long serialVersionUID = 1L;
	private GeneratedPart part;
	public Vector2 selectedBitKey = null;
	
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
	        	
	        	if(layer.getBit3D(b).getLiftPoint() != null){
	        		g.setColor(Color.RED);
	        		Point2D liftPoint = affTrans.transform(layer.getBit3D(b).getLiftPoint(), null);
	        		drawModelCircle(g, new Vector2(liftPoint.getX(), liftPoint.getY()), 10);
	        	}

//	        	Vector<Path2D> cutPaths = pattern.getCutPaths(b);
//	        	if(cutPaths != null){
//	        		for(Path2D cut : cutPaths) {
//	        			if (blue) {
//		        			g2d.setColor( Color.blue);
//		        			blue = false;
//		        		}	
//		        		else {
//		        			blue = true;
//		        			g2d.setColor( Color.red);
//		        		}
//	        			drawModelPath2D(g2d, cut);
//	        		}
//	        			
//	        	}
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
//			if (s.getPrev() == null)
//				drawModelCircle(g, s.start, 10);
//			if (s.getNext() == null)
//				drawModelCircle(g, s.end, 10);
			
		}
		
		private void drawModelLine(Graphics g, Vector2 start, Vector2 end)
		{
			g.drawLine((int) ((start.x + viewOffsetX) * drawScale) + this.getWidth() / 2, (int) ((start.y + viewOffsetY) * drawScale) + this.getHeight() / 2, (int) ((end.x + viewOffsetX) * drawScale) + this.getWidth() / 2, (int) ((end.y + viewOffsetY) * drawScale) + this.getHeight() / 2);
		}
		
		private void drawModelCircle(Graphics g, Vector2 center, int radius)
		{
			g.drawOval((int) ((center.x + viewOffsetX) * drawScale) + this.getWidth() / 2 - radius / 2, (int) ((center.y + viewOffsetY) * drawScale) + this.getHeight() / 2 - radius / 2, (int) (radius * drawScale), (int) (radius * drawScale));
		}
		
		private void drawModelPath2D(Graphics2D g2d, Path2D path){
			AffineTransform tx1 = new AffineTransform();
	        tx1.translate(viewOffsetX * drawScale + this.getWidth()/2, viewOffsetY * drawScale + this.getHeight()/2);
	        tx1.scale(drawScale, drawScale);
//	        g2d.setColor( Color.red);
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

		public void mousePressed(MouseEvent e) {}

		public void mouseReleased(MouseEvent e) {}

		public void mouseEntered(MouseEvent e) {}

		public void mouseExited(MouseEvent e) {}
	}

	
	public PreviewFrame(GeneratedPart part)
	{
		final PreviewPanel viewPanel = new PreviewPanel();
		JPanel actionPanel = new JPanel();
		actionPanel.setLayout(new BoxLayout(actionPanel, BoxLayout.X_AXIS));
		JPanel patternModifPanel = new JPanel();
		patternModifPanel.setLayout(new BoxLayout(patternModifPanel, BoxLayout.X_AXIS));
		this.setTitle("MeshIneBits - 2D preview");
		this.part = part;
		
		final JSpinner layerSpinner = new JSpinner(new SpinnerNumberModel(viewPanel.showLayer, 0, part.getLayers().size() - 1, 1));
		final JSpinner sliceSpinner = new JSpinner(new SpinnerNumberModel(viewPanel.showSlice, -1, 1000, 1));
		final JSpinner zoomSpinner = new JSpinner(new SpinnerNumberModel(viewPanel.drawScale, 1.0, 200.0, 1.0));
		
		sliceSpinner.addChangeListener(new ChangeListener()
		{
			public void stateChanged(ChangeEvent e)
			{
				selectedBitKey = null;
				int spinnerValue = ((Integer) sliceSpinner.getValue()).intValue();
				
				if ((spinnerValue + 1 <= part.getLayers().get(viewPanel.showLayer).getSlices().size()) && (spinnerValue >= 0)){
					viewPanel.showSlice = spinnerValue;
				}
				else if (spinnerValue < 0){
					if (viewPanel.showLayer - 1 >= 0){
						viewPanel.showLayer = ((Integer) layerSpinner.getValue()).intValue() - 1;
						layerSpinner.setValue(((Integer) layerSpinner.getValue()).intValue() - 1);
						viewPanel.showSlice = part.getLayers().get(viewPanel.showLayer).getSlices().size() - 1;
						sliceSpinner.setValue(part.getLayers().get(viewPanel.showLayer).getSlices().size() - 1);
					}
					else{
						sliceSpinner.setValue(0);
					}
				}
				else{
					if (viewPanel.showLayer + 1 <= part.getLayers().size() - 1){
						viewPanel.showLayer = ((Integer) layerSpinner.getValue()).intValue() + 1;
						layerSpinner.setValue(((Integer) layerSpinner.getValue()).intValue() + 1);
						viewPanel.showSlice = 0;
						sliceSpinner.setValue(0);
					}
					else{
						sliceSpinner.setValue(spinnerValue - 1);
					}
				}
				
				viewPanel.repaint();
			}
		});
		
		zoomSpinner.addChangeListener(new ChangeListener()
		{
			public void stateChanged(ChangeEvent e)
			{
				viewPanel.drawScale = ((Double) zoomSpinner.getValue()).doubleValue();
				viewPanel.repaint();
			}
		});
		
		layerSpinner.addChangeListener(new ChangeListener()
		{
			public void stateChanged(ChangeEvent e)
			{
				selectedBitKey = null;
				viewPanel.showLayer = ((Integer) layerSpinner.getValue()).intValue();
				viewPanel.showSlice = 0;
				sliceSpinner.setValue(0);
				viewPanel.repaint();
			}	
			
		});
		
		JButton replaceBitButton1 = new JButton("Replace bit >");
		
		replaceBitButton1.addActionListener(new ActionListener() {
	         public void actionPerformed(ActionEvent e) {
	        	 if(selectedBitKey != null){
	        		 Pattern pattern = part.getLayers().get(viewPanel.showLayer).getPatterns().get(viewPanel.showSlice);
	        		 Bit2D bit = pattern.getBit(selectedBitKey);	 	        
	        		 pattern.removeBit(selectedBitKey);
	        		 double newOriginX = bit.getOrigin().x + bit.getOrientation().normal().x * CraftConfig.bitLength * 0.25;
	        		 double newOriginY = bit.getOrigin().y + bit.getOrientation().normal().y * CraftConfig.bitLength * 0.25;
	        		 Vector2 newOrigin = new Vector2(newOriginX, newOriginY);
	        		 bit = new Bit2D(newOrigin, bit.getOrientation(), 50);
	        		 pattern.addBit(bit);
	        		 selectedBitKey = null;
	        		 viewPanel.repaint();
	        	 }
	        	
	         }          
	      });
		
		JButton replaceBitButton2 = new JButton("< Replace bit");
		
		replaceBitButton2.addActionListener(new ActionListener() {
	         public void actionPerformed(ActionEvent e) {
	        	 if(selectedBitKey != null){
	        		 Pattern pattern = part.getLayers().get(viewPanel.showLayer).getPatterns().get(viewPanel.showSlice);
	        		 Bit2D bit = pattern.getBit(selectedBitKey);	 	        
	        		 pattern.removeBit(selectedBitKey);
	        		 double newOriginX = bit.getOrigin().x - bit.getOrientation().normal().x * CraftConfig.bitLength * 0.25;
	        		 double newOriginY = bit.getOrigin().y - bit.getOrientation().normal().y * CraftConfig.bitLength * 0.25;
	        		 Vector2 newOrigin = new Vector2(newOriginX, newOriginY);
	        		 bit = new Bit2D(newOrigin, bit.getOrientation(), 50);
	        		 pattern.addBit(bit);
	        		 selectedBitKey = null;
	        		 viewPanel.repaint();
	        	 }
	        	
	         }          
	      });
		
		JButton replaceBitButton3 = new JButton("< Replace full bit");
		
		replaceBitButton3.addActionListener(new ActionListener() {
	         public void actionPerformed(ActionEvent e) {
	        	 if(selectedBitKey != null){
	        		 Pattern pattern = part.getLayers().get(viewPanel.showLayer).getPatterns().get(viewPanel.showSlice);
	        		 Bit2D bit = pattern.getBit(selectedBitKey);	 	        
	        		 pattern.removeBit(selectedBitKey);
	        		 double newOriginX = bit.getOrigin().x - bit.getOrientation().normal().x * CraftConfig.bitLength * 0.5;
	        		 double newOriginY = bit.getOrigin().y - bit.getOrientation().normal().y * CraftConfig.bitLength * 0.5;
	        		 Vector2 newOrigin = new Vector2(newOriginX, newOriginY);
	        		 bit = new Bit2D(newOrigin, bit.getOrientation());
	        		 pattern.addBit(bit);
	        		 selectedBitKey = null;
	        		 viewPanel.repaint();
	        	 }
	        	
	         }          
	      });
		
		JButton replaceBitButton4 = new JButton("Replace full bit >");
		
		replaceBitButton4.addActionListener(new ActionListener() {
	         public void actionPerformed(ActionEvent e) {
	        	 if(selectedBitKey != null){
	        		 Pattern pattern = part.getLayers().get(viewPanel.showLayer).getPatterns().get(viewPanel.showSlice);
	        		 Bit2D bit = pattern.getBit(selectedBitKey);	 	        
	        		 pattern.removeBit(selectedBitKey);
	        		 double newOriginX = bit.getOrigin().x + bit.getOrientation().normal().x * CraftConfig.bitLength * 0.5;
	        		 double newOriginY = bit.getOrigin().y + bit.getOrientation().normal().y * CraftConfig.bitLength * 0.5;
	        		 Vector2 newOrigin = new Vector2(newOriginX, newOriginY);
	        		 bit = new Bit2D(newOrigin, bit.getOrientation());
	        		 pattern.addBit(bit);
	        		 selectedBitKey = null;
	        		 viewPanel.repaint();
	        	 }
	        	
	         }          
	      });
		
		JButton removeBitButton = new JButton("Delete bit");
		
		removeBitButton.addActionListener(new ActionListener() {
	         public void actionPerformed(ActionEvent e) {
	        	 if(selectedBitKey != null){
	        		 Pattern pattern = part.getLayers().get(viewPanel.showLayer).getPatterns().get(viewPanel.showSlice); 	        
	        		 pattern.removeBit(selectedBitKey);
	        		 selectedBitKey = null;
	        		 viewPanel.repaint();
	        	 }
	        	
	         }          
	      });
		
		JButton computeBitPatternButton = new JButton("Compute pattern");
		
		computeBitPatternButton.addActionListener(new ActionListener() {
	         public void actionPerformed(ActionEvent e) {
	        	 part.getLayers().get(viewPanel.showLayer).computeBitsPattern(viewPanel.showSlice);
	        	 viewPanel.repaint();
	         }          
	      });
				
		actionPanel.add(new JLabel("  Layer :  "));
		actionPanel.add(layerSpinner);
		actionPanel.add(new JLabel("   Slice:  "));
		actionPanel.add(sliceSpinner);
		actionPanel.add(new JLabel("   Zoom:  "));
		actionPanel.add(zoomSpinner);
		
		patternModifPanel.add(removeBitButton);
		patternModifPanel.add(replaceBitButton3);
		patternModifPanel.add(replaceBitButton2);
		patternModifPanel.add(replaceBitButton1);
		patternModifPanel.add(replaceBitButton4);
		patternModifPanel.add(computeBitPatternButton);
		
		this.setLayout(new BorderLayout());
		this.add(viewPanel, BorderLayout.CENTER);
		this.add(actionPanel, BorderLayout.SOUTH);
		this.add(patternModifPanel, BorderLayout.NORTH);
		
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.pack();
		this.setSize(700, 700);
		this.setVisible(true);
	}
}

