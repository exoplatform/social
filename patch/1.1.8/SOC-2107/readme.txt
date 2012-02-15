Summary

    * Status: Selected date in "experiences" profile is not well interpreted (locales)
    * CCP Issue: CCP-1113, Product Jira Issue: SOC-2107.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    * When choosing date in mini calendar in french language:

    * the mini calendar allows selecting date in european format (DD/MM/YYYY) but it is saved as an US format (MM/DD/YYYY)
    * french translation of the output date isn't correct: it's appeared as Février 9 2010, but it should be le 2 septembre 2010
    * when the first date (DD) is greater than 12, then the month is count as 12 months + the modulo beetween first date and 12. For example: 16/09/2010 will be displayed as Avril 9 2011

Fix description

How is the problem fixed?

    * Get the start date and end date by locale
      ?
      SimpleDateFormat sd = new SimpleDateFormat(DATE_FORMAT_MMDDYYYY, Locale.ENGLISH);
    * Display datetime in correct format for each country.
      ?
      protected String displayDateTime(Object d) throws ParseException{
          Date date = this.stringToCalendar(d.toString()).getTime();
          Locale l = WebuiRequestContext.getCurrentInstance().getLocale();
          Calendar cal = Calendar.getInstance(l) ;
          DateFormat sf = SimpleDateFormat.getDateInstance(DateFormat.LONG, l);
          return sf.format(date) ;

Patch files:SOC-2107.patch

Tests to perform

Reproduction test
*

Tests performed at DevLevel
*

Tests performed at QA/Support Level
*
Documentation changes

Documentation changes:
*
Configuration changes

Configuration changes:
*

Will previous configuration continue to work?
*
Risks and impacts

Can this bug fix have any side effects on current client projects?

    * Function or ClassName change: None

Is there a performance risk/cost?
*
Validation (PM/Support/QA)

PM Comment

    * PM validated

Support Comment

    * Support validated

QA Feedbacks
*

