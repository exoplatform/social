/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
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
package org.exoplatform.social.opensocial;

import java.util.List;
import java.util.Set;

import org.apache.shindig.auth.AuthenticationHandler;
import org.apache.shindig.auth.SecurityTokenCodec;
import org.apache.shindig.common.servlet.ParameterFetcher;
import org.apache.shindig.config.ContainerConfig;
import org.apache.shindig.gadgets.http.HttpFetcher;
import org.apache.shindig.protocol.DataServiceServletFetcher;
import org.apache.shindig.protocol.conversion.BeanConverter;
import org.apache.shindig.protocol.conversion.BeanJsonConverter;
import org.apache.shindig.protocol.conversion.BeanXStreamConverter;
import org.apache.shindig.protocol.conversion.xstream.XStreamConfiguration;
import org.apache.shindig.social.core.oauth.OAuthAuthenticationHandler;
import org.apache.shindig.social.core.util.BeanXStreamAtomConverter;
import org.apache.shindig.social.core.util.xstream.XStream081Configuration;
import org.apache.shindig.social.opensocial.model.Person;
import org.apache.shindig.social.opensocial.oauth.OAuthDataStore;
import org.apache.shindig.social.opensocial.service.ActivityHandler;
import org.apache.shindig.social.opensocial.service.AppDataHandler;
import org.apache.shindig.social.opensocial.service.MessageHandler;
import org.apache.shindig.social.opensocial.service.PersonHandler;
import org.apache.shindig.social.opensocial.spi.ActivityService;
import org.apache.shindig.social.opensocial.spi.AppDataService;
import org.apache.shindig.social.opensocial.spi.PersonService;
import org.exoplatform.portal.gadget.core.ExoContainerConfig;
import org.exoplatform.portal.gadget.core.ExoHttpFetcher;
import org.exoplatform.social.opensocial.auth.ExoAuthenticationHandlerProvider;
import org.exoplatform.social.opensocial.auth.ExoOAuthAuthenticationHandler;
import org.exoplatform.social.opensocial.auth.ExoSecurityTokenDecoder;
import org.exoplatform.social.opensocial.model.ExoPersonImpl;
import org.exoplatform.social.opensocial.oauth.ExoOAuthDataStore;
import org.exoplatform.social.opensocial.service.ExoActivityService;
import org.exoplatform.social.opensocial.service.ExoPeopleService;

import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;


public class ExoSocialApiGuiceModule  extends AbstractModule {


  /** {@inheritDoc} */
  @Override
  protected void configure() {
    bind(ContainerConfig.class).to(ExoContainerConfig.class);
    bind(HttpFetcher.class).to(ExoHttpFetcher.class);
     
    bind(ParameterFetcher.class).annotatedWith(Names.named("DataServiceServlet"))
    .to(DataServiceServletFetcher.class);

    bind(Boolean.class).annotatedWith(Names.named("shindig.oauth.legacy-body-signing"))
        .toInstance(Boolean.TRUE);
    bind(XStreamConfiguration.class).to(XStream081Configuration.class);
    bind(BeanConverter.class).annotatedWith(Names.named("shindig.bean.converter.xml")).to(
        BeanXStreamConverter.class);
    bind(BeanConverter.class).annotatedWith(Names.named("shindig.bean.converter.json")).to(
        BeanJsonConverter.class);
    bind(BeanConverter.class).annotatedWith(Names.named("shindig.bean.converter.atom")).to(
        BeanXStreamAtomConverter.class);
    
    bind(SecurityTokenCodec.class).annotatedWith(Names.named("exo.auth.decoder")).to(ExoSecurityTokenDecoder.class);
    bind(OAuthAuthenticationHandler.class).annotatedWith(Names.named("exo.auth.handler")).
            to(ExoOAuthAuthenticationHandler.class);

    bind(new TypeLiteral<List<AuthenticationHandler>>(){}).toProvider(ExoAuthenticationHandlerProvider.class);

    bind(new TypeLiteral<Set<Object>>(){}).annotatedWith(Names.named("org.apache.shindig.social.handlers"))
    .toInstance(getHandlers());

    bind(String.class).annotatedWith(Names.named("shindig.canonical.json.db"))
    .toInstance("sampledata/canonicaldb.json");

    bind(PersonService.class).to(ExoPeopleService.class);
    bind(AppDataService.class).to(ExoPeopleService.class);
    bind(ActivityService.class).to(ExoActivityService.class);

    bind(Person.class).to(ExoPersonImpl.class);
    bind(OAuthDataStore.class).to(ExoOAuthDataStore.class);

  }



  /**
   * Hook to provide a Set of request handlers.  Subclasses may override
   * to add or replace additional handlers.
   */
  protected Set<Object> getHandlers() {
    return ImmutableSet.<Object>of(ActivityHandler.class, AppDataHandler.class,
        PersonHandler.class, MessageHandler.class);
  }

}
