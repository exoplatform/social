package org.exoplatform.social.notification.mock;

import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.model.WebNotificationFilter;
import org.exoplatform.commons.api.notification.service.storage.WebNotificationStorage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by exo on 22/06/18.
 */
public class MockWebNotificationStorage implements WebNotificationStorage {

  private Map<String, NotificationInfo> map = new HashMap<String, NotificationInfo>();

  public Map<String, NotificationInfo> getMap() {
    return this.map;
  }

  @Override
  public void save(NotificationInfo notification) {
    this.map.put(notification.getId(), notification);
  }

  @Override
  public void update(NotificationInfo notification, boolean moveTop) {
    // TODO Auto-generated method stub

  }

  @Override
  public void markRead(String notificationId) {
    // TODO Auto-generated method stub

  }

  @Override
  public void markAllRead(String userId) {
    // TODO Auto-generated method stub

  }

  @Override
  public void hidePopover(String notificationId) {
    // TODO Auto-generated method stub

  }

  @Override
  public List<NotificationInfo> get(WebNotificationFilter filter, int offset, int limit) {
    return new ArrayList<NotificationInfo>(this.map.values());
  }

  @Override
  public NotificationInfo get(String notificationId) {
    return this.map.get(notificationId);
  }

  @Override
  public boolean remove(String notificationId) {
    if (notificationId == null) {
      this.map.clear();
    } else {
      this.map.remove(notificationId);
    }
    return false;
  }

  @Override
  public boolean remove(String userId, long seconds) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public NotificationInfo getUnreadNotification(String pluginId, String activityId, String owner) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public int getNumberOnBadge(String userId) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public void resetNumberOnBadge(String userId) {
    // TODO Auto-generated method stub

  }

  @Override
  public boolean remove(long seconds) {
    // TODO Auto-generated method stub
    return false;
  }
}
