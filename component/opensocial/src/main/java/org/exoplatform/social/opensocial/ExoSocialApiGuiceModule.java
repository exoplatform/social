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

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import org.apache.shindig.social.opensocial.service.DataServiceServletFetcher;
import org.apache.shindig.social.opensocial.service.BeanConverter;
import org.apache.shindig.social.opensocial.service.HandlerDispatcher;
import org.apache.shindig.social.opensocial.spi.PersonService;
import org.apache.shindig.social.opensocial.spi.AppDataService;
import org.apache.shindig.social.opensocial.spi.ActivityService;
import org.apache.shindig.social.core.util.BeanXmlConverter;
import org.apache.shindig.social.core.util.BeanJsonConverter;
import org.apache.shindig.social.core.util.BeanAtomConverter;
import org.apache.shindig.social.core.oauth.AuthenticationHandlerProvider;
import org.apache.shindig.social.core.config.SocialApiGuiceModule;
import org.apache.shindig.common.servlet.ParameterFetcher;
import org.apache.shindig.auth.AnonymousAuthenticationHandler;
import org.apache.shindig.auth.AuthenticationHandler;
import org.exoplatform.social.opensocial.spi.ExoPeopleService;
import org.exoplatform.social.opensocial.spi.ExoActivityService;

import java.util.List;

public class ExoSocialApiGuiceModule  extends SocialApiGuiceModule {

  public ExoSocialApiGuiceModule() {
    super();
  }

  /** {@inheritDoc} */
  @Override
  protected void configure() {
    super.configure();
//    bind(HandlerProvider.class).to(ExoContainerHandlerProvider.class);
    /*bind(HandlerDispatcher.class).toProvider(HandlerDispatcherProvider.class);

    bind(ParameterFetcher.class).annotatedWith(Names.named("DataServiceServlet"))
        .to(DataServiceServletFetcher.class);

    bind(String.class).annotatedWith(Names.named("shindig.canonical.json.db"))
        .toInstance("sampledata/canonicaldb.json");

    bind(Boolean.class)
        .annotatedWith(Names.named(AnonymousAuthenticationHandler.ALLOW_UNAUTHENTICATED))
        .toInstance(Boolean.TRUE);

    bind(BeanConverter.class).annotatedWith(Names.named("shindig.bean.converter.xml")).to(
        BeanXmlConverter.class);
    bind(BeanConverter.class).annotatedWith(Names.named("shindig.bean.converter.json")).to(
        BeanJsonConverter.class);
    bind(BeanConverter.class).annotatedWith(Names.named("shindig.bean.converter.atom")).to(
        BeanAtomConverter.class);

    bind(new TypeLiteral<List<AuthenticationHandler>>(){}).toProvider(
        AuthenticationHandlerProvider.class);   */
//--------------------------------------------------------------

    bind(PersonService.class).to(ExoPeopleService.class);
    bind(AppDataService.class).to(ExoPeopleService.class);
    bind(ActivityService.class).to(ExoActivityService.class);
//    bind(ExoContainerHandler.class).to(ExoContainerHandler.class);

  }


}