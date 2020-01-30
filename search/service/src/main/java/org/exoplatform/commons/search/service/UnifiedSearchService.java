/*
 * Copyright (C) 2003-2012 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.commons.search.service;

import org.picocontainer.Startable;

import org.exoplatform.commons.api.search.SearchService;
import org.exoplatform.commons.api.search.SearchServiceConnector;
import org.exoplatform.commons.api.search.data.SearchContext;
import org.exoplatform.commons.api.search.data.SearchResult;
import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.SettingValue;
import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.api.settings.data.Scope;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.rest.impl.RuntimeDelegateImpl;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.web.WebAppController;
import org.exoplatform.web.controller.metadata.ControllerDescriptor;
import org.exoplatform.web.controller.metadata.DescriptorBuilder;
import org.exoplatform.web.controller.router.Router;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.ws.rs.ext.RuntimeDelegate;
import java.io.File;
import java.net.URL;
import java.util.*;

/**
 * This class provides RESTful services endpoints which will help all external
 * components to call unified search functions. These services include Search,
 * Registry, Sites, Search settings, Quick search settings, and Enable search
 * type.
 */
@Path("/search")
@Produces(MediaType.APPLICATION_JSON)
public class UnifiedSearchService implements ResourceContainer, Startable {
  private static final Log          LOG = ExoLogger.getLogger(UnifiedSearchService.class);

  private static final CacheControl cacheControl;
  static {
    RuntimeDelegate.setInstance(new RuntimeDelegateImpl());
    cacheControl = new CacheControl();
    cacheControl.setNoCache(true);
    cacheControl.setNoStore(true);
  }

  private static SearchSetting    defaultSearchSetting      = new SearchSetting(10, Arrays.asList("all"), false, false, false);

  private static SearchSetting    defaultQuicksearchSetting = new SearchSetting(5, Arrays.asList("all"), true, true, true);

  private SearchService           searchService;

  private UserPortalConfigService userPortalConfigService;

  private SettingService          settingService;

  private Router                  router;

  /**
   * A constructor creates a instance of unified search service with the
   * specified parameters
   * 
   * @param searchService a service to work with other connectors
   * @param settingService a service to store and get the setting values
   * @param userPortalConfigService a service to get user information from
   *          portal
   * @param webAppController a controller to get configuration path
   * @LevelAPI Experimental
   */
  public UnifiedSearchService(SearchService searchService,
                              SettingService settingService,
                              UserPortalConfigService userPortalConfigService,
                              WebAppController webAppController) {
    this.searchService = searchService;
    this.settingService = settingService;
    this.userPortalConfigService = userPortalConfigService;

    try {
      File controllerXml = new File(webAppController.getConfigurationPath());
      URL url = controllerXml.toURI().toURL();
      ControllerDescriptor routerDesc = new DescriptorBuilder().build(url.openStream());
      this.router = new Router(routerDesc);
    } catch (Exception e) {
      LOG.error(e.getMessage(), e);
    }

  }

  @Override
  public void start() {
    // Added to be started when the server startup
  }

  @Override
  public void stop() {
    // Nothing to stop
  }

