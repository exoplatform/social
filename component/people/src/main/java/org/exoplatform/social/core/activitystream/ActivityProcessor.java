package org.exoplatform.social.core.activitystream;

import org.exoplatform.social.core.activitystream.model.Activity;

public interface ActivityProcessor {

  public void processActivity(Activity activity);
  
  public int getPriority();
  
}
