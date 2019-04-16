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
        $(nameEl).suggester({
          type : 'tag',
          plugins: ['restore_on_backspace'],
          preload: false,
          addPrecedence: true,
          persist: false,
          createOnBlur: true,
          highlight: false,
          hideSelected: true,
          openOnFocus: true,
          maxItems: 1,
          sourceProviders: ['exo:socialMembers'],
          valueField: 'text',
          labelField: 'text',
          searchField: ['text'],
          loadThrottle: null,
          optionIconField: 'avatarUrl',
          defaultOptionIcon: '/eXoSkin/skin/images/system/UserAvtDefault.png',
          onFocus: function() {
            this.positionDropdown();
            var value = this.getValue();
            if(value) {
              this.removeOption(value, false);
              this.setTextboxValue(value);
            }
          },
          onInitialize: function() {
            var searchElInput = UIProfileUserSearch.profileSearch.find('.selectize-input input[type="text"]');
            searchElInput.keydown(function(event) {
              var e = event || window.event;
              var keynum = e.keyCode || e.which;
              if(keynum == 13) {
                UIProfileUserSearch.searchBtn.click();
                event.stopPropagation();
              }
            });
          },
          create: function(input) {
            return {'value': input, 'text': input, 'invalid': true};
          },
          onItemAdd : function(value, $item) {
            $(nameEl)[0].selectize.setTextboxValue(value);
            $(nameEl).val(value);
            $(nameEl).attr('value', value);
            if(!$item.invalid) {
              UIProfileUserSearch.searchBtn.trigger("click");
              $(nameEl)[0].selectize.setTextboxValue(value);
            }
          },
          sortField: [{field: 'order'}, {field: '$score'}],
          providers: {
            'exo:socialMembers': function(query, callback) {
            if (query && query.length > 0) {
                $.ajax({
                    type: "GET",
                    url: buildURL(query),
                    complete: function(jqXHR) {
                        if(jqXHR.readyState === 4) {
                            var json = $.parseJSON(jqXHR.responseText)
                            if (json.options != null) {
                                callback(json.options);
                            }
                        }
                    }
                });
              }
            } 
          }
        });

        //
        function buildURL(query) {
          var restURL = "/" + eXo.social.portal.rest + '/social/people/suggest.json?nameToSearch='+query;
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
        $("#uiTableProfileUserSearchArrow").on("click", function() {
          $(this).closest(".uiProfileUserSearch").toggleClass("open");
          return false;
        });
      },
      resizeForm : function() {
        var parent = UIProfileUserSearch.profileSearch;
        var label = parent.find('label[for=name]');
        var searchBtn = $(UIProfileUserSearch.INPUT_ID.SEARCH, parent);
        var staticSize = label.outerWidth() + searchBtn.outerWidth() + 64;
        var inputSize = (parent.width() - staticSize) / 3;
        parent.find('input[type=text]').css( {'width': inputSize + 'px', 'minWidth' : '80px'});
      }
  };
  //
  $(window).on('resize', UIProfileUserSearch.resizeForm );
  
//  $("#uiTableProfileUserSearchArrow").on("click", UIProfileUserSearch.toggleProfileUserSearch);
  
  return UIProfileUserSearch;
})($);
