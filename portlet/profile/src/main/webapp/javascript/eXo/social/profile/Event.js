/*
 Copyright (c) 2008, Yahoo! Inc. All rights reserved.
 Code licensed under the BSD License:
 http://developer.yahoo.net/yui/license.txt
 version: 2.5.2
 */


var eXo = window.eXo;
eXo.social = eXo.social || {};
eXo.social.profile = eXo.social.profile || {};


function Event() {
  this.listeners = [];

  /**
   * Cache of DOM0 event handlers to work around issues with DOM2 events
   * in Safari
   * @property legacyEvents
   * @static
   * @private
   */
  this.legacyEvents = [];

  /**
   * Lookup table for legacy events
   * @property legacyMap
   * @static
   * @private
   */
  this.legacyMap = [];

  /**
   * User-defined unload function that will be fired before all events
   * are detached
   * @property unloadListeners
   * @type array
   * @static
   * @private
   */
  this.unloadListeners = [];

  /**
   * Counter for auto id generation
   * @property counter
   * @static
   * @private
   */
  this.counter = 0;


  /**
   * Listener stack for DOM0 events
   * @property legacyHandlers
   * @static
   * @private
   */
  this.legacyHandlers = [];


  /**
   * Element to bind, int constant
   * @property EL
   * @type int
   * @static
   * @final
   */
  this.EL = 0;

  /**
   * Type of event, int constant
   * @property TYPE
   * @type int
   * @static
   * @final
   */
  this.TYPE = 1;

  /**
   * Function to execute, int constant
   * @property FN
   * @type int
   * @static
   * @final
   */
  this.FN = 2;

  /**
   * Function wrapped for scope correction and cleanup, int constant
   * @property WFN
   * @type int
   * @static
   * @final
   */
  this.WFN = 3;

  /**
   * Adjusted scope, either the element we are registering the event
   * on or the custom object passed in by the listener, int constant
   * @property ADJ_SCOPE
   * @type int
   * @static
   * @final
   */
  this.ADJ_SCOPE = 4;

  /**
   * The original obj passed into addListener
   * @property OBJ
   * @type int
   * @static
   * @final
   */
  this.OBJ = 5;

  /**
   * The original scope parameter passed into addListener
   * @property OVERRIDE
   * @type int
   * @static
   * @final
   */
  this.OVERRIDE = 6;

}


/**
 * Appends an event handler
 *
 * @method addListener
 *
 * @param {String|HTMLElement} el An id, an element 
 *  reference, or a collection of ids and/or elements to assign the
 *  listener to.
 * @param {String}   sType     The type of event to append
 * @param {Function} fn        The method the event invokes
 * @param {Object}   obj    An arbitrary object that will be
 *                             passed as a parameter to the handler
 * @param {Boolean|object}  override  If true, the obj passed in becomes
 *                             the execution scope of the listener. If an
 *                             object, this object becomes the execution
 *                             scope.
 * @return {Boolean} True if the action was successful or defered,
 *                        false if one or more of the elements
 *                        could not have the listener attached,
 *                        or if the operation throws an exception.
 * @static
 */
