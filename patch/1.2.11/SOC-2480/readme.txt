SOC-2480: [Space] Remove Priority of Space 

Problem description
* What is the problem to fix?
Priority of Space should be removed because space is sorted by alphabet

Fix description
* Problem analysis
Priority of space is useless. So we should remove it.

* How is the problem fixed?
Only remove select box Priority of Space from Space Add Form Popup and Space Setting UI.
Don't make change in data structure so at this time, don't need data migration.

Git Pull Request: https://github.com/exoplatform/social/pull/107

Tests to perform
* Reproduction test
- Create new space
- Go to space settings
- See that Priority of space is disappeared.

* Tests performed at DevLevel
- Create new space
- See that Priority of space is not displayed in Space Add form.
- Go to space settings
- See that Priority of space is disappeared.

* Tests performed at Support Level
...
* Tests performed at QA
...

Changes in Test Referential
* Changes in SNIFF/FUNC/REG tests
No
* Changes in Selenium scripts 
No

Documentation changes
Documentation (User/Admin/Dev/Ref) changes:
No

Configuration changes
* Configuration changes:
No
* Will previous configuration continue to work?
Yes

Risks and impacts
Can this bug fix have any side effects on current client projects?

Function or ClassName change: No
Data (template, node type) migration/upgrade: 
Is there a performance risk/cost?
No

Validation (PM/Support/QA)
PM Comment
...
Support Comment
...
QA Feedbacks
...
