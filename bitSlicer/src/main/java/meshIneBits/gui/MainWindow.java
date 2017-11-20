package meshIneBits.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.plaf.ColorUIResource;

import meshIneBits.GeneratedPart;
import meshIneBits.gui.processing.ProcessingModelView;
import meshIneBits.gui.processing.ProcessingView;

public class MainWindow extends JFrame {
	private static MainWindow instance = null;
	private static final long serialVersionUID = -74349571204769732L;
	private Container content;
	private Ribbon ribbon;
	private ViewObservable viewObservable;
	private ViewPanel viewPanel;

	public static MainWindow getInstance() {
		if (instance == null) {
			instance = new MainWindow();
		}
		return instance;
	}

	private MainWindow() {
		this.setIconImage(new ImageIcon(this.getClass().getClassLoader().getResource("resources/icon.png")).getImage());

		// Visual options
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

		// Window options
		setTitle("MeshIneBits");
		setSize(1280, 700);
		setResizable(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		addWindowListener(new java.awt.event.WindowAdapter() {
		    @Override
		    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
		        ProcessingView.closeProcessingView();
		        ProcessingModelView.closeProcessingView();
		    }
		});

		// Menu with the tabs
		ribbon = new Ribbon();
		
		// Preview of the generated part & controls
		viewPanel = new ViewPanel();
		
		// ViewObservable make connection between generated part and the view
		viewObservable = ViewObservable.getInstance();
		viewObservable.addObserver(viewPanel);
		viewObservable.addObserver(ribbon);

		content = getContentPane();
		content.setLayout(new BorderLayout());
		content.add(ribbon, BorderLayout.NORTH);
		content.add(viewPanel, BorderLayout.CENTER);
		content.add(new StatusBar(), BorderLayout.SOUTH);

		setVisible(true);
	}

	public void refresh() {
		repaint();
		revalidate();
	}

	public void setPart(GeneratedPart part) {
		viewObservable.setPart(part);
	}
}
