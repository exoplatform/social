/**
 * The User model
 */

(function() {
	var window_ = this;

  /**
   * Class definition
   * 
   * @param params
   */
  function User(params) {
  	this.displayName = params.displayName;
  	this.avatarUrl = params.avatarUrl || "";
  	this.profileUrl = params.profileUrl;
  	this.activityTitle = params.activityTitle;
  	this.prettyPostedTime = params.prettyPostedTime;
  	
  	this.position = params.position || "";
  	this.activityId = params.activityId;
  }

  /**
   * Gets the display name.
   */
  User.prototype.getDisplayName = function() {
  	return this.displayName;
  };

  /**
   * Sets the display name
   * 
   * @param displayName
   */
  User.prototype.setDisplayName = function(displayName) {
  	this.displayName = displayName;
  };
  
  /**
   * Gets the avatar url
   */
  User.prototype.getAvatarUrl = function() {
  	return this.avatarUrl;
  };
  
  /**
   * Sets the avatar url.
   * 
   * @param avatarUrl
   */
  User.prototype.setAvatarUrl = function(avatarUrl) {
  	this.avatarUrl = avatarUrl;
  };
  
  /**
   * Gets the profile url.
   */
  User.prototype.getProfileUrl = function() {
  	return this.profileUrl;
  };

  /**
   * Sets the profile url.
   * 
   * @param profileUrl
   */
  User.prototype.setProfileUrl = function(profileUrl) {
  	this.profileUrl = profileUrl;
  };
  
  /**
   * Gets the position.
   */
  User.prototype.getPosition = function() {
  	return this.position;
  };
  
  /**
   * Sets the position.
   * 
   * @param position
   */
  User.prototype.setPosition = function(position) {
  	this.position = position;
  };
  
  /**
   * Gets the activity id.
   */
  User.prototype.getActivityId = function() {
  	return this.activityId;
  };
  
  /**
   * Sets the activity id.
   * 
   * @param activityId
   */
  User.prototype.setActivityId = function(activityId) {
  	this.activityId = activityId;
  };
  
  /**
   * Gets the activity title
   */
  User.prototype.getActivityTitle = function() {
  	return this.activityTitle;
  };
  
  /**
   * Sets the activity title.
   * 
   * @param activityTitle 
   */
  User.prototype.setActivityTitle = function(activityTitle) {
  	this.activityTitle = activityTitle;
  };

  /**
   * Gets the pretty posted time.
   */
  User.prototype.getPrettyPostedTime = function() {
  	return this.prettyPostedTime;
  }
  
  /**
   * Sets the pretty posted time.
   * 
   * @param prettyPostedTime
   */
  User.prototype.setPrettyPostedTime = function(prettyPostedTime) {
  	this.prettyPostedTime = prettyPostedTime;
  }
  
  window_.exo = window_.exo || {};
  window_.exo.social = window_.exo.social || {};
  window_.exo.social.User = User;
})();