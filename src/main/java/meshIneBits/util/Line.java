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

package meshIneBits.util;
/**
 * A Class representing a line in 2D
 */
public class Line  {
   private Vector2 point1;
    private Vector2 point2;
    //y=ax+b
    /**
     * the 2 points that create the line
     * @param point1
     * @param point2
     */
    public Line(Vector2 point1,Vector2 point2){
        this.point1=point1;
        this.point2=point2;
    }

    public Vector2 getPoint1(){
        return point1;
    }
    public Vector2 getPoint2(){
        return point2;
    }
    /**
     *
     * @param lineA
     * @param lineB
     * @return the point of intersection between 2 lines
     */
        public static Vector2 getIntersection(Line lineA, Line lineB)
        {
            final double x1 = lineA.getPoint1().x;
            final double y1 = lineA.getPoint1().y;
            final double x2 = lineA.getPoint2().x;
            final double y2 = lineA.getPoint2().y;

            final double x3 = lineB.getPoint1().x;
            final double y3 = lineB.getPoint1().y;
            final double x4 = lineB.getPoint2().x;
            final double y4 = lineB.getPoint2().y;

            final double d = (x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4);

            if (d != 0)
            {
                final double xi = ((x3 - x4) * (x1 * y2 - y1 * x2) - (x1 - x2) * (x3 * y4 - y3 * x4)) / d;
                final double yi = ((y3 - y4) * (x1 * y2 - y1 * x2) - (y1 - y2) * (x3 * y4 - y3 * x4)) / d;

                return new Vector2(xi, yi);
            }
            return null;
        }

}
