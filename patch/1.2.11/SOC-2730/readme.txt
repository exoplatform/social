SOC-2730: [Space Navigation] Cut node comes back to its old position after click Back button in Edit this node 

Problem description
* What is the problem to fix?
Cut node comes back to its old position after click Back button in Edit this node

Fix description
* Problem analysis
Tree node data is re-loaded in case of clicking 'Back' button when edit node. To ignore this behavior just hide popup in that case not execute others task for example reload data of tree node as now doing.

*How is the problem fixed?
Ignore reloading tree node in case of 'Back' action, just hide the edit form.
Move stuff codes for updating node information and reload tree node into 'Save' action.

Patch file: https://github.com/exoplatform/social/pull/88

Tests to perform
* Reproduction test
- Create new space "space01"
- Go to navigation of space
- Create new node "test"
- Create new sub-node "test1" under "test"
- Right click on "test1" -> Cut node
- Paste this node is the same range with parent node
- Right click and click edit new node
- Don't change anythiing. Click back button.
- Problem: The position of new node will back to old position => NOK

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
