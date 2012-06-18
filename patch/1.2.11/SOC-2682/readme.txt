SOC-2682: share button should be reactive when decide to not attach file or link

Problem description
* What is the problem to fix?
Share button doesn't reactive when decide to not attach file or link

Fix description
* Problem analysis
When user click button to attach a "link" or a "file", the share button is disabled by the js. After that, user decides cancel to attach a "file" or a "link", so click again on "link" or "file", the share button is actived but it still blur like disable status because the js doesn't change the class name of the button.

* How is the problem fixed?
Using js to check for condition of share button to set its appropriate class name.

Git Pull Request: https://github.com/exoplatform/social/pull/62

Tests to perform
* Reproduction test
- Log in and add a space
- Type text in the stream,
- Press button to add "link" or a "file", the SHARE button is greyed out.
- You decided to not attach a file or link, so click again on "link" or "file"
- Click "share" to share your text written in the stream 
Actual result: share button is active (the text is well shared) but it doesn't seems to be (it's is greyed out)
Expected result: the "share" button shoud appear functional.

Tests performed at DevLevel
- Log in and add a space.
- Type text in the stream.
- Press button to add "link" or a "file", the SHARE button is greyed out.
- You decided to not attach a file or link, so click again on "link" or "file".
- The "share" button shoud appear functional.
- Click "share" to share your text written in the stream.

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
n/a

Function or ClassName change: No
Data (template, node type) migration/upgrade: No
Is there a performance risk/cost?
No

Validation (PM/Support/QA)
PM Comment
	PM validated
Support Comment
	Support validated
QA Feedbacks
...
