package org.exoplatform.social.providers;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.springframework.beans.factory.FactoryBean;

public class SpaceServiceBean implements FactoryBean<SpaceService>{

  public SpaceService getObject() throws Exception {
    return (SpaceService) PortalContainer.getInstance().getComponentInstanceOfType(SpaceService.class);
  }

  public Class<SpaceService> getObjectType() {
    return SpaceService.class;
  }

  public boolean isSingleton() {
    return false;
  }

}
