package org.exoplatform.social.opensocial;

import java.util.Map;

import org.apache.shindig.auth.BlobCrypterSecurityToken;
import org.apache.shindig.common.crypto.BlobCrypter;

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
    Map<String,String> map = super.buildValuesMap();
    if (portalContainer != null) {
      map.put(PORTAL_CONTAINER_KEY, portalContainer);
    }
    return map;                                                                                           
   }
  
}
