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

import org.apache.shindig.social.core.config.SocialApiGuiceModule;
import org.apache.shindig.social.opensocial.model.Person;
import org.apache.shindig.social.opensocial.oauth.OAuthDataStore;
import org.apache.shindig.social.opensocial.spi.ActivityService;
import org.apache.shindig.social.opensocial.spi.AppDataService;
import org.apache.shindig.social.opensocial.spi.PersonService;
import org.exoplatform.social.opensocial.model.impl.ExoPersonImpl;
import org.exoplatform.social.opensocial.oauth.EXoOAuthDataStore;
import org.exoplatform.social.opensocial.spi.ExoActivityService;
import org.exoplatform.social.opensocial.spi.ExoPeopleService;

import com.google.inject.name.Names;

public class ExoSocialApiGuiceModule  extends SocialApiGuiceModule {

  public ExoSocialApiGuiceModule() {
    super();
  }

  /** {@inheritDoc} */
  @Override
  protected void configure() {
    super.configure();

    bind(String.class).annotatedWith(Names.named("shindig.canonical.json.db"))
    .toInstance("sampledata/canonicaldb.json");
    bind(PersonService.class).to(ExoPeopleService.class);
    bind(AppDataService.class).to(ExoPeopleService.class);
    bind(ActivityService.class).to(ExoActivityService.class);

    bind(Person.class).to(ExoPersonImpl.class);
    bind(OAuthDataStore.class).to(EXoOAuthDataStore.class);
    
  }


}