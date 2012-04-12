SOC-2614: CLONE - Problem with encode activities in Activity Stream gadget

Problem description
* What is the problem to fix?
- Log in as root to export all gadgets
- Log in as demo, go to dashboard, add Activity Stream gadget
- In canvas mode of Activity Stream gadget, post new unicode activity.
- Click like or unlike in gadget, everything ok.
- Go to Activity Stream portlet, the title of activity has been re-encoded => wrong.

Fix description
* Problem analysis
There is a synchronization problem in encoding between activity gadget and activity portlet. When post unicode activities in activity gadget everything is ok, but at activity portlet problem with encoding occurred since title is re-encoded. The root cause is when action 'like' activity is called then activity is updated ( ActivityManager#update(ExoSocialActivity) ) then the title is changed.

* How is the problem fixed?
To solve this problem, then process 'like' action separately, then when activity is liked then just like information is stored not update the main activity, use updateLike/deleteLike ( ActivityManager#saveLike(ExoSocialActivity, Identity) ) to process these informations.

Patch file: https://github.com/exoplatform/social/pull/23

Tests to perform
* Reproduction test
- Log in as root to export all gadgets
- Log in as demo, go to dashboard, add Activity Stream gadget
- In canvas mode of Activity Stream gadget, post new unicode activity.
- Click like or unlike in gadget, everything ok.
- Go to Activity Stream portlet, the title of activity has been re-encoded => wrong.

* Tests performed at DevLevel
...
Tests performed at Support Level
...
Tests performed at QA
...

Changes in Test Referential
* Changes in SNIFF/FUNC/REG tests
...
* Changes in Selenium scripts 
...

Documentation changes
Documentation (User/Admin/Dev/Ref) changes: None

Configuration changes
Configuration changes:
* None

Will previous configuration continue to work?
* yes

Risks and impacts
Can this bug fix have any side effects on current client projects?

Function or ClassName change: 
Data (template, node type) migration/upgrade: 
Is there a performance risk/cost?
N/A

Validation (PM/Support/QA)
* PM Comment
PM validated
* Support Comment
Support validated
* QA Feedbacks
...
