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

import javax.jcr.Node;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.webui.activity.BaseUIActivity;
import org.exoplatform.social.webui.activity.UIActivitiesContainer;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SAS
 * Author : Zun
 *          exo@exoplatform.com
 * Jul 23, 2010  
 */

@ComponentConfig(
   lifecycle = UIFormLifecycle.class,
     template = "classpath:groovy/social/plugin/doc/UIDocActivity.gtmpl",
   events = {
     @EventConfig(listeners = UIDocActivity.ViewDocumentActionListener.class),
     @EventConfig(listeners = BaseUIActivity.ToggleDisplayLikesActionListener.class),
     @EventConfig(listeners = BaseUIActivity.ToggleDisplayCommentFormActionListener.class),
     @EventConfig(listeners = BaseUIActivity.LikeActivityActionListener.class),
     @EventConfig(listeners = BaseUIActivity.SetCommentListStatusActionListener.class),
     @EventConfig(listeners = BaseUIActivity.PostCommentActionListener.class),
     @EventConfig(listeners = BaseUIActivity.DeleteActivityActionListener.class, confirm = "UIActivity.msg.Are_You_Sure_To_Delete_This_Activity"),
     @EventConfig(listeners = BaseUIActivity.DeleteCommentActionListener.class, confirm = "UIActivity.msg.Are_You_Sure_To_Delete_This_Comment")
   }
 )
public class UIDocActivity extends BaseUIActivity {
  private static final Log LOG = ExoLogger.getLogger(UIDocActivity.class);
  
  public static final String ACTIVITY_TYPE = "DOC_ACTIVITY";
  public static final String DOCLINK = "DOCLINK";
  public static final String MESSAGE = "MESSAGE";
  public static final String REPOSITORY = "REPOSITORY";
  public static final String WORKSPACE = "WORKSPACE";
  public static final String DOCNAME = "DOCNAME";
  public static final String DOCPATH = "DOCPATH";
  
  public String docLink;
  public String message;
  public String docName;
  public String docPath;
  private Node docNode;

  public UIDocActivity() {
  }

  public void setDocNode(Node docNode) {
    this.docNode = docNode;
  }

  public Node getDocNode() {
    return docNode;
  }

  public static class ViewDocumentActionListener extends EventListener<UIDocActivity> {
    @Override
    public void execute(Event<UIDocActivity> event) throws Exception {
      final UIDocActivity docActivity = event.getSource();
      final UIActivitiesContainer activitiesContainer = docActivity.getParent();
      final UIPopupWindow popupWindow = activitiesContainer.getPopupWindow();

      UIDocViewer docViewer = popupWindow.createUIComponent(UIDocViewer.class, null, "DocViewer");
      final Node docNode = docActivity.getDocNode();
      docViewer.setOriginalNode(docNode);
      docViewer.setNode(docNode);

      popupWindow.setUIComponent(docViewer);
      popupWindow.setWindowSize(800, 600);
      popupWindow.setShow(true);
      popupWindow.setResizable(true);

      event.getRequestContext().addUIComponentToUpdateByAjax(activitiesContainer);
    }
  }
}