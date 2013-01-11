<%@ page errorPage="/WEB-INF/jsp/error.jsp" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<script type="text/javascript">
	$(document).ready(function() {
		$(".tab_content").hide();
		$(".tab_content:first").show();
		$("ul.tabs li").click(function() {
			$("ul.tabs li").removeClass("active");
			$(this).addClass("active");
			$(".tab_content").hide();
			var activeTab = $(this).attr("rel");
			$("#" + activeTab).fadeIn();
			// Repaint the map and the graphs
			google.maps.event.trigger(map, 'resize');
		});
	});
</script>

<ul class="tabs">
	<li class="active" rel="dash">Dash</li>
	<li rel="search-advanced">Advanced search</li>
	<li rel="search-geospatial">Geospatial search</li>
	<li rel="search-numeric">Search numeric</li>
	<li rel="search-numeric-range">Search numeric range</li>
	<li rel="indexes">Indexes</li>
	<li rel="properties">Properties</li>
	<li rel="place-holder" style="width : 150px;">&nbsp;</li>
</ul>

<div class="tab_container">
	<div id="dash" class="tab_content">
		<jsp:include page="/WEB-INF/jsp/dash.jsp" />
	</div>
	<div id="search-advanced" class="tab_content">
		<jsp:include page="/search/advanced.jsp" />
	</div>
	<div id="search-geospatial" class="tab_content">
		<jsp:include page="/search/geospatial.jsp" />
	</div>
	<div id="search-numeric" class="tab_content">
		<jsp:include page="/search/numeric.jsp" />
	</div>
	<div id="search-numeric-range" class="tab_content">
		<jsp:include page="/search/numeric-range.jsp" />
	</div>
	<div id="indexes" class="tab_content">
		<jsp:include page="/WEB-INF/jsp/indexes.jsp" />
	</div>
	<div id="properties" class="tab_content">
		<jsp:include page="/WEB-INF/jsp/properties.jsp" />
	</div>
	<div id="place-holder" class="tab_content">
		&nbsp;
	</div>
</div>
</table>