(function($) {  
  var UISpaceSearch = {
    init : function(params) {
      UISpaceSearch.typeOfSuggest = params.typeOfSuggest || '';
      UISpaceSearch.typeOfRelation = params.typeOfRelation || '';
      UISpaceSearch.spaceURL = params.spaceURL || '';
      UISpaceSearch.profileSearch = $("#" + params.uicomponentId);
      UISpaceSearch.searchBtn = UISpaceSearch.profileSearch.find('#SearchButton');
      var searchEl = UISpaceSearch.profileSearch.find('#SpaceSearch');
      // Turn off auto-complete attribute of text-box control
      searchEl.attr('autocomplete', 'off');
      //
      $(searchEl).autosuggest(buildURL(), {
        onSelect : function() {
          UISpaceSearch.searchBtn.trigger("click");
        },
        defaultVal : ''
      });
      //
      function buildURL() {
        var restURL = "/" + eXo.social.portal.rest + eXo.social.portal.context + '/social/spaces/suggest.json?conditionToSearch=input_value';
        //
        var userName = eXo.social.portal.userName;
        if (userName && userName.length > 0) {
          restURL += "&currentUser=" + userName;
        }
        if (UISpaceSearch.typeOfRelation && UISpaceSearch.typeOfRelation.length > 0) {
          restURL += "&typeOfRelation=" + UISpaceSearch.typeOfRelation;
        }
        if (UISpaceSearch.spaceURL && UISpaceSearch.spaceURL.length > 0) {
          if (UISpaceSearch.typeOfSuggest == 'people') {
            restURL += "&spaceURL=" + UISpaceSearch.spaceURL;
          }
        }
        return restURL;
      }
      function keyDownAction(evt) {
        //
        var keynum = evt.keyCode || evt.which;
        if (keynum == 13) {
          //
          evt.stopPropagation();
          UISpaceSearch.searchBtn.trigger("click");
        }
      }
    }
};

return UISpaceSearch;
})($);

