package org.exoplatform.social.portlet.profile;

import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.config.annotation.ComponentConfig;


@ComponentConfig(
    template =  "app:/groovy/portal/webui/component/UIHeaderSection.gtmpl",
    events = {
        @EventConfig(listeners = UIProfileSection.EditActionListener.class),
        @EventConfig(listeners = UIProfileSection.SaveActionListener.class)
    }
)
public class UIHeaderSection extends UIProfileSection {

  public UIHeaderSection() throws Exception { }

}
