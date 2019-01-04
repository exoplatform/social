package org.exoplatform.social.core.space.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.ServletContext;

import org.apache.commons.lang.StringUtils;
import org.exoplatform.management.annotations.ManagedBy;
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
import org.exoplatform.portal.mop.page.PageContext;
import org.exoplatform.portal.mop.page.PageKey;
import org.exoplatform.portal.mop.page.PageService;
import org.exoplatform.portal.mop.page.PageState;
import org.exoplatform.services.security.MembershipEntry;
import org.exoplatform.social.core.space.SpacesAdministrationService;

/**
 * Service to manage administration of spaces
 */
@ManagedBy(SpaceAdministrationServiceManagerBean.class)
public class SpacesAdministrationServiceImpl implements Startable, SpacesAdministrationService {

  private static final String SPACES_SUPER_ADMINISTRATORS_PARAM = "spaces.super.administrators";

  private static final String SPACES_SUPER_CREATORS_PARAM = "spaces.super.creators";

  private static final String SPACES_ADMINISTRATORS_SETTING_KEY = "exo:social_spaces_administrators";

  private static final String SPACES_CREATORS_SETTING_KEY = "exo:social_spaces_creators";

  public static final String SPACES_ADMINISTRATION_PAGE_KEY = "group::/platform/administrators::spacesAdministration";

  private SettingService settingService;

  private List<MembershipEntry> superManagersMemberships = new ArrayList<>();

  private List<MembershipEntry> spaceCreatorsMemberships = new ArrayList<>();

  public SpacesAdministrationServiceImpl(InitParams initParams, SettingService settingService) {
    this.settingService = settingService;

    loadSettings(initParams);
  }

  @Override
  public void start() {
    // update Spaces administration at startup in case the configuration has changed
    PortalContainer.addInitTask(PortalContainer.getInstance().getPortalContext(), new RootContainer.PortalContainerPostInitTask() {
      @Override
      public void execute(ServletContext context, PortalContainer portalContainer) {
        List<MembershipEntry> superManagersMemberships = SpacesAdministrationServiceImpl.this.getSuperManagersMemberships();
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
  public void addSuperManagersMembership(String permissionExpression) {
    if(StringUtils.isBlank(permissionExpression)) {
      throw new IllegalArgumentException("Permission expression couldn't be empty");
    }
    if (!permissionExpression.contains(":/")) {
      throw new IllegalArgumentException("Invalid entry '" + permissionExpression + "'. A permission expression is expected (mstype:groupId).");
    }
    String[] membershipParts = permissionExpression.split(":");
    superManagersMemberships.add(new MembershipEntry(membershipParts[1], membershipParts[0]));

    updateSpacesAdministrationPagePermissions(superManagersMemberships);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void removeSuperManagersMembership(String permissionExpression) {
    if (StringUtils.isBlank(permissionExpression)) {
      throw new IllegalArgumentException("Permission expression couldn't be empty");
    }
    if (!permissionExpression.contains(":/")) {
      throw new IllegalArgumentException("Invalid entry '" + permissionExpression
              + "'. A permission expression is expected (mstype:groupId).");
    }
    Iterator<MembershipEntry> superManagersMembershipsIterator = superManagersMemberships.iterator();
    while (superManagersMembershipsIterator.hasNext()) {
      MembershipEntry membershipEntry = superManagersMembershipsIterator.next();
      if (permissionExpression.trim().equals(membershipEntry.toString())) {
        superManagersMembershipsIterator.remove();
      }
    }

    updateSpacesAdministrationPagePermissions(superManagersMemberships);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<MembershipEntry> getSuperManagersMemberships() {
    return Collections.unmodifiableList(superManagersMemberships);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<MembershipEntry> getSuperCreatorsMemberships() {
    return Collections.unmodifiableList(spaceCreatorsMemberships);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void addSpacesCreatorsMembership(String permissionExpression) {
    if(StringUtils.isBlank(permissionExpression)) {
      throw new IllegalArgumentException("Permission expression couldn't be empty");
    }
    if (!permissionExpression.contains(":/")) {
      this.spaceCreatorsMemberships.add(new MembershipEntry(permissionExpression));
    } else {
      String[] membershipParts = permissionExpression.split(":");
      this.spaceCreatorsMemberships.add(new MembershipEntry(membershipParts[1], membershipParts[0]));
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void removeSpacesCreatorsMembership(String permissionExpression) {
    if (StringUtils.isBlank(permissionExpression)) {
      throw new IllegalArgumentException("Permission expression couldn't be empty");
    }
    Iterator<MembershipEntry> superCreatorsMembershipsIterator = spaceCreatorsMemberships.iterator();
    while (superCreatorsMembershipsIterator.hasNext()) {
      MembershipEntry membershipEntry = superCreatorsMembershipsIterator.next();
      if (permissionExpression.trim().equals(membershipEntry.toString())) {
        superCreatorsMembershipsIterator.remove();
      }
    }
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
      addManagerMemberships(administratorsArray);
    } else if (initParams != null) {
      if (initParams.containsKey(SPACES_SUPER_ADMINISTRATORS_PARAM)) {
        ValueParam superAdministratorParam = initParams.getValueParam(SPACES_SUPER_ADMINISTRATORS_PARAM);
        String superManagersMemberships = superAdministratorParam.getValue();
        if (StringUtils.isNotBlank(superManagersMemberships)) {
          String[] superManagersMembershipsArray = superManagersMemberships.split(",");
          addManagerMemberships(superManagersMembershipsArray);
        }
      }
    }

    SettingValue<String> creators = (SettingValue<String>) settingService.get(Context.GLOBAL, Scope.GLOBAL, SPACES_CREATORS_SETTING_KEY);
    if (creators != null && !StringUtils.isBlank(creators.getValue())) {
      String[] creatorsArray = creators.getValue().split(",");
      addCreatorsMemberships(creatorsArray);
    } else if (initParams != null) {
      if (initParams.containsKey(SPACES_SUPER_CREATORS_PARAM)) {
        ValueParam superCreatorParam = initParams.getValueParam(SPACES_SUPER_CREATORS_PARAM);
        String superCreatorsMemberships = superCreatorParam.getValue();
        if (StringUtils.isNotBlank(superCreatorsMemberships)) {
          String[] superCreatorsMembershipsArray = superCreatorsMemberships.split(",");
          addCreatorsMemberships(superCreatorsMembershipsArray);
        }
      }
    }
  }

  private void addCreatorsMemberships(String[] creatorsArray) {
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

  private void addManagerMemberships(String[] administratorsArray) {
    for(String administrator : administratorsArray) {
      if (StringUtils.isBlank(administrator)) {
        continue;
      }
      if (!administrator.contains(":/")) {
        this.superManagersMemberships.add(new MembershipEntry(administrator));
      } else {
        String[] membershipParts = administrator.split(":");
        this.superManagersMemberships.add(new MembershipEntry(membershipParts[1], membershipParts[0]));
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
}
