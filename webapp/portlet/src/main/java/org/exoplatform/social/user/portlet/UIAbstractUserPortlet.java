package org.exoplatform.social.user.portlet;

import javax.portlet.PortletMode;

import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.webui.Utils;
import org.exoplatform.webui.application.WebuiApplication;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.core.UIPortletApplication;
import org.json.JSONObject;

public abstract class UIAbstractUserPortlet extends UIPortletApplication {
  protected Profile currentProfile;// current user viewing

  public UIAbstractUserPortlet() throws Exception {
    super();
  }

  @Override
  public void processRender(WebuiApplication app, WebuiRequestContext context) throws Exception {
    PortletRequestContext portletReqContext = (PortletRequestContext) context;
    PortletMode portletMode = portletReqContext.getApplicationMode();
    if (portletMode == PortletMode.VIEW) {
      Identity ownerIdentity = Utils.getOwnerIdentity(true);
      currentProfile = ownerIdentity.getProfile();
    }
    //
    super.processRender(app, context);
  }

  protected boolean isOwner() {
    return Utils.isOwner();
  }

  protected String getCurrentRemoteId() {
    return currentProfile.getIdentity().getRemoteId();
  }

  protected void initProfilePopup() throws Exception {
    JSONObject object = new JSONObject();
    WebuiRequestContext context = WebuiRequestContext.getCurrentInstance();
    object.put("StatusTitle", UserProfileHelper.encodeURI(UserProfileHelper.getLabel(context, "UserProfilePopup.label.Loading")));
    String[] keys = new String[]{"Connect", "Confirm", "CancelRequest", "RemoveConnection", "Ignore"};
    for (int i = 0; i < keys.length; i++) {
      object.put(keys[i], UserProfileHelper.encodeURI(UserProfileHelper.getLabel(context, "UserProfilePopup.label." + keys[i])));
    }
    //
    context.getJavascriptManager().getRequireJS().require("SHARED/social-ui-profile", "profile")
           .addScripts("profile.initUserProfilePopup('" + getId() + "', " + object.toString() + ");");
  }
}
