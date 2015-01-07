(function($) {
  var UIProfileUserSearch = { 
      INPUT_ID : {
        NAME : '#name',
        POSITION : '#position',
        SKILLS : '#skills',
        SEARCH : '#SearchButton'
      },
      KEY : {
        ENTER : 13
      },
      init: function(params) {
        console.log(params);
        UIProfileUserSearch.typeOfRelation = params.typeOfRelation || '';
        UIProfileUserSearch.spaceURL = params.spaceURL || '';
        UIProfileUserSearch.profileSearch = $("#" + params.uicomponentId);
        var profileSearch = UIProfileUserSearch.profileSearch;
        UIProfileUserSearch.searchBtn = profileSearch.find(UIProfileUserSearch.INPUT_ID.SEARCH);
        var nameEl = profileSearch.find(UIProfileUserSearch.INPUT_ID.NAME);
        var posEl = profileSearch.find(UIProfileUserSearch.INPUT_ID.POSITION);
        var skillEl = profileSearch.find(UIProfileUserSearch.INPUT_ID.SKILLS);
        
        // Turn off auto-complete attribute of text-box control
        nameEl.attr('autocomplete','off');
        posEl.attr('autocomplete','off').keydown(keyDownAction);
        skillEl.attr('autocomplete','off').keydown(keyDownAction);
        //
        $(nameEl).autosuggest(buildURL(), {onSelect:function(){UIProfileUserSearch.searchBtn.trigger("click");}, defaultVal:''});
        //
        function buildURL() {
          var restURL = "/" + eXo.social.portal.rest + '/social/people/suggest.json?nameToSearch=input_value';
          //
          var userName = eXo.social.portal.userName;
          if (userName && userName.length > 0) {
            restURL += "&currentUser=" + userName;
          }
          if (UIProfileUserSearch.typeOfRelation && UIProfileUserSearch.typeOfRelation.length > 0) {
            restURL += "&typeOfRelation=" + UIProfileUserSearch.typeOfRelation;
          }
          if (UIProfileUserSearch.spaceURL && UIProfileUserSearch.spaceURL.length > 0) {
            restURL += "&spaceURL=" + UIProfileUserSearch.spaceURL;
          }
          return restURL;
        };
        window.abc = UIProfileUserSearch.searchBtn
        function keyDownAction(evt) {
          //
          var keynum = evt.keyCode || evt.which;
          if (keynum == UIProfileUserSearch.KEY.ENTER) {
            //
            evt.stopPropagation();
            UIProfileUserSearch.searchBtn.trigger("click");
          }
        }
        //
        UIProfileUserSearch.resizeForm();
      },
      resizeForm : function() {
        var parent = UIProfileUserSearch.profileSearch;
        var label = parent.find('label:[for=name]');
        var searchBtn = $(UIProfileUserSearch.INPUT_ID.SEARCH, parent);
        var staticSize = label.outerWidth() + searchBtn.outerWidth() + 64;
        var inputSize = (parent.width() - staticSize) / 3;
        parent.find('input[type=text]').css( {'width': inputSize + 'px', 'minWidth' : '80px'});
      }
  };
  //
  $(window).on('resize', UIProfileUserSearch.resizeForm );
  
  return UIProfileUserSearch;
})($);