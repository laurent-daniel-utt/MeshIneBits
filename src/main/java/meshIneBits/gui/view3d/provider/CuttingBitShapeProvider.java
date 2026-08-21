/*------------------------------------------------------------------------------
 -  MeshIneBits is a Java software to disintegrate a 3d mesh (model in .stl)
 -  into a network of standard parts (called "Bits").
 -
 -  Copyright (C) 2016-2026 DANIEL Laurent.
 -  Copyright (C) 2015 Cédric Siourakhan
 -  Copyright (C) 2016 Gabriel Magny
 -  Copyright (C) 2016  CASSARD Thibault & GOUJU Nicolas.
 -  Copyright (C) 2017-2018  TRAN Quoc Nhat Han.
 -  Copyright (C) 2018 VALLON Benjamin.
 -  Copyright (C) 2018 LORIMER Campbell.
 -  Copyright (C) 2018 D'AUTUME Christian.
 -  Copyright (C) 2019 DURINGER Nathan (Tests).
 -  Copyright (C) 2020-2021 CLAIRIS Etienne & RUSSO André.
 -  Copyright (C) 2020-2021 DO Quang Bao.
 -  Copyright (C) 2021 VANNIYASINGAM Mithulan.
 -  Copyright (C) 2021  Quang Long
 -  Copyright (C) 2022 Mhamad Atlab
 -  Copyright (C) 2022 Majed Hlaihel
 -  Copyright (C) 2026 Aymeric Sirejol
 -
 -  This program is free software: you can redistribute it and/or modify
 -  it under the terms of the GNU General Public License as published by
 -  the Free Software Foundation, either version 3 of the License, or
 -  (at your option) any later version.
 -
 -  This program is distributed in the hope that it will be useful,
 -  but WITHOUT ANY WARRANTY; without even the implied warranty of
 -  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 -  GNU General Public License for more details.
 -
 -  You should have received a copy of the GNU General Public License
 -  along with this program. If not, see <https://www.gnu.org/licenses/>.
 -----------------------------------------------------------------------------*/

package meshIneBits.gui.view3d.provider;

import java.util.HashMap;
import java.util.Map;
import meshIneBits.Mesh;
import meshIneBits.NewBit3D;
import meshIneBits.gui.view3d.builder.CuttingBitBuilder;
import meshIneBits.gui.view3d.builder.CuttingBitShape;
import processing.core.PApplet;

public class CuttingBitShapeProvider {

  private final CuttingBitBuilder builder;
  private final Mesh mesh;
  private final Map<NewBit3D, CuttingBitShape> shapeMap = new HashMap<>();

  public CuttingBitShapeProvider(PApplet context, Mesh mesh) {
    this.builder = new CuttingBitBuilder(context);
    this.mesh = mesh;
    initializeShapeMap(mesh);
  }

  private void initializeShapeMap(Mesh mesh) {
    if (mesh == null) {
      return;
    }
    mesh.getScheduler().getSortedBits().forEach(pair -> {
      NewBit3D bit3D = (NewBit3D) pair.getKey();
      shapeMap.put(bit3D, buildCuttingBitShape(bit3D));
    });
  }

  private CuttingBitShape buildCuttingBitShape(NewBit3D bit3D) {
    return builder.buildCuttingBitShape(bit3D);
  }

  public CuttingBitShape getCuttingBitShapeByBit(NewBit3D bit3D) {
    return shapeMap.get(bit3D);
  }

  public CuttingBitShape getCuttingBitShapeById(int bitId) {
    return shapeMap.get(mesh.getScheduler().getSortedBits().get(bitId).getKey());
  }


}
