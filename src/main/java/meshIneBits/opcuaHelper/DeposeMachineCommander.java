
package meshIneBits.opcuaHelper;

import meshIneBits.Mesh;
import meshIneBits.util.CustomLogger;

public class DeposeMachineCommander {

    private final CustomLogger logger = new CustomLogger(this.getClass());
    private final Mesh mesh;
    private DeposeMachineOPCUAHelper test=new DeposeMachineOPCUAHelper();

    public DeposeMachineCommander(Mesh mesh) {
        this.mesh = mesh;
    }

    public DeposeMachineCommander() {

        mesh = null;
    }

    public void deposeStart() throws Exception {
        ICustomResponse res = test.deposeStart();
        if (res.getStatusCode() != CustomStatusCode.STATUS_GOOD) {
            logger.logERRORMessage(res.getMessage());
            throw new Exception("Error of sending request to server :" + test.getEndpointUrl());
        } else {
            logger.logINFOMessage("Starting...");
        }
    }
    public void gearMethode(boolean b) throws Exception {
        ICustomResponse res = test.gearMethode(b);
        if (res.getStatusCode() != CustomStatusCode.STATUS_GOOD) {
            logger.logERRORMessage(res.getMessage());
            throw new Exception("Error of sending request to server :" + test.getEndpointUrl());
        } else {
            logger.logINFOMessage("Starting...");
        }
    }
    public void setPosition(double p) throws Exception {
        ICustomResponse res = test.setPosition(p);
        if (res.getStatusCode() != CustomStatusCode.STATUS_GOOD) {
            logger.logERRORMessage(res.getMessage());
            throw new Exception("Error of sending request to server :" + test.getEndpointUrl());
        } else {
            logger.logINFOMessage("Starting...");
        }
    }

    public double getPosition() throws Exception {
        ICustomResponse res = test.getPosition();
        if (res.getStatusCode() != CustomStatusCode.STATUS_GOOD) {
            logger.logERRORMessage(res.getMessage());
            throw new Exception(
                    "Error of sending request to server :" + test.getEndpointUrl() + ", status code: "
                            + res.getStatusCode());
        } else {
            logger.logINFOMessage("Starting...");
            if (res.getValue() instanceof Double) {
              //  System.out.println(res.getValue());
                return (double) res.getValue();
            } else {
                throw new Exception(
                        "Value returned must be boolean type, Type of obj actual: " + res.getTypeValue());
            }
        }
    }
    public void setVitesse(short v) throws Exception {
        ICustomResponse res = test.setVitesse(v);
        if (res.getStatusCode() != CustomStatusCode.STATUS_GOOD) {
            logger.logERRORMessage(res.getMessage());
            throw new Exception("Error of sending request to server :" + test.getEndpointUrl());
        } else {
            logger.logINFOMessage("Starting...");
        }
    }

    public short getVitesse() throws Exception {
        ICustomResponse res = test.getVitesse();
        if (res.getStatusCode() != CustomStatusCode.STATUS_GOOD) {
            logger.logERRORMessage(res.getMessage());
            throw new Exception(
                    "Error of sending request to server :" + test.getEndpointUrl() + ", status code: "
                            + res.getStatusCode());
        } else {
            logger.logINFOMessage("Starting...");
            if (res.getValue() instanceof Short) {
                System.out.println(res.getValue());
                return (short) res.getValue();
            } else {
                throw new Exception(
                        "Value returned must be boolean type, Type of obj actual: " + res.getTypeValue());
            }
        }
    }
    public void moveX1() throws Exception {
        ICustomResponse res = test.moveX1();
        if (res.getStatusCode() != CustomStatusCode.STATUS_GOOD) {
            logger.logERRORMessage(res.getMessage());
            throw new Exception("Error of sending request to server :" + test.getEndpointUrl());
        } else {
            logger.logINFOMessage("Starting...");
        }
    }
    public void moveX2() throws Exception {
        ICustomResponse res = test.moveX2();
        if (res.getStatusCode() != CustomStatusCode.STATUS_GOOD) {
            logger.logERRORMessage(res.getMessage());
            throw new Exception("Error of sending request to server :" + test.getEndpointUrl());
        } else {
            logger.logINFOMessage("Starting...");
        }
    }
    public void deposeMove() throws Exception {
        ICustomResponse res = test.deposeMove();
        if (res.getStatusCode() != CustomStatusCode.STATUS_GOOD) {
            logger.logERRORMessage(res.getMessage());
            throw new Exception("Error of sending request to server :" + test.getEndpointUrl());
        } else {
            logger.logINFOMessage("Starting...");
        }
    }
    public void deposeSynchro() throws Exception {
        ICustomResponse res = test.deposeSynchro();
        if (res.getStatusCode() != CustomStatusCode.STATUS_GOOD) {
            logger.logERRORMessage(res.getMessage());
            throw new Exception("Error of sending request to server :" + test.getEndpointUrl());
        } else {
            logger.logINFOMessage("Starting...");
        }
    }




    public static void main(String[] args){
        DeposeMachineCommander test=new DeposeMachineCommander();
        int i=0;
        try {
            while(true){
                i++;
                System.out.println(test.getPosition());
                test.setPosition(i);
            System.out.println(test.getPosition());
//            i++;
//            test.setPosition(i);
//            System.out.println(test.getPosition());
            }
//s
//            test.moveX1();
//            test.deposeMove();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
