package meshIneBits.webUI;

import java.io.IOException;

public class jarLoader {
    public static void main(String[] args) {
        try {
            // Replace with the actual path to your JAR file
            String jarPath = "server/build/libs/server.jar";

            // Command to run the JAR file in a visible terminal
            String cmdCommand = "cmd /c start cmd /k java -jar " + jarPath;

            // Start the command in a new terminal window
            Process process = Runtime.getRuntime().exec(cmdCommand);

            // Wait for the process to exit
            int exitCode = process.waitFor();
            System.out.println("Terminal process exited with code " + exitCode);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}