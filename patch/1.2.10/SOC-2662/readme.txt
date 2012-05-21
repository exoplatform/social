SOC-2662: [IE-Activity Stream Gadget] Can not post activity

What is the problem to fix?
Exception raised when posting status:

java.lang.RuntimeException: java.lang.IllegalArgumentException: Invalid format: "{}"
at org.apache.shindig.protocol.conversion.BeanJsonConverter.convertToClass(BeanJsonConverter.java:271)
...........................
Caused by: java.lang.IllegalArgumentException: Invalid format: "{}"
at org.joda.time.format.DateTimeFormatter.parseMillis(DateTimeFormatter.java:634)
at org.joda.time.convert.StringConverter.getInstantMillis(StringConverter.java:65)
at org.joda.time.base.BaseDateTime.<init>(BaseDateTime.java:171)
at org.joda.time.DateTime.<init>(DateTime.java:168)
at org.apache.shindig.protocol.conversion.BeanJsonConverter.convertToObject(BeanJsonConverter.java:163)
at org.apache.shindig.protocol.conversion.BeanJsonConverter.convertToClass(BeanJsonConverter.java:269)


Problem analysis
- Error fired when convert an object that contain Datetime object.

How is the problem fixed?
- Since we let client manipulate date time in displaying then should remove this param in activity part.

Reproduction test
- Open PLF 3.5.4 on Internet Explorer
- Login social intranet as John
- Go to Settings > Applications 
- Click Import Applications
- Go to Dashboard
- Add Activity Stream gadget
- Add new activity stream
