package meshIneBits.util.supportExportFile;

import com.sun.istack.internal.NotNull;
import com.sun.xml.internal.ws.handler.HandlerException;
import jdk.nashorn.internal.runtime.regexp.joni.exception.ValueException;
import meshIneBits.Bit3D;
import meshIneBits.Layer;
import meshIneBits.Mesh;
import meshIneBits.config.CraftConfig;
import meshIneBits.config.MeshTagXML;
import meshIneBits.scheduler.AScheduler;
import meshIneBits.scheduler.BasicScheduler;
import meshIneBits.util.InterfaceXmlTool;
import meshIneBits.util.Logger;
import meshIneBits.util.Vector2;
import meshIneBits.util.Vector3;
import org.w3c.dom.Element;

import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.nio.file.Path;
import java.util.*;

/**
 * This class provide list of function to support writing {@link Mesh} to XML file.
 * Use {@link MeshXMLTool#writeMeshToXML} write Mesh's  XML file
 *
 * @author QuangBao DO
 */
public class MeshXMLTool extends XMLDocument<Mesh> implements InterfaceXmlTool {
    private static MeshXMLTool XML_TOOL;
    private Mesh mMesh;
    //Parameter
    public int remainingBits = CraftConfig.nbBits;
    public final double effectiveWidth = CraftConfig.workingWidth - CraftConfig.margin;


    public MeshXMLTool(Path filePath) {
        super(filePath);
        assert filePath != null;
    }

    public void initialize(Mesh mesh) {
        this.mMesh = mesh;
        changeFilePathToXML();

    }


    @Override
    public void writeMeshToXML(@NotNull Mesh mesh) {
        initialize(mesh);
        writeObjectToXML(mesh);
    }

