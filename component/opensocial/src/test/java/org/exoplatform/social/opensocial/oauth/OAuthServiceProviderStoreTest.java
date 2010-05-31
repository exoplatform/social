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

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.PropertiesParam;

import junit.framework.TestCase;

public class OAuthServiceProviderStoreTest extends TestCase {

  public void testGetConsumer() {
    InitParams params = new InitParams();
    addProviderConfig(params, "p1", "foo", "secret1");
    addProviderConfig(params, "p2", "bar", "secret2");
    ServiceProviderStore store = new ServiceProviderStore(params);

    assertNull(store.getServiceProvider(null));
    assertNull(store.getServiceProvider("zed"));

    assertEquals("secret1", store.getServiceProvider("foo").getSharedSecret());
    assertEquals("secret2", store.getServiceProvider("bar").getSharedSecret());

  }

  /**
   * adds a properties-param according to what is expected by the
   * {@link ServiceProviderStore}
   *
   * @param params
   * @param name
   * @param consumer
   * @param secret
   */
  private void addProviderConfig(InitParams params, String name, String consumer, String secret) {
    PropertiesParam config = new PropertiesParam();
    config.setName(name);
    config.setProperty(ServiceProviderStore.CONSUMER_KEY, consumer);
    config.setProperty(ServiceProviderStore.SHARED_SECRET, secret);
    params.addParam(config);
  }

}
