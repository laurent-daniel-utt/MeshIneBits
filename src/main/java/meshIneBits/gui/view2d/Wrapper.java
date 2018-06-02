package meshIneBits.gui.view2d;

import meshIneBits.GeneratedPart;
import meshIneBits.gui.MainWindow;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Observable;
import java.util.Observer;

/**
 * The panel wrapping 2D representation with view orienting tools. Exported from
 * {@link MainWindow}. It observes {@link Controller}.
 */
public class Wrapper extends JPanel implements Observer {

	private static final long serialVersionUID = 1L;
	private Core coreView;
	private JSlider zoomSlider;
	private JSlider layerSlider;
	private JSpinner layerSpinner;
	private JSlider sliceSlider;
	private JSpinner sliceSpinner;
	private JPanel layerPanel;
	private JPanel slicePanel;
	private JPanel displayOptionsPanel;
	private JLabel bg;

	private final double minZoomValue = 0.5;
	private final double maxZoomValue = 10;
	private final int minZoomSliderValue = 1;
	private final int maxZoomSliderValue = 500;

	// Following attributes are the coefficients from the formula Y = a * EXP(b * x)
	// used for the zoom's log scale
	private final double aCoef;
	private final double bCoef;

	public Wrapper() {
		this.setLayout(new BorderLayout());

		Controller.getInstance().addObserver(this);

		bg = new JLabel("", SwingConstants.CENTER);
		ImageIcon icon = new ImageIcon(
				new ImageIcon(this.getClass().getClassLoader().getResource("resources/MeshIneBitsAlpha.png")).getImage()
						.getScaledInstance(645, 110, Image.SCALE_SMOOTH));
		bg.setIcon(icon);
		bg.setFont(new Font(null, Font.BOLD | Font.ITALIC, 120));
		bg.setForeground(new Color(0, 0, 0, 8));
		this.add(bg, BorderLayout.CENTER);

		bCoef = Math.log(minZoomValue / maxZoomValue) / (minZoomSliderValue - maxZoomSliderValue);
		aCoef = minZoomValue / Math.exp(bCoef * minZoomSliderValue);
		
		// Set up the core of view
		coreView = new Core();
		Controller.getInstance().addObserver(coreView);
	}

	private int getZoomSliderValue(double zoomValue) {
		return (int) (Math.log(zoomValue / aCoef) / bCoef);
	}

	private double getZoomValue(int zoomSliderValue) {
		return aCoef * Math.exp(bCoef * zoomSliderValue);
	}

