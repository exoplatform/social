SOC-2755: Text when Space/Forum hasn't description isn't localized

Problem description
* What is the problem to fix?
When a forum have no description, we can see an english message with french selected :
"(No description)"

Fix description
* Problem analysis
Because the key UISpaceAddForm.msg.default_space_description is added by default. So when a forum have no description, we can see an english message :
"(No description)"
And this key isn't translated in french. 
* How is the problem fixed?
Remove key UISpaceAddForm.msg.default_space_description.

Patch file: https://github.com/exoplatform/social/pull/87

Tests to perform
* Reproduction test
  Create a new forum with no description.
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
  Support validated
QA Feedbacks
...