    @Override
    public Element buildElementResult(Mesh mesh) {
        Element meshElement = createElement(MeshTagXML.MESH_START);
        Element config = buildConfigElement(mesh);
        meshElement.appendChild(config);
        Logger.message("Generating XML file");
        for (int i = 0; i < mesh.getLayers().size(); i++) {
            Element layer = buildLayerElement(mesh.getLayers().get(i));
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
    public Element buildConfigElement(Mesh mesh) {
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

    public Element buildLayerElement(Layer layer) {
        if (mMesh == null) {
            throw new HandlerException(new NullPointerException("Mesh object hasn't be declared yet"));
        }
        // Layer element
        Element layerElement = createElement(MeshTagXML.LAYER);
        AScheduler scheduler = mMesh.getScheduler();
        if (scheduler.getFirstLayerBits().get(layer.getLayerNumber()) != null) {
            Bit3D startBit = scheduler.getFirstLayerBits().get(layer.getLayerNumber());
            int startIndex = scheduler.getBitIndex(startBit);
            List<Bit3D> listBit3DsCurrentLayer = AScheduler.getListBit3DsSortedFrom(((BasicScheduler) scheduler).filterBits(layer.sortBits()));
            int endIndex = listBit3DsCurrentLayer.size();
            List<Bit3D> listAllBit3D = AScheduler.getListBit3DsSortedFrom(scheduler.getSortedBits()).subList(startIndex, startIndex + endIndex);
            Vector3 modelTranslation = mMesh.getModel().getPos();

            //height of layer
            Element height = createElement(MeshTagXML.LAYER_HEIGHT, Double.toString((layer.getLayerNumber() * (CraftConfig.bitThickness + CraftConfig.layersOffset))));
            layerElement.appendChild(height);

            for (Bit3D bit3D : listAllBit3D) {
                // translating the bits - they are generated at the origin of the world coordinate system;
                for (int j = 0; j < bit3D.getRawLiftPoints().size(); j++) {
                    if (bit3D.getRawLiftPoints().get(j) != null) {
                        double oldX = bit3D.getLiftPoints().get(j).x;
                        double oldY = bit3D.getLiftPoints().get(j).y;
                        bit3D.getLiftPoints().set(j, new Vector2(oldX + modelTranslation.x, oldY + modelTranslation.y));
                    }
                }
                Element moveWorkingSpaceElement = buildMoveWorkingSpace(bit3D, listAllBit3D.indexOf(bit3D));
                layerElement.appendChild(moveWorkingSpaceElement);
                Element bitElement = buildBitElement(bit3D);
                layerElement.appendChild(bitElement);
                remainingBits -= 1;
            }
        } else {
            return null;
        }
        return layerElement;


    }

    private Element buildMoveWorkingSpace(Bit3D bit, int id) {
        double currentPos = 0;
        Element moveWorkingSpace = createElement(MeshTagXML.MOVE_WORKING_SPACE);
        if (remainingBits == 0) {
            moveWorkingSpace.appendChild(createElement(MeshTagXML.RETURN));
            remainingBits = CraftConfig.nbBits;
        }
        for (int i = 0; i < bit.getLiftPoints().size(); i++) {
            if (bit.getLiftPoints().get(i) != null) {
                if (id == 0) {
                    currentPos = bit.getLiftPoints().get(i).x + effectiveWidth / 2;
                    Element goTo = createElement(MeshTagXML.GO_TO);
                    Element x = createElement(MeshTagXML.CORDINATE_X, Double.toString(currentPos));
                    goTo.appendChild(x);
                    moveWorkingSpace.appendChild(goTo);
                } else {
                    if (Math.abs(bit.getLiftPoints().get(i).x - currentPos) > effectiveWidth / 2) {
                        currentPos += effectiveWidth;
                        Element goTo = createElement(MeshTagXML.GO_TO);
                        Element x = createElement(MeshTagXML.CORDINATE_X, Double.toString(currentPos));
                        goTo.appendChild(x);
                        moveWorkingSpace.appendChild(goTo);
                    }
                }
            }
        }
        return moveWorkingSpace;
    }

    public Element buildBitElement(Bit3D bit3D) {
        if (mMesh == null) {
            throw new HandlerException(new NullPointerException("Mesh object hasn't be declared yet"));
        }
        Element bit = createElement(MeshTagXML.BIT);
        //bit's ID element
        Element bitId = createElement(MeshTagXML.BIT_ID, Integer.toString(mMesh.getScheduler().getBitIndex(bit3D)));
        bit.appendChild(bitId);
        //Cut bit element
        Element cut = createElement(MeshTagXML.CUT_BIT);
//        if (bit3D.getRawCutPathsSeparate() != null && bit3D.getRawCutPathsSeparate().size() > 0) {
//            for (Vector<Path2D> cutPaths : bit3D.getRawCutPathsSeparate()) {
//                //path cut element
//                Element cutPathsElement = writeCutPathElement(cutPaths);
//                cut.appendChild(cutPathsElement);
//            }
//        }
        Bit3D bit3DToExport = bit3D.getNewBitToExportToXML();
//        Bit3D bit3DToExport = bit3D;
        for (Path2D cutPath : bit3DToExport.getRawCutPaths()) {
            Element cutPathElement = writeCutPathElement(cutPath);
            cut.appendChild(cutPathElement);
        }
        bit.appendChild(cut);

        //sub bit of bit
        writeSubBitElementToBit(bit, bit3DToExport);
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
        Vector<Vector2> listTwoPoints = bit3D.getListTwoDistantPoints();
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

            //Two distant point of SubBit
            if (listTwoPoints.size() >=2*i) {
                for (int j = 0; j < 2; j++) {
                    Vector2 point = listTwoPoints.get(2*i+j);
                    point=point.getTransformed(bit3D.getBaseBit().getInverseTransfoMatrix());
                    Element pointElement = createElement(MeshTagXML.POINT);
                    Element pointIdElement = createElement(MeshTagXML.POINT_ID, Integer.toString(j));
                    pointElement.appendChild(pointIdElement);
                    Element pointXELement = createElement(MeshTagXML.CORDINATE_X, Double.toString(point.x));
                    Element pointYELement = createElement(MeshTagXML.CORDINATE_Y, Double.toString(point.y));
                    pointElement.appendChild(pointXELement);
                    pointElement.appendChild(pointYELement);
                    subBit.appendChild(pointElement);
                }
                double angle=calculateAngleOfTwoPoint(bit3D.getListTwoDistantPoints().get(2*i+0),bit3D.getListTwoDistantPoints().get(2*i+1));
                Element rotation2 = createElement(MeshTagXML.ROTATION_SUB_BIT_SECOND,Double.toString(angle));
                subBit.appendChild(rotation2);
            }

            //TODO
            //Add subBit element into bit element
            bit.appendChild(subBit);
        }

    }

    public Element writeCutPathElement(Path2D cutPath) {
//    public Element writeCutPathElement(Vector<Path2D> cutPaths) {
        Element cutPathsElement = createElement(MeshTagXML.CUT_PATHS);
//        if (cutPaths.size() == 0) {
//            throw new IllegalArgumentException("cutPaths can't not be null");
//        }
//        for (Path2D path : cutPaths) {
        int countMoveTo = 0;
        for (PathIterator pi = cutPath.getPathIterator(null); !pi.isDone(); pi.next()) {
            double[] coords = new double[2];
            int type = pi.currentSegment(coords);
            Element parentTag;
            Element x = createElement(MeshTagXML.CORDINATE_X, Double.toString(coords[0]));
            Element y = createElement(MeshTagXML.CORDINATE_Y, Double.toString(coords[1]));
            switch (type) {
                case PathIterator.SEG_MOVETO:
                    countMoveTo++;
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
            if (parentTag.getTagName().equals(MeshTagXML.MOVE_TO_POSITION) && countMoveTo > 1) {
                cutPathsElement.appendChild(createElement(MeshTagXML.FALL_TYPE, MeshTagXML.CHUTE_TYPE));
            }
            cutPathsElement.appendChild(parentTag);
        }
        cutPathsElement.appendChild(createElement(MeshTagXML.FALL_TYPE, MeshTagXML.SUB_BIT));

//        }
        return cutPathsElement;
    }

    /**
     * Only use to calculate angle between 2 point for file exported
     * @param point1
     * @param point2
     * @return
     */
    private static double calculateAngleOfTwoPoint(Vector2 point1,Vector2 point2){
        Vector2 vectorResult = new Vector2(point2.x-point1.x,point2.y-point1.y);
        return Vector2.calcAngleBetweenVectorAndAxeX(vectorResult);
    }


}
