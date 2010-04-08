package org.exoplatform.social.opensocial;

import java.util.Map;

import org.apache.shindig.auth.BasicSecurityTokenDecoder;
import org.apache.shindig.auth.SecurityToken;
import org.apache.shindig.auth.SecurityTokenDecoder;
import org.apache.shindig.auth.SecurityTokenException;
import org.apache.shindig.config.ContainerConfig;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class ExoSecurityTokenDecoder implements SecurityTokenDecoder {

  private static final String        SECURITY_TOKEN_TYPE = "gadgets.securityTokenType";

  private final SecurityTokenDecoder decoder;

  @Inject
  public ExoSecurityTokenDecoder(ContainerConfig config) {

    String tokenType = config.getString(ContainerConfig.DEFAULT_CONTAINER, SECURITY_TOKEN_TYPE);
    if ("insecure".equals(tokenType)) {
      decoder = new BasicSecurityTokenDecoder();
    } else if ("secure".equals(tokenType)) {
      decoder = new ExoBlobCrypterSecurityDecoder(config);
    } else {
      throw new RuntimeException("Unknown security token type specified in "
          + ContainerConfig.DEFAULT_CONTAINER + " container configuration. " + SECURITY_TOKEN_TYPE
          + ": " + tokenType);
    }
  }

  public SecurityToken createToken(Map<String, String> tokenParameters) throws SecurityTokenException {
    return decoder.createToken(tokenParameters);
  }

}
