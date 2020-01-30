package org.exoplatform.commons.search.indexing.listeners;

import org.exoplatform.commons.api.indexing.IndexingService;
import org.exoplatform.commons.api.indexing.data.SearchEntry;
import org.exoplatform.commons.api.indexing.data.SearchEntryId;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.UserProfile;
import org.exoplatform.services.organization.UserProfileEventListener;

import java.util.HashMap;
import java.util.Map;

/**
 * Indexing with :
 * - collection : "social"
 * - type : "profile"
 * - name : username
 */
public class UnifiedSearchOrganizationProfileListener extends UserProfileEventListener {

  private static Log log = ExoLogger.getLogger(UnifiedSearchOrganizationProfileListener.class);

  private final IndexingService indexingService;

  public UnifiedSearchOrganizationProfileListener(IndexingService indexingService) {
    this.indexingService = indexingService;
  }

  @Override
  public void preSave(UserProfile user, boolean isNew) throws Exception {
  }

  @Override
  public void postSave(UserProfile user, boolean isNew) throws Exception {
    if(indexingService != null) {
      Map<String, Object> content = new HashMap<String, Object>();
      content.put("profile", user);
      if(isNew) {
        SearchEntry searchEntry = new SearchEntry("social", "profile", user.getUserName(), content);
        indexingService.add(searchEntry);
      } else {
        SearchEntryId searchEntryId = new SearchEntryId("social", "profile", user.getUserName());
        indexingService.update(searchEntryId, content);
      }
    }
  }

  @Override
  public void preDelete(UserProfile user) throws Exception {
  }

  @Override
  public void postDelete(UserProfile user) throws Exception {
    // TODO this method is never called as org.exoplatform.services.organization.idm.UserDAOImpl.removeUser() calls 
    // orgService.getUserProfileHandler().removeUserProfile() with an hardcoded broadcast to false

    if(indexingService != null) {
      SearchEntryId searchEntryId = new SearchEntryId("social", "profile", user.getUserName());
      indexingService.delete(searchEntryId);
    }
  }
}
