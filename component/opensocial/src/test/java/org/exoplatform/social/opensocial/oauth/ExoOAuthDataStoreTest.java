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
package org.exoplatform.social.opensocial.oauth;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

public class ExoOAuthDataStoreTest extends TestCase {

  public void testGetConsumer() {
    EXoOAuthDataStore dataStore = new EXoOAuthDataStore(null, null, null);

    Map<String, ServiceProviderData> providers = new HashMap<String, ServiceProviderData>();
    providers.put("foo", new ServiceProviderData("p1",
                                                 null,
                                                 "foo",
                                                 "secret1",
                                                 "http://foo.callback"));
    providers.put("bar", new ServiceProviderData("p2", null, "bar", "secret2", null));

    ServiceProviderStore store = new ServiceProviderStore(null);
    store.setProviders(providers);
    dataStore.setProviderStore(store);

    assertNull(dataStore.getConsumer(null));
    assertNull(dataStore.getConsumer("zed"));

    assertEquals("secret1", dataStore.getConsumer("foo").consumerSecret);
    assertEquals("secret2", dataStore.getConsumer("bar").consumerSecret);

    assertEquals("http://foo.callback", dataStore.getConsumer("foo").callbackURL);
    assertNull(dataStore.getConsumer("bar").callbackURL);

  }

}
