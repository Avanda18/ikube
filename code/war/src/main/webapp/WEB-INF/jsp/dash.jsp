<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<table width="100%">
	<tr>
		<td valign="top">
			<!-- Servers data -->
			<table ng-controller="ServersController">
				<tr ng-repeat="server in servers">
					<td style="border : 1px solid #aaaaaa; padding : 5px;" nowrap="nowrap" valign="top">
						<img src="<c:url value="/images/icons/open.gif" />">&nbsp;<b>Cpu load</b> : {{server.averageCpuLoad}} <br>
						<img src="<c:url value="/images/icons/open.gif" />">&nbsp;<b>Free memory</b> : {{server.freeMemory}} <br>
						<img src="<c:url value="/images/icons/open.gif" />">&nbsp;<b>Max memory</b> : {{server.maxMemory}} <br>
						<img src="<c:url value="/images/icons/open.gif" />">&nbsp;<b>Total memory</b> : {{server.totalMemory}} <br>
						<img src="<c:url value="/images/icons/open.gif" />">&nbsp;<b>Free disk space</b> : {{server.freeDiskSpace}} <br>
						<img src="<c:url value="/images/icons/web.gif" />">&nbsp;<b>Address</b> : {{server.address}} <br>
						<img src="<c:url value="/images/icons/index_performance.gif" />">&nbsp;<b>Processors</b> : {{server.processors}} <br>
						<img src="<c:url value="/images/icons/memory_view.gif" />">&nbsp;<b>Architecture</b> : {{server.architecture}} <br>
						<img src="<c:url value="/images/icons/server.gif" />">&nbsp;<b>Age</b> : {{server.age}} <br>
					</td>
				</tr>
			</table>
		</td>
		
		<td valign="top" width="80%">
			
			<!-- Performance graphs -->
			<table id="performance-graphs-table" name="performance-graphs-table" width="100%">
				<tr>
					<td>
						<div indexing><!-- The indexing performance graph --></div>
					</td>
				</tr>
				<tr>
					<td>
						<div searching><!-- Searching performance graph --></div>
					</td>
				</tr>
			</table>
			
			<table ng-controller="ActionsController" width="100%">
				<tr>
					<th><img src="<c:url value="/images/icons/server.gif" />">&nbsp;Server</th>
					<th><img src="<c:url value="/images/icons/jar_l_obj.gif" />">&nbsp;Action</th>
					<th><img src="<c:url value="/images/icons/index.gif" />">&nbsp;Index</th>
					<th><img src="<c:url value="/images/icons/run_on_server.gif" />">&nbsp;Indexable</th>
					<th><img src="<c:url value="/images/icons/link_obj.gif" />">&nbsp;Per minute</th>
					<th><img src="<c:url value="/images/icons/link_obj.gif" />">&nbsp;Total docs</th>
					<th><img src="<c:url value="/images/icons/launch_run.gif" />">&nbsp;Start time</th>
					<th><img src="<c:url value="/images/icons/relaunch.gif" />">&nbsp;Functions</th>
				</tr>
				<tr ng-repeat="action in actions">
					<td>{{action.server}}</td>
					<td>{{action.actionName}}</td>
					<td>{{action.indexName}}</td>
					<td>{{action.indexableName}}</td>
					<td>{{action.docsPerMinute}}</td>
					<td>{{action.totalDocsIndexed}}</td>
					<td>{{action.startTime}}</td>
					<td>
						<a style="font-color : red;" href="#" ng-click="terminateIndexing(action.indexName);">Terrminate</a>
					</td>
				</tr>
			</table>
			
		</td>
	</tr>
</table>