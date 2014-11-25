package org.exoplatform.social.core.search;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;

import org.exoplatform.commons.api.search.data.SearchContext;
import org.exoplatform.commons.api.search.data.SearchResult;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.portal.mop.SiteType;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.social.core.chromattic.entity.SpaceEntity;
import org.exoplatform.social.core.service.LinkProvider;
import org.exoplatform.social.core.space.SpaceFilter;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.core.storage.impl.StorageUtils;
import org.exoplatform.social.core.storage.query.JCRProperties;
import org.exoplatform.social.core.storage.query.WhereExpression;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 */
public class SpaceSearchConnector extends AbstractSocialSearchConnector {

  private SpaceService spaceService;
  private static final Log LOG = ExoLogger.getLogger(SpaceSearchConnector.class);

  public SpaceSearchConnector(InitParams initParams, SpaceService spaceService) {
    super(initParams);
    this.spaceService = spaceService;
  }

  @Override
  public Collection<SearchResult> search(SearchContext context, String query, Range range, Sorting sorting) {

    List<SearchResult> results = new ArrayList<SearchResult>();

    SpaceFilter filter = new SpaceFilter();
    filter.setSpaceNameSearchCondition(query);
    filter.setSorting(sorting);
    
    //if condition only have special characters or empty
    if(filter.getSpaceNameSearchCondition().isEmpty()){
      return results;
    }

    ExoContainerContext eXoContext = (ExoContainerContext)ExoContainerContext.getCurrentContainer()
        .getComponentInstanceOfType(ExoContainerContext.class);
    String portalName = eXoContext.getPortalContainerName();


    ListAccess<Space> la = spaceService.getUnifiedSearchSpacesWithListAccess(getCurrentUserName(), filter);
    
    //
    try {
      Space[] spaces = la.load(range.offset, range.limit);
      
      //
      RowIterator rowIt = rows(buildQuery(filter), range.offset, range.limit);
      Row row = null;

      //
      for (Space s : spaces) {

        //
        if (Space.HIDDEN.equals(s.getVisibility()) && !spaceService.isMember(s, getCurrentUserName())) continue;
        
        //
        StringBuilder sb = new StringBuilder(s.getDisplayName());
        sb.append(String.format(" - %s Member(s)", s.getMembers().length));
        if (Space.OPEN.equals(s.getRegistration())) {
          sb.append(" - Free to Join");
        } else if (Space.VALIDATION.equals(s.getRegistration())) {
          sb.append(" - Register");
        } else if (Space.CLOSE.equals(s.getRegistration())) {
          sb.append(" - Invitation Only");
        } else {
          LOG.debug(s.getRegistration() + " registration unknown");
        }
        
        //
        row = (rowIt != null && rowIt.hasNext()) ? rowIt.nextRow() : null;
        
        //
        SearchResult result = new SearchResult(
            getSpaceUrl(context, s, portalName),
            s.getDisplayName(),
            //getExcerpt(row),
            s.getDescription(),
            sb.toString(),
            s.getAvatarUrl() != null ? s.getAvatarUrl() : LinkProvider.SPACE_DEFAULT_AVATAR_URL,
            s.getCreatedTime(),
            getRelevancy(row));
        results.add(result);
      }
    } catch (Exception e) {
      LOG.error(e);
    }

    //
    return results;
  }

  protected String getSpaceUrl(SearchContext context, Space space, String portalName) {

    try {
      String permanentSpaceName = space.getPrettyName();
      
      String groupId = space.getGroupId();

      //
      String siteName = groupId.replaceAll("/", ":");

      //
      String siteType = SiteType.GROUP.getName();

      String spaceURI = context.handler(portalName)
          .lang("")
          .siteName(siteName)
          .siteType(siteType)
          .path(permanentSpaceName)
          .renderLink();
      return URLDecoder.decode(String.format("/%s%s", portalName, spaceURI), "UTF-8");
    } catch (Exception e) {
      LOG.error("Cannot compute space url for " + space.getDisplayName(), e);
      return "";
    }

  }

