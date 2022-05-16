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
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.jetbrains.annotations.NotNull;

import java.awt.geom.NoninvertibleTransformException;
import java.util.Vector;

public class COAlgo2 {
    private static final double MARGIN = 4; //le décalage d'un bit vers l'exterieur de la section
    int COEFF = 80;//un coeff de 100% maximise le nombre de points de la section qui sont compris entre les deux lignes
    //on definit nos variables GLOBALES
    final int OFFSET = 100;
    final int NBPOINTS = 4;
    private IntVar[] yLigneGauche;
    private IntVar[] xLigneGauche;
    private IntVar[] xLigneDroite;
    private IntVar[] yLigneDroite;
    //todo avec 5 points et plus, ca ne marche plus, voir pourquoi
    // afficher les points de la ligne pour voir, IDEE : que chaque point de la ligne soit à une distance x du précédent
    // qui correspond aux distances x entre les points de la section


    //todo a l'avenir, pas besoin de repeupler avec un certain nombre de points, on garde les points originaux de la section
// Mmm en même temps, si c'est une section avec bcp bcp de points, ca serait pas mal d'en prendre moins
// Reste à voir comment on les choisis et si y'a pas moyen de garder que les points importants
// ooooh mais si il faut utiliser l'algo de Convex Hull !!! Mmmm ou pas.. a voir
//###############################################################################################################


    public Vector<Bit2D> getBits(Slice slice, double minWidth, double numberMaxBits) throws NoninvertibleTransformException {
        Vector<Bit2D> bits = new Vector<>();

        Vector<Vector<Vector2>> bounds = new GeneralTools().getBoundsAndRearrange(slice);
        for (Vector<Vector2> bound : bounds) {
            Vector2 veryFirstStartPoint = bound.get(0);
            Vector2 nextStartPoint = bound.get(0);

            System.out.println("++++++++++++++ BOUND " + bounds.indexOf(bound) + " ++++++++++++++++");

            Vector<Vector2> sectionPoints;
            int iBit = 0;//TODO DebugOnly
            do {
                System.out.println("PLACEMENT BIT " + iBit + "====================");
                System.out.println("NEXT START POINT : " + nextStartPoint);
                sectionPoints = SectionTransformer.getSectionPointsFromBound(bound, nextStartPoint);

                Bit2D bit = getBitFromSectionWithCP(sectionPoints);
                if (bit != null) {
                    bits.add(bit);
//                placement = getBitPlacement(sectionPoints, areaSlice, minWidthToKeep);
//                bits.add(placement.bit2D);
                    nextStartPoint = GeneralTools.getBitAndContourSecondIntersectionPoint(bit, bound, false, nextStartPoint);
                    System.out.println("FIN PLACEMENT BIT " + iBit + "====================");
                }
                iBit++;


            } while (!((listContainsAsGoodAsEqual(veryFirstStartPoint, sectionPoints) && iBit > 1) || listContainsAllAsGoodAsEqual(bound, sectionPoints)) && iBit < 30);//todo max bits
            //while (!listContainsAsGoodAsEqual(veryFirstStartPoint, placement.sectionCovered.subList(1, placement.sectionCovered.size())) && iBit<40); //Add each bit on the bound


        }


        return bits;
    }

    //todo duplicated code
    private boolean listContainsAsGoodAsEqual(Vector2 point, Vector<Vector2> points) {
        for (Vector2 p : points) {
            if (point.asGoodAsEqual(p)) {
                return true;
            }
        }
        return false;
    }

    //todo duplicated code
    private boolean listContainsAllAsGoodAsEqual(Vector<Vector2> containedList, Vector<Vector2> containerList) {
        for (Vector2 p : containedList) {
            if (!listContainsAsGoodAsEqual(p, containerList)) {
                return false;
            }
        }
        return true;
    }

