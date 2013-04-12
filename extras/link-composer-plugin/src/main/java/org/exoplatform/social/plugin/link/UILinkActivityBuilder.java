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
package org.exoplatform.social.plugin.link;

import java.util.HashMap;
import java.util.Map;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.storage.ActivityStorageException;
import org.exoplatform.social.webui.activity.BaseUIActivity;
import org.exoplatform.social.webui.activity.BaseUIActivityBuilder;
import org.json.JSONException;
import org.json.JSONObject;

public class UILinkActivityBuilder extends BaseUIActivityBuilder {
  private static final Log LOG = ExoLogger.getLogger(UILinkActivityBuilder.class);
  
  @Override
  protected void extendUIActivity(BaseUIActivity uiActivity, ExoSocialActivity activity) {
    UILinkActivity uiLinkActivity = (UILinkActivity) uiActivity;

    Map<String, String> templateParams = activity.getTemplateParams();
    if (templateParams != null) {
        uiLinkActivity.setLinkSource(templateParams.get(UILinkActivityComposer.LINK_PARAM));

        uiLinkActivity.setLinkTitle(templateParams.get(UILinkActivityComposer.TITLE_PARAM));

        uiLinkActivity.setLinkImage(templateParams.get(UILinkActivityComposer.IMAGE_PARAM));

        uiLinkActivity.setLinkDescription(templateParams.get(UILinkActivityComposer.DESCRIPTION_PARAM));

        uiLinkActivity.setLinkComment(templateParams.get(UILinkActivityComposer.COMMENT_PARAM));
        
        uiLinkActivity.setEmbedHtml(templateParams.get(UILinkActivityComposer.HTML_PARAM));
    } else {
      try {
        JSONObject jsonObj = new JSONObject(activity.getTitle());
        uiLinkActivity.setLinkSource(jsonObj.getString(UILinkActivityComposer.LINK_PARAM));
        uiLinkActivity.setLinkTitle(jsonObj.getString(UILinkActivityComposer.TITLE_PARAM));
        uiLinkActivity.setLinkImage(jsonObj.getString(UILinkActivityComposer.IMAGE_PARAM));
        uiLinkActivity.setLinkDescription(jsonObj.getString(UILinkActivityComposer.DESCRIPTION_PARAM));
        uiLinkActivity.setLinkComment(jsonObj.getString(UILinkActivityComposer.COMMENT_PARAM));
        saveToNewDataFormat(activity, uiLinkActivity);
      } catch (JSONException e) {
        LOG.error("Error with link activity's title data");
      }
    }
  }

  private void saveToNewDataFormat(ExoSocialActivity activity, UILinkActivity uiLinkActivity) {
    String linkTitle = "Shared a link: <a href=\"${" + UILinkActivityComposer.LINK_PARAM + "}\">" +
            "${" + UILinkActivityComposer.TITLE_PARAM + "} </a>";
    activity.setTitle(linkTitle);
    Map<String, String> templateParams = new HashMap<String, String>();
    templateParams.put(UILinkActivityComposer.LINK_PARAM, uiLinkActivity.getLinkSource());
    templateParams.put(UILinkActivityComposer.TITLE_PARAM, uiLinkActivity.getLinkTitle());
    templateParams.put(UILinkActivityComposer.IMAGE_PARAM, uiLinkActivity.getLinkImage());
    templateParams.put(UILinkActivityComposer.DESCRIPTION_PARAM, uiLinkActivity.getLinkDescription());
    templateParams.put(UILinkActivityComposer.COMMENT_PARAM, uiLinkActivity.getLinkComment());
    activity.setTemplateParams(templateParams);
    ActivityManager am = (ActivityManager) PortalContainer.getInstance().getComponentInstanceOfType(ActivityManager.class);
    try {
      am.saveActivityNoReturn(activity);
    } catch (ActivityStorageException ase) {
      LOG.warn("Could not save new data format for document activity.", ase);
    }
  }
}