  /**
   * Searches for a query.
   * 
   * @param uriInfo Search context
   * @param query Searches for a query which is entered by the user.
   * @param sSites Searches in the specified sites only (for example, ACME or
   *          Intranet).
   * @param sTypes Searches for these specified content types only (for example,
   *          people, discussions, events, tasks, wikis, spaces, files, and
   *          documents).
   * @param sOffset Starts the offset of the results set.
   * @param sLimit Limit the maximum size of the results set.
   * @param sort Defines the Sort type (relevancy, date, title).
   * @param order Defines the Sort order (ascending, descending).
   * @return a map of connectors, including their search results.
   * @LevelAPI Experimental
   */
  @GET
  public Response REST_search(
                              @javax.ws.rs.core.Context UriInfo uriInfo,
                              @QueryParam("q") String query,
                              @QueryParam("sites") @DefaultValue("all") String sSites,
                              @QueryParam("types") String sTypes,
                              @QueryParam("offset") @DefaultValue("0") String sOffset,
                              @QueryParam("limit") String sLimit,
                              @QueryParam("sort") @DefaultValue("relevancy") String sort,
                              @QueryParam("order") @DefaultValue("desc") String order,
                              @QueryParam("lang") @DefaultValue("en") String lang) {
    try {
      MultivaluedMap<String, String> queryParams = uriInfo.getQueryParameters();
      String siteName = queryParams.getFirst("searchContext[siteName]");
      SearchContext context = new SearchContext(this.router, siteName);
      context.lang(lang);

      if (null == query || query.isEmpty())
        return Response.ok("", MediaType.APPLICATION_JSON).cacheControl(cacheControl).build();

      String userId = ConversationState.getCurrent().getIdentity().getUserId();
      boolean isAnonymous = null == userId || userId.isEmpty() || userId.equals("__anonim");
      SearchSetting searchSetting = isAnonymous ? getAnonymSearchSetting() : getSearchSetting();

      List<String> sites = Arrays.asList(sSites.split(",\\s*"));
      if (sites.contains("all"))
        sites = userPortalConfigService.getAllPortalNames();

      List<String> types = null == sTypes ? searchSetting.getSearchTypes() : Arrays.asList(sTypes.split(",\\s*"));
      if (isAnonymous && null != sTypes)
        types = this.getAnonymSearchTypes(types);

      int offset = Integer.parseInt(sOffset);
      int limit = null == sLimit || sLimit.isEmpty() ? (int) searchSetting.getResultsPerPage() : Integer.parseInt(sLimit);

      Map<String, Collection<SearchResult>> results = searchService.search(context,
                                                                           query,
                                                                           sites,
                                                                           types,
                                                                           offset,
                                                                           limit,
                                                                           sort,
                                                                           order);

      // get the base URI - http://<host>:<port>
      String baseUri = uriInfo.getBaseUri().toString(); // http://<host>:<port>/rest
      baseUri = baseUri.substring(0, baseUri.lastIndexOf((new URL(baseUri)).getPath()));
      String resultUrl, previewUrl, imageUrl;

      // use absolute path for URLs in search results
      for (Collection<SearchResult> connectorResults : results.values()) {
        for (SearchResult result : connectorResults) {
          resultUrl = result.getUrl();
          previewUrl = result.getPreviewUrl();
          imageUrl = result.getImageUrl();
          if (null != resultUrl && resultUrl.startsWith("/")) {
            result.setUrl(baseUri + resultUrl);
          }
          if (null != previewUrl && previewUrl.startsWith("/")) {
            result.setPreviewUrl(baseUri + previewUrl);
          }
          if (null != imageUrl && imageUrl.startsWith("/")) {
            result.setImageUrl(baseUri + imageUrl);
          }
        }
      }

      return Response.ok(results, MediaType.APPLICATION_JSON).cacheControl(cacheControl).build();
    } catch (Exception e) {
      LOG.error(e.getMessage(), e);
      return Response.serverError().status(Response.Status.INTERNAL_SERVER_ERROR).entity(e).cacheControl(cacheControl).build();
    }
  }

  /**
   * Gets all connectors which are registered in the system and are enabled.
   * 
   * @return List of connectors and names of the enabled ones.
   * @LevelAPI Experimental
   */
  @GET
  @Path("/registry")
  public Response REST_getRegistry() {
    LinkedHashMap<String, SearchServiceConnector> searchConnectors = new LinkedHashMap<String, SearchServiceConnector>();
    for (SearchServiceConnector connector : searchService.getConnectors()) {
      searchConnectors.put(connector.getSearchType(), connector);
    }
    return Response.ok(Arrays.asList(searchConnectors, getEnabledSearchTypes()), MediaType.APPLICATION_JSON)
                   .cacheControl(cacheControl)
                   .build();
  }

  /**
   * Gets all available sites in the system.
   * 
   * @return a list of site names
   * @LevelAPI Experimental
   */
  @GET
  @Path("/sites")
  public Response REST_getSites() {
    try {
      return Response.ok(userPortalConfigService.getAllPortalNames(), MediaType.APPLICATION_JSON)
                     .cacheControl(cacheControl)
                     .build();
    } catch (Exception e) {
      LOG.error(e.getMessage(), e);
      return Response.serverError().status(Response.Status.INTERNAL_SERVER_ERROR).entity(e).cacheControl(cacheControl).build();
    }
  }

  private SearchSetting getAnonymSearchSetting() {
    SearchSetting newSearchSetting = getSearchSetting();
    newSearchSetting.setSearchTypes(getAnonymSearchTypes(newSearchSetting.getSearchTypes()));
    return newSearchSetting;
  }

  private List<String> getAnonymSearchTypes(List<String> inputSearchTypes) {
    ArrayList<String> anonymSearchTypes;
    if (inputSearchTypes.contains("all")) {
      anonymSearchTypes = new ArrayList(this.getEnabledSearchTypes());
    } else {
      anonymSearchTypes = new ArrayList<>(inputSearchTypes);
    }

    anonymSearchTypes.remove("people");
    anonymSearchTypes.remove("space");

    return anonymSearchTypes;
  }

