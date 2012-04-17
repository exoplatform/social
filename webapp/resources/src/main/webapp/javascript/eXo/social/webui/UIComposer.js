/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */

/**
 * UIComposer.js
 *
 */


(function () {
  var window_ = this;

  function UIComposer(params) {
    this.configure(params);
    this.init();
  }

  function handleShareButtonState(uiComposer) {
    if (uiComposer.minCharactersRequired !== 0) {
      //TODO hoatle handle backspace problem
      if (uiComposer.composer.val().length >= uiComposer.minCharactersRequired) {
        uiComposer.shareButton.className = 'ShareButtonDisable';
        if(gj("#ComposerContainer") == null){
          uiComposer.shareButton.removeAttr("disabled");
          uiComposer.shareButton.attr("class",'ShareButton');
        }
      } else {
        uiComposer.shareButton.css(background, '');
        uiComposer.shareButton.attr('class','ShareButton');
      }
    } else {
      if(document.getElementById("ComposerContainer") == null){
        uiComposer.shareButton.removeAttr("disabled");
        uiComposer.shareButton.attr("class",'ShareButton');
      }
    }

    if (uiComposer.maxCharactersAllowed !== 0) {
      if (uiComposer.composer.val().length >= uiComposer.maxCharactersAllowed) {
        //substitue it
        //TODO hoatle have a countdown displayed on the form
        uiComposer.composer.val( uiComposer.composer.val().substring(0, uiComposer.maxCharactersAllowed));
      }
    }
  }

  UIComposer.prototype.configure = function(params) {
    this.composerId = params.composerId || 'composerInput';
    this.defaultInput = params.defaultInput || "";
    this.minCharactersRequired = params.minCharactersRequired || 0;
    this.maxCharactersAllowed = params.maxCharactersAllowed || 0;
    this.focusColor = params.focusColor || '#000000';
    this.blurColor = params.blurColor || '#777777';
    this.minHeight = params.minHeight || '20px';
    this.focusHeight = params.focusHeight || '35px';
    this.maxHeight = params.maxHeight || '50px';
    this.padding = params.padding || '11px 0 11px 8px';
    this.focusCallback = params.focusCallback;
    this.blurCallback = params.blurCallback;
    this.keypressCallback = params.keypressCallback;
    this.postMessageCallback = params.postMessageCallback;
    this.userTyped = false;
  }


  UIComposer.prototype.init = function() {
    this.composer = gj('#' + this.composerId);
    this.shareButton = gj('#ShareButton');
    if (!(this.composer && this.shareButton)) {
      alert('error: can not find composer or shareButton!');
    }

    this.composer.val(this.defaultInput);
    this.composer.css({'height':this.minHeight,
      'color':this.blurColor,
      'padding':this.padding
    });
    this.shareButton.attr('class','ShareButtonDisable');
    this.shareButton.attr('disabled',"disabled");
    this.currentValue = this.composer.val();
    var uiComposer = this;
    var isReadyEl = gj("#isReadyId");
    var composerContainerEl = gj("#ComposerContainer");
    var isReadyVal;
    this.composer.on('focus', function() {
      handleShareButtonState(uiComposer);
      if (uiComposer.composer.val() === uiComposer.defaultInput) {
        uiComposer.composer.val('') ;
      }
      if (uiComposer.focusCallback) {
        uiComposer.focusCallback();
      }
      uiComposer.composer.css({'height' : uiComposer.maxHeight,
        'color'  : uiComposer.focusColor});
    });


    this.composer.on('blur', function() {
      if (uiComposer.composer.val() === '') {
        uiComposer.composer.val(uiComposer.defaultInput);
        uiComposer.composer.css({'height' : uiComposer.minHeight,
                                 'color'  : uiComposer.blurColor});

        //if current composer is default composer then disable share button
        if(gj("#ComposerContainer") == null){
          uiComposer.shareButton.attr('disabled',"disabled");
          uiComposer.shareButton.attr('class','ShareButtonDisable');
        }

      } else {
        uiComposer.currentValue = uiComposer.composer.val();
      }

      if (uiComposer.blurCallback) {
        uiComposer.blurCallback();
      }
    });

    this.composer.on('keypress', handleShareButtonState(uiComposer));
  }

    UIComposer.prototype.getValue = function() {
      if (!this.currentValue) {
        return this.defaultInput;
      }
      return this.currentValue;
    }

    UIComposer.prototype.setCurrentValue = function() {
      var uiInputText = gj("#" + this.composerId);
      this.currentValue = uiInputText.val();
    }

    //expose
    window_.eXo.social.webui.UIComposer = UIComposer;
  })();
