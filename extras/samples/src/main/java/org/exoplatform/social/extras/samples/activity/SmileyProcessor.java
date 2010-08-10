package org.exoplatform.social.extras.samples.activity;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.social.core.BaseActivityProcessorPlugin;
import org.exoplatform.social.core.activity.model.Activity;

public class SmileyProcessor extends BaseActivityProcessorPlugin {

  public SmileyProcessor(InitParams params) {
    super(params);
  }

  String smiley =  "<img src=\"http://www.tombraider4u.com/pictures/smiley.gif\"/>";

  public void processActivity(Activity activity) {
    String title = activity.getTitle();
    activity.setTitle(title.replaceAll(":-\\)", smiley));
  }

}
