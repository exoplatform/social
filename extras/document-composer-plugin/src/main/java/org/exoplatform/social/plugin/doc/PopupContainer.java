package org.exoplatform.social.plugin.doc;

import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIPopupWindow;

/**
 * Created by IntelliJ IDEA.
 * User: zun
 * Date: Jul 29, 2010
 * Time: 2:19:15 PM
 * To change this template use File | Settings | File Templates.
 */
@ComponentConfig(
  template = "classpath:groovy/social/plugin/doc/PopupContainer.gtmpl"
)
public class PopupContainer extends UIContainer{
  private UIPopupWindow popupWindow;

  public PopupContainer() {
    try {
      popupWindow = addChild(UIPopupWindow.class, null, "UIPopupWindow");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public UIPopupWindow getPopupWindow() {
    return popupWindow;
  }

  @Override
  public void processRender(WebuiRequestContext context) throws Exception {
    if(!popupWindow.isRendered()){
      popupWindow.setRendered(true);
    }
    super.processRender(context);
  }
}
