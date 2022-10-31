/*
 * MeshIneBits is a Java software to disintegrate a 3d mesh (model in .stl)
 * into a network of standard parts (called "Bits").
 *
 * Copyright (C) 2016-2022 DANIEL Laurent.
 * Copyright (C) 2016  CASSARD Thibault & GOUJU Nicolas.
 * Copyright (C) 2017-2018  TRAN Quoc Nhat Han.
 * Copyright (C) 2018 VALLON Benjamin.
 * Copyright (C) 2018 LORIMER Campbell.
 * Copyright (C) 2018 D'AUTUME Christian.
 * Copyright (C) 2019 DURINGER Nathan (Tests).
 * Copyright (C) 2020-2021 CLAIRIS Etienne & RUSSO André.
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
 *
 */

package meshIneBits.scheduler;

import javafx.util.Pair;
import meshIneBits.*;
import meshIneBits.config.CraftConfig;
import meshIneBits.util.Logger;
import meshIneBits.util.Vector2;

import java.util.*;

import static meshIneBits.config.CraftConfig.nbBitesBatch;

public class BasicScheduler extends AScheduler {

  private  ArrayList<ArrayList<Strip>> strips=new ArrayList<>();
  private int num=0;
  public BasicScheduler() {
  }

  public BasicScheduler(Mesh m) {
    super(m);
  }

  @Override
  public int getBitIndex(Bit3D bit) {
    if (sortedBits.isEmpty()) {
      return 0;
    }
    for (Pair<Bit3D, Vector2> pair : this.sortedBits) {
      if (pair.getKey() == bit) {
        return this.sortedBits.indexOf(pair);
      }
    }
    return 0;
  }

  @Override
  public int getBitBatch(Bit3D bit) {
    if (sortedBits.isEmpty()) {
      return 0;
    }
    return this.getBitIndex(bit) / CraftConfig.nbBitesBatch;
  }

  @Override
  public int getBitPlate(Bit3D bit) {
    if (sortedBits.isEmpty()) {
      return 0;
    }
    return this.getBitIndex(bit) / CraftConfig.nbBitesByPlat;
  }

  @Override
  public boolean order() {
    return false;
  }

  @Override
  public boolean schedule() {
    System.out.println("Basic scheduler schedule");
    Logger.setProgress(0, this.mesh.getLayers()
        .size());
    double xMin;
    this.sortedBits.clear();
    Logger.message("Size of layer " + this.mesh.getLayers()
        .size());
    int i = 0;
    for (Layer curLayer : this.mesh.getLayers()) {
      Vector<Pair<Bit3D, Vector2>> bits = curLayer.sortBits();
      bits = this.filterBits(bits);
      if (bits.size() > 0) {
        for (Pair<Bit3D, Vector2> pair : bits) {
          this.bit_layer_map.put(pair.getKey(), curLayer);
          sortedBits.add(pair);
        }
        this.firstLayerBits.put(i, bits.firstElement().getKey());
      }
      Logger.setProgress(curLayer.getLayerNumber() + 1, this.mesh.getLayers()
          .size());
      i++;
    }
    System.out.println("Basic scheduler end scheduling");

    int nbIrrefularBits = 0;
    for (Layer curLayer : this.mesh.getLayers()) {
      nbIrrefularBits += curLayer.getIrregularBits()
          .size();
    }

    System.out.println("Number of Irregular bits in the Mesh: " + nbIrrefularBits);

    mesh.setStripes(build_scheduleStrips());


    return true;
  }

  public Vector<Pair<Bit3D, Vector2>> filterBits(Vector<Pair<Bit3D, Vector2>> bits) {
    double xMin;
    if (bits.size() > 0) {
      xMin = bits.get(0).getValue().x;
      bits = this.sortBits(bits, Math.abs(xMin));
    }
    return bits;
  }

