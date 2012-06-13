SOC-2686: Problem of UpgradeToMOSPlugin in migration from PLF 3.0.x to PLF 3.5.x

What is the problem to fix?
On following the documentation of migration, in order to migrate PLF tomcat server from 3.0.9 to 3.5.3, we do following steps:
- Log in to PLF 3.0.9 tomcat
- Create a space named testspace then add to it an event, a document and post a subject in forum
- Configure PLF 3.5.3 tomcat as mentionned in the documentation above
- Start PLF 3.5.3 server to migration
Problem: The migration process cannot continue if UpgradeToMOSPlugin is enable due to

...
Write profile space/testspace
Exception in thread "Thread-52" org.exoplatform.social.extras.migration.MigrationException: javax.jcr.PathNotFoundException: Node not found /exo:applications/Social_Activity/organization
        at org.exoplatform.social.extras.migration.rw.NodeReader_11x_12x$ActivityRunnable.run(NodeReader_11x_12x.java:243)
        at java.lang.Thread.run(Thread.java:662)
Caused by: javax.jcr.PathNotFoundException: Node not found /exo:applications/Social_Activity/organization
        at org.exoplatform.services.jcr.impl.core.NodeImpl.getNode(NodeImpl.java:1025)
        at org.exoplatform.social.extras.migration.rw.NodeReader_11x_12x$ActivityRunnable.run(NodeReader_11x_12x.java:194)



Problem analysis
Its a special case since created spaces have no activity then the node "/exo:applications/Social_Activity/organization" not exists.
Maybe because there is less data and this node is not existing yet. We handle this issue skiping activity migration if this intermediate node is missing.



How is the problem fixed?
Check to ignore migrating in case of node has not existed as code below:
........
if (!rootNode.hasNode("/exo:applications/Social_Activity/organization")) {
  os.close();
 return;
}

// Migrating codes go here
........

We cover all cases like this also with check if migrated node exist or not.
