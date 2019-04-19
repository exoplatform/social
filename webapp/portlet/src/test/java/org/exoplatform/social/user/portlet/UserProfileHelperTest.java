package org.exoplatform.social.user.portlet;

import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static junit.framework.TestCase.*;

public class UserProfileHelperTest {

    @Test
    public void shouldSortExperiencesWithCurrentExperienceFirst() throws Exception {
        // Given
        Profile profile = new Profile(new Identity("john"));
        List<Map<String, String>> experiences = new ArrayList<>();
        Map<String, String> experience1 = new HashMap<>();
        experience1.put(Profile.EXPERIENCES_ID, "1");
        experience1.put(Profile.EXPERIENCES_COMPANY, "Google");
        experience1.put(Profile.EXPERIENCES_POSITION, "Developer");
        experience1.put(Profile.EXPERIENCES_IS_CURRENT, "true");
        experience1.put(Profile.EXPERIENCES_START_DATE,"01/09/2001");
        experiences.add(experience1);
        Map<String, String> experience2 = new HashMap<>();
        experience2.put(Profile.EXPERIENCES_ID, "2");
        experience2.put(Profile.EXPERIENCES_COMPANY, "eXo");
        experience2.put(Profile.EXPERIENCES_POSITION, "Developer");
        experience2.put(Profile.EXPERIENCES_IS_CURRENT, "false");
        experience2.put(Profile.EXPERIENCES_START_DATE,"01/09/2003");
        experience2.put(Profile.EXPERIENCES_END_DATE,"01/09/2004");
        experiences.add(experience2);
        Map<String, String> experience3 = new HashMap<>();
        experience3.put(Profile.EXPERIENCES_ID, "3");
        experience3.put(Profile.EXPERIENCES_COMPANY, "Apple");
        experience3.put(Profile.EXPERIENCES_POSITION, "Developer");
        experience3.put(Profile.EXPERIENCES_IS_CURRENT, "true");
        experience3.put(Profile.EXPERIENCES_START_DATE,"01/09/2007");
        experiences.add(experience3);
        Map<String, String> experience4 = new HashMap<>();
        experience4.put(Profile.EXPERIENCES_ID, "4");
        experience4.put(Profile.EXPERIENCES_COMPANY, "Youtube");
        experience4.put(Profile.EXPERIENCES_POSITION, "Developer");
        experience4.put(Profile.EXPERIENCES_IS_CURRENT, "false");
        experience4.put(Profile.EXPERIENCES_START_DATE,"01/09/2005");
        experience4.put(Profile.EXPERIENCES_END_DATE,"01/12/2006");
        experiences.add(experience4);
        profile.setProperty(Profile.EXPERIENCES, experiences);

        // When
        List<Map<String, String>> displayExperiences = UserProfileHelper.getSortedExperiences(profile);

        // Then
        assertNotNull(displayExperiences);
        assertEquals(4, displayExperiences.size());
        // The current experience must be the first one
        assertEquals("3", displayExperiences.get(0).get(Profile.EXPERIENCES_ID));
        assertEquals("1", displayExperiences.get(1).get(Profile.EXPERIENCES_ID));
        assertEquals("4", displayExperiences.get(2).get(Profile.EXPERIENCES_ID));
        assertEquals("2", displayExperiences.get(3).get(Profile.EXPERIENCES_ID));
    }

    @Test
    public void shouldEscapeHTMLInExperience() throws Exception {
        // Given
        Map<String, String> experience = new HashMap<>();
        experience.put(Profile.EXPERIENCES_ID, "1");
        experience.put(Profile.EXPERIENCES_COMPANY, "<script>alert(1)</script>");
        experience.put(Profile.EXPERIENCES_POSITION, "<script>alert(2)</script>");
        experience.put(Profile.EXPERIENCES_DESCRIPTION, "<svg onload=alert(3)//");
        experience.put(Profile.EXPERIENCES_SKILLS, "<svg onload=alert(4)//");
        experience.put(Profile.EXPERIENCES_IS_CURRENT, "false");

        // When
        Map<String, String> escapedExperience = UserProfileHelper.escapeExperience(experience);

        // Then
        assertNotNull(escapedExperience);
        assertEquals("1", escapedExperience.get(Profile.EXPERIENCES_ID));
        assertEquals("&lt;script&gt;alert(1)&lt;/script&gt;", escapedExperience.get(Profile.EXPERIENCES_COMPANY));
        assertEquals("&lt;script&gt;alert(2)&lt;/script&gt;", escapedExperience.get(Profile.EXPERIENCES_POSITION));
        assertEquals("&lt;svg onload=alert(3)//", escapedExperience.get(Profile.EXPERIENCES_DESCRIPTION));
        assertEquals("&lt;svg onload=alert(4)//", escapedExperience.get(Profile.EXPERIENCES_SKILLS));
    }
}