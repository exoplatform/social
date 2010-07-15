/*
 * Copyright (C) 2003-2010 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.social.opensocial.model;

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
  public static final String CALLBACK_URL = "callbackUrl";

  private static Log log = ExoLogger.getExoLogger(ServiceProviderStore.class);

  private Map<String,ServiceProviderData> providers;


  public ServiceProviderStore(InitParams params) {
    providers =  new HashMap<String, ServiceProviderData>();
    try {
      if (params == null) {
        return;
      }
    Iterator<PropertiesParam> it = params.getPropertiesParamIterator();
    while (it.hasNext()) {
      PropertiesParam propertiesParam = (PropertiesParam) it.next();
      String name = propertiesParam.getName();
      String description = propertiesParam.getDescription();
      String consumerKey = propertiesParam.getProperty(CONSUMER_KEY);
      String sharedSecret = propertiesParam.getProperty(SHARED_SECRET);
      String callbackUrl = propertiesParam.getProperty(CALLBACK_URL);
      ServiceProviderData provider = new ServiceProviderData(name, description, consumerKey, sharedSecret, callbackUrl);
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
