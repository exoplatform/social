package org.exoplatform.social.opensocial;

import java.util.Map;

import org.apache.shindig.auth.BlobCrypterSecurityToken;
import org.apache.shindig.common.crypto.BlobCrypter;
import org.apache.shindig.common.crypto.BlobCrypterException;

public class ExoBlobCrypterSecurityToken extends BlobCrypterSecurityToken {

  protected static final String PORTAL_CONTAINER_KEY = "p";

  public ExoBlobCrypterSecurityToken(BlobCrypter crypter, String container, String domain) {
    super(crypter, container, domain);
  }

  protected String portalContainer;

  public String getPortalContainer() {
    return portalContainer;
  }

  public void setPortalContainer(String portalContainer) {
    this.portalContainer = portalContainer;
  }

  protected Map<String, String> buildValuesMap() {
    Map<String, String> map = super.buildValuesMap();
    if (portalContainer != null) {
      map.put(PORTAL_CONTAINER_KEY, portalContainer);
    }
    return map;
  }

  /**
   * {@inheritDoc}
   */
  static BlobCrypterSecurityToken decrypt(BlobCrypter crypter,
                                          String container,
                                          String domain,
                                          String token,
                                          String activeUrl) throws BlobCrypterException {
    Map<String, String> values = crypter.unwrap(token, MAX_TOKEN_LIFETIME_SECS);
    BlobCrypterSecurityToken t = new ExoBlobCrypterSecurityToken(crypter, container, domain);
    setTokenValues(t, values);
    t.setActiveUrl(activeUrl);
    return t;
  }

  /**
   * {@inheritDoc}
   * @param token
   * @param values
   */
  protected static void setTokenValues(ExoBlobCrypterSecurityToken token, Map<String, String> values) {
    BlobCrypterSecurityToken.setTokenValues(token, values);
    token.setPortalContainer(values.get(PORTAL_CONTAINER_KEY));
  }

}
