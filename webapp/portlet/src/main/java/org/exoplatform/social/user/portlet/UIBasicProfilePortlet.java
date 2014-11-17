/***************************************************************************
 * Copyright (C) 2003-2014 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 ***************************************************************************/
package org.exoplatform.social.user.portlet;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.portlet.PortletMode;

import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.webui.Utils;
import org.exoplatform.webui.application.WebuiApplication;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIPortletApplication;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;

@ComponentConfig(
  lifecycle = UIApplicationLifecycle.class,
  template = "app:/groovy/social/portlet/user/UIBasicProfilePortlet.gtmpl"
)
public class UIBasicProfilePortlet extends UIPortletApplication {
  private Profile currentProfile;
  

  public UIBasicProfilePortlet() throws Exception {
  }

  public void processRender(WebuiApplication app, WebuiRequestContext context) throws Exception {
    PortletRequestContext portletReqContext = (PortletRequestContext) context;
    PortletMode portletMode = portletReqContext.getApplicationMode();
    if (portletMode == PortletMode.VIEW) {
      Identity ownerIdentity = Utils.getOwnerIdentity(false);
      currentProfile = ownerIdentity.getProfile();
    }
    //
    super.processRender(app, context);
  }

  protected Map<String, Object> getProfileInfo() {
    Map<String, Object> infos = new LinkedHashMap<String, Object>();
    infos.put(Profile.EMAIL, currentProfile.getEmail());
    //
    String jobTitle = currentProfile.getPosition();
    if(!isEmpty(jobTitle)) {
      infos.put(Profile.POSITION, jobTitle);
    }
    String gender = currentProfile.getGender();
    if(!isEmpty(gender)) {
      infos.put(Profile.GENDER, gender);
    }
    //
    List<Map<String, String>> phones = currentProfile.getPhones();
    if (phones != null && phones.size() > 0) {
      Map<String, String> phoneInfos = new HashMap<String, String>();
      for (Map<String, String> map : phones) {
        for (String key : map.keySet()) {
          phoneInfos.put(key, map.get(key));
        }
      }
      //
      infos.put(Profile.CONTACT_PHONES, phoneInfos);
    }
    //
    List<Map<String, String>> ims = (List<Map<String, String>>) currentProfile.getProperty(Profile.CONTACT_IMS);
    if (ims != null && ims.size() > 0) {
      Map<String, String> imInfos = new HashMap<String, String>();
      for (Map<String, String> map : ims) {
        for (String key : map.keySet()) {
          imInfos.put(key, map.get(key));
        }
      }
      //
      infos.put(Profile.CONTACT_IMS, imInfos);
    }
    //
    List<Map<String, String>> urls = (List<Map<String, String>>) currentProfile.getProperty(Profile.CONTACT_URLS);
    if (urls != null && urls.size() > 0) {
      Map<String, String> urlInfos = new HashMap<String, String>();
      for (Map<String, String> map : urls) {
        for (String key : map.keySet()) {
          urlInfos.put(key, map.get(key));
        }
      }
      //
      infos.put(Profile.CONTACT_URLS, urlInfos);
    }
    //
    return infos;
  }

  private static boolean isEmpty(String s) {
    return s == null || s.trim().length() == 0;
  }

  protected boolean isString(Object s) {
    return s instanceof String;
  }
  
  

}



















