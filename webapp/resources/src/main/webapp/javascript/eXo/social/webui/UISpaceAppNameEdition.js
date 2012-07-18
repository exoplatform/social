/**
 * Copyright (C) 2009 eXo Platform SAS.
 * 
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 * 
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

eXo.social.webui.UISpaceAppNameEdition = {

  renameAppLabel : function(input) {
    var newLabel = input.val();
    if (newLabel && newLabel.length > 0) {
      var portletID = input.closest(".PORTLET-FRAGMENT").parent().attr("id");

      var href = eXo.env.server.portalBaseURL + "?portal:componentId=" + portletID;
      href += "&portal:type=action";
      href += "&portal:isSecure=false";
      href += "&uicomponent=UISpaceMenu";
      href += "&op=RenameSpaceAppName";
      href += "&newSpaceAppName=" + encodeURIComponent(newLabel);
      window.location = href;
    }
  },

  showEditLabelInput : function(target, nodeName, currentLabel) {
    var jqObj = gj(target);

    var input = gj("<input>").attr({type : "text", id : nodeName, name : currentLabel, value : currentLabel, maxLength : 50});
    input.css("border", "1px solid #b7b7b7").css("width", (target.offsetWidth - 2) + "px");

    jqObj = jqObj.replaceWith(input);
    input.blur(function() {
      gj(this).replaceWith(jqObj);
    });

    input.keypress(function(e) {
      var keyNum = e.keyCode ? e.keyCode : e.which;
      if (keyNum == 13) {
        eXo.social.webui.UISpaceAppNameEdition.renameAppLabel(gj(this));
      } else if (keyNum == 27) {
        gj(this).replaceWith(jqObj);
      }
    });

    input.closest(".UITab").addClass("EditTab");
    input.focus();
  }
};