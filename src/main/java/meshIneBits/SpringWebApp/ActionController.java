package meshIneBits.SpringWebApp;
import meshIneBits.MeshIneBitsWebMain;
import meshIneBits.util.Vector3;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/model-viewer")
public class ActionController {

    private final ResourceLoader resourceLoader;

    @Autowired
    public ActionController(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @PostMapping("/post-pos")
    public ResponseEntity<String> postPositions(
            @RequestParam("x") Float x,
            @RequestParam("y") Float y,
            @RequestParam("z") Float z,
            @RequestParam("rot") Float rot
    ) {
        System.out.println("x: " + x + " y: " + y + " z: " + z);
        MeshIneBitsWebMain.projectWindow.getMeshController().getMesh().getModel().setPos(new Vector3(x,y,z));
        return new ResponseEntity<>("Position posted.", HttpStatus.CREATED);
    }

    @PostMapping("/slice")
    public ResponseEntity<String> slice() {

        try {
            MeshIneBitsWebMain.projectWindow.getMeshController().sliceMesh();
            return ResponseEntity.ok("Mesh sliced.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Slicing failed.");
        }

    }

}