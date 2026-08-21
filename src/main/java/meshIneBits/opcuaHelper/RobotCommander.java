
/*------------------------------------------------------------------------------
 -  MeshIneBits is a Java software to disintegrate a 3d mesh (model in .stl)
 -  into a network of standard parts (called "Bits").
 -
 -  Copyright (C) 2015-2026 DANIEL Laurent.
 -  Copyright (C) 2015 Cédric Siourakhan
 -  Copyright (C) 2016 Gabriel Magny
 -  Copyright (C) 2016  CASSARD Thibault & GOUJU Nicolas.
 -  Copyright (C) 2017-2018  TRAN Quoc Nhat Han.
 -  Copyright (C) 2018 VALLON Benjamin.
 -  Copyright (C) 2018 LORIMER Campbell.
 -  Copyright (C) 2018 D'AUTUME Christian.
 -  Copyright (C) 2019 DURINGER Nathan (Tests).
 -  Copyright (C) 2020-2021 CLAIRIS Etienne & RUSSO André.
 -  Copyright (C) 2020-2021 DO Quang Bao.
 -  Copyright (C) 2021 VANNIYASINGAM Mithulan.
 -  Copyright (C) 2021  Quang Long
 -  Copyright (C) 2022 Mhamad Atlab
 -  Copyright (C) 2022 Majed Hlaihel
 -  Copyright (C) 2026 Aymeric Sirejol
 -  Copyright (C) 2026 Hugo BENOIT
 -
 -  This program is free software: you can redistribute it and/or modify
 -  it under the terms of the GNU General Public License as published by
 -  the Free Software Foundation, either version 3 of the License, or
 -  (at your option) any later version.
 -
 -  This program is distributed in the hope that it will be useful,
 -  but WITHOUT ANY WARRANTY; without even the implied warranty of
 -  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 -  GNU General Public License for more details.
 -
 -  You should have received a copy of the GNU General Public License
 -  along with this program. If not, see <https://www.gnu.org/licenses/>.
 -----------------------------------------------------------------------------*/

package meshIneBits.opcuaHelper;

import meshIneBits.util.CustomLogger;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class RobotCommander {


    private final CustomLogger logger = new CustomLogger(this.getClass());
    private RobotOPCUAHelper robotOPCUAHelper;


    public RobotCommander(ROBOT robot)  {//robot = manip ou decoupe
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
        try {
           // RobotCommander robotManip=new RobotCommander(ROBOT.MANIP);
            RobotCommander robotDecoup=new RobotCommander(ROBOT.DECOUPE);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

/*

        try {
            Map<Integer,Short> map;
            //int i=0;
            while(true){

                    while (robotDecoup.getHoldingRegisters()[2]!=1){//wait R[3] = 1 --> fin de programme 1 de robot Decoupe
                    }                                               // R[3] variable de robot decoupe
        //            map= new HashMap<>();
        //            map.put(1, (short) 0);
        //            robotDecoup.setHoldingRegisters(map);

                    map= new HashMap<>();
                    map.put(3, (short) 1);
                    robotManip.setHoldingRegisters(map);//R[3] de robot manip = 1
                      while (robotManip.getHoldingRegisters()[2]!=0){// wait R[3] = 0 --> fin de programme 1 de robot manip
                      }                                               // R[3] variable de robot manip

        //            while (robotManip.getHoldingRegisters()[2]!=0){
        //            }
                    map= new HashMap<>();
                    map.put(3, (short) 0);
                    robotDecoup.setHoldingRegisters(map);//R[3] de robot decoupe = 0

                    while (robotDecoup.getHoldingRegisters()[2]!=2){//wait R[3] = 2 --> fin de programme 2 de robot Decoupe
                    }                                               // R[3] variable de robot decoupe
                    map= new HashMap<>();
                    map.put(3, (short) 2);
                    robotManip.setHoldingRegisters(map);//R[3] de robot manip = 2

                    while (robotManip.getHoldingRegisters()[2]!=0){// wait R[3] = 0 --> fin de programme 2 de robot manip
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