Event.prototype.addListener = function(/* String|HTMLElement */ el, /* String */ sType,
  /* Function */ fn, /* Object */ obj, /* Boolean|object */ override) {
  if (!fn || !fn.call) {
    return false;
  }

  if (eXo.social.profile.Type.isString(el)) {
    el = document.getElementById(el);
  }

  if (!el) {
    return false;
  }

  // we need to make sure we fire registered unload events 
  // prior to automatically unhooking them.  So we hang on to
  // these instead of attaching them to the window and fire the
  // handles explicitly during our one unload event.
  if ("unload" == sType && obj !== this) {
      this.unloadListeners[this.unloadListeners.length] =
              [el, sType, fn, obj, override];
      return true;
  }

  // if the user chooses to override the scope, we use the custom
  // object passed in, otherwise the executing scope will be the
  // HTML element that the event is registered on
  var scope = el;
  if (override) {
    if (override === true) {
      scope = obj;
    } else {
      scope = override;
    }
  }

  // wrap the function so we can return the obj object when
  // the event fires;
  var wrappedFn = function(e) {
    return fn.call(scope, eXo.social.profile.Event.getEvent(e, el),
        obj);
  };

  var li = [el, sType, fn, wrappedFn, scope, obj, override];
  var index = this.listeners.length;
  // cache the listener so we can try to automatically unload
  this.listeners[index] = li;

  if (this.useLegacyEvent(el, sType)) {
    var legacyIndex = this.getLegacyIndex(el, sType);

    // Add a new dom0 wrapper if one is not detected for this
    // element
    if (legacyIndex == -1 ||
        el != this.legacyEvents[legacyIndex][0]) {

      legacyIndex = this.legacyEvents.length;
      this.legacyMap[el.id + sType] = legacyIndex;

      // cache the signature for the DOM0 event, and
      // include the existing handler for the event, if any
      this.legacyEvents[legacyIndex] =
      [el, sType, el["on" + sType]];
      this.legacyHandlers[legacyIndex] = [];

      el["on" + sType] =
      function(e) {
        eXo.social.profile.Event.fireLegacyEvent(
            eXo.social.profile.Event.getEvent(e), legacyIndex);
      };
    }

    // add a reference to the wrapped listener to our custom
    // stack of events
    //legacyHandlers[legacyIndex].push(index);
    this.legacyHandlers[legacyIndex].push(li);

  } else {
    try {
      this._simpleAdd(el, sType, wrappedFn, false);
    } catch(ex) {
      // handle an error trying to attach an event.  If it fails
      // we need to clean up the cache
      this.lastError = ex;
      this.removeListener(el, sType, fn);
      return false;
    }
  }

  return true;


}


/**
 * Removes an event listener
 *
 * @method removeListener
 *
 * @param {String|HTMLElement|Array|NodeList} el An id, an element
 *  reference, or a collection of ids and/or elements to remove
 *  the listener from.
 * @param {String} sType the type of event to remove.
 * @param {Function} fn the method the event invokes.  If fn is
 *  undefined, then all event handlers for the type of event are
 *  removed.
 * @return {boolean} true if the unbind was successful, false
 *  otherwise.
 * @static
 */
Event.prototype.removeListener = function(el, sType, fn) {
  var i, len, li;

  // The el argument can be a string
  if (typeof el == "string") {
    el = document.getElementById(el);
    // The el argument can be an array of elements or element ids.
  }

  if (!fn || !fn.call) {
    // this.logger.debug("Error, function is not valid " + fn);
    //return false;
    return this.purgeElement(el, false, sType);
  }

  if ("unload" == sType) {

    for (i = this.unloadListeners.length - 1; i > -1; i--) {
      li = this.unloadListeners[i];
      if (li &&
          li[0] == el &&
          li[1] == sType &&
          li[2] == fn) {
        this.unloadListeners.splice(i, 1);
        // unloadListeners[i]=null;
        return true;
      }
    }

    return false;
  }

  var cacheItem = null;

  // The index is a hidden parameter; needed to remove it from
  // the method signature because it was tempting users to
  // try and take advantage of it, which is not possible.
  var index = arguments[3];

  if ("undefined" === typeof index) {
    index = this._getCacheIndex(el, sType, fn);
  }

  if (index >= 0) {
    cacheItem = this.listeners[index];
  }

  if (!el || !cacheItem) {
    // this.logger.debug("cached listener not found");
    return false;
  }

  // this.logger.debug("Removing handler: " + el + ", " + sType);

  if (this.useLegacyEvent(el, sType)) {
    var legacyIndex = this.getLegacyIndex(el, sType);
    var llist = this.legacyHandlers[legacyIndex];
    if (llist) {
      for (i = 0,len = llist.length; i < len; ++i) {
        // for (i in llist.length) {
        li = llist[i];
        if (li &&
            li[this.EL] == el &&
            li[this.TYPE] == sType &&
            li[this.FN] == fn) {
          llist.splice(i, 1);
          // llist[i]=null;
          break;
        }
      }
    }

  } else {
    try {
      this._simpleRemove(el, sType, cacheItem[this.WFN], false);
    } catch(ex) {
      this.lastError = ex;
      return false;
    }
  }

  // removed the wrapped handler
  delete this.listeners[index][this.WFN];
  delete this.listeners[index][this.FN];
  this.listeners.splice(index, 1);
  // listeners[index]=null;

  return true;

}

