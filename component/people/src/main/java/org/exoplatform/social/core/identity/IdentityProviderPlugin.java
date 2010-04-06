package org.exoplatform.social.core.identity;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValuesParam;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

public class IdentityProviderPlugin extends BaseComponentPlugin {

  List<IdentityProvider<?>> providers;

  private static Log        LOG = ExoLogger.getExoLogger(IdentityProviderPlugin.class);

  @SuppressWarnings("unchecked")
  public IdentityProviderPlugin(InitParams initParams) {
    providers = new ArrayList<IdentityProvider<?>>();

    ValuesParam values = initParams.getValuesParam("providers");
    if (values == null) {
      LOG.warn("Missing expected <values-param>. : providers");
      return;
    }
    ArrayList<String> classes = values.getValues();
    for (String className : classes) {
      try {
        Class t = Class.forName(className);
        if (IdentityProvider.class.isAssignableFrom(t)) {
          IdentityProvider provider = (IdentityProvider) ExoContainerContext.getCurrentContainer()
                                                                            .getComponentInstanceOfType(t);
          if (provider != null) {
            providers.add(provider);
          } else {
            LOG.warn("No component of type " + className + " found in ExoContainer");
          }

        } else {
          LOG.warn(className + " must be of type " + IdentityProvider.class);

        }
      } catch (Exception e) {
        LOG.error("Failed to instanciate provider of type " + className, e);
      }
    }
  }

  public List<IdentityProvider<?>> getProviders() {
    return providers;
  }

}