    private Bit2D getBitFromSectionWithCP(Vector<Vector2> sectionPoints) throws NoninvertibleTransformException {
        //on recupere la section de la slice
//        Vector<Vector2> sectionPoints = SectionTransformer.getSectionPointsFromBound(bound, slice.getSegmentList()
//                .get(0).start);

        System.out.println("sectionPointsLocal repère global : " + sectionPoints); //todo test only

        //on met la section dans un repère local
        double angleLocalSystem = SectionTransformer.getLocalCoordinateSystemAngle(sectionPoints);
        Vector2 startPointGlobalSystem = sectionPoints.firstElement();
        Vector<Vector2> sectionPointsLocal = SectionTransformer.getGlobalSectionInLocalCoordinateSystem(sectionPoints, angleLocalSystem, startPointGlobalSystem);

        //on repeuple avec des nouveaux points réguliers
        sectionPointsLocal = SectionTransformer.repopulateWithNewPoints(NBPOINTS, sectionPointsLocal, false);
//        sectionPointsLocal.remove(sectionPointsLocal.lastElement());//todo le dernier point est dupliqué !

        //on rend tous les points entiers
        for (int i = 0; i < sectionPointsLocal.size(); i++) {
            sectionPointsLocal.set(i, new Vector2((int) sectionPointsLocal.get(i).x, (int) sectionPointsLocal.get(i).y));
        }

        //on trouve les startPoint et endPoint
        Vector2 startPoint = sectionPointsLocal.firstElement();
        Vector2 endPoint = sectionPointsLocal.lastElement();

        System.out.println("sectionPointsLocal nouveau repère : " + sectionPointsLocal);


//###############################################################################################################
        //on cree le modèle
        Model model = defineModel(startPoint, endPoint, sectionPointsLocal);

//###############################################################################################################
        //on résout le problème
        System.out.println();
        System.out.println("SOLVING");
        Solver solver = model.getSolver();
        solver.showDashboard();

        Solution solution = null;
        while (solver.solve()) {
            System.out.println("    SOLVED : Sum=" + model.getObjective());
            solution = new Solution(model);
            solution.record();
            printSolution(solution);
        }
        assert solution != null;
        System.out.println("BEST SOLVED : Sum=" + solution.getIntVal(model.getObjective().asIntVar()));

//###############################################################################################################
        //on recupere les points de la solution
        Vector<Vector2> lignePointsLocal = new Vector<>();
        for (int i = 0; i < NBPOINTS; i++) {
            lignePointsLocal.add(new Vector2(solution.getIntVal(xLigneGauche[i]), solution.getIntVal(yLigneGauche[i])));
        }

        //remet les points de la ligne dans le repère global
        Vector<Vector2> lignePointsLocalReduit = new Vector<>();
        lignePointsLocalReduit.add(lignePointsLocal.firstElement());
        lignePointsLocalReduit.add(lignePointsLocal.lastElement());
        Vector<Vector2> lignePointsGlobal = SectionTransformer.getLocalSectionInGlobalCoordinateSystem(lignePointsLocalReduit, angleLocalSystem, startPointGlobalSystem);
        Bit2D bit = getBit(lignePointsGlobal, sectionPoints.firstElement());

        System.out.println("lignePointsGlobal : " + lignePointsGlobal);
        System.out.println("lignePointsLocalReduit : " + lignePointsLocalReduit);
        System.out.println("lignePointsLocal : " + lignePointsLocal);

        //###############################################################################################################


        //on dessine les résultats
        DebugTools.setPaintForDebug(true);
        DebugTools.pointsToDrawBLUE.addAll(sectionPoints);
        DebugTools.pointsToDrawORANGE.addAll(lignePointsGlobal);
        DebugTools.pointsToDrawGREEN.add(lignePointsLocal.firstElement());
        DebugTools.pointsToDrawGREEN.add(lignePointsLocal.lastElement());
        DebugTools.pointsToDrawRED.add(lignePointsGlobal.firstElement());

        return bit;
    }

