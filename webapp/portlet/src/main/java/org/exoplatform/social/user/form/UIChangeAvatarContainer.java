package org.exoplatform.social.user.form;

import java.io.Writer;

import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.service.LinkProvider;
import org.exoplatform.social.user.portlet.UserProfileHelper;
import org.exoplatform.social.webui.UIAvatarUploader;
import org.exoplatform.social.webui.Utils;
import org.exoplatform.social.webui.composer.PopupContainer;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPortletApplication;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIFormInputSet;

@ComponentConfig(
  events = {
    @EventConfig(listeners = UIChangeAvatarContainer.ChangeAvatarActionListener.class)
  }
)
public class UIChangeAvatarContainer extends UIFormInputSet {
  
  public UIChangeAvatarContainer() throws Exception {
    
  }

  @Override
  public void processRender(WebuiRequestContext context) throws Exception {
    Profile ownerProfile = Utils.getViewerIdentity(true).getProfile();
    String fullName = ownerProfile.getFullName();
    String avatarURL = ownerProfile.getAvatarUrl();
    if(UserProfileHelper.isEmpty(avatarURL)) {
      avatarURL = LinkProvider.PROFILE_DEFAULT_AVATAR_URL;
    }
    Writer writer = context.getWriter();
    writer.append("<div class=\"uiRelationshipAction clearfix\" id=\"").append(getId()).append("\">");
    writer.append("  <div class=\"avatarLarge pull-left\">")
          .append("    <img title=\"").append(fullName).append("\" alt=\"").append(fullName).append("\" src=\"").append(avatarURL).append("\"/>")
          .append("  </div>");
    writer.append("  <div class=\"breadcumbEntry pull-left\">")
          .append("    <a class=\"changeAvatar\" onclick=\"").append(event("ChangeAvatar")).append("\" href=\"javascript:void(0)\">")
          .append(UserProfileHelper.getLabel(context, "UIAvatarUploadContent.label.ChangeAvatar")).append("</a>")
          .append("  </div>");
    writer.append("</div>");
  }
 
  public static class ChangeAvatarActionListener extends EventListener<UIChangeAvatarContainer> {
    @Override
    public void execute(Event<UIChangeAvatarContainer> event) throws Exception {
      UIChangeAvatarContainer avatarContainer = event.getSource();
      PopupContainer uiPopup = avatarContainer.getAncestorOfType(UIPortletApplication.class).getChild(PopupContainer.class);
      uiPopup.activate(UIAvatarUploader.class, 500, "UIWindowAvatarUploader");
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopup);
    }
  }
}
