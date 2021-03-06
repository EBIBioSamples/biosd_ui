<?xml version="1.0" encoding="windows-1252"?>
<!-- cannto change the enconding to ISO-8859-1 or UTF-8 -->

<!DOCTYPE xsl:stylesheet [ <!ENTITY nbsp "&#160;"> ]>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:aejava="http://www.ebi.ac.uk/arrayexpress/XSLT/SearchExtension"
	xmlns:fn="http://www.w3.org/2005/xpath-functions" xmlns:html="http://www.w3.org/1999/xhtml"
	extension-element-prefixes="xs fn aejava html" exclude-result-prefixes="xs fn aejava html"
	version="2.0">



	<xsl:param name="page" />
	<xsl:param name="pagesize" />


	<xsl:param name="sortby" />
	<xsl:param name="sortorder" />

	<xsl:param name="queryid" />
	<xsl:param name="keywords" />
	<xsl:param name="id" />

	<xsl:param name="basepath" />
	<xsl:param name="total" />

	<xsl:variable name="vTotal"
		select="if ($total) then $total cast as xs:integer else -1" />

	<xsl:variable name="vBaseUrl">
		http://
		<xsl:value-of select="$host" />
		<xsl:value-of select="$basepath" />
	</xsl:variable>

	<xsl:variable name="vBrowseMode" select="not($id)" />

	<xsl:variable name="vkeywords" select="$keywords" />

	<!-- <xsl:output omit-xml-declaration="yes" method="html" indent="no" encoding="ISO-8859-1" 
		doctype-public="-//W3C//DTD HTML 4.01 Transitional//EN" /> -->
	<!-- <xsl:output omit-xml-declaration="yes" method="html" indent="no" encoding="windows-1252" 
		doctype-public="-//W3C//DTD HTML 4.01 Transitional//EN" /> -->



	<!-- <xsl:include href="ae-sort-arrays.xsl"/> -->
	<xsl:include href="biosamples-highlight.xsl" />
	<xsl:include href="biosamples-html-page.xsl" />

	<xsl:template match="/">

		<xsl:call-template name="ae-page">
			<xsl:with-param name="pIsSearchVisible" select="fn:true()" />
			<xsl:with-param name="pSearchInputValue"
				select="if (fn:true()) then $keywords else ''" />
			<xsl:with-param name="pTitleTrail" select="''" />
			<xsl:with-param name="pExtraCSS">
				<link rel="stylesheet"
					href="${interface.application.base.path}/assets/stylesheets/biosamples_browse_10.css"
					type="text/css" />
				<!-- need this to have the scrollbars on Chrome/firefox/safari - overwrite 
					the ebi css definition [PT:53620963] -->
				<style type="text/css">
					html {overflow-y:auto;}
				</style>
			</xsl:with-param>
			<xsl:with-param name="pBreadcrumbTrail">
				<xsl:choose>
					<xsl:when test="$vkeywords!=''">
						<a href="${interface.application.base.path}/browse.html">Samples</a>
						&gt; Search results for "
						<xsl:copy-of select="$vkeywords" />
						"
					</xsl:when>
					<xsl:otherwise>
						Samples
					</xsl:otherwise>
				</xsl:choose>
				<!-- > <xsl:value-of select="$accession" /> -->
			</xsl:with-param>
			<xsl:with-param name="pExtraJS">
				<script src="//www.ebi.ac.uk/web_guidelines/js/ebi-global-search-run.js"
					type="text/javascript"></script>
				<script src="//www.ebi.ac.uk/web_guidelines/js/ebi-global-search.js"
					type="text/javascript"></script>

				<!-- <script src="${interface.application.base.path}/assets/scripts/ebi-global-search-run.js" 
					type="text/javascript"></script> <script src="${interface.application.base.path}/assets/scripts/ebi-global-search.js" 
					type="text/javascript"></script> -->
				<!-- <script src="${interface.application.base.path}/assets/scripts/biosamples_browse_10.js" 
					type="text/javascript"></script> -->
				<!-- <script src="${interface.application.base.path}/assets/scripts/jquery.query-2.1.7m-ebi.js" 
					type="text/javascript"></script> <script src="${interface.application.base.path}/assets/scripts/biosamples_common_10.js" 
					type="text/javascript"></script> -->
			</xsl:with-param>
		</xsl:call-template>
	</xsl:template>



	<xsl:template name="ae-content-section">

		<xsl:variable name="vSortBy" select="if ($sortby) then $sortby else ''" />
		<xsl:variable name="vSortOrder"
			select="if ($sortorder) then $sortorder else 'descending'" />

		<xsl:variable name="vPage"
			select="if ($page) then $page cast as xs:integer else 1" />
		<xsl:variable name="vPageSize"
			select="if ($pagesize) then $pagesize cast as xs:integer else 50" />

		<xsl:variable name="vFrom" as="xs:integer">
			<xsl:choose>
				<xsl:when test="$vPage > 0">
					<xsl:value-of select="1 + ( $vPage - 1 ) * $vPageSize" />
				</xsl:when>
				<xsl:when test="$vTotal = 0">
					0
				</xsl:when>
				<xsl:otherwise>
					1
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<xsl:variable name="vTo" as="xs:integer">
			<xsl:choose>
				<xsl:when test="( $vFrom + $vPageSize - 1 ) > $vTotal">
					<xsl:value-of select="$vTotal" />
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="$vFrom + $vPageSize - 1" />
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>

		<!-- <div id="content" role="main" class="grid_24 clearfix"> -->

		<!-- <nav id="breadcrumb"> <p> <a href="${interface.application.base.path}">BioSamples</a> 
			&gt; <xsl:choose> <xsl:when test="$vkeywords!=''"> <a href="${interface.application.base.path}/browse.html">Sample 
			Groups</a> &gt; Search results for <xsl:copy-of select="$vkeywords"/> </xsl:when> 
			<xsl:otherwise> Sample Groups </xsl:otherwise> </xsl:choose> </p> </nav> -->
		<xsl:choose>
			<xsl:when test="$vkeywords!=''">
				<section class="grid_18 alpha">
					<h2>
						BioSamples results for
						<span class="searchterm">
							<xsl:copy-of select="$vkeywords" />
						</span>
					</h2>
				</section>
				<aside class="grid_6 omega shortcuts expander" id="search-extras">
					<div id="ebi_search_results">
						<h3 class="slideToggle icon icon-functional" data-icon="u">Show
							more data from EMBL-EBI</h3>
					</div>
				</aside>
			</xsl:when>
		</xsl:choose>


		<section class="grid_24 alpha omega">
			<div id="bs_browse">

				<div id="contentspane" class="contentspane">

					<div class="persist-area">
						<table class="persist-header">
							<colgroup>
								<col class="col_id" />
								<col class="col_organisms" />
								<col class="col_name" />
								<col class="col_description" />
								<col class="col_groups" />
								<col class="col_database" />
							</colgroup>
							<thead>
								<!-- <tr> <th colspan="4" class="col_pager"> <div class="bs-pager"></div> 
									<div class="bs-page-size"></div> <div class="bs-stats"></div> </th> </tr> -->
								<xsl:call-template name="table-pager">
									<xsl:with-param name="pColumnsToSpan" select="6" />
									<xsl:with-param name="pName" select="'Sample'" />
									<xsl:with-param name="pTotal" select="$vTotal" />
									<xsl:with-param name="pPage" select="$vPage" />
									<xsl:with-param name="pPageSize" select="$vPageSize" />
								</xsl:call-template>
								<tr>
									<th class="sortable bs_results_id col_accession" id="bs_results_header_accession">
										<xsl:call-template name="add-table-sort">
											<xsl:with-param name="pKind" select="'accession'" />
											<xsl:with-param name="pLinkText" select="'Accession'" />
											<xsl:with-param name="pSortBy" select="$vSortBy" />
											<xsl:with-param name="pSortOrder" select="$vSortOrder" />
										</xsl:call-template>
									</th>
									<th class="sortable col_title" id="bs_results_header_title">
										<xsl:call-template name="add-table-sort">
											<xsl:with-param name="pKind" select="'organism'" />
											<xsl:with-param name="pLinkText" select="'Organism'" />
											<xsl:with-param name="pSortBy" select="$vSortBy" />
											<xsl:with-param name="pSortOrder" select="$vSortOrder" />
										</xsl:call-template>
									</th>
									<th class="sortable col_samples" id="bs_results_header_name"
										align="center">
										<xsl:call-template name="add-table-sort">
											<xsl:with-param name="pKind" select="'name'" />
											<xsl:with-param name="pLinkText" select="'Name'" />
											<xsl:with-param name="pSortBy" select="$vSortBy" />
											<xsl:with-param name="pSortOrder" select="$vSortOrder" />
										</xsl:call-template>
									</th>
									<th class="sortable col_samples" id="bs_results_header_description"
										align="center">
										<xsl:call-template name="add-table-sort">
											<xsl:with-param name="pKind" select="'description'" />
											<xsl:with-param name="pLinkText" select="'Description'" />
											<xsl:with-param name="pSortBy" select="$vSortBy" />
											<xsl:with-param name="pSortOrder" select="$vSortOrder" />
										</xsl:call-template>
									</th>
									<th class="sortable col_database" id="bs_results_header_groups"
										align="center">
										<xsl:call-template name="add-table-sort">
											<xsl:with-param name="pKind" select="'groups'" />
											<xsl:with-param name="pLinkText" select="'Groups'" />
											<xsl:with-param name="pSortBy" select="$vSortBy" />
											<xsl:with-param name="pSortOrder" select="$vSortOrder" />
										</xsl:call-template>
									</th>
									<th class="sortable col_database" id="bs_results_header_database"
										align="center">
										<xsl:call-template name="add-table-sort">
											<xsl:with-param name="pKind" select="'database'" />
											<xsl:with-param name="pLinkText" select="'Database'" />
											<xsl:with-param name="pSortBy" select="$vSortBy" />
											<xsl:with-param name="pSortOrder" select="$vSortOrder" />
										</xsl:call-template>
									</th>
								</tr>
							</thead>
						</table>
						<table id="table_samplegroups" border="0" cellpadding="0"
							cellspacing="0">
							<colgroup>
								<col class="col_id"/>
								<col class="col_organisms"/>
								<col class="col_name"/>
								<col class="col_description"/>
								<col class="col_groups"/>
								<col class="col_database"/>
							</colgroup>
							<tbody id="bs_results_tbody">
								<xsl:apply-templates select="//Sample"></xsl:apply-templates>
							</tbody>
						</table>
					</div>

				</div>
			</div>
		</section>
		<!-- </div> -->


	</xsl:template>


	<xsl:template match="Sample">
		<tr>
			<td class="col_id">
				<div>
					<a href="{$basepath}/sample/{id}?keywords={$vkeywords}">
						<xsl:call-template name="highlight">
							<xsl:with-param name="pText" select="id" />
							<xsl:with-param name="pFieldName" select="'accession'" />
						</xsl:call-template>
					</a>
				</div>
			</td>
			<td>
				<xsl:call-template name="process_organisms">
					<xsl:with-param name="pValue" select="./organisms" />
				</xsl:call-template>
			</td>

			<td class="col_name">
				<xsl:call-template name="highlight">
					<xsl:with-param name="pText" select="name" />
					<xsl:with-param name="pFieldName" select="'name'" />
				</xsl:call-template>
			</td>

			<td class="col_description">
				<xsl:call-template name="highlight">
					<xsl:with-param name="pText" select="description" />
					<xsl:with-param name="pFieldName" select="'description'" />
				</xsl:call-template>
			</td>


			<td class="col_group">
				<xsl:call-template name="process_groups">
					<xsl:with-param name="pValue" select="./groupaccession" />
				</xsl:call-template>
			</td>

			<td class="col_database">
				<xsl:call-template name="process_databases">
					<xsl:with-param name="pValue" select="./databases" />
				</xsl:call-template>
			</td>


		</tr>
	</xsl:template>

	<xsl:template name="process_organisms">
		<xsl:param name="pValue" />
		<xsl:for-each select="$pValue/organism">
			<xsl:call-template name="highlight">
				<xsl:with-param name="pText" select="." />
				<xsl:with-param name="pFieldName" select="'organism'" />
			</xsl:call-template>
			<xsl:if test="position()!=last()">
				<xsl:copy-of select="', '" />
			</xsl:if>

		</xsl:for-each>
	</xsl:template>

	<xsl:template name="process_databases">
		<xsl:param name="pValue" />
		<xsl:for-each select="$pValue/database">
			<xsl:call-template name="process_database">
				<xsl:with-param name="pName" select="name" />
				<xsl:with-param name="pUrl" select="url" />
				<xsl:with-param name="pId" select="id" />
			</xsl:call-template>
		</xsl:for-each>
	</xsl:template>

	<xsl:template name="process_database">
		<xsl:param name="pName" />
		<xsl:param name="pUrl" />
		<xsl:param name="pId" />

		<!-- I will compare only with the first word - it works for the myequivalents service names -->
		<!-- sometimes the dbname just have one string, so i need to add a space to assure that the substring-before works -->
		<xsl:variable name="bdName" select="lower-case(substring-before(concat($pName,' '),' '))"></xsl:variable>
		<xsl:choose>
			<xsl:when
				test="$bdName=('arrayexpress','ena','ena sra','dgva','pride') and not($pUrl='')">
				<!-- PRIDE changed the user interface: this is temporary -->
				<xsl:variable name="pUrl"
					select="replace($pUrl,'http://www.ebi.ac.uk/pride/showExperiment.do\?experimentAccessionNumber','http://www.ebi.ac.uk/pride/archive/simpleSearch?q')"></xsl:variable>

				<a href="{$pUrl}" target="ext" id="iconelink">
					<img src="{$basepath}/assets/images/dblinkslogos/{$bdName}_logo.gif" alt="{$pName} Link"
						border="0" title="{$pName}" />
				</a>
			</xsl:when>
			<xsl:when test="not($pUrl='')">
				<a href="{$pUrl}" target="ext" title="{$pName}" id="iconelink">
					<font class="icon icon-generic" data-icon="L" title="{$pName}" />
				</a>
			</xsl:when>

			<xsl:otherwise>
				<xsl:copy-of select="$pName"></xsl:copy-of>
			</xsl:otherwise>

		</xsl:choose>

	</xsl:template>


	<xsl:template name="process_groups">
		<xsl:param name="pValue" />
		<xsl:for-each select="$pValue/id">
			<a href="group/{.}?sampleskeywords={$keywords}">
				<xsl:call-template name="highlight">
					<xsl:with-param name="pText" select="." />
					<xsl:with-param name="pFieldName" select="'groupaccession'" />
				</xsl:call-template>
			</a>
			<xsl:if test="position()!=last()">
				<xsl:copy-of select="', '" />
			</xsl:if>
		</xsl:for-each>
	</xsl:template>



</xsl:stylesheet>
