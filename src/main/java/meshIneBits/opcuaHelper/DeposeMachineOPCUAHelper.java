
package meshIneBits.opcuaHelper;

import meshIneBits.util.CustomLogger;

import java.util.concurrent.ExecutionException;

public class DeposeMachineOPCUAHelper extends BitSLickrMachineAdapter {

    private static final CustomLogger logger = new CustomLogger(DeposeMachineOPCUAHelper.class);

    public final String deposeStart = "|var|CPX-E-CEC-M1-PN.Application.GVL.START";
    public final String deposeGearMethode = "|var|CPX-E-CEC-M1-PN.Application.GVL.gearMethod.";
    public final String deposeVitesse = "|var|CPX-E-CEC-M1-PN.Application.GVL.vitesse";
    public final String deposePosition = "|var|CPX-E-CEC-M1-PN.Application.GVL.pos";
    public final String deposeMoveX1 = "|var|CPX-E-CEC-M1-PN.Application.GVL.x1";
    public final String deposeMoveX2 = "|var|CPX-E-CEC-M1-PN.Application.GVL.x2";
    public final String deposeSynchro = "|var|CPX-E-CEC-M1-PN.Application.GVL.synchrox1x2";
    public final String deposeMove = "|var|CPX-E-CEC-M1-PN.Application.GVL.movex1x3";


    @Override
    public ICustomResponse stopMachine(){
        return null;
    }

    @Override
    public ICustomResponse startMachine() {
        return null;
    }

    public ICustomResponse deposeStart() {
        try {
            return writeVariableNode(deposeStart, "Boolean", true);
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

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
        return BitSLickrHelperConfig.depose_machine_url;
    }
}

