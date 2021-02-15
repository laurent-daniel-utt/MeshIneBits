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

import java.io.Serializable;
import java.util.Iterator;
import java.util.Stack;

/**
 * The Tree2D stores a binary AABB tree. AABB are rectangle 2D 'objects' The
 * Tree2D allows a fast query of all objects in an area.
 */
public class AABBTree<T extends AABBrect> implements Serializable {
    private class Tree2Diterator implements Iterator<T> {
        private AABBrect area;
        private Stack<TreeNode> stack;
        private T ret;

        Tree2Diterator(AABBrect area) {
            this.area = area;
            stack = new Stack<>();
            if (root != null) {
                stack.add(root);
            }
        }

        @Override
        @SuppressWarnings("unchecked")
        public boolean hasNext() {
            if (ret != null) {
                return true;
            }
            while (stack.size() > 0) {
                TreeNode n = stack.pop();
                if (n.aabb.overlap(area)) {
                    if (n.isLeaf()) {
                        ret = (T) n.aabb;
                        return true;
                    } else {
                        stack.push(n.child1);
                        stack.push(n.child2);
                    }
                }
            }
            return false;
        }

        @Override
        public T next() {
            if (!hasNext()) {
                return null;
            }
            T r = ret;
            ret = null;
            return r;
        }

        @Override
        public void remove() {
        }
    }

    public class Tree2Dquery implements Iterable<T> {
        private AABBrect area;

        Tree2Dquery(AABBrect area) {
            this.area = area;
        }

        @Override
        public Iterator<T> iterator() {
            return new Tree2Diterator(area);
        }
    }

    class TreeNode implements Serializable {
        TreeNode parent;
        TreeNode child1;
        TreeNode child2;
        AABBrect aabb;
        int height;

        TreeNode(AABBrect aabb) {
            this.aabb = aabb;
            if (aabb.node != null) {
                throw new RuntimeException();
            }
            aabb.node = this;
        }

        boolean isLeaf() {
            return child1 == null;
        }
    }

    private TreeNode root = null;

    private TreeNode balance(TreeNode A) {
        if (A.isLeaf() || (A.height < 2)) {
            return A;
        }

        TreeNode B = A.child1;
        TreeNode C = A.child2;

        int balance = C.height - B.height;

        // Rotate C up
        if (balance > 1) {
            TreeNode F = C.child1;
            TreeNode G = C.child2;

            // Swap A and C
            C.child1 = A;
            C.parent = A.parent;
            A.parent = C;

            // A's old parent should point to C
            if (C.parent != null) {
                if (C.parent.child1 == A) {
                    C.parent.child1 = C;
                } else {
                    C.parent.child2 = C;
                }
            } else {
                root = C;
            }

            // Rotate
            if (F.height > G.height) {
                C.child2 = F;
                A.child2 = G;
                G.parent = A;
                A.aabb = B.aabb.combine(G.aabb);
                C.aabb = A.aabb.combine(F.aabb);

                A.height = 1 + Math.max(B.height, G.height);
                C.height = 1 + Math.max(A.height, F.height);
            } else {
                C.child2 = G;
                A.child2 = F;
                F.parent = A;
                A.aabb = B.aabb.combine(F.aabb);
                C.aabb = A.aabb.combine(G.aabb);

                A.height = 1 + Math.max(B.height, F.height);
                C.height = 1 + Math.max(A.height, G.height);
            }

            return C;
        }

        // Rotate B up
        if (balance < -1) {
            TreeNode D = B.child1;
            TreeNode E = B.child2;

            // Swap A and B
            B.child1 = A;
            B.parent = A.parent;
            A.parent = B;

            // A's old parent should point to B
            if (B.parent != null) {
                if (B.parent.child1 == A) {
                    B.parent.child1 = B;
                } else {
                    B.parent.child2 = B;
                }
            } else {
                root = B;
            }

            // Rotate
            if (D.height > E.height) {
                B.child2 = D;
                A.child1 = E;
                E.parent = A;
                A.aabb = C.aabb.combine(E.aabb);
                B.aabb = A.aabb.combine(D.aabb);

                A.height = 1 + Math.max(C.height, E.height);
                B.height = 1 + Math.max(A.height, D.height);
            } else {
                B.child2 = E;
                A.child1 = D;
                D.parent = A;
                A.aabb = C.aabb.combine(D.aabb);
                B.aabb = A.aabb.combine(E.aabb);

                A.height = 1 + Math.max(C.height, D.height);
                B.height = 1 + Math.max(A.height, E.height);
            }

            return B;
        }

        return A;
    }

