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

package meshIneBits;

import meshIneBits.config.CraftConfig;

import java.io.Serializable;
import java.util.ArrayList;

import static meshIneBits.config.CraftConfig.printerX;
import static meshIneBits.config.CraftConfig.printerY;

/**
 * class représentant les Stripes,chaque stripe est caractérisé par sa position X,l'hauteur (position Z),
 *le nombre des bits qu'on peut mettre dedans et à quelle layer cette stripe appartien puisqu'on crée les stripes par layer
 * c.à.d chaque layer peut contenir plusieurs strip ou minimmum 1 et chaque stripe peut contenir plusieurs bits ou minimmum 1
 */
public class Strip implements Serializable {
private double Xposition=-printerX / 2 - CraftConfig.workingWidth - 20;

private double Zposition;
private ArrayList<Bit3D> bits=new ArrayList<Bit3D>();
private Float width= CraftConfig.workingWidth;
private Float length= printerY;
private Layer layer;
    public Strip(Bit3D firstbit,Layer layer){
this.Xposition=firstbit.getTwoExtremeXPointsCS().get(0).x;
this.Zposition=firstbit.getLowerAltitude();
this.layer=layer;
    }
public void addBit3D(Bit3D bit3D){
        bits.add(bit3D);
}

    public ArrayList<Bit3D> getBits() {
        return bits;
    }

    public double getXposition(){

        return this.Xposition;
}
}
