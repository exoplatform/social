SOC-2768: Wrong document preview since the second page of Activity Stream

Problem description
* What is the problem to fix?
Wrong document preview since the second page of Activity Stream.

Fix description
* Problem analysis
When Social change it activity stream display from page to "load more" the UIActivitiesContainer was not updated so each "page" still contain one UIActivityPopup. When pages 2 show popup to display document preview, it show popup on "page" 1 ( blank popup) cause the duplicate of popup ID. 
* How is the problem fixed?
Move UIPop on UIActivitiesContainer to UIActivitiesLoader so every "page" only use one popup. 

Patch file: PROD-ID.patch

Tests to perform
Reproduction test
- Login. 
- In User Public folder: upload more than 20 files (pdf, images)
- Return intranet home page. In Activity Stream.

Case 1: 
- Preview 1 pdf file (File A): OK
- Click "Show more posts..."
- Preview 1 pdf file (File B) in the 2nd page: File A appears in Doc Viewer: NOK
- Close Doc Viewer -> come back the default Activity Stream (20 activities): NOK

Case 2: 
- Click "Show more posts..."
- Preview 1 pdf file (File B) in the 2nd page: empty Popup Window: NOK
- Close Doc Viewer -> come back the default Activity Stream (20 activities): NOK

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
...
Support Comment
...
QA Feedbacks
...
