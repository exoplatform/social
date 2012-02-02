Summary

Status: Show message alert when add new space which was deleted
CCP Issue: CCP-xyz, Product Jira Issue: SOC-2357.
Complexity: N/A
The Proposal

Problem description
What is the problem to fix?
Show message alert when add new space which has the same name as a deleted space.
Steps to reproduce:

Go to My Space
Add new space with name "test"
Delete new space
Continue add new space with name is the same with space which was deleted above
Error: Show message alert "A page named 'test' already exists, please choose a different name for the space."
Fix description
How is the problem fixed?
In case one space is deleted all related information of that space is removed also. But it seems cause by cache problem of navigation then the check method return the wrong result. To fix it, we add more condition in check space directly from our service to ignore the cache of navigation.

SpaceUtils.java
static public boolean isSpaceNameExisted(String spaceName) throws SpaceException {
    ..................
 
      if ((pn.getNode(space_Name) != null) ||
          (pn.getNode(spacePrettyName) != null)) && 
          (getSpaceByGroupId(pn.getOwnerId()) != null)) return true;
 
    ..................
     
    return false;
  }


Patch files: SOC-2357.patch


Tests to perform
Reproduction test
*

Tests performed at DevLevel
*

Tests performed at QA/Support Level
*

Documentation changes:
* No


Configuration changes:
* No

Will previous configuration continue to work?
* Yes

Risks and impacts
Can this bug fix have any side effects on current client projects?

Function or ClassName change
No
Is there a performance risk/cost?
* No
Validation (PM/Support/QA)

PM Comment
* PM validated

Support Comment
* Support validated

QA Feedbacks
*
