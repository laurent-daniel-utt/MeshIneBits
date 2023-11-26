package meshIneBits.webUI;

import java.awt.Desktop;
import java.io.Console;
import java.net.URI;

public class WebLauncher {
    public static void main() throws Exception {
        jarLoader.main(new String[0]);
        // Determine the URL of your web app
        String webAppUrl = "http://localhost:8080/";

        // Open the web app URL in the user's default browser
        Desktop desktop = Desktop.getDesktop();
        desktop.browse(new URI(webAppUrl));

        WebClient.main(new String[0]);
    }

    public static void start() {
        try {
            main();

        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
}
