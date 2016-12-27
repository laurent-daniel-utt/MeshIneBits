package meshIneBits.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.Observable;
import java.util.Observer;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import meshIneBits.GeneratedPart;

public class ViewPanel extends JPanel implements Observer {

	private static final long serialVersionUID = 1L;
	private View view;
	private JSlider zoomSlider;
	private JSlider layerSlider;
	private JSpinner layerSpinner;
	private JSlider sliceSlider;
	private JSpinner sliceSpinner;
	private JPanel layerPanel;
	private JPanel slicePanel;
	private JPanel displayOptionsPanel;
	public JLabel bg;
	private ViewObservable viewObservable;

	public ViewPanel() {
		this.setLayout(new BorderLayout());

		viewObservable = ViewObservable.getInstance();

		bg = new JLabel("", SwingConstants.CENTER);
		ImageIcon icon = new ImageIcon(
				new ImageIcon(this.getClass().getClassLoader().getResource("resources/MeshIneBitsAlpha.png")).getImage().getScaledInstance(645, 110, Image.SCALE_SMOOTH));
		bg.setIcon(icon);
		bg.setFont(new Font(null, Font.BOLD | Font.ITALIC, 120));
		bg.setForeground(new Color(0, 0, 0, 8));
		this.add(bg);
	}

	@SuppressWarnings("incomplete-switch")
	public void update(Observable o, Object arg) {		
		if (arg != null) {
			switch((ViewObservable.Component) arg){
			case PART:
				GeneratedPart part = this.viewObservable.getCurrentPart();
				if(part != null && (part.isGenerated() || part.isSliced())) 
				{
					init();						
				}else
					noPart();
				break;
			case LAYER:
				updateLayerChoice(this.viewObservable.getCurrentLayerNumber());
				break;
			case ZOOM:
				updateZoom(this.viewObservable.getZoom());
				break;
			case SLICE:
				updateSliceChoice(this.viewObservable.getCurrentSliceNumber());
				break;
			}
		}
		repaint();
		revalidate();
	}

	public void noPart(){
		for (Component c : this.getComponents())
			remove(c);

		this.add(bg);
		repaint();
		revalidate();
	}

	public void init() {
		this.setLayout(new BorderLayout());
		

		this.view = new View();
		viewObservable.addObserver(view);

		// remove old components
		for(Component c : this.getComponents())
			this.remove(c);

		if (viewObservable.getCurrentPart().isGenerated())
			buildLayerPanel();
		else
			buildSlicePanel();
		
		buildDisplayOptionsPanel();
		
		this.add(view, BorderLayout.CENTER);
	}
	
	private void buildLayerPanel(){
		GeneratedPart part = viewObservable.getCurrentPart();
		
		layerSlider = new JSlider(SwingConstants.VERTICAL, 0, part.getLayers().size() - 1, 0);
		layerSpinner = new JSpinner(new SpinnerNumberModel(0, 0, part.getLayers().size() - 1, 1));
		
		layerSpinner.setFocusable(false);
		layerSpinner.setMaximumSize(new Dimension(40, 40));
		
		layerPanel = new JPanel();
		layerPanel.setLayout(new BoxLayout(layerPanel, BoxLayout.PAGE_AXIS));
		layerPanel.add(layerSlider);
		layerPanel.add(layerSpinner);
		layerPanel.setBorder(new EmptyBorder(0, 0, 5, 5));
		
		this.add(layerPanel, BorderLayout.EAST);
		
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
	
	private void buildSlicePanel(){
		GeneratedPart part = viewObservable.getCurrentPart();
		
		sliceSlider = new JSlider(SwingConstants.VERTICAL, 0, part.getSlices().size() - 1, 0);
		sliceSpinner = new JSpinner(new SpinnerNumberModel(0, 0, part.getSlices().size() - 1, 1));
		
		sliceSpinner.setFocusable(false);
		sliceSpinner.setMaximumSize(new Dimension(40, 40));
		
		slicePanel = new JPanel();
		slicePanel.setLayout(new BoxLayout(slicePanel, BoxLayout.PAGE_AXIS));
		slicePanel.add(sliceSlider);
		slicePanel.add(sliceSpinner);
		slicePanel.setBorder(new EmptyBorder(0, 5, 5, 0));
		
		this.add(slicePanel, BorderLayout.EAST);
		
		sliceSpinner.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				viewObservable.setSlice(((Integer) sliceSpinner.getValue()).intValue());
			}
		});

		sliceSlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				viewObservable.setSlice(((Integer) sliceSlider.getValue()).intValue());
			}
		});
	}
	
	private void buildDisplayOptionsPanel(){
		// Zoom slider
				zoomSlider = new JSlider(SwingConstants.HORIZONTAL, 20, 500, (int) (viewObservable.getZoom() * 100.0));
				zoomSlider.setMaximumSize(new Dimension(500, 20));

				displayOptionsPanel = new JPanel();
				displayOptionsPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
				displayOptionsPanel.add(new JLabel(new ImageIcon(
						new ImageIcon(this.getClass().getClassLoader().getResource("resources/" + "search-minus.png")).getImage().getScaledInstance(22, 22, Image.SCALE_DEFAULT))));
				displayOptionsPanel.add(zoomSlider);
				displayOptionsPanel.add(new JLabel(new ImageIcon(
						new ImageIcon(this.getClass().getClassLoader().getResource("resources/" + "search-plus.png")).getImage().getScaledInstance(22, 22, Image.SCALE_DEFAULT))));
				displayOptionsPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

				
				this.add(displayOptionsPanel, BorderLayout.SOUTH);

				zoomSlider.addChangeListener(new ChangeListener() {

					@Override
					public void stateChanged(ChangeEvent e) {
						viewObservable.setZoom(zoomSlider.getValue() / 100.0);
					}
				});		
	}
	
	private void updateLayerChoice(int layerNr) {
		try{
			layerSpinner.setValue(layerNr);
			layerSlider.setValue(layerNr);
		}
		catch(Exception e){
			//If layer spinner and slider don't exist
		}
	}

	private void updateZoom(double zoom) {
		try{
			zoomSlider.setValue((int) (zoom * 100));
		}
		catch(Exception e){
			//If the slider doesn't exist
		}
	}
	
	private void updateSliceChoice(int sliceNr){
		try{
			sliceSpinner.setValue(sliceNr);
			sliceSlider.setValue(sliceNr);
		}
		catch(Exception e){
			//If slice spinner and slider don't exist
		}
	}
}
