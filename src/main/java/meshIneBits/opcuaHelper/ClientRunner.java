package meshIneBits.opcuaHelper;

import meshIneBits.util.CustomLogger;
import org.bouncycastle.pqc.jcajce.provider.BouncyCastlePQCProvider;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.stack.client.security.DefaultClientCertificateValidator;
import org.eclipse.milo.opcua.stack.core.security.DefaultTrustListManager;
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Security;
import java.util.concurrent.CompletableFuture;

import static org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned.uint;

public class ClientRunner {

  static {
    Security.addProvider(new BouncyCastlePQCProvider());
  }

  private final CustomLogger logger = new CustomLogger(getClass());
  private final IClientHelper clientAction;
  private DefaultTrustListManager trustListManager;
  private OpcUaClient client;

  public ClientRunner(IClientHelper clientAction)  {
    System.out.println("in ClientRunner constructor");
    this.clientAction = clientAction;
    try {
      client = createClient();

    } catch (Exception e   ) {
      e.printStackTrace();
    }
  }

  private OpcUaClient createClient() throws Exception {
    Path securityTempDir = Paths.get(System.getProperty("java.io.tmpdir"), "client", "security");
    Files.createDirectories(securityTempDir);
    if (!Files.exists(securityTempDir)) {
      throw new Exception("unable to create security dir: " + securityTempDir);
    }

    File pkiDir = securityTempDir.resolve("pki").toFile();
    logger.logINFOMessage("security dir: " + securityTempDir.toAbsolutePath());
    logger.logINFOMessage("security pki dir: " + pkiDir.getAbsolutePath());

    KeyStoreLoader loader = new KeyStoreLoader().load(securityTempDir);

    trustListManager = new DefaultTrustListManager(pkiDir);

    DefaultClientCertificateValidator certificateValidator =
        new DefaultClientCertificateValidator(trustListManager);

    return OpcUaClient.create(
        clientAction.getEndpointUrl(),
        endpoints -> endpoints
            .stream()
            .filter(clientAction.endpointFilter())
            .findFirst(),
        configBuilder ->
            configBuilder
                .setApplicationName(LocalizedText.english(BitSLicRHelperConfig.APPLICATION_NAME))
                .setApplicationUri(BitSLicRHelperConfig.APPLICATION_URI)
                .setKeyPair(loader.getClientKeyPair())
                .setCertificate(loader.getClientCertificate())
                .setCertificateChain(loader.getClientCertificateChain())
                .setCertificateValidator(certificateValidator)
                .setIdentityProvider(clientAction.getIdentityProvider())
                .setRequestTimeout(uint(5000))
                .build()
    );
  }

  public void runAction(IClientAction clientAction, CompletableFuture<ICustomResponse> future) {
    try {
      //client = createClient();

      clientAction.run(client,future);

    } catch (Throwable t) {
      logger.logERRORMessage("Error getting client: " + t.getMessage());
    }
  }
}
