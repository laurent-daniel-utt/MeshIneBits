
package meshIneBits.opcuaHelper;

import meshIneBits.util.CustomLogger;

import java.io.IOException;
import java.util.*;

public class RobotCommander {


    private final CustomLogger logger = new CustomLogger(this.getClass());
    private RobotOPCUAHelper robotOPCUAHelper;


    public RobotCommander(ROBOT robot) {//robot = manip ou decoupe
                                        //pour savoir quel robot on utilise
        this.robotOPCUAHelper=new RobotOPCUAHelper(robot);
    }

    public short[] getHoldingRegisters() throws Exception {
        ICustomResponse res = robotOPCUAHelper.getHoldingRegisters();
        if (res.getStatusCode() != CustomStatusCode.STATUS_GOOD) {
            logger.logERRORMessage(res.getMessage());
            throw new Exception(
                    "Error of sending request to server :" + robotOPCUAHelper.getEndpointUrl() + ", status code: "
                            + res.getStatusCode());
        } else {
            String s=Arrays.toString((Object[]) res.getValue());//lire les valeurs sous forme String=[0, 0, ...]
            return stringToShort(s);
        }
    }

    public void setHoldingRegisters(Map<Integer, Short> map) throws Exception {
        short[] arr=getHoldingRegisters();
        int index;
        short var;

        Set<Integer> setCodes = map.keySet();
        Iterator<Integer> iterator = setCodes.iterator();

        while (iterator.hasNext()) {
            index = iterator.next();
            var = map.get(index);
            arr[index]=var;
        }

        ICustomResponse res = robotOPCUAHelper.setHoldingRegisters(arr);
        if (res.getStatusCode() != CustomStatusCode.STATUS_GOOD) {
            logger.logERRORMessage(res.getMessage());
            throw new Exception("Error of sending request to server :" + robotOPCUAHelper.getEndpointUrl());
        } else {
            logger.logINFOMessage("Starting...");
        }
    }

    private short[] stringToShort(String s){
        short[] res=new short[16384];

        StringBuilder ss=new StringBuilder(s);
        ss.deleteCharAt(0);//supprimer [
        ss.deleteCharAt(ss.length()-1);//supprimer ]
        s=ss.toString();//---> s=0, 0, ..., 0

        String[] arr=s.split(", ");
        for (int i=0;i<arr.length;i++){
            res[i]=Short.parseShort(arr[i]);
        }
        return res;
    }



    public static void main(String[] args){
        RobotCommander robotManip=new RobotCommander(ROBOT.MANIP);
        RobotCommander robotDecoup=new RobotCommander(ROBOT.DECOUPE);
/*

        try {
            Map<Integer,Short> map;
            //int i=0;
            while(true){

                    while (robotDecoup.getHoldingRegisters()[3]!=1){//wait R[3] = 1 --> fin de programme 1 de robot Decoupe
                    }                                               // R[3] variable de robot decoupe
        //            map= new HashMap<>();
        //            map.put(1, (short) 0);
        //            robotDecoup.setHoldingRegisters(map);

                    map= new HashMap<>();
                    map.put(3, (short) 1);
                    robotManip.setHoldingRegisters(map);//R[3] de robot manip = 1
                      while (robotManip.getHoldingRegisters()[3]!=0){// wait R[3] = 0 --> fin de programme 1 de robot manip
                      }                                               // R[3] variable de robot manip

        //            while (robotManip.getHoldingRegisters()[3]!=0){
        //            }
                    map= new HashMap<>();
                    map.put(3, (short) 0);
                    robotDecoup.setHoldingRegisters(map);//R[3] de robot decoupe = 0

                    while (robotDecoup.getHoldingRegisters()[3]!=2){//wait R[3] = 2 --> fin de programme 2 de robot Decoupe
                    }                                               // R[3] variable de robot decoupe
                    map= new HashMap<>();
                    map.put(3, (short) 2);
                    robotManip.setHoldingRegisters(map);//R[3] de robot manip = 2

                    while (robotManip.getHoldingRegisters()[3]!=0){// wait R[3] = 0 --> fin de programme 2 de robot manip
                    }                                              // R[3] variable de robot manip
                    map= new HashMap<>();
                    map.put(3, (short) 0);
                    robotDecoup.setHoldingRegisters(map);//R[3] de robot decoupe = 0
        //            map= new HashMap<>();
        //            map.put(0, (short) 0);
        //            robotManip.setHoldingRegisters(map);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }*/
    }
}

