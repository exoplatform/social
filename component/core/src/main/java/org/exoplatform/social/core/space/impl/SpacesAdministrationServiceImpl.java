package org.exoplatform.social.core.space.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.ServletContext;

import org.apache.commons.lang3.StringUtils;
import org.picocontainer.Startable;

import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.SettingValue;
import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.api.settings.data.Scope;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.RootContainer;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.management.annotations.ManagedBy;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.portal.mop.page.PageContext;
import org.exoplatform.portal.mop.page.PageKey;
import org.exoplatform.portal.mop.page.PageService;
import org.exoplatform.portal.mop.page.PageState;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.Membership;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.security.IdentityConstants;
import org.exoplatform.services.security.IdentityRegistry;
import org.exoplatform.services.security.MembershipEntry;
import org.exoplatform.social.core.space.SpacesAdministrationService;

/**
 * Service to manage administration of spaces
 */
@ManagedBy(SpacesAdministrationServiceManagerBean.class)
public class SpacesAdministrationServiceImpl implements Startable, SpacesAdministrationService {

  private static final Log LOG = ExoLogger.getLogger(SpacesAdministrationServiceImpl.class);

  private static final String SPACES_ADMINISTRATORS_PARAM = "social.spaces.administrators";

  private static final String SPACES_CREATORS_PARAM = "social.spaces.creators";

  private static final String SPACES_ADMINISTRATORS_SETTING_KEY = "social.spaces.administrators";

  private static final String SPACES_CREATORS_SETTING_KEY = "social.spaces.creators";

  public static final String SPACES_ADMINISTRATION_PAGE_KEY = "group::/platform/users::spacesAdministration";

  private SettingService settingService;
  
  private IdentityRegistry identityRegistry;

  private OrganizationService organizationService;

  private UserACL userACL;

  private InitParams initParams;

  private List<MembershipEntry> spacesAdministratorsMemberships = new ArrayList<>();

  private List<MembershipEntry> spaceCreatorsMemberships = new ArrayList<>();

  public SpacesAdministrationServiceImpl(InitParams initParams, SettingService settingService,
                                         IdentityRegistry identityRegistry, OrganizationService organizationService,
                                         UserACL userACL) {
    this.settingService = settingService;
    this.identityRegistry = identityRegistry;
    this.organizationService = organizationService;
    this.userACL = userACL;
    this.initParams = initParams;
  }

  @Override
  public void start() {
    loadSettings(initParams);

    // update Spaces administration at startup in case the configuration has changed
    PortalContainer.addInitTask(PortalContainer.getInstance().getPortalContext(), new RootContainer.PortalContainerPostInitTask() {
      @Override
      public void execute(ServletContext context, PortalContainer portalContainer) {
        List<MembershipEntry> superManagersMemberships = SpacesAdministrationServiceImpl.this.getSpacesAdministratorsMemberships();
        updateSpacesAdministrationPagePermissions(superManagersMemberships);
      }
    });
  }

