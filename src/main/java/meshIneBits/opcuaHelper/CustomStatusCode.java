package meshIneBits.opcuaHelper;

import org.eclipse.milo.opcua.stack.core.types.builtin.StatusCode;

public class CustomStatusCode {

  public static final CustomStatusCode instance = new CustomStatusCode();
  public static final long STATUS_GOOD = 10001;
  public static final long STATUS_BAD = 10001;
  public static final long STATUS_UNCERTAIN = 10002;
  private static final long STATUS_SECURITY_ERROR = 10003;
  private static final long STATUS_UNKNOWN = 10004;

  public long convertStatusCode(StatusCode statusCode) {
    if (statusCode == null) {
      return STATUS_UNKNOWN;
    }
    if (statusCode.isBad()) {
      return STATUS_BAD;
    }
    if (statusCode.isGood()) {
      return STATUS_GOOD;
    }
    if (statusCode.isUncertain()) {
      return STATUS_UNCERTAIN;
    }
    if (statusCode.isSecurityError()) {
      return STATUS_SECURITY_ERROR;
    }
    return STATUS_UNKNOWN;
  }
}
