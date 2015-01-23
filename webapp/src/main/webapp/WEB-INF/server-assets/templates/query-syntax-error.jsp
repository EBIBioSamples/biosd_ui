<%@ page import="java.net.*"%>


<!doctype html>
<!-- paulirish.com/2008/conditional-stylesheets-vs-css-hacks-answer-neither/ -->
<!--[if lt IE 7]> <html class="no-js ie6 oldie" lang="en"> <![endif]-->
<!--[if IE 7]>    <html class="no-js ie7 oldie" lang="en"> <![endif]-->
<!--[if IE 8]>    <html class="no-js ie8 oldie" lang="en"> <![endif]-->
<!-- Consider adding an manifest.appcache: h5bp.com/d/Offline -->
<!--[if gt IE 8]><!-->
<html class="no-js" lang="en">
<!--<![endif]-->
<head>
<meta charset="utf-8">

<!-- Use the .htaccess and remove these lines to avoid edge case issues.
       More info: h5bp.com/b/378 -->
<!-- <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1"> -->
<!-- Not yet implemented -->

<title>BioSamples &lt; EMBL-EBI</title>
<meta name="description" content="EMBL-EBI">
<!-- Describe what this page is about -->
<meta name="keywords" content="bioinformatics, europe, institute">
<!-- A few keywords that relate to the content of THIS PAGE (not the whol project) -->
<meta name="author" content="EMBL-EBI">
<!-- Your [project-name] here -->

<!-- Mobile viewport optimized: j.mp/bplateviewport -->
<meta name="viewport" content="width=device-width,initial-scale=1">

<!-- Place favicon.ico and apple-touch-icon.png in the root directory: mathiasbynens.be/notes/touch-icons -->

<!-- CSS: implied media=all -->
<!-- CSS concatenated and minified via ant build script-->
<link rel="stylesheet"
	href="//www.ebi.ac.uk/web_guidelines/css/compliance/develop/boilerplate-style.css">
<link rel="stylesheet"
	href="//www.ebi.ac.uk/web_guidelines/css/compliance/develop/ebi-global.css"
	type="text/css" media="screen">
<link rel="stylesheet"
	href="//www.ebi.ac.uk/web_guidelines/css/compliance/develop/ebi-visual.css"
	type="text/css" media="screen">
<link rel="stylesheet"
	href="//www.ebi.ac.uk/web_guidelines/css/compliance/develop/984-24-col-fluid.css"
	type="text/css" media="screen">

<!-- you can replace this with [projectname]-colours.css. See http://frontier.ebi.ac.uk/web/style/colour for details of how to do this -->
<!-- also inform ES so we can host your colour palette file -->
<link rel="stylesheet"
	href="//www.ebi.ac.uk/web_guidelines/css/compliance/develop/embl-petrol-colours.css"
	type="text/css" media="screen">

<link rel="stylesheet"
	href="assets/stylesheets/biosamples_browse_10.css" type="text/css" />
<link rel="stylesheet"
	href="assets/stylesheets/biosamples_common_10.css" type="text/css" />
<link rel="stylesheet"
	href="assets/stylesheets/biosamples_metadata_10.css" type="text/css">
<link rel="stylesheet" href="assets/stylesheets/font-awesome.css"
	type="text/css">

<!-- for production the above can be replaced with -->
<!--
  <link rel="stylesheet" href="//www.ebi.ac.uk/web_guidelines/css/compliance/mini/ebi-fluid-embl.css">
  -->

<style type="text/css">
/* You have the option of setting a maximum width for your page, and making sure everything is centered */
/* body { max-width: 1600px; margin: 0 auto; } */
</style>

<!-- end CSS-->


<!-- All JavaScript at the bottom, except for Modernizr / Respond.
       Modernizr enables HTML5 elements & feature detects; Respond is a polyfill for min/max-width CSS3 Media Queries
       For optimal performance, use a custom Modernizr build: www.modernizr.com/download/ -->

<!-- Full build -->
<!-- <script src="//www.ebi.ac.uk/web_guidelines/js/libs/modernizr.minified.2.1.6.js"></script> -->

