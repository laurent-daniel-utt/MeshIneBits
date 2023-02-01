/*
 * MeshIneBits is a Java software to disintegrate a 3d mesh (model in .stl)
 * into a network of standard parts (called "Bits").
 *
 * Copyright (C) 2016-2021 DANIEL Laurent.
 * Copyright (C) 2016  CASSARD Thibault & GOUJU Nicolas.
 * Copyright (C) 2017-2018  TRAN Quoc Nhat Han.
 * Copyright (C) 2018 VALLON Benjamin.
 * Copyright (C) 2018 LORIMER Campbell.
 * Copyright (C) 2018 D'AUTUME Christian.
 * Copyright (C) 2019 DURINGER Nathan (Tests).
 * Copyright (C) 2020 CLARIS Etienne & RUSSO André.
 * Copyright (C) 2020-2021 DO Quang Bao.
 * Copyright (C) 2021 VANNIYASINGAM Mithulan.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package meshIneBits.util.supportExportFile;

import jdk.nashorn.internal.runtime.regexp.joni.exception.ValueException;
import meshIneBits.Bit3D;
import meshIneBits.Mesh;
import meshIneBits.NewBit2D;
import meshIneBits.SubBit2D;
import meshIneBits.config.CraftConfig;
import meshIneBits.config.MeshTagXML;
import meshIneBits.scheduler.AScheduler;
import meshIneBits.util.*;
import org.w3c.dom.Element;

import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Vector;

/**
 * This class provide list of function to support writing {@link Mesh} to XML file. Use {@link
 * MeshXMLTool#writeMeshToXML} write Mesh's  XML file
 *
 * @author QuangBao DO
 */
public class MeshXMLTool extends XMLDocument<Mesh> implements InterfaceXmlTool {

  private static MeshXMLTool XML_TOOL;
  private Mesh mMesh;
  private Bit3D currentBit;

  //Parameter
  public int remainingBits = CraftConfig.nbBits;
  public final double effectiveWidth = CraftConfig.workingWidth - CraftConfig.margin;
  public int subBitId = 1;
  public int slotPosition = 1;
  public double workingPlacePosition = 0;

  public MeshXMLTool(Path filePath) {
    super(filePath);
    assert filePath != null;
  }

  public void initialize(Mesh mesh) {
    this.mMesh = mesh;
    //changeFilePathToXML();

  }


  @Override
  public void writeMeshToXML(Mesh mesh) {
    initialize(mesh);
    writeObjectToXML(mesh);
  }

  @Override
  protected Element buildElementResult(Mesh mesh, int batchNumber) {
    Element meshElement = null;
    Logger.message("Generating XML file");

    AScheduler scheduler = mMesh.getScheduler();
    List<Bit3D> listAllBit3D = AScheduler.getSetBit3DsSortedFrom(scheduler.getSortedBits());
    int nbSubBit = getCountSubBitElement(listAllBit3D);
    //Get the bit of the batch and put them on the ArrayList below.
    ArrayList<Bit3D> listBitByBatch = new ArrayList<Bit3D>();
    for (int j = 0; j < CraftConfig.nbBitesBatch; j++) {
      if (batchNumber * CraftConfig.nbBitesBatch + j < listAllBit3D.size()) {
        listBitByBatch.add(listAllBit3D.get(batchNumber * CraftConfig.nbBitesBatch + j));
      }
    }

    meshElement = createElement(MeshTagXML.MESH_START);
    Element config = buildConfigElement(mesh, batchNumber);
    meshElement.appendChild(config);
    meshElement.appendChild(buildBatchElement(listBitByBatch));

    return meshElement;
  }

  /**
   * Write the config of mesh file
   *
   * @param mesh object to write to xml file
   * @return return element XML that contain values config of {@link Mesh}
   */
  public Element buildConfigElement(Mesh mesh, int batch) {
    //Config element
    Element config = createElement(MeshTagXML.MESH_CONFIG);
    //file's name element
    Element name = createElement(MeshTagXML.MESH_NAME,
        getNameFromFileLocation() + " Batch " + batch);
    config.appendChild(name);
    //date element
    Element date = createElement(MeshTagXML.DATE, new Date().toString());
    config.appendChild(date);

    //bit's dimension element
    Element dimension = createElement(MeshTagXML.BIT_DIMENSION);
    //height element
    Element height = createElement(MeshTagXML.BIT_HEIGHT,
        Double.toString(CraftConfig.bitThickness));
    dimension.appendChild(height);
    //width element
    Element width = createElement(MeshTagXML.BIT_WIDTH, Double.toString(CraftConfig.bitWidth));
    dimension.appendChild(width);
    //length element
    Element length = createElement(MeshTagXML.BIT_LENGTH, Double.toString(CraftConfig.lengthFull));
    dimension.appendChild(length);
    config.appendChild(dimension);

    //part skirt element
    Element partSkirt = createElement(MeshTagXML.PART_SKIRT);
    Element heightSkirt = createElement(MeshTagXML.PART_SKIRT_HEIGHT, Double.toString(
        ((mesh.getLayers()
            .size() + CraftConfig.layersOffset) * CraftConfig.bitThickness)
            - CraftConfig.layersOffset));
    partSkirt.appendChild(heightSkirt);
    Element radius = createElement(MeshTagXML.PART_SKIRT_RADIUS,
        Double.toString(mesh.getSkirtRadius()));
    partSkirt.appendChild(radius);
    config.appendChild(partSkirt);
    return config;
  }

