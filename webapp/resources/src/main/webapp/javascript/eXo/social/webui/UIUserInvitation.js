(function($) {
    var invite = {
        build: function(selector, url, placeholder) {
            $('#' + selector).suggester({
                type : 'tag',
                placeholder: placeholder,
                plugins: ['remove_button', 'restore_on_backspace'],
                preload: true,
                maxItems: null,
                valueField: 'value',
                labelField: 'text',
                searchField: ['text', 'value'],
                sourceProviders: ['exo:social'],
                create: true,
                renderMenuItem: function(item, escape) {
                  var avatar = item.avatarUrl;
                  if (avatar == null) {
                      if (item.type == "space") {
                          avatar = '/eXoSkin/skin/images/system/SpaceAvtDefault.png';
                      } else {
                          avatar = '/eXoSkin/skin/images/system/UserAvtDefault.png';
                      }
                  }

                  var text = item.text;
                  if (item.type == 'user') {
                      text += ' (' + item.value + ')';
                  }

                  return '<div class="option">' +
                  '<img width="20px" height="20px" src="' + avatar + '"> ' +
                  escape(text) + '</div>';
              },
              sortField: [{field: 'order'}, {field: '$score'}],
              providers: {
                'exo:social': function(query, callback) {
                    if (query == '') {
                      var thizz = this;
                      // Pre-load options for initial users
                      if (this.items && this.items.length > 0) {
                          $.ajax({
                              type: "GET",
                              url: url,
                              data: { nameToSearch : this.items.join() },
                              complete: function(jqXHR) {
                                  if(jqXHR.readyState === 4) {
                                      var json = $.parseJSON(jqXHR.responseText)
                                      if (json.options != null) {
                                          callback(json.options);
                                          for (var i = 0; i < json.options.length; i++) {
                                              thizz.updateOption(json.options[i].value, json.options[i]);
                                          }
                                      }
                                  }
                              }
                          });
                      }
                    } else {
                        $.ajax({
                            type: "GET",
                            url: url,
                            data: { nameToSearch : query },
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
        },

        notify: function(selector, anchor) {
            $(anchor).append($(selector));
        }
    };

    return invite;
})($);