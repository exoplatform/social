package org.exoplatform.social.core.storage.streams;

import java.io.Serializable;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;

public class StreamConfig implements Serializable {

	private static final long serialVersionUID = 1L;

	/** defines the connections threshold to create the Activity References. */
	private static final int CONNECTIONS_THRESHOLD = 1000;

	/** only focus the users who have the last login around 30 days. */
	private static final int LAST_LOGIN_AROUND_DAYS = 30;

	private final String CONNECTIONS_THRESHOLD_PARAMS = "connections-threshold-param";
	private final String LAST_LOGIN_AROUND_DAYS_PARAMS = "last-login-around-days";
	private final String LIMIT_PER_LOADING_PARAMS = "limit-per-loading-param";
	private final String ACTIVE_USER_GROUPS_PARAMS = "active-user-groups-param";

	private int connectionsThreshold = -1;
	private int lastLoginAroundDays = -1;
	private int limitThresholdLoading = 10;
	private String activeUserGroups = null;

	public StreamConfig(InitParams params) {

		//
		ValueParam connectionsThresholdParam = params.getValueParam(CONNECTIONS_THRESHOLD_PARAMS);
		ValueParam lastLoginAroundDaysParam = params.getValueParam(LAST_LOGIN_AROUND_DAYS_PARAMS);
		ValueParam limitPerLoadingParam = params.getValueParam(LIMIT_PER_LOADING_PARAMS);
		ValueParam activeUserGroupsParam = params.getValueParam(ACTIVE_USER_GROUPS_PARAMS);

		//
		if (connectionsThresholdParam != null) {
		  try {
	      this.connectionsThreshold = Integer.valueOf(connectionsThresholdParam.getValue());
	    } catch (Exception e) {
	      this.connectionsThreshold = CONNECTIONS_THRESHOLD;
	    }
		}
		

		if (lastLoginAroundDaysParam != null) {
		  //
	    try {
	      this.lastLoginAroundDays = Integer.valueOf(lastLoginAroundDaysParam.getValue());
	    } catch (Exception e) {
	      this.lastLoginAroundDays = LAST_LOGIN_AROUND_DAYS;
	    }
		}
		
		if (limitPerLoadingParam != null) {
		  //
	    try {
	      this.limitThresholdLoading = Integer.valueOf(limitPerLoadingParam.getValue());
	    } catch (Exception e) {
	      this.limitThresholdLoading = 10;
	    }
		}
		
    if (activeUserGroupsParam != null) {
      //
      try {
        this.activeUserGroups = activeUserGroupsParam.getValue();
      } catch (Exception e) {
        this.activeUserGroups = null;
      }
    }
    
	}

	/**
	 * Retrieves the connections threshold for creating Activity Ref
	 * @return connections threshold
	 */
  public int getConnectionsThreshold() {
    return connectionsThreshold;
  }

  /**
   * Retrieves the last login around days of an user
   * @return
   */
  public int getLastLoginAroundDays() {
    return lastLoginAroundDays;
  }

  /**
   * The batch limit to load the connection from storage.
   * - batch limit to persist to storage when CRUD node.
   * @return the limit
   */
  public int getLimitThresholdLoading() {
    return limitThresholdLoading;
  }
  
  /**
   * Gets the user groups configuration
   * if user belong to this group, he is always active default.
   * for example: /platform/users,/platform/administrators...
   * 
   * @return return multiple user groups, separate by comma
   */
  public String getActiveUserGroups() {
    return activeUserGroups;
  }

}
