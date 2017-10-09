(function($) { 
var UIProfile = {
  labels: {},
  KEYS : {
    ENTER : 13
  },
  init: function(params) {
    var positionId = params.positionId || null;
    var saveButtonId = params.saveButtonId || null;

    var positionEl = $("#" + positionId);
    var saveButtonEl = $("#" + saveButtonId);

    if (positionEl.length > 0 && saveButtonEl.length > 0) {
      positionEl.on('keydown', function(event) {
        if ((event.keyCode || event.which) == UIProfile.KEYS.ENTER) {
          saveButtonEl.click();
          event.preventDefault();
          return;
        }
      });
    }
  },
  initUserProfilePopup : function(uicomponentId, labels) {
    //
    UIProfile.labels = $.extend(true, {}, UIProfile.labels, labels);
    $.each(UIProfile.labels, function(key) {
      UIProfile.labels[key] =  window.decodeURIComponent(UIProfile.labels[key]);
    });
    
    // User Profile Popup initialize
    var portal = eXo.env.portal;
    var restUrl = '//' + window.location.host + portal.context + '/' + portal.rest + '/social/people' + '/getPeopleInfo/{0}.json';
    var container = $('#' + uicomponentId).closest('.PORTLET-FRAGMENT');
    var userLinks = $(container).find('a[href*="/profile/"]');
    $.each(userLinks, function (idx, el) {
        var userUrl = $(el).attr('href');
        var userId = userUrl.substring(userUrl.lastIndexOf('/') + 1);
        
        $(el).userPopup({
          restURL: restUrl,
          labels: UIProfile.labels,
          content: false,
          defaultPosition: "left",
          keepAlive: true,
          maxWidth: "240px"
        });
    });
  },
  initSpaceInfoPopup : function(uicomponentId, labels, defaultAvatarUrl) {
    //
    UIProfile.labels = $.extend(true, {}, UIProfile.labels, labels);
    $.each(UIProfile.labels, function(key) {
      UIProfile.labels[key] =  window.decodeURIComponent(UIProfile.labels[key]);
    });

    // User Profile Popup initialize
    var portal = eXo.env.portal;
    var spaceRestUrl = '//' + window.location.host + portal.context + '/' + portal.rest + '/v1/social/spaces/{0}';
    var membersRestUrl = '//' + window.location.host + portal.context + '/' + portal.rest + '/v1/social/spaces/{0}/users?returnSize=true';
    var managerRestUrl = '//' + window.location.host + portal.context + '/' + portal.rest + '/v1/social/spaces/{0}/users?role=manager&returnSize=true';
    var membershipRestUrl = '//' + window.location.host + portal.context + '/' + portal.rest + '/v1/social/spacesMemberships?space={0}&returnSize=true';
    var deleteMembershipRestUrl = '//' + window.location.host + portal.context + '/' + portal.rest + '/v1/social/spacesMemberships/{0}:{1}:{2}';

    var container = $('#' + uicomponentId).closest('.PORTLET-FRAGMENT');
    var spaceLinks = $(container).find('.space-avatar');
    var defaultAvatar =  decodeURIComponent(defaultAvatarUrl);
    $.each(spaceLinks, function (idx, el) {
      var spaceID = $(el).attr('space-data');
      $(el).spacePopup({
        userName : portal.userName,
        spaceID:spaceID,
        restURL: spaceRestUrl,
        membersRestURL: membersRestUrl,
        managerRestUrl: managerRestUrl,
        membershipRestUrl : membershipRestUrl,
        defaultAvatarUrl : defaultAvatar,
        deleteMembershipRestUrl : deleteMembershipRestUrl,
        labels: UIProfile.labels,
        content: false,
        defaultPosition: "left",
        keepAlive: true,
        maxWidth: "240px"
      });
    });
  },
  clearUserProfilePopup : function() {
    $('div#socialUsersData').stop().animate({
        'cursor':'none'
    }, 1000, function () {
        $(this).data("CacheSearch", {});
    });
  },
  clearSocialInfoPopup : function() {
    $('div#socialSpaceData').stop().animate({
      'cursor':'none'
    }, 1000, function () {
      $(this).data("CacheSearch", {});
    });
  },
  addLabelToCheckBoxes: function(uicomponentId, label) {
    var component = $('#' + uicomponentId);
    var checkBoxes = component.find('span.uiCheckbox');
    $.each(checkBoxes, function(idx, checkBox) {
      $(checkBox).find('span').text(label);
    });
  }
};

return UIProfile;
})($);
