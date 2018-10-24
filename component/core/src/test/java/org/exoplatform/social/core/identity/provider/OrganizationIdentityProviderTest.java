package org.exoplatform.social.core.identity.provider;

import org.exoplatform.services.organization.*;
import org.exoplatform.services.organization.idm.UserImpl;
import org.exoplatform.services.organization.impl.UserProfileImpl;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class OrganizationIdentityProviderTest {

    @Mock
    private OrganizationService organizationService;

    @Mock
    private UserHandler userHandler;

    @Mock
    private UserProfileHandler userProfileHandler;

    @Test
    public void shouldUpdateProfileWhenResetingGender() throws Exception {
        // Given
        Mockito.when(userHandler.findUserByName(Mockito.eq("john"))).thenReturn(new UserImpl("john"));
        Mockito.when(organizationService.getUserHandler()).thenReturn(userHandler);
        UserProfileImpl existingProfile = new UserProfileImpl("john");
        existingProfile.setAttribute(Profile.GENDER, "Male");
        existingProfile.setAttribute(Profile.POSITION, "Product Manager");
        Mockito.when(userProfileHandler.findUserProfileByName(Mockito.eq("john"))).thenReturn(existingProfile);
        Mockito.when(organizationService.getUserProfileHandler()).thenReturn(userProfileHandler);

        OrganizationIdentityProvider organizationIdentityProvider = new OrganizationIdentityProvider(organizationService);

        Profile updatedProfile = new Profile(new Identity("john"));
        updatedProfile.setProperty(Profile.USERNAME, "john");
        updatedProfile.setProperty(Profile.GENDER, "");
        updatedProfile.setProperty(Profile.POSITION, "Product Manager");
        updatedProfile.setListUpdateTypes(Arrays.asList(Profile.UpdateType.CONTACT));

        // When
        organizationIdentityProvider.onUpdateProfile(updatedProfile);

        // Then
        ArgumentCaptor<UserProfile> userProfileCaptor = ArgumentCaptor.forClass(UserProfile.class);
        Mockito.verify(userProfileHandler, Mockito.times(1)).saveUserProfile(userProfileCaptor.capture(), Mockito.eq(false));
        UserProfile savedProfile = userProfileCaptor.getValue();
        assertEquals("", savedProfile.getAttribute(UserProfile.PERSONAL_INFO_KEYS[4]));
        assertEquals("Product Manager", savedProfile.getAttribute(UserProfile.PERSONAL_INFO_KEYS[7]));
    }

    @Test
    public void shouldUpdateProfileWhenResetingPosition() throws Exception {
        // Given
        Mockito.when(userHandler.findUserByName(Mockito.eq("john"))).thenReturn(new UserImpl("john"));
        Mockito.when(organizationService.getUserHandler()).thenReturn(userHandler);
        UserProfileImpl existingProfile = new UserProfileImpl("john");
        existingProfile.setAttribute(Profile.POSITION, "Product Manager");
        Mockito.when(userProfileHandler.findUserProfileByName(Mockito.eq("john"))).thenReturn(existingProfile);
        Mockito.when(organizationService.getUserProfileHandler()).thenReturn(userProfileHandler);

        OrganizationIdentityProvider organizationIdentityProvider = new OrganizationIdentityProvider(organizationService);

        Profile updatedProfile = new Profile(new Identity("john"));
        updatedProfile.setProperty(Profile.USERNAME, "john");
        updatedProfile.setProperty(Profile.POSITION, "");
        updatedProfile.setListUpdateTypes(Arrays.asList(Profile.UpdateType.CONTACT));

        // When
        organizationIdentityProvider.onUpdateProfile(updatedProfile);

        // Then
        ArgumentCaptor<UserProfile> userProfileCaptor = ArgumentCaptor.forClass(UserProfile.class);
        Mockito.verify(userProfileHandler, Mockito.times(1)).saveUserProfile(userProfileCaptor.capture(), Mockito.eq(false));
        UserProfile savedProfile = userProfileCaptor.getValue();
        assertEquals("", savedProfile.getAttribute(UserProfile.PERSONAL_INFO_KEYS[7]));
    }
}