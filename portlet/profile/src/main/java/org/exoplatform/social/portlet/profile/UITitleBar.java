package org.exoplatform.social.portlet.profile;

import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;


@ComponentConfig(
    template =  "app:/groovy/portal/webui/component/UITitleBar.gtmpl"
)
public class UITitleBar  extends UIComponent {

  public UITitleBar() throws Exception { }

  public String eventSubmit(String name) throws Exception {
    UIProfileSection pf = getParent();
    return pf.eventSubmit(name);
  }

  public String event(String name, String beanId) throws Exception {
    UIProfileSection pf = getParent();
    return pf.event(name, beanId);
  }

  public String getTranlationKey() {
    UIProfileSection pf = getParent();
    return pf.getName();
  }

  public boolean isEditable() {
    UIProfileSection pf = getParent();
    return pf.isEditable();
  }

  public boolean isEditMode() {
    UIProfileSection pf = getParent();
    return pf.isEditMode();
  }
}
