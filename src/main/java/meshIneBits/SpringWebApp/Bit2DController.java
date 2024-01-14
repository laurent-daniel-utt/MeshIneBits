package meshIneBits.SpringWebApp;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import meshIneBits.Bit2D;
import meshIneBits.Layer;
import meshIneBits.MeshIneBitsWebMain;
import meshIneBits.gui.view3d.provider.ProjectProvider;
import meshIneBits.util.Vector2;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;
import java.util.Vector;

@RestController
public class Bit2DController {
    @GetMapping("/bit2d")
    @ResponseBody
    public String getBit2d() {

        Bit2D bit2d = new Bit2D(new Vector2(0, 0), new Vector2(1, 1));
        Vector<Layer> layers = ProjectProvider.getInstance().getCurrentMesh().getLayers();
        //Set<Bit2D> bits =  ProjectProvider.getInstance().getCurrentMesh().getLayers().get(0).getFlatPavement().getBits();

        StringBuilder jsonMsg = new StringBuilder();
        jsonMsg.append("[");
        int count = 0;
        for (Layer layer : layers) {
            try {
                if (count != 0) jsonMsg.append(",");
                ObjectMapper objectMapper = new ObjectMapper();
                int count2 = 0;
                jsonMsg.append("{\"z\":");
                jsonMsg.append(layer.getHigherAltitude());
                jsonMsg.append(",\"bits\":[");
                for (Bit2D bit : layer.getFlatPavement().getBits()) {
                    if (count2 != 0) jsonMsg.append(",");
                    jsonMsg.append(objectMapper.writeValueAsString(bit));
                    count2++;
                }
                jsonMsg.append("]}");
                count++;
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
        jsonMsg.append("]");

        System.out.println("Bit2D");
        try {
            return jsonMsg.toString();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            // Handle exception
            return "Error";
        }
    }

    public String getTest() {
        Vector2 test = new Vector2(1,2);
        ObjectMapper objectMapper = new ObjectMapper();
        System.out.println("Test");
        try {
            return objectMapper.writeValueAsString(test);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            // Handle exception
            return "Error";
        }
    }
}
