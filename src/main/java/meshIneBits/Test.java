package meshIneBits;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PShape;
import processing.event.MouseEvent;

public class Test extends PApplet {PShape rec;
    float Xp=0,Yp=0;
    Thread t;
    @Override
    public void settings() {
        size(500,500,P2D);
    }

    public void setup(){System.out.println("ThreadSetup="+Thread.currentThread().getName());
         rec=this.createShape();

        rec.beginShape();

        rec.vertex(50,50);
        rec.vertex(50,100);
        rec.vertex(100,100);
    rec.vertex(100,50);
    rec.endShape(PConstants.CLOSE);

    }
    public void draw(){
background(140);
        pushMatrix();
        translate(Xp,Yp);
        shape(rec);
popMatrix();
       if(Xp<200)Xp++;
    }
public static void main(String[] args){
        PApplet.main(Test.class.getCanonicalName());
}

    @Override
    public void mouseClicked(MouseEvent event) {
        while (Xp<200){
            Xp++;

        }
    }

   /*  @Override
   public void mouseMoved() {System.out.println("Thread="+Thread.currentThread().getName());
        Xp=mouseX;
    }*/
}