    private Bit2D getBit(Vector<Vector2> lignePoints, Vector2 startPointOnTheSection) {
        // le premier point de la ligne est le coin inférieur gauche de la lamelle
        // le dernier point de la ligne est le coin supérieur gauche de la lamelle
        // on calcule le centre de la lamelle
        Vector2 firstPointLigne = lignePoints.firstElement();
        Vector2 lastPointLigne = lignePoints.lastElement();
        //todo enlever ? on recalcule angleLocalSystem
        Vector2 angleVector = new Vector2(lastPointLigne.x - firstPointLigne.x, lastPointLigne.y - firstPointLigne.y).normal();

//        Vector2 center = new Vector2(
//                firstPointLigne.x+ CraftConfig.bitWidth/2*Math.cos(Math.toRadians(angleLocalSystem)),
//                firstPointLigne.y+ CraftConfig.lengthFull/2*Math.sin(Math.toRadians(angleLocalSystem))
//        );


//        Vector2 colinear = Vector2.getEquivalentVector(angleLocalSystem).normal();
//        Vector2 center = firstPointLigne
//                .add(colinear.mul(CraftConfig.lengthFull/2))
//                .add(orthogonal.mul(CraftConfig.bitWidth/2));
        Vector2 orthogonal = angleVector.rotate(new Vector2(0, -1).normal()); // 90deg anticlockwise rotation

        //on calcule un nouveau firstPointLigne :
        // on prolonge le vecteur angleVector vers le Bas grâce à un segment
        //on crée un vecteur orthogonal à angleVector, qui part de du startPointOnTheSection
        //on cherche l'intersection entre ces vecteurs, c'est la nouvelle valeur de firstPointLigne
        Segment2D segmentAngleVector = new Segment2D(firstPointLigne.add(angleVector.mul(CraftConfig.lengthFull*100)),
                                                     firstPointLigne.sub(angleVector.mul(CraftConfig.lengthFull*100)));
        Segment2D segmentOrthogonal = new Segment2D(startPointOnTheSection.sub(orthogonal.mul(CraftConfig.lengthFull*100)),
                                                    startPointOnTheSection.add(orthogonal.mul(CraftConfig.lengthFull*100)));
        Vector2 intersection = Segment2D.getIntersectionPoint(segmentAngleVector, segmentOrthogonal);

        Vector2 origin = intersection.sub(
                new Vector2(-CraftConfig.lengthNormal / 2, -CraftConfig.bitWidth / 2 + MARGIN)
                        .rotate(angleVector));

//        Vector2 A = this.getOrigin()
//                .add(colinear.mul(length / 2))
//                .add(orthogonal.mul(CraftConfig.bitWidth/2));

        Bit2D bit = new Bit2D(origin, angleVector);
        DebugTools.pointsToDrawBLUE.add(bit.getBitSidesSegments().get(0).start);
        DebugTools.pointsToDrawBLUE.add(bit.getBitSidesSegments().get(1).start);
        DebugTools.pointsToDrawBLUE.add(bit.getBitSidesSegments().get(2).start);
        DebugTools.pointsToDrawBLUE.add(bit.getBitSidesSegments().get(3).start);
        return bit;
    }

    private void printSolution(Solution solution) {
        //on récupère les résultats et on les affiche
        System.out.println("\n###############################################################################");
        for (IntVar integers : solution.retrieveIntVars(true)) {
            System.out.print(integers.toString() + "\t");
        }
        System.out.println("\n###############################################################################");
    }


