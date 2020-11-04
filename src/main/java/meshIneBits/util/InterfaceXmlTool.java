package meshIneBits.util;

import meshIneBits.Bit3D;
import meshIneBits.Layer;
import meshIneBits.Mesh;
import org.w3c.dom.Element;

import java.nio.file.Path;

public interface InterfaceXmlTool {

    void writeMeshToXML(Mesh mesh, Path filePath);
    Element writeMesh(Mesh mesh);
    Element writeConfig(Mesh mesh);
    Element writeLayer(Layer layer);
    org.w3c.dom.Element writeBit(Bit3D bit3D);


}
