Summary

    * Status: Attach link function doesn't work properly:Exception with some special links
    * CCP Issue: CCP-956, Product Jira Issue: SOC-1772.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?
How to reproduce this problem:

    * Connect ad root under SOC 1.1.x.
    * Create a new space and add an activity.
    * Attach this link http://tinyurl.com/635lr75 to the activity .
    * Unknown error
      ?
      GRAVE: Error during the processAction phase
      java.lang.StringIndexOutOfBoundsException: String index out of range: 499
          at java.lang.String.substring(String.java:1934)
          at org.exoplatform.social.service.rest.LinkShare.getInstance(LinkShare.java:374)
          at org.exoplatform.social.service.rest.LinkShare.getInstance(LinkShare.java:321)
          at org.exoplatform.social.plugin.link.UILinkActivityComposer.setLink(UILinkActivityComposer.java:119)
          at org.exoplatform.social.plugin.link.UILinkActivityComposer.access$000(UILinkActivityComposer.java:68)
          at org.exoplatform.social.plugin.link.UILinkActivityComposer$AttachActionListener.execute(UILinkActivityComposer.java:153)
          at org.exoplatform.webui.event.Event.broadcast(Event.java:89)
          at org.exoplatform.webui.core.lifecycle.Lifecycle.processAction(Lifecycle.java:56)
          at org.exoplatform.webui.core.UIComponent.processAction(UIComponent.java:133)
          at org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle.processAction(UIApplicationLifecycle.java:58)
          at org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle.processAction(UIApplicationLifecycle.java:31)
          at org.exoplatform.webui.core.UIComponent.processAction(UIComponent.java:133)
          at org.exoplatform.webui.core.UIApplication.processAction(UIApplication.java:120)
          at org.exoplatform.webui.application.portlet.PortletApplication.processAction(PortletApplication.java:168)
          at org.exoplatform.webui.application.portlet.PortletApplicationController.processAction(PortletApplicationController.java:80)
          at org.gatein.pc.portlet.impl.jsr168.PortletContainerImpl$Invoker.doFilter(PortletContainerImpl.java:558)
          at org.gatein.pc.portlet.impl.jsr168.api.FilterChainImpl.doFilter(FilterChainImpl.java:109)
          at org.gatein.pc.portlet.impl.jsr168.api.FilterChainImpl.doFilter(FilterChainImpl.java:72)
          at org.gatein.pc.portlet.impl.jsr168.PortletContainerImpl.dispatch(PortletContainerImpl.java:506)
          at org.gatein.pc.portlet.container.ContainerPortletDispatcher.invoke(ContainerPortletDispatcher.java:42)
          at org.gatein.pc.portlet.PortletInvokerInterceptor.invoke(PortletInvokerInterceptor.java:89)
          at org.gatein.pc.portlet.aspects.EventPayloadInterceptor.invoke(EventPayloadInterceptor.java:197)
          at org.gatein.pc.portlet.PortletInvokerInterceptor.invoke(PortletInvokerInterceptor.java:89)
          at org.gatein.pc.portlet.aspects.RequestAttributeConversationInterceptor.invoke(RequestAttributeConversationInterceptor.java:119)

Fix description

How is the problem fixed?
With this case, we should put substring the description string in if clause for sure the length of the string is greater than 500 escape StringIndexOutOfBoundsException error.

Patch files: SOC-1772.patch

Tests to perform

Reproduction test
*

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
*PL review: Patch validated

Support Comment
* Support review: Patch validated

QA Feedbacks
*

