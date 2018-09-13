/*
 * MeshIneBits is a Java software to disintegrate a 3d mesh (model in .stl)
 * into a network of standard parts (called "Bits").
 *
 * Copyright (C) 2016  Thibault Cassard & Nicolas Gouju.
 * Copyright (C) 2017-2018  TRAN Quoc Nhat Han.
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

package meshIneBits.gui.view3d;

import java.util.Vector;

/**
 * 
 * @author Nicolas
 *
 */
class PolygonPointsList {
	
	private Vector<int[]> points;
	private int curPosition = -1;
	
	/**
	 * 
	 * @param points
	 * @throws Exception 
	 */
	public PolygonPointsList(Vector<int[]> points) throws Exception{
		if(points.size() < 3){
			throw new Exception("Not enough points to build a polygon !");
		}
		this.points = points;
	}
	
	/**
	 * 
	 * @return the next point in the list
	 */
	public int[] getNextPoint(){
		if(curPosition < points.size() - 1)
			curPosition++;
		else
			curPosition = 0;
		return points.get(curPosition);
	}
	
	/**
	 * 
	 * @return Number of points in the list
	 */
	public int getLength(){
		return points.size();
	}
}
