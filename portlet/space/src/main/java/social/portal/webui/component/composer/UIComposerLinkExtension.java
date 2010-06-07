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
package social.portal.webui.component.composer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.exoplatform.portal.webui.container.UIContainer;
import org.exoplatform.social.services.rest.opensocial.LinkShare;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIFormStringInput;
import org.json.JSONObject;

import social.portal.webui.component.composer.UIComposerExtensionContainer.Extension;

/**
 * UIComposerLinkExtension.java
 * <p>
 * an ui component to attach link, gets link information and displays; changes link title,
 * description content inline.
 * </p>
 *
 * @author    <a href="http://hoatle.net">hoatle</a>
 * @since 	  Apr 19, 2010
 * @copyright eXo Platform SAS
 */
@ComponentConfig(
  template = "app://groovy/portal/webui/component/composer/UIComposerLinkExtension.gtmpl",
  events = {
    @EventConfig(listeners = UIComposerLinkExtension.AttachActionListener.class),
    @EventConfig(listeners = UIComposerLinkExtension.ChangeLinkContentActionListener.class)
  }
)
public class UIComposerLinkExtension extends UIContainer {
  public static final String LINK_PARAM = "link";
  public static final String IMAGE_PARAM = "image";
  public static final String TITLE_PARAM = "title";
  public static final String DESCRIPTION_PARAM = "description";

  private static final String MSG_ERROR_ATTACH_LINK = "UIComposerLinkExtension.msg.error.Attach_Link";
  private static final String HTTP = "http://";
  private static final String HTTPS = "https://";
  private LinkShare linkShare_;
  private boolean linkInfoDisplayed_ = false;
  private JSONObject dataLink_;

  /**
   * constructor
   */
  public UIComposerLinkExtension() {
    addChild(new UIFormStringInput("InputLink", "InputLink", null));
  }

  public void setLinkInfoDisplayed(boolean displayed) {
    linkInfoDisplayed_ = displayed;
  }

  public boolean isLinkInfoDisplayed() {
    return linkInfoDisplayed_;
  }

  public void setDataLink(JSONObject dataLink) {
    dataLink_ = dataLink;
  }

  public JSONObject getDataLink() {
    return dataLink_;
  }

  public void clearLinkShare() {
    linkShare_ = null;
  }

  public LinkShare getLinkShare() {
    return linkShare_;
  }

  /**
   * sets link url to gets content
   * @param url
   * @throws Exception
   */
  private void setLink(String url) throws Exception {
    if (!(url.contains(HTTP) || url.contains(HTTPS))) {
      url = HTTP + url;
    }
    linkShare_ = LinkShare.getInstance(url);
    dataLink_ = new JSONObject();
    dataLink_.put(LINK_PARAM, linkShare_.getLink());
    String image = "";
    List<String> images = linkShare_.getImages();
    if (images != null && images.size() > 0) {
      image = images.get(0);
    }
    dataLink_.put(IMAGE_PARAM, image);
    dataLink_.put(TITLE_PARAM, linkShare_.getTitle());
    dataLink_.put(DESCRIPTION_PARAM, linkShare_.getDescription());
    UIComposerExtensionContainer uiComposerExtensionContainer = getAncestorOfType(UIComposerExtensionContainer.class);
    if (uiComposerExtensionContainer != null) {
      uiComposerExtensionContainer.setExtensionAttached(true);
      Map<Extension, JSONObject> attachedData = new HashMap<Extension, JSONObject>();
      attachedData.put(Extension.LINK, dataLink_);
      uiComposerExtensionContainer.setData(attachedData);
    }
    setLinkInfoDisplayed(true);
  }

  static public class AttachActionListener extends EventListener<UIComposerLinkExtension> {

    @Override
    public void execute(Event<UIComposerLinkExtension> event) throws Exception {
      WebuiRequestContext requestContext = event.getRequestContext();
      UIApplication uiApplication = requestContext.getUIApplication();
      UIComposerLinkExtension uiComposerLinkExtension = event.getSource();
      String url = requestContext.getRequestParameter(OBJECTID);
      try {
        uiComposerLinkExtension.setLink(url);
      } catch (Exception e) {
        uiApplication.addMessage(new ApplicationMessage(MSG_ERROR_ATTACH_LINK, null, ApplicationMessage.ERROR));
        return;
      }
      requestContext.addUIComponentToUpdateByAjax(uiComposerLinkExtension);
    }
  }

  static public class ChangeLinkContentActionListener extends EventListener<UIComposerLinkExtension> {

    @Override
    public void execute(Event<UIComposerLinkExtension> event) throws Exception {
      WebuiRequestContext requestContext = event.getRequestContext();
      UIComposerLinkExtension uiComposerLinkExtension = event.getSource();
      JSONObject dataLink = new JSONObject();
      dataLink.put(LINK_PARAM, requestContext.getRequestParameter(LINK_PARAM));
      dataLink.put(IMAGE_PARAM, requestContext.getRequestParameter(IMAGE_PARAM));
      dataLink.put(TITLE_PARAM, requestContext.getRequestParameter(TITLE_PARAM));
      dataLink.put(DESCRIPTION_PARAM, requestContext.getRequestParameter(DESCRIPTION_PARAM));
      uiComposerLinkExtension.setDataLink(dataLink);
      requestContext.addUIComponentToUpdateByAjax(uiComposerLinkExtension);
      UIComponent uiParent = uiComposerLinkExtension.getParent();
      if (uiParent != null) {
        uiParent.broadcast(event, event.getExecutionPhase());
      }
    }
  }
}
