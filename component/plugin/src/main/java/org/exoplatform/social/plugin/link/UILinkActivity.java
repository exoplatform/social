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

import org.apache.commons.lang.Validate;
import org.exoplatform.social.core.activity.model.Activity;
import org.exoplatform.social.webui.activity.BaseUIActivity;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Jul 23, 2010  
 */

@ComponentConfig(
   lifecycle = UIFormLifecycle.class,
   template = "classpath:groovy/social/plugin/link/UILinkActivity.gtmpl",
   events = {
     @EventConfig(listeners = BaseUIActivity.ToggleDisplayLikesActionListener.class),
     @EventConfig(listeners = BaseUIActivity.ToggleDisplayCommentFormActionListener.class),
     @EventConfig(listeners = BaseUIActivity.LikeActivityActionListener.class),
     @EventConfig(listeners = BaseUIActivity.SetCommentListStatusActionListener.class),
     @EventConfig(listeners = BaseUIActivity.PostCommentActionListener.class)
   }
 )
public class UILinkActivity extends BaseUIActivity {
  public static final String ACTIVITY_TYPE = "LINK_ACTIVITY";
  
  private JSONObject titleData;
  private static final String DATA_KEY = "data";

  public String getLinkSource() throws JSONException {
    Activity activity = getActivity();
    Validate.notNull(activity.getTitle(), "activity_.getTitle() must not be null.");
    titleData = new JSONObject(activity.getTitle());

    if (titleData != null) {
      return titleData.getString(UILinkActivityComposer.LINK_PARAM);
    }
    return "";
  }
  
  public String getLinkTitle() throws JSONException {
    Activity activity = getActivity();
    Validate.notNull(activity.getTitle(), "activity_.getTitle() must not be null.");
    titleData = new JSONObject(activity.getTitle());

    if (titleData != null) {
      return titleData.getString(UILinkActivityComposer.TITLE_PARAM);
    }
    return "";
  }
  
  public String getLinkImage() throws JSONException {
    Activity activity = getActivity();
    Validate.notNull(activity.getTitle(), "activity_.getTitle() must not be null.");
    titleData = new JSONObject(activity.getTitle());

    if (titleData != null) {
      return titleData.getString(UILinkActivityComposer.IMAGE_PARAM);
    }
    return "";
  }
  
  public String getLinkDescription() throws JSONException {
    Activity activity = getActivity();
    Validate.notNull(activity.getTitle(), "activity_.getTitle() must not be null.");
    titleData = new JSONObject(activity.getTitle());

    if (titleData != null) {
      return titleData.getString(UILinkActivityComposer.DESCRIPTION_PARAM);
    }
    return "";
  }
  
  public String getLinkComment() throws JSONException {
    Activity activity = getActivity();
    Validate.notNull(activity.getTitle(), "activity_.getTitle() must not be null.");
    titleData = new JSONObject(activity.getTitle());

    if (titleData != null) {
      return titleData.getString(UILinkActivityComposer.COMMENT_PARAM);
    }
    return "";
  }

}
