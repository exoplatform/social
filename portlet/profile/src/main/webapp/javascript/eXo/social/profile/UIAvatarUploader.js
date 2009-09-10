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
 * @author <a href="mailto:hoatlevan@gmail.com">hoatle</a>
 */
eXo = window.eXo || {};
eXo.social = eXo.social || {};
eXo.social.profile = eXo.social.profile || {};

function UIAvatarUploader() {
    function getLogger() {
      if (typeof log == 'undefined') {
        //TODO hoatle: Should create a similar logger as firebug logger
        return null;
      } else {
        return log;
      }
    }
    
    this.avatarChangeLink = document.getElementById('AvatarChangeLink')
    if (this.avatarChangeLink == null) {
        var log = getLogger();
        if (log) {
            log.warn('AvatarChangeLink id not found!');
        }
    }
};

UIAvatarUploader.prototype.showAvatarChangeLink = function() {
    //alert(this);
    var domUtil = eXo.core.DOMUtil;
    domUtil.removeClass(this.changeAvatarLink, 'HiddenAvatarChangeLink');
    domUtil.addClass(this.changeAvatarLink, 'ShowedAvatarChangeLink');

}

UIAvatarUploader.prototype.hideAvatarChangeLink = function() {
    alert(this.avatarChangeLink);
    var domUtil = eXo.core.DOMUtil;
    domUtil.removeClass(this.avatarChangeLink, 'ShowedAvatarChangeLink');
    domUtil.addClass(this.avatarChangeLink, 'HiddenAvatarChangeLink');
}

eXo.social.profile.UIAvatarUploader = new UIAvatarUploader();