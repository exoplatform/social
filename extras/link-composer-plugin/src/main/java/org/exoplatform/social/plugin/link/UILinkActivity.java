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

import org.apache.commons.lang.StringEscapeUtils;
import org.exoplatform.social.webui.activity.BaseUIActivity;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;

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
     @EventConfig(listeners = BaseUIActivity.PostCommentActionListener.class),
     @EventConfig(listeners = BaseUIActivity.DeleteActivityActionListener.class,
                  confirm = "UIActivity.msg.Are_You_Sure_To_Delete_This_Activity"),
     @EventConfig(listeners = BaseUIActivity.DeleteCommentActionListener.class,
                  confirm = "UIActivity.msg.Are_You_Sure_To_Delete_This_Comment")
   }
 )
public class UILinkActivity extends BaseUIActivity {
  public static final String ACTIVITY_TYPE = "LINK_ACTIVITY";
  
  private String linkSource = "";
  private String linkTitle = "";
  private String linkImage = "";
  private String linkDescription = "";
  private String linkComment = "";
  
  public String getLinkComment() {
    return linkComment;
  }
  public void setLinkComment(String linkComment) {
    this.linkComment = linkComment;
  }
  public String getLinkDescription() {
    return linkDescription;
  }
  public void setLinkDescription(String linkDescription) {
    this.linkDescription = linkDescription;
  }
  public String getLinkImage() {
    return linkImage;
  }
  public void setLinkImage(String linkImage) {
    this.linkImage = linkImage;
  }
  public String getLinkSource() {
    return linkSource;
  }
  public void setLinkSource(String linkSource) {
    this.linkSource = linkSource;
  }
  public String getLinkTitle() {
    return StringEscapeUtils.escapeHtml(linkTitle);
  }
  public void setLinkTitle(String linkTitle) {                
    this.linkTitle = linkTitle;
  }
}
