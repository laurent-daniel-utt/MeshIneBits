package meshIneBits.opcuaHelper;

import java.util.function.Predicate;
import org.eclipse.milo.opcua.sdk.client.api.identity.AnonymousProvider;
import org.eclipse.milo.opcua.sdk.client.api.identity.IdentityProvider;
import org.eclipse.milo.opcua.stack.core.security.SecurityPolicy;
import org.eclipse.milo.opcua.stack.core.types.structured.EndpointDescription;

public interface IClientHelper {

  String getEndpointUrl();

  default Predicate<EndpointDescription> endpointFilter() {
    return e -> getSecurityPolicy().getUri().equals(e.getSecurityPolicyUri());
  }

  default SecurityPolicy getSecurityPolicy() {
    return SecurityPolicy.None;
  }

  default IdentityProvider getIdentityProvider() {
    return new AnonymousProvider();
  }
}
