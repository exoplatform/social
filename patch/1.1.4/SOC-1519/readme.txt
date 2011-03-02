Summary

    * Status: Use name instead of login for connection activities
    * CCP Issue: CCP-804, Product Jira Issue: SOC-1519.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    * Follow http://int.exoplatform.org/portal/public/intranet/forum/topic/topica83aff6e2e8902a900706f4795eff45e

When an user accepts a connection, the activity message displays the login instead of the name of the user :

thomas_delhomenie is now connected with patrice_lamarque.

Should be

Thomas Delhoménie is now connected with Patrice Lamarque.
Fix description

How is the problem fixed?

    * The problem: from UIRelationshipActivity:

        private String getProfileLink(String username) {
          LinkProvider linkProvider = getApplicationComponent(LinkProvider.class);
          return "<a href=" + linkProvider.getProfileUri(username) + ">" + username +"</a>";
        }

To fix it, use LinkProvider#getProfileLink(String) instead:

  private String getProfileLink(String username) {
    LinkProvider linkProvider = getApplicationComponent(LinkProvider.class);
    return linkProvider.getProfileLink(username);
  }

Patch information:
Patch files: SOC-1519.patch

Tests to perform

Reproduction test

    * Login as Demo, send connection request to John
    * Access John's activities page
      -> See the activity:
      + Expected: "Demo gtn invited John Anthony to connect."
      + Actual: "demo invited john to connect."
    * Login as John, accept Demo's connection request
    * Login as Demo, access John's activities page
      -> See the activity:
      + Expected: "Demo gtn is now connected with John Anthony."
      + Actual: "demo is now connected with john."

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

    * Function or ClassName change

Is there a performance risk/cost?

    * No

Validation (PM/Support/QA)

PM Comment
* PM validated

Support Comment
* Support validated

QA Feedbacks
*

