package meshIneBits.IA;

import meshIneBits.Bit2D;
import meshIneBits.IA.IA_util.Description;
import meshIneBits.slicer.Slice;
import meshIneBits.util.Segment2D;
import meshIneBits.util.Vector2;

import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Vector;

public class Training {
    private AI_Tool ai_tool;

    public Training(AI_Tool ai_tool) {
        this.ai_tool = ai_tool;
    }

    //todo finalement on va ajouter les points bruts au fichier
    public void trainNN() {
        //todo appeller les fonctions de traitement de points
        //placeBitsOnSlices(sliceMap);
        //todo ajouter les nouveaux exemples au réseau pour l'entrainer
    }

}
