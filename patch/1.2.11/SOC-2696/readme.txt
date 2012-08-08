SOC-2696: Path to intranet site is harcoded in My Connections gadget

Problem description
* What is the problem to fix?
Path to intranet site is harcoded in some js.
We should dynamically get the current site.

Fix description
* Problem analysis
Path to intranet site is harcoded in some js.

* How is the problem fixed?
Dynamically get the current site.

Patch file: PROD-ID.patch

Tests to perform
* Reproduction test
- Go to ACME. Login as John
- Go to Settings > Portal > Sites
- Delete intranet site
- Create new intranet site, called "mysite" for example
- Go to "mysite" site
- Edit > Page > Add Page: add new page "test"
- Add Application Registry into new page "test"
- Click Import Applications
- Go to Dashboard
- Add My Connections gadget
- See that the link of People Directory is correct (not hard code to intranet) 
Correct link: http://localhost:8080/portal/mysite/people
Wrong link: http://localhost:8080/portal/intranet/people

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
	Support validated
QA Feedbacks
...
