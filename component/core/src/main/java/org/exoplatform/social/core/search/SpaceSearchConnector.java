package org.exoplatform.social.core.search;

import org.apache.commons.collections.map.HashedMap;
import org.exoplatform.commons.api.search.data.SearchResult;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.portal.mop.SiteType;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.social.core.service.LinkProvider;
import org.exoplatform.social.core.space.SpaceFilter;
import org.exoplatform.social.core.space.SpaceUtils;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.web.application.RequestContext;
import org.exoplatform.web.controller.QualifiedName;
import org.exoplatform.web.controller.router.Router;
import org.exoplatform.web.controller.router.URIWriter;
import org.exoplatform.web.url.navigation.NavigationResource;
import org.exoplatform.web.url.navigation.NodeURL;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

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
  public Collection<SearchResult> search(String query, Range range, Sorting sorting) {

    List<SearchResult> results = new ArrayList<SearchResult>();

    SpaceFilter filter = new SpaceFilter();
    filter.setSpaceNameSearchCondition(query);
    filter.setSorting(sorting);

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
            getSpaceUrl(s),
            s.getDisplayName(),
            s.getDescription(),
            sb.toString(),
            s.getAvatarUrl() != null ? s.getAvatarUrl() : LinkProvider.SPACE_DEFAULT_AVATAR_URL,
            s.getCreatedTime(),
            0); // TODO : implement relevancy
        results.add(result);
      }
    } catch (Exception e) {
      LOG.error(e);
    }

    //
    return results;
  }

  protected String getSpaceUrl(Space space) {

    try {
      String groupId = space.getGroupId();
      String permanentSpaceName = groupId.split("/")[2];

      RequestContext ctx = RequestContext.getCurrentInstance();
      NodeURL nodeURL =  ctx.createURL(NodeURL.TYPE);
      NavigationResource resource = null;
      if (permanentSpaceName.equals(space.getPrettyName())) {
        //work-around for SOC-2366 when delete space after that create new space with the same name
        resource = new NavigationResource(SiteType.GROUP, SpaceUtils.SPACE_GROUP + "/"
                                          + permanentSpaceName, permanentSpaceName);
      } else {
        resource = new NavigationResource(SiteType.GROUP, SpaceUtils.SPACE_GROUP + "/"
                                          + permanentSpaceName, space.getPrettyName());
      }

      return nodeURL.setResource(resource).toString();
    } catch (Exception e) {
      LOG.error("Cannot compute space url for " + space.getDisplayName(), e);
      return "";
    }

  }

  public String getCurrentUserName() {
    return ConversationState.getCurrent().getIdentity().getUserId();
  }

}
