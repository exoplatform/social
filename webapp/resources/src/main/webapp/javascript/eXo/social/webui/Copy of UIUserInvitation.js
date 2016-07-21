(function($, selectize) {
    var invite = {
        build: function(selector, url, value) {
            if (value != '') {
                $.ajax({
                    type: "GET",
                    url: url,
                    data: { nameToSearch : '', initial : value },
                    complete: function(jqXHR) {
                        if(jqXHR.readyState === 4) {
                            var json = $.parseJSON(jqXHR.responseText)
                            if (json.options != null) {
                                invite.select(selector, url, json.options, value.split(','));
                            }
                        }
                    }
                });
            } else {
                invite.select(selector, url, null, null);
            }

        },

        select: function(selector, url, options, items) {
            $('#' + selector).selectize({
                options: options,
                items: items,
                plugins: ['remove_button', 'restore_on_backspace'],
                maxItems: null,
                valueField: 'value',
                labelField: 'text',
                searchField: ['text'],
                load: function(query, callback) {
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
                },
                create: true,
                render: {
                    option: function(item, escape) {
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
                    }
                },
                sortField: [{field: 'order'}, {field: '$score'}]
            });
        },

        notify: function(selector, anchor) {
            $(anchor).append($(selector));
        }
    };

    return invite;
})($, selectize);