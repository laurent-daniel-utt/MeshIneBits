
package meshIneBits.opcuaHelper;

import meshIneBits.Mesh;
import meshIneBits.util.CustomLogger;

public class DeposeMachineCommander {

    private final CustomLogger logger = new CustomLogger(this.getClass());
    private final Mesh mesh;
    private DeposeMachineOPCUAHelper helper =new DeposeMachineOPCUAHelper();

    public DeposeMachineCommander(Mesh mesh) {
        this.mesh = mesh;
    }

    public DeposeMachineCommander() {

        mesh = null;
    }

    public void startDepose() throws Exception {
        ICustomResponse res = helper.startDepose();
        if (res.getStatusCode() != CustomStatusCode.STATUS_GOOD) {
            logger.logERRORMessage(res.getMessage());
            throw new Exception("Error of sending request to server :" + helper.getEndpointUrl());
        } else {
            logger.logINFOMessage("Starting...");
        }
    }
    public void stopDepose() throws Exception {
        ICustomResponse res = helper.stopDepose();
        if (res.getStatusCode() != CustomStatusCode.STATUS_GOOD) {
            logger.logERRORMessage(res.getMessage());
            throw new Exception("Error of sending request to server :" + helper.getEndpointUrl());
        } else {
            logger.logINFOMessage("Stop...");
        }
    }
    public void resetDepose() throws Exception {
        ICustomResponse res = helper.resetDepose();
        if (res.getStatusCode() != CustomStatusCode.STATUS_GOOD) {
            logger.logERRORMessage(res.getMessage());
            throw new Exception("Error of sending request to server :" + helper.getEndpointUrl());
        } else {
            logger.logINFOMessage("Reset...");
        }
    }
    public void continueDepose() throws Exception {
        ICustomResponse res = helper.continueDepose();
        if (res.getStatusCode() != CustomStatusCode.STATUS_GOOD) {
            logger.logERRORMessage(res.getMessage());
            throw new Exception("Error of sending request to server :" + helper.getEndpointUrl());
        } else {
            logger.logINFOMessage("Continuing...");
        }
    }
    public void pauseDepose() throws Exception {
        ICustomResponse res = helper.pauseDepose();
        if (res.getStatusCode() != CustomStatusCode.STATUS_GOOD) {
            logger.logERRORMessage(res.getMessage());
            throw new Exception("Error of sending request to server :" + helper.getEndpointUrl());
        } else {
            logger.logINFOMessage("Pausing...");
        }
    }
    public void continueAfterTurnOff() throws Exception {
        ICustomResponse res = helper.continueAfterTurnOff();
        if (res.getStatusCode() != CustomStatusCode.STATUS_GOOD) {
            logger.logERRORMessage(res.getMessage());
            throw new Exception("Error of sending request to server :" + helper.getEndpointUrl());
        } else {
            logger.logINFOMessage("Continuing after turn off...");
        }
    }
    public void continueAfterESTOP() throws Exception {
        ICustomResponse res = helper.continueAfterESTOP();
        if (res.getStatusCode() != CustomStatusCode.STATUS_GOOD) {
            logger.logERRORMessage(res.getMessage());
            throw new Exception("Error of sending request to server :" + helper.getEndpointUrl());
        } else {
            logger.logINFOMessage("Continuing after emergency stop...");
        }
    }
    public void cameraLogin() throws Exception {
        ICustomResponse res = helper.cameraLogin();
        if (res.getStatusCode() != CustomStatusCode.STATUS_GOOD) {
            logger.logERRORMessage(res.getMessage());
            throw new Exception("Error of sending request to server :" + helper.getEndpointUrl());
        } else {
            logger.logINFOMessage("Camera logging...");
        }
    }
    public void cameraCaptureImage() throws Exception {
        ICustomResponse res = helper.cameraCaptureImage();
        if (res.getStatusCode() != CustomStatusCode.STATUS_GOOD) {
            logger.logERRORMessage(res.getMessage());
            throw new Exception("Error of sending request to server :" + helper.getEndpointUrl());
        } else {
            logger.logINFOMessage("Capturing image...");
        }
    }
    public void acknowledgeError() throws Exception {
        ICustomResponse res = helper.acknowledgeError();
        if (res.getStatusCode() != CustomStatusCode.STATUS_GOOD) {
            logger.logERRORMessage(res.getMessage());
            throw new Exception("Error of sending request to server :" + helper.getEndpointUrl());
        } else {
            logger.logINFOMessage("Acknowledge error...");
        }
    }
    public String getMessageError() throws Exception {
        ICustomResponse res = helper.getMessageError();
        if (res.getStatusCode() != CustomStatusCode.STATUS_GOOD) {
            logger.logERRORMessage(res.getMessage());
            throw new Exception(
                    "Error of sending request to server :" + helper.getEndpointUrl() + ", status code: "
                            + res.getStatusCode());
        } else {
     /*          logger.logINFOMessage("Starting...");
            if (res.getValue() instanceof Double) {
                //  System.out.println(res.getValue());
                return (double) res.getValue();
            } else {
                throw new Exception(
                        "Value returned must be boolean type, Type of obj actual: " + res.getTypeValue());
            }
      */
            return res.getValue().toString();
        }
    }



