SOC-2485: Added status shared with an attached link is not shown on the Activity stream gadget

What is the problem to fix?
- Added status shared with an attached link is not shown on the Activity stream gadget.
- Link activity is recognized as default activity.
- Link activity display information in Activity stream gadget and Activity stream portlet is not synchronized.

Problem analysis
- The link activity which is posted from Activity stream gadget always recognized as default activity.
- Link activity display information in Activity stream gadget and Activity stream portlet is not synchronized.

How is the problem fixed?
- Build appropriate activity based on activity type and externalId in UIActivityFactory#addChild(ExoSocialActivity, UIContainer).
- Make link activity display information in Activity stream gadget and Activity stream portlet synchronized.

Reproduction test
- Login by root
- Choose Dashboard on UserToolbarDashboard portlet
- Click Add Gadget link
- Drag and drop the "Activity Stream" Gadget
- Go on the maximized mode
- Write something on the "what are you working on field" and attache a link with it, then share
- The shared status and shared link should be shown on the Activity Stream gadget.
- Only the shared link is shown on the activity stream gadget

Tests performed at DevLevel
Steps to check on social-standalone:
- Login by root
- Choose Dashboard on UserToolbarDashboard portlet
- Click Add Gadget link
- Drag and drop the "Activity Stream" Gadget
- Go on the maximized mode
- Write something on the "what are you working on field" and attach a link, then click share button