  //create the Batch XML
  public Element buildBatchElement(ArrayList<Bit3D> listBitByBatch) {
    if (mMesh == null) {
      throw new NullPointerException("Mesh object hasn't be declared yet");
    }

    Element batchElement = createElement(MeshTagXML.BATCH);

    //batch Number
    Element batchNumber = createElement(MeshTagXML.BATCH_NUMBER,
        Integer.toString(mMesh.getScheduler()
            .getBitBatch(listBitByBatch.get(0))));
    batchElement.appendChild(batchNumber);

    //Count Bits
    Element numberOfBits = createElement(MeshTagXML.NUMBER_OF_BITS,
        Integer.toString(listBitByBatch.size()));
    batchElement.appendChild(numberOfBits);

    //contain all bit of same layer.
    ArrayList<Bit3D> listBitByLayer = new ArrayList<Bit3D>();
    //value which help to get the bit of same layer by comparing.
    double bitAltitude = listBitByBatch.get(0)
        .getLowerAltitude();

    //try to put all bit of the same layer in an ArrayList to then apply buildLayerElement.
    for (Bit3D bit : listBitByBatch) {
      if (bit.getLowerAltitude() == bitAltitude) {
        listBitByLayer.add(bit);
      }
      //means that we got all by of same layer
      else {
        batchElement.appendChild(buildLayerElement(listBitByLayer));
        //init to 0 the array and value
        listBitByLayer = new ArrayList<Bit3D>();
        bitAltitude = bit.getLowerAltitude();

        listBitByLayer.add(bit);
      }
    }
    batchElement.appendChild(buildLayerElement(listBitByLayer));
    return batchElement;
  }

  //create the Layer XML
  public Element buildLayerElement(ArrayList<Bit3D> listBitLayer) {
    Element layerElement = createElement(MeshTagXML.LAYER);
    AScheduler scheduler = mMesh.getScheduler();
    //height of layer
    Element height = createElement(MeshTagXML.LAYER_HEIGHT,
        Double.toString(listBitLayer.get(0)
            .getLowerAltitude()));
    layerElement.appendChild(height);

    List<Bit3D> listAllBit3D = AScheduler.getSetBit3DsSortedFrom(scheduler.getSortedBits());
    Vector3 modelTranslation = mMesh.getModel()
        .getPos();

    for (Bit3D bit3D : listBitLayer) {
      // translating the bits - they are generated at the origin of the world coordinate system;
      for (int j = 0; j < bit3D.getLiftPointsCB()
          .size(); j++) {
        if (bit3D.getLiftPointsCB()
            .get(j) != null) {
          double oldX = bit3D.getLiftPointsCS()
              .get(j).x;
          double oldY = bit3D.getLiftPointsCS()
              .get(j).y;
          bit3D.getLiftPointsCS()
              .set(j, new Vector2(oldX + modelTranslation.x, oldY + modelTranslation.y));
        }
      }
      Element moveWorkingSpaceElement = buildMoveWorkingSpace(bit3D, listAllBit3D.indexOf(bit3D));
      layerElement.appendChild(moveWorkingSpaceElement);
      Element bitElement = buildBitElement(bit3D);
      layerElement.appendChild(bitElement);
      remainingBits -= 1;
    }
    return layerElement;
  }

