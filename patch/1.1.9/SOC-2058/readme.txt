Summary
Hard coded labels in Publishers (Space Activity, Profile Updates)
CCP Issue:  CCP-1086 
Product Jira Issue: SOC-2058.
Complexity: high
Proposal
 

Problem description
What is the problem to fix?
Some hard coded labels of Activities, Contact View and Public Profile.
SpaceActivityPublisher.java: 
- has joined, has left the space
- was created by

ProfileUpdatesPublisher.java: 
- has a new profile picture, has updated his header info.
- profile has updated his basic profile info, profile has updated his contact info, profile has an updated experience section: not correct grammar.

Fix description
Problem analysis
Some hard coded labels of Activities, Contact View and Public Profile.

How is the problem fixed?
- Allows all activities plugin to have the same mechanism to I18N-ize those kinds of messages.
- It's easy for Rest services to use that same mechanism mentioned above to I18N-ize those kinds of messages, too.
- I18N support for OpenSocial activities is needed.
- Support I18N feature out of the box for external activity plugins.
- As the idea for I18N is partly supported before: titleId and templateParams of an activity is used for that purpose. For convention of this feature, titleId must be used to detect that is an I18N activity when it is not null, and it's used as resource bundle key. And templateParams must be used as arguments for compound resource bundle, see more at: http://docs.oracle.com/javase/tutorial/i18n/format/messageFormat.html. Compound resource bundle is already supported by ResourceBundleService.
- For more detail please refer to Spec document for using resource bundle for publishers

Tests to perform
Reproduction test
Case 1: Check Space activities "was created by, has join, has left the space"
    * Mary creates space "Space 001"
    * Demo joins "Space 001"
    * Mary accepts demo's request
    * Demo quits space "Space 001"
    * Mary: check French translation
          o Go to My Activities 
          o Go to Spaces tab
          o Change language to French: the activities are still in English
	    Jack Miller has left the space.
	    Jack Miller has joined.
	    Space 001 was created by Mary Williams.

Case 2: Check Profile updates
    * Update profile picture 
          o Change language to French
          o Make sure that you see this result:
	    Mary Williams has updated his header info.
	    Mary Williams profile has updated his contact info.
            Mary Williams has a new profile picture.
	    Mary Williams profile has an updated experience section.  

Tests performed at DevLevel
* Unit tests

Tests performed at Support Level
Case 1: 
    * Mary: check French translation: the activities are in French
	    Jack Miller a quitté l'espace.
	    Jack Miller a rejoint l'espace.
	    Space 001 a été créé par Mary Williams.
    * Add a comment
          o Add new comment (e.g: "abcdef") on any of the above activity
          o Make sure that you still see the activity in French 
    * Like/Unlike an activity: the activity's language doesn't come back to English

Case 2: Check Profile updates
    * When change language to French: the activities must be in French
	    Mary Williams a mis à jour ses informations d'en-tête.
	    Mary Williams a mis à jour ses informations de contact.
	    Mary Williams a mis à jour son image de profil.
	    Mary Williams a mis à jour ses expériences.	
    * In English, there isn't any typo.

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
