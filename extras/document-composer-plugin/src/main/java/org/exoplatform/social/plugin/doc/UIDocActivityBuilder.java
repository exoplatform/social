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
package org.exoplatform.social.plugin.doc;

import java.util.HashMap;
import java.util.Map;

import javax.jcr.Node;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.wcm.core.NodeLocation;
import org.exoplatform.social.core.activity.model.Activity;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.storage.ActivityStorageException;
import org.exoplatform.social.webui.activity.BaseUIActivity;
import org.exoplatform.social.webui.activity.BaseUIActivityBuilder;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by The eXo Platform SAS
 * Author : Zun
 *          exo@exoplatform.com
 * Jul 23, 2010
 */
public class UIDocActivityBuilder extends BaseUIActivityBuilder {
  private static final Log LOG = ExoLogger.getLogger(UIDocActivityBuilder.class);
  @Override
  protected void extendUIActivity(BaseUIActivity uiActivity, ExoSocialActivity activity) {
    UIDocActivity docActivity = (UIDocActivity) uiActivity;
    String repository = null;
    String workspace = null;
    if (activity.getTemplateParams() != null) {
      Map<String,String> activityParams = activity.getTemplateParams();
      docActivity.docLink = activityParams.get(UIDocActivity.DOCLINK);
      docActivity.docName = activityParams.get(UIDocActivity.DOCNAME);
      docActivity.message = activityParams.get(UIDocActivity.MESSAGE);
      docActivity.docPath = activityParams.get(UIDocActivity.DOCPATH);
      repository = activityParams.get(UIDocActivity.REPOSITORY);
      workspace = activityParams.get(UIDocActivity.WORKSPACE);
    } else {
      //backward compatible with old data
      try {
        final JSONObject jsonObject = new JSONObject(activity.getTitle());
        docActivity.docLink = jsonObject.getString(UIDocActivity.DOCLINK);
        docActivity.docName = jsonObject.getString(UIDocActivity.DOCNAME);
        docActivity.message = jsonObject.getString(UIDocActivity.MESSAGE);
        docActivity.docPath = jsonObject.getString(UIDocActivity.DOCPATH);
        repository = jsonObject.getString(UIDocActivity.REPOSITORY);
        workspace = jsonObject.getString(UIDocActivity.WORKSPACE);

        saveToNewDataFormat(activity, docActivity, repository, workspace);

      } catch (JSONException je) {
        LOG.error("Error with activity's title data");
      }

    }

    NodeLocation nodeLocation = new NodeLocation(repository, workspace, docActivity.docPath);
    final Node docNode = NodeLocation.getNodeByLocation(nodeLocation);
    docActivity.setDocNode(docNode);
  }

  private void saveToNewDataFormat(ExoSocialActivity activity, UIDocActivity docActivity, String repository , String workspace) {
    final String docActivityTitle = "Shared a document <a href=\"${"+ UIDocActivity.DOCLINK +"}\">${" +UIDocActivity.DOCNAME +"}</a>";
    activity.setTitle(docActivityTitle);
    Map<String, String> activityParams = new HashMap<String, String>();
    activityParams.put(UIDocActivity.DOCNAME, docActivity.docName);
    activityParams.put(UIDocActivity.DOCLINK, docActivity.docLink);
    activityParams.put(UIDocActivity.DOCPATH, docActivity.docPath);
    activityParams.put(UIDocActivity.REPOSITORY, repository);
    activityParams.put(UIDocActivity.WORKSPACE, workspace);
    activityParams.put(UIDocActivity.MESSAGE, docActivity.message);
    activity.setTemplateParams(activityParams);
    ActivityManager activityManager = (ActivityManager) PortalContainer.getInstance().getComponentInstanceOfType(ActivityManager.class);
    try {
      activityManager.saveActivity(activity);
    } catch (ActivityStorageException ase) {
      LOG.warn("Could not save new data format for document activity.", ase);
    }
  }
}