  //create the move-working-space XML
  private Element buildMoveWorkingSpace(Bit3D bit, int id) {

    Element moveWorkingSpace = createElement(MeshTagXML.MOVE_WORKING_SPACE);
    if (remainingBits == 0) {
      moveWorkingSpace.appendChild(createElement(MeshTagXML.RETURN));
      remainingBits = CraftConfig.nbBits;
    }
    for (int i = 0; i < bit.getLiftPointsCS()
        .size(); i++) {
      if (bit.getLiftPointsCS()
          .get(i) != null) {
        //init safetySpace use
        double safetySpace = CraftConfig.bitWidth / 2;
        Vector2 bitOrientation = bit.getOrientation();
        if (bitOrientation.x != 1) {
          safetySpace = Math.abs(CraftConfig.lengthFull * bitOrientation.x / 2);
        }
        double xMinInMachineRef = bit.getMinAndMaxXDistantPoint()
            .get(0) + CraftConfig.printerX / 2
            + CraftConfig.xPrintingSpace;
        double xMaxInMachineRef = bit.getMinAndMaxXDistantPoint()
            .get(1) + CraftConfig.printerX / 2
            + CraftConfig.xPrintingSpace;

        if (id == 0) {
          ;
          workingPlacePosition = xMinInMachineRef - safetySpace;
          Element goTo = createElement(MeshTagXML.GO_TO);
          Element x = createElement(MeshTagXML.COORDINATE_X, Double.toString(workingPlacePosition));
          goTo.appendChild(x);
          moveWorkingSpace.appendChild(goTo);
        } else {
          if (xMinInMachineRef - safetySpace <= workingPlacePosition
              || xMaxInMachineRef + safetySpace >= (workingPlacePosition
              + CraftConfig.workingWidth)) {
            workingPlacePosition = xMinInMachineRef - safetySpace;
            Element goTo = createElement(MeshTagXML.GO_TO);
            Element x = createElement(MeshTagXML.COORDINATE_X,
                Double.toString(workingPlacePosition));
            goTo.appendChild(x);
            moveWorkingSpace.appendChild(goTo);
          }
        }
      }
    }
    return moveWorkingSpace;
  }

  //create the bit XML
  public Element buildBitElement(Bit3D bit3D) {
    if (mMesh == null) {
      throw new NullPointerException("Mesh object hasn't be declared yet");
    }
    Element elementBit = createElement(MeshTagXML.BIT);
    //bit's ID element
    Element bitId = createElement(MeshTagXML.BIT_ID,
        Integer.toString(mMesh.getScheduler()
            .getBitIndex(bit3D)));
    elementBit.appendChild(bitId);
    //Cut bit element
    Element cut = bit3D.getCutPathsCB()
        .size() == 0 ? createElement(MeshTagXML.NO_CUT_BIT)
        : createElement(MeshTagXML.CUT_BIT);
    rebuildBit3d(bit3D);
    prepareBitToExport(bit3D);
    for (Path2D cutPath : bit3D.getCutPathsCB()) {
      Element cutPathElement = writeCutPathElement(cutPath);
      cut.appendChild(cutPathElement);
    }
    elementBit.appendChild(cut);

    //sub bit of bit
    writeSubBitElementToBit(elementBit, bit3D);
    return elementBit;
  }

 /**removes irregular sbbits for the case when a bit is divided to multiple subbits some are regulars and some are irregulars */
  private void rebuildBit3d(Bit3D bit){

    NewBit2D newbit=(NewBit2D)bit.getBaseBit();
    Vector<SubBit2D> subs=newbit.getValidSubBits();
    Vector<Area> areas=new Vector<>();
    for (SubBit2D sub:subs){

   areas.add(sub.getAreaCB());
    }
    bit.getBaseBit().setAreas(areas);
    bit.setRawCutPaths(CutPathCalc.instance.calcCutPathFrom(bit.getBaseBit()));
  }

