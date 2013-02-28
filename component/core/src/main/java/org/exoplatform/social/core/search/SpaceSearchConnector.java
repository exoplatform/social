package org.exoplatform.social.core.search;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.exoplatform.commons.api.search.data.SearchContext;
import org.exoplatform.commons.api.search.data.SearchResult;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.portal.mop.SiteType;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.social.core.service.LinkProvider;
import org.exoplatform.social.core.space.SpaceFilter;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;

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

    ExoContainerContext eXoContext = (ExoContainerContext)ExoContainerContext.getCurrentContainer()
        .getComponentInstanceOfType(ExoContainerContext.class);
    String portalName = eXoContext.getPortalContainerName();


    ListAccess<Space> la = spaceService.getVisibleSpacesWithListAccess(getCurrentUserName(), filter);
    try {
      for (Space s : la.load(range.offset, range.limit)) {

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
        SearchResult result = new SearchResult(
            getSpaceUrl(context, s, portalName),
            s.getDisplayName(),
            s.getDescription(),
            sb.toString(),
            s.getAvatarUrl() != null ? s.getAvatarUrl() : LinkProvider.SPACE_DEFAULT_AVATAR_URL,
            s.getCreatedTime(),
            0);
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
      String groupId = space.getGroupId();
      String permanentSpaceName = groupId.split("/")[2];

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

}
