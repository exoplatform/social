package org.exoplatform.social.portlet.breadcrumb;

import com.google.common.collect.Lists;

import java.util.List;

public class PathElement {
  String name;
  String url;
  List<PathElement> potentialChild = Lists.newArrayList();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public List<PathElement> getPotentialChild() {
        return potentialChild;
    }

    public void setPotentialChild(List<PathElement> potentialChild) {
        this.potentialChild = potentialChild;
    }
}
