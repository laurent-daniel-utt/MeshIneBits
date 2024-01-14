package meshIneBits.SpringWebApp;

import meshIneBits.MeshIneBitsWebMain;
import meshIneBits.util.SimultaneousOperationsException;
import meshIneBits.util.Vector3;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import java.io.*;
import java.net.URL;

@RestController
@RequestMapping("/model-viewer")
public class STLController {

    private final ResourceLoader resourceLoader;

    @Autowired
    public STLController(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public static void main(String[] args) {
        SpringApplication.run(STLController.class, args);
    }

    @GetMapping()
    public ModelAndView root() {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("model-viewer.html");
        return mav;
    }

    @GetMapping("/download-stl")
    public ResponseEntity<Resource> downloadSTL(@RequestParam(value = "name", defaultValue = "Blob.stl") String fileName) throws IOException {
        // Build the path to the STL file. You can adjust the path as needed.
        String filePath = "classpath:uploads/" + fileName;

        // Load the STL file as a Resource
        Resource stlFile = resourceLoader.getResource(filePath);

        if (stlFile.exists() && stlFile.isReadable()) {
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header("Content-Disposition", "attachment; filename=\"" + fileName + "\"")
                    .body(stlFile);
        } else {
            // Handle the case where the file doesn't exist or is not readable.
            return ResponseEntity.notFound().build();
        }
    }


    @PostMapping("/upload-stl")
    public ResponseEntity<String> uploadFile(@RequestParam("stlFile") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Please select a file to upload.");
        }

        try {
            // Normalize the file name
            String fileName = file.getOriginalFilename();

            // Get the resource (directory) relative to the classpath
            Resource resource = resourceLoader.getResource("classpath:uploads/");

            // Create an OutputStream to write the file to the classpath resource
            try (InputStream inputStream = file.getInputStream();
                 OutputStream outputStream = new FileOutputStream(resource.getFile().getAbsolutePath() + "/" + fileName)) {

                int bytesRead;
                byte[] buffer = new byte[4096];
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }

            URL fileUrl = getClass().getResource("/uploads/"+fileName);
            File stlFile = new File(fileUrl.getPath());

            MeshIneBitsWebMain.projectWindow.getMeshController().newMesh(stlFile);
            return ResponseEntity.ok(fileName);
        } catch (IOException ex) {
            return ResponseEntity.status(500).body("File upload failed. Please try again.");
        } catch (SimultaneousOperationsException e) {
            throw new RuntimeException(e);
        }
    }
}