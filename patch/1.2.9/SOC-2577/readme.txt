SOC-2577: Groovy template exception in social during TC PLF-3.5.2

What is the problem to fix?
During the PLF 3.5.2 TC, We get exception in social context. According to the stacktrace, the problem occurs when convert the following message in org.exoplatform.social.webui.activity.plugin.UIRelationshipActivity.getActivityTitle method:
___________________________________________________________________________________________________________________
return ResourceBundleUtil.               replaceArguments(ctx.appRes("UIRelationshipActivity.msg.UserName_Invited_UserName_To_Connect"),
                                                 new String[] { senderLink, receiverLink });
___________________________________________________________________________________________________________________
An StringIndexOutOfBoundsException exception is rised in message process. 
We are using PLF_PERF_04_SocialRead-3.5.x a social bench script, with 30 VU with plf-3.5.x-datasets-3.5.x02-ds3 described in the link.

Problem analysis
Due to the fact that MessageFormat is not thread-safe.

How is the problem fixed?
Use ThreadLocale for MessageFormat in ResourceBundleUtil.
