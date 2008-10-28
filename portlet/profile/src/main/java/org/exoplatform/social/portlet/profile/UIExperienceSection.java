package org.exoplatform.social.portlet.profile;

import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.config.annotation.ComponentConfig;


@ComponentConfig(
    template =  "app:/groovy/portal/webui/component/UIExperienceSection.gtmpl",
    events = {
        @EventConfig(listeners = UIProfileSection.EditActionListener.class),
        @EventConfig(listeners = UIProfileSection.SaveActionListener.class),
        @EventConfig(listeners = UIProfileSection.CancelActionListener.class)
    }
)
public class UIExperienceSection extends UIProfileSection {

  public UIExperienceSection() throws Exception {
    addChild(UITitleBar.class, null, null);
    addChild(UIAddButton.class, null, null);
  }

}