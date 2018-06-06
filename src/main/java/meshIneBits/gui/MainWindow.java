package meshIneBits.gui;

import meshIneBits.gui.view2d.Window;
import meshIneBits.gui.view3d.ProcessingView;

import javax.swing.*;
import javax.swing.plaf.ColorUIResource;
import java.awt.*;

/**
 * Main window should only contain menu bar, toolbar, log and status bar. Every
 * graphic shall be executed in {@link SubWindow}, which can be toggled by menu
 * bar.
 *
 */
public class MainWindow extends JFrame {
	private static MainWindow instance = null;
	private static final long serialVersionUID = -74349571204769732L;
	private Container content;
	private Toolbar toolbar;
	private SubWindow view2DWindow;
	private SubWindow view3DWindow;

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
		setSize(1280, 500);
		setResizable(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// Menu with the tabs
		toolbar = new Toolbar();

		// Preview of the generated part & controls
		view2DWindow = new Window();
		view3DWindow = new ProcessingView();

		content = getContentPane();
		content.setLayout(new BorderLayout());
		content.add(toolbar, BorderLayout.NORTH);
		// TODO
		// A text log here
		content.add(new StatusBar(), BorderLayout.SOUTH);

		// Show the frames
		setVisible(true);
	}

	SubWindow get2DView() {
		return view2DWindow;
	}
	
	SubWindow get3DView() {
		return view3DWindow;
	}
}
