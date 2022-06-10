package meshIneBits.opcuaHelper;


import java.util.Objects;

public class BaseCustomResponse implements ICustomResponse {

  private final long statusCode;
  private final String message;
  private final Object value;
  private final String typeValue;
  private final String nodeId;

  private BaseCustomResponse(String nodeId, long statusCode, String message, Object value, String typeValue) {
    this.statusCode = statusCode;
    this.message = message;
    this.value = value;
    this.typeValue = typeValue;
    this.nodeId = nodeId;
  }

  @Override
  public long getStatusCode() {
    return statusCode;
  }

  @Override
  public String getMessage() {
    return message;
  }

  @Override
  public Object getValue() {
    return value;
  }

  @Override
  public String getTypeValue() {
    return typeValue;
  }

  @Override
  public String getNodeId() {
    return nodeId;
  }

  public static class BaseCustomResponseBuilder {

    private long statusCode;
    private String message;
    private Object value;
    private String typeValue;
    private String nodeId;

    public BaseCustomResponseBuilder setStatusCode(long statusCode) {
      this.statusCode = statusCode;
      return this;
    }

    public BaseCustomResponseBuilder setMessage(String message) {
      this.message = message;
      return this;
    }

    public BaseCustomResponseBuilder setValue(Object value) {
      this.value = value;
      return this;
    }

    public BaseCustomResponseBuilder setTypeValue(String typeValue) {
      this.typeValue = typeValue;
      return this;
    }

    public BaseCustomResponseBuilder setNodeId(String nodeId) {
      this.nodeId = nodeId;
      return this;
    }
    public BaseCustomResponse build(){
      Objects.requireNonNull(nodeId);
      Objects.requireNonNull(value);
      Objects.requireNonNull(typeValue);
      return new BaseCustomResponse(nodeId,statusCode,message,value,typeValue);
    }
  }
}
