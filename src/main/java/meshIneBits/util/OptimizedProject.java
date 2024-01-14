/*
 * MeshIneBits is a Java software to disintegrate a 3d project (model in .stl)
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

package meshIneBits.util;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import meshIneBits.Bit2D;
import meshIneBits.Bit3D;
import meshIneBits.Layer;
import meshIneBits.Project;

public class OptimizedProject {

  private Project project;
  private Map<Integer, List<Bit3D>> mapBitNotFullLength;
  private List<Map<Bit3D, List<Bit3D>>> listToOptimized;
  private List<Layer> layers;
  private ExecutorService executorService = Executors.newSingleThreadExecutor();

  public OptimizedProject() {
  }

  public OptimizedProject updateMesh(Project project) {
    this.project = project;
    layers = project.getLayers();
    return this;
  }

  public void optimize() {
    if (project == null) {
      throw new NullPointerException("project is null");
    }
    executorService.execute(() -> {
      for (Layer layer : layers) {
        if (!layer.isPaved()) {
          continue;
        }
        System.out.println("layer " + layers.indexOf(layer));
        for (Bit3D bit3D : layer.getAllBit3D()) {
          if (!bit3D.getBaseBit()
              .isFullLength()) {
            Set<Bit3D> bits = new HashSet<>();
            Bit2D bit2D = bit3D.getBaseBit();
            Vector2 orientation = bit2D.getOrientation();
            Vector2 origin = bit2D.getOriginCS();
//                        double line = origin.x*orientation.x+origin.y*orientation.y;
            double a = orientation.x * origin.x + orientation.y * origin.y;
            System.out.println(
                "bit not full length: orientation " + orientation.toString() + ", origin: "
                    + origin.toString());
            for (Bit3D bit : layer.getAllBit3D()) {
              double result = -1;
              boolean b = false;
              if (bit.getOrientation()
                  .asGoodAsEqual(orientation)) {
                if (orientation.x == 0) {
                  result = bit.getOrigin().x - origin.x;
                  b = orientation.y * (bit.getOrigin().y - origin.y) > 0;
//                                    if(result<=0.0001&&result>=-0.0001&&b){
//                                        layer.scaleBit(bit3D,50,100);
//                                        layer.moveBit(bit,new Vector2(-1,0));
//                                    }
                } else if (orientation.x == 1 && orientation.y == 0) {
                  result = bit.getOrigin().y - origin.y;
                  b = orientation.x * (bit.getOrigin().x - origin.x) > 0;
                }
                if (result <= 0.0001 && result >= -0.0001 && b) {
                  System.out.println(
                      "bit on line : orientation " + bit.getOrientation()
                          .toString() + ", origin: "
                          + bit.getOrigin()
                          .toString());
                  bits.add(bit);
                }
              }
            }
            if (bits.size() > 0) {
              if (orientation.x == 0) {
                layer.scaleBit(bit3D, 50, 100);
                layer.moveBits(bits,
                    new Vector2(orientation.y > 0 ? -orientation.y : orientation.y, 0));
              } else if (orientation.y == 0) {
                layer.scaleBit(bit3D, 50, 100);
                layer.moveBits(bits,
                    new Vector2(orientation.x > 0 ? -orientation.x : orientation.x, 0));
              }
            }
          }
        }

      }
    });
  }
}
