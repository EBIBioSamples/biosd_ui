<?xml version="1.0" encoding="windows-1252"?>
<!-- * Copyright 2009-2013 European Molecular Biology Laboratory * * Licensed 
	under the Apache License, Version 2.0 (the "License"); * you may not use 
	this file except in compliance with the License. * You may obtain a copy 
	of the License at * * http://www.apache.org/licenses/LICENSE-2.0 * * Unless 
	required by applicable law or agreed to in writing, software * distributed 
	under the License is distributed on an "AS IS" BASIS, * WITHOUT WARRANTIES 
	OR CONDITIONS OF ANY KIND, either express or implied. * See the License for 
	the specific language governing permissions and * limitations under the License. 
	* -->
<!DOCTYPE xsl:stylesheet [ <!ENTITY nbsp "&#160;"> ]>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:fn="http://www.w3.org/2005/xpath-functions"
	xmlns:ae="http://www.ebi.ac.uk/arrayexpress/XSLT/Extension" xmlns:html="http://www.w3.org/1999/xhtml"
	extension-element-prefixes="html xs fn ae" exclude-result-prefixes="html xs fn ae"
	version="2.0">




<xsl:template name="process_multiple_values">
		<xsl:param name="pValue" />
		<xsl:param name="pField" />
		<xsl:for-each select="$pValue//value">
			<!-- <xsl:call-template name="highlight"> <xsl:with-param name="pText" 
				select="$pValue" /> <xsl:with-param name="pFieldName" select="concat('attributes:',$pField)" 
				/> </xsl:call-template> -->

			<xsl:call-template name="highlight">
				<xsl:with-param name="pText" select="." />
				<xsl:with-param name="pFieldName" select="$pField" />
			</xsl:call-template>
			<!-- <xsl:copy-of select="."></xsl:copy-of> -->
			<xsl:if test="position()!=last()">
				,
			</xsl:if>
		</xsl:for-each>
	</xsl:template>
	
	<xsl:template name="process_multiple_values_multiline">
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


	<xsl:template name="process_derived_from">
		<xsl:param name="pAttribute" />
		<xsl:for-each select="$pAttribute//simpleValue/value">
			<a href="{$basepath}/sample/{.}">
				<xsl:call-template name="highlight">
					<xsl:with-param name="pText" select="." />
					<xsl:with-param name="pFieldName" select="''" />
				</xsl:call-template>

				<!-- <xsl:copy-of select="."></xsl:copy-of> -->
			</a>
		</xsl:for-each>

	</xsl:template>

	<xsl:template name="process_efo">
		<xsl:param name="pAttribute" />
		<xsl:param name="pField" />
		<xsl:for-each select="$pAttribute/simpleValue">
			<xsl:choose>
				<xsl:when
					test="count(.//attribute/simpleValue/value[../../@class='Term Source URI'])=0">
					<!-- <xsl:copy-of select="$pAttribute/simpleValue/value"></xsl:copy-of> -->
					<xsl:call-template name="highlight">
						<xsl:with-param name="pText" select="./value" />.
						<xsl:with-param name="pFieldName" select="$pField" />
					</xsl:call-template>

				</xsl:when>
				<xsl:otherwise>

					<xsl:call-template name="process_efo_url">
						<xsl:with-param name="pAttribute" select="." />
						<xsl:with-param name="pField" select="$pField" />
					</xsl:call-template>
					<!-- <a href="{.//attribute/simpleValue/value[../../@class='Term Source 
						URI']}" target="ext"> <xsl:value-of select="simpleValue/value"></xsl:value-of> 
						</a> -->
				</xsl:otherwise>
			</xsl:choose>
			<br/> 
		</xsl:for-each>
	</xsl:template>




	<xsl:template name="process_efo_url">
		<xsl:param name="pAttribute" />
		<xsl:param name="pField" />

		<!-- <xsl:for-each select="value"> -->

		<xsl:choose>
			<xsl:when
				test="starts-with(.//attribute/simpleValue/value[../../@class='Term Source URI'],'http://www.ncbi.nlm.nih.gov/taxonomy')">
				<a
					href="http://www.ncbi.nlm.nih.gov/taxonomy/?term={.//attribute/simpleValue/value[../../@class='Term Source ID']}"
					target="ext">
					<xsl:call-template name="highlight">
						<xsl:with-param name="pText" select="value" />
						<xsl:with-param name="pFieldName" select="$pField" />
					</xsl:call-template>
					<!-- <xsl:value-of select="$pAttribute/simpleValue/value"></xsl:value-of> -->
				</a>
			</xsl:when>
			
			<xsl:when
				test="starts-with(.//attribute/simpleValue/value[../../@class='Term Source ID'],'http:')">
				<a
					href="{.//attribute/simpleValue/value[../../@class='Term Source ID']}"
					target="ext">
					<xsl:call-template name="highlight">
						<xsl:with-param name="pText" select="value" />
						<xsl:with-param name="pFieldName" select="$pField" />
					</xsl:call-template>
					<xsl:value-of select="$pAttribute/simpleValue/value"></xsl:value-of>
				</a>
			</xsl:when>
			<!-- if joining URI and ID together, make sure they have a / between them -->
			<xsl:when
				test="ends-with(.//attribute/simpleValue/value[../../@class='Term Source URI'],'/')">
				<a
					href="{.//attribute/simpleValue/value[../../@class='Term Source URI']}{.//attribute/simpleValue/value[../../@class='Term Source ID']}"
					target="ext">
					<xsl:call-template name="highlight">
						<xsl:with-param name="pText" select="value" />
						<xsl:with-param name="pFieldName" select="$pField" />
					</xsl:call-template>
				</a>
			</xsl:when>
			
			<xsl:otherwise>
				<a
					href="{.//attribute/simpleValue/value[../../@class='Term Source URI']}{.//attribute/simpleValue/value[../../@class='Term Source ID']}"
					target="ext">
					<xsl:call-template name="highlight">
						<xsl:with-param name="pText" select="value" />
						<xsl:with-param name="pFieldName" select="$pField" />
					</xsl:call-template>
				</a>
			</xsl:otherwise>
		<!-- 	<xsl:when
				test="starts-with(.//attribute/simpleValue/value[../../@class='Term Source URI'],'http://www.ebi.ac.uk/efo/EFO')">
				<a
					href="{.//attribute/simpleValue/value[../../@class='Term Source ID']}"
					target="ext">
					<xsl:call-template name="highlight">
						<xsl:with-param name="pText" select="value" />
						<xsl:with-param name="pFieldName" select="$pField" />
					</xsl:call-template>
					<xsl:value-of select="$pAttribute/simpleValue/value"></xsl:value-of>
				</a>
			</xsl:when>
			<xsl:otherwise>
				<a
					href="{.//attribute/simpleValue/value[../../@class='Term Source URI']}"
					target="ext">
					<xsl:call-template name="highlight">
						<xsl:with-param name="pText" select="value" />
						<xsl:with-param name="pFieldName" select="$pField" />
					</xsl:call-template>
				</a>
			</xsl:otherwise> -->
		</xsl:choose>

		<!-- </xsl:for-each> -->
	</xsl:template>


	<xsl:template name="process_unit">
		<xsl:param name="pAttribute" />
		<xsl:param name="pField" />
				<xsl:for-each select="$pAttribute/simpleValue">
							<xsl:call-template name="highlight">
								<xsl:with-param name="pText"
									select="concat(./value, ' (',.//attribute/simpleValue/value[../../@class='Unit'] , ')')" />
								<xsl:with-param name="pFieldName" select="$pField" />
							</xsl:call-template>
							<br/>
				</xsl:for-each>
	</xsl:template>
	
	
	<xsl:template name="process_references">
		<xsl:param name="pReferences" />
				<xsl:for-each select="$pReferences/ref">
							<xsl:call-template name="process_ref">
								<xsl:with-param name="pReference"
									select="." />
							</xsl:call-template>
							<br/>
				</xsl:for-each>
	</xsl:template>


	<xsl:template name="process_ref">
		<xsl:param name="pReference" />
				<xsl:copy-of select="string($pReference/@title)"></xsl:copy-of>: <a href="{$pReference/@url}" target="ext">
							<xsl:call-template name="highlight">
								<xsl:with-param name="pText"
									select="$pReference/@id" />
								<xsl:with-param name="pFieldName" select="'references'" />
							</xsl:call-template>
							</a>
	</xsl:template>	

</xsl:stylesheet>