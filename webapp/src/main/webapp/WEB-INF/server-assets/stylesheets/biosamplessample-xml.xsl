<?xml version="1.0" encoding="windows-1252"?>
<!-- cannto change the enconding to ISO-8859-1 or UTF-8 -->

<!DOCTYPE xsl:stylesheet [ <!ENTITY nbsp "&#160;"> ]>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns="http://www.ebi.ac.uk/biosamples/SampleGroupExport" xmlns:xs="http://www.w3.org/2001/XMLSchema"
	xmlns:aejava="java:uk.ac.ebi.arrayexpress.utils.saxon.ExtFunctions"
	xmlns:escape="org.apache.commons.lang.StringEscapeUtils" xmlns:html="http://www.w3.org/1999/xhtml"
	extension-element-prefixes="xs aejava html escape"
	exclude-result-prefixes="xs aejava html escape" version="2.0">

	<xsl:include href="biosamples-xml-page.xsl" />
	<xsl:strip-space elements="*" />

	<xsl:param name="groupslist" />

	<xsl:variable name="vGroupsList"
		select="if ($groupslist and $groupslist='true') then  1  else 0" />

	<xsl:strip-space elements="*" />
	<xsl:template match="//Sample">
		<BioSample>
			<!-- cannot copy all because I have there samplescount -->
			<xsl:copy-of select="@id" />


			<xsl:for-each
				select="./attribute[upper-case(@dataType)!='OBJECT' and @class!='Derived From']">
				<xsl:call-template name="process_atomic_attribute">
					<xsl:with-param name="pAttribute" select="." />
				</xsl:call-template>
			</xsl:for-each>

			<xsl:for-each select="./attribute[@class='Derived From']">
				<xsl:call-template name="process_derived_from">
					<xsl:with-param name="pAttribute" select="." />
				</xsl:call-template>
			</xsl:for-each>

			<xsl:for-each select=".//object[@class='Database']">
				<xsl:call-template name="process_database">
					<xsl:with-param name="pName"
						select="string(.//attribute/simpleValue/value[../../@class='Database Name'])" />
					<xsl:with-param name="pId"
						select="string(.//attribute/simpleValue/value[../../@class='Database ID'])" />
					<xsl:with-param name="pUrl"
						select="string(.//attribute/simpleValue/value[../../@class='Database URI'])" />
				</xsl:call-template>
			</xsl:for-each>

			<xsl:apply-templates select="//GroupIds"></xsl:apply-templates>
		</BioSample>
	</xsl:template>


	<xsl:template match="//GroupIds">
		<xsl:if test="$vGroupsList=1">
			<GroupIds>
				<xsl:for-each select="Id">
					<Id>
						<xsl:copy-of select="string(.)"></xsl:copy-of>
					</Id>
				</xsl:for-each>
			</GroupIds>
		</xsl:if>
	</xsl:template>



</xsl:stylesheet>
