

package meshIneBits.opcuaHelper;

import meshIneBits.util.CustomLogger;

import java.util.concurrent.ExecutionException;

public class RobotOPCUAHelper extends BitSLickrMachineAdapter {

    private static final CustomLogger logger = new CustomLogger(RobotOPCUAHelper.class);

    public final String robotDiscreteInput = "301";
    public final String robotCoils = "302";
    public final String robotInputRegisters = "303";
    public final short robotHoldingRegisters = 304;
    public final String robotCommand = "305";

    public RobotOPCUAHelper(ROBOT robot)  {
        super(robot);
    }


    public ICustomResponse getHoldingRegisters(){
        try {
            return readVariableNode(robotHoldingRegisters);
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    public ICustomResponse setHoldingRegisters(short[] s) {
        try {
            return writeVariableNode(robotHoldingRegisters, "short", s);
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public String getEndpointUrl() {

        return this.url;
    }
}

