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

import org.exoplatform.social.core.activity.model.Activity;
import org.exoplatform.social.webui.activity.BaseUIActivity;
import org.exoplatform.social.webui.activity.BaseUIActivityBuilder;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Jul 23, 2010  
 */
public class DocUIActivityBuilder extends BaseUIActivityBuilder {
  @Override
  protected void extendUIActivity(BaseUIActivity uiActivity, Activity activity) {
    UIDocActivity docActivity = (UIDocActivity) uiActivity;
    final String JSONdata = activity.getTitle();
    try {
      final JSONObject jsonObject = new JSONObject(JSONdata);
      docActivity.documentRefPath = jsonObject.getString(UIDocActivity.REFPATH);
      docActivity.message = jsonObject.getString(UIDocActivity.MESSAGE);      
    } catch (JSONException e) {
      e.printStackTrace();
    }
  }
}
