package org.exoplatform.social.core.storage.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

import org.junit.Test;

import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.social.core.space.SpaceUtils;
import org.exoplatform.social.core.space.impl.DynamicSpacePortalLayoutMatcher;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;

public class DynamicSpacePortalLayoutMatcherTest {

  @Test
  public void testMatchSpaceLayout() {
    SpaceService spaceService = mock(SpaceService.class);
    String spacePrefix = "testMatchSpaceLayout";
    String templateName = spacePrefix + " Template";
    String groupId = SpaceUtils.SPACE_GROUP + "/" + spacePrefix;

    mockSpaceService(spaceService, spacePrefix, groupId, templateName);

    DynamicSpacePortalLayoutMatcher dynamicSpacePortalLayoutMatcher = new DynamicSpacePortalLayoutMatcher();
    dynamicSpacePortalLayoutMatcher.setSpaceService(spaceService);
    dynamicSpacePortalLayoutMatcher.setSpaceTemplateRegex(".+Template.*");

    assertFalse("Portal site shouldn't match",
                dynamicSpacePortalLayoutMatcher.matches(SiteKey.portal(spacePrefix), "currentSite"));
    assertTrue("Group site with temmplate name matching should match",
               dynamicSpacePortalLayoutMatcher.matches(SiteKey.group(groupId), "currentSite"));
    assertFalse("Super Dynamic layout Matcher class should fail when no current site name",
                dynamicSpacePortalLayoutMatcher.matches(SiteKey.group(groupId), null));
    assertFalse("Non existing space shouldn't match",
                dynamicSpacePortalLayoutMatcher.matches(SiteKey.group(groupId + "1"), "currentSite"));
  }

  @Test
  public void testEmptySpaceLayoutMatcher() {
    SpaceService spaceService = mock(SpaceService.class);
    String spacePrefix = "testMatchSpaceLayout";
    String templateName = spacePrefix + " Template";
    String groupId = SpaceUtils.SPACE_GROUP + "/" + spacePrefix;

    mockSpaceService(spaceService, spacePrefix, groupId, templateName);

    DynamicSpacePortalLayoutMatcher dynamicSpacePortalLayoutMatcher = new DynamicSpacePortalLayoutMatcher();
    dynamicSpacePortalLayoutMatcher.setSpaceService(spaceService);

    assertFalse("Portal site shouldn't match",
                dynamicSpacePortalLayoutMatcher.matches(SiteKey.portal(spacePrefix), "currentSite"));
    assertTrue("Group site with any temmplate name matching should match",
               dynamicSpacePortalLayoutMatcher.matches(SiteKey.group(groupId), "currentSite"));
    assertFalse("Super Dynamic layout Matcher class should fail when no current site name",
                dynamicSpacePortalLayoutMatcher.matches(SiteKey.group(groupId), null));
    assertFalse("Non existing space shouldn't match",
                dynamicSpacePortalLayoutMatcher.matches(SiteKey.group(groupId + "1"), "currentSite"));
  }

  private void mockSpaceService(SpaceService spaceService, String spacePrefix, String groupId, String templateName) {
    Space space = new Space();
    space.setDisplayName(spacePrefix + " Display Name");
    space.setPrettyName(spacePrefix + " Pretty Name");
    space.setGroupId(groupId);
    space.setTemplate(templateName);

    reset(spaceService);
    when(spaceService.getSpaceByGroupId(eq(groupId))).thenReturn(space);
  }
}