	private void buildDisplayOptionsPanel() {
		Controller controller = Controller.getInstance();
		zoomSlider = new JSlider(SwingConstants.HORIZONTAL, minZoomSliderValue, maxZoomSliderValue,
				getZoomSliderValue(controller.getZoom()));
		zoomSlider.setMaximumSize(new Dimension(maxZoomSliderValue, 20));

		ButtonIcon zoomMinusBtn = new ButtonIcon("search-minus.png");
		ButtonIcon zoomPlusBtn = new ButtonIcon("search-plus.png");

		displayOptionsPanel = new JPanel();
		displayOptionsPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		displayOptionsPanel.add(zoomMinusBtn);
		displayOptionsPanel.add(zoomSlider);
		displayOptionsPanel.add(zoomPlusBtn);
		displayOptionsPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

		this.add(displayOptionsPanel, BorderLayout.SOUTH);

		zoomSlider.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				controller.setZoom(getZoomValue(zoomSlider.getValue()));
			}
		});

		zoomMinusBtn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				controller.setZoom(controller.getZoom() / 2);
			}
		});

		zoomPlusBtn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				controller.setZoom(controller.getZoom() * 2);
			}
		});
	}

	private void buildLayerPanel() {
		Controller controller = Controller.getInstance();
		GeneratedPart part = controller.getCurrentPart();

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
				controller.setLayer(((Integer) layerSpinner.getValue()).intValue());
			}
		});

		layerSlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				controller.setLayer(((Integer) layerSlider.getValue()).intValue());
			}
		});
	}

	private void buildSlicePanel() {
		Controller controller = Controller.getInstance();
		GeneratedPart part = controller.getCurrentPart();

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
				controller.setSlice(((Integer) sliceSpinner.getValue()).intValue());
			}
		});

		sliceSlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				controller.setSlice(((Integer) sliceSlider.getValue()).intValue());
			}
		});
	}

	private void init() {
		// remove old components
		this.removeAll();
		
		// Repaint
		this.add(coreView, BorderLayout.CENTER);

		if (Controller.getInstance().getCurrentPart().isGenerated()) {
			buildLayerPanel();
		} else {
			buildSlicePanel();
		}

		buildDisplayOptionsPanel();
	}

	private void noPart() {
		this.removeAll();

		this.add(bg);
		repaint();
		revalidate();
	}

	@Override
	@SuppressWarnings("incomplete-switch")
	public void update(Observable o, Object arg) {
		if (arg != null) {
			Controller controller = Controller.getInstance();
			switch ((Controller.Component) arg) {
			case PART:
				GeneratedPart part = controller.getCurrentPart();
				System.out.println(
						part.toString() + " isGenerated = " + part.isGenerated() + " isSliced = " + part.isSliced());
				if ((part != null) && (part.isGenerated() || part.isSliced())) {
					System.out.println("Initializing 2D view");
					init();
					repaint();
					revalidate();
				} else {
					noPart();
				}
				break;
			case LAYER:
				updateLayerChoice(controller.getCurrentLayerNumber());
				break;
			case ZOOM:
				updateZoom(controller.getZoom());
				break;
			case SLICE:
				updateSliceChoice(controller.getCurrentSliceNumber());
				break;
			}
		}
		repaint();
		revalidate();
	}

	private void updateLayerChoice(int layerNr) {
		try {
			layerSpinner.setValue(layerNr);
			layerSlider.setValue(layerNr);
		} catch (Exception e) {
			// If layer spinner and slider don't exist
		}
	}

	private void updateSliceChoice(int sliceNr) {
		try {
			sliceSpinner.setValue(sliceNr);
			sliceSlider.setValue(sliceNr);
		} catch (Exception e) {
			// If slice spinner and slider don't exist
		}
	}

	private void updateZoom(double zoom) {
		try {
			if (getZoomSliderValue(zoom) <= maxZoomSliderValue)
				zoomSlider.setValue(getZoomSliderValue(zoom));
			else if (getZoomSliderValue(zoom) > maxZoomSliderValue && zoomSlider.getValue() < maxZoomSliderValue - 1) {
				zoomSlider.setValue(maxZoomSliderValue - 1);
			}

		} catch (Exception e) {
			// If the slider doesn't exist
		}
	}

	private class ButtonIcon extends JButton {
		/**
		 *
		 */
		private static final long serialVersionUID = -7001268017690625534L;

		public ButtonIcon(String iconName) {
			super("");
			try {
				ImageIcon icon = new ImageIcon(
						new ImageIcon(this.getClass().getClassLoader().getResource("resources/" + iconName)).getImage()
								.getScaledInstance(22, 22, Image.SCALE_DEFAULT));
				this.setIcon(icon);
			} catch (Exception e) {
				e.printStackTrace();
			}

			setContentAreaFilled(false);
			setBorder(new EmptyBorder(3, 3, 3, 3));

			addMouseListener(new java.awt.event.MouseAdapter() {
				@Override
				public void mouseEntered(java.awt.event.MouseEvent evt) {
					setContentAreaFilled(true);
				}

				@Override
				public void mouseExited(java.awt.event.MouseEvent evt) {
					setContentAreaFilled(false);
				}
			});

			this.setHorizontalAlignment(LEFT);
			this.setMargin(new Insets(0, 0, 0, 0));
		}
	}
}