    /*
    public void setPosition(double p) throws Exception {
        ICustomResponse res = helper.setPosition(p);
        if (res.getStatusCode() != CustomStatusCode.STATUS_GOOD) {
            logger.logERRORMessage(res.getMessage());
            throw new Exception("Error of sending request to server :" + helper.getEndpointUrl());
        } else {
            logger.logINFOMessage("Starting...");
        }
    }

    public double getPosition() throws Exception {
        ICustomResponse res = helper.getPosition();
        if (res.getStatusCode() != CustomStatusCode.STATUS_GOOD) {
            logger.logERRORMessage(res.getMessage());
            throw new Exception(
                    "Error of sending request to server :" + helper.getEndpointUrl() + ", status code: "
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
        ICustomResponse res = helper.setVitesse(v);
        if (res.getStatusCode() != CustomStatusCode.STATUS_GOOD) {
            logger.logERRORMessage(res.getMessage());
            throw new Exception("Error of sending request to server :" + helper.getEndpointUrl());
        } else {
            logger.logINFOMessage("Starting...");
        }
    }

    public short getVitesse() throws Exception {
        ICustomResponse res = helper.getVitesse();
        if (res.getStatusCode() != CustomStatusCode.STATUS_GOOD) {
            logger.logERRORMessage(res.getMessage());
            throw new Exception(
                    "Error of sending request to server :" + helper.getEndpointUrl() + ", status code: "
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
        ICustomResponse res = helper.moveX1();
        if (res.getStatusCode() != CustomStatusCode.STATUS_GOOD) {
            logger.logERRORMessage(res.getMessage());
            throw new Exception("Error of sending request to server :" + helper.getEndpointUrl());
        } else {
            logger.logINFOMessage("Starting...");
        }
    }
    public void moveX2() throws Exception {
        ICustomResponse res = helper.moveX2();
        if (res.getStatusCode() != CustomStatusCode.STATUS_GOOD) {
            logger.logERRORMessage(res.getMessage());
            throw new Exception("Error of sending request to server :" + helper.getEndpointUrl());
        } else {
            logger.logINFOMessage("Starting...");
        }
    }
    public void deposeMove() throws Exception {
        ICustomResponse res = helper.deposeMove();
        if (res.getStatusCode() != CustomStatusCode.STATUS_GOOD) {
            logger.logERRORMessage(res.getMessage());
            throw new Exception("Error of sending request to server :" + helper.getEndpointUrl());
        } else {
            logger.logINFOMessage("Starting...");
        }
    }
    public void deposeSynchro() throws Exception {
        ICustomResponse res = helper.deposeSynchro();
        if (res.getStatusCode() != CustomStatusCode.STATUS_GOOD) {
            logger.logERRORMessage(res.getMessage());
            throw new Exception("Error of sending request to server :" + helper.getEndpointUrl());
        } else {
            logger.logINFOMessage("Starting...");
        }
    }

*/



    public static void main(String[] args){
        DeposeMachineCommander test=new DeposeMachineCommander();
        int i=0;
        try {
            test.resetDepose();
/*
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

 */
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