  @Override
  public Vector<SubBit2D> sortedSubBits(Vector<SubBit2D> subBits) {
    Objects.requireNonNull(subBits);
    if (subBits.size() > 0) {
      double xMin = subBits.firstElement().getLiftPointCB().x;
      double xInterval = CraftConfig.workingWidth;
      subBits.sort((v1, v2) -> {
        int v1XColumn = (int) (v1.getLiftPointCB().x +
            mesh.getModel()
                .getPos().x + xMin) / (int) xInterval;
        int v2XColumn =
            (int) (v2.getLiftPointCB().x + mesh.getModel()
                .getPos().x + xMin) / (int) xInterval;

        return v1XColumn - v2XColumn;
//        if (v1XColumn == v2XColumn) {
//          if (Double.compare(v1.getLiftPointCB().x, v2.getLiftPointCB().x) == 0) {
//            return Double.compare(v1.getLiftPointCB().y, v2.getLiftPointCB().y);
//          } else {
//            return Double.compare(v1.getLiftPointCB().x, v2.getLiftPointCB().x);
//          }
//        } else if (v1XColumn < v2XColumn) {
//          return -1;
//        } else {
//          return 1;
//        }
      });
    }
    return subBits;
  }

  public Vector<Pair<Bit3D, Vector2>> sortBits(Vector<Pair<Bit3D, Vector2>> keySet,
      double offsetX) {
    double xInterval = CraftConfig.workingWidth;
    keySet.sort((v1, v2) -> {
      int v1XColumn =
          (int) (v1.getValue().x + mesh.getModel()
              .getPos().x + offsetX) / (int) xInterval;
      int v2XColumn =
          (int) (v2.getValue().x + mesh.getModel()
              .getPos().x + offsetX) / (int) xInterval;

      if (v1XColumn == v2XColumn) {
        if (Double.compare(v1.getValue().x, v2.getValue().x) == 0) {
          return Double.compare(v1.getValue().y, v2.getValue().y);
        } else {
          return Double.compare(v1.getValue().x, v2.getValue().x);
        }
      } else if (v1XColumn < v2XColumn) {
        return -1;
      } else {
        return 1;
      }
    });
    return keySet;
  }




  public ArrayList<ArrayList<Strip>> build_scheduleStrips(){
if(!strips.isEmpty())strips.clear();
System.out.println(strips.size());
    if (!mesh.isPaved()) {
      return null;
    }


    mesh.getLayers().forEach((layer) -> {

      List<Bit3D> bitsInCurrentLayer = AScheduler.getSetBit3DsSortedFrom(
              mesh.getScheduler().filterBits(layer.sortBits()));
      ArrayList<Strip> layerstrips=new ArrayList<>();
System.out.println("bitsInCurrentLayer="+bitsInCurrentLayer.size());
      HashSet<Bit3D> toremove=new HashSet<>();
      Collections.sort(bitsInCurrentLayer,Comparator.comparing(Bit3D::getMinX));


      int size=bitsInCurrentLayer.size();

      while(bitsInCurrentLayer.size()>0){


// loop to find the extremist bit to left
        TreeSet<Bit3D>tofindfirstbit=new TreeSet<>(Comparator.comparing(Bit3D::getMinX ));


        for (Bit3D bit:bitsInCurrentLayer){
          tofindfirstbit.add(bit);
        }

        Iterator<Bit3D>itfirst=tofindfirstbit.iterator();
        layerstrips.add(  new Strip ((NewBit3D)itfirst.next(),layer));

        for(Bit3D bit3D:bitsInCurrentLayer){

          //we verify if the bit can fit in the current strip if not we create a new strip
          if(bit3D.getTwoExtremeXPointsCS().get(0).x>=layerstrips.get(layerstrips.size()-1).getXposition()&&
                  bit3D.getTwoExtremeXPointsCS().get(1).x<=layerstrips.get(layerstrips.size()-1).getXposition()
                          + CraftConfig.workingWidth && num<nbBitesBatch)
          {
            layerstrips.get(layerstrips.size()-1).addBit3D((NewBit3D) bit3D);
            toremove.add(bit3D);


            num++;

          }


        }
        if(num==nbBitesBatch) {
          num=0;
        }
        layerstrips.get(layerstrips.size()-1).getBits().sort(Comparator.comparing(Bit3D::getMinX));
        bitsInCurrentLayer.removeAll(toremove);
      }


     if(!layerstrips.isEmpty()) {System.out.println("layerstrip="+layerstrips.size());
       System.out.println("index="+layer.getLayerNumber());
       strips.add(layerstrips);}
    });
System.out.println("Somme="+strips.stream().mapToInt(ArrayList::size).sum());
    return strips;

  }






  public String toString() {
    return "Basic";
  }

}
