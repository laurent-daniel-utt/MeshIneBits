package meshIneBits;

import meshIneBits.config.CraftConfig;

import java.util.ArrayList;

import static meshIneBits.config.CraftConfig.printerX;
import static meshIneBits.config.CraftConfig.printerY;

public class Strip {
private double Xposition=-printerX / 2 - CraftConfig.workingWidth - 20;
private double Yposition=-printerY / 2;
private double Zposition;
private ArrayList<Bit3D> bits=new ArrayList<Bit3D>();
private Float width= CraftConfig.workingWidth;
private Float length= printerY;
private Layer layer;
    public Strip(Bit3D firstbit,Layer layer){
this.Xposition=firstbit.getTwoExtremeXPointsCS().get(0).x;
System.out.println("Xposition="+Xposition);
this.Zposition=firstbit.getLowerAltitude();
//bits.add(firstbit);
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
