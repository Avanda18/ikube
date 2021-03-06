<%-- <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<% response.setHeader("Access-Control-Allow-Origin", "*"); %>

<script type="text/javascript">

	// The global map
	var map = null;
	
	// The controller that does the search
	module.controller('GeospatialSearcherController', function($http, $scope) {
		
		// The model data that we bind to in the form
		$scope.allWords = 'hotel'; // Default is hotel
		$scope.latitude = '-33.9693580'; // Default is cape town
		$scope.longitude = '18.4622110'; // Default is cape town
		$scope.distance = '20'; // Distance from point of origin
		$scope.pageBlock = 10; // Only results per page
		$scope.geospatial = false;
		$scope.endResult = 0;

		$scope.statistics = {};
		$scope.pagination = []

		// The form parameters we send to the server
		$scope.searchParameters = { 
			indexName : 'geospatial', // The default is the geospatial index
			searchStrings : $scope.allWords,
			fragment : true,
			firstResult : 0,
			maxResults : $scope.pageBlock
		};
		
		// The configuration for the request to the server for the results
		$scope.config = { params : $scope.searchParameters };
		
		// Go to the web service for the results
		$scope.doSearch = function() {
			// Geospatial search
			$scope.url = getServiceUrl('/ikube/service/search/json/multi/spatial/all');
			$scope.searchParameters['searchStrings'] = $scope.allWords;
			$scope.searchParameters['distance'] = $scope.distance;
			$scope.searchParameters['latitude'] = $scope.latitude;
			$scope.searchParameters['longitude'] = $scope.longitude;
			var promise = $http.get($scope.url, $scope.config);
			promise.success(function(data, status) {
				// Pop the statistics Json off the array
				$scope.data = data;
				$scope.status = status;
				$scope.statistics = $scope.data.pop();
				$scope.doPagination($scope.data);
				$scope.doMarkers();
			});
			promise.error(function(data, status) {
				$scope.status = status;
			});
		};
		
		// Sets the first result based on the pagination page requested
		$scope.doFirstResult = function(firstResult) {
			$scope.searchParameters.firstResult = firstResult;
		}
		
		// Creates the Json pagination array for the next pages in the search
		$scope.doPagination = function(data) {
			$scope.pagination = [];
			var total = $scope.statistics.total;
			// Exception or no results
			if (total == null || total == 0) {
				$scope.searchParameters.firstResult = 0;
				$scope.endResult = 0;
				return;
			}
			// We just started a search and got the first results
			var pages = total / $scope.pageBlock;
			// Create one 'page' for each block of results
			for (var i = 0; i < pages && i < $scope.pageBlock; i++) {
				var firstResult = i * $scope.pageBlock;
				var active = firstResult == $scope.searchParameters.firstResult ? 'black' : 'blue';
				$scope.pagination[i] = { page : i, firstResult : firstResult, active : active };
			};
			// Find the 'to' result being displayed
			var modulo = total % $scope.pageBlock;
			$scope.endResult = $scope.searchParameters.firstResult + modulo == total ? total : $scope.searchParameters.firstResult + $scope.pageBlock;
		}
		
		// This function will put the markers on the map
		$scope.doMarkers = function() {
			var latitude = $scope.searchParameters.latitude;
			var longitude = $scope.searchParameters.longitude;
			var origin = new google.maps.LatLng(latitude, longitude);
			var mapElement = document.getElementById('map_canvas');
			var options = {
				zoom: 13,
				center: new google.maps.LatLng(latitude, longitude),
				mapTypeId: google.maps.MapTypeId.ROADMAP
			};
			map = new google.maps.Map(mapElement, options);
			// Add the point or origin marker
			var marker = new google.maps.Marker({
				map : map,
				position: origin,
				title : 'You are here :) => [' + latitude + ', ' + longitude + ']',
				icon: '/ikube/img/icons/center_pin.png'
			});
			for (var key in $scope.data) {
				var datum = $scope.data[key];
				if (datum.latitude != null && datum.longitude) {
					pointMarker = new google.maps.Marker({
						map : map,
						position: new google.maps.LatLng(datum.latitude, datum.longitude),
						title : 'Name : ' + datum.name + ', distance : ' + datum.distance
					});
				}
			}
			// And finally set the waypoints
			$scope.doWaypoints(origin);
		}
		
		// This function will put the way points on the map
		$scope.doWaypoints = function(origin) {
			var waypoints = [];
			var destination = origin;
			var maxWaypoints = 8;
			for (var key in $scope.data) {
				var waypoint = new google.maps.LatLng($scope.data[key].latitude, $scope.data[key].longitude);
				waypoints.push({ location: waypoint });
				destination = waypoint;
				if (waypoints.length >= maxWaypoints) {
					break;
				}
			}
			var rendererOptions = { map: map };
			var directionsDisplay = new google.maps.DirectionsRenderer(rendererOptions);
			var request = {
					origin: origin,
					destination: destination,
					waypoints: waypoints,
					optimizeWaypoints : true,
					travelMode: google.maps.TravelMode.DRIVING,
					unitSystem: google.maps.UnitSystem.METRIC
			};
			var directionsService = new google.maps.DirectionsService();
			directionsService.route(request, 
				function(response, status) {
					if (status == google.maps.DirectionsStatus.OK) {
						directionsDisplay.setDirections(response);
					} else {
						alert ('Failed to get directions from Googy, sorry : ' + status);
					}
				}
			);
		}
	});
	
	/** This directive will just init the map and put it on the page. */
	module.directive('googleMap', function() {
		return {
			restrict : 'A',
			compile : function($tElement, $tAttributes, $transclude) {
				return function($scope, $element, $attributes) {
					$scope.$watch($tAttributes.event, function(value) {
						var latitude = -33.9693580;
						var longitude = 18.4622110;
						var mapElement = document.getElementById('map_canvas');
						var options = {
							zoom: 13,
							center: new google.maps.LatLng(latitude, longitude),
							mapTypeId: google.maps.MapTypeId.ROADMAP
						};
						map = new google.maps.Map(mapElement, options);
					});
				}
			},
			controller : function($scope, $element, $location) {
				// Put something here?
			}
		};
	});
