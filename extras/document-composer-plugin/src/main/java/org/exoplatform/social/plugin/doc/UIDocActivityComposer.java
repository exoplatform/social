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

import org.exoplatform.container.PortalContainer;
import org.exoplatform.ecm.webui.selector.UISelectable;
import org.exoplatform.ecm.webui.tree.selectone.UIOneNodePathSelector;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.activity.model.Activity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.SpaceUtils;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.webui.composer.UIActivityComposer;
import org.exoplatform.social.webui.composer.UIComposer;
import org.exoplatform.social.webui.composer.UIComposer.PostContext;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.json.JSONObject;

/**
 * @author    <a href="http://hoatle.net">hoatle</a>
 * @since     Apr 19, 2010
 * @copyright eXo Platform SAS
 */
@ComponentConfig(
  template = "classpath:groovy/social/plugin/doc/UIDocActivityComposer.gtmpl",
  events = {
    @EventConfig(listeners = UIActivityComposer.CloseActionListener.class),
    @EventConfig(listeners = UIActivityComposer.SubmitContentActionListener.class),
    @EventConfig(listeners = UIActivityComposer.ActivateActionListener.class),
    @EventConfig(listeners = UIDocActivityComposer.SelectDocumentActionListener.class)
  }
)
public class UIDocActivityComposer extends UIActivityComposer implements UISelectable {
  private static final Log LOG = ExoLogger.getLogger(UIDocActivityComposer.class);
  private String documentRefPath;
  private boolean isDocumentReady;

  private final String repository = "repository";
  private final String workspace = "collaboration";
  private String rootpath;
  private final String POPUP_COMPOSER = "UIPopupComposer";

  /**
   * constructor
   */
  public UIDocActivityComposer() {
    resetValues();
  }

  private void resetValues() {
    documentRefPath = "";
    isDocumentReady = false;
    setReadyForPostingActivity(false);
  }

  public String getDocumentRefPath() {
    return documentRefPath;
  }

  public boolean isDocumentReady() {
    return isDocumentReady;
  }

  @Override
  protected void onActivate(Event<UIActivityComposer> event) {
    isDocumentReady = false;
    rootpath = "/Users/"+ event.getRequestContext().getRemoteUser();
    final UIDocActivityComposer docActivityComposer = (UIDocActivityComposer) event.getSource();
    showDocumentPopup(docActivityComposer);
  }

  private void showDocumentPopup(UIDocActivityComposer docActivityComposer) {
    UIComposer uiComposer = docActivityComposer.getAncestorOfType(UIComposer.class);
    final UIContainer optionContainer = uiComposer.getOptionContainer();
    optionContainer.removeChild(UIPopupWindow.class);

    UIPopupWindow uiPopup = null;
    try {
      uiPopup = optionContainer.addChild(UIPopupWindow.class, null, POPUP_COMPOSER);
    } catch (Exception e) {
      LOG.error(e);
    }
    uiPopup.setWindowSize(600, 600);
    UIOneNodePathSelector uiOneNodePathSelector;
    try {
      uiOneNodePathSelector = uiPopup.createUIComponent(UIOneNodePathSelector.class, null, null);

      uiOneNodePathSelector.setRootNodeLocation(repository, workspace, rootpath);
      uiOneNodePathSelector.init(SessionProviderFactory.createSessionProvider());

      uiPopup.setUIComponent(uiOneNodePathSelector);
      uiOneNodePathSelector.setSourceComponent(this, null);
      uiPopup.setShow(true);
      uiPopup.setResizable(true);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  protected void onClose(Event<UIActivityComposer> event) {
    resetValues();
  }

  @Override
  protected void onSubmit(Event<UIActivityComposer> event) {
  }

  @Override
  public void onPostActivity(PostContext postContext, UIComponent source, WebuiRequestContext requestContext, String postedMessage) throws Exception {
    if(!isDocumentReady){
      requestContext.getUIApplication().addMessage(new ApplicationMessage("You have to choose document first!!!", null, ApplicationMessage.INFO));
    } else {
      if(postContext == UIComposer.PostContext.SPACE){
        UIApplication uiApplication = requestContext.getUIApplication();
        final UIComposer uiComposer = (UIComposer) source;

        JSONObject jsonData = new JSONObject();
        jsonData.put(UIDocActivity.REFPATH, documentRefPath);
        jsonData.put(UIDocActivity.MESSAGE, postedMessage);

        String activityJSONData = jsonData.toString();
        if (activityJSONData.equals("")) {
          uiApplication.addMessage(new ApplicationMessage("UIComposer.msg.error.Empty_Message",
                                                        null,
                                                        ApplicationMessage.ERROR));
        } else {
          String member = requestContext.getRemoteUser();

          SpaceService spaceSrv = uiComposer.getApplicationComponent(SpaceService.class);
          Space space = spaceSrv.getSpaceByUrl(SpaceUtils.getSpaceUrl());

          ActivityManager activityManager = uiComposer.getApplicationComponent(ActivityManager.class);
          IdentityManager identityManager = uiComposer.getApplicationComponent(IdentityManager.class);
          Identity spaceIdentity = identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME,
                                                                 space.getId(),
                                                                 false);
          Identity userIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, member);
          Activity activity = new Activity(userIdentity.getId(),
                                       SpaceService.SPACES_APP_ID,
                                       activityJSONData,
                                       null);
          activity.setType(UIDocActivity.ACTIVITY_TYPE);
          activityManager.saveActivity(spaceIdentity, activity);
        }
      }
    }

    resetValues();
  }

  public void doSelect(String selectField, Object value) throws Exception {
    String documentPath = value.toString();
    documentRefPath = documentPath.substring(documentPath.indexOf(":/") + 2);
    documentRefPath = buildDocumentLink(documentRefPath);
    isDocumentReady = true;

    setReadyForPostingActivity(true);
  }

  private String buildDocumentLink(String documentPath) {
    String portalContainerName = PortalContainer.getCurrentPortalContainerName();
    String restContextName = PortalContainer.getCurrentRestContextName();
    String restService = "jcr";
    return new StringBuilder().append("/").append(portalContainerName)
                                            .append("/").append(restContextName)
                                            .append("/").append(restService)
                                            .append("/").append(repository)
                                            .append("/").append(workspace)
                                            .append(rootpath)
                                            .append(documentPath).toString();
  }

  public static class SelectDocumentActionListener  extends EventListener<UIDocActivityComposer> {
    @Override
    public void execute(Event<UIDocActivityComposer> event) throws Exception {
      final UIDocActivityComposer docActivityComposer = event.getSource();
      docActivityComposer.rootpath = "/Users/"+ event.getRequestContext().getRemoteUser();
      docActivityComposer.showDocumentPopup(docActivityComposer);
    }
  }
}