<?xml version="1.0" encoding="windows-1252"?>


<!DOCTYPE xsl:stylesheet [ <!ENTITY nbsp "&#160;"> ]>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:fn="http://www.w3.org/2005/xpath-functions"
	xmlns:search="http://www.ebi.ac.uk/arrayexpress/XSLT/SearchExtension"
	xmlns:escape="org.apache.commons.lang.StringEscapeUtils" xmlns:html="http://www.w3.org/1999/xhtml"
	extension-element-prefixes="html xs fn  escape"
	exclude-result-prefixes="html xs fn escape" version="2.0">



	<xsl:param name="page" />
	<xsl:param name="pagesize" />

	<xsl:param name="sortby" />
	<xsl:param name="sortorder" />

	<xsl:param name="queryid" />
	<xsl:param name="keywords" />
	<xsl:param name="sampleskeywords" />


	<xsl:param name="accession" />

	<!-- <xsl:param name="userid" /> -->
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


	<xsl:variable name="vBrowseMode" select="not($accession)" />

	<xsl:variable name="vkeywords" select="$keywords" />

	<xsl:variable name="vsampleskeywords" select="$sampleskeywords" />

	<xsl:variable name="vSearchMode"
		select="fn:ends-with($relative-uri, 'search.html')" />
	<xsl:variable name="vQueryString"
		select="if ($query-string) then fn:concat('?', $query-string) else ''" />


	<!-- <xsl:output omit-xml-declaration="no" method="html" indent="no" encoding="windows-1252" 
		doctype-public="-//W3C//DTD XHTML 4.01 Transitional//EN" /> -->

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
				<a href="{$context-path}/browse.html">Sample Groups</a>
				>
				<xsl:value-of select="$accession" />
			</xsl:with-param>
			<xsl:with-param name="pExtraJS">
				<!-- <script src="{$context-path}/assets/scripts/jsdeferred.jquery-0.3.1.js" 
					type="text/javascript"></script> -->
				<script src="{$context-path}/assets/scripts/biosamples_detail_10.js"
					type="text/javascript"></script>
			</xsl:with-param>
		</xsl:call-template>
	</xsl:template>


	<xsl:template name="ae-content-section">

		<xsl:variable name="vFiltered" select="//all/SampleGroup" />
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


		<xsl:choose>
			<xsl:when test="$vTotal&gt;0">

				<xsl:apply-templates select="$vFiltered">
				</xsl:apply-templates>
			</xsl:when>
			<xsl:otherwise>
				<xsl:call-template name="block-warning">
					<xsl:with-param name="pStyle" select="'bs_warn_area'" />
					<xsl:with-param name="pMessage">
						<xsl:text>There are no Samples matching your search criteria found in BioSample Database.</xsl:text>
					</xsl:with-param>
				</xsl:call-template>
			</xsl:otherwise>
		</xsl:choose>

	</xsl:template>

	<xsl:template match="SampleGroup">
		<tr>
			<td>
				<div class="detail_table">
					<xsl:call-template name="detail-table">
						<xsl:with-param name="id" select="$accession" />
						<xsl:with-param name="sampleGroup" select="." />
					</xsl:call-template>

				</div>
			</td>
		</tr>
	</xsl:template>

	<xsl:template name="detail-table">
		<xsl:param name="id" />
		<xsl:param name="sampleGroup" />

		<xsl:variable name="numberOfSamples" select="count(SampleIds/Id)" />
		<xsl:variable name="commonAttributes"
			select="$sampleGroup/SampleAttributes/attribute[count(.//simpleValue)>0 and not($numberOfSamples=1)]"></xsl:variable>

		<!-- <xsl:for-each select="$commonAttributes"> rr <xsl:copy-of select="string(./@class)"></xsl:copy-of> 
			</xsl:for-each> -->

		<h4>
			Group Accession
			<xsl:choose>
				<!-- test="string-length($vkeywords)>0"> -->
				<xsl:when
					test="string-length($vkeywords)>0 and contains($vkeywords,'accession:')">
					<xsl:call-template name="highlight">
						<xsl:with-param name="pText" select="@id" />
						<xsl:with-param name="pFieldName" select="'accession'" />
					</xsl:call-template>
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="@id"></xsl:value-of>
				</xsl:otherwise>

			</xsl:choose>
		</h4>

		<table id="bs_results_tablesamplegroupdetail">
			<tr>
				<td class="col_title">
					<b>Name:</b>
				</td>
				<td width="800">
					<xsl:call-template name="highlight">
						<xsl:with-param name="pText"
							select="attribute/simpleValue/value[../../@class='Name']" />
						<xsl:with-param name="pFieldName" select="'name'" />
					</xsl:call-template>
				</td>
			</tr>
			<tr>
				<td class="col_title">
					<b>Submission Title:</b>
				</td>
				<td>
					<xsl:call-template name="highlight">
						<xsl:with-param name="pText"
							select="attribute/simpleValue/value[../../@class='Submission Title']" />
						<xsl:with-param name="pFieldName" select="'title'" />
					</xsl:call-template>

				</td>
			</tr>
			<tr>
				<td class="col_title">
					<b>Submission Identifier:</b>
				</td>
				<td>
					<xsl:call-template name="highlight">
						<xsl:with-param name="pText"
							select="attribute/simpleValue/value[../../@class='Submission Identifier']" />
						<xsl:with-param name="pFieldName" select="'identifier'" />
					</xsl:call-template>

				</td>
			</tr>
			<tr>
				<td class="col_title">
					<b>Submission Description:</b>
				</td>
				<td>
					<!-- <xsl:value-of select="attribute/value[../@class='Submission Description']"></xsl:value-of> -->
					<xsl:call-template name="highlight">
						<!-- <xsl:with-param name="pText" select="escape:escapeHtml(attribute/value[../@class='Submission 
							Description'])" /> -->
						<xsl:with-param name="pText"
							select="attribute/simpleValue/value[../../@class='Submission Description']" />
						<xsl:with-param name="pFieldName" select="'description'" />
					</xsl:call-template>
				</td>
			</tr>
			<!-- <tr> <td class="col_title"> <b>Submission Version:</b> </td> <td> 
				<xsl:call-template name="highlight"> <xsl:with-param name="pText" select="attribute/value[../@class='Submission 
				Version']" /> <xsl:with-param name="pFieldName" select="'version'" /> </xsl:call-template> 
				</td> </tr> -->
			<tr>
				<td class="col_title">
					<b>Submission Release Date:</b>
				</td>
				<td>
					<xsl:call-template name="highlight">
						<xsl:with-param name="pText"
							select="substring(attribute/simpleValue/value[../../@class='Submission Release Date'],0,11)" />
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
							select="substring(attribute/simpleValue/value[../../@class='Submission Update Date'],0,11)" />
						<xsl:with-param name="pFieldName" select="'updatedate'" />
					</xsl:call-template>
				</td>
			</tr>

			<tr>
				<td class="col_title">
					<b>Submission Reference Layer:</b>
				</td>
				<td>
					<xsl:call-template name="highlight">
						<xsl:with-param name="pText"
							select="attribute/simpleValue/value[../../@class='Submission Reference Layer']" />
						<xsl:with-param name="pFieldName" select="'referencelayer'" />
					</xsl:call-template>
				</td>
			</tr>

			<xsl:choose>
				<xsl:when test="count(attribute[@class='Databases'])>0">
					<tr>
						<td class="col_title">
							<b>Databases:</b>
						</td>
						<td>
							<!-- <xsl:call-template name="highlight"> <xsl:with-param name="pText" 
								select="string-join(attribute/value[../@class='Databases']//value, ', ')" 
								/> <xsl:with-param name="pFieldName" select="'databases'" /> </xsl:call-template> -->

							<xsl:call-template name="process_databases">
								<xsl:with-param name="pValue"
									select="attribute[@class='Databases']"></xsl:with-param>
							</xsl:call-template>

						</td>
					</tr>
				</xsl:when>
			</xsl:choose>



			<xsl:choose>
				<xsl:when test="count(attribute[@class='Organizations'])>0">
					<tr>
						<td class="col_title">
							<b>Organizations:</b>
						</td>
						<td>
							<!-- <xsl:call-template name="highlight"> <xsl:with-param name="pText" 
								select="string-join(attribute/value[../@class='Organizations']//value, ', 
								')" /> <xsl:with-param name="pFieldName" select="'organizations'" /> </xsl:call-template> -->

							<xsl:call-template name="process_organization">
								<xsl:with-param name="pValue"
									select="attribute/objectValue[../@class='Organizations']"></xsl:with-param>
							</xsl:call-template>

						</td>
					</tr>
				</xsl:when>
			</xsl:choose>


			<xsl:choose>
				<xsl:when test="count(attribute[@class='Term Sources'])>0">

					<tr>
						<td class="col_title">
							<b>Term Sources:</b>
						</td>
						<td>


							<xsl:call-template name="process_termsource">
								<xsl:with-param name="pValue"
									select="attribute/objectValue[../@class='Term Sources']"></xsl:with-param>
							</xsl:call-template>

						</td>
					</tr>
				</xsl:when>
			</xsl:choose>


			<xsl:choose>
				<xsl:when test="count(attribute[@class='Persons'])>0">

					<tr>
						<td class="col_title">
							<b>Persons:</b>
						</td>
						<td>
							<xsl:call-template name="process_person">
								<xsl:with-param name="pValue"
									select="attribute/objectValue[../@class='Persons']"></xsl:with-param>
							</xsl:call-template>


							<!-- <xsl:call-template name="highlight"> <xsl:with-param name="pText" 
								select="string-join(attribute/value[../@class='Persons']//value, ', ')" /> 
								<xsl:with-param name="pFieldName" select="'persons'" /> </xsl:call-template> -->

						</td>
					</tr>
				</xsl:when>
			</xsl:choose>

			<xsl:choose>
				<xsl:when test="count(attribute[@class='Publications'])>0">

					<tr>
						<td class="col_title">
							<b>Publications:</b>
						</td>
						<td>
							<xsl:call-template name="process_publications">
								<xsl:with-param name="pValue"
									select="attribute/objectValue[../@class='Publications']"></xsl:with-param>
							</xsl:call-template>


							<!-- <xsl:call-template name="highlight"> <xsl:with-param name="pText" 
								select="string-join(attribute/value[../@class='Persons']//value, ', ')" /> 
								<xsl:with-param name="pFieldName" select="'persons'" /> </xsl:call-template> -->
						</td>
					</tr>
				</xsl:when>
			</xsl:choose>

			<!-- Display attributes on the group object JIRA:BSD-66 -->
			<!-- <xsl:for-each select="attribute[@classDefined='false']"> -->
			<!-- all other attributes. I'm doing that to maintain the order. I should 
				have an internal list to be easier to manage -->

			<xsl:for-each
				select="attribute[string(@class)!='Publications' and string(@class)!='Persons' and string(@class)!='Term Sources' and string(@class)!='Organizations' and string(@class)!='Databases' and string(@class)!='Submission Reference Layer' 
	and string(@class)!='Submission Update Date' and string(@class)!='Submission Release Date' and string(@class)!='Submission Description' and string(@class)!='Submission Identifier' 
	and string(@class)!='Submission Title' and string(@class)!='Name']">
				<!-- @@<xsl:copy-of select="string(@class)"></xsl:copy-of>@@ -->
				<tr>
					<td class="col_title">
						<b>
							<xsl:copy-of select="string(@class)"></xsl:copy-of>
							:
						</b>
					</td>
					<td>
						<xsl:variable name="attributeClass" select="string(@class)"></xsl:variable>
						<xsl:variable name="attribute" select="."></xsl:variable>
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
							<!-- SampleTab FTP location -->
							<xsl:when test="$attributeClass='SampleTab FTP location'">
								<xsl:call-template name="process_ftplocation">
									<xsl:with-param name="pValue" select="$attribute"></xsl:with-param>
								</xsl:call-template>
							</xsl:when>
							<!-- SampleTab FTP location -->
							<!-- normal value -->
							<xsl:when
								test="count($attribute//attribute[@class='Term Source REF'])=0 and count($attribute//attribute[@class='Unit'])=0">
								<!-- <xsl:copy-of select="value"></xsl:copy-of> -->
								<xsl:call-template name="process_multiple_values">
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

			<tr>
				<td class="col_title">
					<b>Samples in group:</b>
				</td>
				<td>
					<xsl:value-of select="$numberOfSamples"></xsl:value-of>
				</td>
			</tr>


			<!-- references from my equivalents -->
			<tr>
				<td class="col_title">
					<b>References:</b>
				</td>

				<td>
					<!-- <div id="wrapper_top_scroll"> <div id="div_top_scroll"></div> </div> -->
					<xsl:call-template name="process_references">
						<xsl:with-param name="pReferences" select="References"></xsl:with-param>
					</xsl:call-template>
				</td>
			</tr>

			<!-- Other common attributes - sometimes they are used to have common 
				information about all sample attributes - data protection -->
			<!-- I will not show common attributes when I have only one sample -->
			<xsl:if test="count($commonAttributes)>0">
				<!-- test="count(SampleAttributes//simpleValue)>0 and not(count(SampleIds/Id)=1)"> -->
				<tr>
					<td class="col_title">
						<b>Common attributes:</b>
					</td>

					<td>
						<!-- <div id="wrapper_top_scroll"> <div id="div_top_scroll"></div> 
							</div> -->
						<xsl:call-template name="process_common_attributes">
							<xsl:with-param name="pAttributes" select="SampleAttributes"></xsl:with-param>
						</xsl:call-template>
					</td>
				</tr>
			</xsl:if>



			<xsl:if test="$numberOfSamples>0">
				<tr>
					<td colspan="2">
						<br />
						<div id="bs_results_tbody" style=" display: none;visible:false"></div>
						<div id="bs_browse">
							<table width="100%" border="0" cellpadding="0"
								id="table_browse_samples" cellspacing="0">

								<tr>
									<td>
										<div id="bs_query_box">
											<form id="bs_query_form" method="get"
												action="javascript:searchSamples(document.forms['bs_query_form'].bs_keywords_field.value);">
												<table border="0" id="table-bs-query" width="100%">
													<tbody>
														<tr>
															<td colspan="3" id="td_no_padding">
																Search inside the
																<xsl:value-of select="@samplecount"></xsl:value-of>
																<xsl:text> sample</xsl:text>
																<xsl:if test="$vTotal != 1">
																	<xsl:text>s</xsl:text>
																</xsl:if>
																of SampleGroup [
																<a href="javascript:aeClearField('#bs_keywords_field')">clear</a>
																]
															</td>
														</tr>
														<tr>
															<td valign="top" id="td_no_padding">
																<fieldset id="bs_keywords_fset">
																	<xsl:variable name="vSamplesKeywordsAux"
																		select="if (fn:matches($vsampleskeywords,'\s*\w\s*:')) then ''  else $vsampleskeywords" />
																	<input id="bs_keywords_field" type="text"
																		name="sampleskeywords" value="{$vSamplesKeywordsAux}"
																		maxlength="255" size="60" autocomplete="off" />
																</fieldset>
															</td>
															<td valign="bottom" align="right" id="td_no_padding">
																<input type="submit" value="Search" class="submit" />
															</td>
															<td width='100%'>&nbsp;
															</td>
														</tr>
													</tbody>
												</table>
											</form>
										</div>

									</td>
								</tr>


								<tr>
									<td>
										<div id="bs_results_listsamples">
											<table class="persist-header" id="bs_samples_table"
												border="0" cellpadding="0" width="100%" cellspacing="0"
												style="visibility: visible;">
												<colgroup>
													<col class="col_left_fixed" style="width:120px;" />
													<col class="col_middle_scrollable" style="width:100%" />
													<col class="col_right_fixed" style="width: 100px;" />
												</colgroup>
												<thead>
													<tr>
														<th colspan="3" class="col_pager">
															<div class="bs-pager">
															</div>
															<div class="bs-page-size">
															</div>
															<div class="bs-stats">
															</div>
														</th>
													</tr>
												</thead>
												<tbody>
													<tr>
														<td id="left_fixed">
															<!-- just to have the scroll height size to be aligned with 
																the attributes table (middle area) -->
															<div id="wrapper_top_scroll_sides">
																<div id="div_top_scroll_sides">
																</div>
															</div>
															<table id="src_name_table" border="0" cellpadding="0"
																cellspacing="0" width="100%">
																<thead>
																	<tr>
																		<th
																			class="bs_results_accession sortable bs_results_Accession"
																			id="bs_results_header_accession">
																			<a href="javascript:aeSort('accession')" title="Click to sort by Accession">
																				<span class="table_header_inner">Accession</span>
																			</a>
																		</th>
																	</tr>
																</thead>
																<tbody id="bs_results_tbody_left">
																	<!-- Left content -->
																</tbody>
															</table>

														</td>
														<td id="middle_scrollable">
															<div id="wrapper_top_scroll">
																<div id="div_top_scroll">
																</div>
															</div>
															<div class="attr_table_shadow_container">
																<div class="attr_table_scroll">
																	<table id="attr_table" border="0" cellpadding="0"
																		cellspacing="0" width="100%">
																		<thead>
																			<tr>
																				<xsl:if
																					test="not (exists($commonAttributes/@class[. = 'Organism']))">
																					<th
																						class="bs_results_organism sortable bs_resultsorganism"
																						id="bs_results_header_organism">
																						<a href="javascript:aeSort('organism')" title="Click to sort by Organism">
																							<span class="table_header_inner">Organism</span>
																						</a>
																					</th>
																				</xsl:if>
																				<xsl:if
																					test="not (exists($commonAttributes/@class[. = 'Name']))">
																					<th
																						class="bs_results_description sortable bs_results_name"
																						id="bs_results_header_name">
																						<a href="javascript:aeSort('name')" title="Click to sort by Name">
																							<span class="table_header_inner">Name</span>
																						</a>
																					</th>
																				</xsl:if>
																				<xsl:if
																					test="not (exists($commonAttributes/@class[. = 'Sample Description']))">
																					<th
																						class="bs_results_description sortable bs_results_description"
																						id="bs_results_header_description">
																						<a href="javascript:aeSort('description')"
																							title="Click to sort by Description">
																							<span class="table_header_inner">Description</span>
																						</a>
																					</th>
																				</xsl:if>

																				<xsl:for-each select="SampleAttributes/attribute/@class">
																					<xsl:if
																						test=".!='Sample Accession' and .!='Organism' and .!='Sample Name' and .!='Sample Description' and .!='Databases'">
																						<xsl:variable name="classVar" select="."></xsl:variable>
																						<xsl:if
																							test="not (exists($commonAttributes/@class[. = $classVar]))">
																							<th>
																								<span class="table_header_inner_att">
																									<!-- <xsl:value-of select="replace(.,' ' , '_')"></xsl:value-of>&nbsp; -->
																									<xsl:value-of select="."></xsl:value-of>&nbsp;
																								</span>
																							</th>
																						</xsl:if>

																						<!-- <th class="bs_results_accession sortable bs_results_{position()}" 
																							id="bs_results_header_{position()}"> <a href="javascript:aeSort('{position()}');" 
																							title="Click to sort by {.}"> <div class="table_header_inner"> <xsl:value-of 
																							select="."></xsl:value-of>&nbsp; </div> </a> </th> -->
																					</xsl:if>

																					<!-- xsl:for-each select="SampleAttributes/attribute/@class[.!='Sample 
																						Accession']"> <xsl:choose> <xsl:when test="position()=1"> <th class="bs_results_accession 
																						begin_scroll sortable bs_results_{replace(.,' ' , '-')}" id="bs_results_header_{position()}"> 
																						<a href="javascript:aeSort('{position()}');" title="Click to sort by {.}"> 
																						<div class="table_header_inner"> <xsl:value-of select="."></xsl:value-of>&nbsp; 
																						</div> </a> </th> </xsl:when> <xsl:when test="position()=last()"> <th class="bs_results_accession 
																						end_scroll sortable bs_results_{replace(.,' ' , '-')}" id="bs_results_header_{position()}"> 
																						<a href="javascript:aeSort('{position()}');" title="Click to sort by {.}"> 
																						<div class="table_header_inner"> <xsl:value-of select="."></xsl:value-of>&nbsp; 
																						</div> </a> </th> </xsl:when> <xsl:otherwise> <th class="bs_results_accession 
																						sortable bs_results_{replace(.,' ' , '-')}" id="bs_results_header_{position()}"> 
																						<a href="javascript:aeSort('{position()}');" title="Click to sort by {.}"> 
																						<div class="table_header_inner"> <xsl:value-of select="."></xsl:value-of>&nbsp; 
																						</div> </a> </th> </xsl:otherwise> </xsl:choose> </xsl:for-each> -->
																				</xsl:for-each>
																			</tr>
																		</thead>
																		<tbody id="bs_results_tbody_middle">
																			<!-- Middle content -->
																		</tbody>
																	</table>
																</div>
																<div class="left_shadow" style="display: none;"></div>
																<div class="right_shadow" style="display: none;"></div>
															</div>
														</td>
														<td id="right_fixed">
															<!-- just to have the scroll height size to be aligned with 
																the attributes table (middle area) -->
															<div id="wrapper_top_scroll_sides">
																<div id="div_top_scroll_sides">
																</div>
															</div>
															<table id="links_table" border="0" cellpadding="0"
																cellspacing="0" width="100%">
																<thead>
																	<tr>
																		<!-- I will not allow to sort -->
																		<th class="bs_results_database sortable bs_results_database"
																			id="bs_results_header_database">
																			<a href="javascript:aeSort('database')" title="Click to sort by Database">
																				<span class="table_header_inner">Database</span>
																			</a>
																		</th>
																	</tr>
																</thead>
																<tbody id="bs_results_tbody_right">
																	<!-- Right content -->
																</tbody>
															</table>
														</td>
													</tr>
													<!-- <tr> <td class="bottom_filler" style="height: 0px;"></td> 
														<td class="bottom_filler"></td> </tr> <tr> <td colspan="3" class="col_footer">&nbsp; 
														</td> </tr> -->
												</tbody>
											</table>
										</div>
									</td>
								</tr>
							</table>
						</div>
					</td>
				</tr>
			</xsl:if>
		</table>
	</xsl:template>





	<xsl:template name="detail-row">
		<xsl:param name="pName" />
		<xsl:param name="pFieldName" />
		<xsl:param name="pValue" />
		<xsl:if test="$pValue/node()">
			<xsl:call-template name="detail-section">
				<xsl:with-param name="pName" select="$pName" />
				<xsl:with-param name="pContent">
					<xsl:for-each select="$pValue">
						<div>
							<xsl:apply-templates select="." mode="highlight">
								<xsl:with-param name="pFieldName" select="$pFieldName" />
							</xsl:apply-templates>
						</div>
					</xsl:for-each>
				</xsl:with-param>
			</xsl:call-template>
		</xsl:if>
	</xsl:template>

	<xsl:template name="detail-section">
		<xsl:param name="pName" />
		<xsl:param name="pContent" />
		<tr>
			<td class="detail_name">
				<div class="outer">
					<xsl:value-of select="$pName" />
				</div>
			</td>
			<td class="detail_value">
				<div class="outer">
					<xsl:copy-of select="$pContent" />
				</div>
			</td>
		</tr>
	</xsl:template>

	<xsl:template name="add-sort">
		<xsl:param name="pKind" />
		<xsl:if test="$pKind = $vSortBy">
			<xsl:choose>
				<xsl:when test="'ascending' = $vSortOrder">
					<img src="{$basepath}/assets/images/mini_arrow_up.gif" width="12"
						height="16" alt="^" />
				</xsl:when>
				<xsl:otherwise>
					<img src="{$basepath}/assets/images/mini_arrow_down.gif" width="12"
						height="16" alt="v" />
				</xsl:otherwise>
			</xsl:choose>
		</xsl:if>
	</xsl:template>



	<xsl:template name="process_person">
		<xsl:param name="pValue" />

		<table border="0" cellpadding="0" cellspacing="0"
			id="table_inside_attr">
			<tbody>
				<tr>
					<td>
						<xsl:for-each select="$pValue/object">

							<xsl:choose>
								<xsl:when
									test="count(.//attribute/simpleValue/value[../../@class='Person Email'])>0 and (.//attribute/simpleValue/value[../../@class='Person Email']!='')">
									<xsl:variable name="vStringContacts"
										select="concat(.//attribute/simpleValue/value[../../@class='Person First Name'], ' ', .//attribute/simpleValue/value[../../@class='Person Last Name'] , ' &lt;' ,.//attribute/simpleValue/value[../../@class='Person Email'], '>')"></xsl:variable>

									<a
										href="mailto:{.//attribute/simpleValue/value[../../@class='Person Email']}"
										class="icon icon-generic" data-icon="E">
										<xsl:call-template name="highlight">
											<xsl:with-param name="pText" select="$vStringContacts" />
											<xsl:with-param name="pFieldName" select="'persons'" />
										</xsl:call-template>
									</a>
								</xsl:when>
								<xsl:otherwise>
									<xsl:variable name="vStringContacts"
										select="concat(.//attribute/simpleValue/value[../../@class='Person First Name'], ' ', .//attribute/simpleValue/value[../../@class='Person Last Name'])"></xsl:variable>
									<xsl:call-template name="highlight">
										<xsl:with-param name="pText" select="$vStringContacts" />
										<xsl:with-param name="pFieldName" select="'persons'" />
									</xsl:call-template>
								</xsl:otherwise>

							</xsl:choose>
							<xsl:choose>
								<xsl:when test="position()&lt;last()">
									,
								</xsl:when>
							</xsl:choose>
						</xsl:for-each>
					</td>
				</tr>
			</tbody>
		</table>

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
			<br />
		</xsl:for-each>
	</xsl:template>

	<xsl:template name="process_database">
		<xsl:param name="pName" />
		<xsl:param name="pUrl" />
		<xsl:param name="pId" />

		<xsl:variable name="bdName" select="lower-case(substring-before($pName,' '))"></xsl:variable>
		<xsl:choose>
			<xsl:when
				test="$bdName=('arrayexpress','ena','ena sra','dgva','pride') and not($pUrl='')">
				<!-- PRIDE changed the user interface: this is temporary -->
				<xsl:variable name="pUrl"
					select="replace($pUrl,'http://www.ebi.ac.uk/pride/showExperiment.do\?experimentAccessionNumber','http://www.ebi.ac.uk/pride/archive/simpleSearch?q')"></xsl:variable>

				<a href="{$pUrl}" target="ext">
					<img src="{$basepath}/assets/images/dblinkslogos/{$bdName}_logo.gif" alt="{$pName} Link"
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

	</xsl:template>

	<!-- <td vertical-align="middle">&nbsp;Name: <xsl:copy-of select=".//attribute/value[../@class='Database 
		Name']"/>; ID: <xsl:copy-of select=".//attribute/value[../@class='Database 
		ID']"/>; URI: <a href="{.//attribute/value[../@class='Database URI']}" target="ext"> 
		<xsl:value-of select=".//attribute/value[../@class='Database URI']"></xsl:value-of> 
		</a> -->

	<xsl:template name="process_ftplocation">
		<xsl:param name="pValue" />
		<xsl:for-each select="$pValue/simpleValue">
			<a href="{./value}" target="ext">
				<xsl:copy-of select="./value"></xsl:copy-of>
			</a> &nbsp;
		</xsl:for-each>
	</xsl:template>

	<xsl:template name="process_publications">
		<xsl:param name="pValue" />
		<table border="0" cellpadding="0" cellspacing="0"
			id="table_inside_attr">
			<tbody>
				<xsl:for-each select="$pValue/object">

					<tr>
						<td>
							Publication DOI:
							<xsl:call-template name="highlight">
								<xsl:with-param name="pText"
									select=".//attribute/simpleValue/value[../../@class='Publication DOI']" />
								<xsl:with-param name="pFieldName" select="'publications'" />
							</xsl:call-template>
							Publication PubMed ID:
							<a
								href="http://europepmc.org/abstract/MED/{.//attribute/simpleValue/value[../../@class='Publication PubMed ID']}"
								target="ext">
								<xsl:call-template name="highlight">
									<xsl:with-param name="pText"
										select=".//attribute/simpleValue/value[../../@class='Publication PubMed ID']" />
									<xsl:with-param name="pFieldName" select="'publications'" />
								</xsl:call-template>
							</a>
							<xsl:choose>
								<xsl:when test="position()&lt;last()">
									<br />
								</xsl:when>
							</xsl:choose>
						</td>
					</tr>
				</xsl:for-each>
			</tbody>
		</table>
	</xsl:template>


	<xsl:template name="process_organization">
		<xsl:param name="pValue" />
		<table border="0" cellpadding="0" cellspacing="0"
			id="table_inside_attr">
			<tbody>
				<xsl:for-each select="$pValue/object">
					<tr>
						<td>
							<xsl:call-template name="highlight">
								<xsl:with-param name="pText"
									select="concat('Name: ', .//attribute/simpleValue/value[../../@class='Organization Name'],'; Address: ', .//attribute/simpleValue/value[../../@class='Organization Address'], '; Role: ', .//attribute/simpleValue/value[../../@class='Organization Role'], '; Email: ', .//attribute/simpleValue/value[../../@class='Organization Email'])" />
								<xsl:with-param name="pFieldName" select="'organizations'" />
							</xsl:call-template>
							; URI:
							<a href="{.//attribute/value[../@class='Organization URI']}"
								target="ext">
								<xsl:value-of
									select=".//attribute/simpleValue/value[../../@class='Organization URI']"></xsl:value-of>
							</a>
						</td>
						<!-- <td width="100%">&nbsp; </td> -->
					</tr>
				</xsl:for-each>
			</tbody>
		</table>
	</xsl:template>



	<xsl:template name="process_termsource">
		<xsl:param name="pValue" />
		<table border="0" cellpadding="0" cellspacing="0"
			id="table_inside_attr">
			<!-- <thead> <th>Name </th> <th>URI</th> <th>&nbsp;</th> </thead> -->
			<tbody>
				<xsl:for-each select="$pValue/object">
					<tr>
						<td id="td_nowrap">
							<xsl:call-template name="highlight">
								<xsl:with-param name="pText"
									select=".//attribute/simpleValue/value[../../@class='Term Source Name']" />
								<xsl:with-param name="pFieldName" select="'termsources'" />
							</xsl:call-template>
						</td>
						<td>
							<a
								href="{.//attribute/simpleValue/value[../../@class='Term Source URI']}"
								target="ext">
								<xsl:value-of
									select=".//attribute/simpleValue/value[../../@class='Term Source URI']"></xsl:value-of>
							</a>
						</td>
						<td width="100%">&nbsp;
						</td>
					</tr>
					<!-- <xsl:choose> <xsl:when test="position()&lt;last()"> <div>&nbsp;</div> 
						</xsl:when> </xsl:choose> -->
				</xsl:for-each>
			</tbody>
		</table>
	</xsl:template>



	<xsl:template name="process_common_attributes">
		<xsl:param name="pAttributes" />


		<!-- £££££££ -->
		<!-- <div id="wrapper_top_scroll"> <div id="div_top_scroll"> </div> </div> -->
		<div class="attr_table_shadow_container">
			<div class="attr_table_sample_group_scroll">
				<table width="100%" border="0" cellpadding="0" cellspacing="0"
					style="table-layout:fixed;border:0px;margin:0px;">
					<tr>
						<td>
							<table id="attr_common_table" border="0" cellpadding="0"
								cellspacing="0" width="100%">
								<thead>
									<tr>
										<xsl:for-each
											select="$pAttributes/attribute[count(.//simpleValue)>0]">
											<th>
												<xsl:copy-of select="string(./@class)"></xsl:copy-of>
											</th>
										</xsl:for-each>
									</tr>
								</thead>
								<tbody id="bs_results_tbody_common">
									<!-- <tr> <xsl:for-each select="$pAttributes/attribute[count(.//simpleValue)>0]"> 
										<td> <xsl:variable name="attributeClass" select="@class"></xsl:variable> 
										<xsl:variable name="attribute" select="."></xsl:variable> <xsl:choose> <xsl:when 
										test="$attributeClass='Derived From'"> <xsl:call-template name="process_derived_from"> 
										<xsl:with-param name="pAttribute" select="$attribute"></xsl:with-param> </xsl:call-template> 
										</xsl:when> <xsl:when test="count($attribute//attribute[@class='Term Source 
										REF'])=0 and count($attribute//attribute[@class='Unit'])=0 "> <xsl:call-template 
										name="process_multiple_values"> <xsl:with-param name="pField" select="lower-case(replace(@attributeClass,' 
										' , '-'))"></xsl:with-param> <xsl:with-param name="pValue" select="$attribute"></xsl:with-param> 
										</xsl:call-template> </xsl:when> <xsl:when test="count($attribute//attribute[@class='Unit'])>0"> 
										<xsl:call-template name="process_unit"> <xsl:with-param name="pAttribute" 
										select="$attribute"></xsl:with-param> <xsl:with-param name="pField" select="lower-case(replace($attributeClass,' 
										' , '-'))"></xsl:with-param> </xsl:call-template> </xsl:when> <xsl:otherwise> 
										<xsl:call-template name="process_efo"> <xsl:with-param name="pAttribute" 
										select="$attribute"></xsl:with-param> <xsl:with-param name="pField" select="lower-case(replace(@attributeClass,' 
										' , '-'))"></xsl:with-param> </xsl:call-template> </xsl:otherwise> </xsl:choose> 
										</td> </xsl:for-each> </tr> -->
								</tbody>
							</table>
						</td>
					</tr>
				</table>



			</div>
			<div class="left_shadow" style="display: none;"></div>
			<div class="right_shadow" style="display: none;"></div>
		</div>

	</xsl:template>




</xsl:stylesheet>