
package meshIneBits.opcuaHelper;


import meshIneBits.Mesh;
import meshIneBits.util.CustomLogger;

public class DepositingMachineCommander {

    private final CustomLogger logger = new CustomLogger(this.getClass());
    private final Mesh mesh;
    //private final DepositingMachineOPCUAHelper helper=new DepositingMachineOPCUAHelper();
    private  DepositingMachineOPCUAHelper helper;

    public DepositingMachineCommander(Mesh mesh)  {
           helper =new DepositingMachineOPCUAHelper();
        this.mesh = mesh;
    }

    public DepositingMachineCommander() {

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
            if (res.getValue() instanceof String) {
            } else {
                throw new Exception(
                        "Value returned must be String type, Type of obj actual: " + res.getTypeValue());
            }

            return res.getValue().toString();
        }

    }
    public boolean getLockContinueButton() throws Exception {
        ICustomResponse res = helper.getLockContinueButton();
        if (res.getStatusCode() != CustomStatusCode.STATUS_GOOD) {
            logger.logERRORMessage(res.getMessage());
            throw new Exception(
                    "Error of sending request to server :" + helper.getEndpointUrl() + ", status code: "
                            + res.getStatusCode());
        } else {
            if (res.getValue() instanceof Boolean) {
            } else {
                throw new Exception(
                        "Value returned must be Boolean type, Type of obj actual: " + res.getTypeValue());
            }
            return (boolean)res.getValue();
        }
    }
    public double getXCurrentPosition() throws Exception {
        ICustomResponse res = helper.getXCurrentPosition();
        if (res.getStatusCode() != CustomStatusCode.STATUS_GOOD) {
            logger.logERRORMessage(res.getMessage());
            throw new Exception(
                    "Error of sending request to server :" + helper.getEndpointUrl() + ", status code: "
                            + res.getStatusCode());
        } else {
            if (res.getValue() instanceof Double) {
            } else {
                throw new Exception(
                        "Value returned must be Double type, Type of obj actual: " + res.getTypeValue());
            }
            return (double)res.getValue();
        }
    }
    public double getZCurrentPosition() throws Exception {
        ICustomResponse res = helper.getZCurrentPosition();
        if (res.getStatusCode() != CustomStatusCode.STATUS_GOOD) {
            logger.logERRORMessage(res.getMessage());
            throw new Exception(
                    "Error of sending request to server :" + helper.getEndpointUrl() + ", status code: "
                            + res.getStatusCode());
        } else {
            if (res.getValue() instanceof Double) {
            } else {
                throw new Exception(
                        "Value returned must be Double type, Type of obj actual: " + res.getTypeValue());
            }
            return (double)res.getValue();
        }
    }
    public double getYCurrentPosition() throws Exception {
        ICustomResponse res = helper.getYCurrentPosition();
        if (res.getStatusCode() != CustomStatusCode.STATUS_GOOD) {
            logger.logERRORMessage(res.getMessage());
            throw new Exception(
                    "Error of sending request to server :" + helper.getEndpointUrl() + ", status code: "
                            + res.getStatusCode());
        } else {
            if (res.getValue() instanceof Double) {
            } else {
                throw new Exception(
                        "Value returned must be Double type, Type of obj actual: " + res.getTypeValue());
            }
            return (double)res.getValue();
        }
    }
    public double getSubXCurrentPosition() throws Exception {
        ICustomResponse res = helper.getSubXCurrentPosition();
        if (res.getStatusCode() != CustomStatusCode.STATUS_GOOD) {
            logger.logERRORMessage(res.getMessage());
            throw new Exception(
                    "Error of sending request to server :" + helper.getEndpointUrl() + ", status code: "
                            + res.getStatusCode());
        } else {
            if (res.getValue() instanceof Double) {
            } else {
                throw new Exception(
                        "Value returned must be Double type, Type of obj actual: " + res.getTypeValue());
            }
            return (double)res.getValue();
        }
    }
    public double getSubZCurrentPosition() throws Exception {
        ICustomResponse res = helper.getSubZCurrentPosition();
        if (res.getStatusCode() != CustomStatusCode.STATUS_GOOD) {
            logger.logERRORMessage(res.getMessage());
            throw new Exception(
                    "Error of sending request to server :" + helper.getEndpointUrl() + ", status code: "
                            + res.getStatusCode());
        } else {
            if (res.getValue() instanceof Double) {
            } else {
                throw new Exception(
                        "Value returned must be Double type, Type of obj actual: " + res.getTypeValue());
            }
            return (double)res.getValue();
        }
    }
    public double getThetaCurrentPosition() throws Exception {
        ICustomResponse res = helper.getThetaCurrentPosition();
        if (res.getStatusCode() != CustomStatusCode.STATUS_GOOD) {
            logger.logERRORMessage(res.getMessage());
            throw new Exception(
                    "Error of sending request to server :" + helper.getEndpointUrl() + ", status code: "
                            + res.getStatusCode());
        } else {
            if (res.getValue() instanceof Double) {
            } else {
                throw new Exception(
                        "Value returned must be Double type, Type of obj actual: " + res.getTypeValue());
            }
            return (double)res.getValue();
        }
    }
    public long getCurrentBitId() throws Exception {
        ICustomResponse res = helper.getCurrentBitId();
        if (res.getStatusCode() != CustomStatusCode.STATUS_GOOD) {
            logger.logERRORMessage(res.getMessage());
            throw new Exception(
                    "Error of sending request to server :" + helper.getEndpointUrl() + ", status code: "
                            + res.getStatusCode());
        } else {
            if (res.getValue() instanceof Long) {
            } else {
                throw new Exception(
                        "Value returned must be Long type, Type of obj actual: " + res.getTypeValue());
            }
            return (long)res.getValue();
        }
    }
    public long getCurrentBitIdInBatch() throws Exception {
        ICustomResponse res = helper.getCurrentBitIdInBatch();
        if (res.getStatusCode() != CustomStatusCode.STATUS_GOOD) {
            logger.logERRORMessage(res.getMessage());
            throw new Exception(
                    "Error of sending request to server :" + helper.getEndpointUrl() + ", status code: "
                            + res.getStatusCode());
        } else {
            if (res.getValue() instanceof Long) {
            } else {
                throw new Exception(
                        "Value returned must be Integer type, Type of obj actual: " + res.getTypeValue());
            }
            return (long)res.getValue();
        }
    }
    public double getCurrentBitX() throws Exception {
        ICustomResponse res = helper.getCurrentBitX();
        if (res.getStatusCode() != CustomStatusCode.STATUS_GOOD) {
            logger.logERRORMessage(res.getMessage());
            throw new Exception(
                    "Error of sending request to server :" + helper.getEndpointUrl() + ", status code: "
                            + res.getStatusCode());
        } else {
            if (res.getValue() instanceof Double) {
            } else {
                throw new Exception(
                        "Value returned must be Double type, Type of obj actual: " + res.getTypeValue());
            }
            return (double)res.getValue();
        }
    }
    public double getCurrentBitZ() throws Exception {
        ICustomResponse res = helper.getCurrentBitZ();
        if (res.getStatusCode() != CustomStatusCode.STATUS_GOOD) {
            logger.logERRORMessage(res.getMessage());
            throw new Exception(
                    "Error of sending request to server :" + helper.getEndpointUrl() + ", status code: "
                            + res.getStatusCode());
        } else {
            if (res.getValue() instanceof Double) {
            } else {
                throw new Exception(
                        "Value returned must be Double type, Type of obj actual: " + res.getTypeValue());
            }
            return (double)res.getValue();
        }
    }
    public double getCurrentBitY() throws Exception {
        ICustomResponse res = helper.getCurrentBitY();
        if (res.getStatusCode() != CustomStatusCode.STATUS_GOOD) {
            logger.logERRORMessage(res.getMessage());
            throw new Exception(
                    "Error of sending request to server :" + helper.getEndpointUrl() + ", status code: "
                            + res.getStatusCode());
        } else {
            if (res.getValue() instanceof Double) {
            } else {
                throw new Exception(
                        "Value returned must be Double type, Type of obj actual: " + res.getTypeValue());
            }
            return (double)res.getValue();
        }
    }
    public double getCurrentBitSubX() throws Exception {
        ICustomResponse res = helper.getCurrentBitSubX();
        if (res.getStatusCode() != CustomStatusCode.STATUS_GOOD) {
            logger.logERRORMessage(res.getMessage());
            throw new Exception(
                    "Error of sending request to server :" + helper.getEndpointUrl() + ", status code: "
                            + res.getStatusCode());
        } else {
            if (res.getValue() instanceof Double) {
            } else {
                throw new Exception(
                        "Value returned must be Double type, Type of obj actual: " + res.getTypeValue());
            }
            return (double)res.getValue();
        }
    }
    public double getCurrentBitRotation() throws Exception {
        ICustomResponse res = helper.getCurrentBitRotation();
        if (res.getStatusCode() != CustomStatusCode.STATUS_GOOD) {
            logger.logERRORMessage(res.getMessage());
            throw new Exception(
                    "Error of sending request to server :" + helper.getEndpointUrl() + ", status code: "
                            + res.getStatusCode());
        } else {
            if (res.getValue() instanceof Double) {
            } else {
                throw new Exception(
                        "Value returned must be Double type, Type of obj actual: " + res.getTypeValue());
            }
            return (double)res.getValue();
        }
    }
    public double getCurrentBitReflineVu() throws Exception {
        ICustomResponse res = helper.getCurrentBitReflineVu();
        if (res.getStatusCode() != CustomStatusCode.STATUS_GOOD) {
            logger.logERRORMessage(res.getMessage());
            throw new Exception(
                    "Error of sending request to server :" + helper.getEndpointUrl() + ", status code: "
                            + res.getStatusCode());
        } else {
            if (res.getValue() instanceof Double) {
            } else {
                throw new Exception(
                        "Value returned must be Double type, Type of obj actual: " + res.getTypeValue());
            }
            return (double)res.getValue();
        }
    }
    public double getCurrentBitReflineRot() throws Exception {
        ICustomResponse res = helper.getCurrentBitReflineRot();
        if (res.getStatusCode() != CustomStatusCode.STATUS_GOOD) {
            logger.logERRORMessage(res.getMessage());
            throw new Exception(
                    "Error of sending request to server :" + helper.getEndpointUrl() + ", status code: "
                            + res.getStatusCode());
        } else {
            if (res.getValue() instanceof Double) {
            } else {
                throw new Exception(
                        "Value returned must be Double type, Type of obj actual: " + res.getTypeValue());
            }
            return (double)res.getValue();
        }
    }
    public double getCurrentBitTheta() throws Exception {
        ICustomResponse res = helper.getCurrentBitTheta();
        if (res.getStatusCode() != CustomStatusCode.STATUS_GOOD) {
            logger.logERRORMessage(res.getMessage());
            throw new Exception(
                    "Error of sending request to server :" + helper.getEndpointUrl() + ", status code: "
                            + res.getStatusCode());
        } else {
            if (res.getValue() instanceof Double) {
            } else {
                throw new Exception(
                        "Value returned must be Double type, Type of obj actual: " + res.getTypeValue());
            }
            return (double)res.getValue();
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
    public void resetPowerAxes() throws Exception {
        ICustomResponse res = helper.resetPowerAxes();
        if (res.getStatusCode() != CustomStatusCode.STATUS_GOOD) {
            logger.logERRORMessage(res.getMessage());
            throw new Exception("Error of sending request to server :" + helper.getEndpointUrl());
        } else {
            logger.logINFOMessage("reset power axes...");
        }
    }
    public void takeBatch() throws Exception {
        ICustomResponse res = helper.takeBatch();
        if (res.getStatusCode() != CustomStatusCode.STATUS_GOOD) {
            logger.logERRORMessage(res.getMessage());
            throw new Exception("Error of sending request to server :" + helper.getEndpointUrl());
        } else {
            logger.logINFOMessage("Capturing image...");
        }
    }
    public void deposeBatch() throws Exception {
        ICustomResponse res = helper.deposeBatch();
        if (res.getStatusCode() != CustomStatusCode.STATUS_GOOD) {
            logger.logERRORMessage(res.getMessage());
            throw new Exception("Error of sending request to server :" + helper.getEndpointUrl());
        } else {
            logger.logINFOMessage("Camera logging...");
        }
    }
    public void readXMLFile() throws Exception {
        ICustomResponse res = helper.readXMLFile();
        if (res.getStatusCode() != CustomStatusCode.STATUS_GOOD) {
            logger.logERRORMessage(res.getMessage());
            throw new Exception("Error of sending request to server :" + helper.getEndpointUrl());
        } else {
            logger.logINFOMessage("Capturing image...");
        }
    }
    public void renameXMLFile() throws Exception {
        ICustomResponse res = helper.renameXMLFile();
        if (res.getStatusCode() != CustomStatusCode.STATUS_GOOD) {
            logger.logERRORMessage(res.getMessage());
            throw new Exception("Error of sending request to server :" + helper.getEndpointUrl());
        } else {
            logger.logINFOMessage("Camera logging...");
        }
    }
    public void synchroAxesX() throws Exception {
        ICustomResponse res = helper.synchroAxesX();
        if (res.getStatusCode() != CustomStatusCode.STATUS_GOOD) {
            logger.logERRORMessage(res.getMessage());
            throw new Exception("Error of sending request to server :" + helper.getEndpointUrl());
        } else {
            logger.logINFOMessage("Capturing image...");
        }
    }
    public void synchroAxesZ() throws Exception {
        ICustomResponse res = helper.synchroAxesZ();
        if (res.getStatusCode() != CustomStatusCode.STATUS_GOOD) {
            logger.logERRORMessage(res.getMessage());
            throw new Exception("Error of sending request to server :" + helper.getEndpointUrl());
        } else {
            logger.logINFOMessage("Camera logging...");
        }
    }
    public void homingAxisSubX() throws Exception {
        ICustomResponse res = helper.homingAxisSubX();
        if (res.getStatusCode() != CustomStatusCode.STATUS_GOOD) {
            logger.logERRORMessage(res.getMessage());
            throw new Exception("Error of sending request to server :" + helper.getEndpointUrl());
        } else {
            logger.logINFOMessage("Capturing image...");
        }
    }
    public void homingAxisSubZ() throws Exception {
        ICustomResponse res = helper.homingAxisSubZ();
        if (res.getStatusCode() != CustomStatusCode.STATUS_GOOD) {
            logger.logERRORMessage(res.getMessage());
            throw new Exception("Error of sending request to server :" + helper.getEndpointUrl());
        } else {
            logger.logINFOMessage("Camera logging...");
        }
    }
    public void homingAxisTheta() throws Exception {
        ICustomResponse res = helper.homingAxisTheta();
        if (res.getStatusCode() != CustomStatusCode.STATUS_GOOD) {
            logger.logERRORMessage(res.getMessage());
            throw new Exception("Error of sending request to server :" + helper.getEndpointUrl());
        } else {
            logger.logINFOMessage("Capturing image...");
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
    public void setVitesse(short v) throws Exception {
        ICustomResponse res = helper.setVitesse(v);
        if (res.getStatusCode() != CustomStatusCode.STATUS_GOOD) {
            logger.logERRORMessage(res.getMessage());
            throw new Exception("Error of sending request to server :" + helper.getEndpointUrl());
        } else {
            logger.logINFOMessage("Starting...");
        }
    }
*/



    public static void main(String[] args){
        DepositingMachineCommander test=new DepositingMachineCommander();
        int i=0;
        try {
            test.resetPowerAxes();
        //    System.out.println(test.getCurrentBitId());
         //   System.out.println(test.getCurrentBitIdInBatch());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
