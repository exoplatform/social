Summary

    * Status: Problem with OSHtmlSanitizerProcessor
    * CCP Issue: PLF:CCP-923, Product Jira Issue: PLF:SOC-1532.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?
How to reproduce:

1.

Login as Demo, access his "activities" page.
Share first activity as: "Hello World".
Share second activity as "<b>Hello then".

=> The activity stream has problems as shown in the screenshot:

+ Everything is bold
+ All comments form are visible

2. Use <br /> is ok, <br/> is not ok. Is this the normal behavior?
Fix description

How is the problem fixed?

    * Introduce org.exoplatform.social.common.xmlprocessor.XMLProcessor component to process input string.
    * Configure plugins component for XMLProcessor to work by adding external filters.
    * Configure plugins component for XMLTagFilterPolicy by adding allowed tags.
    * OSHtmlSanitizerProcessor uses XMLProcessor to process the activity's input.

Patch files:SOC-1532.patch

Reproduction test

    * As described above.

Tests performed at DevLevel
*

Tests performed at QA/Support Level
*
Documentation changes

Documentation changes:

    * No.

Configuration changes

Configuration changes:

    * Add configuration for XMLProcessor, XMLTagFilterPolicy and their plugins.

Will previous configuration continue to work?

    * Yes, definitely.

Risks and impacts

Can this bug fix have any side effects on current client projects?

    * No.

Is there a performance risk/cost?

    * No risk at all.
    * New components are introduced.
    * The patch does not only fix the problem, but also add improvement: smart completion of missing tags, allow new line characters to interpreted with <br /> tag.

Validation (PM/Support/QA)

PM Comment

    * PL review: Patch validated

Support Comment

    * Support review: Patch validated

QA Feedbacks
*
Labels parameters
Étiquettes :
Aucun
sl3 sl3 Supprimer
patch patch Supprimer
Modifier les étiquettes
Saisissez les étiquettes à ajouter à cette page:
Please wait 
Vous recherchez une étiquette ? Commencez à taper.
Ajouter un 
