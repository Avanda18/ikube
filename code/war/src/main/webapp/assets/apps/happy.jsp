<%@ include file="header.jsp" %>

<div class="container-fluid" ng-controller="HappyController">
    <form>
        <div class="row-fluid">
            <div class="span12">
                <div class="box tex" style="width: 100%; height: 650px;">
                    <div class="tab-header">
                        Geo tweets - this is a map of real time positive tweets from around the world
                        <img
                            ng-show="!!analyzing"
                            alt="Loading spinner"
                            src="<c:url value="/assets/images/loading.gif" />"
                            height="16px"
                            width="16px">
                    </div>
                    <div id="map_canvas" google-map style="width: 100%; height: 650px;"></div>
                    <%--<div id="geo_chart_div" style="width: 100%; height: 500px;"></div>--%>
                </div>
            </div>
        </div>
    </form>
</div>

<%@ include file="footer.jsp" %>