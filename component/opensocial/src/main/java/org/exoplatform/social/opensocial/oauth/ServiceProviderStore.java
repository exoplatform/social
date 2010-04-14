package org.exoplatform.social.opensocial.oauth;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.PropertiesParam;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;


/**
 * Simple store for OAuth providers data.
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice Lamarque</a>
 * @version $Revision$
 */
public class ServiceProviderStore {

  public static final String SHARED_SECRET = "sharedSecret";
  public static final String CONSUMER_KEY = "consumerKey";
  private static Log log = ExoLogger.getExoLogger(ServiceProviderStore.class);
  private Map<String,ServiceProviderData> providers;
  

  public ServiceProviderStore(InitParams params) {
    providers =  new HashMap<String, ServiceProviderData>();
    try {

    Iterator<PropertiesParam> it = params.getPropertiesParamIterator();
    while (it.hasNext()) {
      PropertiesParam propertiesParam = (PropertiesParam) it.next();
      String name = propertiesParam.getName();
      String description = propertiesParam.getDescription();
      String consumerKey = propertiesParam.getProperty(CONSUMER_KEY);
      String sharedSecret = propertiesParam.getProperty(SHARED_SECRET);
      ServiceProviderData provider = new ServiceProviderData(name, description, consumerKey, sharedSecret);
      providers.put(consumerKey, provider);
    }

    } catch (Exception e) {
      log.error("failed to initialize properties from init-params", e);
    }
  }
  

  /**
   * Get the provider for a given consumer key
   * @param consumerKey
   * @return
   */
  public ServiceProviderData getServiceProvider(String consumerKey) {
    return providers.get(consumerKey);
  }


  public void setProviders(Map<String, ServiceProviderData> providers) {
    this.providers = providers;
  }

  
  
}
