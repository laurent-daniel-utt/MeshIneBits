package meshIneBits.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.Observable;
import java.util.Observer;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class PreviewFrame extends JPanel implements MouseWheelListener, Observer {

	private static final long serialVersionUID = 1L;
	private PreviewPanel pp;
	private JSlider zoomSlider;
	private JSpinner zoomSpinner;
	private JSlider layerSlider;
	private JSpinner layerSpinner;
	JPanel layerPanel;
	JPanel zoomPanel;
	public JLabel bg;
	ShowedView sv;
	
	public PreviewFrame(PreviewPanel pp) {
		this.setLayout(new BorderLayout());
		this.pp = pp;

		bg = new JLabel("", SwingConstants.CENTER);
		ImageIcon icon = new ImageIcon(
				new ImageIcon(this.getClass().getClassLoader().getResource("resources/MeshIneBitsAlpha.png")).getImage().getScaledInstance(645, 110, Image.SCALE_SMOOTH));
		bg.setIcon(icon);
		bg.setFont(new Font(null, Font.BOLD | Font.ITALIC, 120));
		bg.setForeground(new Color(0, 0, 0, 8));
		this.add(bg);
	}
	
	@SuppressWarnings("incomplete-switch")
	public void update(Observable sv, Object arg) {
		this.sv = (ShowedView) sv;
		switch((ShowedView.Component) arg){
		case PART:
			if(this.sv.getCurrentPart() != null)
				init();
			else
				noPart();
			break;
		case LAYER:
			updateLayerChoice(this.sv.getCurrentLayerNumber());
			break;
		case ZOOM:
			updateZoom(this.sv.getZoom());
			break;
		}
	}
	
	public void noPart(){
		remove(pp);
		remove(layerPanel);
		remove(zoomPanel);
		bg.setVisible(true);
	}
	
	public void init() {
		bg.setVisible(false);
		this.setLayout(new BorderLayout());
		addMouseWheelListener(this);

		// Layer slider
		layerSlider = new JSlider(SwingConstants.VERTICAL, 0, sv.getCurrentPart().getLayers().size() - 1, 0);
		layerSpinner = new JSpinner(new SpinnerNumberModel(0, 0, sv.getCurrentPart().getLayers().size() - 1, 1));

		layerSpinner.setFocusable(false);
		layerSpinner.setMaximumSize(new Dimension(40, 40));

		layerPanel = new JPanel();
		layerPanel.setLayout(new BoxLayout(layerPanel, BoxLayout.PAGE_AXIS));
		layerPanel.add(layerSlider);
		layerPanel.add(layerSpinner);
		layerPanel.setBorder(new EmptyBorder(0, 0, 5, 5));

		// Zoom slider
		System.out.println((int) (sv.getZoom() * 100.0));
		zoomSlider = new JSlider(SwingConstants.HORIZONTAL, 20, 2000, (int) (sv.getZoom() * 100.0));
		zoomSlider.setMaximumSize(new Dimension(500, 20));
		zoomSpinner = new JSpinner(new SpinnerNumberModel(sv.getZoom(), 0, 250.0, 1));
		zoomSpinner.setFocusable(false);
		zoomSpinner.setMaximumSize(new Dimension(40, 40));

		zoomPanel = new JPanel();
		zoomPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		zoomPanel.add(new JLabel("Zoom :  "));
		zoomPanel.add(zoomSpinner);
		zoomPanel.add(zoomSlider);
		zoomPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
		;

		this.add(layerPanel, BorderLayout.EAST);
		this.add(zoomPanel, BorderLayout.SOUTH);
		this.add(pp, BorderLayout.CENTER);

		zoomSpinner.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				sv.setZoom((Double) zoomSpinner.getValue());
			}
		});

		zoomSlider.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				sv.setZoom(zoomSlider.getValue() / 100.0);
			}
		});

		layerSpinner.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				sv.setLayer(((Integer) layerSpinner.getValue()).intValue());
			}
		});

		layerSlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				sv.setLayer(((Integer) layerSlider.getValue()).intValue());
			}
		});
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		int notches = e.getWheelRotation();
		double zoom = (double) zoomSpinner.getValue();
		if (notches > 0) {
			zoom -= Math.abs(notches / 10.0);
		} else {
			zoom += Math.abs(notches / 10.0);
		}
		sv.setZoom(zoom);
	}

	private void updateLayerChoice(int layerNr) {
		layerSpinner.setValue(layerNr);
		layerSlider.setValue(layerNr);
	}

	private void updateZoom(double zoom) {
		zoomSpinner.setValue(zoom);
		zoomSlider.setValue((int) (zoom * 100));
	}
}
