SOC-2752: Improve the speed in space creation

Problem description
* What is the problem to fix?
Space Injector is very slow in Platform 3.5.4

Fix description
* Problem analysis
When creating space, with admin user, using UserPortal to get UserNavigation with UserContext, then all navigations will be loaded => performance problem.

* How is the problem fixed?
- Avoid using UserPortal, instead of NavigationService to get NavigationContext.
- Create UserNavigation instance, but this one only provide private constructor, so that workaround to use reflection.
- Prevent for changing internal UserNavigation class, adds UT to verify this.

Patch file: https://github.com/exoplatform/social/pull/86

Tests to perform
* Reproduction test
steps ...
* Tests performed at DevLevel
...
* Tests performed at Support Level
...
* Tests performed at QA
...

Changes in Test Referential
Changes in SNIFF/FUNC/REG tests
...
Changes in Selenium scripts 
...

Documentation changes
Documentation (User/Admin/Dev/Ref) changes:


Configuration changes
Configuration changes:
*

Will previous configuration continue to work?
*

Risks and impacts
Can this bug fix have any side effects on current client projects?

Function or ClassName change: 
Data (template, node type) migration/upgrade: 
Is there a performance risk/cost?
...
Validation (PM/Support/QA)
PM Comment
  PM validated
Support Comment
...
QA Feedbacks
...
