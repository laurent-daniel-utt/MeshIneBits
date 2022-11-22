package meshIneBits.util.supportImportFile;

import javafx.util.Pair;
import meshIneBits.config.CraftConfig;
import meshIneBits.util.CustomLogger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.geom.Path2D;
import java.io.IOException;
import java.util.ArrayList;

public class DomParser {
    public static final CustomLogger logger = new CustomLogger(DomParser.class);
    private static int bit_id;
    private static int numbatch=0;
    private static boolean Batch_Normal=true;

    public static int getBatch_num() {
        return numbatch;
    }




    public static ArrayList<ArrayList<Pair<FallType, Path2D.Double>>> parseXml(int batch_num){
        numbatch=batch_num;
        // this list reprensts all the nodes of <cutting> +no-cutting cases
        ArrayList<Pair<Boolean, Node>> cutting=new ArrayList<>();
        // this list reprensts all the nodes of <cut-paths> +no-cutting cases
        ArrayList<ArrayList<Pair<FallType, Path2D.Double>>> cutpaths=new ArrayList<>();
        DocumentBuilderFactory factory= DocumentBuilderFactory.newInstance();
        //this list represents 1 node of <cut-paths>
        ArrayList<Pair<FallType, Path2D.Double>> paths_by_FallType=new ArrayList<>();
        try {
            DocumentBuilder builder=factory.newDocumentBuilder();
            String num=String.valueOf(batch_num);
           // String file="\\Exported\\Classic_Pattern_Donut\\batch "+num+".xml";
            String file="\\batch "+num+".xml";
            String path=System.getProperty("user.dir");
            System.getProperty("user.dir");
            logger.logDEBUGMessage("file:"+path+file);
            Document doc= builder.parse(path+file);
            NodeList bitlist=doc.getElementsByTagName("bit");
            System.out.println("bitlist size:"+bitlist.getLength());//72 if batch is full
            for(int j=0;j<bitlist.getLength();j++){
                Node bit_node=bitlist.item(j);
                if(bit_node.getNodeType()==Node.ELEMENT_NODE) {
                    Element bit=(Element) bit_node;

                    NodeList bitchilds=bit.getChildNodes();
                    for(int i=0 ;i<bitchilds.getLength();i++){
                        if(bitchilds.item(i).getNodeType()==Node.ELEMENT_NODE){
                            Element el=(Element) bitchilds.item(i);
                            if(el.getTagName().equals("cutting") ){
                                cutting.add(new Pair<>(true,bitchilds.item(i)));
                            }else if (el.getTagName().equals("no-cutting")) {
                                cutting.add(new Pair<>(false,bitchilds.item(i)));
                            }
                        }
                    }
                }
            }logger.logDEBUGMessage("cutting:"+cutting.size());
            for(int k=0;k<cutting.size();k++){
                if(cutting.get(k).getKey()){
                    NodeList cutpathsNodes= cutting.get(k).getValue().getChildNodes();
                    // iterating through nodes
                    for(int l=0;l<cutpathsNodes.getLength();l++){
                        Node cutpathnode=cutpathsNodes.item(l);
                        if(cutpathnode.getNodeType()== Node.ELEMENT_NODE){
                            Element cutType=(Element) cutpathnode;
                            NodeList childes=cutType.getChildNodes();
                           // just for initializing puroposes
                            FallType type=FallType.Default;
                            Path2D.Double pathcut_by_FallType=new Path2D.Double();
                            //iterating in a Node
                            for (int ch=0;ch<childes.getLength();ch++){
                                if(((Element) childes.item(ch)).getTagName().equals("fall-type")){
                                    //if path is not empty
                                    if(! pathcut_by_FallType.getPathIterator(null).isDone()) {
                                        pathcut_by_FallType.closePath();
                                        paths_by_FallType.add(new Pair<>(type,pathcut_by_FallType));
                                    }/*else {paths_by_FallType.add(new Pair<>(type,pathcut_by_FallType));
                                        paths_by_FallType=new ArrayList<>();
                                    }*/
                                     pathcut_by_FallType=new Path2D.Double();
                                    String type_str=((Element) childes.item(ch)).getTextContent();

                                    if(type_str.equals("chute"))type=FallType.Chute;
                                    else {type=FallType.Subbit;}
                                    //case of Drop
                                    if (ch==childes.getLength()-2) {System.out.println("Drop");
                                       // paths_by_FallType.add(new Pair<>(type,pathcut_by_FallType));
                                        pathcut_by_FallType=new Path2D.Double();
                                    type=FallType.Sub_Drop;
                                    }

                                }
                                else if(((Element) childes.item(ch)).getTagName().equals("move-to")){
                                    NodeList xy=   childes.item(ch).getChildNodes();
                                    Double x=  Double.valueOf( ((Element)xy.item(0)).getTextContent()) ;
                                    Double y=  Double.valueOf( ((Element)xy.item(1)).getTextContent()) ;
                                    //to make sure the cutting will be done we adjust a bit the positions
                                    // because sometimes the PathIterator doesnt see it
                                    x= adjustValue(x);
                                    if(x>=CraftConfig.lengthFull/2)Batch_Normal=false;
                                    y=  adjustValue(y);
                                    pathcut_by_FallType.moveTo(x,y);
                                } else if (((Element) childes.item(ch)).getTagName().equals("cut-to")) {
                                    NodeList xy=   childes.item(ch).getChildNodes();
                                    Double x=  Double.valueOf( ((Element)xy.item(0)).getTextContent()) ;
                                    Double y=  Double.valueOf( ((Element)xy.item(1)).getTextContent()) ;
                                    //to make sure the cutting will be done we adjust a bit the positions
                                    x= adjustValue(x);
                                    y= adjustValue(y);
                                if(x>=CraftConfig.lengthFull/2)Batch_Normal=false;
                                    pathcut_by_FallType.lineTo(x,y);

                                }

                            }if(!pathcut_by_FallType.getPathIterator(null).isDone())pathcut_by_FallType.closePath();
                            //ADDING THE LAST PATH WHEN CHILDES REACH THE OVER
                            paths_by_FallType.add(new Pair<>(type,pathcut_by_FallType));
                            cutpaths.add(paths_by_FallType);
                            paths_by_FallType=new ArrayList<>();
                               }
                             }
                }else { Path2D.Double pathcut=new Path2D.Double();
                    paths_by_FallType=new ArrayList<>();
                    paths_by_FallType.add(new Pair<>(FallType.Nocutting,pathcut));
                    cutpaths.add(paths_by_FallType);
                    paths_by_FallType=new ArrayList<>();
                }

            }logger.logDEBUGMessage("number of cutpaths(no-cutting included):"+cutpaths.size());
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }
        System.out.println("Somme="+cutpaths.stream().mapToInt(ArrayList::size).sum());
        if(Batch_Normal)logger.logDEBUGMessage("Values in Batch seems good");
        else logger.logDEBUGMessage("Values in Batch doesn't seem good, x in cut-path Node can't be 80 because it's the" +
                " holding area ");
        return cutpaths;
    }








    private static double adjustValue(double value){
        double mlength=(Math.signum(value)*CraftConfig.lengthFull-Math.signum(value)*CraftConfig.incertitude)/2;
        double mwidth=(Math.signum(value)*CraftConfig.bitWidth-Math.signum(value)*CraftConfig.incertitude)/2;
        if(Math.abs(value-mlength) <= (Math.abs(CraftConfig.lengthFull/2-mlength))
                || Math.abs(value-mwidth) <= (Math.abs(CraftConfig.bitWidth/2-mwidth)))
        {
            value=value+Math.signum(value)*(20*CraftConfig.incertitude);
        }
        return   value;
    }


}
