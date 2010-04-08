package org.exoplatform.social.opensocial;

import java.util.Map;

import org.apache.shindig.auth.AnonymousSecurityToken;
import org.apache.shindig.auth.BlobCrypterSecurityToken;
import org.apache.shindig.auth.BlobCrypterSecurityTokenDecoder;
import org.apache.shindig.auth.SecurityToken;
import org.apache.shindig.auth.SecurityTokenDecoder;
import org.apache.shindig.auth.SecurityTokenException;
import org.apache.shindig.common.crypto.BlobCrypter;
import org.apache.shindig.common.crypto.BlobCrypterException;
import org.apache.shindig.config.ContainerConfig;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class ExoBlobCrypterSecurityDecoder extends BlobCrypterSecurityTokenDecoder {

  @Inject
  public ExoBlobCrypterSecurityDecoder(ContainerConfig config) {
    super(config);
  }
  
  /**
   * Decrypt and verify the provided security token.
   */
  @Override
  public SecurityToken createToken(Map<String, String> tokenParameters)
      throws SecurityTokenException {
    String token = tokenParameters.get(SecurityTokenDecoder.SECURITY_TOKEN_NAME);
    if (token == null || token.trim().length() == 0) {
      // No token is present, assume anonymous access
      return new AnonymousSecurityToken();
    }
    String[] fields = token.split(":");
    if (fields.length != 2) {
      throw new SecurityTokenException("Invalid security token " + token);
    }
    String container = fields[0];
    BlobCrypter crypter = crypters.get(container);
    if (crypter == null) {
      throw new SecurityTokenException("Unknown container " + token);
    }
    String domain = domains.get(container);
    String activeUrl = tokenParameters.get(SecurityTokenDecoder.ACTIVE_URL_NAME);
    String crypted = fields[1];
    try {
      return ExoBlobCrypterSecurityToken.decrypt(crypter, container, domain, crypted, activeUrl);
    } catch (BlobCrypterException e) {
      throw new SecurityTokenException(e);
    }
  }

}