    /*
     *TODO : ameliorer le model : maintenant on veut calculer la solution en utilisant 2 lignes,
     * une qui doit être au dessus de la section et une en dessous.
     */
    @NotNull
    private Model defineModel(Vector2 startPoint, Vector2 endPoint, Vector<Vector2> sectionPoints) {
        Model model = new Model("Bit placement optimisation");

//on définit les variables et les contraintes du modele
        //variables des points de la ligne : on laisse libre le premier et dernier point,
        // les autres sont calculés en fonction des deux extrémités
        xLigneGauche = new IntVar[NBPOINTS];
        yLigneGauche = new IntVar[NBPOINTS];
        int L = NBPOINTS - 1;
        xLigneGauche[0] = model.intVar("x_" + 0, (int) startPoint.x - OFFSET, (int) startPoint.x + OFFSET);
        yLigneGauche[0] = model.intVar("y_" + 0, (int) startPoint.y - OFFSET, (int) startPoint.y + OFFSET);
        xLigneGauche[L] = model.intVar("x_" + L, (int) endPoint.x - OFFSET, (int) endPoint.x + OFFSET);
        yLigneGauche[L] = model.intVar("y_" + L, (int) endPoint.y - OFFSET, (int) endPoint.y + OFFSET);

        //contrainte sur le coin HautGauche pour qu'il soit a une longueur de CraftConfig.lengthNormal
        model.arithm(xLigneGauche[0].sub(xLigneGauche[L]).pow(2).add(yLigneGauche[0].sub(yLigneGauche[L]).pow(2))
                             .intVar(), "=", (int) (CraftConfig.lengthNormal * CraftConfig.lengthNormal)).post();


//      FORMULE xLigne[i] = xLigne[0] + (xLigne[-1]-xLigne[0])/i
        for (int i = 1; i < NBPOINTS - 1; i++) {
            xLigneGauche[i] = model.intVar("x_" + i, xLigneGauche[L].sub(xLigneGauche[0]).div(NBPOINTS - 1).mul(i)
                    .add(xLigneGauche[0]).intVar());
            yLigneGauche[i] = model.intVar("y_" + i, yLigneGauche[L].sub(yLigneGauche[0]).div(NBPOINTS - 1).mul(i)
                    .add(yLigneGauche[0]).intVar());
            xLigneGauche[i].eq(xLigneGauche[L].sub(xLigneGauche[0]).div(NBPOINTS - 1).mul(i).add(xLigneGauche[0]))
                    .post();
            yLigneGauche[i].eq(yLigneGauche[L].sub(yLigneGauche[0]).div(NBPOINTS - 1).mul(i).add(yLigneGauche[0]))
                    .post();
        }


        // points de la ligne du dessous du bit
        xLigneDroite = new IntVar[NBPOINTS];
        yLigneDroite = new IntVar[NBPOINTS];

        Vector2 coinBasGauche = startPoint;//calculés tout seul
        Vector2 coinHautGauche = endPoint;

        // on veut calculer la position du coinBasDroite et du coinHautDroite
        Vector2 collinear = coinHautGauche.sub(coinBasGauche);
        int xCollinear = (int) (coinHautGauche.x - coinBasGauche.x);
        int yCollinear = (int) (coinHautGauche.y - coinBasGauche.y);

        Vector2 ortho = collinear.getCWAngularRotated();
        int xOrtho = -yCollinear;
        int yOrtho = xCollinear;

        Vector2 coinBasDroite = coinBasGauche.add(ortho.mul(CraftConfig.bitWidth));
        xLigneDroite[0] = xLigneGauche[0].add((int) (xOrtho * CraftConfig.bitWidth)).intVar();
        yLigneDroite[0] = yLigneGauche[0].add((int) (yOrtho * CraftConfig.bitWidth)).intVar();
        Vector2 coinHautDroite = coinHautGauche.add(ortho.mul(CraftConfig.bitWidth));
        xLigneDroite[L] = xLigneGauche[L].add((int) (xOrtho * CraftConfig.bitWidth)).intVar();
        yLigneDroite[L] = yLigneGauche[L].add((int) (yOrtho * CraftConfig.bitWidth)).intVar();

        //post des contraintes
        xLigneDroite[0].eq(xLigneGauche[0].add((int) (xOrtho * CraftConfig.bitWidth))).post();
        yLigneDroite[0].eq(yLigneGauche[0].add((int) (yOrtho * CraftConfig.bitWidth))).post();
        xLigneDroite[L].eq(xLigneGauche[L].add((int) (xOrtho * CraftConfig.bitWidth))).post();
        yLigneDroite[L].eq(yLigneGauche[L].add((int) (yOrtho * CraftConfig.bitWidth))).post();


        //      FORMULE xLigne[i] = xLigne[0] + (xLigne[-1]-xLigne[0])/i
        for (int i = 1; i < NBPOINTS - 1; i++) {
            xLigneDroite[i] = model.intVar("x_" + i, xLigneDroite[L].sub(xLigneDroite[0]).div(NBPOINTS - 1).mul(i)
                    .add(xLigneDroite[0]).intVar());
            yLigneDroite[i] = model.intVar("y_" + i, yLigneDroite[L].sub(yLigneDroite[0]).div(NBPOINTS - 1).mul(i)
                    .add(yLigneDroite[0]).intVar());
            xLigneDroite[i].eq(xLigneDroite[L].sub(xLigneDroite[0]).div(NBPOINTS - 1).mul(i).add(xLigneDroite[0]))
                    .post();
            yLigneDroite[i].eq(yLigneDroite[L].sub(yLigneDroite[0]).div(NBPOINTS - 1).mul(i).add(yLigneDroite[0]))
                    .post();
        }


        //variables des points de la section
        IntVar[] xSection = new IntVar[NBPOINTS];
        IntVar[] ySection = new IntVar[NBPOINTS];
        for (int i = 0; i < NBPOINTS; i++) {
            xSection[i] = model.intVar("xS" + i, (int) sectionPoints.get(i).x);
            ySection[i] = model.intVar("yS" + i, (int) sectionPoints.get(i).y);
        }

        //la distance entre les extrémités doit être de la longueur d'un bit//todo remettre ?
//        model.arithm(xLigne[0].sub(xSection[0]).pow(2).add(yLigne[0].sub(ySection[0]).pow(2)).abs().intVar(),
//                ">=",
//                (int) CraftConfig.lengthNormal).post();//todo lengthNormal ou lengthFull ?

        //on veut que les distances xLigne des points de la ligne et les points de la section soient égales entre elles
        //--> trouve pas de solution...
        for (int i = 0; i < NBPOINTS - 1; i++) {
//           model.arithm(xLigne[i+1].sub(xLigne[i]).intVar(),"=",xSection[i+1].sub(xSection[i]).intVar()).post();
        }


        //variables des distances ligne-section
        IntVar[] distances = new IntVar[NBPOINTS];
        for (int i = 0; i < NBPOINTS; i++) {
            IntVar distX = model.intVar("D" + i + "xLigne", xLigneGauche[i].sub(xSection[i]).intVar());
            IntVar distY = model.intVar("D" + i + "yLigne", yLigneGauche[i].sub(ySection[i]).intVar());
            distX.eq(xLigneGauche[i].sub(xSection[i]).intVar()).post();
            distY.eq(yLigneGauche[i].sub(ySection[i]).intVar()).post();
//            model.arithm(distX, ">=", 0).post();
//            model.arithm(distY, ">=", 0).post();//todo ok ? --> pas de solution si <= pour les deux
//            model.arithm(xLigne[i],"<=",xSection[i]).post();
            model.arithm(yLigneGauche[i], "<", ySection[i])
                    .post();//todo on enleve la contrainte, mais on change la fonction objectif
//            model.arithm(yLigneDroite[i], ">", ySection[i]).post();

            //on crée des variables intermédiaires des distancesX et Y qui sont les valeurs absolues
            IntVar distXAbs = model.intVar("D" + i + "xAbs", distX.abs().intVar());
            IntVar distYAbs = model.intVar("D" + i + "yAbs", distY.abs().intVar());
            distXAbs.eq(distX.abs().intVar()).post();
            distYAbs.eq(distY.abs().intVar()).post();

            distances[i] = model.intVar("D" + i, distXAbs.add(distYAbs).intVar()); //OK
            distances[i].eq(distXAbs.add(distYAbs).intVar()).post();//OK attention on n'a pas les carrés et la racine
        }

        //variables représentant si un point de la section est compris entre les deux lignes
        BoolVar[] sectionInLigneX = new BoolVar[NBPOINTS];
        BoolVar[] sectionInLigneY = new BoolVar[NBPOINTS];
        BoolVar[] sectionInLigne = new BoolVar[NBPOINTS];
        for (int i = 0; i < NBPOINTS; i++) {
            sectionInLigneX[i] = model.boolVar("SX" + i + "inLigne");
            sectionInLigneY[i] = model.boolVar("SY" + i + "inLigne");
            //on vérifie que le pointX de la section est entre les points des deux lignes
            sectionInLigneX[i].eq(xSection[i].ge(xLigneGauche[i]).and(xSection[i].le(xLigneDroite[i])).intVar()).post();
            //on vérifie que le pointY de la section est entre les points des deux lignes
            sectionInLigneY[i].eq(ySection[i].ge(yLigneGauche[i]).and(ySection[i].le(yLigneDroite[i])).intVar()).post();
            //on vérifie que le point de la section est entre les deux lignes
            sectionInLigne[i] = model.boolVar("S" + i + "inLigne");
            sectionInLigne[i].eq(sectionInLigneX[i].and(sectionInLigneY[i]).intVar()).post();
        }


        //variable somme des distances
        IntVar SUM = model.intVar("SUM", 0, 1000);
        //variable somme des points de la section qui sont compris entre les deux lignes
        IntVar SUM2 = model.intVar("SUM2", 0, 10000);
        model.sum(sectionInLigne, "=", SUM2).post();
        //sommer les valeurs absolues des distances
        model.sum(distances, "=", SUM).post();
        IntVar SUMFINAL = model.intVar("SUMFINAL", 0, 2000);
        SUMFINAL.eq(SUM.mul(100 - COEFF).sub(SUM2.mul(COEFF)).intVar()).post();

        //la distance du coin de la lamelle au point de départ de la section doit être inférieur à la largeur de la lamelle
//        model.distance(xLigne[0], xSection[0], "<", (int) CraftConfig.bitWidth).post();//todo voir après


        //problème de minimisation de la somme des distances
        model.setObjective(Model.MINIMIZE, SUMFINAL); //TODO on veut maintenant que le nombre de points de la ligne dedans soit maximisé
        return model;
    }
}
