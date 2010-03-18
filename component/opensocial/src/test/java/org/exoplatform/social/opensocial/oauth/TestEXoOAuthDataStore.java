package org.exoplatform.social.opensocial.oauth;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

public class TestEXoOAuthDataStore extends TestCase {

  public void testGetConsumer() {
    EXoOAuthDataStore dataStore = new EXoOAuthDataStore(null, null, null);
    
    Map<String,ServiceProviderData> providers = new HashMap<String, ServiceProviderData>();
    providers.put("foo", new ServiceProviderData("p1", null, "foo", "secret1"));
    providers.put("bar", new ServiceProviderData("p2", null, "bar", "secret2"));
    dataStore.setProviderStore(new ServiceProviderStore(providers));
    
    assertNull(dataStore.getConsumer(null));
    assertNull(dataStore.getConsumer("zed"));

    assertEquals("secret1",dataStore.getConsumer("foo").consumerSecret);
    assertEquals("secret2",dataStore.getConsumer("bar").consumerSecret);


    
    
  }


}
