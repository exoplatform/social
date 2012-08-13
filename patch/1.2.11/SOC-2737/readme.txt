SOC-2737: new and deleted application is not updated in Clustering mode in the node 2

Problem description
* What is the problem to fix?
new and deleted application is not updated in Clustering mode in the node 2 

Fix description
* Problem analysis
When display UISpaceApplication the UI logic doesn't check the data has been change by other node ( node 1 in this case).

* How is the problem fixed?
Fixed by call checkUpdate() every-time page load to check if data have been change or not. If data have been change then force reload all page component.

Patch file: https://github.com/exoplatform/social/pull/84

Tests to perform
* Reproduction test

Case1: new application is not updated in node2
- Node1:
Create a space toto. Go to space settings/ application.
Add a new application (e.g: address book)
- Node2:
Go to space toto. Go to space settings/ Application
The new application is not in the list

Case2: deleted application is not updated in node2
- Node1:
Go to space toto/ settings/ application.
Delete an application (e.g: Calendar)
- Node2:
Go to space toto/ settings/ Application
The application Calendar is still in the list

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