  private String getCurrentUserName() {
    return ConversationState.getCurrent().getIdentity().getUserId();
  }
  
  /**
   * Builds query statement
   * @param spaceFilter
   * @return
   */
  private String buildQuery(SpaceFilter spaceFilter) {
    WhereExpression whereExpression = new WhereExpression();
    String spaceNameSearchCondition = StorageUtils.escapeSpecialCharacter(spaceFilter.getSpaceNameSearchCondition());
    
    if (spaceNameSearchCondition != null && spaceNameSearchCondition.length() != 0) {

        spaceNameSearchCondition = this.processSearchCondition(spaceNameSearchCondition);

        if (spaceNameSearchCondition.contains(StorageUtils.PERCENT_STR)) {
          whereExpression.startGroup();
          whereExpression
              .like(SpaceEntity.name, spaceNameSearchCondition)
              .or()
              .like(SpaceEntity.description, spaceNameSearchCondition);
          whereExpression.endGroup();
        }
        else {
          whereExpression.startGroup();
          whereExpression
              .contains(SpaceEntity.name, spaceNameSearchCondition)
              .or()
              .contains(SpaceEntity.description, spaceNameSearchCondition);
          whereExpression.endGroup();
        }
    }
    //
    StringBuilder sb = new StringBuilder("SELECT ").append(JCRProperties.JCR_EXCERPT.getName()).append(" FROM ");
    sb.append(JCRProperties.SPACE_NODE_TYPE);
    if (whereExpression.toString().trim().length() > 0) {
      sb.append(" WHERE ");
      //sb.append("CONTAINS(*, '").append(spaceFilter.getSpaceNameSearchCondition()).append("')");
      sb.append(whereExpression.toString());
    }
    sb.append(applyOrder(spaceFilter));
    
    //
    return sb.toString();
  }
  
  private String processSearchCondition(String searchCondition) {
    StringBuffer searchConditionBuffer = new StringBuffer();
    if (!searchCondition.contains(StorageUtils.ASTERISK_STR) && !searchCondition.contains(StorageUtils.PERCENT_STR)) {
      if (searchCondition.charAt(0) != StorageUtils.ASTERISK_CHAR) {
        searchConditionBuffer.append(StorageUtils.ASTERISK_STR).append(searchCondition);
      }
      if (searchCondition.charAt(searchCondition.length() - 1) != StorageUtils.ASTERISK_CHAR) {
        searchConditionBuffer.append(StorageUtils.ASTERISK_STR);
      }
    } else {
      searchCondition = searchCondition.replace(StorageUtils.ASTERISK_STR, StorageUtils.PERCENT_STR);
      searchConditionBuffer.append(StorageUtils.PERCENT_STR).append(searchCondition).append(StorageUtils.PERCENT_STR);
    }
    return searchConditionBuffer.toString();
  }
  
  /**
   * 
   * @param spaceFilter
   */
  private String applyOrder(SpaceFilter spaceFilter) {

    StringBuilder sb = new StringBuilder(" ORDER BY ");
    
    //
    Sorting sorting;
    if (spaceFilter == null) {
      sorting = new Sorting(Sorting.SortBy.TITLE, Sorting.OrderBy.ASC);
    } else {
      sorting = spaceFilter.getSorting();
    }

    //
    switch (sorting.sortBy) {
      case DATE:
        sb.append(SpaceEntity.createdTime.getName()).append(" ").append(sorting.orderBy.toString());
        break;
      case RELEVANCY:
        sb.append(JCRProperties.JCR_RELEVANCY.getName()).append(" ").append(sorting.orderBy.toString());
        break;
      case TITLE:
        sb.append(SpaceEntity.name.getName()).append(" ").append(sorting.orderBy.toString());
        break;
    }
    
    return sb.toString();
  }

}
