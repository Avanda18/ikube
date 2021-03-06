<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

	3) Add the configuration to the instance: Assuming that you have already followed the quick start guide, and that the server is up and running? If not please 
	do this, shouldn't take more than a few minutes, <a href="<c:url value="/quick.html" />">quick start</a>.
	
	Place the configuration file that you have just created in the Ikube folder ($TOMCAT_INSTALL/bin/ikube). This folder is in the bin directory of the Tomcat, or if you are using another server it should be in the directory where 
	the startup script is. Ikube will look for the configuration in the user directory first, this is the directory where you start the server, i.e. the 'bin' directory in Tomcat and JBoss. Start the server, either ./startup.sh or startup.bat.
	<br><br>
	
	Ikube will start indexing the data you specified, in about five minutes (this is the default delay before indexing, this can be changed of course). 
	Note that depending on the volume of data, this could take some time, and you can monitor 
	the progress of the indexing process in the user interface (<a href="http://localhost:8080/ikube">user interface</a>).