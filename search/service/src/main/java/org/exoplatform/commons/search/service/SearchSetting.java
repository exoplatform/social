package org.exoplatform.commons.search.service;

import java.util.List;

public class SearchSetting {
  long resultsPerPage;
  List<String> searchTypes;
  boolean searchCurrentSiteOnly;
  boolean hideSearchForm;
  boolean hideFacetsFilter;
  
  public long getResultsPerPage() {
    return resultsPerPage;
  }
  public void setResultsPerPage(long resultsPerPage) {
    this.resultsPerPage = resultsPerPage;
  }
  public List<String> getSearchTypes() {
    return searchTypes;
  }
  public void setSearchTypes(List<String> searchTypes) {
    this.searchTypes = searchTypes;
  }
  public boolean isSearchCurrentSiteOnly() {
    return searchCurrentSiteOnly;
  }
  public void setSearchCurrentSiteOnly(boolean searchCurrentSiteOnly) {
    this.searchCurrentSiteOnly = searchCurrentSiteOnly;
  }
  public boolean isHideSearchForm() {
    return hideSearchForm;
  }
  public void setHideSearchForm(boolean hideSearchForm) {
    this.hideSearchForm = hideSearchForm;
  }
  public boolean isHideFacetsFilter() {
    return hideFacetsFilter;
  }
  public void setHideFacetsFilter(boolean hideFacetsFilter) {
    this.hideFacetsFilter = hideFacetsFilter;
  }
  
  public SearchSetting(long resultsPerPage, List<String> searchTypes, boolean searchCurrentSiteOnly, boolean hideSearchForm, boolean hideFacetsFilter) {
    this.resultsPerPage = resultsPerPage;
    this.searchTypes = searchTypes;
    this.searchCurrentSiteOnly = searchCurrentSiteOnly;
    this.hideSearchForm = hideSearchForm;
    this.hideFacetsFilter = hideFacetsFilter;
  }
}


