package org.exoplatform.social.opensocial.oauth;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.PropertiesParam;

import junit.framework.TestCase;

public class OAuthServiceProviderStore extends TestCase {

  public void testGetConsumer() { 
  InitParams params = new InitParams();
  addProviderConfig(params,"p1", "foo", "secret1");
  addProviderConfig(params,"p2", "bar", "secret2");
  ServiceProviderStore store = new ServiceProviderStore(params);
  
  assertNull(store.getServiceProvider(null));
  assertNull(store.getServiceProvider("zed"));

  assertEquals("secret1",store.getServiceProvider("foo").getSharedSecret());
  assertEquals("secret2",store.getServiceProvider("bar").getSharedSecret());
  
}
/**
 * adds a properties-param according to what is expected by the {@link ServiceProviderStore}
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