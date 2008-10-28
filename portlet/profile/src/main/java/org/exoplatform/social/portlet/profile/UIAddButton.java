package org.exoplatform.social.portlet.profile;

import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;

@ComponentConfig(
    template =  "app:/groovy/portal/webui/component/UIAddButton.gtmpl"
)
public class UIAddButton  extends UIComponent {
  public UIAddButton() {

  }

  public String getCurrentProperty() {
    UIProfileSection ps = this.getAncestorOfType(UIProfileSection.class);
    return ps.getCurrentProperty();
  }
}
