package org.exoplatform.social.portlet.profile;

import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.config.annotation.ComponentConfig;


@ComponentConfig(
    template =  "app:/groovy/portal/webui/component/UIContactSection.gtmpl",
    events = {
        @EventConfig(listeners = UIProfileSection.EditActionListener.class),
        @EventConfig(listeners = UIProfileSection.SaveActionListener.class),
        @EventConfig(listeners = UIProfileSection.CancelActionListener.class)
    }
)
public class UIContactSection extends UIProfileSection {

  public UIContactSection() throws Exception {
    addChild(UITitleBar.class, null, null);
    addChild(UIAddButton.class, null, null);
  }

}