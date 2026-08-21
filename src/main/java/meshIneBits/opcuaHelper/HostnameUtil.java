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

package meshIneBits.opcuaHelper;

import org.slf4j.LoggerFactory;

import java.net.*;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;

public class HostnameUtil {

  /**
   * @return the local hostname, if possible. Failure results in "localhost".
   */
  public static String getHostname() {
    try {
      return InetAddress.getLocalHost().getHostName();
    } catch (UnknownHostException e) {
      return "localhost";
    }
  }

  /**
   * Given an address resolve it to as many unique addresses or hostnames as can be found.
   *
   * @param address the address to resolve.
   * @return the addresses and hostnames that were resolved from {@code address}.
   */
  public static Set<String> getHostnames(String address) {
    return getHostnames(address, true);
  }

  /**
   * Given an address resolve it to as many unique addresses or hostnames as can be found.
   *
   * @param address         the address to resolve.
   * @param includeLoopback if {@code true} loopback addresses will be included in the returned set.
   * @return the addresses and hostnames that were resolved from {@code address}.
   */
  public static Set<String> getHostnames(String address, boolean includeLoopback) {
    Set<String> hostnames = newHashSet();

    try {
      InetAddress inetAddress = InetAddress.getByName(address);

      if (inetAddress.isAnyLocalAddress()) {
        try {
          Enumeration<NetworkInterface> nis = NetworkInterface.getNetworkInterfaces();

          for (NetworkInterface ni : Collections.list(nis)) {
            Collections.list(ni.getInetAddresses()).forEach(ia -> {
              if (ia instanceof Inet4Address) {
                if (includeLoopback || !ia.isLoopbackAddress()) {
                  hostnames.add(ia.getHostName());
                  hostnames.add(ia.getHostAddress());
                  hostnames.add(ia.getCanonicalHostName());
                }
              }
            });
          }
        } catch (SocketException e) {
          LoggerFactory.getLogger(HostnameUtil.class)
              .warn("Failed to NetworkInterfaces for bind address: {}", address, e);
        }
      } else {
        if (includeLoopback || !inetAddress.isLoopbackAddress()) {
          hostnames.add(inetAddress.getHostName());
          hostnames.add(inetAddress.getHostAddress());
          hostnames.add(inetAddress.getCanonicalHostName());
        }
      }
    } catch (UnknownHostException e) {
      LoggerFactory.getLogger(HostnameUtil.class)
          .warn("Failed to get InetAddress for bind address: {}", address, e);
    }

    return hostnames;
  }

}
