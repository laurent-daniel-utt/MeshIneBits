package meshIneBits.opcuaHelper;

public class BitSLickrHelperConfig {

  public static final String APPLICATION_NAME = "meshIneBits opc-ua client";
  public static final String APPLICATION_URI = "urn:MeshIneBits:client";

  //Cutting machine config
  public static final String cutting_machine_url = "opc.tcp://192.168.10.20:4840";



  public static final String assembling_machine_url = "opc.tcp://192.168.10.20:4840";

  public static final String depose_machine_url = "opc.tcp://192.168.10.20:4840";

  public static final String robot_manip_url = "opc.tcp://192.168.10.30:4880/FANUC/NanoUaServer";

  public static final String robot_decoupe_url = "opc.tcp://192.168.10.31:4880/FANUC/NanoUaServer";

}
