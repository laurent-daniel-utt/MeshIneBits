package meshIneBits.webUI;

import java.io.IOException;

public class jarLoader {
    public static void main(String[] args) {
        try {
            // Specify the JAR file you want to execute
            String jarFilePath = "server/build/libs/server.jar";

            // Use ProcessBuilder to run the JAR file
            ProcessBuilder processBuilder = new ProcessBuilder("java", "-jar", jarFilePath);

            // Start the process
            Process process = processBuilder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}