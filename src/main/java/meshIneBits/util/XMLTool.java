package meshIneBits.util;

import com.sun.istack.internal.NotNull;
import com.sun.xml.internal.ws.handler.HandlerException;
import javafx.util.Pair;
import jdk.nashorn.internal.runtime.regexp.joni.exception.ValueException;
import meshIneBits.Bit3D;
import meshIneBits.Layer;
import meshIneBits.Mesh;
import meshIneBits.config.CraftConfig;
import meshIneBits.scheduler.AScheduler;
import meshIneBits.scheduler.BasicScheduler;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * This class provide list of function to support writing {@link Mesh} to XML file.
 * Use {@link XMLTool#getInstance()} to get the unique instance of class.
 * Use {@link XMLTool#writeMeshToXML} write Mesh's  XML file
 *
 * @author QuangBao DO
 */
public class XMLTool implements InterfaceXmlTool {
    private static XMLTool XML_TOOL;
    private Path mFilePath;
    private Mesh mMesh;
    private Document mDocument;

    public static XMLTool getInstance() {
        if (XML_TOOL == null) {
            XML_TOOL = new XMLTool();
        }
        return XML_TOOL;
    }

    private XMLTool() {

    }

    public void initialize(Mesh mesh, Path filePath) throws Exception {
        try {
            mDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            this.mFilePath = filePath;
            this.mMesh = mesh;
            setFilePathToXml();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
            throw new Exception("Can't initialize");
        }
    }

    @Override
    public void writeMeshToXML(@NotNull Mesh mesh, @NotNull Path filePath) {
        try {
            assert filePath != null;
            if (mesh == null) {
                throw new HandlerException(new NullPointerException("Mesh can't be null"));
            }
            initialize(mesh, filePath);
            Element meshElement = writeMesh(mesh);
            mDocument.appendChild(meshElement);
            writeNodeToXML(mDocument, filePath);
            Logger.message("The XML file has been generated and saved in " + filePath);
        } catch (Exception e) {
            Logger.error("The XML file has not been generated, Message: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void writeNodeToXML(Node node, Path filePath) {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        try {
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource domSource = new DOMSource(node);
            StreamResult streamResult = new StreamResult(new File(filePath.toString()));

            transformer.transform(domSource, streamResult);
        } catch (TransformerException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Element writeMesh(Mesh mesh) {
        Element meshElement = createElement(MeshTagXML.MESH_START);
        Element config = writeConfig(mesh);
        meshElement.appendChild(config);
        Logger.message("Generating XML file");
        for (int i = 0; i < mesh.getLayers().size(); i++) {
            Element layer = writeLayer(mesh.getLayers().get(i));
            Logger.setProgress(i, mesh.getLayers().size() - 1);
            if (layer != null) meshElement.appendChild(layer);
        }
        return meshElement;
    }

    /**
     * Write the config of mesh file
     *
     * @param mesh object to write to xml file
     * @return return element XML that contain values config of {@link Mesh}
     */
    @Override
    public Element writeConfig(Mesh mesh) {
        //Config element
        Element config = createElement(MeshTagXML.MESH_CONFIG);
        //file's name element
        Element name = createElement(MeshTagXML.MESH_NAME, getNameFromFileLocation());
        config.appendChild(name);
        //date element
        Element date = createElement(MeshTagXML.DATE, new Date().toString());
        config.appendChild(date);

        //bit's dimension element
        Element dimension = createElement(MeshTagXML.BIT_DIMENSION);
        //height element
        Element height = createElement(MeshTagXML.BIT_HEIGHT, Double.toString(CraftConfig.bitThickness));
        dimension.appendChild(height);
        //width element
        Element width = createElement(MeshTagXML.BIT_WIDTH, Double.toString(CraftConfig.bitWidth));
        dimension.appendChild(width);
        //length element
        Element length = createElement(MeshTagXML.BIT_LENGTH, Double.toString(CraftConfig.bitLength));
        dimension.appendChild(length);
        config.appendChild(dimension);

        //part skirt element
        Element partSkirt = createElement(MeshTagXML.PART_SKIRT);
        Element heightSkirt = createElement(MeshTagXML.PART_SKIRT_HEIGHT, Double.toString(((mesh.getLayers().size() + CraftConfig.layersOffset) * CraftConfig.bitThickness) - CraftConfig.layersOffset));
        partSkirt.appendChild(heightSkirt);
        Element radius = createElement(MeshTagXML.PART_SKIRT_RADIUS, Double.toString(mesh.getSkirtRadius()));
        partSkirt.appendChild(radius);
        config.appendChild(partSkirt);
        return config;
    }

    @Override
    public Element writeLayer(Layer layer) {
        if (mMesh == null) {
            throw new HandlerException(new NullPointerException("Mesh object hasn't be declared yet"));
        }
        // Layer element
        Element layerElement = createElement(MeshTagXML.LAYER);
        AScheduler scheduler = mMesh.getScheduler();
        if (scheduler.getFirstLayerBits().get(layer.getLayerNumber()) != null) {
            Bit3D startBit = scheduler.getFirstLayerBits().get(layer.getLayerNumber());
            int startIndex = scheduler.getBitIndex(startBit);
            int endIndex = ((BasicScheduler) scheduler).filterBits(layer.sortBits()).size();
            List<Pair<Bit3D, Vector2>> bits3DKeys = scheduler.getSortedBits().subList(startIndex, startIndex + endIndex);
            Vector3 modelTranslation = mMesh.getModel().getPos();

            //height of layer
            Element height = createElement(MeshTagXML.LAYER_HEIGHT, Double.toString((layer.getLayerNumber() * (CraftConfig.bitThickness + CraftConfig.layersOffset))));
            layerElement.appendChild(height);

            for (Pair<Bit3D, Vector2> bits3DKey : bits3DKeys) {
                Bit3D bit = bits3DKey.getKey();
                // translating the bits - they are generated at the origin of the world coordinate system;
                for (int j = 0; j < bit.getRawLiftPoints().size(); j++) {
                    if (bit.getRawLiftPoints().get(j) != null) {
                        double oldX = bit.getLiftPoints().get(j).x;
                        double oldY = bit.getLiftPoints().get(j).y;
                        bit.getLiftPoints().set(j, new Vector2(oldX + modelTranslation.x, oldY + modelTranslation.y));
                    }
                }
                Element bitElement = writeBit(bit);
                layerElement.appendChild(bitElement);
            }
        } else {
            return null;
        }
        return layerElement;

    }

    private void moveWorkingSpace(Bit3D bit, int id) {
//        if (remainingBits == 0) {
//            writer.println("		<return>");
//            writer.println("		</return>");
//            remainingBits = nbBits;
//        }
//        for (int i = 0; i < bit.getLiftPoints().size(); i++) {
//            if (bit.getLiftPoints().get(i) != null) {
//                if (id == 0) {
//                    writer.println("		<goTo>");
//                    currentPos = bit.getLiftPoints().get(i).x + effectiveWidth / 2;
//                    writer.println("			<x>" + currentPos + "</x>");
//                    writer.println("		</goTo>");
//                } else {
//                    if (Math.abs(bit.getLiftPoints().get(i).x - currentPos) > effectiveWidth / 2) {
//                        currentPos += effectiveWidth;
//                        writer.println("		<goTo>");
//                        writer.println("			<x>" + currentPos + "</x>");
//                        writer.println("		</goTo>");
//                    }
//                }
//            }
//        }
    }

    @Override
    public Element writeBit(Bit3D bit3D) {
        if (mMesh == null) {
            throw new HandlerException(new NullPointerException("Mesh object hasn't be declared yet"));
        }
        Element bit = createElement(MeshTagXML.BIT);
        //bit's ID element
        Element bitId = createElement(MeshTagXML.BIT_ID, Integer.toString(mMesh.getScheduler().getBitIndex(bit3D)));
        bit.appendChild(bitId);
        //Cut bit element
        Element cut = createElement(MeshTagXML.CUT_BIT);
        if (bit3D.getRawCutPathsSeparate() != null && bit3D.getRawCutPathsSeparate().size() > 0) {
            for (Vector<Path2D> cutPaths : bit3D.getRawCutPathsSeparate()) {
                //path cut element
                Element cutPathsElement = writeCutPathElement(cutPaths);
                cut.appendChild(cutPathsElement);
            }
        }
        bit.appendChild(cut);

        //sub bit of bit
        writeSubBitElementToBit(bit, bit3D);
        return bit;
    }

    /**
     * write list <subbit> into element <bit>
     *
     * @param bit   element bit that will add elements subbits
     * @param bit3D object Bit3D {@link Bit3D}
     */
    private void writeSubBitElementToBit(Element bit, Bit3D bit3D) {
        if (mMesh == null) {
            throw new HandlerException(new NullPointerException("Mesh object hasn't be declared yet"));
        }
        for (int i = 0; i < bit3D.getRawLiftPoints().size(); i++) {
            //Subit element i
            Element subBit = createElement(MeshTagXML.SUB_BIT);

            //subBit's id
            Element id = createElement(MeshTagXML.SUB_BIT_ID, Integer.toString(i));
            subBit.appendChild(id);

            //subBit's batch
            Element batch = createElement(MeshTagXML.BATCH, Integer.toString(mMesh.getScheduler().getBitBatch(bit3D)));
            subBit.appendChild(batch);

            //subBit's plate
            Element plate = createElement(MeshTagXML.PLATE, Integer.toString(mMesh.getScheduler().getBitPlate(bit3D)));
            subBit.appendChild(plate);

            //subBit's lift point
            Element liftPoint = createElement(MeshTagXML.POSITION_BIT_COORDINATE);
            //LiftPoint's position in Bit coordinate system
            Element xInBit = createElement(MeshTagXML.CORDINATE_X, Double.toString(bit3D.getRawLiftPoints().get(i).x));
            Element yInBit = createElement(MeshTagXML.CORDINATE_Y, Double.toString(bit3D.getRawLiftPoints().get(i).y));
            liftPoint.appendChild(xInBit);
            liftPoint.appendChild(yInBit);
            subBit.appendChild(liftPoint);

            //Rotation of SubBit
            Element rotationLiftPoint = createElement(MeshTagXML.ROTATION_SUB_BIT, Double.toString(bit3D.getOrientation().getEquivalentAngle()));
            subBit.appendChild(rotationLiftPoint);

            //LiftPoint's position in Mesh coordinate system
            Element positionSubBit = createElement(MeshTagXML.POSITION_MESH_COORDINATE);
            Element xInMesh = createElement(MeshTagXML.CORDINATE_X, Double.toString(bit3D.getLiftPoints().get(i).x));
            Element yInMesh = createElement(MeshTagXML.CORDINATE_Y, Double.toString(bit3D.getLiftPoints().get(i).y));
            positionSubBit.appendChild(xInMesh);
            positionSubBit.appendChild(yInMesh);
            subBit.appendChild(positionSubBit);

            //TODO
            //Add subBit element into bit element
            bit.appendChild(subBit);
        }

    }

    public Element writeCutPathElement(Vector<Path2D> cutPaths) {
        Element cutPathsElement = mDocument.createElement(MeshTagXML.CUT_PATHS);
        if (cutPaths.size() == 0) {
            throw new IllegalArgumentException("cutPaths can't not be null");
        }
        for (Path2D path : cutPaths) {
            for (PathIterator pi = path.getPathIterator(null); !pi.isDone(); pi.next()) {
                double[] coords = new double[2];
                int type = pi.currentSegment(coords);
                Element parentTag;
                Element x = createElement(MeshTagXML.CORDINATE_X, Double.toString(coords[0]));
                Element y = createElement(MeshTagXML.CORDINATE_Y, Double.toString(coords[1]));
                switch (type) {
                    case PathIterator.SEG_MOVETO:
                        parentTag = createElement(MeshTagXML.MOVE_TO_POSITION);
                        break;
                    case PathIterator.SEG_LINETO:
                        parentTag = createElement(MeshTagXML.CUT_TO_POSITION);
                        break;
                    default:
                        throw new ValueException("Type of point isn't defined: " + type);

                }
                parentTag.appendChild(x);
                parentTag.appendChild(y);
                cutPathsElement.appendChild(parentTag);
            }
        }
        return cutPathsElement;
    }

    private String getNameFromFileLocation() {
        return mFilePath.getFileName().toString().split("[.]")[0];
    }

    private Element createElement(String tag, String value) {
        Element element = mDocument.createElement(tag);
        element.appendChild(mDocument.createTextNode(value));
        return element;
    }

    private Element createElement(String tag) {
        return mDocument.createElement(tag);
    }

    private void setFilePathToXml() {
        String fileName = mFilePath.getFileName().toString();
        if (fileName.split("[.]").length >= 2) {
            fileName = fileName.split("[.]")[0];
        }
        fileName = fileName + "." + "xml";
        mFilePath = Paths.get(mFilePath.getParent().toString() + File.separator + fileName);
    }
}
