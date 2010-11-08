=====================================================
    Release Notes - eXo Social - Version 1.1.1
=====================================================

===============
1 Introduction
===============

eXo Social is composed of 2 modules :

    * eXo People: brings Enterprise Social Networking to your work and allows you to organize your
                  workforce in an efficient way through a better understanding of your people skills.
        - Turn your directory into a social network
        - Users can fill their profile
        - Search by skills and experience
        - Activity status updates
        - OpenSocial API support

    * eXo Spaces: add communities to your work and enable a collaborative work.
                  It's the communities workgroups.
        - Let dormant communities reveal themselves
        - Open community management
        - Activity streams to aggregate knowledge
        - Easily deploy custom applications
        - User friendly interface


=============
2 What's new?
=============

- Focus more on Social's performance with ExoCache. Make avatar images are cached.
- Make it configurable for OpenSocial Rest Context API.
- UI enhancement: more incentive "Add New Space" button; move edit space navigation into space settings

=========
3 INSTALL
=========

Find the latest install guide here : http://wiki.exoplatform.com/xwiki/bin/view/Social/InstallGuide

- System Requirements
        Web Browser: IE6, IE7, FF2, FF3 (recommended), Safari.
        JVM: version 1.6.0_0 or higher
        Application Server : tomcat-6.0 and up
        Building Tools: Maven 2.2.1 and up

- Social quick start guide
  Social have 1 server need to run to use:
    +) tomcat: this is main tomcat server include Social web applications and all dependencies.

Need to set the JAVA_HOME variable for run Social servers.
+) How to start Social:
   * First thing first you need to give all script files the executable permission if you are in unix family environment.
   Use command: "chmod +x *.sh" (without quote) to have execute permission on these files.

   * NOTE for cygwin's user: the JAVA_HOME must be in MS Windows format like: "C:\Program Files\JDK 1.5"
    Example use: export JAVA_HOME=`cygpath -w "$JAVA_HOME"`; to convert unix like format to MS Windows format.



   * Start tomcat server

     +) On the Windows platform
       Open a DOS prompt command, go to tomcat/bin and type the command:
        "gatein.bat run" for production
        "gatein-dev.bat run" for development


     +) On Unix/Linux/cygwin
       Open a terminal, go to tomcat/bin and type the command:
         "./gatein.sh run" for production
         "./gatein-dev.sh run" for development

-) How to access the eXo Social

* Enter one of the following addresses into your browser address bar:
   Social demo portal
      http://localhost:8080/socialdemo
   Classic :
      http://localhost:8080/portal
      http://localhost:8080/portal/public/classic


You can log into the portal with the following accounts: root, john, marry, demo.
All those accounts have the default password "gtn".

* Direct link to access applications in Social :
    +) Spaces application: http://localhost:8080/socialdemo/private/classic/spaces
    +) People application: http://localhost:8080/socialdemo/private/classic/people
    +) Activity stream application: http://localhost:8080/socialdemo/private/classic/activities
    +) Profile of user: http://localhost:8080/socialdemo/private/classic/profile

  You will get login form if you are not yet logged in to Social.


===========
4 RESOURCES
===========

     Company site        http://www.exoplatform.com
     Community JIRA      http://jira.exoplatform.org
     Community site      http://www.exoplatform.org
     Community gatein    http://www.jboss.org/gatein/
     Developers wiki     http://wiki.exoplatform.org


===========
5 CHANGELOG
===========

- 1.1.1

