package org.exoplatform.social.webui.activity;

import java.io.Writer;

import org.exoplatform.commons.serialization.api.annotations.Serialized;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.lifecycle.UIContainerLifecycle;
@ComponentConfig(lifecycle = UIContainerLifecycle.class)
@Serialized
public class UIActivityLoader extends UIContainer {
  public UIActivityLoader() {
  }

  @Override
  public void processRender(WebuiRequestContext context) throws Exception {
    if (getTemplate() != null) {
      super.processRender(context);
      return;
    }
    Writer writer = context.getWriter();
    writer.append("<div class=\"uiActivityLoader\" id=\"").append(getId()).append("\">");
    renderChildren(context);
    writer.append("</div>");
  }
}
