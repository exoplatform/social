Summary
Hard coded labels in SpaceActivityPublisher.java and ProfileUpdatesPublisher.java 
CCP Issue:  CCP-xyz 
Product Jira Issue: SOC-2058.
Complexity: N/A
Proposal
 

Problem description
What is the problem to fix?
Some hard coded labels of Activities, Contact View and Public Profile.
SpaceActivityPublisher.java: 
- has join, has left the space
- was created by

ProfileUpdatesPublisher.java: 
- has a new profile picture, has updated his header info.
- profile has updated his basic profile info, profile has updated his contact info, profile has an updated experience section: not correct grammar.

RelationshipPublisher.java

Fix description
Problem analysis
Some hard coded labels of Activities, Contact View and Public Profile.

How is the problem fixed?

- Allows all activities plugin to have the same mechanism to I18N-ize those kinds of messages.
- It's easy for Rest services to use that same mechanism mentioned above to I18N-ize those kinds of messages, too.
- I18N support for OpenSocial activities is needed.
- Support I18N feature out of the box for external activity plugins.
- As the idea for I18N is partly supported before: titleId and templateParams of an activity is used for that purpose. For convention of this feature, titleId must be used to detect that is an I18N activity when it is not null, and it's used as resource bundle key. And templateParams must be used as arguments for compound resource bundle, see more at: http://docs.oracle.com/javase/tutorial/i18n/format/messageFormat.html. Compound resource bundle is already supported by ResourceBundleService.
- For more detail please refer to Spec docment for using resource bundle for publishers

Tests to perform
Reproduction test

 cf. above
Tests performed at DevLevel

cf. above
Tests performed at Support Level

cf. above
Tests performed at QA
*

Changes in Test Referential
Changes in SNIFF/FUNC/REG tests

None
Changes in Selenium scripts 

None
Documentation changes
Documentation (User/Admin/Dev/Ref) changes:

None
Configuration changes
Configuration changes:

Yes 
Will previous configuration continue to work?

Yes
Risks and impacts
Can this bug fix have any side effects on current client projects?

Function or ClassName change: Yes
Data (template, node type) migration/upgrade: None
Is there a performance risk/cost?

None
Validation (PM/Support/QA)
PM Comment

PL validated
Support Comment

Support validated
QA Feedbacks

N/A
