package org.exoplatform.social.core.search;

import org.exoplatform.commons.api.search.data.SearchResult;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.profile.ProfileFilter;
import org.exoplatform.social.core.service.LinkProvider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 */
public class PeopleSearchConnector extends AbstractSocialSearchConnector {

  private IdentityManager identityManager;
  private static final Log LOG = ExoLogger.getLogger(PeopleSearchConnector.class);

  public PeopleSearchConnector(InitParams initParams, IdentityManager identityManager) {
    super(initParams);
    this.identityManager = identityManager;
  }



  @Override
  public Collection<SearchResult> search(String query, Range range, Sorting sorting) {

    List<SearchResult> results = new ArrayList<SearchResult>();

    ProfileFilter filter = new ProfileFilter();
    filter.setAll(query);
    ListAccess<Identity> la = identityManager.getIdentitiesByProfileFilter(OrganizationIdentityProvider.NAME, filter, true);
    try {
      for (Identity i : la.load(range.offset, range.limit)) {
        Profile p = i.getProfile();
        StringBuilder sb = new StringBuilder();

        //
        if (p.getEmail() != null) {
          sb.append(p.getEmail());
        }

        //
        List<Map> phones = (List<Map>) p.getProperty(Profile.CONTACT_PHONES);
        if (phones != null && phones.size() > 0) {
          sb.append(" - " + phones.get(0).get("value"));
        }

        //
        if (p.getProperty(Profile.GENDER) != null) {
          sb.append(" - " + p.getProperty(Profile.GENDER));
        }

        SearchResult result = new SearchResult(
            p.getUrl(),
            p.getFullName(),
            p.getPosition(),
            sb.toString(),
            p.getAvatarUrl() != null ? p.getAvatarUrl() : LinkProvider.PROFILE_DEFAULT_AVATAR_URL,
            0,
            0); // implement sort / order
        results.add(result);
      }
    } catch (Exception e) {
      LOG.error(e);
    }

    //
    return results;
  }

}