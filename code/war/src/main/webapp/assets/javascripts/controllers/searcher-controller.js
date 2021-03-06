/**
 * The search controller, note that the default is to distribute the
 * search in the cluster. This functionality picks a server at random and
 * executes the code on that server. The cluster needs to be heterogeneous of
 * course.
 *
 * @author Michael Couck
 * @since 10-11-2013
 */
module.controller('SearcherController', function($scope, $http, $timeout, $log) {

	$scope.searchUrl = '/ikube/service/search/json';
	$scope.fieldsUrl = '/ikube/service/monitor/fields';
	$scope.indexesUrl = '/ikube/service/monitor/indexes';
	$scope.searchAllUrl = '/ikube/service/search/json/all';
	$scope.headers = {
		headers : {
			'Content-Type' : 'application/json'
		}
	};

	$scope.path = '/ikube/assets/images/icons/file-types/';
	$scope.icon = getServiceUrl($scope.path + 'txt.png');
	$scope.images = [];
	$scope.testedImages = [];

	$scope.status = 200;
	$scope.search = null;
	$scope.statistics = null;
	$scope.pagination = null;

	$scope.indexes = undefined;
	$scope.indexName = undefined;
	
	// Go to the web service for the results
	$scope.doSearch = function() {
		$scope.url = getServiceUrl($scope.searchUrl);
		var search = $scope.doSearchPreProcessing($scope.search);
		search.searchResults = null;

		var promise = $http.post($scope.url, search);
		promise.success(function(data, status) {
			$scope.status = status;
			$scope.search.searchResults = data.searchResults;
			$scope.doSearchPostProcessing($scope.search);
		});
		promise.error(function(data, status) {
			$scope.status = status;
			$scope.log('Error in doSearch', status);
		});
	};
	

	// This function will search every field in every index, expensive!
	$scope.doSearchAll = function(searchStrings) {
		$scope.status = null;
		$scope.statistics = null;

		$scope.search.sortFields = null;
		$scope.search.coordinate = null;
		$scope.search.indexName = null;
		$scope.search.searchFields = null;
		$scope.search.searchResults = null;
		$scope.search.searchStrings = searchStrings;
		$scope.url = getServiceUrl($scope.searchAllUrl);

		var promise = $http.post($scope.url, $scope.search);
		promise.success(function(data, status) {
			if (!!data.searchStrings && data.searchStrings.toString() == $scope.search.searchStrings.toString()) {
				$scope.status = status;
				$scope.search.searchResults = data.searchResults;
				$scope.doSearchPostProcessing($scope.search);
			} else {
				$scope.log('Old search : ' + data.searchStrings.toString(), status);
			}
		});
		promise.error(function(data, status) {
			$scope.status = status;
			$scope.log('Do search all error', status);
		});
	};

	$scope.doPagedSearch = function(firstResult) {
		$scope.search.firstResult = firstResult;
		if (!!$scope.search.indexName) {
			$scope.doSearch();
		} else {
			$scope.doSearchAll($scope.search.searchStrings);
		}
	};
	
	$scope.doSearchPreProcessing = function(search) {
		$scope.status = null;
		$scope.statistics = null;
		$scope.search.coordinate = null;

		var searchStrings = [];
		var searchFields = [];
		var typeFields = [];
		var occurrenceFields = [];

		for ( var i = 0; i < search.searchStrings.length; i++) {
			var searchString = search.searchStrings[i];
			var searchField = search.searchFields[i];
			if (!!searchString) {
				if ($scope.isNumeric(searchString)) {
					// typeFields.push('numeric');
					if (searchField == 'latitude') {
						$scope.doCoordinate(search, 'latitude', searchString);
					} else if (searchField == 'longitude') {
						$scope.doCoordinate(search, 'longitude', searchString);
					}
				} else {
					searchStrings.push(searchString);
					searchFields.push(searchField);
					var strings = searchString.split('-');
					if (strings.length > 1) {
						typeFields.push('range');
					} else {
						typeFields.push('string');
					}
                    occurrenceFields.push('must');
				}
			}
		}

		var cloneSearch = angular.copy(search);

        cloneSearch.searchStrings = searchStrings;
        cloneSearch.searchFields = searchFields;
        cloneSearch.typeFields = typeFields;
        cloneSearch.occurrenceFields = occurrenceFields;
        cloneSearch.distributed = true;

		return cloneSearch;
	};

	$scope.doCoordinate = function(search, property, value) {
		if (!search.coordinate) {
			search.coordinate = {};
		}
		search.coordinate[property] = value;
	};

	$scope.doSearchPostProcessing = function(search) {
		if (!!search.searchResults && search.searchResults.length > 0) {
			$scope.statistics = search.searchResults.pop();
			// $scope.log('Corrections : ' + $scope.statistics.corrections, 200);
			if (!!$scope.statistics.total && $scope.statistics.total > 0) {
				$scope.doPagination();
			}
			if (!!search.coordinate && !!search.coordinate.latitude && !!search.coordinate.longitude) {
				$scope.doMarkers();
			}
			if (!!$scope.statistics && (!$scope.statistics.corrections || $scope.statistics.corrections.length == 0)) {
				$scope.statistics.corrections = null;
			}
			$scope.doApply = function() {
				return $timeout(function() {
					$scope.$apply();
				}, 100);
			};
			$scope.doApply();
		}
	};

	$scope.doFileTypeImage = function(identifier, mimeType) {
		if (!identifier && !mimeType) {
			return $scope.icon;
		}
		var extension;
		if (!!mimeType) {
			extension = mimeType.substr((~-mimeType.lastIndexOf("/") >>> 0) + 2);
		} else {
			extension = identifier.substr((~-identifier.lastIndexOf(".") >>> 0) + 2);
		}
		if (!extension) {
			return $scope.icon;
		}
		var stringBuilder = [];
		stringBuilder.push($scope.path);
		stringBuilder.push(extension);
		stringBuilder.push('.png');
		var iconUrl = getServiceUrl(stringBuilder.join(''));

		// Verify that the image exists
		if ($scope.images.indexOf(iconUrl) > -1) {
			return iconUrl;
		} else {
			if ($scope.testedImages.indexOf(iconUrl) < 0) {
				$scope.testedImages.push(iconUrl);
				var retries = 5;
				var iconStatus = null;
				var promise = $http.get(iconUrl);
				promise.success(function(data, status) {
					iconStatus = status;
				});
				promise.error(function(data, status) {
					iconStatus = status;
				});
				$scope.checkImage = function() {
					return $timeout(function() {
						// $scope.log('Get icon : ' + iconUrl, iconStatus);
						if (!!iconStatus) {
							if (iconStatus === 200) {
								$scope.images.push(iconUrl);
								return iconUrl;
							}
							return iconUrl;
						} else if (retries-- > 0) {
							return $scope.checkImage();
						} else {
							return $scope.icon;
						}
					}, 100);
				};
				return $scope.checkImage();
			}
		}
		return $scope.icon;
	};

	$scope.log = function(message, status) {
		$log.log(message + ':' + status);
	};

	// This function resets the search and nulls the statistics and pagination
	$scope.resetSearch = function() {
		$scope.search = {
			indexName : null,
			searchStrings : [],
			sortFields : [],
			fragment : true,
			firstResult : 0,
			maxResults : 10,
			distance : 20,
			coordinate : null
		};
		$scope.statistics = {};
		$scope.pagination = null;
	};
	$scope.resetSearch();

	// This is called by the sub controller(s), probably the type ahead
	// controller when the user has selected a search string
	$scope.$on('doSearch', function(event, search) {
		$scope.search.firstResult = 0;
		$scope.doSearchAll(search.searchStrings);
	});

	// Watch the index name and get the search fields
	$scope.$watch('search.indexName', function() {
		$scope.waitForDigest = function() {
			return $timeout(function() {
				$scope.search.searchResults = null;
				$scope.search.firstResult = 0;
				if (!!$scope.search.indexName) {
					$scope.doFields($scope.search.indexName);
					$scope.$apply();
				}
			}, 100);
		};
		$scope.waitForDigest();
	}, true);

	// This function gets all the index names from the server
	$scope.doIndexes = function() {
		var url = getServiceUrl($scope.indexesUrl);
		var promise = $http.get(url);
		promise.success(function(data, status) {
			// $scope.log('Status', status);
			$scope.indexes = data;
		});
		promise.error(function(data, status) {
			$scope.log('Status', status);
			// alert('Data : ' + data + ', status : ' + status);
		});
	};
	$scope.doIndexes();

	// Get the fields for the index
	$scope.doFields = function(indexName) {
		$scope.url = getServiceUrl($scope.fieldsUrl);
		$scope.parameters = {
			indexName : indexName
		};
		$scope.config = {
			params : $scope.parameters
		};
		var promise = $http.get($scope.url, $scope.config);
		promise.success(function(data, status) {
			$scope.log('Status', status);
			$scope.search.searchFields = data;
		});
		promise.error(function(data, status) {
			$scope.log('Status', status);
		});

		var maxRetries = 50;
		$scope.wait = function() {
			return $timeout(function() {
				if (maxRetries-- > 0 && !!$scope.search.searchFields) {
					$scope.search.searchStrings = new Array($scope.search.searchFields.length);
					$scope.search.typeFields = new Array($scope.search.searchFields.length);
					return $scope.search.searchFields;
				}
				return $scope.wait();
			}, 100);
		};
		return $scope.wait();
	};

	// Creates the Json pagination array for the next pages in the search
	$scope.doPagination = function() {
		$scope.pagination = [];
		// We just started a search and got the first results
		var pages = $scope.statistics.total / $scope.search.maxResults;
		// Create one 'page' for each block of results
		for ( var i = 0; i < pages && i < 10; i++) {
			var firstResult = i * $scope.search.maxResults;
			$scope.pagination[i] = {
				page : i,
				firstResult : firstResult
			};
		}
		// Find the 'to' result being displayed
		var modulo = $scope.statistics.total % $scope.search.maxResults;
		$scope.search.endResult = $scope.search.firstResult + modulo == $scope.statistics.total ? $scope.statistics.total
				: $scope.search.firstResult + parseInt($scope.search.maxResults, 10);
	};

	// This function will put the markers on the map
	$scope.doMarkers = function() {
		var latitude = $scope.search.coordinate.latitude;
		var longitude = $scope.search.coordinate.longitude;
		var origin = new google.maps.LatLng(latitude, longitude);
		var mapElement = document.getElementById('map_canvas');
		var options = {
			zoom : 13,
			center : new google.maps.LatLng(latitude, longitude),
			mapTypeId : google.maps.MapTypeId.ROADMAP
		};
		var map = new google.maps.Map(mapElement, options);
		// Add the point or origin marker
		new google.maps.Marker({
			map : map,
			position : origin,
			title : 'You are here :) => [' + latitude + ', ' + longitude + ']',
			icon : '/ikube/assets/images/icons/person_obj.gif'
		});
		if (!!$scope.search.searchResults) {
			angular.forEach($scope.search.searchResults, function(key, value) {
				var latitude = key['latitude'];
				var longitude = key['longitude'];
				var fragment = key['fragment'];
				var distance = key['distance'];
				if (!!latitude && !!longitude) {
					pointMarker = new google.maps.Marker({
						map : map,
						draggable: true,
						icon: getServiceUrl('/ikube/assets/images/icons/person_obj.gif'),
						position : new google.maps.LatLng(latitude, longitude),
						title : 'Name : ' + fragment + ', distance : ' + distance
					});
				}
			});
		}
		// And finally set the waypoints
		$scope.doWaypoints(origin, map);
	};

	// This function will put the way points on the map
	$scope.doWaypoints = function(origin, map) {
		var waypoints = [];
		var destination = origin;
		var maxWaypoints = 8;

		angular.forEach($scope.search.searchResults, function(key, value) {
			var latitude = key['latitude'];
			var longitude = key['longitude'];
			if (!!latitude && !!longitude) {
				if (waypoints.length < maxWaypoints) {
					var waypoint = new google.maps.LatLng(latitude, longitude);
					waypoints.push({
						location : waypoint
					});
					destination = waypoint;
				}
			}
		});

		var rendererOptions = {
			map : map
		};
		var directionsDisplay = new google.maps.DirectionsRenderer(rendererOptions);
		var request = {
			origin : origin,
			destination : destination,
			waypoints : waypoints,
			optimizeWaypoints : true,
			travelMode : google.maps.TravelMode.DRIVING,
			unitSystem : google.maps.UnitSystem.METRIC
		};
		var directionsService = new google.maps.DirectionsService();
		directionsService.route(request, function(result, status) {
			$scope.log('Status', status);
			if (status == google.maps.DirectionsStatus.OK) {
				directionsDisplay.setDirections(result);
			}
		});
	};

	$scope.alert = function(name) {
		alert(name);
	};

	$scope.isNumeric = function(string) {
		return !isNaN(string);
	};

});