  /**
   * write list <subbit> into element <bit>
   *
   * @param elementBit element bit that will add elements subbits
   * @param bit3D      object Bit3D {@link Bit3D}
   */
  private void writeSubBitElementToBit(Element elementBit, Bit3D bit3D) {
    if (mMesh == null) {
      throw new NullPointerException("Mesh object hasn't be declared yet");
    }
//        Vector<Vector2> listTwoPoints = bit3D.getListTwoDistantPoints();
    Vector<Vector<Vector2>> listTwoPoints = bit3D.getListTwoDistantPoints();
    for (int i = 0; i < bit3D.getLiftPointsCB()
        .size(); i++) {
      //Subit element i
      Element subBit = createElement(MeshTagXML.SUB_BIT);

      //subBit's id
      Element id = createElement(MeshTagXML.SUB_BIT_ID, Integer.toString(subBitId));
      subBit.appendChild(id);
      subBitId += 1;
      //subBit's plate
      Element plate = createElement(MeshTagXML.PLATE,
          Integer.toString(mMesh.getScheduler()
              .getBitPlate(bit3D)));
      subBit.appendChild(plate);

      //subBit's slot
      if (slotPosition > CraftConfig.nbBitesByPlat) {
        slotPosition = 1;
      }
      Element slot = createElement(MeshTagXML.SLOT, Integer.toString(slotPosition));
      subBit.appendChild(slot);
      slotPosition += 1;

      //subBit's lift point
      Element liftPoint = createElement(MeshTagXML.POSITION_BIT_COORDINATE);
      //LiftPoint's position in Bit coordinate system
      Element xInBit = createElement(MeshTagXML.COORDINATE_X,
          Double.toString(bit3D.getLiftPointsCS()
              .get(i).x));
      Element yInBit = createElement(MeshTagXML.COORDINATE_Y,
          Double.toString(bit3D.getLiftPointsCS()
              .get(i).y));
      liftPoint.appendChild(xInBit);
      liftPoint.appendChild(yInBit);
      subBit.appendChild(liftPoint);

      //Rotation of SubBit
      Element rotationLiftPoint = createElement(MeshTagXML.ROTATION_SUB_BIT,
          Double.toString(bit3D.getOrientation()
              .getEquivalentAngle()));
      subBit.appendChild(rotationLiftPoint);

      //LiftPoint's position in Mesh coordinate system
      Element positionSubBit = createElement(MeshTagXML.POSITION_MESH_COORDINATE);
      double xInPrinterRef = bit3D.getLiftPointsCS()
          .get(i).x;
      double yInPrinterRef = bit3D.getLiftPointsCS()
          .get(i).y;
      double xInSubXRef = xInPrinterRef + CraftConfig.printerX / 2 + CraftConfig.xPrintingSpace
          - workingPlacePosition;
      double yInMachineRef = yInPrinterRef + CraftConfig.printerY / 2 + CraftConfig.yEmptySpace;

      Element xInMesh = createElement(MeshTagXML.COORDINATE_X, Double.toString(xInSubXRef));
      Element yInMesh = createElement(MeshTagXML.COORDINATE_Y, Double.toString(yInMachineRef));
      positionSubBit.appendChild(xInMesh);
      positionSubBit.appendChild(yInMesh);
      subBit.appendChild(positionSubBit);
//Two distant point of SubBit
      if (listTwoPoints.get(i)
          .size() >= 2) {
        for (int j = 0; j < 2; j++) {
          Vector2 point = listTwoPoints.get(i)
              .get(j);
          Element pointElement = createElement(MeshTagXML.POINT);
          Element pointIdElement = createElement(MeshTagXML.POINT_ID, Integer.toString(j));
          pointElement.appendChild(pointIdElement);
          Element pointXELement = createElement(MeshTagXML.COORDINATE_X, Double.toString(point.x));
          Element pointYELement = createElement(MeshTagXML.COORDINATE_Y, Double.toString(point.y));
          pointElement.appendChild(pointXELement);
          pointElement.appendChild(pointYELement);
          subBit.appendChild(pointElement);
        }

      }
      if (bit3D.getListAngles()
          .get(i) != null) {
        Element rotation2 = createElement(MeshTagXML.ROTATION_SUB_BIT_SECOND,
            Double.toString(bit3D.getListAngles()
                .get(i)));
        subBit.appendChild(rotation2);
      }
      elementBit.appendChild(subBit);
    }

  }

  private int getCountSubBitElement(List<Bit3D> allBits) {
    int count = 0;
    for (Bit3D bit : allBits) {
      count += bit.getLiftPointsCB()
          .size();
    }
    return count;
  }

  public Element writeCutPathElement(Path2D cutPath) {
    Element cutPathsElement = createElement(MeshTagXML.CUT_PATHS);
    int countMoveTo = 0;
    Element currentFallType = null;
    for (PathIterator pi = cutPath.getPathIterator(null); !pi.isDone(); pi.next()) {
      double[] coords = new double[2];
      int type = pi.currentSegment(coords);
      Element parentTag;
      Element x = createElement(MeshTagXML.COORDINATE_X, Double.toString(coords[0]));
      Element y = createElement(MeshTagXML.COORDINATE_Y, Double.toString(coords[1]));
      switch (type) {
        case PathIterator.SEG_MOVETO:
          if (countMoveTo > 0) {
            appendTextNode(currentFallType, MeshTagXML.CHUTE_TYPE);
          }
          currentFallType = createElement(MeshTagXML.FALL_TYPE);
          cutPathsElement.appendChild(currentFallType);
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
      cutPathsElement.appendChild(parentTag);
    }
    if (currentBit.checkIfLastCutPath(cutPath) && currentBit.isHoldedInCUt()) {
      appendTextNode(currentFallType, MeshTagXML.CHUTE_TYPE);
      cutPathsElement.appendChild(createElement(MeshTagXML.FALL_TYPE, MeshTagXML.SUB_BIT));
      cutPathsElement.appendChild(createElement(MeshTagXML.DROP));
    } else {
      appendTextNode(currentFallType, MeshTagXML.SUB_BIT);
    }
    return cutPathsElement;
  }

  private void prepareBitToExport(Bit3D bit3D) {
    bit3D.prepareBitToExport();
    currentBit = bit3D;
  }


}
