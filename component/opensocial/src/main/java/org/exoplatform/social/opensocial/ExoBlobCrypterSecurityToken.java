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

  @Override
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
    ExoBlobCrypterSecurityToken t = new ExoBlobCrypterSecurityToken(crypter, container, domain);
    t.setOwnerId(values.get(OWNER_KEY));
    t.setViewerId(values.get(VIEWER_KEY));
    t.setAppUrl(values.get(GADGET_KEY));
    String moduleId = values.get(GADGET_INSTANCE_KEY);
    if (moduleId != null) {
      t.setModuleId(Long.parseLong(moduleId));
    }
    t.setTrustedJson(values.get(TRUSTED_JSON_KEY));
    t.setPortalContainer(values.get(PORTAL_CONTAINER_KEY));
    t.setActiveUrl(activeUrl);
    return t;
  }
  

  

}
