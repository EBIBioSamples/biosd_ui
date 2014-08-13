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
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns="http://www.ebi.ac.uk/biosamples/SampleGroupExport/1.0"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xs="http://www.w3.org/2001/XMLSchema"
	xmlns:fn="http://www.w3.org/2005/xpath-functions" xmlns:ae="http://www.ebi.ac.uk/arrayexpress/XSLT/Extension"
	xmlns:html="http://www.w3.org/1999/xhtml" extension-element-prefixes="html xs fn ae"
	exclude-result-prefixes="html xs fn ae" version="2.0">


	<!-- When I change the API version number, I need to change it here also. 
		so for instance if I'm developing 1.1 api, I will copy all the xml API xsl 
		and give them the 1.1 name, anda change this variable to 1.1 on the new file -->
	<xsl:variable name="apiVersion" select="'1.0'"></xsl:variable>
	<!-- <xsl:output omit-xml-declaration="yes" /> -->

	<xsl:template name="process_schemaLocation">
		<xsl:param name="pRootTag" />
		<xsl:param name="pSchemaLocation" />
		<xsl:for-each select="$pRootTag">
			<xsl:attribute name="xsi:schemaLocation"><xsl:copy-of select="$pSchemaLocation"></xsl:copy-of></xsl:attribute>
		</xsl:for-each>
	</xsl:template>

	<xsl:template name="process_organizations">
		<xsl:param name="pAttribute" />
		<xsl:for-each select="$pAttribute/objectValue">
			<xsl:call-template name="process_organization">
				<xsl:with-param name="pName"
					select="string(.//attribute/simpleValue/value[../../@class='Organization Name'])" />
				<xsl:with-param name="pAddress"
					select="string(.//attribute/simpleValue/value[../../@class='Organization Address'])" />
				<xsl:with-param name="pUrl"
					select="string(.//attribute/simpleValue/value[../../@class='Organization URI'])" />
				<xsl:with-param name="pEmail"
					select="string(.//attribute/simpleValue/value[../../@class='Organization Email'])" />
				<xsl:with-param name="pRole"
					select="string(.//attribute/simpleValue/value[../../@class='Organization Role'])" />
			</xsl:call-template>
		</xsl:for-each>
	</xsl:template>


	<xsl:template name="process_organization">
		<xsl:param name="pName" />
		<xsl:param name="pAddress" />
		<xsl:param name="pUrl" />
		<xsl:param name="pEmail" />
		<xsl:param name="pRole" />
		<Organization>
			<Name>
				<xsl:copy-of select="$pName"></xsl:copy-of>
			</Name>
			<Address>
				<xsl:copy-of select="$pAddress"></xsl:copy-of>
			</Address>
			<URI>
				<xsl:copy-of select="$pUrl"></xsl:copy-of>
			</URI>
			<Email>
				<xsl:copy-of select="$pEmail"></xsl:copy-of>
			</Email>
			<Role>
				<xsl:copy-of select="$pRole"></xsl:copy-of>
			</Role>
		</Organization>
	</xsl:template>



	<xsl:template name="process_persons">
		<xsl:param name="pAttribute" />
		<xsl:for-each select="$pAttribute/objectValue">
			<xsl:call-template name="process_person">
				<xsl:with-param name="pFirstName"
					select="string(.//attribute/simpleValue/value[../../@class='Person First Name'])" />
				<xsl:with-param name="pLastName"
					select="string(.//attribute/simpleValue/value[../../@class='Person Last Name'])" />
				<xsl:with-param name="pInitials"
					select="string(.//attribute/simpleValue/value[../../@class='Person Initials'])" />
				<xsl:with-param name="pEmail"
					select="string(.//attribute/simpleValue/value[../../@class='Person Email'])" />
				<xsl:with-param name="pRole"
					select="string(.//attribute/simpleValue/value[../../@class='Person Role'])" />
			</xsl:call-template>
		</xsl:for-each>
	</xsl:template>


	<xsl:template name="process_person">
		<xsl:param name="pFirstName" />
		<xsl:param name="pLastName" />
		<xsl:param name="pInitials" />
		<xsl:param name="pEmail" />
		<xsl:param name="pRole" />
		<Person>
			<FirstName>
				<xsl:copy-of select="$pFirstName"></xsl:copy-of>
			</FirstName>
			<LastName>
				<xsl:copy-of select="$pLastName"></xsl:copy-of>
			</LastName>
			<MidInitials>
				<xsl:copy-of select="$pInitials"></xsl:copy-of>
			</MidInitials>
			<Email>
				<xsl:copy-of select="$pEmail"></xsl:copy-of>
			</Email>
			<Role>
				<xsl:copy-of select="$pRole"></xsl:copy-of>
			</Role>
		</Person>
	</xsl:template>




	<xsl:template name="process_databases">
		<xsl:param name="pAttribute" />
		<xsl:for-each select="$pAttribute/objectValue">
			<xsl:call-template name="process_database">
				<xsl:with-param name="pName"
					select="string(.//attribute/simpleValue/value[../../@class='Database Name'])" />
				<xsl:with-param name="pId"
					select="string(.//attribute/simpleValue/value[../../@class='Database ID'])" />
				<xsl:with-param name="pUrl"
					select="string(.//attribute/simpleValue/value[../../@class='Database URI'])" />
			</xsl:call-template>
		</xsl:for-each>
	</xsl:template>


	<xsl:template name="process_database">
		<xsl:param name="pName" />
		<xsl:param name="pId" />
		<xsl:param name="pUrl" />
		<Database>
			<Name>
				<xsl:copy-of select="$pName"></xsl:copy-of>
			</Name>
			<ID>
				<xsl:copy-of select="$pId"></xsl:copy-of>
			</ID>
			<URI>
				<xsl:copy-of select="$pUrl"></xsl:copy-of>
			</URI>
		</Database>
	</xsl:template>




	<xsl:template name="process_publications">
		<xsl:param name="pAttribute" />
		<xsl:for-each select="$pAttribute/objectValue">
			<xsl:call-template name="process_publication">
				<xsl:with-param name="pDoi"
					select="string(.//attribute/simpleValue/value[../../@class='Publication DOI'])" />
				<xsl:with-param name="pPubMedID"
					select="string(.//attribute/simpleValue/value[../../@class='Publication PubMed ID'])" />
			</xsl:call-template>
		</xsl:for-each>
	</xsl:template>


	<xsl:template name="process_publication">
		<xsl:param name="pDoi" />
		<xsl:param name="pPubMedID" />
		<Publication>
			<DOI>
				<xsl:copy-of select="$pDoi"></xsl:copy-of>
			</DOI>
			<PubMedID>
				<xsl:copy-of select="$pPubMedID"></xsl:copy-of>
			</PubMedID>

		</Publication>
	</xsl:template>



	<xsl:template name="process_term_sources">
		<xsl:param name="pAttribute" />
		<xsl:for-each select="$pAttribute/objectValue">
			<xsl:call-template name="process_term_source">
				<xsl:with-param name="pName"
					select="string(.//attribute/simpleValue/value[../../@class='Term Source Name'])" />
				<xsl:with-param name="pUrl"
					select="string(.//attribute/simpleValue/value[../../@class='Term Source URI'])" />
				<xsl:with-param name="pVersion"
					select="string(.//attribute/simpleValue/value[../../@class='Term Source Version'])" />
				<xsl:with-param name="pId"
					select="string(.//attribute/simpleValue/value[../../@class='Term Source ID'])" />

			</xsl:call-template>
		</xsl:for-each>
	</xsl:template>


	<xsl:template name="process_term_source">
		<xsl:param name="pName" />
		<xsl:param name="pUrl" />
		<xsl:param name="pVersion" />
		<xsl:param name="pId" />
		<TermSource>
			<Name>
				<xsl:copy-of select="$pName"></xsl:copy-of>
			</Name>
			<Description></Description>
			<URI>
				<xsl:copy-of select="$pUrl"></xsl:copy-of>
			</URI>
			<Version>
				<xsl:copy-of select="$pVersion"></xsl:copy-of>
			</Version>
			<TermSourceID>
				<xsl:copy-of select="$pId"></xsl:copy-of>
			</TermSourceID>
		</TermSource>
	</xsl:template>


	<xsl:template name="process_term_source_ref">
		<xsl:param name="pName" />
		<xsl:param name="pUrl" />
		<xsl:param name="pVersion" />
		<xsl:param name="pId" />
		<TermSourceREF>
			<Name>
				<xsl:copy-of select="$pName"></xsl:copy-of>
			</Name>
			<Description></Description>
			<URI>
				<xsl:copy-of select="$pUrl"></xsl:copy-of>
			</URI>
			<Version>
				<xsl:copy-of select="$pVersion"></xsl:copy-of>
			</Version>
			<TermSourceID>
				<xsl:copy-of select="$pId"></xsl:copy-of>
			</TermSourceID>
		</TermSourceREF>
	</xsl:template>



	<xsl:template name="process_atomic_attribute">
		<xsl:param name="pAttribute" />
		<Property class="{$pAttribute/@class}" characteristic="{boolean($pAttribute/@characteristic)}"
			comment="{boolean($pAttribute/@comment)}" type="{$pAttribute/@dataType}">
			<xsl:for-each select="$pAttribute/simpleValue">
				<QualifiedValue>
					<Value>
						<xsl:apply-templates select="value"></xsl:apply-templates>
					</Value>
					<xsl:if test="count(./attribute[@class='Term Source REF'])>0">
						<xsl:call-template name="process_term_source_ref">
							<xsl:with-param name="pName"
								select="string(.//attribute/simpleValue/value[../../@class='Term Source Name'])" />
							<xsl:with-param name="pUrl"
								select="string(.//attribute/simpleValue/value[../../@class='Term Source URI'])" />
							<xsl:with-param name="pVersion"
								select="string(.//attribute/simpleValue/value[../../@class='Term Source Version'])" />
							<xsl:with-param name="pId"
								select="string(.//attribute/simpleValue/value[../../@class='Term Source ID'])" />
						</xsl:call-template>
					</xsl:if>
					<xsl:if test="count(./attribute[@class='Unit'])>0">
						<Unit>
							<xsl:copy-of select="string(./attribute[@class='Unit']/simpleValue/value)"></xsl:copy-of>
						</Unit>
					</xsl:if>
				</QualifiedValue>
			</xsl:for-each>
		</Property>

	</xsl:template>




	<xsl:template name="process_derived_from">
		<xsl:param name="pAttribute" />

		<xsl:for-each select="$pAttribute/simpleValue/value">
			<derivedFrom>
				<xsl:copy-of select="string(.)"></xsl:copy-of>
			</derivedFrom>
		</xsl:for-each>
	</xsl:template>


</xsl:stylesheet>