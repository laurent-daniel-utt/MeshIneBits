package meshIneBits;

import meshIneBits.config.CraftConfig;

import java.io.Serializable;
import java.util.ArrayList;

import static meshIneBits.config.CraftConfig.printerX;
import static meshIneBits.config.CraftConfig.printerY;

/**
 * class représentant les Stripes,chaque stripe est caractérisé par sa position X,l'hauteur (position Z),
 *le nombre des bits qu'on peut mettre dedans et à quelle layer cette stripe appartien puisqu'on crée les stripes par layer
 * c.à.d chaque layer peut contenir plusieurs strip ou minimmum 1 et chaque stripe peut contenir plusieurs bits ou minimmum 1
 */
public class Strip implements Serializable {
private double Xposition=-printerX / 2 - CraftConfig.workingWidth - 20;

private double Zposition;
private ArrayList<Bit3D> bits=new ArrayList<Bit3D>();
private Float width= CraftConfig.workingWidth;
private Float length= printerY;
private Layer layer;
    public Strip(Bit3D firstbit,Layer layer){
this.Xposition=firstbit.getTwoExtremeXPointsCS().get(0).x;
this.Zposition=firstbit.getLowerAltitude();
this.layer=layer;
    }
public void addBit3D(Bit3D bit3D){
        bits.add(bit3D);
}

    public ArrayList<Bit3D> getBits() {
        return bits;
    }

    public double getXposition(){

        return this.Xposition;
}
}
