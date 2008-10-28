package org.exoplatform.social.portlet.breadcrumb;

import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.social.portlet.URLUtils;
import org.exoplatform.web.application.RequestContext;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;

@ComponentConfig(
  template =  "app:/groovy/portal/webui/component/UIBreadcrumb.gtmpl"
)
public class UIBreadcrumb extends UIComponent {
  public UIBreadcrumb() {
    
  }

  List<PathElement> getPath() {
    List<PathElement> res = Lists.newArrayList();

    Map<String, String> url = URLUtils.decodeURL();

    PathElement el = createElement("home", URLUtils.generateURL(null, null, null));
    el.getPotentialChild().add(createElement("mydashboard", URLUtils.generateURL("mydashboard", null, null)));
    el.getPotentialChild().add(createElement("people", URLUtils.generateURL("people", null, null)));
    el.getPotentialChild().add(createElement("spaces", URLUtils.generateURL("manageSpace", null, null)));
    res.add(el);

    if(url.containsKey(URLUtils.MODULE)) {
      String moduleName = url.get(URLUtils.MODULE);
      el = createElement(moduleName, URLUtils.generateURL(moduleName, null, null));
      if(moduleName.equals("people")) {
        el.getPotentialChild().add(createElement("me", URLUtils.generateURL(moduleName, getCurrentUserName(), null)));
      }
      res.add(el);
      if(url.containsKey(URLUtils.USERNAME)) {
        String username = url.get(URLUtils.USERNAME);
        el = createElement(username, URLUtils.generateURL(moduleName, username, null));
        if(moduleName.equals("people")) {
          el.getPotentialChild().add(createElement("activities", URLUtils.generateURL(moduleName, username, "activities")));
          el.getPotentialChild().add(createElement("dashboard", URLUtils.generateURL(moduleName, username, "dashboard")));
          el.getPotentialChild().add(createElement("profile", URLUtils.generateURL(moduleName, username, null)));
        }
        res.add(el);
        if(url.containsKey(URLUtils.APPLICATION)) {
          res.add(createElement(url.get(URLUtils.APPLICATION), URLUtils.generateURL(moduleName, username, url.get(URLUtils.APPLICATION))));
        }
      }
    }
    return res;
  }

  public String getCurrentUserName() {
      // if we are not on the page of a user, we display the profile of the current user
    RequestContext context = RequestContext.getCurrentInstance();
    return context.getRemoteUser();
  }

  PathElement createElement(String name, String url) {
    PathElement el = new PathElement();
    el.setUrl(url);
    el.setName(name);
    return el;
  }
}
