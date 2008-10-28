package org.exoplatform.social.portlet.pagination;

/**
 * Created by IntelliJ IDEA.
 * User: jeremi
 * Date: Oct 17, 2008
 * Time: 3:54:27 PM
 * To change this template use File | Settings | File Templates.
 */
public interface Paginated {
  /**
   * callback when the page number is changed
   * @param page the number if the current page
   */
  void changePage(int page);

  /**
   *
   * @return the number of pages available
   */
  int getPageAvailable();
}
