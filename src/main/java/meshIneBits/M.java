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

package meshIneBits;

import java.util.HashMap;
import java.util.Map;

/**
 * Upgrade for the existing event notification with a map of tags, so that we can know the event in
 * detail.<br/>
 */
public class M {

  /**
   * Should be one of open string values of this class
   */
  public final String event;
  /**
   * A map of tags defined in this class.
   */
  public final Map<String, Object> valueMap;

  /**
   * Example:<br/>
   * <code>
   * notifyObservers(new M(M.LAYER_ADDED_BIT, M.map(M.OLD_BIT, oldBit3D, M.NEW_BIT, newBit3D)));
   * </code>
   *
   * @param event    should be one of public string field for tag in this class
   * @param valueMap Use {@link #map(Object...)} to create the value map from couples of tag and
   *                 object (tag placed before order)
   */
  public M(String event, Map<String, Object> valueMap) {
    this.event = event;
    this.valueMap = valueMap;
  }

  /**
   * Convenient method to get object given tag
   *
   * @param tag should be one of public string field for tag in this class
   * @return object, should be cast
   */
  public Object get(String tag) {
    return valueMap.get(tag);
  }

  /**
   * @param objects should have an even of args. If else, the last arg will be omitted. Elements at
   *                even position should be the tag, and ones at odd position should be the
   *                corresponding object
   * @return value map with couples of tag and object
   */
  public static Map<String, Object> map(Object... objects) {
    Map<String, Object> m = new HashMap<>(objects.length / 2);
    for (int i = 0; i < objects.length - 1; i += 2) {
      m.put(objects[i].toString(), objects[i + 1]);
    }
    return m;
  }

  ///////////////
  // Events
  ///////////////
  /**
   * {@link #valueMap} should contains the rebuilt {@link Layer} with tag {@link #REBUILT_LAYER}
   */
  public static final String LAYER_REBUILT = "layerRebuilt";

  /**
   * {@link #valueMap} should contains the added {@link Bit3D} with tag {@link #NEW_BIT}
   */
  public static final String LAYER_ADDED_BIT = "layerAddedBit";

  /**
   * {@link #valueMap} should contains the old {@link Bit3D} with tag {@link #OLD_BIT} and the new
   * {@link Bit3D} with tag {@link #NEW_BIT}
   */
  public static final String LAYER_MOVED_BIT = "layerMovedBit";

  /**
   * {@link #valueMap} should contains the collection of old {@link Bit3D}s with tag {@link
   * #OLD_BIT} and the collection of new {@link Bit3D} with tag {@link #NEW_BIT}
   */
  public static final String LAYER_MOVED_BITS = "layerMovedBits";

  /**
   * {@link #valueMap} should contains the old {@link Bit3D} with tag {@link #OLD_BIT}
   */
  public static final String LAYER_REMOVED_BIT = "layerRemovedBit";

  /**
   * {@link #valueMap} should contains the collection of old {@link Bit3D}s with tag {@link
   * #OLD_BIT}
   */
  public static final String LAYER_REMOVED_BITS = "layerRemovedBits";

  ///////////////
  // Tag
  ///////////////
  /**
   * Tag of the {@link Layer} rebuilt
   */
  public static final String REBUILT_LAYER = "rebuiltLayer";

  /**
   * Tag of the old {@link Bit3D}
   */
  public static final String OLD_BIT = "oldBit";

  /**
   * Tag of the new {@link Bit3D}
   */
  public static final String NEW_BIT = "newBit";
}
