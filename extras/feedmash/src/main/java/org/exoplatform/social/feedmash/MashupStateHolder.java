package org.exoplatform.social.feedmash;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MashupStateHolder {

  public Map<String, Object> mashupState;
  
  public MashupStateHolder() { 
    mashupState = new ConcurrentHashMap<String, Object>();
  }
  
  public Object getState(String key) {
    return mashupState.get(key);
  }
  
  public void saveState(String key, Object state) {
    mashupState.put(key, state);
  }
  
}
