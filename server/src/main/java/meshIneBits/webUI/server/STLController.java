package meshIneBits.webUI.server;

import org.springframework.boot.SpringApplication;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import java.io.IOException;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.http.HttpStatus;

@RestController
public class STLController {

    private final ResourceLoader resourceLoader;

    @Autowired
    public STLController(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public static void main(String[] args) {
        SpringApplication.run(STLController.class, args);
    }

    @GetMapping("/model-viewer")
    public ModelAndView hello(@RequestParam(value = "name", defaultValue = "Blob.stl") String name) {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("model-viewer.html");
        return mav;
    }

    @GetMapping("/download-stl")
    @ResponseBody
    public ResponseEntity<Resource> downloadSTL(@RequestParam(value = "name", defaultValue = "Blob.stl") String fileName) throws IOException {
        // Build the path to the STL file. You can adjust the path as needed.
        String filePath = "classpath:stlModel/" + fileName;

        // Load the STL file as a Resource
        Resource stlFile = resourceLoader.getResource(filePath);
        System.out.println(stlFile.exists());
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

    @PostMapping("/post-pos")
    public ResponseEntity<String> createPost(
            @RequestParam("x") Float x,
            @RequestParam("y") Float y,
            @RequestParam("z") Float z,
            @RequestParam("rot") Float rot
    ) {
        System.out.println("x: " + x + " y: " + y + " z: " + z);

        return new ResponseEntity<>("\uD83D\uDC4D", HttpStatus.CREATED);
    }
}