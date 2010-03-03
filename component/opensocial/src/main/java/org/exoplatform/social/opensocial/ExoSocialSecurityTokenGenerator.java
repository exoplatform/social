/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
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
package org.exoplatform.social.opensocial;


import org.apache.shindig.auth.BlobCrypterSecurityToken;
import org.apache.shindig.common.crypto.BlobCrypterException;
import org.apache.shindig.common.crypto.BlobCrypter;
import org.apache.shindig.common.crypto.BasicBlobCrypter;
import org.apache.shindig.common.util.TimeSource;
import org.exoplatform.web.application.RequestContext;
import org.exoplatform.portal.gadget.core.SecurityTokenGenerator;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.IdentityManager;
import org.exoplatform.social.core.identity.impl.organization.OrganizationIdentityProvider;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.RootContainer;

import java.io.File;
import java.io.IOException;

public class ExoSocialSecurityTokenGenerator implements SecurityTokenGenerator {
    private String containerKey;
    private final TimeSource timeSource;


  public ExoSocialSecurityTokenGenerator() {
    //TODO should be moved to config
    this.containerKey = "key.txt";
    this.timeSource = new TimeSource();
  }

  protected String createToken(String gadgetURL, String owner, String viewer, Long moduleId, String container) {
      try {
        BlobCrypterSecurityToken t = new BlobCrypterSecurityToken(
          getBlobCrypter(this.containerKey), container, null);

        t.setAppUrl(gadgetURL);
        t.setModuleId(moduleId);
        t.setOwnerId(owner);
        t.setViewerId(viewer);
        t.setTrustedJson("trusted");

        return t.encrypt();
    } catch (IOException e) {
      e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
    } catch (BlobCrypterException e) {
      e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
    }
      return null;
  }

  public String createToken(String gadgetURL, Long moduleId) {
    RequestContext context = RequestContext.getCurrentInstance();
    String rUserId = getIdentityId(context.getRemoteUser());

    PortalRequestContext request = Util.getPortalRequestContext() ;
    String uri = request.getNodePath();


    String[] els = uri.split("/");
    String ownerId = rUserId;
    if (els.length >= 3 && els[1].equals("people")) {
      ownerId = getIdentityId(els[2]);
    }
    /*else if(els.length == 2 && els[1].equals("mydashboard")) {
      owner = rUser;
    }*/

    return createToken(gadgetURL, ownerId, rUserId, moduleId, "default");
  }

  protected String getIdentityId(String remoteId){
    PortalContainer pc = PortalContainer.getInstance();
    IdentityManager im = (IdentityManager) pc.getComponentInstanceOfType(IdentityManager.class);

    Identity id = null;
    try {
      id = im.getIdentityByRemoteId(OrganizationIdentityProvider.NAME, remoteId);
    } catch (Exception e) {
      e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
    }
    if(id != null)
      return id.getId();
    return null;
  }

  private BlobCrypter getBlobCrypter(String fileName) throws IOException {
    BasicBlobCrypter c = new BasicBlobCrypter(new File(fileName));
    c.timeSource = timeSource;
    return c;
  }

}