  @SuppressWarnings("unchecked")
  private SearchSetting getSearchSetting() {
    SearchSetting newSearchSetting = defaultSearchSetting;
    try {
      Long resultsPerPage = Long.parseLong(settingService.get(Context.GLOBAL, Scope.WINDOWS, "searchResult_resultsPerPage")
                                                         .getValue()
                                                         .toString());
      newSearchSetting.setResultsPerPage(resultsPerPage);
    } catch (Exception e) {
      LOG.info("Cannot get searchResult_resultsPerPage parameter for search settings. Use default one instead");
    }
    try {
      String searchTypes = ((SettingValue<String>) settingService.get(Context.GLOBAL,
                                                                      Scope.WINDOWS,
                                                                      "searchResult_searchTypes")).getValue();
      newSearchSetting.setSearchTypes(Arrays.asList(searchTypes.split(",\\s*")));
    } catch (Exception e) {
      LOG.info("Cannot get searchResult_searchTypes parameter for search settings. Use default one instead");
    }
    try {
      Boolean searchCurrentSiteOnly = Boolean.parseBoolean(settingService
                                                                         .get(Context.GLOBAL,
                                                                              Scope.WINDOWS,
                                                                              "searchResult_searchCurrentSiteOnly")
                                                                         .getValue()
                                                                         .toString());
      newSearchSetting.setSearchCurrentSiteOnly(searchCurrentSiteOnly);
    } catch (Exception e) {
      LOG.info("Cannot get searchResult_searchCurrentSiteOnly parameter for search settings. Use default one instead");
    }
    try {
      Boolean hideSearchForm =
                             Boolean.parseBoolean(settingService.get(Context.GLOBAL, Scope.WINDOWS, "searchResult_hideSearchForm")
                                                                .getValue()
                                                                .toString());
      newSearchSetting.setHideSearchForm(hideSearchForm);
    } catch (Exception e) {
      LOG.info("Cannot get searchResult_hideSearchForm parameter for search settings. Use default one instead");
    }
    try {
      Boolean hideFacetsFilter = Boolean.parseBoolean(settingService.get(Context.GLOBAL,
                                                                         Scope.WINDOWS,
                                                                         "searchResult_hideFacetsFilter")
                                                                    .getValue()
                                                                    .toString());
      newSearchSetting.setHideFacetsFilter(hideFacetsFilter);
    } catch (Exception e) {
      LOG.info("Cannot get searchResult_hideFacetsFilter parameter for search settings. Use default one instead");
    }
    return newSearchSetting;
  }

  /**
   * Gets current user's search settings.
   * 
   * @return search settings of the current logging in (or anonymous) user
   * @LevelAPI Experimental
   */
  @GET
  @Path("/setting")
  public Response REST_getSearchSetting() {
    String userId = ConversationState.getCurrent().getIdentity().getUserId();
    return Response.ok(userId.equals("__anonim") ? getAnonymSearchSetting() : getSearchSetting(), MediaType.APPLICATION_JSON)
                   .cacheControl(cacheControl)
                   .build();
  }

  /**
   * Saves current user's search settings.
   * 
   * @return "ok" when succeed
   * @LevelAPI Experimental
   */
  @POST
  @Path("/setting")
  @RolesAllowed("administrators")
  public Response REST_setSearchSetting(@FormParam("resultsPerPage") long resultsPerPage,
                                        @FormParam("searchTypes") String searchTypes,
                                        @FormParam("searchCurrentSiteOnly") boolean searchCurrentSiteOnly,
                                        @FormParam("hideSearchForm") boolean hideSearchForm,
                                        @FormParam("hideFacetsFilter") boolean hideFacetsFilter) {
    settingService.set(Context.GLOBAL, Scope.WINDOWS, "searchResult_resultsPerPage", new SettingValue<Long>(resultsPerPage));
    settingService.set(Context.GLOBAL, Scope.WINDOWS, "searchResult_searchTypes", new SettingValue<String>(searchTypes));
    settingService.set(Context.GLOBAL,
                       Scope.WINDOWS,
                       "searchResult_searchCurrentSiteOnly",
                       new SettingValue<Boolean>(searchCurrentSiteOnly));
    settingService.set(Context.GLOBAL, Scope.WINDOWS, "searchResult_hideSearchForm", new SettingValue<Boolean>(hideSearchForm));
    settingService.set(Context.GLOBAL,
                       Scope.WINDOWS,
                       "searchResult_hideFacetsFilter",
                       new SettingValue<Boolean>(hideFacetsFilter));

    return Response.ok("ok", MediaType.APPLICATION_JSON).cacheControl(cacheControl).build();
  }

