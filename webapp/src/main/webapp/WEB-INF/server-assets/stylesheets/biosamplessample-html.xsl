<?xml version="1.0" encoding="windows-1252"?>
<!-- cannto change the enconding to ISO-8859-1 or UTF-8 -->
<!DOCTYPE xsl:stylesheet [ <!ENTITY nbsp "&#160;"> ]>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:fn="http://www.w3.org/2005/xpath-functions"
	xmlns:aejava="java:uk.ac.ebi.arrayexpress.utils.saxon.ExtFunctions"
	xmlns:html="http://www.w3.org/1999/xhtml" extension-element-prefixes="xs aejava fn html"
	exclude-result-prefixes="xs aejava fn html" version="2.0">

	<xsl:param name="page" />
	<xsl:param name="pagesize" />

	<xsl:variable name="vPage"
		select="if ($page) then $page cast as xs:integer else 1" />
	<xsl:variable name="vPageSize"
		select="if ($pagesize) then $pagesize cast as xs:integer else 25" />

	<xsl:param name="sortby" />
	<xsl:param name="sortorder" />

	<xsl:variable name="vSortBy"
		select="if ($sortby) then $sortby else 'accession'" />
	<xsl:variable name="vSortOrder"
		select="if ($sortorder) then $sortorder else 'ascending'" />

	<xsl:param name="queryid" />
	<xsl:param name="keywords" />
	<xsl:variable name="vkeywords" select="$keywords" />
	<xsl:param name="id" />

	<xsl:param name="userid" />

	<xsl:param name="host" />
	<xsl:param name="basepath" />
	<xsl:param name="total" />
	<xsl:variable name="vTotal"
		select="if ($total) then $total cast as xs:integer else -1" />

	<xsl:variable name="vBaseUrl">
		http://
		<xsl:value-of select="$host" />
		<xsl:value-of select="$basepath" />
	</xsl:variable>

	<xsl:variable name="numberOfGroupSamples" select="//Samples/@groupNumberOfSamples" />
	<xsl:variable name="commonAttributes"
		select="//SampleAttributes/attribute[count(.//simpleValue)>0 and not($numberOfGroupSamples=1)]"></xsl:variable>

	<xsl:include href="biosamples-highlight.xsl" />
	<xsl:include href="biosamples-process-attributes.xsl" />

	<xsl:template match="/">


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



		<div id="bs_results_summary_info">
			<div id="bs_results_total">
				<xsl:value-of select="$vTotal" />
			</div>
			<div id="bs_results_from">
				<xsl:value-of select="$vFrom" />
			</div>
			<div id="bs_results_to">
				<xsl:value-of select="$vTo" />
			</div>
			<div id="bs_results_page">
				<xsl:value-of select="$vPage" />
			</div>
			<div id="bs_results_pagesize">
				<xsl:value-of select="$vPageSize" />
			</div>
		</div>

		<div id="samplescommon">
			<xsl:for-each select="$commonAttributes">
				<xsl:variable name="attributeClass" select="@class"></xsl:variable>
				<xsl:variable name="attribute" select="."></xsl:variable>
				<bs_value_att>
					<xsl:choose>
						<xsl:when test="$attributeClass='Derived From'">
							<xsl:call-template name="process_derived_from">
								<xsl:with-param name="pAttribute" select="$attribute"></xsl:with-param>
							</xsl:call-template>
						</xsl:when>
						<xsl:when
							test="count($attribute//attribute[@class='Term Source REF'])=0 and count($attribute//attribute[@class='Unit'])=0 ">

							<xsl:call-template name="process_multiple_values">
								<xsl:with-param name="pField"
									select="lower-case(replace(@attributeClass,' ' , '-'))"></xsl:with-param>
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
									select="lower-case(replace(@attributeClass,' ' , '-'))"></xsl:with-param>
							</xsl:call-template>
						</xsl:otherwise>

					</xsl:choose>

				</bs_value_att>
			</xsl:for-each>
		</div>


		<xsl:apply-templates select="//Sample">
			<xsl:with-param name="pAttributes" select="//SampleAttributes"></xsl:with-param>
			<xsl:with-param name="numberOfGroupSamples" select="$numberOfGroupSamples"></xsl:with-param>
		</xsl:apply-templates>


	</xsl:template>



	<xsl:template match="Sample">
		<xsl:param name="pAttributes"></xsl:param>
		<xsl:param name="numberOfGroupSamples"></xsl:param>
		<!-- <xsl:param name="pAttributesinteger"></xsl:param> -->

		<xsl:variable name="vSample" select="."></xsl:variable>

		<div id="samplesleft{position()}">
			<bs_value_att>
				<a href="../sample/{@id}?keywords={$vkeywords}">
					<xsl:call-template name="highlight">
						<xsl:with-param name="pText" select="string(@id)" />
						<xsl:with-param name="pFieldName" select="'accession'" />
					</xsl:call-template>
				</a>
			</bs_value_att>
		</div>

		<div id="samplesmiddle{position()}">

			<!-- Organism and Description and Name are estatic -->
			<xsl:if test="not (exists($commonAttributes/@class[. = 'Organism']))">
				<bs_value_att>
					<xsl:call-template name="process_efo">
						<xsl:with-param name="pAttribute"
							select="$vSample/attribute[@class='Organism']"></xsl:with-param>
						<xsl:with-param name="pField" select="'organism'"></xsl:with-param>
					</xsl:call-template>
					<!-- <xsl:call-template name="highlight"> <xsl:with-param name="pText" 
						select="string($vSample/attribute/simpleValue/value[../../@class='Organism'])" 
						/> <xsl:with-param name="pFieldName" select="'organism'" /> </xsl:call-template> -->
				</bs_value_att>
			</xsl:if>

			<xsl:if test="not (exists($commonAttributes/@class[. = 'Sample Name']))">
				<bs_value_att>
					<xsl:call-template name="highlight">
						<xsl:with-param name="pText"
							select="string($vSample/attribute/simpleValue/value[../../@class='Sample Name'])" />
						<xsl:with-param name="pFieldName" select="'name'" />
					</xsl:call-template>
				</bs_value_att>
			</xsl:if>
			<xsl:if
				test="not (exists($commonAttributes/@class[. = 'Sample Description']))">
				<bs_value_att>
					<xsl:call-template name="highlight">
						<xsl:with-param name="pText"
							select="string($vSample/attribute/simpleValue/value[../../@class='Sample Description'])" />
						<xsl:with-param name="pFieldName" select="'description'" />
					</xsl:call-template>
				</bs_value_att>
			</xsl:if>
			<xsl:for-each select="$pAttributes/attribute/@class">
				<xsl:variable name="attributeClass" select="."></xsl:variable>
				<xsl:variable name="attribute"
					select="$vSample/attribute[@class=$attributeClass]"></xsl:variable>
				<xsl:if
					test=".!='Sample Accession' and .!='Organism' and .!='Sample Name' and .!='Sample Description' and .!='Databases'">
					<xsl:if
						test="not (exists($commonAttributes/@class[. = $attributeClass]))">
						<bs_value_att>
							<xsl:choose>
								<xsl:when test="$attributeClass='Derived From'">
									<xsl:call-template name="process_derived_from">
										<xsl:with-param name="pAttribute" select="$attribute"></xsl:with-param>
									</xsl:call-template>
								</xsl:when>
								<!-- <xsl:when test="$attributeClass='Databases'"> <a href="{simpleValue/value}" 
									target="ext"> <xsl:copy-of select="$attribute"></xsl:copy-of> </a> </xsl:when> -->
								<!-- normal value -->
								<xsl:when
									test="count($attribute//attribute[@class='Term Source REF'])=0 and count($attribute//attribute[@class='Unit'])=0 ">

									<xsl:call-template name="process_multiple_values">
										<xsl:with-param name="pField"
											select="lower-case(replace(@attributeClass,' ' , '-'))"></xsl:with-param>
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
											select="lower-case(replace(@attributeClass,' ' , '-'))"></xsl:with-param>
									</xsl:call-template>
								</xsl:otherwise>

							</xsl:choose>

						</bs_value_att>
					</xsl:if>



				</xsl:if>

			</xsl:for-each>

		</div>

		<div id="samplesright{position()}">
			<div>
				<!-- <tr> <td> -->
				<bs_value_att>
					<xsl:choose>
						<!-- firts I will see if the database is defined inside the Sample -->
						<xsl:when test="count($vSample/attribute[@class='Databases'])=1">
							<xsl:call-template name="process_databases">
								<xsl:with-param name="pValue"
									select="$vSample/attribute[@class='Databases']"></xsl:with-param>
							</xsl:call-template>

						</xsl:when>
						<!-- database inside the samplegroup -->
						<xsl:when
							test="count(../DatabaseGroup/attribute[@class='Databases'])=1">
							<xsl:call-template name="process_databases">
								<xsl:with-param name="pValue" select="../DatabaseGroup/attribute"></xsl:with-param>
							</xsl:call-template>
						</xsl:when>
					</xsl:choose>
				</bs_value_att>
				<!-- </td> </tr> -->
			</div>
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
				</a>
			</xsl:when>
			<xsl:otherwise>
				<xsl:copy-of select="$pName"></xsl:copy-of>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>





</xsl:stylesheet>