    public void insert(T e) {
        TreeNode leaf = new TreeNode(e);
        if (root == null) {
            root = leaf;
            return;
        }

        // Find the best sibling for this node
        TreeNode node = root;
        while (node.isLeaf() == false) {
            TreeNode child1 = node.child1;
            TreeNode child2 = node.child2;

            double area = node.aabb.getPerimeter();

            AABBrect combinedAABB = node.aabb.combine(e);
            double combinedArea = combinedAABB.getPerimeter();

            // Cost of creating a new parent for this node and the new leaf
            double cost = 2.0f * combinedArea;

            // Minimum cost of pushing the leaf further down the tree
            double inheritanceCost = 2.0f * (combinedArea - area);

            // Cost of descending into child1
            double cost1;
            if (child1.isLeaf()) {
                AABBrect aabb = e.combine(child1.aabb);
                cost1 = aabb.getPerimeter() + inheritanceCost;
            } else {
                AABBrect aabb = e.combine(child1.aabb);
                double oldArea = child1.aabb.getPerimeter();
                double newArea = aabb.getPerimeter();
                cost1 = (newArea - oldArea) + inheritanceCost;
            }

            // Cost of descending into child2
            double cost2;
            if (child2.isLeaf()) {
                AABBrect aabb = e.combine(child2.aabb);
                cost2 = aabb.getPerimeter() + inheritanceCost;
            } else {
                AABBrect aabb = e.combine(child2.aabb);
                double oldArea = child2.aabb.getPerimeter();
                double newArea = aabb.getPerimeter();
                cost2 = (newArea - oldArea) + inheritanceCost;
            }

            // Descend according to the minimum cost.
            if ((cost < cost1) && (cost < cost2)) {
                break;
            }

            // Descend
            if (cost1 < cost2) {
                node = child1;
            } else {
                node = child2;
            }
        }

        TreeNode sibling = node;

        // Create a new parent.
        TreeNode oldParent = sibling.parent;
        TreeNode newParent = new TreeNode(e.combine(sibling.aabb));
        newParent.parent = oldParent;
        newParent.height = sibling.height + 1;

        if (oldParent != null) {
            // The sibling was not the root.
            if (oldParent.child1 == sibling) {
                oldParent.child1 = newParent;
            } else {
                oldParent.child2 = newParent;
            }

            newParent.child1 = sibling;
            newParent.child2 = leaf;
            sibling.parent = newParent;
            leaf.parent = newParent;
        } else {
            // The sibling was the root.
            newParent.child1 = sibling;
            newParent.child2 = leaf;
            sibling.parent = newParent;
            leaf.parent = newParent;
            root = newParent;
        }

        // Walk back up the tree fixing heights and AABBs
        node = leaf.parent;
        while (node != null) {
            node = balance(node);

            TreeNode child1 = node.child1;
            TreeNode child2 = node.child2;

            node.height = 1 + Math.max(child1.height, child2.height);
            node.aabb = child1.aabb.combine(child2.aabb);

            node = node.parent;
        }
    }

    public Tree2Dquery query(AABBrect treeAABB) {
        return new Tree2Dquery(treeAABB);
    }

    @SuppressWarnings("unchecked")
    public void remove(T e) {
        TreeNode leaf = e.node;
        e.node = null;
        if (leaf == root) {
            root = null;
            return;
        }

        TreeNode parent = leaf.parent;
        TreeNode grandParent = parent.parent;
        TreeNode sibling;
        if (parent.child1 == leaf) {
            sibling = parent.child2;
        } else {
            sibling = parent.child1;
        }

        if (grandParent != null) {
            // Destroy parent and connect sibling to grandParent.
            if (grandParent.child1 == parent) {
                grandParent.child1 = sibling;
            } else {
                grandParent.child2 = sibling;
            }
            sibling.parent = grandParent;

            // Adjust ancestor bounds.
            TreeNode index = grandParent;
            while (index != null) {
                index = balance(index);

                TreeNode child1 = index.child1;
                TreeNode child2 = index.child2;

                index.aabb = child1.aabb.combine(child2.aabb);
                index.height = 1 + Math.max(child1.height, child2.height);

                index = index.parent;
            }
        } else {
            root = sibling;
            sibling.parent = null;
        }
    }
}