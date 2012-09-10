SOC-2775: Technical name of space drives 

Problem description
* What is the problem to fix?
The drive name shows the technical name.
Expected behaviour : the group name = user given name

Fix description
* Problem analysis
When creating a space, the space name is not set as group label.

* How is the problem fixed?
When creating a space, set the space name as group label.

Patch file: https://github.com/exoplatform/social/pull/99

Tests to perform
* Reproduction test
- Create new space, e.g: "Big Bang"
- Go to Personal Documents
- See that in Group Drives, there is a new drive named "Big Bang".

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
