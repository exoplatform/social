package org.exoplatform.social.core.storage;

import java.util.*;

import org.apache.commons.lang3.StringUtils;

import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.social.core.activity.model.ActivityFile;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.identity.model.Identity;

/**
 * This is an abstract class that is the base class of component plugins that
 * will handle Activity attachments storage
 */
public abstract class ActivityFileStoragePlugin extends BaseComponentPlugin {

  public static final String ACTIVITY_FILE_STORAGE_PARAM_NAME = "storage";

  public static final String ACTIVITY_FILE_ID_PARAM_NAME      = "id";

  public static final String TEMPLATE_PARAMS_SEPARATOR        = "|@|";

  protected String           datasourceName;

  protected int              priority;

  public ActivityFileStoragePlugin(InitParams initParams) {
    if (initParams == null) {
      throw new IllegalStateException("Init param 'datasource' is mandatory");
    }

    if (initParams.containsKey("datasource")) {
      this.datasourceName = initParams.getValueParam("datasource").getValue();
    } else {
      throw new IllegalStateException("Init param 'datasource' is mandatory");
    }

    if (initParams.containsKey("priority")) {
      String priorityString = initParams.getValueParam("priority").getValue();
      this.priority = Integer.parseInt(priorityString);
    }
  }

  /**
   * Store attachments to a given activity
   * 
   * @param activity
   * @param streamOwner
   * @param attachments
   * @throws Exception
   */
  public abstract void storeAttachments(ExoSocialActivity activity,
                                        Identity streamOwner,
                                        ActivityFile... attachments) throws Exception;

  public String getDatasourceName() {
    return datasourceName;
  }

  public void setDatasourceName(String datasourceName) {
    this.datasourceName = datasourceName;
  }

  public void setPriority(int priority) {
    this.priority = priority;
  }

  public int getPriority() {
    return priority;
  }

  protected void concatenateParam(Map<String, String> activityParams, String paramName, String paramValue) {
    String oldParamValue = activityParams.get(paramName);
    if (StringUtils.isBlank(oldParamValue)) {
      activityParams.put(paramName, paramValue);
    } else {
      activityParams.put(paramName, oldParamValue + TEMPLATE_PARAMS_SEPARATOR + paramValue);
    }
  }

  protected List<String> readParamValues(Map<String, String> activityParams, String paramName) {
    String paramValue = activityParams.get(paramName);
    if (StringUtils.isBlank(paramValue)) {
      return Collections.emptyList();
    }
    return Arrays.asList(paramValue.split(TEMPLATE_PARAMS_SEPARATOR));
  }

}
