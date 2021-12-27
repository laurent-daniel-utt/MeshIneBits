package meshIneBits.opcuaHelper;

public interface ICustomResponse {
  long getCodeStatus();
  String getMessage();
  Object getValue();
  String getTypeValue();
  String getNodeId();
}
