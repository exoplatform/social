(function(w) {
  var eXo = w.eXo || {};
  if (eXo.provide) {
    //when eXo.provide available from Portal code base
    //see: https://github.com/exoportal/exogtn/pull/5
    return;
  }
  /**
   * Exposes a function to attach its to a namespace, this is similar but more advanced than goog.provide(namespace)
   *
   * @param namespace the name space string, for example: "eXo.social.MyClass".
   * @param obj the associated function or singleton object to be associated with this name space
   */
  eXo.provide = function(namespace, obj) {
    var names = namespace.split('.');
    if (names.length > 0) {
      var tempNamespace = window;
      for (var i = 0, l = names.length; i < l; i++) {
        if (i == (l - 1)) {
          //the last one
          tempNamespace[names[i]] = obj;
        } else {
          tempNamespace = tempNamespace[names[i]] = tempNamespace[names[i]] || {};
        }
      }
    }
  };

  /**
   * Simple clone of jQuery#extend().
   *
   * @param targetObj    An object that will receive the new properties if additional objects are passed in.
   * @param obj          An object containing additional properties to merge in.
   */
  eXo.extend = function(targetObj, obj) {
    var src, copy;
    if (window.jQuery) {
      if (console) {
        console.warn("jQuery is available now. Please use jQuery#extend() instead.");
      }
    }
    if (targetObj && obj) {
      for (name in obj) {
        src = targetObj[name];
        copy = obj[name];
        if (copy !== null && src !== copy) {
          targetObj[name] = obj[name];
        }
      }
    }
    return targetObj;
  }

})(window);
