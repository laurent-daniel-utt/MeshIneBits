package meshIneBits.webUI;

import java.awt.Desktop;
import java.io.Console;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class WebLauncher {
    public static void launchBrowser() {
        // Determine the URL of your web app
        String webAppUrl = "http://localhost:8080/model-viewer";

        // Open the web app URL in the user's default browser
        Desktop desktop = Desktop.getDesktop();
        try {
            desktop.browse(new URI(webAppUrl));
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
