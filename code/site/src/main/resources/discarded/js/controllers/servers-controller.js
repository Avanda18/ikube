/** This controller will get the server data from the grid. */
module.controller('ServersController', function($http, $scope, databaseService) {
	$scope.server;
	$scope.servers = [];
	$scope.entities = undefined;
	
	$scope.refreshServer = function() {
		$scope.url = getServiceUrl('/ikube/service/monitor/server');
		var promise = $http.get($scope.url);
		promise.success(function(data, status) {
			$scope.server = data;
			$scope.status = status;
		});
		promise.error(function(data, status) {
			$scope.status = status;
		});
	}
	$scope.refreshServer();
	setInterval(function() {
		$scope.refreshServer();
	}, refreshInterval);
	
	$scope.refreshServers = function() {
		$scope.url = getServiceUrl('/ikube/service/monitor/servers');
		var promise = $http.get($scope.url);
		promise.success(function(data, status) {
			$scope.servers = data;
			$scope.status = status;
		});
		promise.error(function(data, status) {
			$scope.status = status;
		});
	}
	$scope.refreshServers();
	setInterval(function() {
		$scope.refreshServers();
	}, refreshInterval);
	
	$scope.startupAll = function() {
		if (confirm('Re-start all schedules and actions in the cluster : ')) {
			$scope.url = getServiceUrl('/ikube/service/monitor/startup-all');
			$scope.parameters = {};
			// The configuration for the request to the server
			$scope.config = { params : $scope.parameters };
			// And start all the schedules again
			var promise = $http.get($scope.url, $scope.config);
			promise.success(function(data, status) {
				$scope.status = status;
			});
			promise.error(function(data, status) {
				$scope.status = status;
			});
		}
		
		$scope.direction = true;
		$scope.orderProp = "address";
		$scope.sort = function(column) {
			if ($scope.orderProp === column) {
				$scope.direction = !$scope.direction;
			} else {
				$scope.orderProp = column;
				$scope.direction = true;
			}
		};
	}
	
	$scope.terminateAll = function() {
		$scope.url = getServiceUrl('/ikube/service/monitor/terminate-all');
		$scope.parameters = {};
		// The configuration for the request to the server
		$scope.config = { params : $scope.parameters };
		// And terminate the schedules in the cluster
		var promise = $http.get($scope.url, $scope.config);
		promise.success(function(data, status) {
			$scope.status = status;
		});
		promise.error(function(data, status) {
			$scope.status = status;
		});
	}
	
	$scope.date = function(millis) {
		return new Date(millis).toLocaleTimeString();
	};
	
	$scope.toggleSchedules = function() {
		if ($scope.server.threadsRunning) {
			$scope.terminateAll();
		} else {
			$scope.startupAll();
		}
		$scope.refreshServer();
	};
	
	$scope.toggleCpuThrottling = function() {
		$scope.url = getServiceUrl('/ikube/service/monitor/cpu-throttling');
		$scope.parameters = {};
		// The configuration for the request to the server
		$scope.config = { params : $scope.parameters };
		// And terminate the schedules in the cluster
		var promise = $http.get($scope.url, $scope.config);
		promise.success(function(data, status) {
			$scope.status = status;
		});
		promise.error(function(data, status) {
			$scope.status = status;
		});
		$scope.refreshServer();
	}
	
	$scope.cpuLoadTooHigh = function(server) {
		return server.averageCpuLoad / server.processors > 0.9;
	}
	
	$scope.entities = databaseService.getEntities('ikube.model.Search', '0', '10');
	
});