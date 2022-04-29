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

package meshIneBits.util;

import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import meshIneBits.config.CraftConfig;

/**
 * A polygon is an enclosed set of Segment2D.
 */
public class Polygon implements Iterable<Segment2D>, Serializable {

  private Segment2D first = null;

  private Segment2D last = null;
  private boolean enclosed = false;
  /**
   * Cache of path2D
   */
  private Path2D _path2D = null;

  public Polygon() {
  }

  public Polygon(Segment2D segment) {
    first = segment;
    if (first == null) {
      return;
    }
    last = first;
    for (Segment2D s = first.getNext(); s != null; s = s.getNext()) {
      if (s == first) {
        enclosed = true;
        break;
      }
      last = s;
    }
  }

  /**
   * Sort and connect segments. Remove isolated ones.
   *
   * @param segments maybe in disorder and not connecting.
   * @return the first closed polygon. <tt>null</tt> if no closed polygon found
   */
  public static Polygon extractFrom(List<Segment2D> segments) {
    while (!segments.isEmpty()) {
      Vector<Segment2D> sortedSegments = new Vector<>(segments.size());
      sortedSegments.add(segments.get(0));
      // Reconnect the segments
      boolean open = false;
      while (sortedSegments.lastElement()
          .getNext() != sortedSegments.firstElement()) {
        Segment2D lastSegment = sortedSegments.lastElement();
        boolean foundNext = false;
        for (int i = 0; i < segments.size(); i++) {
          Segment2D nextSegment = segments.get(i);
          if (nextSegment.start.asGoodAsEqual(lastSegment.end)) {
            if (i != 0) {
              sortedSegments.add(nextSegment);
            }
            // In case of returning to the starter,
            // We do not need to add
            lastSegment.setNext(nextSegment);
            foundNext = true;
            break;
          }
        }
        // In case this polygon is open
        if (!foundNext) {
          open = true;
          break;
        }
      }
      // Check closeness
      if (open) {
        segments.removeAll(sortedSegments);
      } else {
        return new Polygon(sortedSegments.firstElement());
      }
    }
    // If no closed polygon found
    return null;
  }

  public void addEnd(Segment2D s) {
    if (enclosed) {
      throw new RuntimeException();
    }
    if (first == null) {
      first = s;
    }
    if (last != null) {
      last.setNext(s);
    }
    last = s;
  }

  /**
   * Check integrity of the polygon.
   */
  public void check() {
    if (first == null) {
      return;
    }
    if (enclosed) {
      if (first.getPrev() == null) {
        throw new RuntimeException();
      }
      if (last.getNext() == null) {
        throw new RuntimeException();
      }
      if (last.getNext() != first) {
        throw new RuntimeException();
      }
      if (first.getPrev() != last) {
        throw new RuntimeException();
      }
      for (Segment2D s = first.getNext(); s != first; s = s.getNext()) {
        if (s == null) {
          throw new RuntimeException();
        }
        if (s.getPrev()
            .getNext() != s) {
          throw new RuntimeException();
        }
      }
    } else {
      if (first.getPrev() != null) {
        throw new RuntimeException();
      }
      if (last.getNext() != null) {
        throw new RuntimeException();
      }
    }
  }

  public void close() {
    if (enclosed) {
      throw new UnsupportedOperationException();
    }
    check();
    enclosed = true;
    last.setNext(first);
    check();
  }

  /**
   * Get the closest segment in this segment loop
   *
   * @param p starting point
   * @return closest segment
   */
  public Segment2D closestTo(Vector2 p) {
    Segment2D best = first;
    double bestDist = 99999;
    for (Segment2D s : this) {
      if (s.start.sub(p)
          .vSize2() < bestDist) {
        bestDist = s.start.sub(p)
            .vSize2();
        best = s;
      }
    }
    return best;
  }

  public boolean contains(Segment2D s) {
    return this.toPath2D()
        .contains(s.getMidPoint().x, s.getMidPoint().y);
  }

  /**
   * Check if a point is inside the polygon. Uses boolean java.awt.geom.Path2D.contains.
   */
  private boolean contains(Vector2 point) {
    return this.toPath2D()
        .contains(point.x, point.y);
  }

  /**
   * @param point a point on surface
   * @return <tt>true</tt> if approx point intersects with polygon's border
   * @see CraftConfig#errorAccepted
   */
  private boolean approximatelyContains(Vector2 point) {
    Path2D path2D = this.toPath2D(); // Update the contour
    // Use a rectangle instead of circle
    Rectangle2D.Double r = new Rectangle2D.Double();
    double d = Math.pow(10, -CraftConfig.errorAccepted);
    r.setRect(point.x - d, point.y - d, 2 * d, 2 * d);
    return path2D.intersects(r);
  }

