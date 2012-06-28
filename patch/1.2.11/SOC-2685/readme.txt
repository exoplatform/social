SOC-2685: Encoding issue on activity stream

Problem description
* What is the problem to fix?
This issue was already fixed for the most of case.
We still can reproduce it with like action from mobile.
To fix this issue we added updateActivity in storage API allowing to skip escape processing if necessary.
Mobile is using REST API and I'm note sure this update method is reachable from REST API.
To fix correctly this issue we probably should call updateActivity when necessary in saveActivity method.

Fix description
*Problem analysis
Mobile is using REST API and updateActivity method is not reachable from REST API.

* How is the problem fixed?
Call updateActivity when necessary in saveActivity method.

Patch file: PROD-ID.patch

Tests to perform
* Reproduction test
steps ...
* Tests performed at DevLevel
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
...
Support Comment
...
QA Feedbacks
...
