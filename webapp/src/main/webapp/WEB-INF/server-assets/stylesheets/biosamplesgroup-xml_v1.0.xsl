<?xml version="1.0" encoding="windows-1252"?>
<!-- cannto change the enconding to ISO-8859-1 or UTF-8 -->

<!DOCTYPE xsl:stylesheet [ <!ENTITY nbsp "&#160;"> ]>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns="http://www.ebi.ac.uk/biosamples/SampleGroupExport/1.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:aejava="java:uk.ac.ebi.arrayexpress.utils.saxon.ExtFunctions"
	xmlns:escape="org.apache.commons.lang.StringEscapeUtils" xmlns:html="http://www.w3.org/1999/xhtml"
	extension-element-prefixes="xs aejava html escape"
	exclude-result-prefixes="xs aejava html escape" version="2.0">

	<xsl:include href="biosamples-xml-page_v1.0.xsl" />
	<xsl:strip-space elements="*" />

	<xsl:param name="id" />
	<xsl:param name="sampleslist" />

	<xsl:param name="host" />
	<xsl:param name="context-path" />



	<xsl:variable name="vSamplesList"
		select="if ($sampleslist and $sampleslist='true') then  1  else 0" />


	<xsl:variable name="vSchemaLocation"
		select="concat('http://',$host, $context-path, '/assets/xsd/v',$apiVersion, '/BioSDSchema.xsd')"></xsl:variable>

	<xsl:strip-space elements="*" />
	<xsl:template match="//SampleGroup">
		<xsl:comment>
			BioSamples XML API - version 1.0
		</xsl:comment>
		<BioSampleGroup>
			<xsl:call-template name="process_schemaLocation">
				<xsl:with-param name="pRootTag" select="."></xsl:with-param>
				<xsl:with-param name="pSchemaLocation" select="$vSchemaLocation"></xsl:with-param>
			</xsl:call-template>
			<!-- cannot copy all because I have there samplescount -->
			<xsl:copy-of select="@id" />
			<!-- <xsl:apply-templates select="*[not(self::SampleAttributes)]"></xsl:apply-templates> -->

			<!-- <xsl:apply-templates select="attribute[simpleValue/value/text() and 
				count(.//object)=0]"></xsl:apply-templates> -->

			<xsl:if
				test="count(./attribute[@class='Term Sources'])>0 and count(.//object[@class='Term Source'])>0">
				<xsl:call-template name="process_term_sources">
					<xsl:with-param name="pAttribute"
						select="./attribute[@class='Term Sources']" />
				</xsl:call-template>
			</xsl:if>

			<xsl:for-each select="./attribute[upper-case(@dataType)!='OBJECT']">
				<xsl:call-template name="process_atomic_attribute">
					<xsl:with-param name="pAttribute" select="." />
				</xsl:call-template>
			</xsl:for-each>



			<xsl:if
				test="count(./attribute[@class='Organizations'])>0 and count(.//object[@class='Organization'])>0">
				<xsl:call-template name="process_organizations">
					<xsl:with-param name="pAttribute"
						select="./attribute[@class='Organizations']" />
				</xsl:call-template>
			</xsl:if>


			<xsl:if
				test="count(./attribute[@class='Persons'])>0 and count(.//object[@class='Person'])>0">
				<xsl:call-template name="process_persons">
					<xsl:with-param name="pAttribute" select="./attribute[@class='Persons']" />
				</xsl:call-template>
			</xsl:if>

			<xsl:if
				test="count(./attribute[@class='Databases'])>0 and count(.//object[@class='Database'])>0">
				<xsl:call-template name="process_databases">
					<xsl:with-param name="pAttribute"
						select="./attribute[@class='Databases']" />
				</xsl:call-template>
			</xsl:if>

			<xsl:if
				test="count(./attribute[@class='Publications'])>0 and count(.//object[@class='Publication'])>0">
				<xsl:call-template name="process_publications">
					<xsl:with-param name="pAttribute"
						select="./attribute[@class='Publications']" />
				</xsl:call-template>
			</xsl:if>



			<xsl:apply-templates select="//SampleIds"></xsl:apply-templates>
		</BioSampleGroup>
	</xsl:template>


	<xsl:template match="//SampleIds">
		<xsl:if test="$vSamplesList=1">
			<SampleIds>
				<xsl:for-each select="Id">
					<Id>
						<xsl:copy-of select="string(.)"></xsl:copy-of>
					</Id>
				</xsl:for-each>
			</SampleIds>
		</xsl:if>
	</xsl:template>




	<!-- <xsl:template match="//attribute[simpleValue/value/text()]"> <Property 
		class="{@class}" characteristic="false" comment="false" type="{@dataType}"> 
		<xsl:copy-of select="."></xsl:copy-of> <Value> <xsl:apply-templates select="simpleValue/value"></xsl:apply-templates> 
		</Value> </Property> </xsl:template> -->

</xsl:stylesheet>













