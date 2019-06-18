package org.exoplatform.social.core.search;

import org.exoplatform.social.core.search.Sorting.OrderBy;
import org.exoplatform.social.core.search.Sorting.SortBy;

import junit.framework.TestCase;

/**
 * Test {@link Sorting} enum class
 */
public class SortingTest extends TestCase {

  public void testValueOf() {
    Sorting sortLastNameAsc = Sorting.valueOf("lastname", "asc");
    assertEquals(sortLastNameAsc, new Sorting(SortBy.LASTNAME, OrderBy.ASC));
    Sorting sortLastNameDesc = Sorting.valueOf("lastname", "desc");
    assertEquals(sortLastNameDesc, new Sorting(SortBy.LASTNAME, OrderBy.DESC));

    Sorting sortFirstNameAsc = Sorting.valueOf("firstname", "asc");
    assertEquals(sortFirstNameAsc, new Sorting(SortBy.FIRSTNAME, OrderBy.ASC));
    Sorting sortFirstNameDesc = Sorting.valueOf("firstname", "desc");
    assertEquals(sortFirstNameDesc, new Sorting(SortBy.FIRSTNAME, OrderBy.DESC));

    Sorting sortFullNameAsc = Sorting.valueOf("fullname", "asc");
    assertEquals(sortFullNameAsc, new Sorting(SortBy.FULLNAME, OrderBy.ASC));
    Sorting sortFullNameDesc = Sorting.valueOf("fullname", "desc");
    assertEquals(sortFullNameDesc, new Sorting(SortBy.FULLNAME, OrderBy.DESC));
  }
}