** Bug
    * [SOC-817] - Internal Error when updating activities with very long message - All following messages, even short, give same error (demo2)
    * [SOC-819] - In profile, required fields are not marked as such (start and end date in "Experiences")(demo2)
    * [SOC-981] - Attaching an image to an activity stream of a space causes javascript misinterpretation
    * [SOC-1005] - [Space][activity] Disable Share button when attach some link
    * [SOC-1018] - [SOC]: Change message when edit FN/LN contain specical chars in Public Profile
    * [SOC-1042] - spaces and people selector font is not consistent with overall look and feel
    * [SOC-1053] - Shared link is not available on User'activity
    * [SOC-1058] - Apps preconfigured on Spaces does not work, need lifecycle for this
    * [SOC-1059] - Some labels are not translated in space
    * [SOC-1101] - Space: Change label for icons in share comment
    * [SOC-1102] - Text right after [@] character is not displayed in user activites
    * [SOC-1108] - [Activity] text for comment is clean after share link or document
    * [SOC-1112] - Can't publish activity stream using the opensocial API
    * [SOC-1113] - Space, FF3.6: Text for comment is clean after choosing attachment
    * [SOC-1114] - When adding an activity and attaching an image only the image is taken into account
    * [SOC-1119] - [space] lost Revoke icon on pending space list
    * [SOC-1124] - opensocial.Person.Field.PROFILE_URL should return a valid URL
    * [SOC-1125] - Bug in Activity Stream when user deletes inputted text in the status textbox when share link, document
    * [SOC-1127] - Share image link and input status text, input text disappears
    * [SOC-1129] - [People]_A wrong control on Start date
    * [SOC-1131] - Activity extension are resetting the activity field to blank
    * [SOC-1134] - Bug in templateParams of activity
    * [SOC-1136] - It is possible to remove last space leader
    * [SOC-1142] - Avatar images are not cached by browser
    * [SOC-1154] - Can't share links with images

** Feedback
    * [SOC-743] - SNF_PRL_03 fail : nothing gets translated
    * [SOC-866] - order the list of people by last name

** Improvement
    * [SOC-726] - Move edit space navigation into space settings
    * [SOC-1066] - More incentive "Add New Space" button
    * [SOC-1069] - Use applications icons


- 1.1.0-GA

** Bug
    * [SOC-695] - Show message wrong when edit space has some special chars
    * [SOC-699] - Unkown error when edit naviagation of space which was delete in Group Navigation
    * [SOC-707] - Have problem when create new space but delete space template in Manage page
    * [SOC-813] - screen is blinked continuously when add  Space List Gadget
    * [SOC-848] - Activity title is not displayed in streams
    * [SOC-857] - Space tab disappears when other user is creating space
    * [SOC-863] - error deleting a space
    * [SOC-865] - After accepting an invitation to a space, the menu at the top of the page don't display their submenu
    * [SOC-867] - removing yourself from the admin of a space break the UI
    * [SOC-870] - Activities retrieval is unlimited
    * [SOC-872] - Unknown error occurs when trying to rename a application portlet with Space characters
    * [SOC-876] - Space characters should not allowed when posting an activity
    * [SOC-879] - Change the message type
    * [SOC-889] - REST api is not secured
    * [SOC-894] - after creating a space (the first one) you can't access it without logging out
    * [SOC-900] - Show error message when adding an invalid link in Attach Link (Space)
    * [SOC-949] - a navigation cache issue
    * [SOC-952] - [Space] show Javascript when share link on Activities portlet on Space
    * [SOC-955] - Validate Space Name
    * [SOC-957] - multiple javascript alerts SyntaxError: unterminated string literal
    * [SOC-963] - Exception when creating new space by widget
    * [SOC-969] - Can't publish an activity for a space created in socialdemo by opensocial API
    * [SOC-985] - Space avatar is not used in the activity stream
    * [SOC-990] - [profile] Can not add more experience
    * [SOC-1009] - User Activity does not display shared document properly
    * [SOC-1010] - Bad link when sharing doc in activities
    * [SOC-1013] - Comment on "Connections" and "Spaces" does not work
    * [SOC-1014] - Javascript error occurs in space activity in IE 7
    * [SOC-1015] - Canvas navigation should not be populated from extension
    * [SOC-1016] - Activity is displayed in list after adding in case we switch acrossing 3 tabs 'Connections', 'Spaces' and 'My Status'
    * [SOC-1017] - Some minor bugs occur in space activities
    * [SOC-1021] - Have to click x twice to delete added activity
    * [SOC-1023] - [Social] - Show error message when open a Space in IE browser
    * [SOC-1025] - Can not upload new avatar for space and User on Social Branchs 1.1.x
    * [SOC-1033] - Space avatar is not used in Space activities
    * [SOC-1040] - [Social] User Activity Stream : Encoding problem
    * [SOC-1041] - Bad Download link for  a document shared below the root of the drive
    * [SOC-1051] - [Soc] Error with link of attached DMS document in added activity
    * [SOC-1052] - Must disable 'Share' button when still not ready for posting activity
    * [SOC-1061] - Document Share Plugin does not work well
    * [SOC-1063] - A frenche label is not correctly spelled
    * [SOC-1074] - Problem with encoding of that property file when change language to French in my space
    * [SOC-1081] - Property missing for UIOneNodePathSelector
    * [SOC-1086] - IE6 UI problems

