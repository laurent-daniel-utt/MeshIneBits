/*------------------------------------------------------------------------------
 -  MeshIneBits is a Java software to disintegrate a 3d mesh (model in .stl)
 -  into a network of standard parts (called "Bits").
 -
 -  Copyright (C) 2016-2026 DANIEL Laurent.
 -  Copyright (C) 2015 Cédric Siourakhan
 -  Copyright (C) 2016 Gabriel Magny
 -  Copyright (C) 2016  CASSARD Thibault & GOUJU Nicolas.
 -  Copyright (C) 2017-2018  TRAN Quoc Nhat Han.
 -  Copyright (C) 2018 VALLON Benjamin.
 -  Copyright (C) 2018 LORIMER Campbell.
 -  Copyright (C) 2018 D'AUTUME Christian.
 -  Copyright (C) 2019 DURINGER Nathan (Tests).
 -  Copyright (C) 2020-2021 CLAIRIS Etienne & RUSSO André.
 -  Copyright (C) 2020-2021 DO Quang Bao.
 -  Copyright (C) 2021 VANNIYASINGAM Mithulan.
 -  Copyright (C) 2021  Quang Long
 -  Copyright (C) 2022 Mhamad Atlab
 -  Copyright (C) 2022 Majed Hlaihel
 -  Copyright (C) 2026 Aymeric Sirejol
 -
 -  This program is free software: you can redistribute it and/or modify
 -  it under the terms of the GNU General Public License as published by
 -  the Free Software Foundation, either version 3 of the License, or
 -  (at your option) any later version.
 -
 -  This program is distributed in the hope that it will be useful,
 -  but WITHOUT ANY WARRANTY; without even the implied warranty of
 -  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 -  GNU General Public License for more details.
 -
 -  You should have received a copy of the GNU General Public License
 -  along with this program. If not, see <https://www.gnu.org/licenses/>.
 -----------------------------------------------------------------------------*/

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