/**
 * Removes all listeners attached to the given element via addListener.
 * Optionally, the node's children can also be purged.
 * Optionally, you can specify a specific type of event to remove.
 * @method purgeElement
 * @param {HTMLElement} el the element to purge
 * @param {boolean} recurse recursively purge this element's children
 * as well.  Use with caution.
 * @param {string} sType optional type of listener to purge. If
 * left out, all listeners will be removed
 * @static
 */
Event.prototype.purgeElement = function(el, recurse, sType) {
  var oEl = (eXo.social.profile.Type.isString(el)) ? document.getElementById(el) : el;
  var elListeners = this.getListeners(oEl, sType), i, len;
  if (elListeners) {
    for (i = elListeners.length - 1; i > -1; i--) {
      var l = elListeners[i];
      this.removeListener(oEl, l.type, l.fn);
    }
  }

  if (recurse && oEl && oEl.childNodes) {
    for (i = 0,len = oEl.childNodes.length; i < len; ++i) {
      this.purgeElement(oEl.childNodes[i], recurse, sType);
    }
  }
}

/**
 * Returns all listeners attached to the given element via addListener.
 * Optionally, you can specify a specific type of event to return.
 * @method getListeners
 * @param el {HTMLElement|string} the element or element id to inspect
 * @param sType {string} optional type of listener to return. If
 * left out, all listeners will be returned
 * @return {Object} the listener. Contains the following fields:
 * &nbsp;&nbsp;type:   (string)   the type of event
 * &nbsp;&nbsp;fn:     (function) the callback supplied to addListener
 * &nbsp;&nbsp;obj:    (object)   the custom object supplied to addListener
 * &nbsp;&nbsp;adjust: (boolean|object)  whether or not to adjust the default scope
 * &nbsp;&nbsp;scope: (boolean)  the derived scope based on the adjust parameter
 * &nbsp;&nbsp;index:  (int)      its position in the Event util listener cache
 * @static
 */
Event.prototype.getListeners = function(el, sType) {
  var results = [], searchLists;
  if (!sType) {
    searchLists = [this.listeners, this.unloadListeners];
  } else if (sType === "unload") {
    searchLists = [this.unloadListeners];
  } else {
    searchLists = [this.listeners];
  }

  var oEl = (eXo.social.profile.Type.isString(el)) ? document.getElementById(el) : el;

  for (var j = 0; j < searchLists.length; j = j + 1) {
    var searchList = searchLists[j];
    if (searchList) {
      for (var i = 0,len = searchList.length; i < len; ++i) {
        var l = searchList[i];
        if (l && l[this.EL] === oEl &&
            (!sType || sType === l[this.TYPE])) {
          results.push({
            type:   l[this.TYPE],
            fn:     l[this.FN],
            obj:    l[this.OBJ],
            adjust: l[this.OVERRIDE],
            scope:  l[this.ADJ_SCOPE],
            index:  i
          });
        }
      }
    }
  }

  return (results.length) ? results : null;
}


/**
 * Logic that determines when we should automatically use legacy
 * events instead of DOM2 events.  Currently this is limited to old
 * Safari browsers with a broken preventDefault
 * @method useLegacyEvent
 * @static
 * @private
 */
Event.prototype.useLegacyEvent = function(el, sType) {
  if (eXo.core.Browser.webkit && ("click" == sType || "dblclick" == sType)) {
    var v = parseInt(eXo.core.Browser.webkit, 10);
    if (!isNaN(v) && v < 418) {
      return true;
    }
  }
  return false;
}


/**
 * Finds the event in the window object, the caller's arguments, or
 * in the arguments of another method in the callstack.  This is
 * executed automatically for events registered through the event
 * manager, so the implementer should not normally need to execute
 * this function at all.
 * @method getEvent
 * @param {Event} e the event parameter from the handler
 * @param {HTMLElement} boundEl the element the listener is attached to
 * @return {Event} the event
 * @static
 */
