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

public class MainWindow extends JFrame {
	private static final long serialVersionUID = -74349571204769732L;
	private static MainWindow instance = null;
	private ViewObservable viewObservable;
	private ViewPanel viewPanel;
	private Ribbon ribbon;
	private View view;
	private Container content;

	public static MainWindow getInstance() {
		if (instance == null) {
			instance = new MainWindow();
		}
		return instance;
	}

	private MainWindow() {
		this.setIconImage(new ImageIcon(this.getClass().getClassLoader().getResource("resources/icon.png")).getImage());

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
		setSize(1280, 700);
		setResizable(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		viewObservable = new ViewObservable();
		
		view = new View();
		viewPanel = new ViewPanel(view);
		ribbon = new Ribbon();
		
		viewObservable.addObserver(viewPanel);
		viewObservable.addObserver(ribbon);
		viewObservable.addObserver(view);
		
		viewObservable.letObserversKnowMe();

		content = getContentPane();
		content.setLayout(new BorderLayout());
		content.add(ribbon, BorderLayout.NORTH);
		content.add(viewPanel, BorderLayout.CENTER);
		content.add(new StatusBar(), BorderLayout.SOUTH);

		setVisible(true);
	}

	public void setPart(GeneratedPart part) {
		viewObservable.setPart(part);
	}
	
	public void refresh() {
		repaint();
		revalidate();
	}
}
