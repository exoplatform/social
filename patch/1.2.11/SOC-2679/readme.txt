SOC-2679: Warning popups when sharing URL image on activity stream

Problem description
* What is the problem to fix?
Warning popups when sharing URL image on activity stream 

Fix description
* Problem analysis
This problem happens because there is no condition to check that link title and link description are not empty.

* How is the problem fixed?
Add condition to check that link title and link description are not empty.

Patch file: PROD-ID.patch

Tests to perform
* Reproduction test
- Login to social intranet
- Go to activity stream
- Share an image link. E.g: http://www.jesus-is-savior.com/Evils%20in%20America/Rock-n-Roll/party.gif
- Click "+" button to add. Error popup appears.
- Click OK. Another popup appears.

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
