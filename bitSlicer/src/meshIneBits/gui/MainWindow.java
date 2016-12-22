package meshIneBits.gui;

import java.awt.Color;
import java.awt.Container;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.plaf.ColorUIResource;

import meshIneBits.GeneratedPart;

public class MainWindow extends JFrame {
	
	private static MainWindow instance = null;
	ShowedView sv;
	private PreviewFrame pf;
	Ribbon ribbon;
	PreviewPanel pp;

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

		sv = new ShowedView();
		
		pp = new PreviewPanel();
		pf = new PreviewFrame(pp);
		ribbon = new Ribbon();
		
		sv.addObserver(pf);
		sv.addObserver(ribbon);
		sv.addObserver(pp);
		
		sv.letObserversKnowMe();

		Container content = getContentPane();
		content.setLayout(new BoxLayout(content, BoxLayout.PAGE_AXIS));
		content.add(ribbon);
		content.add(pf);

		setVisible(true);
	}

	public void closePart() {
		sv.setPart(null);
//		pf.revalidate();
//		pf.repaint();
//		pf.bg.setVisible(true);
	}

	public void setPart(GeneratedPart part) {
		
		sv.setPart(part);
//		pf.init(pp);
//		pf.revalidate();
//		pf.repaint();
	}
}
