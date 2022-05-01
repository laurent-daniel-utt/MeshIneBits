/*
 * MeshIneBits is a Java software to disintegrate a 3d mesh (model in .stl)
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

package meshIneBits.artificialIntelligence;

import meshIneBits.Bit2D;
import meshIneBits.artificialIntelligence.debug.DebugTools;
import meshIneBits.artificialIntelligence.util.SectionTransformer;
import meshIneBits.config.CraftConfig;
import meshIneBits.slicer.Slice;
import meshIneBits.util.Segment2D;
import meshIneBits.util.Vector2;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.variables.IntVar;

import java.awt.geom.NoninvertibleTransformException;
import java.util.Vector;

public class ConstraintOptimizedAlgorithm {
    public ConstraintOptimizedAlgorithm() {

    }

    public Vector<Bit2D> getBits(Slice slice, double minWidth, double numberMaxBits) throws NoninvertibleTransformException {
        Vector<Bit2D> resultBits = new Vector<>();
        Vector<Vector<Vector2>> bounds = new GeneralTools().getBoundsAndRearrange(slice);

        int OFFSET = 100;
        int NBPOINTS = 3;
        //todo multiple bits
        Vector<Vector2> bound = bounds.get(0);
        Vector2 startPoint = slice.getSegmentList().get(0).start;
        Vector<Vector2> sectionPoints = SectionTransformer.getSectionPointsFromBound(bound, startPoint);
        sectionPoints = SectionTransformer.repopulateWithNewPoints(NBPOINTS, sectionPoints, false);
        sectionPoints.remove(sectionPoints.lastElement());//todo le dernier point est dupliqué !
        //rendre les points entiers
        for (int i = 0; i < sectionPoints.size(); i++) {
            sectionPoints.set(i, new Vector2((int) sectionPoints.get(i).x, (int) sectionPoints.get(i).y));
        }
        System.out.println("sectionPoints : " + sectionPoints);
        Vector2 endPoint = sectionPoints.get(NBPOINTS - 1);

        Model model = new Model("Bit Placement");

        System.out.println("SETTING VARIABLES");
        //variables des points de la ligne : on laisse libre le premier et dernier point,
        // les autres sont calculés en fonction des deux
        IntVar[] x = new IntVar[NBPOINTS];
        IntVar[] y = new IntVar[NBPOINTS];
        int L = NBPOINTS-1;
        System.out.println("L = " + L);
        x[0] = model.intVar("x_" + 0, (int) startPoint.x - OFFSET, (int) startPoint.x + OFFSET);
        y[0] = model.intVar("y_" + 0, (int) startPoint.y - OFFSET, (int) startPoint.y + OFFSET);
        x[L] = model.intVar("x_" + L, (int) endPoint.x - OFFSET, (int) endPoint.x + OFFSET);
        y[L] = model.intVar("y_" + L, (int) endPoint.y - OFFSET, (int) endPoint.y + OFFSET);

        System.out.println("\nCALCUL DES POINTS INTERMEDIAIRES");
        for (int i = 1; i < NBPOINTS - 1; i++) {
//              FORMULE x[i] = x[0] + (x[-1]-x[0])/i
            x[i] = model.intVar("x_" + i, x[L].sub(x[0]).div(NBPOINTS - 1).mul(i).add(x[0]).intVar());
            y[i] = model.intVar("y_" + i, y[L].sub(y[0]).div(NBPOINTS - 1).mul(i).add(y[0]).intVar());
            x[i].eq(x[L].sub(x[0]).div(NBPOINTS - 1).mul(i).add(x[0])).post();
            y[i].eq(y[L].sub(y[0]).div(NBPOINTS - 1).mul(i).add(y[0])).post();
            System.out.println("x[" + i + "] = " + x[i] + " y[" + i + "] = " + y[i]);
        }


        //variables des points de la section
        IntVar[] xSection = new IntVar[NBPOINTS];
        IntVar[] ySection = new IntVar[NBPOINTS];
        for (int i = 0; i < NBPOINTS; i++) {
            xSection[i] = model.intVar("xS" + i, (int) sectionPoints.get(i).x);
            ySection[i] = model.intVar("yS" + i, (int) sectionPoints.get(i).y);
        }

        //variables des distances
        System.out.println("\nCALCUL DES DISTANCES");
        IntVar[] distances = new IntVar[NBPOINTS];
        for (int i = 0; i < NBPOINTS; i++) {
            //distance entre le point x,y et le point de sectionPoints
//            int dist = (int) Math.sqrt(Math.pow(x[i].getValue() - xSection[i].getValue(), 2) + Math.pow(y[i].getValue() - ySection[i].getValue(), 2));
//            distances[i] = model.intVar("distance" + i, dist);
//            distances[i] = model.intVar("Di", 0, 200);//todo on peut ne pas mettre le model.intvar ?
//            (x-xS)²+ (y-yS)²
//            distances[i] = model.intOffsetView(
//                    model.intScaleView(
//                            model.intOffsetView(x[i],model.intMinusView(xSection[i])),2));

//            distances[i] = distances[i].add(x[i].sub(xSection[i]).pow(2),y[i].sub(ySection[i]).pow(2)).intVar();
//            distances[i].eq(x[i].sub(xSection[i]).pow(2).add(y[i].sub(ySection[i]).pow(2)).sqr()).post();//todo don't post ?
//            IntVar distX = model.intVar("D" + i + "x", 0, 600);//version avec les model.distance
//            IntVar distY = model.intVar("D" + i + "y", 0, 600);
            IntVar distX = model.intVar("D" + i + "x", x[i].sub(xSection[i]).intVar());
            IntVar distY = model.intVar("D" + i + "y", y[i].sub(ySection[i]).intVar());
//            distX.eq(x[i].sub(xSection[i]).intVar()).post();
//            distY.eq(y[i].sub(ySection[i]).intVar()).post();

            System.out.println(x[i] + " - " + xSection[i] + " - " + distX);
            System.out.println(y[i] + " - " + ySection[i] + " - " + distY);
//            model.distance(x[i], xSection[i], "=", distX).post();
//            model.distance(y[i], ySection[i], "=", distY).post();

            distances[i] = model.intVar("D" + i, distX.add(distY).intVar()); //OK
            distances[i].eq(distX.add(distY).intVar()).post();//OK
//            model.arithm(distX, ">", 0).post();//marche pas car val absolue
//            model.arithm(distY, ">", 0).post();
//            model.arithm(x[i],"<=",xSection[i]).post();//pareil
//            model.arithm(y[i],"<=",ySection[i]).post();
            //TODO OOOOOOOOOOOOOOOO on veut que les segments [xi,xi+1] et [xSi,xSi+1] ne se croisent pas
            // ou alors, on recalcule les distances sans le model.distance, pour pas qu'il y ait de val absolue
        }

        //SOMME DES DISTANCES
//        IntVar SUM = model.sum("SUM", distances);
        IntVar SUM = model.intVar("SUM", 0, 600);
        model.sum(distances, "=", SUM).post();
        //la distance du coin de la lamelle au point de départ de la section doit être inférieur à la largeur de la lamelle
        model.distance(x[0], xSection[0], "<", (int) CraftConfig.bitWidth).post();

        //constraints
        System.out.println("SETTING CONSTRAINTS");
        for (int i = 0; i < distances.length; i++) {
//            model.arithm(distances[i], ">=", 0).post();

            //on fixe les distances entre les paires de points
//            model.arithm(distances[i],"=",
//                            x[i].sub(xSection[i]).pow(2).add(y[i].sub(ySection[i]).pow(2))
//                            .intVar())
//                    .post();
        }
        for (int i = 0; i < distances.length - 1; i++) {
            //tous les points de la ligne ont la même distance entre eux
//            model.arithm(x[i].sub(x[i+1]).pow(2).add(y[i].sub(y[i+1]).pow(2)).sqr().intVar(),//todo pas forcément besoin en fait
//                    "=", (int) distBetweenPointsOfLine).post();
        }
        for (int i = 1; i < distances.length - 1; i++) {
            //les points sont sur la même ligne
//            model.arithm(
//                    x[i].sub(x[i+1]).pow(2).add(y[i].sub(y[i+1]).pow(2)).sqr().add(
//                                        x[i-1].sub(x[i]).pow(2).add(y[i-1].sub(y[i]).pow(2)).sqr()
//                                ).sub(1).intVar(),
//                                "=",
//                    (int)(2*distBetweenPointsOfLine)).post();
//            model.arithm(
//                    x[i-1].sub(x[i+1]).pow(2).add(y[i-1].sub(y[i+1]).pow(2)).sqr().intVar(),
//                    ">",
//                    x[i].sub(x[i+1]).pow(2).add(y[i].sub(y[i+1]).pow(2)).sqr().add(
//                            x[i-1].sub(x[i]).pow(2).add(y[i-1].sub(y[i]).pow(2)).sqr()
//                    ).sub(1).intVar()).post();
        }

        //solving
        System.out.println("SOLVING : ");
//        model.setObjective(Model.MINIMIZE, model.sum("sumOfDistances", distances));
        model.setObjective(Model.MINIMIZE, SUM);
        Solver solver = model.getSolver();
//        solver.showStatistics();
//        solver.showSolutions();
//        solver.showDashboard();
//        Solution solution;
//        while (solver.solve()) {
//            System.out.println("    NEW SOLUTION... SUM="+SUM.getValue());
//            solution = new Solution(model);
//            //print solution
////            solution.retrieveIntVars(false).forEach(System.out::println);
////            // an improving solution has been found
//        }
//         the last solution found was optimal (if search completed)
//         solution = new Solution(model);
//        solution.record();

//        Solution solution = solver.findSolution();// ! sat problem et ca retire la fonction objectif
//        Solution solution = solver.findOptimalSolution(model.sum("sum",distances), false); //renvoie une solution où toutes les contraintes sont pas satisfaites ??
        System.out.println("\n######## SOLUTION SUM="+SUM.getValue());
        solver.solve();
        Solution solution = new Solution(model);
        for (IntVar integers : solution.retrieveIntVars(false)) {
            System.out.println(integers.toString());
        }

//        System.out.println("RETRIEVING ALL SOLUTIONS : ");
//        List<Solution> sols = solver.findAllOptimalSolutions(SUM,false);
//        for (Solution sol : sols) {
//            System.out.println("    SOLUTION... SUM="+SUM.getValue());
//            solution=sol;
//        }



        System.out.println("POINTS DE LA SECTION CALCULES");
        for (int i = 0; i < xSection.length; i++) {
            ;
            System.out.print("(" + solution.getIntVal(xSection[i]) + "," + solution.getIntVal(ySection[i]) + ") ");
        }
        System.out.println("\n");
        System.out.println("POINTS DE LA LIGNE CALCULES");
        for (int i = 0; i < x.length; i++) {
            System.out.print("(" + solution.getIntVal(x[i]) + "," + solution.getIntVal(y[i]) + ") ");
        }

        System.out.println("\n");

        //PAINT
        DebugTools.setPaintForDebug(true);
        DebugTools.pointsToDrawORANGE = sectionPoints;
        //ajouter aux pointsToDrawGREEN les points de la ligne calculée
        for (int i = 0; i < x.length; i++) {
            DebugTools.pointsToDrawGREEN.add(new Vector2(solution.getIntVal(x[i]), solution.getIntVal(y[i])));
        }
        DebugTools.segmentsToDraw.add(new Segment2D(
                new Vector2(solution.getIntVal(x[0]), solution.getIntVal(y[0]))
                , new Vector2(solution.getIntVal(x[x.length-1]), solution.getIntVal(y[y.length-1]))));
        return resultBits;
    }
}
