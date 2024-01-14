package meshIneBits;

import meshIneBits.config.CraftConfig;
import meshIneBits.config.CraftConfigLoader;
import meshIneBits.gui.utilities.CustomFileChooser;
import meshIneBits.gui.view2d.ProjectWindow;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;

@SpringBootApplication
@Controller
public class MeshIneBitsWebMain {

	// MeshIneBits ProjectWindow
	public static ProjectWindow projectWindow;

	public static void main(String[] args) {
		System.setProperty("java.awt.headless", "false");
		SpringApplication.run(MeshIneBitsWebMain.class, args);

		CraftConfigLoader.loadConfig(null);

		projectWindow = new ProjectWindow();
	}

	private static void openFile() {
		Project project = new Project();

		Project Project = new Project();
		final JFileChooser fc = new CustomFileChooser();
		String meshExt = CraftConfigLoader.MESH_EXTENSION;
		fc.addChoosableFileFilter(new FileNameExtensionFilter(meshExt + " files", meshExt));
		String dir;
		if (CraftConfig.lastMesh == null || CraftConfig.lastMesh.equals("")) {
			dir = System.getProperty("user.home");
		} else {
			dir = CraftConfig.lastMesh.replace("\n", "\\n");
		}
		fc.setSelectedFile(new File(dir));
		File f = fc.getSelectedFile();
		try {
			//ProjectController.MeshOpener meshOpener = new ProjectController.MeshOpener(file);
			//meshOpener.addObserver(this);
			//serviceExecutor.execute(meshOpener);
		} catch (Exception e) {}
	}

	@GetMapping ("/")
	public String index() {
		return "index";
	}
}