<!-- custom build (lacks most of the "advanced" HTML5 support -->
<script
	src="//www.ebi.ac.uk/web_guidelines/js/libs/modernizr.custom.49274.js"></script>

</head>

<body class="level2 ${interface.application.body.class}">
	<!-- add any of your classes or IDs -->
	<div id="skip-to">
		<ul>
			<li><a href="#content">Skip to main content</a></li>
			<li><a href="#local-nav">Skip to local navigation</a></li>
			<li><a href="#global-nav">Skip to EBI global navigation menu</a></li>
			<li><a href="#global-nav-expanded">Skip to expanded EBI
					global navigation menu (includes all sub-sections)</a></li>
		</ul>
	</div>

	<%@ include file="WEB-INF/server-assets/html/header_ebi.jsp"%>

	<div id="content" role="main" class="grid_24 clearfix">

		<nav id="breadcrumb">
			<p>
				<a href="${interface.application.base.path}">BioSamples</a> &gt; 
				<%
					if (request.getParameter("keywords") != null
							&& !request.getParameter("keywords").equalsIgnoreCase("")) {
				%>
				<a href="${interface.application.base.path}/browse_samples.html">Samples</a> &gt; Search results for "<%=request.getParameter("keywords")%>"
				<%
					} else {
				%>
				Samples
				<%
					}
				%>

			</p>
		</nav>

		<%
			if (request.getParameter("keywords") != null
					&& !request.getParameter("keywords").equalsIgnoreCase("")) {
		%>
		<section class="grid_18 alpha">
			<h2>
				BioSamples results for <span class="searchterm"><%=request.getParameter("keywords")%></span>
			</h2>
		</section>
		<aside class="grid_6 omega shortcuts expander" id="search-extras">
			<div id="ebi_search_results">
				<h3 class="slideToggle icon icon-functional" data-icon="u">Show
					more data from EMBL-EBI</h3>
			</div>
		</aside>
		<%
			}
		%>
		
			
		<section class="grid_24 alpha omega">
			<div id="bs_browse">

				<div id="contentspane" class="contentspane">
					<%-- after I need to put here a filter
				<div id="bs-query">
					<form id="bs_query_form" method="get" action="browse.html">
						<table border="0" id="table-bs-query" width="100%">
							<tr>
								<td colspan="4">Search BioSample Database at EBI [<a
									href="javascript:aeClearField('#bs_keywords_field')">clear</a>]
								</td>
							</tr>
							<tr>
								<td id="td_no_padding" valign="middle" width="480">
									<fieldset id="bs_keywords_fset">
										<input id="bs_keywords_field" type="text" name="keywords"
											value="" maxlength="200" class="bs_assign_font"
											autocomplete="off" value="<%=request.getParameter("keywords") %>" />
									</fieldset>
								</td>
								<td id="td_no_padding" valign="middle" align="right" width="66"><input
									type="submit" value="Search" class="submit"></td>
								<td id="td_no_padding" valign="middle" width="800">&nbsp;Sort by: <select
									id="sortby" name="sortby" disabled>
										<option value="">Relevance</option>
										<option value="id">Accession</option>
										<option value="title">Title</option>
										<option value="samples">Samples</option>
								</select>
								</td>
								<td id="td_no_padding" valign="middle" align="right" width="66"><span
									class="${interface.application.metadata.class}"><a
										href="metadata?class=${interface.application.body.class}"
										target="ext">Metadata</a></span></td>
							</tr>
						</table>
					</form>
				</div>
 --%>

					<!-- 	<div id="bs_results_area">
			
			
					<div id="bs_results_stats">
						<div id="bs_results_stats_fromto"></div>
						<div id="bs_results_pager"></div>
					</div> -->
					<!-- 		 <table width="100%">
		 <tr>
            <td style="width:150px;border-spacing:10px" bgcolor="yellow" valign="top">
  			<font size="+1">Filter by Source</font></br>
            	<b>All results (1171)</b><br/>
            	<a style="color: #0f2559;border-bottom-style: dotted;border-bottom-width: 1px;line-height:150%">ArrayExpress (1020)</a><br/>
            	<a style="color: #0f2559;border-bottom-style: dotted;border-bottom-width: 1px;line-height:150%">ENA (121)</a><br/>
            	<a style="color: #0f2559;border-bottom-style: dotted;border-bottom-width: 1px;line-height:150%">Pride (4)</a>     
 
            </td>
            <td> -->
					<div class="persist-area">
						<table class="persist-header">
							<colgroup>
								<col class="col_id">
								<col class="col_organisms">
								<col class="col_name">
								<col class="col_description">
								<col class="col_groups">
								<col class="col_database">
							</colgroup>
							<thead>
								<tr>
									<th colspan="6" class="col_pager">
										<div class="bs-pager"></div>
										<div class="bs-page-size"></div>
										<div class="bs-stats"></div>
									</th>
								</tr>
								<tr>
									<th class="sortable bs_results_id col_accession"
										id="bs_results_header_accession"><a
										href="javascript:aeSort('accession')"
										title="Click to sort by accession"><span
												class="table_header_inner">Accession</span></a> <!-- <img
								src="/arrayexpress/assets/images/mini_arrow_up.gif" width="12"
								height="16" alt="^"> --></th>
									<th class="sortable col_organisms" id="bs_results_header_org"
										align="center"><a href="javascript:aeSort('organism')"
										title="Click to sort by Organism"><span
												class="table_header_inner">Organism</span></a></th>
									<th class="sortable col_name" id="bs_results_header_name"><a
										href="javascript:aeSort('name')"
										title="Click to sort by Name"><span
												class="table_header_inner">Name</span></a></th>
									<th class="sortable col_description" id="bs_results_header_description"><a
										href="javascript:aeSort('description')"
										title="Click to sort by description"><span
												class="table_header_inner">Description</span></a></th>
									<th class="sortable col_groups" id="bs_results_header_groups"
										align="center"><a href="javascript:aeSort('groups')"
										title="Click to sort by Groups"><span
												class="table_header_inner">Groups</span></a></th>
									<th class="sortable col_database" id="bs_results_header_database"
										align="center"><a href="javascript:aeSort('database')"
										title="Click to sort by Database"><span
												class="table_header_inner">Database</span></a></th>			
								</tr>
							</thead>
						</table>
						<table id="table_samplegroups" border="0" cellpadding="0"
							cellspacing="0">
							<colgroup>
								<col class="col_id">
								<col class="col_organisms">
								<col class="col_name">
								<col class="col_description">
								<col class="col_groups">
								<col class="col_database">
							</colgroup>
							<tbody id="bs_results_tbody"></tbody>

						</table>
					</div>

					<!-- 		</td>
			</tr>
			</table> -->

				</div>
			</div>
		</section>
	</div>
	</div>
	<%@ include file="WEB-INF/server-assets/html/footer_ebi.html"%>

	<!--! end of #wrapper -->


	<!-- JavaScript at the bottom for fast page loading -->

	<!-- Grab Google CDN's jQuery, with a protocol relative URL; fall back to local if offline -->
	<!--
  <script src="//ajax.googleapis.com/ajax/libs/jquery/1.6.2/jquery.min.js"></script>
  <script>window.jQuery || document.write('<script src="../js/libs/jquery-1.6.2.min.js"><\/script>')</script>
  -->


	<!-- Your custom JavaScript file scan go here... change names accordingly -->
	<script type="text/javascript">
		var contextPath = "${interface.application.base.path}";
	</script>
	<script src="assets/scripts/jquery-1.8.2.min.js" type="text/javascript"></script>
	<script src="assets/scripts/jquery.cookie-1.0.js"
		type="text/javascript"></script>
	<script src="assets/scripts/jquery.caret-range-1.0.js"
		type="text/javascript"></script>
	<script src="assets/scripts/jquery.autocomplete-1.1.0m-ebi.js"
		type="text/javascript"></script>
	<script src="assets/scripts/biosamples_common_10.js"
		type="text/javascript"></script>
	<script src="assets/scripts/biosamples_browse_samples_10.js"
		type="text/javascript"></script>

	<script
		src="//www.ebi.ac.uk/web_guidelines/js/ebi-global-search-run.js"
		type="text/javascript"></script>
	<script src="//www.ebi.ac.uk/web_guidelines/js/ebi-global-search.js"
		type="text/javascript"></script>
	<script src="/biosamples/assets/scripts/jquery.query-2.1.7m-ebi.js"
		type="text/javascript"></script>


	<!--
  <script defer="defer" src="//www.ebi.ac.uk/web_guidelines/js/plugins.js"></script>
	<script defer="defer" src="//www.ebi.ac.uk/web_guidelines/js/script.js"></script>
	-->
	<script defer="defer"
		src="//www.ebi.ac.uk/web_guidelines/js/cookiebanner.js"></script>
	<script defer="defer" src="//www.ebi.ac.uk/web_guidelines/js/foot.js"></script>
	<!-- end scripts-->

	<!-- Google Analytics details... -->
	<!-- Change UA-XXXXX-X to be your site's ID -->
	${interface.application.google.analytics}
	<!--
 
  <script>
    window._gaq = [['_setAccount','UAXXXXXXXX1'],['_trackPageview'],['_trackPageLoadTime']];
    Modernizr.load({
      load: ('https:' == location.protocol ? '//ssl' : '//www') + '.google-analytics.com/ga.js'
    });
  </script>
  -->


	<!-- Prompt IE 6 users to install Chrome Frame. Remove this if you want to support IE 6.
       chromium.org/developers/how-tos/chrome-frame-getting-started -->
	<!--[if lt IE 7 ]>
    <script src="//ajax.googleapis.com/ajax/libs/chrome-frame/1.0.3/CFInstall.min.js"></script>
    <script>window.attachEvent('onload',function(){CFInstall.check({mode:'overlay'})})</script>
  <![endif]-->

</body>
</html>

