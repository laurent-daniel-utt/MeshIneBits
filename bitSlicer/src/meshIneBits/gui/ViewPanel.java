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

public class ViewPanel extends JPanel implements MouseWheelListener, Observer {

	private static final long serialVersionUID = 1L;
	private View pp;
	private JSlider zoomSlider;
	private JSpinner zoomSpinner;
	private JSlider layerSlider;
	private JSpinner layerSpinner;
	JPanel layerPanel;
	JPanel zoomPanel;
	public JLabel bg;
	ViewObservable viewObservable;
	
	public ViewPanel(View pp) {
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
		this.viewObservable = (ViewObservable) sv;
		switch((ViewObservable.Component) arg){
		case PART:
			if(this.viewObservable.getCurrentPart() != null)
				init();
			else
				noPart();
			break;
		case LAYER:
			updateLayerChoice(this.viewObservable.getCurrentLayerNumber());
			break;
		case ZOOM:
			updateZoom(this.viewObservable.getZoom());
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
		if (viewObservable.getCurrentPart().isGenerated()) {
			layerSlider = new JSlider(SwingConstants.VERTICAL, 0, viewObservable.getCurrentPart().getLayers().size() - 1, 0);
			layerSpinner = new JSpinner(new SpinnerNumberModel(0, 0, viewObservable.getCurrentPart().getLayers().size() - 1, 1));
		} else {
			layerSlider = new JSlider(SwingConstants.VERTICAL, 0, viewObservable.getCurrentPart().getSlices().size() - 1, 0);
			layerSpinner = new JSpinner(new SpinnerNumberModel(0, 0, viewObservable.getCurrentPart().getSlices().size() - 1, 1));
		}

		layerSpinner.setFocusable(false);
		layerSpinner.setMaximumSize(new Dimension(40, 40));

		layerPanel = new JPanel();
		layerPanel.setLayout(new BoxLayout(layerPanel, BoxLayout.PAGE_AXIS));
		layerPanel.add(layerSlider);
		layerPanel.add(layerSpinner);
		layerPanel.setBorder(new EmptyBorder(0, 0, 5, 5));

		// Zoom slider
		System.out.println((int) (viewObservable.getZoom() * 100.0));
		zoomSlider = new JSlider(SwingConstants.HORIZONTAL, 20, 2000, (int) (viewObservable.getZoom() * 100.0));
		zoomSlider.setMaximumSize(new Dimension(500, 20));
		zoomSpinner = new JSpinner(new SpinnerNumberModel(viewObservable.getZoom(), 0, 250.0, 1));
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
				viewObservable.setZoom((Double) zoomSpinner.getValue());
			}
		});

		zoomSlider.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				viewObservable.setZoom(zoomSlider.getValue() / 100.0);
			}
		});

		layerSpinner.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				viewObservable.setLayer(((Integer) layerSpinner.getValue()).intValue());
			}
		});

		layerSlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				viewObservable.setLayer(((Integer) layerSlider.getValue()).intValue());
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
		viewObservable.setZoom(zoom);
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
