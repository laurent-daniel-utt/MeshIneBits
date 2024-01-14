/*
 * MeshIneBits is a Java software to disintegrate a 3d project (model in .stl)
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

package meshIneBits.gui.view2d;

import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;

class PropertyTableModel extends AbstractTableModel {

  private static String[] columnNames = {
      "Property",
      "Value"
  };
  private String[][] properties;
  private List<String> propertyLabels = new ArrayList<>();

  PropertyTableModel(String[][] properties) {
    this.properties = properties;
    for (String[] property : properties) {
      propertyLabels.add(property[0]);
    }
  }

  @Override
  public int getRowCount() {
    return properties.length;
  }

  @Override
  public int getColumnCount() {
    return columnNames.length;
  }

  @Override
  public boolean isCellEditable(int row, int column) {
    return false;
  }

  @Override
  public String getValueAt(int rowIndex, int columnIndex) {
    if (columnIndex < 2 && rowIndex < properties.length) {
      return properties[rowIndex][columnIndex];
    } else {
      return "";
    }
  }

  void setValueAt(String label, String value) {
    int i = propertyLabels.indexOf(label);
    if (i > -1) {
      properties[i][1] = value;
      fireTableCellUpdated(i, 1);
    }
  }
}
