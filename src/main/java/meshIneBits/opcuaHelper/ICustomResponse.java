package meshIneBits.opcuaHelper;

public interface ICustomResponse {
  long getStatusCode();
  String getMessage();
  Object getValue();
  String getTypeValue();
  Object getNodeId();
}
