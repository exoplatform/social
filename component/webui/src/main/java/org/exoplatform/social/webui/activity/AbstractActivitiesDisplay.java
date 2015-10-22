package org.exoplatform.social.webui.activity;

import org.exoplatform.webui.core.UIContainer;

public abstract class AbstractActivitiesDisplay extends UIContainer {
  protected boolean isRenderFull = false;

  public boolean isRenderFull() {
    return isRenderFull;
  }

  public void setRenderFull(boolean isRenderFull) {
    this.isRenderFull = isRenderFull;
  }

  public void init() throws Exception {
  }
}
