package org.exoplatform.social.core.space.impl;

import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.portal.config.*;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.SiteType;
import org.exoplatform.social.core.space.SpaceUtils;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;

/**
 * A dynamic layout matcher that could be injected via
 * {@link DynamicPortalLayoutMatcherPlugin} on
 * {@link DynamicPortalLayoutService} to customize site layout to display for
 * exisintg spaces. This matcher add additional checks comparing to
 * {@link DynamicPortalLayoutMatcher} in order to :
 * 
 * <pre>
 * - Check if visited site is a space
 * 
 * - Check if visited space site matches spaceTemplate if a regex is configured
 * </pre>
 */
public class DynamicSpacePortalLayoutMatcher extends DynamicPortalLayoutMatcher {

  private String       spaceTemplateRegex   = null;

  private Pattern      spaceTemplatePattern = null;

  private SpaceService spaceService         = null;

  public void setSpaceTemplateRegex(String spaceTemplateRegex) {
    this.spaceTemplateRegex = spaceTemplateRegex;
  }

  public Pattern getSpaceTemplatePattern() {
    if (spaceTemplatePattern == null && StringUtils.isNotBlank(spaceTemplateRegex)) {
      spaceTemplatePattern = Pattern.compile(spaceTemplateRegex);
    }
    return spaceTemplatePattern;
  }

  @Override
  public boolean matches(SiteKey siteKey, String currentSiteName) {
    if (!super.matches(siteKey, currentSiteName)) {
      return false;
    }

    if (isSpaceSite(siteKey)) {
      Space space = getSpaceService().getSpaceByGroupId(siteKey.getName());
      if (space == null) {
        return false;
      }

      if (getSpaceTemplatePattern() == null) {
        return true;
      }

      if (space.getTemplate() == null || !getSpaceTemplatePattern().matcher(space.getTemplate()).matches()) {
        return false;
      }
    } else {
      return false;
    }
    return true;
  }

  public SpaceService getSpaceService() {
    if (spaceService == null) {
      spaceService = ExoContainerContext.getService(SpaceService.class);
    }
    return spaceService;
  }

  public void setSpaceService(SpaceService spaceService) {
    this.spaceService = spaceService;
  }

  private boolean isSpaceSite(SiteKey siteKey) {
    return SiteType.GROUP.equals(siteKey.getType()) && siteKey.getName().startsWith(SpaceUtils.SPACE_GROUP);
  }
}
