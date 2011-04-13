Summary

    * Status: Spelling in French message of connection request
    * CCP Issue: CCP-894, Product Jira Issue: SOC-1481.
    * Complexity: trivial

The Proposal
Problem description

What is the problem to fix?

    * When I receive requests for connection in French, it is written connxion instead of connexion.

Fix description

How is the problem fixed?
Correct in UIRelationshipActivity_fr.properties
vous a envoyé une demande de connxion --> vous a envoyé une demande de connexion

Patch file: SOC-1481.patch

Tests to perform

Reproduction test
Use language French

   1. Login as john
   2. Go to Mon Réseau/Sortant/Ajouter des Contacts
   3. Invite Mary Williams
   4. Login as mary
   5. Go to Mes Activités/Mon status
      Expected result: john vous a envoyé une demande de connexion.
      Actual : john vous a envoyé une demande de connxion.

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

    * Function or ClassName change: None

Is there a performance risk/cost?

    * No

Validation (PM/Support/QA)

PM Comment

    * Patch validated by PM

Support Comment

    * Patch validated

QA Feedbacks
*

