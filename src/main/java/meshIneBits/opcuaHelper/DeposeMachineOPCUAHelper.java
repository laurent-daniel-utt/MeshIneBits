
package meshIneBits.opcuaHelper;

import meshIneBits.util.CustomLogger;

import java.util.concurrent.ExecutionException;

public class DeposeMachineOPCUAHelper extends BitSLickrMachineAdapter {

    private static final CustomLogger logger = new CustomLogger(DeposeMachineOPCUAHelper.class);

   /*
    public final String deposeStart = "|var|CPX-E-CEC-M1-PN.Application.GVL.START";
    public final String deposeGearMethode = "|var|CPX-E-CEC-M1-PN.Application.GVL.gearMethod.";
    public final String deposeVitesse = "|var|CPX-E-CEC-M1-PN.Application.GVL.vitesse";
    public final String deposePosition = "|var|CPX-E-CEC-M1-PN.Application.GVL.pos";
    public final String deposeMoveX1 = "|var|CPX-E-CEC-M1-PN.Application.GVL.x1";
    public final String deposeMoveX2 = "|var|CPX-E-CEC-M1-PN.Application.GVL.x2";
    public final String deposeSynchro = "|var|CPX-E-CEC-M1-PN.Application.GVL.synchrox1x2";
    public final String deposeMove = "|var|CPX-E-CEC-M1-PN.Application.GVL.movex1x3";
*/

    public final String start_depose = "|var|CPX-E-CEC-M1-PN.Application.visu.start_button";
    public final String stop_depose = "|var|CPX-E-CEC-M1-PN.Application.visu.stop_button";
    public final String reset_depose = "|var|CPX-E-CEC-M1-PN.Application.visu.reset_button";
    public final String continue_depose = "|var|CPX-E-CEC-M1-PN.Application.visu.continue_button";
    public final String pause_depose = "|var|CPX-E-CEC-M1-PN.Application.visu.pause_button";
    public final String continue_after_turn_off = "|var|CPX-E-CEC-M1-PN.Application.visu.continue_after_turn_off_button";
    public final String continue_after_E_STOP = "|var|CPX-E-CEC-M1-PN.Application.visu.continue_after_E_STOP_button";
    public final String camera_login = "|var|CPX-E-CEC-M1-PN.Application.visu.camera_login_button";
    public final String camera_capture_image = "|var|CPX-E-CEC-M1-PN.Application.visu.camera_capture_image_button";
    public final String acknowledge_error = "|var|CPX-E-CEC-M1-PN.Application.visu.acknowledge_error_button";
    public final String message_error = "|var|CPX-E-CEC-M1-PN.Application.GDA.message_error";


    @Override
    public ICustomResponse stopMachine(){
        return null;
    }

    @Override
    public ICustomResponse startMachine() {
        return null;
    }

    public ICustomResponse startDepose() {
        try {
            return writeVariableNode(start_depose, "Boolean", true);
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    public ICustomResponse stopDepose() {
        try {
            return writeVariableNode(stop_depose, "Boolean", true);
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    public ICustomResponse resetDepose() {
        try {
            return writeVariableNode(reset_depose, "Boolean", true);
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    public ICustomResponse continueDepose() {
        try {
            return writeVariableNode(continue_depose, "Boolean", true);
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    public ICustomResponse pauseDepose() {
        try {
            return writeVariableNode(pause_depose, "Boolean", true);
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    public ICustomResponse continueAfterTurnOff() {
        try {
            return writeVariableNode(continue_after_turn_off, "Boolean", true);
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    public ICustomResponse continueAfterESTOP() {
        try {
            return writeVariableNode(continue_after_E_STOP, "Boolean", true);
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    public ICustomResponse cameraLogin() {
        try {
            return writeVariableNode(camera_login, "Boolean", true);
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    public ICustomResponse cameraCaptureImage() {
        try {
            return writeVariableNode(camera_capture_image, "Boolean", true);
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    public ICustomResponse acknowledgeError() {
        try {
            return writeVariableNode(acknowledge_error, "Boolean", true);
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    public ICustomResponse getMessageError(){
        try {
            return readVariableNode(message_error);
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e.getMessage());
        }
    }






/*
    public ICustomResponse gearMethode(boolean b) {
        try {
            return writeVariableNode(deposeGearMethode, "Boolean", b);
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public ICustomResponse setPosition(double p) {
        try {
            return writeVariableNode(deposePosition, "short", p);
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    public ICustomResponse getPosition(){
        try {
            return readVariableNode(deposePosition);
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    public ICustomResponse setVitesse(short v) {
        try {
            return writeVariableNode(deposeVitesse, "short", v);
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    public ICustomResponse getVitesse(){
        try {
            return readVariableNode(deposeVitesse);
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    public ICustomResponse moveX1() {
        try {
            return writeVariableNode(deposeMoveX1, "Boolean", true);
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    public ICustomResponse moveX2() {
        try {
            return writeVariableNode(deposeMoveX2, "Boolean", true);
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    public ICustomResponse deposeMove() {
        try {
            return writeVariableNode(deposeMove, "Boolean", true);
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    public ICustomResponse deposeSynchro() {
        try {
            return writeVariableNode(deposeSynchro, "Boolean", true);
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
*/

    @Override
    public ICustomResponse getMachineState() {
        return null;
    }

    @Override
    public ICustomResponse pauseMachine() {
        return null;
    }

    public ICustomResponse getCuttingBitId(){
        return null;
    }

    public ICustomResponse getCuttingPathId(){
        return null;
    }



    @Override
    public String getEndpointUrl() {
        return BitSLicRHelperConfig.depose_machine_url;
    }
}

