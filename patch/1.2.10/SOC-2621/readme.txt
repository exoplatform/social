SOC-2621: Wrong context exception in org.exoplatform.social.webui.URLUtils.getCurrentUser()

Problem description
What is the problem to fix?
	On Intranet's log, there are many exceptions flood log file like:

		 INFO: Cannot obtain user: all-spaces; 
		org.picketlink.idm.common.exception.IdentityException: IdentityObjectType[USER] not present in the store.
		 at org.picketlink.idm.impl.store.hibernate.HibernateIdentityStoreImpl.getHibernateIdentityObjectType(HibernateIdentityStoreImpl.java:2812)
		 at org.picketlink.idm.impl.store.hibernate.HibernateIdentityStoreImpl.getHibernateIdentityObject(HibernateIdentityStoreImpl.java:2827)
		   ...

		INFO: Cannot obtain user: engineering; 
		org.picketlink.idm.common.exception.IdentityException: IdentityObjectType[USER] not present in the store.
		 at org.picketlink.idm.impl.store.hibernate.HibernateIdentityStoreImpl.getHibernateIdentityObjectType(HibernateIdentityStoreImpl.java:2812)
		 at org.picketlink.idm.impl.store.hibernate.HibernateIdentityStoreImpl.getHibernateIdentityObject(HibernateIdentityStoreImpl.java:2827)
		   ..

		INFO: Cannot obtain user: cloud_workspaces; 
		org.picketlink.idm.common.exception.IdentityException: IdentityObjectType[USER] not present in the store.
		 at org.picketlink.idm.impl.store.hibernate.HibernateIdentityStoreImpl.getHibernateIdentityObjectType(HibernateIdentityStoreImpl.java:2812)
		 at org.picketlink.idm.impl.store.hibernate.HibernateIdentityStoreImpl.getHibernateIdentityObject(HibernateIdentityStoreImpl.java:2827)
		   ...

		INFO: Cannot obtain user: TC-PLF-3.5.3_Performance_Reports; 
		org.picketlink.idm.common.exception.IdentityException: IdentityObjectType[USER] not present in the store.
		 at org.picketlink.idm.impl.store.hibernate.HibernateIdentityStoreImpl.getHibernateIdentityObjectType(HibernateIdentityStoreImpl.java:2812)
		   ...

Fix description
Problem analysis
	All logs fired out cause the wrong in using org.exoplatform.social.webui.Utils.getOwnerRemoteId(). 
	This method call URLUtils.getCurrentUser(). 
	URLUtils.getCurrentUser() get current user base on URL. 
	In case of got wrong user name from URL to put as parameter into UserHandler#findUserByName(String) then IdentityException is thrown. 
	Maybe the out of control in using that method then need to check and fix all other same cases.

How is the problem fixed?
	Don't call org.exoplatform.social.webui.Utils.getOwnerIdentity(Utils.java:110) method in
	org.exoplatform.social.webui.space.UIManageAllSpaces.getTypeOfSpace(UIManageAllSpaces.java:300)
	Need to validate SpaceIdentity before.

Tests to perform
Reproduction test
	This issue occurs quite often in production (int.e.o, cwks) but not easy to reproduce in local environment.
Tests performed at DevLevel
...
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
* No

Will previous configuration continue to work?
* No

Risks and impacts
Can this bug fix have any side effects on current client projects?
	N/A

Function or ClassName change: 
Data (template, node type) migration/upgrade: 
Is there a performance risk/cost?
...

Validation (PM/Support/QA)
PM Comment
	PM validated.
Support Comment
	Support validated.
QA Feedbacks
...