** Documentation
    * [SOC-1070] - Reference Guide

** Feedback
    * [SOC-749] - search space should match terms from space description
    * [SOC-750] - space priority is not self explanatory
    * [SOC-753] - Space navigation should match navigation order
    * [SOC-792] - Don't use a trash to remove a relation
    * [SOC-869] - creating a space take around 45sec
    * [SOC-1027] - Listing activities is slow, no pagination ?
    * [SOC-1030] - Share document does not work well
    * [SOC-1032] - UI for spaces is bad

** Improvement
    * [SOC-725] - socialdemo relooking
    * [SOC-859] - Add the possibility to edit or delete an activity from the space activity stream
    * [SOC-861] - Change the way of creating a space bound to an existing group
    * [SOC-1034] - Do not display "Portlet" in application names
    * [SOC-1045] - Document View for document share plugin

** New Feature
    * [SOC-656] - user in space without receive an invitation
    * [SOC-740] - Space page template
    * [SOC-755] - Add invite in Profile menu
    * [SOC-852] - pluggable share action
    * [SOC-932] - New Status Updates application
    * [SOC-933] - My Spaces Gadget
    * [SOC-941] - Extensible Actions for each type of Activity

** Task
    * [SOC-466] - Upgrade to Shindig 1.1
    * [SOC-597] - cleanup root pom.xml
    * [SOC-691] - Make a bundle with social extension ear deployed beside Gatein 3.0
    * [SOC-860] - Refactor code in Space portlets
    * [SOC-884] - Project cleanup
    * [SOC-885] - Refactor and simplify UIUserListPortlet
    * [SOC-891] - Reorganize ui components
    * [SOC-892] - make possible to create a space by a rest service
    * [SOC-897] - Properly organize gadgets
    * [SOC-899] - Upgrade to GateIn 3.1 CR1
    * [SOC-910] - apply new brand
    * [SOC-913] - Allows multi-tenancy
    * [SOC-920] - make isFirstCharOfSpaceName an optional parameter of getSpacesByName
    * [SOC-942] - Study how to plugin, understand new features and how to implement
    * [SOC-946] - Configuration for Pluggable Share
    * [SOC-947] - BaseActivity
    * [SOC-948] - UIDisplay for activities
    * [SOC-962] - rename social-ext.war to social-extension.war
    * [SOC-964] - new project layout and arfifactIds
    * [SOC-1028] - Use UserActivityPortlet on user activities page
    * [SOC-1062] - Social 1.1 JBoss EAR packaging
    * [SOC-1073] - No distribution was uploaded with social 1.1.0-CR02
    * [SOC-1088] - Do not duplicate resource bundle
    * [SOC-1089] - Correct name: opensocial.war to social.war in EAR packaging
    * [SOC-1092] - Release Social 1.1.0-GA

