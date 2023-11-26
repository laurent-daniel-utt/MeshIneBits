package meshIneBits.webUI;


import org.apache.arrow.flatbuf.Null;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;

public class WebClient {

    public static void main(String[] args) throws IOException, InterruptedException {
        URLConnection urlconnection = null;
        try {
            File file = new File("server/src/main/resources/stlModel/Blob.stl");
            URL url = new URL("http://localhost:8080/upload-stl");
            urlconnection = url.openConnection();
            urlconnection.setDoOutput(true);
            urlconnection.setDoInput(true);

            if (urlconnection instanceof HttpURLConnection) {
                ((HttpURLConnection) urlconnection).setRequestMethod("POST");
                ((HttpURLConnection) urlconnection).setRequestProperty("Content-type", "text/plain");
                ((HttpURLConnection) urlconnection).connect();
            }

            BufferedOutputStream bos = new BufferedOutputStream(urlconnection.getOutputStream());
            BufferedInputStream bis = new BufferedInputStream(Files.newInputStream(file.toPath()));
            int i;
            // read byte by byte until end of stream
            while ((i = bis.read()) > 0) {
                bos.write(i);
            }
            bis.close();
            bos.close();
            System.out.println(((HttpURLConnection) urlconnection).getResponseMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {

            InputStream inputStream;

            int responseCode = ((HttpURLConnection) urlconnection).getResponseCode();

            if ((responseCode >= 200) && (responseCode <= 202)) {
                inputStream = ((HttpURLConnection) urlconnection).getInputStream();
                int j;
                while ((j = inputStream.read()) > 0) {
                    System.out.println(j);
                }

            } else {
                inputStream = ((HttpURLConnection) urlconnection).getErrorStream();
            }
            ((HttpURLConnection) urlconnection).disconnect();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
