package org.exoplatform.social.portlet.pagination;

import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.dashboard.webui.component.UIDashboard;

import java.util.List;

import com.google.common.collect.Lists;

public class UIPagination extends UIComponent {
  List<Paginated> paginatedList = Lists.newArrayList();
  int currentPage;
    
  public UIPagination() {

  }

  public void addListener(Paginated paginated) {
    paginatedList.add(paginated);
  }

  public int getCurrentPage() {
    return currentPage;
  }

  public void setCurrentPage(int currentPage) {
    if (currentPage != this.currentPage) {
      this.currentPage = currentPage;
      notifyListeners();
    }
    else
      this.currentPage = currentPage;
  }

  private void notifyListeners() {
    for (Paginated paginated : paginatedList) {
      paginated.changePage(this.currentPage);
    }
  }

  protected List<Paginated> getPaginatedList() {
    return paginatedList;
  }

  public static class ChangePageActionListener extends EventListener<UIPagination> {
    public final void execute(final Event<UIPagination> event) throws Exception {
      WebuiRequestContext context = event.getRequestContext();
      UIPagination uiPagination = event.getSource();

      String page = context.getRequestParameter(OBJECTID);
      uiPagination.setCurrentPage(Integer.parseInt(page));

      for (Paginated paginated : uiPagination.getPaginatedList()) {
        if(paginated instanceof UIComponent)
         context.addUIComponentToUpdateByAjax((UIComponent) paginated);
      }
    }
  }
}
