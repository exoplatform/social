/*
 * Copyright (C) 2003-2014 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.social.notification;

import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.space.model.Space;

public class LinkProviderUtilsTest extends AbstractCoreTest {

  @Override
  public void setUp() throws Exception {
    super.setUp();
  }

  public void testGetInviteToConnectUrl() {
    String receiverId = "demo", senderId = "root";
    String expected = "http://exoplatform.com/rest/social/notifications/inviteToConnect/demo/root";
    assertEquals(expected, LinkProviderUtils.getInviteToConnectUrl(receiverId, senderId));
  }

  public void testGetConfirmInvitationToConnectUrl() {
    String receiverId = "demo", senderId = "root";
    String expected = "http://exoplatform.com/rest/social/notifications/confirmInvitationToConnect/root/demo";
    assertEquals(expected, LinkProviderUtils.getConfirmInvitationToConnectUrl(senderId, receiverId));
  }

  public void testGetIgnoreInvitationToConnectUrl() {
    String receiverId = "demo", senderId = "root";
    String expected = "http://exoplatform.com/rest/social/notifications/ignoreInvitationToConnect/root/demo";
    assertEquals(expected, LinkProviderUtils.getIgnoreInvitationToConnectUrl(senderId, receiverId));
  }

  public void testGetAcceptInvitationToJoinSpaceUrl() {
    String spaceId = "5fc9eef07f000101", userId = "root";
    String expected = "http://exoplatform.com/rest/social/notifications/acceptInvitationToJoinSpace/5fc9eef07f000101/root";
    assertEquals(expected, LinkProviderUtils.getAcceptInvitationToJoinSpaceUrl(spaceId, userId));
  }

  public void testGetIgnoreInvitationToJoinSpaceUrl() {
    String spaceId = "5fc9eef07f000101", userId = "root";
    String expected = "http://exoplatform.com/rest/social/notifications/ignoreInvitationToJoinSpace/5fc9eef07f000101/root";
    assertEquals(expected, LinkProviderUtils.getIgnoreInvitationToJoinSpaceUrl(spaceId, userId));
  }

  public void testGetValidateRequestToJoinSpaceUrl() {
    String spaceId = "5fc9eef07f000101", userId = "root";
    String expected = "http://exoplatform.com/rest/social/notifications/validateRequestToJoinSpace/5fc9eef07f000101/root";
    assertEquals(expected, LinkProviderUtils.getValidateRequestToJoinSpaceUrl(spaceId, userId));
  }

  public void testGetRefuseRequestToJoinSpaceUrl() {
    String spaceId = "5fc9eef07f000101", userId = "root";
    String expected = "http://exoplatform.com/rest/social/notifications/refuseRequestToJoinSpace/5fc9eef07f000101/root";
    assertEquals(expected, LinkProviderUtils.getRefuseRequestToJoinSpaceUrl(spaceId, userId));
  }

  public void testGetRedirectUrl() {
    String type = "activity", objectId = "5fc9eef07f000101";
    String expected = "http://exoplatform.com/rest/social/notifications/redirectUrl/activity/5fc9eef07f000101";
    assertEquals(expected, LinkProviderUtils.getRedirectUrl(type, objectId));
  }

  public void testGetRestUrl() {
    String objectId1 = "openspace", objectId2 = "spaceId";
    String expected = "http://exoplatform.com/rest/social/openspace/spaceId";
    assertEquals(expected, LinkProviderUtils.getRestUrl("social", objectId1, objectId2));
  }

  public void testGetBaseRestUrl() {
    assertEquals("http://exoplatform.com/rest", LinkProviderUtils.getBaseRestUrl());
  }

  public void testGetUserAvatarUrl() {
    String expected = "http://exoplatform.com/eXoSkin/skin/less/social/skin/images/ShareImages/UserAvtDefault.png";
    assertEquals(expected, LinkProviderUtils.getUserAvatarUrl(null));
    Profile profile = new Profile(new Identity("demo"));
    assertEquals(expected, LinkProviderUtils.getUserAvatarUrl(profile));
    //
    profile.setAvatarUrl("/rest/jcr/repository/social/users/avatar/demo");
    expected = "http://exoplatform.com/rest/jcr/repository/social/users/avatar/demo";
    assertEquals(expected, LinkProviderUtils.getUserAvatarUrl(profile));
  }

  public void testGetSpaceAvatarUrl() {
    String expected = "http://exoplatform.com/eXoSkin/skin/less/social/skin/images/ShareImages/SpaceAvtDefault.png";
    assertEquals(expected, LinkProviderUtils.getSpaceAvatarUrl(null));
    Space space = new Space();
    assertEquals(expected, LinkProviderUtils.getSpaceAvatarUrl(space));
    //
    space.setAvatarUrl("/rest/jcr/repository/social/spaces/avatar/space_test");
    expected = "http://exoplatform.com/rest/jcr/repository/social/spaces/avatar/space_test";
    assertEquals(expected, LinkProviderUtils.getSpaceAvatarUrl(space));
  }
}