  public boolean empty() {
    return first == null;
  }

  public AABBrect getAABB() {
    AABBrect ret = new AABBrect(first);
    for (Segment2D s : this) {
      ret.addAABB(s);
    }
    return ret;
  }

  @Override
  public Iterator<Segment2D> iterator() {
    return new Segment2DIterator();
  }

  /**
   * removeEnd removes this segment from the segment list, and links up the next segment to the
   * previous. Removing 1 point in the polygon. The point removed is the endpoint of this segment.
   *
   * @param s target
   */
  public void remove(Segment2D s) {
    if (s == first) {
      first = s.getNext();
      //In case we are enclosed with a single segment, the next is the same one. So we are back to an empty polygon.
      if (first == s) {
        first = null;
      }
    }
    if (s == last) {
      last = last.getPrev();
      //In case we are enclosed with a single segment, the prev is the same one. So we are back to an empty polygon.
      if (last == s) {
        last = null;
      }
    }

    if (s.getNext() == null) {
      if (enclosed) {
        throw new RuntimeException();
      }
      // Remove 's' from the linked list.
      s.getPrev()
          .setNext(null);
    } else {
      // Update the start point of s.next to the end of the previous point. Effectively removing
      // s.end from the polygon.
      s.getNext()
          .update(s.getPrev().end, s.getNext().end);
      // Remove 's' from the linked list.
      // We can set 's.next' to null here, even if we are iterating over 's',
      // because the next point of iteration has already been stored by the iterator.
      Segment2D prev = s.getPrev();
      Segment2D next = s.getNext();
      prev.setNext(null);
      s.setNext(null);
      prev.setNext(next);
    }
  }

  /**
   * Convert polygon to Path2D. Used to generate area.
   *
   * @return path in 2D
   */
  public Path2D toPath2D() {
    if (_path2D != null) {
      return _path2D;
    }
    // If not created
    Vector<Double> x = new Vector<>();
    Vector<Double> y = new Vector<>();
    for (Segment2D s : this) {
      x.add(s.start.x);
      y.add(s.start.y);
    }

    _path2D = new Path2D.Double();
    _path2D.moveTo(x.get(0), y.get(0));
    for (int i = 1; i < x.size(); i++) {
      _path2D.lineTo(x.get(i), y.get(i));
    }
    _path2D.closePath();

    _path2D.setWindingRule(Path2D.WIND_EVEN_ODD);

    return _path2D;
  }

  /**
   * @return <tt>true</tt> if bound's height and width after rounding art not 0
   * @see Rounder#round(double, int)
   */
  boolean isNotNegligible() {
    return Rounder.round(toPath2D().getBounds2D()
        .getWidth(), CraftConfig.errorAccepted) > 0
        && Rounder.round(toPath2D().getBounds2D()
        .getHeight(), CraftConfig.errorAccepted) > 0;
  }

  private class Segment2DIterator implements Iterator<Segment2D> {

    private Segment2D next;

    Segment2DIterator() {
      this.next = first;
    }

    @Override
    public boolean hasNext() {
      return next != null;
    }

    @Override
    public Segment2D next() {
      Segment2D ret = next;
      next = next.getNext();
      if (next == first) {
        next = null;
      }
      return ret;
    }
  }

  /**
   * Check if this polygon contains entirely the other
   *
   * @param other non intersecting with this
   * @return <tt>true</tt> if all other's vertices stay inside this
   */
  public boolean contains(Polygon other) {
    // Each time we check the starting point of each segment
    for (Segment2D nextSegment : other) {
      if (!this.contains(nextSegment.start)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Check if this polygon contains entirely the other using approximate check
   *
   * @param other non intersecting with this
   * @return <tt>true</tt> if all other's vertices stay inside this
   * @see #approximatelyContains(Vector2)
   */
  public boolean approximatelyContains(Polygon other) {
    // Each time we check the starting point of each segment
    for (Segment2D nextSegment : other) {
      if (!this.approximatelyContains(nextSegment.start)) {
        return false;
      }
    }
    return true;
  }

  @Override
  public String toString() {
    StringBuilder str = new StringBuilder();
    forEach(segment2D -> str.append(segment2D.start)
        .append(" -> "));
    str.delete(str.lastIndexOf(" -> "), str.length() - 1);
    return "Polygon{" + str.toString() + "}";
  }
}