Event.prototype.getEvent = function(e, boundEl) {
  var ev = e || window.event;

  if (!ev) {
    var c = this.getEvent.caller;
    while (c) {
      ev = c.arguments[0];
      if (ev && Event == ev.constructor) {
        break;
      }
      c = c.caller;
    }
  }

  return ev;
}

/**
 * Returns the legacy event index that matches the supplied
 * signature
 * @method getLegacyIndex
 * @static
 * @private
 */
Event.prototype.getLegacyIndex = function(el, sType) {
  var key = this.generateId(el) + sType;
  if (typeof this.legacyMap[key] == "undefined") {
    return -1;
  } else {
    return this.legacyMap[key];
  }
}


/**
 * Generates an unique ID for the element if it does not already
 * have one.
 * @method generateId
 * @param el the element to create the id for
 * @return {string} the resulting id of the element
 * @static
 */
Event.prototype.generateId = function(el) {
  var id = el.id;

  if (!id) {
    id = "exoevtautoid-" + this.counter;
    this.counter += 1;
    el.id = id;
  }

  return id;
}

/**
 * When using legacy events, the handler is routed to this object
 * so we can fire our custom listener stack.
 * @method fireLegacyEvent
 * @static
 * @private
 */
Event.prototype.fireLegacyEvent = function(e, legacyIndex) {
  // this.logger.debug("fireLegacyEvent " + legacyIndex);
  var ok = true, le, lh, li, scope, ret;

  lh = this.legacyHandlers[legacyIndex].slice();
  for (var i = 0, len = lh.length; i < len; ++i) {
    li = lh[i];
    if (li && li[this.WFN]) {
      scope = li[this.ADJ_SCOPE];
      ret = li[this.WFN].call(scope, e);
      ok = (ok && ret);
    }
  }

  // Fire the original handler if we replaced one.  We fire this
  // after the other events to keep stopPropagation/preventDefault
  // that happened in the DOM0 handler from touching our DOM2
  // substitute
  le = this.legacyEvents[legacyIndex];
  if (le && le[2]) {
    le[2](e);
  }

  return ok;
}

/**
 * Adds a DOM event directly without the caching, cleanup, scope adj, etc
 *
 * @method _simpleAdd
 * @param {HTMLElement} el      the element to bind the handler to
 * @param {string}      sType   the type of event handler
 * @param {function}    fn      the callback to invoke
 * @param {boolean}      capture capture or bubble phase
 * @static
 * @private
 */
Event.prototype._simpleAdd = function () {
  if (window.addEventListener) {
    return function(el, sType, fn, capture) {
      el.addEventListener(sType, fn, (capture));
    };
  } else if (window.attachEvent) {
    return function(el, sType, fn, capture) {
      el.attachEvent("on" + sType, fn);
    };
  } else {
    return function() {
    };
  }
}()

/**
 * Basic remove listener
 *
 * @method _simpleRemove
 * @param {HTMLElement} el      the element to bind the handler to
 * @param {string}      sType   the type of event handler
 * @param {function}    fn      the callback to invoke
 * @param {boolen}      capture capture or bubble phase
 * @static
 * @private
 */
Event.prototype._simpleRemove = function() {
  if (window.removeEventListener) {
    return function (el, sType, fn, capture) {
      el.removeEventListener(sType, fn, (capture));
    };
  } else if (window.detachEvent) {
    return function (el, sType, fn) {
      el.detachEvent("on" + sType, fn);
    };
  } else {
    return function() {
    };
  }
}()


/**
 * Locating the saved event handler data by function ref
 *
 * @method _getCacheIndex
 * @static
 * @private
 */
Event.prototype._getCacheIndex = function(el, sType, fn) {
  for (var i = 0, l = this.listeners.length; i < l; i = i + 1) {
    var li = this.listeners[i];
    if (li &&
        li[this.FN] == fn &&
        li[this.EL] == el &&
        li[this.TYPE] == sType) {
      return i;
    }
  }

  return -1;
}


eXo.social.profile.Event = new Event();