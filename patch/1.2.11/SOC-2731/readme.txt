SOC-2731: Still show application which was deleted in navigation of space 

Problem description
* What is the problem to fix?
After delete node in navigation, application still appears in Application tab.

Fix description
* Problem analysis
The data of Space object in UISpaceApplication is not updated when delete node in navigation.

* How is the problem fixed?
Call method UISpaceApplication#setValue(Space) in UISpaceApplication#getApplications() to always get the newest data of space.

Git Pull Request: https://github.com/exoplatform/social/pull/110

Tests to perform
* Reproduction test
- Select navigation of space
- Choose a node and delete it ( ex: Agenda)
- Select Application tab
Problem: Still show Agenda application in list ==> NOK 

* Tests performed at DevLevel
- Select navigation of space
- Choose a node and delete it ( ex: Agenda)
- Select Application tab
- Agenda is not shown in the application list.

Tests performed at Support Level
...
Tests performed at QA
...

Changes in Test Referential
Changes in SNIFF/FUNC/REG tests
No
Changes in Selenium scripts 
No

Documentation changes
Documentation (User/Admin/Dev/Ref) changes:
  No

Configuration changes
Configuration changes:
  No

Will previous configuration continue to work?
  Yes

Risks and impacts
Can this bug fix have any side effects on current client projects?

Function or ClassName change: No
Data (template, node type) migration/upgrade: No
Is there a performance risk/cost?
  No

Validation (PM/Support/QA)
PM Comment
...
Support Comment
...
QA Feedbacks
...
