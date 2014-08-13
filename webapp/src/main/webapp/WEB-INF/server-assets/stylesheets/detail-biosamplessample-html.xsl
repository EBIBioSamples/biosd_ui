<?xml version="1.0" encoding="UTF-8"?>
<!-- cannot change the enconding to ISO-8859-1 or UTF-8 -->

<!DOCTYPE xsl:stylesheet [ <!ENTITY nbsp "&#160;"> ]>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:fn="http://www.w3.org/2005/xpath-functions" xmlns:xs="http://www.w3.org/2001/XMLSchema"
	xmlns:html="http://www.w3.org/1999/xhtml" extension-element-prefixes="xs fn html"
	exclude-result-prefixes="xs fn html" version="2.0">

	<xsl:param name="page" />
	<xsl:param name="pagesize" />
	<xsl:param name="sortby" />
	<xsl:param name="sortorder" />
	<xsl:param name="queryid" />
	<xsl:param name="keywords" />
	<xsl:param name="accession" />
	<xsl:param name="basepath" />
	<xsl:param name="total" />



	<xsl:include href="biosamples-html-page.xsl" />
	<!-- <xsl:include href="ae-sort-arrays.xsl"/> -->
	<xsl:include href="biosamples-highlight.xsl" />
	<xsl:include href="biosamples-process-attributes.xsl" />

	<xsl:variable name="vPage"
		select="if ($page) then $page cast as xs:integer else 1" />
	<xsl:variable name="vPageSize"
		select="if ($pagesize) then $pagesize cast as xs:integer else 25" />


	<xsl:variable name="vSortBy"
		select="if ($sortby) then $sortby else 'accession'" />
	<xsl:variable name="vSortOrder"
		select="if ($sortorder) then $sortorder else 'ascending'" />


	<xsl:variable name="vTotal"
		select="if ($total) then $total cast as xs:integer else -1" />

	<xsl:variable name="vBaseUrl">
		http://
		<xsl:value-of select="$host" />
		<xsl:value-of select="$basepath" />
	</xsl:variable>


	<xsl:variable name="vkeywords" select="$keywords" />


	<xsl:variable name="vSearchMode"
		select="fn:ends-with($relative-uri, 'search.html')" />



	<xsl:template match="/">

		<xsl:call-template name="ae-page">
			<xsl:with-param name="pIsSearchVisible" select="fn:true()" />
			<xsl:with-param name="pSearchInputValue"
				select="if (fn:true()) then $keywords else ''" />
			<xsl:with-param name="pTitleTrail" select="$accession" />
			<xsl:with-param name="pExtraCSS">
				<link rel="stylesheet"
					href="{$context-path}/assets/stylesheets/biosamples_detail_10.css"
					type="text/css" />
					<!-- need this to have the scrollbars on Chrome/firefox/safari - overwrite 
					the ebi css definition [PT:53620963] -->
				<style type="text/css">
					html {overflow-y:auto;}
				</style>
			</xsl:with-param>
			<xsl:with-param name="pBreadcrumbTrail">
				<a href="{$context-path}/browse_samples.html">Samples</a>
				>
				<xsl:value-of select="$accession" />
			</xsl:with-param>
			<xsl:with-param name="pExtraJS">
			</xsl:with-param>
		</xsl:call-template>
	</xsl:template>


	<xsl:template name="ae-content-section">

		<xsl:variable name="vFiltered" select="//all/Sample" />

		<div id="bs_content">

			<xsl:choose>
				<xsl:when test="$vTotal&gt;0">
					<xsl:apply-templates select="$vFiltered">
					</xsl:apply-templates>
				</xsl:when>
				<xsl:otherwise>
					<xsl:call-template name="block-warning">
						<xsl:with-param name="pStyle" select="'bs_warn_area'" />
						<xsl:with-param name="pMessage">
							<xsl:text>There are no Samples matching your search criteria found in BioSamples Database.</xsl:text>
						</xsl:with-param>
					</xsl:call-template>
				</xsl:otherwise>
			</xsl:choose>

		</div>

	</xsl:template>

	<xsl:template match="Sample">
		<xsl:variable name="vSample" select="."></xsl:variable>
		<div class="detail_table">
			<h4>
				Sample Accession
				<xsl:copy-of select="string(@id)" />
			</h4>
			<table id="bs_results_tablesamplegroupdetail">

		<tr>
				<td class="col_title">
					<b>Submission Release Date:</b>
				</td>
				<td>
					<xsl:call-template name="highlight">
						<xsl:with-param name="pText"
							select="substring(submission/attribute/simpleValue/value[../../@class='Submission Release Date'],0,11)" />
						<xsl:with-param name="pFieldName" select="'releasedate'" />
					</xsl:call-template>
				</td>
			</tr>
			<tr>
				<td class="col_title">
					<b>Submission Update Date:</b>
				</td>
				<td>
					<xsl:call-template name="highlight">
						<xsl:with-param name="pText"
							select="substring(submission/attribute/simpleValue/value[../../@class='Submission Update Date'],0,11)" />
						<xsl:with-param name="pFieldName" select="'updatedate'" />
					</xsl:call-template>
				</td>
			</tr>

				<tr>
					<td class="col_title">
						<b>
							Organism
							:
						</b>
					</td>
					<td>
						<xsl:for-each select="$vSample/attribute[@class='Organism']">
							<xsl:call-template name="process_efo">
								<xsl:with-param name="pAttribute" select="."></xsl:with-param>
								<xsl:with-param name="pField" select="'organism'"></xsl:with-param>
							</xsl:call-template>
						</xsl:for-each>
					</td>
				</tr>
				<tr>
					<td class="col_title">
						<b>
							Sample Name
							:
						</b>
					</td>
					<td>
						<xsl:call-template name="process_multiple_values_multiline">
							<xsl:with-param name="pValue"
								select="$vSample/attribute[@class='Sample Name']"></xsl:with-param>
							<xsl:with-param name="pField" select="'name'"></xsl:with-param>
						</xsl:call-template>
					</td>
				</tr>
				<tr>
					<td class="col_title">
						<b>
							Sample Description
							:
						</b>
					</td>
					<td>
						<xsl:call-template name="process_multiple_values_multiline">
							<xsl:with-param name="pValue"
								select="$vSample/attribute[@class='Sample Description']"></xsl:with-param>
							<xsl:with-param name="pField" select="'description'"></xsl:with-param>
						</xsl:call-template>
					</td>
				</tr>
				<xsl:for-each
					select="attribute[@class!='Sample Accession' and @class!='Organism' and @class!='Sample Name' and @class!='Sample Description']">
					<xsl:variable name="attribute" select="."></xsl:variable>
					<xsl:variable name="attributeClass" select="./@class"></xsl:variable>
					<tr>
						<td class="col_title">
							<b>
								<xsl:value-of select="$attributeClass"></xsl:value-of>
								:
							</b>
						</td>
						<td>
							<xsl:choose>
								<xsl:when test="$attributeClass='Derived From'">
									<xsl:call-template name="process_derived_from">
										<xsl:with-param name="pAttribute" select="$attribute"></xsl:with-param>
									</xsl:call-template>
								</xsl:when>
								<xsl:when test="$attributeClass='Databases'">
									<xsl:call-template name="process_databases">
										<xsl:with-param name="pValue" select="$attribute"></xsl:with-param>
									</xsl:call-template>
								</xsl:when>
								<!-- normal value -->
								<xsl:when
									test="count($attribute//attribute[@class='Term Source REF'])=0 and count($attribute//attribute[@class='Unit'])=0">
									<!-- <xsl:copy-of select="value"></xsl:copy-of> -->
									<xsl:call-template name="process_multiple_values_multiline">
										<xsl:with-param name="pField"
											select="lower-case(replace(@class,' ' , '-'))"></xsl:with-param>
										<xsl:with-param name="pValue" select="$attribute"></xsl:with-param>
									</xsl:call-template>
								</xsl:when>

								<xsl:when test="count($attribute//attribute[@class='Unit'])>0">
									<xsl:call-template name="process_unit">
										<xsl:with-param name="pAttribute" select="$attribute"></xsl:with-param>
										<xsl:with-param name="pField"
											select="lower-case(replace($attributeClass,' ' , '-'))"></xsl:with-param>
									</xsl:call-template>

								</xsl:when>
								<xsl:otherwise>
									<xsl:call-template name="process_efo">
										<xsl:with-param name="pAttribute" select="$attribute"></xsl:with-param>
										<xsl:with-param name="pField"
											select="lower-case(replace($attributeClass,' ' , '-'))"></xsl:with-param>
									</xsl:call-template>
								</xsl:otherwise>

							</xsl:choose>
						</td>
					</tr>

			
				</xsl:for-each>
			
			
			
				<!--  references from my equivalents -->
				<tr>
					<td class="col_title">
						<b>References:</b>
					</td>

					<td>
						<!-- <div id="wrapper_top_scroll"> <div id="div_top_scroll"></div> 
							</div> -->
						<xsl:call-template name="process_references">
							<xsl:with-param name="pReferences" select="$vSample/References"></xsl:with-param>
						</xsl:call-template>
					</td>
				</tr>
				
				<tr>
					<td class="col_title">
						<b>Groups :
						</b>
					</td>
					<td>
						<xsl:for-each select="GroupIds/Id">
							<a href="../group/{.}">
								<xsl:apply-templates select="." mode="highlight">
									<xsl:with-param name="pFieldName" select="'groupaccession'" />
								</xsl:apply-templates>
							</a>
							<xsl:if test="position()!=last()">
								<xsl:copy-of select="', '" />
							</xsl:if>
						</xsl:for-each>
					</td>
				</tr>

			</table>
		</div>
	</xsl:template>



	<xsl:template name="process_databases">
		<xsl:param name="pValue" />
		<xsl:for-each select="$pValue/objectValue">
			<xsl:call-template name="process_database">
				<xsl:with-param name="pName"
					select=".//attribute[@class='Database Name']/simpleValue/value" />
				<xsl:with-param name="pUrl"
					select=".//attribute[@class='Database URI']/simpleValue/value" />
				<xsl:with-param name="pId"
					select=".//attribute[@class='Database ID']/simpleValue/value" />
			</xsl:call-template>
		</xsl:for-each>
	</xsl:template>

	<xsl:template name="process_database">
		<xsl:param name="pName" />
		<xsl:param name="pUrl" />
		<xsl:param name="pId" />

		<xsl:variable name="bdName" select="lower-case($pName)"></xsl:variable>
		<!-- PRIDE changed the user interface: this is temporary -->
		<xsl:variable name="pUrl"
			select="replace($pUrl,'http://www.ebi.ac.uk/pride/showExperiment.do\?experimentAccessionNumber','http://www.ebi.ac.uk/pride/archive/simpleSearch?q')"></xsl:variable>


		<xsl:choose>
			<xsl:when
				test="$bdName=('arrayexpress','ena','ena sra','dgva','pride') and not($pUrl='')">

				<a href="{$pUrl}" target="ext">
					<img src="{$basepath}/assets/images/{$bdName}_logo.gif" alt="{$pName} Link"
						border="0" title="{$pName}" />
				</a>
			</xsl:when>
			<xsl:when test="not($pUrl='')">
				<a href="{$pUrl}" target="ext" title="{$pName}">
					<font class="icon icon-generic" data-icon="L" title="{$pName}" />
					<xsl:copy-of select="$pName"></xsl:copy-of>
				</a>
			</xsl:when>
		</xsl:choose>
		<br />
		URI:
		<a href="{$pUrl}" target="ext">
			<xsl:copy-of select="$pUrl"></xsl:copy-of>
		</a>
		;
		<br />
		ID:
		<xsl:copy-of select="$pId"></xsl:copy-of>
		;
		<br/>
	</xsl:template>

<!--  -->
	
	<!-- <xsl:template name="process_multiple_values">
		<xsl:param name="pValue" />
		<xsl:param name="pField" />
		<table border="0" cellpadding="0" cellspacing="0"
			id="table_inside_attr">
			<tbody>
				<xsl:for-each select="$pValue/simpleValue">
					<tr>
						<td width="900">
							<xsl:call-template name="highlight">
								<xsl:with-param name="pText" select="./value" />
								<xsl:with-param name="pFieldName" select="$pField" />
							</xsl:call-template>
						</td>
						<td>&nbsp;
						</td>
					</tr>
				</xsl:for-each>
			</tbody>
		</table>
	</xsl:template>
 -->




</xsl:stylesheet>