  @SuppressWarnings("unchecked")
  private SearchSetting getQuickSearchSetting() {
    SearchSetting newSearchSetting = defaultQuicksearchSetting;
    try {
      Long resultsPerPage = Long.parseLong(settingService.get(Context.GLOBAL, Scope.WINDOWS, "resultsPerPage")
                                                         .getValue()
                                                         .toString());
      newSearchSetting.setResultsPerPage(resultsPerPage);
    } catch (Exception e) {
      LOG.info("Cannot get resultsPerPage parameter for quick search settings. Use default one instead");
    }
    try {
      String searchTypes = ((SettingValue<String>) settingService.get(Context.GLOBAL, Scope.WINDOWS, "searchTypes")).getValue();
      newSearchSetting.setSearchTypes(Arrays.asList(searchTypes.split(",\\s*")));
    } catch (Exception e) {
      LOG.info("Cannot get searchTypes parameter for quick search settings. Use default one instead");
    }
    try {
      Boolean searchCurrentSiteOnly = Boolean.parseBoolean(settingService.get(Context.GLOBAL,
                                                                              Scope.WINDOWS,
                                                                              "searchCurrentSiteOnly")
                                                                         .getValue()
                                                                         .toString());
      newSearchSetting.setSearchCurrentSiteOnly(searchCurrentSiteOnly);
    } catch (Exception e) {
      LOG.info("Cannot get searchCurrentSiteOnly parameter for quick search settings. Use default one instead");
    }
    return newSearchSetting;
  }

  /**
   * Gets current user's quick search settings.
   * 
   * @return quick search settings of the current logging in user
   * @LevelAPI Experimental
   */
  @GET
  @Path("/setting/quicksearch")
  public Response REST_getQuicksearchSetting() {
    return Response.ok(getQuickSearchSetting(), MediaType.APPLICATION_JSON).cacheControl(cacheControl).build();
  }

  /**
   * Saves current user's quick search settings.
   *
   * @return "ok" when succeed
   * @LevelAPI Experimental
   */
  @POST
  @Path("/setting/quicksearch")
  public Response REST_setQuicksearchSetting(@FormParam("resultsPerPage") long resultsPerPage,
                                             @FormParam("searchTypes") String searchTypes,
                                             @FormParam("searchCurrentSiteOnly") boolean searchCurrentSiteOnly) {
    settingService.set(Context.GLOBAL, Scope.WINDOWS, "resultsPerPage", new SettingValue<Long>(resultsPerPage));
    settingService.set(Context.GLOBAL, Scope.WINDOWS, "searchTypes", new SettingValue<String>(searchTypes));
    settingService.set(Context.GLOBAL, Scope.WINDOWS, "searchCurrentSiteOnly", new SettingValue<Boolean>(searchCurrentSiteOnly));
    return Response.ok("ok", MediaType.APPLICATION_JSON).cacheControl(cacheControl).build();
  }

  @SuppressWarnings("unchecked")
  public static List<String> getEnabledSearchTypes() {
    SettingService settingService = (SettingService) ExoContainerContext.getCurrentContainer()
                                                                        .getComponentInstanceOfType(SettingService.class);
    SettingValue<String> enabledSearchTypes = (SettingValue<String>) settingService.get(Context.GLOBAL,
                                                                                        Scope.APPLICATION,
                                                                                        "enabledSearchTypes");
    if (null != enabledSearchTypes)
      return Arrays.asList(enabledSearchTypes.getValue().split(",\\s*"));

    SearchService searchService = (SearchService) ExoContainerContext.getCurrentContainer()
                                                                     .getComponentInstanceOfType(SearchService.class);
    LinkedList<String> allSearchTypes = new LinkedList<String>();
    for (SearchServiceConnector connector : searchService.getConnectors()) {
      if (connector.isEnable()) {
        allSearchTypes.add(connector.getSearchType());
      }
    }
    return allSearchTypes;
  }

  /**
   * Sets the "enabledSearchTypes" variable in a global context.
   * 
   * @param searchTypes List of search types in the form of a comma-separated
   *          string.
   * @return "ok" if the caller's role is administrator, otherwise, returns
   *         "nok: administrators only".
   * @LevelAPI Experimental
   */
  @POST
  @Path("/enabled-searchtypes/{searchTypes}")
  public Response REST_setEnabledSearchtypes(@PathParam("searchTypes") String searchTypes) {
    UserACL userAcl = (UserACL) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(UserACL.class);

    if (ConversationState.getCurrent().getIdentity().isMemberOf(userAcl.getAdminGroups())) {// only
                                                                                            // administrators
                                                                                            // can
                                                                                            // set
                                                                                            // this
      settingService.set(Context.GLOBAL, Scope.APPLICATION, "enabledSearchTypes", new SettingValue<String>(searchTypes));
      return Response.ok("ok", MediaType.APPLICATION_JSON).cacheControl(cacheControl).build();
    }
    return Response.ok("nok: administrators only", MediaType.APPLICATION_JSON).cacheControl(cacheControl).build();
  }

}
