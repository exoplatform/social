(function($, selectize) {
    var invite = {
        build: function(selector, url) {
            $('#' + selector).selectize({
                plugins: ['remove_button', 'restore_on_backspace'],
                maxItems: null,
                valueField: 'value',
                labelField: 'text',
                searchField: ['value', 'text'],
                load: function(query, callback) {
                    $.ajax({
                        type: "GET",
                        url: url,
                        data: { nameToSearch : query },
                        complete: function(jqXHR) {
                            if(jqXHR.readyState === 4) {
                                var json = $.parseJSON(jqXHR.responseText)
                                for (var i = 0; i < json.names.length; i++) {
                                    var obj = {};
                                    obj.value = json.names[i];
                                    obj.text = json.fullNames[i];
                                    obj.avatarUrl = json.avatars[i];
                                    var array = [];
                                    array.push(obj);
                                    callback(array);
                                }
                            }
                        }
                    });
                },
                create: true
            });
        }
    };

    return invite;
})($, selectize);