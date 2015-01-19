<?xml version="1.0" encoding="windows-1252"?>
<!-- cannto change the enconding to ISO-8859-1 or UTF-8 -->

<!DOCTYPE xsl:stylesheet [ <!ENTITY nbsp "&#160;"> ]>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns="http://www.ebi.ac.uk/biosamples/ResultQuery/1.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:aejava="java:uk.ac.ebi.arrayexpress.utils.saxon.ExtFunctions"
	xmlns:escape="org.apache.commons.lang.StringEscapeUtils" xmlns:html="http://www.w3.org/1999/xhtml"
	extension-element-prefixes="xs aejava html escape"
	exclude-result-prefixes="xs aejava html escape" version="2.0">


	<xsl:include href="biosamples-xml-page_v1.0.xsl" />
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

	<xsl:param name="total" />

	<xsl:variable name="vTotal"
		select="if ($total) then $total cast as xs:integer else -1" />

	<xsl:param name="host" />
	<xsl:param name="context-path" />
	<xsl:variable name="vSchemaLocation"
		select="concat('http://',$host, $context-path, '/assets/xsd/v',$apiVersion, '/ResultQuerySampleGroupSchema.xsd')"></xsl:variable>

	<xsl:template match="/">
		<xsl:comment>
			BioSamples XML API - version 1.0
		</xsl:comment>
		

		<xsl:variable name="vFrom" as="xs:integer">
			<xsl:choose>
				<xsl:when test="$vPage > 0">
					<xsl:value-of select="1 + ( $vPage - 1 ) * $vPageSize" />
				</xsl:when>
				<xsl:when test="$vTotal eq 0">
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
		<ResultQuery>
		<xsl:call-template name="process_schemaLocation">
			<xsl:with-param name="pRootTag" select="."></xsl:with-param>
			<xsl:with-param name="pSchemaLocation" select="$vSchemaLocation"></xsl:with-param>
		</xsl:call-template>
			<SummaryInfo>
				<Total>
					<xsl:value-of select="$vTotal" />
				</Total>
				<From>
					<xsl:value-of select="$vFrom" />
				</From>
				<To>
					<xsl:value-of select="$vTo" />
				</To>
				<PageNumber>
					<xsl:value-of select="$vPage" />
				</PageNumber>
				<PageSize>
					<xsl:value-of select="$vPageSize" />
				</PageSize>
			</SummaryInfo>
			<xsl:apply-templates select="*"></xsl:apply-templates>
		</ResultQuery>
	</xsl:template>

	<xsl:template match="//SampleGroup">
		<BioSampleGroup id="{./id}">
			<!-- <xsl:copy-of select="@*" /> -->
			<!-- I'm only returning a list of Ids -->
			<!-- <xsl:copy-of select="*" /> -->
		</BioSampleGroup>
	</xsl:template>


</xsl:stylesheet>
