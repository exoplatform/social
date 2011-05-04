Summary

    * Status: Space Settings option can be seen by non-moderator user
    * CCP Issue: CCP-831, Product Jira Issue: SOC-1575.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

We expect that only moderator user can see this option in toolbar
if we click on space setting into toolbar, a [blank page|CCP-831 SOC-1575^blank.GIF|\||] will be displayed
Strange, if you're not manager of a space, its space settings page node must not be displayed
Fix description

How is the problem fixed?

Someone who can access the setting portlet (as an application in created space) if he has manager role. So to display the link or not base on checking the role of viewer. Before, we check the permission with the "SpaceSettingPortlet" as name, but after issue ( SOC-1068 | Allow to customize default application names ) that made some changes in the name of application so the returned result in checking is not right longer. To fix we check the node name with value "settings" instead of "SpaceSettingPortlet".

Patch files: SOC-1575.patch

Tests to perform

Reproduction test
1. Login as root
2. Add new space (eg. named RootSpace)
3. Goto Space/RootSpace/Space Settings/Members: Add john to join this space
4. Logout of root and Login as john
5. Go to Space/Invation and accept request to join RootSpace of root
 --> John is not a manager of RootSpace but he can view the Space Setting option in toolbar.: NOK
Expected: Only moderator user can see this option in toolbar.

Tests performed at DevLevel
*

Tests performed at QA/Support Level
*
Documentation changes

Documentation changes:
* No
Configuration changes

Configuration changes:
* No

Will previous configuration continue to work?
* Yes
Risks and impacts

Can this bug fix have any side effects on current client projects?

    * Function or ClassName change : None

Is there a performance risk/cost?
* No
Validation (PM/Support/QA)

PM Comment
* PL review: Patch validated

Support Comment
* Support review: Patch validated

QA Feedbacks
*

