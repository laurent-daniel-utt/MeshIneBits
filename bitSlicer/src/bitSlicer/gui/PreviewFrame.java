package bitSlicer.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import bitSlicer.GeneratedPart;
import bitSlicer.Slicer.Slice;
import bitSlicer.util.Polygon;
import bitSlicer.util.Segment2D;
import bitSlicer.util.Shape2D;
import bitSlicer.util.Vector2;


public class PreviewFrame extends JFrame
{
	private static final long serialVersionUID = 1L;
	private GeneratedPart part;
	
	public class PreviewPanel extends JPanel implements MouseMotionListener
	{
		private static final long serialVersionUID = 1L;
		
		public int showSlice = 0;
		public double drawScale = 1.0;
		public double viewOffsetX, viewOffsetY;
		
		private int oldX, oldY;
		
		public PreviewPanel()
		{
			addMouseMotionListener(this);
		}
		
		public void paint(Graphics g)
		{
			super.paint(g);
			
			Graphics2D g2d = (Graphics2D) g;
			
			
			for (Shape2D s : part.getLayers().get(showSlice).getPatterns()){
				drawModelPath2D(g2d, s.getLargestPolygon().toPath2D());				
			}
			
			
			Polygon p = part.getLayers().get(showSlice).getSlices().get(0).getLargestPolygon();
			drawModelPath2D(g2d, p.toPath2D());
			
		}
		
		private void drawSegment(Graphics g, Segment2D s)
		{		
			g.setColor(Color.GREEN);
			drawModelLine(g, s.start, s.end);
			
			// Show discontinuity
			/*
			if (s.getPrev() == null)
				drawModelCircle(g, s.start, 10);
			if (s.getNext() == null)
				drawModelCircle(g, s.end, 10);
			*/
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
			g2d.draw(path.createTransformedShape(tx1));
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
	}
	
	public PreviewFrame(GeneratedPart part)
	{
		final PreviewPanel viewPanel = new PreviewPanel();
		JPanel actionPanel = new JPanel();
		actionPanel.setLayout(new BoxLayout(actionPanel, BoxLayout.X_AXIS));
		this.setTitle("Preview");
		this.part = part;
		
		final JSpinner sliceSpinner = new JSpinner(new SpinnerNumberModel(viewPanel.showSlice, 0, part.getLayers().size() - 1, 1));
		sliceSpinner.addChangeListener(new ChangeListener()
		{
			public void stateChanged(ChangeEvent e)
			{
				viewPanel.showSlice = ((Integer) sliceSpinner.getValue()).intValue();
				viewPanel.repaint();
			}
		});
		final JSpinner zoomSpinner = new JSpinner(new SpinnerNumberModel(viewPanel.drawScale, 1.0, 200.0, 1.0));
		zoomSpinner.addChangeListener(new ChangeListener()
		{
			public void stateChanged(ChangeEvent e)
			{
				viewPanel.drawScale = ((Double) zoomSpinner.getValue()).doubleValue();
				viewPanel.repaint();
			}
		});
		
		actionPanel.add(new JLabel("Slice:"));
		actionPanel.add(sliceSpinner);
		actionPanel.add(new JLabel("Zoom:"));
		actionPanel.add(zoomSpinner);
		
		this.setLayout(new BorderLayout());
		this.add(viewPanel, BorderLayout.CENTER);
		this.add(actionPanel, BorderLayout.SOUTH);
		
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.pack();
		this.setSize(600, 600);
		this.setVisible(true);
	}
}

