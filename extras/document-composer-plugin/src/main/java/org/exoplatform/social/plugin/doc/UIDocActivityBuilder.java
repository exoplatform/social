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

import java.util.Map;

import javax.jcr.Node;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.wcm.core.NodeLocation;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.webui.activity.BaseUIActivity;
import org.exoplatform.social.webui.activity.BaseUIActivityBuilder;

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
    try {
      Map<String,String> activityParams = activity.getTemplateParams();
      docActivity.docLink = activityParams.get(UIDocActivity.DOCLINK);
      docActivity.docName = activityParams.get(UIDocActivity.DOCNAME);
      docActivity.message = activityParams.get(UIDocActivity.MESSAGE);
      docActivity.docPath = activityParams.get(UIDocActivity.DOCPATH);
      String repository = activityParams.get(UIDocActivity.REPOSITORY);
      String workspace = activityParams.get(UIDocActivity.WORKSPACE);
      
      NodeLocation nodeLocation = new NodeLocation(repository, workspace, docActivity.docPath);
      final Node docNode = NodeLocation.getNodeByLocation(nodeLocation);
      docActivity.setDocNode(docNode);
    } catch (Exception e) {
      LOG.error("Binding activity data error : ", e);
    }
  }
}