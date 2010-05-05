==============================================
    Release Notes - exo-social - Version 1.0.0 GA
==============================================

===============
1 Introduction
===============

eXo Social is composed of 2 modules :

    * eXo People: brings Enterprise Social Networking to your work and allows you to organize your workforce in an efficient way through a better understanding of your people skills.
    		- Turn your directory into a social network
				- Users can fill their profile
				- Search by skills and experience
				- Activity status updates
				- Opensocial API support
			
    * eXo Spaces: add communities to your work and enable a collaborative work. It's the communities workgroups.
	    	- Let dormant communities reveal themselves
				- Open community management
				- Activity streams to aggregate knowledge
				- Easily deploy custom applications
				- User friendly interface 


=============
2 What's new?
=============


* French translation
* Enhanced Space Activities
* Several usability enhancements
              
          
    * Find the latest release notes here : http://wiki.exoplatform.com/xwiki/bin/view/Social/Release+Notes            
          
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