</script>

<table ng-app="ikube" ng-controller="GeospatialSearcherController" style="border : 1px solid #aaaaaa;" width="100%">
	<tr>
		<th colspan="3"><img src="<c:url value="/img/icons/launch_run.gif" />">&nbsp;Geospatial search</th>
	</tr>
	
	<tr class="odd" nowrap="nowrap" valign="bottom">
		<td><b>Collection:</b></td>
		<td>
			<select ng-controller="IndexesController" ng-model="searchParameters.indexName">
				<option ng-repeat="index in indexes" value="{{index}}">{{index}}</option>
			</select>
		</td>
					
		<td width="340px" align="left" rowspan="6">
			<div id="map_canvas" google-map style="height: 340px; width: 550px; border : 1px solid black;"></div>
		</td>
					
	</tr>
	<tr class="even" nowrap="nowrap" valign="bottom">
		<td><b>All of these words:</b></td>
		<td><input id="allWords" name="allWords" ng-model="allWords"></td>
	</tr>
	<tr class="odd" nowrap="nowrap" valign="bottom">
		<td><b>Latitude:</b></td>
		<td><input id="latitude" ng-model="latitude" placeholder="latitude"></td>
	</tr>
	<tr class="even" nowrap="nowrap" valign="bottom">
		<td><b>Longitude:</b></td>
		<td><input id="longitude" ng-model="longitude" placeholder="longitude"></td>
	</tr>
	<tr class="odd" nowrap="nowrap" valign="bottom">
		<td><b>Distance:</b></td>
		<td><input id="distance" ng-model="distance" placeholder="distance"></td>
	</tr>
	<tr class="even" nowrap="nowrap" valign="top">
		<td colspan="2">
			<input type="submit" ng-click="doSearch();" value="Go!">
		</td>
	</tr>
	
	<tr><td colspan="2">&nbsp;</td></tr>
	
	<tr nowrap="nowrap" valign="bottom">
		<td colspan="3">
			Showing results '{{searchParameters.firstResult}} 
			to {{endResult}} 
			of {{statistics.total}}' 
			for search '{{searchParameters.searchStrings}}', 
			corrections : {{statistics.corrections}}, 
			duration : {{statistics.duration}}</td>
	</tr>
	
	<tr nowrap="nowrap" valign="bottom">
		<td colspan="3" nowrap="nowrap">
			<span ng-repeat="page in pagination">
				<a style="font-color : {{page.active}}" href="#" ng-click="
					doFirstResult(page.firstResult);
					doSearch();">{{page.page}}</a>
			</span>
		</td>
	</tr>
	
	<tr><td colspan="3">&nbsp;</td></tr>
	
	<tr ng-repeat="datum in data" ng-class-odd="'odd'" ng-class-even="'even'">
		<td colspan="3">
			<span ng-hide="!datum.id"><b>Identifier</b> : {{datum.id}}<br></span> 
			<b>Score</b> : {{datum.score}}<br>
			<b>Fragment</b> : <span ng-bind-html-unsafe="datum.fragment"></span><br>
			<span ng-hide="!datum.latitude"><b>Latitude</b> : {{datum.latitude}}<br></span>
			<span ng-hide="!datum.longitude"><b>Longitude</b> : {{datum.longitude}}<br></span>
			<span ng-hide="!datum.distance"><b>Distance</b> : {{datum.distance}}<br></span>
			<span ng-hide="!datum.path"><b>Path</b> : {{datum.path}}<br></span>
			<br>
		</td>
	</tr>
	
	<tr><td colspan="3">&nbsp;</td></tr>
	
	<tr>
		<td colspan="3" nowrap="nowrap">
			<span ng-repeat="page in pagination">
				<a style="font-color : {{page.active}}" href="#" 
					ng-click="doFirstResult(page.firstResult);doSearch();">{{page.page}}</a>
			</span>
		</td>
	</tr>
	
</table> --%>