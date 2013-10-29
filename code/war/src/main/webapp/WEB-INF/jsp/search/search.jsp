<%@ page errorPage="/WEB-INF/jsp/error.jsp"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<div class="container-fluid">
	<div class="row-fluid">
		<div class="span4">
			<div class="tabbable black-box" style="margin-bottom: 18px;">
				<div class="tab-header">
					Search indexes and options
					<span class="pull-right">
						<span class="options">
							<div class="btn-group">
								<a class="dropdown-toggle" data-toggle="dropdown">
									<i class="icon-cog"></i>
								</a>
								<ul class="dropdown-menu black-box-dropdown dropdown-left">
									<li><a href="#">Show images</a></li>
									<li><a href="#">Toggle instant search</a></li>
									<li><a href="#">Toggle table or list for results</a></li>
									<li class="divider"></li>
									<li><a href="#">Separated link</a></li>
								</ul>
							</div>
					</span>
					</span>
				</div>
				<ul class="nav nav-tabs">
					<li class="active"><a href="#tab1" data-toggle="tab">Indexes</a></li>
					<li class=""><a href="#tab2" data-toggle="tab">Search</a></li>
					<li class=""><a href="#tab3" data-toggle="tab">Geospatial</a></li>
				</ul>
				<div class="tab-content">
					<div class="tab-pane active" id="tab1" ng-controller="IndexesController">
						<div class="padded">
							<div class="inner-well clearfix" ng-repeat="index in indexes">
								<b>{{index}}</b>
								<div class="pull-right">
									<input type="checkbox" id="{{index}}" class="checky" />
									<label for="{{index}}" class="checky"><span></span></label>
								</div>
							</div>
						</div>
					</div>
					<div class="tab-pane" id="tab2">
						<div class="padded">
							<p>
								<span style="display: inline-block; min-width: 150px;">badge
									color gray</span> <span class="badge gray">1</span>
							</p>
							<p>
								<span style="display: inline-block; min-width: 150px;">badge
									color green</span> <span class="badge green">1</span>
							</p>
						</div>
					</div>
					<div class="tab-pane" id="tab3">
						<div class="padded">
							<p>
								<span style="display: inline-block; min-width: 150px;">badge
									color blue</span> <span class="badge blue">1</span>
							</p>
							<p>
								<span style="display: inline-block; min-width: 150px;">badge
									color gray</span> <span class="badge gray">1</span>
							</p>
						</div>
					</div>
				</div>
			</div>
		</div>
		
		<div class="span8">
			<div class="black-box tex">
				<div class="tab-header">Search results</div>
				<ul class="recent-comments">
					<li class="separator">
						<div class="avatar pull-left">
							<img src="<c:url value="/assets/images/MYP_1376-small.jpg" />" />
						</div>
						<div class="article-post">
							<div class="user-info">Posted by jordan, 3 days ago</div>
							<div class="user-content">Vivamus sed auctor nibh congue,
								ligula vitae tempus pharetra... Vivamus sed auctor nibh congue,
								ligula vitae tempus pharetra... Vivamus sed auctor nibh congue,
								ligula vitae tempus pharetra...</div>
							<div class="btn-group">
								<button class="button black mini">
									<i class="icon-pencil"></i> Edit
								</button>
								<button class="button black mini">
									<i class="icon-remove"></i> Delete
								</button>
								<button class="button black mini">
									<i class="icon-ok"></i> Approve
								</button>
							</div>
						</div>
					</li>
					<li class="separator">
						<div class="avatar pull-left">
							<img src="<c:url value="/assets/images/MYP_1376-small.jpg" />" />
						</div>
						<div class="article-post">
							<div class="user-info">Posted by jordan, 3 days ago</div>
							<div class="user-content">Vivamus sed auctor nibh congue,
								ligula vitae tempus pharetra... Vivamus sed auctor nibh congue,
								ligula vitae tempus pharetra... Vivamus sed auctor nibh congue,
								ligula vitae tempus pharetra...</div>
							<div class="btn-group">
								<button class="button black mini">
									<i class="icon-pencil"></i> Edit
								</button>
								<button class="button black mini">
									<i class="icon-remove"></i> Delete
								</button>
								<button class="button black mini">
									<i class="icon-ok"></i> Approve
								</button>
							</div>
						</div>
					</li>
				</ul>
			</div>
		</div>
		
		<!-- <div class="span6">
			<div class="box" style="position: relative;">
				<div class="tab-header">Browser stats</div>
				<table class="table table-striped data-table">
					<thead>
						<tr>
							<th>Rendering engine</th>
							<th>Browser</th>
							<th>Platform(s)</th>
							<th>Engine version</th>
						</tr>
					</thead>
					<tbody>
						<tr class="gradeX">
							<td>Trident</td>
							<td>Internet Explorer 4.0</td>
							<td>Win 95+</td>
							<td class="center">4</td>
						</tr>
						<tr class="gradeC">
							<td>Trident</td>
							<td>Internet Explorer 5.0</td>
							<td>Win 95+</td>
							<td class="center">5</td>
						</tr>
					</tbody>
				</table>
			</div>
		</div> -->
		
	</div>

	<div class="row-fluid">
		<!-- <div class="span6">
			<div class="black-box tex">
				<div class="tab-header">Recent comments</div>
				<ul class="recent-comments">
					<li class="separator">
						<div class="avatar pull-left">
							<img src="<c:url value="/assets/images/MYP_1376-small.jpg" />" />
						</div>
						<div class="article-post">
							<div class="user-info">Posted by jordan, 3 days ago</div>
							<div class="user-content">Vivamus sed auctor nibh congue,
								ligula vitae tempus pharetra... Vivamus sed auctor nibh congue,
								ligula vitae tempus pharetra... Vivamus sed auctor nibh congue,
								ligula vitae tempus pharetra...</div>
							<div class="btn-group">
								<button class="button black mini">
									<i class="icon-pencil"></i> Edit
								</button>
								<button class="button black mini">
									<i class="icon-remove"></i> Delete
								</button>
								<button class="button black mini">
									<i class="icon-ok"></i> Approve
								</button>
							</div>
						</div>
					</li>
					<li class="separator">
						<div class="avatar pull-left">
							<img src="<c:url value="/assets/images/MYP_1376-small.jpg" />" />
						</div>
						<div class="article-post">
							<div class="user-info">Posted by jordan, 3 days ago</div>
							<div class="user-content">Vivamus sed auctor nibh congue,
								ligula vitae tempus pharetra... Vivamus sed auctor nibh congue,
								ligula vitae tempus pharetra... Vivamus sed auctor nibh congue,
								ligula vitae tempus pharetra...</div>
							<div class="btn-group">
								<button class="button black mini">
									<i class="icon-pencil"></i> Edit
								</button>
								<button class="button black mini">
									<i class="icon-remove"></i> Delete
								</button>
								<button class="button black mini">
									<i class="icon-ok"></i> Approve
								</button>
							</div>
						</div>
					</li>
					<li class="separator" style="text-align: center"><a href="#">View
							all</a></li>
				</ul>
			</div>
		</div>  -->
		<!-- <div class="span6">
			<div class="box" style="position: relative;">
				<div class="tab-header">Browser stats</div>
				<table class="table table-striped data-table">
					<thead>
						<tr>
							<th>Rendering engine</th>
							<th>Browser</th>
							<th>Platform(s)</th>
							<th>Engine version</th>
						</tr>
					</thead>
					<tbody>
						<tr class="gradeX">
							<td>Trident</td>
							<td>Internet Explorer 4.0</td>
							<td>Win 95+</td>
							<td class="center">4</td>
						</tr>
						<tr class="gradeC">
							<td>Trident</td>
							<td>Internet Explorer 5.0</td>
							<td>Win 95+</td>
							<td class="center">5</td>
						</tr>
					</tbody>
				</table>
			</div>
		</div> -->
	</div>

	<!-- <table ng-controller="SearcherController">
	<tr>
		<td>
			<form ng-submit="doSearch()">
			<table>
				<tr>
					<td style="border: 0px solid black;"><b>Collection:</b></td>
					<td colspan="4" nowrap="nowrap">
						<select 
							ng-controller="IndexesController" 
							ng-model="indexName" 
							ng-model="indexes" 
							ng-options="index for index in indexes" 
							ng-change="
								resetSearch();
								doFields(indexName);
								setField('indexName', indexName);"
								class="input-medium">
							<option style="display:none" value="">collection</option>
						</select>
						<select ng-model="pageBlock">
							<option value="10" >10</option>
							<option value="25">25</option>
							<option value="50">50</option>
							<option value="100">100</option>
							<option value="1000">1000</option>
							<option value="10000">10000</option>
						</select>
					</td>
				</tr>
				<tr>
					<td><b>Fields:</b></td>
					<td colspan="4">
						<select 
							ng-model="field" 
							ng-model="fields" 
							ng-options="field for field in fields" 
							ng-change="
								pushField('searchFields', field); 
								pushField('typeFields', 'string');
								setField('firstResult', 0);">
							<option style="display:none" value="">field</option>
						</select>
						<select 
							ng-model="distance" 
							ng-model="search.distance"
							ng-change="setField('distance', distance);"
							ng-show="
								search.searchFields.indexOf('latitude') > -1 && 
								search.searchFields.indexOf('longitude') > -1">
							<option style="display:none" value="">distance</option>
							<option value="1">1</option>
							<option value="3">3</option>
							<option value="5">5</option>
							<option value="10">10</option>
							<option value="20">20</option>
							<option value="100">100</option>
							<option value="1000">1000</option>
						</select>
					</td>
				</tr>
				<tr ng-repeat="field in search.searchFields" ng-hide="search.searchFields == undefined || search.searchFields == null || search.searchFields.length == 0">
					<td><b>{{field}}:</b></td>
					<td colspan="3"><input id="{{field}}" name="{{field}}" ng-model="search.searchStrings[$index]"></td>
					<td>
						<a href="#" 
							ng-click="
								removeField('searchFields', $index); 
								removeField('searchStrings', $index); 
								removeField('typeFields', $index);">Remove</a>
					</td>
				</tr>
				<tr>
					<td colspan="5">
						<button type="submit">Go!</button>
					</td>
				</tr>
			</table>
			</form>
		</td>
		<td ng-show="
			search.searchFields.indexOf('latitude') > -1 && 
			search.searchFields.indexOf('longitude') > -1">
			<div 
				id="map_canvas" 
				google-map 
				style="height: 250px; width: 350px; border : 2px solid black;"></div>
		</td>
	</tr>
	
	<tr ng-show="statistics != undefined && statistics">
		<td colspan="2">
			Showing results {{search.firstResult}} 
			to {{endResult}} 
			of {{statistics.total}} 
			for {{search.searchStrings}} 
			in fields {{search.searchFields}}, 
			duration {{statistics.duration}} milliseconds<br>
			<div ng-show="statistics != undefined && statistics.corrections != undefined && statistics.corrections.length > 0">
				<a href="#" ng-click="setField('searchStrings', statistics.corrections);doSearch();">Did you mean : {{statistics.corrections}}</a>
			</div>
			<div>
	      		<div>
					<div>
						<ul>
							<li ng-repeat="page in pagination">
								<a href="#" ng-click="setField('firstResult', page.firstResult);doSearch();">{{page.page}}</a>
							</li>
						</ul>
					</div>
				</div>
			</div>
		</td>
	</tr>
	
	<tr ng-repeat="result in search.searchResults" ng-show="search.searchResults != undefined && endResult > 0">
		<td colspan="2" nowrap="nowrap">
			<div ng-repeat="(field, value) in result | orderBy:predicate:reverse">{{field}} : {{value}}</div>
		</td>
	</tr>
	
	<tr ng-show="endResult > 0">
		<td colspan="2">
			<div>
	      		<div>
					<div>
						<ul>
							<li ng-repeat="page in pagination">
								<a href="#" ng-click="setField('firstResult', page.firstResult);doSearch();">{{page.page}}</a>
							</li>
						</ul>
					</div>
				</div>
			</div>
		</td>
	</tr>
</table> -->