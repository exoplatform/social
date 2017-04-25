(function($) {
    var locale = {
        YES : '${UINotification.action.switch.on}',
        NO : '${UINotification.action.switch.off}'
    };

    locale.resolve = function(key) {
        var message = key;
        if (locale[key] != undefined) {
            message = locale[key];
        }
        return message;
    };

    return locale;
})($);
