package meshIneBits.util;

import meshIneBits.Bit2D;
import meshIneBits.Bit3D;
import meshIneBits.Layer;
import meshIneBits.Mesh;

import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class OptimizedMesh {
    private Mesh mesh;
    private Map<Integer, List<Bit3D>> mapBitNotFullLength;
    private List<Map<Bit3D, List<Bit3D>>> listToOptimized;
    private List<Layer> layers;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    public OptimizedMesh() {
    }
    public OptimizedMesh updateMesh(Mesh mesh){
        this.mesh = mesh;
        layers = mesh.getLayers();
        return this;
    }

    public void optimize() {
        if(mesh==null)throw new NullPointerException("mesh is null");
        executorService.execute(() -> {
            for (Layer layer : layers) {
                if (!layer.isPaved()) continue;
                System.out.println("layer "+layers.indexOf(layer));
                for (Bit3D bit3D : layer.getAllBit3D()) {
                    if (!bit3D.getBaseBit().isFullLength()) {
                        Set<Bit3D> bits = new HashSet<>();
                        Bit2D bit2D = bit3D.getBaseBit();
                        Vector2 orientation = bit2D.getOrientation();
                        Vector2 origin = bit2D.getOrigin();
//                        double line = origin.x*orientation.x+origin.y*orientation.y;
                        double a = orientation.x*origin.x+orientation.y*origin.y;
                        System.out.println("bit not full length: orientation "+orientation.toString()+", origin: "+origin.toString());
                        for (Bit3D bit : layer.getAllBit3D()) {
                            double result=-1;
                            boolean b = false;
                            if(bit.getOrientation().asGoodAsEqual(orientation)){
                                if(orientation.x==0){
                                    result = bit.getOrigin().x-origin.x;
                                    b = orientation.y*(bit.getOrigin().y-origin.y)>0;
//                                    if(result<=0.0001&&result>=-0.0001&&b){
//                                        layer.scaleBit(bit3D,50,100);
//                                        layer.moveBit(bit,new Vector2(-1,0));
//                                    }
                                }else if(orientation.x==1&&orientation.y==0){
                                    result = bit.getOrigin().y-origin.y;
                                    b = orientation.x*(bit.getOrigin().x-origin.x)>0;
                                }
                                if(result<=0.0001&&result>=-0.0001&&b){
                                    System.out.println("bit on line : orientation "+bit.getOrientation().toString()+", origin: "+bit.getOrigin().toString());
                                    bits.add(bit);
                                }
                            }
                        }
                        if(bits.size()>0){
                            if(orientation.x==0){
                                layer.scaleBit(bit3D,50,100);
                                layer.moveBits(bits,new Vector2(orientation.y>0?-orientation.y : orientation.y,0));
                            }else if(orientation.y==0){
                                layer.scaleBit(bit3D,50,100);
                                layer.moveBits(bits,new Vector2(orientation.x>0?-orientation.x : orientation.x,0));
                            }
                        }
                    }
                }

            }
        });
    }
}
