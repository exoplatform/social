package org.exoplatform.social.core.search;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;

import org.apache.lucene.queryParser.QueryParser;
import org.exoplatform.commons.api.search.data.SearchContext;
import org.exoplatform.commons.api.search.data.SearchResult;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.chromattic.entity.ProfileEntity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.profile.ProfileFilter;
import org.exoplatform.social.core.service.LinkProvider;
import org.exoplatform.social.core.storage.impl.StorageUtils;
import org.exoplatform.social.core.storage.query.JCRProperties;
import org.exoplatform.social.core.storage.query.WhereExpression;

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
  public Collection<SearchResult> search(SearchContext context, String query, Range range, Sorting sorting) {

    List<SearchResult> results = new ArrayList<SearchResult>();

    ProfileFilter filter = new ProfileFilter();
    filter.setAll(query);
    filter.setSorting(sorting);
    ListAccess<Identity> la = identityManager.getIdentitiesForUnifiedSearch(OrganizationIdentityProvider.NAME, filter);
    
    try {
      
      Identity[] identities = la.load(range.offset, range.limit);
      //
      RowIterator rowIt = rows(buildQuery(filter), range.offset, range.limit);
      Row row = null;
      
      for (Identity i : identities) {
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
        
        //
        row = (rowIt != null && rowIt.hasNext()) ? rowIt.nextRow() : null;
        
        //
        SearchResult result = new SearchResult(
            p.getUrl(),
            p.getFullName(),
            p.getPosition(),
            sb.toString(),
            p.getAvatarUrl() != null ? p.getAvatarUrl() : LinkProvider.PROFILE_DEFAULT_AVATAR_URL,
            p.getCreatedTime(),
            getRelevancy(row));
        results.add(result);
      }
    } catch (IllegalArgumentException aex) {
      LOG.warn(aex.getMessage());
    } catch (Exception e) {
      LOG.error(e);
    }

    //
    return results;
  }
  
  /**
   * Builds query statement
   * @param filter
   * @return
   */
  private String buildQuery(ProfileFilter filter) {
    WhereExpression whereExpression = new WhereExpression();
    whereExpression
    .like(JCRProperties.path, getProviderRoot().getProviders().get(OrganizationIdentityProvider.NAME).getPath() + StorageUtils.SLASH_STR + StorageUtils.PERCENT_STR)
    .and()
    .not().equals(ProfileEntity.deleted, "true");
    
    if (filter.getAll().length() != 0) {
      String value = StorageUtils.escapeSpecialCharacter(filter.getAll());

      whereExpression.and().startGroup()
          .contains(ProfileEntity.fullName, value.toLowerCase())
          .or().contains(ProfileEntity.firstName, value.toLowerCase())
          .or().contains(ProfileEntity.lastName, value.toLowerCase())
          .or().contains(ProfileEntity.position, value.toLowerCase())
          .or().contains(ProfileEntity.skills, value.toLowerCase())
          .or().contains(ProfileEntity.positions, value.toLowerCase())
          .or().contains(ProfileEntity.organizations, value.toLowerCase())
          .or().contains(ProfileEntity.jobsDescription, value.toLowerCase())
          .endGroup();
    }
    
    //
    StringBuilder sb = new StringBuilder("SELECT ").append(JCRProperties.JCR_EXCERPT.getName()).append(" FROM ");
    sb.append(JCRProperties.PROFILE_NODE_TYPE);
    sb.append(" WHERE ");
    sb.append(whereExpression.toString());
    sb.append(applyOrder(filter));
    
    //
    return sb.toString();
  }
  
  
  
  
  /**
   * build order by statement
   * 
   * @param filter
   */
  private String applyOrder(ProfileFilter filter) {

    StringBuilder sb = new StringBuilder(" ORDER BY ");
    
    //
    Sorting sorting;
    if (filter == null) {
      sorting = new Sorting(Sorting.SortBy.TITLE, Sorting.OrderBy.ASC);
    } else {
      sorting = filter.getSorting();
    }

    //
    switch (sorting.sortBy) {
      case DATE:
        sb.append(ProfileEntity.createdTime.getName()).append(" ").append(sorting.orderBy.toString());
        break;
      case RELEVANCY:
        sb.append(JCRProperties.JCR_RELEVANCY.getName()).append(" ").append(sorting.orderBy.toString());
        break;
      case TITLE:
        sb.append(ProfileEntity.fullName.getName()).append(" ").append(sorting.orderBy.toString());
        break;
    }
    
    return sb.toString();
  }

}