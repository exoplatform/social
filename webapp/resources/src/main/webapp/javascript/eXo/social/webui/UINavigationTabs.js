/**
 * UINavigationTabs.js for processing many added tabs, for scrolling tabs elements.
 *
 * @Requires eXo#provide, eXo#extend (eXo-patch.js)
 * @Requires Array.indexOf (ie-patch.js)
 * @Requires eXo.core.DOMUtil, eXo.core.Browser
 * @Requires eXo.social.Util
 * @author <a href="hoatle.net">hoatlevan at gmail dot com</a>
 * @since Social 1.2.8
 */

(function(eXo) {
  var tabContainerId,
      //default configuration and can be overridden
      configuration = {
        displayedElements: 7,
        pageScrolled: false, //if true, when click next item, all items of next page will be displayed.
        mouseDownTimeoutDelay: 500, //mili-seconds
        mouseDownIntervalDelay: 200,
        messages: {
          previous: 'Previous',
          next: 'Next'
        }
      },
      Browser = eXo.core.Browser,
      DOMUtil = eXo.core.DOMUtil,
      Util = eXo.social.Util;

  var tabContainerEl,
      tabElements = [],
      previousButton,
      nextButton,
      calculatedDisplayElements = 3,
      buttonWidth = 0,
      selectedTabIndex = -1; //not yet calculated

  var UINavigationTabs = {
    tabContainerId: function(tabContainerId) {
      _setTabContainerId(tabContainerId);
      return this;
    },
    configure: function(configurationParams) {
      _setConfiguration(configurationParams);
      return this;
    },
    previousItems: function() {
      _previousItems();
    },
    nextItems: function() {
      _nextItems();
    },
    _: { //internal stuff
      previousMouseDownTimeout: null,
      previousMouseDownInterval: null,
      nextMouseDownTimeout: null,
      nextMouseDownInterval: null
    }
  };

  function _moveToPreviousItems(numberOfItems) {
    if (!_hasHiddenTabs() || configuration.pageScrolled) {
      return;
    }
    // firstIndex - firstVisibleIndex - lastVisibleIndex - lastIndex
    // => firstIndex - (firstVisibleIndex - numberOfItems) - (lastVisibleIndex - numberOfItems) - lastIndex
    var visibleTabs = _getVisibleTabs();
    var firstVisibleIndex = _getUITabIndex(visibleTabs[0]);
    var lastVisibleIndex = _getUITabIndex(visibleTabs[visibleTabs.length -1]);

    var newFirstVisibleIndex = firstVisibleIndex - numberOfItems;
    if (newFirstVisibleIndex < 0) {
      newFirstVisibleIndex = 0;
    }
    var newLastVisibleIndex = lastVisibleIndex - numberOfItems;

    if (0 < newFirstVisibleIndex) {
      _hideRange(0, newFirstVisibleIndex - 1);
    }
    _showRange(newFirstVisibleIndex, newLastVisibleIndex);

    if (newLastVisibleIndex < tabElements.length - 1) {
      _hideRange(newLastVisibleIndex + 1, tabElements.length - 1);
    }
    _initNavigationButtons();
  }

  function _moveToNextItems(numberOfItems) {
    if (!_hasHiddenTabs() || configuration.pageScrolled) {
      return;
    }
    // firstIndex - firstVisibleIndex - lastVisibleIndex - lastIndex
    // => firstIndex - (firstVisibleIndex + numberOfItems) - (lastVisibleIndex + numberOfItems) - lastIndex
    var visibleTabs = _getVisibleTabs();
    var firstVisibleIndex = _getUITabIndex(visibleTabs[0]);
    var lastVisibleIndex = _getUITabIndex(visibleTabs[visibleTabs.length -1]);

    var newFirstVisibleIndex = firstVisibleIndex + numberOfItems;
    if (newFirstVisibleIndex > tabElements.length - 1) {
      newFirstVisibleIndex = tabElements.length - 1;
    }
    var newLastVisibleIndex = lastVisibleIndex + numberOfItems;

    if (newLastVisibleIndex > tabElements.length - 1) {
      newLastVisibleIndex = tabElements.length - 1;
    }

    if (0 < newFirstVisibleIndex) {
      _hideRange(0, newFirstVisibleIndex - 1);
    }
    _showRange(newFirstVisibleIndex, newLastVisibleIndex);

    _hideRange(newLastVisibleIndex + 1, tabElements.length - 1);

    _initNavigationButtons();
  }

  function alerta(sms) {
    document.getElementById('composerInput').value += sms;
  }

  function _hideRange(firstIndex, lastIndex) {
    for (var i = ((firstIndex < 0) ? 0 : firstIndex); i <= lastIndex && i < tabElements.length; i++) {
      tabElements[i].style.display = 'none';
      DOMUtil.removeClass(tabElements[i], 'UITab');
      DOMUtil.addClass(tabElements[i], 'HiddenUITab');
    }
  }

  function _showRange(firstIndex, lastIndex) {
    for (var i = ((firstIndex < 0) ? 0 : firstIndex); i <= lastIndex && i < tabElements.length; i++) {
      tabElements[i].style.display = '';
      DOMUtil.removeClass(tabElements[i], 'HiddenUITab');
      DOMUtil.addClass(tabElements[i], 'UITab');
    }
  }

  function _scrollPreviousItems() {
    if (!(_hasHiddenTabs() && configuration.pageScrolled)) {
      return;
    }
    var visibleTabs = _getVisibleTabs();
    var firstVisibleIndex = _getUITabIndex(visibleTabs[0]);
    var lastVisibleIndex = _getUITabIndex(visibleTabs[visibleTabs.length -1]);

    _hideRange(firstVisibleIndex, lastVisibleIndex);

    var previousVisibleIndex = firstVisibleIndex - configuration.displayedElements;
    if (previousVisibleIndex < 0) {
      previousVisibleIndex = 0;
    }
    var nextVisibleIndex = previousVisibleIndex + configuration.displayedElements - 1;

    _showRange(previousVisibleIndex, nextVisibleIndex);

    _initNavigationButtons();
  }


  function _scrollNextItems() {
    if (!(_hasHiddenTabs() && configuration.pageScrolled)) {
      return;
    }
    var visibleTabs = _getVisibleTabs();
    var firstVisibleIndex = _getUITabIndex(visibleTabs[0]);
    var lastVisibleIndex = _getUITabIndex(visibleTabs[visibleTabs.length -1]);

    _hideRange(firstVisibleIndex, lastVisibleIndex);
    var maxNumberOfNextVisibleIndex = lastVisibleIndex + configuration.displayedElements;

    if (maxNumberOfNextVisibleIndex > (tabElements.length - 1)) {
      maxNumberOfNextVisibleIndex = tabElements.length - 1;
    }

    _showRange(lastVisibleIndex + 1, maxNumberOfNextVisibleIndex);

    _initNavigationButtons();
  }


  function _init() {
    if (!_isValidConfiguration()) {
      return;
    }
    _setUISubTabHeight();
    _showAndHideItems();
    _initNavigationButtons();
  }

  function _setUISubTabHeight() {
    //when > 13 items
    var uiSubTab = document.getElementById('UISubTabId');
    if (uiSubTab && tabElements.length > 13) {
      if (!DOMUtil.hasClass(uiSubTab, 'UISubTabFixedHeight')) {
        DOMUtil.addClass(uiSubTab, 'UISubTabFixedHeight');
      }
    }
  }

  function _showAndHideItems() {
    tabContainerEl.style.width = (tabContainerEl.parentNode.offsetWidth - 30) + "px";
    var containerWidth = tabContainerEl.offsetWidth;
    //reset
    calculatedDisplayElements = 3;
    var uiTabWidth = _getVisibleTabs()[0].clientWidth; //check IE?
    var delta = 30 + buttonWidth;
    var numberTabs = tabElements.length;
    //min container's width
    if (containerWidth < 500) {
      calculatedDisplayElements = 3;
    } else {
      calculatedDisplayElements = Math.floor((containerWidth - delta) / uiTabWidth);
    }

    calculatedDisplayElements = (calculatedDisplayElements > configuration.displayedElements) ? 
                                  configuration.displayedElements : calculatedDisplayElements;

    calculatedDisplayElements = (calculatedDisplayElements > numberTabs) ? 
                                  numberTabs : calculatedDisplayElements;

    if(_showAndHideButton(calculatedDisplayElements === numberTabs)) {
      delta = 30;
    }
    
    var numberTabsDisplay = 3;
    if (configuration.pageScrolled) {
      var scrollPage = _getScrollPage();
      var firstVisibleIndex = (scrollPage - 1) * calculatedDisplayElements;
      var lastVisibleIndex = firstVisibleIndex + calculatedDisplayElements - 1;
      if (lastVisibleIndex > numberTabs - 1) {
        lastVisibleIndex = numberTabs - 1;
      }
      //hide previous range
      if (firstVisibleIndex > 0) {
        _hideRange(0, firstVisibleIndex - 1);
      }
      //show current scroll page
      _showRange(firstVisibleIndex, lastVisibleIndex);
      numberTabsDisplay = lastVisibleIndex - firstVisibleIndex;

      //hide next range
      if (lastVisibleIndex < numberTabs - 1) {
        _hideRange(lastVisibleIndex + 1, numberTabs -1);
      }
    } else {
      selectedTabIndex = _getSelectedTabIndex();

      if ((selectedTabIndex + calculatedDisplayElements) > (numberTabs - 1)) {
        selectedTabIndex = numberTabs - calculatedDisplayElements;
      }

      //display range: firstIndex - [selectedTabIndex - (selectedTabIndex + calculatedDisplayElements)] - lastIndex
      var lastShownTabIndex = selectedTabIndex + calculatedDisplayElements - 1;

      if (lastShownTabIndex > numberTabs - 1) {
        lastShownTabIndex = numberTabs - 1;
      }

      if (selectedTabIndex > 0) {
        _hideRange(0, selectedTabIndex - 1);
      }
      _showRange(selectedTabIndex, lastShownTabIndex);
      _hideRange(lastShownTabIndex + 1, numberTabs - 1);
      numberTabsDisplay = lastShownTabIndex - selectedTabIndex;
    }
    if(numberTabsDisplay > 0) {
	    tabContainerEl.style.width = ((numberTabsDisplay+1) * uiTabWidth + delta - 22) + "px";
    }
  }

  function _showAndHideButton(isDisable) {
    var allButton = DOMUtil.findFirstDescendantByClass(tabContainerEl, 'div', 'AllButton');
    if(allButton) {
      if(isDisable) {
        allButton.style.display = "none";
      } else {
        allButton.style.display = "block";
      }
    }
    return isDisable;
  }

  function _initNavigationButtons() {
    if (previousButton) {
      DOMUtil.removeElement(previousButton);
    }
    previousButton = document.createElement('a');
    previousButton.setAttribute('class', 'PrevButton IconTab');
    previousButton.setAttribute('className', 'PrevButton IconTab');
    previousButton.setAttribute('title', configuration.messages.previous);
    previousButton.innerHTML = configuration.messages.previous;
    tabContainerEl.insertBefore(previousButton, tabElements[0]);

    var visibleTabs = _getVisibleTabs();
    var firstVisiblePosition = Util.findPosition(visibleTabs[0]);
    var lastVisiblePosition = Util.findPosition(visibleTabs[visibleTabs.length - 1]);
    var rightLastVisiblePositionX = lastVisiblePosition['x'] + tabElements[0].innerWidth;

    Util.addEventListener(previousButton, 'click', function() {
      _previousItems();
    }, false);

    Util.addEventListener(previousButton, ['mousedown', 'keydown'], function() {
      UINavigationTabs._.previousMouseDownTimeout = setTimeout(function() {
          UINavigationTabs._.previousMouseDownInterval = setInterval(function() {
          if (_previousButtonShown()) {
            _previousItems();
          } else {
            clearInterval(UINavigationTabs._.previousMouseDownInterval);
          }
        }, configuration.mouseDownIntervalDelay);
      }, configuration.mouseDownTimeoutDelay);

    }, false);

    Util.addEventListener(previousButton, ['mouseup', 'mouseout', 'keyup'], function() {
      if (UINavigationTabs._.previousMouseDownTimeout) {
        clearTimeout(UINavigationTabs._.previousMouseDownTimeout);
      }
      if (UINavigationTabs._.previousMouseDownInterval) {
        clearInterval(UINavigationTabs._.previousMouseDownInterval);
      }
    }, false);


    if (nextButton) {
      DOMUtil.removeElement(nextButton);
    }

    nextButton = document.createElement('a');

    nextButton.setAttribute('class', 'NextButton IconTab');
    nextButton.setAttribute('className', 'NextButton IconTab');
    nextButton.setAttribute('title', configuration.messages.next);
    nextButton.innerHTML = configuration.messages.next;
    Util.insertAfter(nextButton, tabElements[tabElements.length - 1]);

    Util.addEventListener(nextButton, 'click', function() {
      _nextItems();
    }, false);

   Util.addEventListener(nextButton, ['mousedown', 'keydown'], function() {
      UINavigationTabs._.nextMouseDownTimeout = setTimeout(function() {
          UINavigationTabs._.nextMouseDownInterval = setInterval(function() {
          if (_nextButtonShown()) {
            _nextItems();
          } else {
            clearInterval(UINavigationTabs._.nextMouseDownInterval);
          }
        }, configuration.mouseDownIntervalDelay);
      }, configuration.mouseDownTimeoutDelay);

    }, false);

    Util.addEventListener(nextButton, ['mouseup', 'mouseout', 'keyup'], function() {
      if (UINavigationTabs._.nextMouseDownTimeout) {
        clearTimeout(UINavigationTabs._.nextMouseDownTimeout);
      }
      if (UINavigationTabs._.nextMouseDownInterval) {
        clearInterval(UINavigationTabs._.nextMouseDownInterval);
      }
    }, false);

    if (!_previousButtonShown()) {
      previousButton.style.display = 'none';
    } else {
      previousButton.style.display = '';
      previousButton.style.position = 'absolute';
      previousButton.style.left = '5px';
    }

    if (!_nextButtonShown()) {
      nextButton.style.display = 'none';
    } else {
      nextButton.style.display = '';
      nextButton.style.position = 'absolute';
      nextButton.style.right = (buttonWidth + 10) + 'px';
    }
  }

  function _previousItems() {
    if (configuration.pageScrolled) {
      _scrollPreviousItems();
    } else {
      _moveToPreviousItems(1);
    }
  }

  function _nextItems() {
    if (configuration.pageScrolled) {
      _scrollNextItems();
    } else {
      _moveToNextItems(1);
    }
  }

  function _previousButtonShown() {
    if (!_hasHiddenTabs()) {
      return false;
    }
    var hiddenTabEls = _getHiddenTabs();
    var visibleTabEls = _getVisibleTabs();
    var firstHiddenIndex = _getUITabIndex(hiddenTabEls[0]);
    var firstVisibleIndex = _getUITabIndex(visibleTabEls[0]);
    return firstHiddenIndex < firstVisibleIndex;
  }

  function _nextButtonShown() {
    if (!_hasHiddenTabs()) {
      return false;
    }
    var hiddenTabEls = _getHiddenTabs();
    var visibleTabEls = _getVisibleTabs();
    var lastHiddenIndex = _getUITabIndex(hiddenTabEls[hiddenTabEls.length - 1]);
    var lastVisibleIndex = _getUITabIndex(visibleTabEls[visibleTabEls.length - 1]);

    return (lastHiddenIndex > lastVisibleIndex);
  }

  function _getScrollPage() {
    var totalNumberOfPages = Math.floor(tabElements.length / configuration.displayedElements);
    if (totalNumberOfPages < (tabElements.length / configuration.displayedElements)) {
      totalNumberOfPages = totalNumberOfPages + 1;
    }
    var scrollPage;
    for (var i = 0; i < totalNumberOfPages; i++) {
      var firstIndexOfPage = i * configuration.displayedElements;
      var lastIndexOfPage = firstIndexOfPage + configuration.displayedElements - 1;
      if (firstIndexOfPage <= _getSelectedTabIndex() && _getSelectedTabIndex() <= lastIndexOfPage) {
        scrollPage = i + 1;
        break;
      }
    }
    return scrollPage;
  }


  function _getSelectedTabIndex() {
    if (selectedTabIndex == -1) {
      var selectedTabEl = DOMUtil.findDescendantsByClass(tabContainerEl, 'div', 'SelectedTab');
      if (selectedTabEl.length != 0) {
        selectedTabEl = selectedTabEl[0].parentNode;
        selectedTabIndex = _getUITabIndex(selectedTabEl);
      }
    }
    //not found selectedTabIndex, imply that it's the first one.
    if (selectedTabIndex < 0) {
      selectedTabIndex = 0;
    }
    return selectedTabIndex;
  }

  function _getUITabIndex(uiTab) {
    return tabElements.indexOf(uiTab);
  }

  function _setTabContainerId(setTabContainerId) {
    tabContainerId = setTabContainerId;
    tabContainerEl = DOMUtil.get(tabContainerId);
    if (tabContainerEl) {
      tabElements = DOMUtil.findChildrenByClass(tabContainerEl, 'div', 'NormalTabStyle');
    }
    var allButton = DOMUtil.findFirstDescendantByClass(tabContainerEl, 'div', 'AllButton');
    if(allButton) {
      buttonWidth = allButton.offsetWidth;
    }
  }

  function extend(targetObj, obj) {
    var src, copy;
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
  
  function _setConfiguration(configurationParams) {
    configuration = extend(configuration, configurationParams);
    setTimeout(_init, 30);
    Browser.addOnResizeCallback('UINavigationTabs', _init);
  }

  function _isValidConfiguration() {
    if (!tabContainerEl) {
      return false;
    }
    return true;
  }

  function _hasHiddenTabs() {
    var els = _getHiddenTabs();
    return els.length > 0;
  }

  function _getHiddenTabs() {
    return DOMUtil.findChildrenByClass(tabContainerEl, 'div', 'HiddenUITab');
  }

  function _getVisibleTabs() {
    return DOMUtil.findChildrenByClass(tabContainerEl, 'div', 'UITab');
  }

  window.onresize = function() {
    eXo.core.Browser.managerResize();
    if(this.currWidth == undefined || this.currWidth != document.documentElement.clientWidth) {
      _init();
    }
    this.currWidth  = document.documentElement.clientWidth;
  };

  //expose
  eXo.social = eXo.social || {};
  eXo.social.webui = eXo.social.webui || {};
  eXo.social.webui.UINavigationTabs = UINavigationTabs;
})(eXo);