  @Override
  public void stop() {
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void updateSpacesAdministratorsMemberships(List<MembershipEntry> permissionsExpressions) {
    if(permissionsExpressions == null) {
      throw new IllegalArgumentException("Permission expressions list couldn't be null");
    }

    this.spacesAdministratorsMemberships = permissionsExpressions;

    settingService.set(Context.GLOBAL, Scope.GLOBAL, SPACES_ADMINISTRATORS_SETTING_KEY, SettingValue.create(StringUtils.join(this.spacesAdministratorsMemberships, ",")));

    updateSpacesAdministrationPagePermissions(this.spacesAdministratorsMemberships);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<MembershipEntry> getSpacesAdministratorsMemberships() {
    return Collections.unmodifiableList(spacesAdministratorsMemberships);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<MembershipEntry> getSpacesCreatorsMemberships() {
    return Collections.unmodifiableList(spaceCreatorsMemberships);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void updateSpacesCreatorsMemberships(List<MembershipEntry> permissionsExpressions) {
    if(permissionsExpressions == null) {
      throw new IllegalArgumentException("Permission expressions list couldn't be null");
    }

    this.spaceCreatorsMemberships = permissionsExpressions;

    settingService.set(Context.GLOBAL, Scope.GLOBAL, SPACES_CREATORS_SETTING_KEY, SettingValue.create(StringUtils.join(this.spaceCreatorsMemberships, ",")));

    updateSpacesAdministrationPagePermissions(this.spaceCreatorsMemberships);
  }

  /**
   * Load Spaces Administration settings
   * For both Spaces Administrators and Spaces Creators settings, it uses the value stored in the settings if any,
   * otherwise it uses the value from the configuration
   * @param initParams Service init parameters
   */
  protected void loadSettings(InitParams initParams) {
    SettingValue<String> administrators = (SettingValue<String>) settingService.get(Context.GLOBAL, Scope.GLOBAL, SPACES_ADMINISTRATORS_SETTING_KEY);
    if (administrators != null && !StringUtils.isBlank(administrators.getValue())) {
      String[] administratorsArray = administrators.getValue().split(",");
      addSpacesAdministratorsMemberships(administratorsArray);
    } else if (initParams != null) {
      ValueParam spacesAdministratorsParam = initParams.getValueParam(SPACES_ADMINISTRATORS_PARAM);
      if (spacesAdministratorsParam != null) {
        String spacesAdministratorsMemberships = spacesAdministratorsParam.getValue();
        if (StringUtils.isNotBlank(spacesAdministratorsMemberships)) {
          String[] spacesAdministratorsMembershipsArray = spacesAdministratorsMemberships.split(",");
          addSpacesAdministratorsMemberships(spacesAdministratorsMembershipsArray);
        }
      }
    }

    SettingValue<String> creators = (SettingValue<String>) settingService.get(Context.GLOBAL, Scope.GLOBAL, SPACES_CREATORS_SETTING_KEY);
    if (creators != null && !StringUtils.isBlank(creators.getValue())) {
      String[] creatorsArray = creators.getValue().split(",");
      addSpacesCreatorsMemberships(creatorsArray);
    } else if (initParams != null) {
      ValueParam spacesCreatorsParam = initParams.getValueParam(SPACES_CREATORS_PARAM);
      if (spacesCreatorsParam != null) {
        String spacesCreatorsMemberships = spacesCreatorsParam.getValue();
        if (StringUtils.isNotBlank(spacesCreatorsMemberships)) {
          String[] spacesCreatorsMembershipsArray = spacesCreatorsMemberships.split(",");
          addSpacesCreatorsMemberships(spacesCreatorsMembershipsArray);
        }
      }
    }
  }

  private void addSpacesCreatorsMemberships(String[] creatorsArray) {
    for(String creatorArray : creatorsArray) {
      if (StringUtils.isBlank(creatorArray)) {
        continue;
      }
      if (!creatorArray.contains(":/")) {
        this.spaceCreatorsMemberships.add(new MembershipEntry(creatorArray));
      } else {
        String[] membershipParts = creatorArray.split(":");
        this.spaceCreatorsMemberships.add(new MembershipEntry(membershipParts[1], membershipParts[0]));
      }
    }
  }

  private void addSpacesAdministratorsMemberships(String[] administratorsArray) {
    for(String administrator : administratorsArray) {
      if (StringUtils.isBlank(administrator)) {
        continue;
      }
      if (!administrator.contains(":/")) {
        this.spacesAdministratorsMemberships.add(new MembershipEntry(administrator));
      } else {
        String[] membershipParts = administrator.split(":");
        this.spacesAdministratorsMemberships.add(new MembershipEntry(membershipParts[1], membershipParts[0]));
      }
    }
  }

  /**
   * Update permissions of the Spaces Administration page
   * @param superManagersMemberships New memberships to apply as read permissions on the page
   */
  private void updateSpacesAdministrationPagePermissions(List<MembershipEntry> superManagersMemberships) {
    if(superManagersMemberships != null) {
      RequestLifeCycle.begin(PortalContainer.getInstance());
      try {
        PageService pageService = CommonsUtils.getService(PageService.class);
        PageKey pageKey = PageKey.parse(SPACES_ADMINISTRATION_PAGE_KEY);
        PageContext pageContext = pageService.loadPage(pageKey);
        if(pageContext != null) {
          PageState page = pageContext.getState();
          PageState pageState = new PageState(page.getDisplayName(),
                  page.getDescription(),
                  page.getShowMaxWindow(),
                  page.getFactoryId(),
                  superManagersMemberships.stream()
                          .map(membership -> membership.getMembershipType() + ":"
                                  + membership.getGroup())
                          .collect(Collectors.toList()),
                  page.getEditPermission(),
                  page.getMoveAppsPermissions(),
                  page.getMoveContainersPermissions());
          pageService.savePage(new PageContext(pageKey, pageState));
        }
      } finally {
        RequestLifeCycle.end();
      }
    }
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public boolean canCreateSpace(String userId) {
    if (StringUtils.isBlank(userId) || IdentityConstants.ANONIM.equals(userId) || IdentityConstants.SYSTEM.equals(userId)) {
      return false;
    }
    if (userId.equals(userACL.getSuperUser())) {
      return true;
    }
    org.exoplatform.services.security.Identity identity = identityRegistry.getIdentity(userId);
    if (identity == null) {
      Collection<Membership> memberships;
      try {
        memberships = organizationService.getMembershipHandler().findMembershipsByUser(userId);
      } catch (Exception e) {
        throw new RuntimeException("Can't get user '" + userId + "' memberships", e);
      }
      List<MembershipEntry> entries = new ArrayList<>();
      for (Membership membership : memberships) {
        entries.add(new MembershipEntry(membership.getGroupId(), membership.getMembershipType()));
      }
      identity = new org.exoplatform.services.security.Identity(userId, entries);
    }
    List<MembershipEntry> spacesCreatorsMemberships = getSpacesCreatorsMemberships();
    if (spacesCreatorsMemberships != null && !spacesCreatorsMemberships.isEmpty()) {
      for (MembershipEntry spacesCreatorMembership : spacesCreatorsMemberships) {
        if ((spacesCreatorMembership.getMembershipType().equals("*") && identity.isMemberOf(spacesCreatorMembership.getGroup()))
            || identity.isMemberOf(spacesCreatorMembership)) {
          return true;
        }
      }
    }
    return false;
  }
}