** Sub-task
    * [SOC-904] - Fix build failed for exo.social.component.exosocial
    * [SOC-905] - Fix build failed for project: exo.social.component.opensocial
    * [SOC-906] - Fix build failed for exo.social.component.people
    * [SOC-907] - Fix build failed for project: exo.social.component.space
    * [SOC-908] - Fix build failed for project: exo.social.extras.benches
    * [SOC-909] - UI issues fixing
    * [SOC-911] - Lost avatar in people portlet and not load changed avatar in space activity.
    * [SOC-914] - Code coverage for people project
    * [SOC-917] - Study how to render xml gadget to html gadget from shindig, applied in GateIn, Social
    * [SOC-918] - Study how to serve a json-rpc, how to create and wire a sample service
    * [SOC-919] - Study how features work and create a sample feature (eXo-Environment)
    * [SOC-951] - [Bug] Gadget loads comments as activities to user activity stream
    * [SOC-965] - New mockup for Activity (composer and activity stream)
    * [SOC-976] - Translate label into French
    * [SOC-1024] - Resource Bundle Needed for document share plugin
    * [SOC-1093] - Update Release Notes

-  1.1.0-CR03

** Bug
    * [SOC-1061] - Document Share Plugin does not work well
    * [SOC-1087] - Revert bad rollback of release plugin execution (wrong dependencies versions declared)

** Feedback
    * [SOC-1065] - need to improve some inconsistent grey bars acrros Social

** Improvement
    * [SOC-1034] - Do not display "Portlet" in application names
    * [SOC-1067] - Relationship activity type

** Task
    * [SOC-1062] - Social 1.1 JBoss EAR packaging
    * [SOC-1076] - Remove jboss-web.xml from extension war
    * [SOC-1085] - Release Social 1.1.0-CR03

** Sub-task
    * [SOC-1075] - Translate into French


-  1.1.0-CR02

** Bug
    * [SOC-1029] - @mentions hardcode "classic" in the URL
    * [SOC-1033] - Space avatar is not used in Space activities
    * [SOC-1040] - [Social] User Activity Stream : Encoding problem
    * [SOC-1043] - Social extension forces the creation of the 'classic' portal
    * [SOC-1044] - Space name's validation is wrong
    * [SOC-1049] - invited User can't access space #2
    * [SOC-1051] - [Soc] Error with link of attached DMS document in added activity
    * [SOC-1055] - DateTime message is hard-coded
    * [SOC-1056] - Click on author in user activity title links to "classic"
    * [SOC-1057] - Missing gadgets when english language is not selected

** Feedback
    * [SOC-1030] - Share document does not work well
    * [SOC-1032] - UI for spaces is bad

** Improvement
    * [SOC-1045] - Document View for document share plugin

** Task
    * [SOC-1028] - Use UserActivityPortlet on user activities page
    * [SOC-1072] - Release Social 1.1.0-CR02

** Sub-task
    * [SOC-1060] - Translate the time labels into French


-  1.1.0-CR01

** Bug
    * [SOC-1009] - User Activity does not display shared document properly
    * [SOC-1010] - Bad link when sharing doc in activities
    * [SOC-1011] - Groovy Compilation error on User activity stream portlet
    * [SOC-1013] - Comment on "Connections" and "Spaces" does not work
    * [SOC-1014] - Javascript error occurs in space activity in IE 7
    * [SOC-1015] - Canvas navigation should not be populated from extension
    * [SOC-1019] - [Social] - Can share the activity without inputing any value in the text box
    * [SOC-1021] - Have to click x twice to delete added activity
    * [SOC-1023] - [Social] - Show error message when open a Space in IE browser
    * [SOC-1031] - Impossible to share a document

** Feedback
    * [SOC-757] - activities page is too long

** Task
    * [SOC-988] - release social 1.1 CR01
    * [SOC-1035] - base structure for eXo Social Reference Guide


