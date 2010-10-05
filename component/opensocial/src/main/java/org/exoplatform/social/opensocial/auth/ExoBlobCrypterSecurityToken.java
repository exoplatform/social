/*
 * Copyright (C) 2003-2010 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.social.opensocial.auth;

import java.util.Map;

import org.apache.shindig.auth.BlobCrypterSecurityToken;
import org.apache.shindig.common.crypto.BlobCrypter;
import org.apache.shindig.common.crypto.BlobCrypterException;

public class ExoBlobCrypterSecurityToken extends BlobCrypterSecurityToken {

  protected static final String PORTAL_CONTAINER_KEY = "p";
  protected static final String HOST_NAME="h";
  protected static final String PORTAL_OWNER_KEY="w";

  public ExoBlobCrypterSecurityToken(BlobCrypter crypter, String container, String domain) {
    super(crypter, container, domain);
  }

  protected String portalContainer;
  private String hostName;
  private String portalOwner;

  public String getPortalContainer() {
    return portalContainer;
  }

  public void setPortalContainer(String portalContainer) {
    this.portalContainer = portalContainer;
  }

  public void setHostName(String hostName) {
	this.hostName = hostName;
  }

  public String getHostName() {
	return hostName;
  }

  public String getPortalOwner() {
    return portalOwner;
  }

  public void setPortalOwner(String portalOwner) {
    this.portalOwner = portalOwner;
  }

  @Override
  protected Map<String, String> buildValuesMap() {
    Map<String, String> map = super.buildValuesMap();
    if (portalContainer != null) {
      map.put(PORTAL_CONTAINER_KEY, portalContainer);
    }
    if(hostName !=null) {
    	map.put(HOST_NAME, hostName);
    }
    if (portalOwner != null) {
      map.put(PORTAL_OWNER_KEY, portalOwner);
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
    t.setHostName(values.get(HOST_NAME));
    t.setPortalOwner(values.get(PORTAL_OWNER_KEY));
    return t;
  }

}
