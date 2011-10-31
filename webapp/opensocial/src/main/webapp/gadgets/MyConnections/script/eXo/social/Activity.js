(function {
	var window_ = this;
	
	function Activity(params) {
    this.init(params);

    //Getters, Setters
    Activity.prototype.getType = function() {
      return this.type;
    };

    Activity.prototype.setType = function(activityType) {
      this.type = activityType;
    };

    Activity.prototype.getContent = function() {
      return this.content;
    };

    Activity.prototype.setContent = function(activityContent) {
      this.content = activityContent;
    };

    Activity.prototype.getDisplayName = function() {
      return this.displayName;
    };

    Activity.prototype.setDisplayName = function(userDisplayName) {
      this.displayName = userDisplayName;
    };

    Activity.prototype.getProfileUrl = function() {
      return this.profileUrl;
    };

    Activity.prototype.setProfileUrl = function(userProfileUrl) {
      this.profileUrl = userProfileUrl;
    };

    Activity.prototype.getAvatarUrl = function() {
      return this.avatarUrl;
    };

    Activity.prototype.setAvatarUrl = function(userAvatarUrl) {
      this.avatarUrl = userAvatarUrl;
    };

    Activity.prototype.getPostedTime = function() {
      return this.postedTime;
    };

    Activity.prototype.setPostedTime = function(activityPostedTime) {
      this.postedTime = activityPostedTime;
    };

    Activity.prototype.getPrettyTime = function() {
      return this.prettyTime;
    };

    Activity.prototype.setPrettyTime = function(activityPrettyTime) {
      this.prettyTime = activityPrettyTime;
    };
  }

  Activity.prototype.init = function(params) {
    this.type = params.type;
    this.content = params.content;
    this.displayName = params.displayName;
    this.profileUrl = params.profileUrl;
    this.avatarUrl = params.avatarUrl;
    this.postedTime = params.postedTime;
    this.prettyTime = getPrettyTime(params.postedTime);
  };

  /**
   * Gets pretty time from timestamp.
   *
   * @param postedTimeStamp
   */
  function getPrettyTime(postedTimeStamp) {
    return Util.getPrettyTime(new Date(postedTimeStamp));
  };

	//name space
	window_.exo = window_.exo || {};
	window_.exo.social = window_.exo.social;
	window_.exo.social.Activity = Activity;
})();