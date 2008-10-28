package org.exoplatform.social.portlet.profilelist;

import org.exoplatform.portal.webui.application.UIPortlet;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;
import org.exoplatform.webui.core.UIPortletApplication;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.IdentityManager;
import org.exoplatform.social.core.identity.impl.organization.OrganizationIdentityProvider;
import org.exoplatform.social.core.relationship.RelationshipManager;
import org.exoplatform.social.core.relationship.Relationship;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.web.application.RequestContext;

import java.util.List;
import java.util.ArrayList;


@ComponentConfig(
    lifecycle = UIApplicationLifecycle.class,
    template = "app:/groovy/portal/webui/component/UIProfileListPortlet.gtmpl"
)
public class UIProfileListPortlet extends UIPortletApplication {

    public UIProfileListPortlet() throws Exception {
      addChild(UIProfileList.class, null, null);
    }


}