** Sub-task
    * [SOC-1026] - Translate Resource Bundle into French

===================================================================================================
-  1.0.0 GA

Bug
    * [SOC-589] - Show exception when delete user who invited
    * [SOC-671] - Session timeout when delete user in member tab of space which used to create this space
    * [SOC-697] - Duplicate Space's group in Application registry portlet
    * [SOC-702] - Show wrong in Popup My Space after created new node
    * [SOC-704] - Lose page's title of request page in Manage Page
    * [SOC-712] - IE6: Error displaying in Search invited form when don't any user is invited
    * [SOC-814] - Error when go to Activity page with Ubuntu
    * [SOC-815] - Can not search by alphabet or All after inputing some special chars (eg: @, #, $,\, ...) into search text box
    * [SOC-835] - Not display navigation in menu of group toolbar after creating space bound to existing group
    * [SOC-837] - gadget on the dashboard of a space
    * [SOC-838] - New clonned node does not have same properties and page as original node
    * [SOC-839] - Display collapse/append when view other activities
    * [SOC-840] - Occur exception and can't view space setting portlet in special case
    * [SOC-841] - User can't add navigation if user  is manager of group but navigation for that group has not been created yet
    * [SOC-842] - Can not view friend's activity in list of invited user
    * [SOC-843] - Page index of the member list of a space does not work well
    * [SOC-846] - Blue border is missing on space layout

Feedback
    * [SOC-82] - should synchronous data in some portlet
    * [SOC-745] - look and feel of login page is outdated
    * [SOC-747] - enhance readability of space description
    * [SOC-752] - Members screen is confusing.
    * [SOC-756] -  "Friends activities" should be renamed "${OWNER} Relations Activities"
    * [SOC-761] - Enhance how empty fields of someone else's profile are displayed
    * [SOC-825] - When going to someone else's profile, it first shows me the status update field, then disappears

Improvement
    * [SOC-830] - Update link title, description inlined after attaching a link
    * [SOC-833] - Make user activies and space activities's has the same look and feel
    * [SOC-849] - Enhance look and feel of activities

Task
    * [SOC-687] - social.ear for JBoss
    * [SOC-693] - Rename Invitation/Request listing
    * [SOC-737] - Data injector
    * [SOC-763] - Fix the invalid gatein-resources.xml in ExoResourcesSocial
    * [SOC-783] - french translation
    * [SOC-847] - Global wording review


- 1.0.0 CR02

Bug

    * [SOC-563] - IE6: Error UI in Social Navigation
    * [SOC-588] - Error displaying in Avatar upload form when have long of name of picture in space setting
    * [SOC-589] - Show exception when delete user who invited
    * [SOC-674] - Can not creating a new space from an user-created existing group
    * [SOC-691] - the Social.ear dosn't exist on jboss
    * [SOC-692] - problem when start jboss
    * [SOC-695] - Show message wrong when edit space has some special chars
    * [SOC-697] - Duplicate Space's group in Application registry portlet
    * [SOC-698] - Still create new space with name is existing name's group in user and group management
    * [SOC-701] - Show wrong in menu UserToolBarGroupportlet when create new space
    * [SOC-702] - Show wrong in Popup My Space after created new node
    * [SOC-704] - Lose page's title of request page in Manage Page
    * [SOC-707] - Have problem when create new space but delete space template in Manage page
    * [SOC-711] - Lose Information in menu item of User ToolBar Group portlet when have some spaces page
    * [SOC-712] - IE6: Error displaying in Search invited form when don't any user is invited
    * [SOC-715] - Show wrong page(node) of space when click switch view mode in edit node's pagge
    * [SOC-720] - Nothing happen when click Expand All in Sitemap page when add application to Space menu portlet
    * [SOC-721] - Have problem when drag and drop ComplianceTest08 gadget into Dashboard page
    * [SOC-731] - Unknown error when delete appliaction which name the same with existing
    * [SOC-738] - Spaces button in toolbar also lists group pages
    * [SOC-739] - I can only add a single IFame
    * [SOC-754] - Exception when switching view mode on a space navigation's page
    * [SOC-759] - SNF_PRL_13: spaces are listed in people directory
    * [SOC-764] - Activities Gadget does not work elsewhere but localhost:8080
    * [SOC-766] - some label values are missing in socialdemo
    * [SOC-769] - Extracting link thumbnails does not work on social.demo.exoplatform.org
    * [SOC-770] - Unknown error when searching by alphabet on socialdemo
    * [SOC-780] - can't get the provider store in EXoOAuthDataStore
    * [SOC-784] - writing an activity in a space break the activity stream of the space
    * [SOC-785] - Error occurs when renaming a space name

Feedback

    * [SOC-82] - should synchronous data in some portlet
    * [SOC-743] - SNF_PRL_03 fail : nothing gets translated
    * [SOC-745] - look and feel of login page is outdated
    * [SOC-747] - enhance readability of space description
    * [SOC-748] - edit space icon is not easy to understand
    * [SOC-749] - search space should match terms from space description
    * [SOC-750] - space priority is not self explanatory
    * [SOC-752] - Members screen is confusing.
    * [SOC-753] - Space navigation should match navigation order
    * [SOC-756] - "Friends activities" should be renamed "${OWNER} Relations Activtties"
    * [SOC-757] - activities page is too long
    * [SOC-761] - Enhance how empty fields of someone else's profile are displayed
    * [SOC-767] - Allow international phone numbers
    * [SOC-772] - Homescreen text does not reflect current message
    * [SOC-773] - Activities should provide full name + link to profile when a user is mentionned

Improvement

    * [SOC-725] - elastic layout for socialdemo
    * [SOC-735] - comments, attachments and like for space activities
    * [SOC-760] - Space navigation does not display all its sub menu
    * [SOC-774] - Experiences section enhancements
    * [SOC-775] - Focus on others activities on My profile
    * [SOC-776] - Substitute @username constructs in activity titles and bodies

New Feature

    * [SOC-630] - Publish update on edit profile
    * [SOC-668] - Support template message id and template params for activities

Task

    * [SOC-540] - Should support vietnamese in space's name
    * [SOC-597] - cleanup root pom.xml
    * [SOC-687] - social.ear for JBoss
    * [SOC-693] - Rename Invitation/Request listing
    * [SOC-713] - Change config when delete group which is not mandatory in user and group management
    * [SOC-762] - Create and bundle release notes
    * [SOC-763] - Fix the invalid gatein-resources.xml in ExoResourcesSocial
    * [SOC-781] - add the ability to specify a space in an opensocial call by it's name and not only by it's ID
    * [SOC-782] - publishing a space activity using the notation : space:UUID
    * [SOC-783] - french translation


- 1.0.0 CR01

Bug

    * [SOC-42] - fix the compliance bugs
    * [SOC-134] - [space] after move user from space, user still access to portlet of space
    * [SOC-377] - UI problem with 3 columns dashboard
    * [SOC-502] - fix bug internal error when using social demo portal
    * [SOC-536] - Show exception in case select group doesn't have any user when create new space
    * [SOC-548] - Show exception when delete space group in user and group management
    * [SOC-558] - Should show message when click edit space which space was deleted in Space Navigation
    * [SOC-559] - NPE when create new space in special case
    * [SOC-562] - Show wrong Site editor when login portal
    * [SOC-565] - Groovy template exception when add many application in sapce setting
    * [SOC-568] - Bug JS: Show wrong in Application list form
    * [SOC-573] - Fix bug seft remove in space manage
    * [SOC-578] - IE6: Error UI in Select user form
    * [SOC-580] - Show exception in case error accept invitation
    * [SOC-581] - Error is occured when delete user who use to leave space
    * [SOC-590] - NPE when change member to leader
    * [SOC-591] - Still show message can not remove member when delete member in space setting
    * [SOC-613] - NPE when edit space which was deleted
    * [SOC-614] - Nothing happen when click 'like' in my status which was delete
    * [SOC-615] - Don't displaying all filter when search in people and my relation list
    * [SOC-616] - Show exception when delete all comment in Activity
    * [SOC-617] - Exception after deleting all comments
    * [SOC-625] - Can not create new space when delete space's group
    * [SOC-626] - NPE when edit space which have group was deleted
    * [SOC-646] - security of shindig rpc and REST API
    * [SOC-647] - Can't authenticate with oAuth to social's REST and RPC API
    * [SOC-648] - Impossible to login with java5
    * [SOC-650] - the consumer secret is hardcoded for any consumerkey
    * [SOC-689] - Session time out when leave user of space
    * [SOC-710] - Show message when edit basic infor of user with FN/LN has number
    * [SOC-719] - Cannot edit portlet preferences on Edit Page mode inside a space aplpication page
    * [SOC-724] - Space created by root appears empty for john
    * [SOC-730] - Change avatar and profile have unknow error
    * [SOC-733] - John cannot join space created by root
    * [SOC-734] - Status update gadget not compliant with /socialdemo
    * [SOC-741] - user cannot see friends activities
    * [SOC-744] - SNF_PRL_04 : fail page not found error on login

Improvement

    * [SOC-234] - Improvements in Social asked by Benjamin
    * [SOC-243] - Links/URL in the activity stream must be clickable and will open a new page
    * [SOC-602] - Change icon to invite
    * [SOC-603] - tooltips to guide user
    * [SOC-606] - Update navigation name with space name
    * [SOC-609] - Make social SocialDemo more focused
    * [SOC-610] - Rename List users in "members"
    * [SOC-655] - Renaming application should only be available to space admin
    * [SOC-708] - Spaces toolbar menu should list My spaces
    * [SOC-714] - My Relations
    * [SOC-722] - Profile / Relations title should show owner's name

New Feature

    * [SOC-629] - Space activities
    * [SOC-630] - Publish update on edit profile
    * [SOC-645] - Spaces lifecycle listener
    * [SOC-678] - Space activity stream on space home
    * [SOC-727] - People Relations Lifecycle listener
    * [SOC-732] - Profile updates listener

Task

    * [SOC-189] - Please put proper JavaDoc in ALL classes of social
    * [SOC-396] - [profile] should auto reload Basic information after update successfully
    * [SOC-560] - Should have message to alert when space doesn't exits
    * [SOC-569] - Should have 'show' 'hide' feature to expand application in spaces.
    * [SOC-576] - Change copyright info from 2009 to 2010
    * [SOC-598] - rename root pom as "social"
    * [SOC-599] - Add more sniff tests
    * [SOC-604] - do not override /portal UI
    * [SOC-608] - Update the build to generate binary bundles on release
    * [SOC-622] - Write unit test for space, people, and opensocial component.
    * [SOC-652] - Upgrade to GateIn GA
    * [SOC-654] - Allow HTML tag in the activity stream
    * [SOC-658] - verify every gadget shipped with eXo Social
    * [SOC-661] - support identity ID of the form provider:id
    * [SOC-664] - change the dependency version of "exo.portal.component.web" in demo/jar/pom.xml
    * [SOC-666] - Startup severe error
    * [SOC-723] - Profile / Relations should not have Relation sub menu
    * [SOC-728] - Generic lifecycle
    * [SOC-729] - ExoService should be able to target any container

Sub-task

    * [SOC-532] - [DEV] figure out another test case
    * [SOC-533] - [TEST] test empty application in space settings when using demo user.
    * [SOC-600] - Specify more scenarios
    * [SOC-636] - space activities REST API
    * [SOC-637] - space activities UI
