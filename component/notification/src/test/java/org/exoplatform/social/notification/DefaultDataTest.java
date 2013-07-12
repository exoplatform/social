/*
 * Copyright (C) 2003-2013 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
* modify it under the terms of the GNU Affero General Public License
* as published by the Free Software Foundation; either version 3
* of the License, or (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.social.notification;

import java.util.List;
import java.util.Random;

import org.exoplatform.commons.api.notification.ProviderData;
import org.exoplatform.commons.api.notification.plugin.MappingKey;
import org.exoplatform.commons.api.notification.plugin.ProviderPlugin;
import org.exoplatform.commons.api.notification.plugin.TemplateConfigurationPlugin;
import org.exoplatform.commons.api.notification.service.AbstractNotificationProvider;
import org.exoplatform.commons.api.notification.service.NotificationProviderService;
import org.exoplatform.commons.api.notification.service.ProviderService;
import org.exoplatform.commons.api.notification.service.TemplateGenerator;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ObjectParameter;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.notification.provider.SocialProviderImpl;

public class DefaultDataTest {

  public static ProviderService getProviderService() {
    ProviderService providerService = new ProviderService() {
      @Override
      public void saveProvider(ProviderData provider) {
      }
      @Override
      public void registerProviderPlugin(ProviderPlugin providerPlugin) {
      }
      @Override
      public ProviderData getProvider(String providerType) {
        return new ProviderData().setType(providerType);
      }
      @Override
      public List<ProviderData> getAllProviders() {
        return null;
      }
    };
    return providerService;
  }
  
  public static SocialProviderImpl getSocialProviderImpl(ActivityManager activityManager, IdentityManager identityManager,
                                                         SpaceService spaceService, TemplateGenerator templateGenerator) {
  SocialProviderImpl providerImpl = new SocialProviderImpl(activityManager,identityManager, spaceService, 
                             DefaultDataTest.getProviderService(), templateGenerator);

    NotificationProviderService providerService = CommonsUtils.getService(NotificationProviderService.class);
    providerService.addSupportProviderImpl((AbstractNotificationProvider)providerImpl);
    

    InitParams params = new InitParams();
    Random random = new Random(System.currentTimeMillis());
    for(String providerId : providerImpl.getSupportType()) {
      
      MappingKey mappingKey = new MappingKey();
      mappingKey.setProviderId(providerId);
      mappingKey.addKeyMapping(MappingKey.SUBJECT_KEY, "Notification." + providerId + ".subject")
                .addKeyMapping(MappingKey.TEMPLATE_KEY, "Notification." + providerId + ".template")
                .addKeyMapping(MappingKey.DIGEST_KEY, "Notification." + providerId + ".digest")
                .addKeyMapping(MappingKey.DIGEST_ONE_KEY, "Notification." + providerId + ".digestone")
                .addKeyMapping(MappingKey.DIGEST_MORE_KEY, "Notification." + providerId + ".digestmore")
                .addKeyMapping(MappingKey.DIGEST_THREE_KEY, "Notification." + providerId + ".digestthree");
      
      ObjectParameter parameter = new ObjectParameter();
      parameter.setName("pr" + random.nextLong());
      parameter.setObject(mappingKey);
      parameter.setDescription("");
      params.addParam(parameter);
      
    }
    TemplateConfigurationPlugin configurationPlugin = new TemplateConfigurationPlugin(params);
    templateGenerator.registerTemplateConfigurationPlugin(configurationPlugin);
    return providerImpl;
  }

}
