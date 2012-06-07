SOC-2674: Navigation nodes without pages are displayed in My Spaces Menu and in UISpaceMenu

Problem description
* What is the problem to fix?
Navigation nodes without pages shouldn't be displayed in My Spaces Menu and in UISpaceMenu

Fix description
* Problem analysis

* How is the problem fixed?
Base on UserNode#getPageRef() value then node will be displayed or not, node hidden in case of null value.

Patch file: PROD-ID.patch

Tests to perform
* Reproduction test
- Create a new space
- Go to the space navigation portlet
- Click on Add new node button or Choose a parent node and Right click on one node and select Add new node
- Input valid values for required fields
- Choose Visible option
- Don't choose a page for new node
- Click Save
- Expected result :
Node without page should not be displayed in "My Spaces" navigation and in "UISpaceMenu"

Tests performed at DevLevel
...
Tests performed at Support Level
...
Tests performed at QA
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
