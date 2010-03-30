<?xml version="1.0"?>

  <!--
   * Licensed to the Apache Software Foundation (ASF) under one
   * or more contributor license agreements. See the NOTICE file
   * distributed with this work for additional information
   * regarding copyright ownership. The ASF licenses this file
   * to you under the Apache License, Version 2.0 (the  "License");
   * you may not use this file except in compliance with the License.
   * You may obtain a copy of the License at
   *
   *     http://www.apache.org/licenses/LICENSE-2.0
   *
   * Unless required by applicable law or agreed to in writing, software
   * distributed under the License is distributed on an "AS IS" BASIS,
   * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   * See the License for the specific language governing permissions and
   * limitations under the License.
  -->

<!--    XSLT stylesheet to convert the Xalan documentation collected in one xml file into a fo file
        for use in FOP 

TBD: - The faq doesn't show in the content
     - check why margin-bottom on the page with properties is too large
     - check why keep-next not only doesn't work, but leads to repeating already printed lines
     - make lines containing only code look nicer (smaller line height)
     - replace bullets in ordered lists with numbers
     - correct the hack replacing nbsp with '-'
     - handle the links correctly which have been external in the html doc and are now internal

-->

<xsl:stylesheet
     xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
     xmlns:fo="http://www.w3.org/1999/XSL/Format">
     
     <!-- to use with document() to get resources.xml -->
    <xsl:param name="resourceFile" select="'../../sources/xalan/resources.xml'"/>
    <xsl:param name="project" select="Xalan"/>
              
<xsl:template match ="/">
	<fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format">

	  <!-- defines page layout -->
	  <fo:layout-master-set>
		<fo:simple-page-master master-name="simple"
							   page-height="29.7cm" 
							   page-width="21cm"
							   margin-top="1.5cm" 
							   margin-bottom="2cm" 
							   margin-left="2.5cm" 
							   margin-right="2.5cm">
		  <fo:region-body margin-top="3cm"/>
		  <fo:region-before extent="1.5cm"/>
		  <fo:region-after extent="1.5cm"/>
		</fo:simple-page-master>
	  </fo:layout-master-set>

	  <fo:page-sequence master-name="simple">
		<fo:static-content flow-name="xsl-region-before">
			<fo:block text-align="end" 
			    	  font-size="10pt" 
					  font-family="serif" 
					  line-height="14pt" >
				<xsl:value-of select="$project"/> documentation - p. <fo:page-number/>
			</fo:block>
		</fo:static-content> 

		<fo:flow flow-name="xsl-region-body">

          <fo:block font-size="18pt" 
                    font-family="sans-serif" 
                    line-height="24pt"
                    space-after.optimum="15pt"
                    background-color="blue"
                    color="white"
                    text-align="center">
            <xsl:value-of select="$project"/> - an XSL Transformer
          </fo:block>


        <!-- generates table of contents and puts it into a table -->

         <fo:block font-size="14pt" 
                  font-family="sans-serif" 
                  line-height="18pt"
                  space-after.optimum="10pt"
                  font-weight="bold"
                  start-indent="15pt">
            Content
         </fo:block>

         <fo:table>
            <fo:table-column column-width="1cm"/>
            <fo:table-column column-width="10cm"/>
            <fo:table-column column-width="5cm"/>
            <fo:table-body font-size="12pt" 
                           line-height="16pt"
                           font-family="sans-serif">
              <fo:table-row>
                  <fo:table-cell>
                     <fo:block text-align="end" >
                     </fo:block>
                  </fo:table-cell>
                  <fo:table-cell>
                     <fo:block text-align="end" >
                     </fo:block>
                  </fo:table-cell>
                  <fo:table-cell>
                     <fo:block text-align="end" >
                     page
                     </fo:block>
                  </fo:table-cell>
              </fo:table-row>                                 
              <xsl:for-each select="documentation/chapter"> 
                <fo:table-row>
                  <fo:table-cell>
                     <fo:block text-align="end" >
                        <xsl:number value="position()" format="I"/>.  
                     </fo:block>
                  </fo:table-cell>
                  <fo:table-cell>
                     <fo:block  text-align="start" >
                        <fo:basic-link color="blue">
                           <xsl:attribute name="internal-destination">
                           <xsl:value-of select="@id"/>
                           </xsl:attribute>
                          <xsl:value-of select="s1/@title|faqs/@title"/>
                        </fo:basic-link>
                     </fo:block>
                  </fo:table-cell>
                  <fo:table-cell>
                     <fo:block text-align="end">                                
                       <fo:page-number-citation ref-id="{@id}"/>
                     </fo:block>
                  </fo:table-cell>
               </fo:table-row>
            </xsl:for-each>
            </fo:table-body>
         </fo:table>
		 <xsl:apply-templates/> 
	   </fo:flow>
	   </fo:page-sequence>
	</fo:root>
</xsl:template>

<!--chapter-->
<xsl:template match="chapter">
  <fo:block id="{@id}" break-before="page"/>
	<xsl:apply-templates/>   
</xsl:template>  

<!-- s1 -->
<xsl:template match ="s1">
   <fo:block font-size="18pt" 
            font-family="sans-serif" 
            line-height="24pt"
            space-before.optimum="15pt"
            space-after.optimum="15pt"
            background-color="blue"
            color="white"
            keep-with-next.within-page="always"
            text-align="center">
     <xsl:attribute name="id">
     <xsl:value-of select="translate(@title,' ),-.(','____')"/>
     </xsl:attribute>
     <xsl:value-of select="@title"/>
   </fo:block>
    <xsl:apply-templates/> 
</xsl:template>

<!-- s2 -->
<xsl:template match ="s2">
   <fo:block font-size="16pt" 
            font-family="sans-serif" 
            line-height="20pt"
            keep-with-next.within-page="always"            
            space-before.optimum="15pt"
            space-after.optimum="12pt"
            text-align="start"
            padding-top="3pt"
            >
     <xsl:value-of select="@title"/>
   </fo:block>
    <xsl:apply-templates/> 
</xsl:template>

<!-- s3 -->
<xsl:template match ="s3">
   <fo:block font-size="14pt" 
            font-family="sans-serif" 
            line-height="18pt"
            keep-with-next.within-page="always"
            space-before.optimum="10pt"
            space-after.optimum="9pt"
            text-align="start"
            padding-top="3pt">
     <xsl:value-of select="@title"/>
   </fo:block>
    <xsl:apply-templates/> 
</xsl:template>

<!-- p  [not(code)] -->
<xsl:template match ="p"> 
   <fo:block font-size="11pt" 
            font-family="sans-serif" 
            line-height="13pt"
            space-after.optimum="3pt"
            space-before.optimum="3pt"
            text-align="start">
     <xsl:apply-templates/> 
   </fo:block>
</xsl:template>

<xsl:template match ="note"> 
   <fo:block font-size="11pt" 
            font-family="sans-serif" 
            font-weight="italic"
            line-height="13pt"
            space-after.optimum="3pt"
            space-before.optimum="3pt"
            text-align="start">
        Note:     
     <xsl:apply-templates/> 
   </fo:block>
</xsl:template>

<xsl:template match="anchor">
  <fo:block> <!--id="concat(local-name(ancestor::node()="chapter"/@id.,'_',{./@name}"/-->
    <xsl:attribute name="id">
      <xsl:value-of select="concat(ancestor::chapter/@id,'_',./@name)"/>
    </xsl:attribute>
  </fo:block>    
</xsl:template>

<xsl:template match="table">
  <xsl:variable name="colwidth" select="14.5 div count(tr[1]/td)"/>
  <fo:table>
  <xsl:for-each select="tr[1]/td">
    <fo:table-column column-width="{$colwidth}cm"/>
  </xsl:for-each>
  <fo:table-body font-size="10pt" font-family="sans-serif">
  <xsl:apply-templates/>
  </fo:table-body>
  </fo:table>  
</xsl:template>  
<xsl:template match="tr">
  <fo:table-row>
    <xsl:apply-templates/>
  </fo:table-row>
</xsl:template>
<xsl:template match="td">
  <fo:table-cell>
    <fo:block>
      <xsl:apply-templates/>
    </fo:block>
   </fo:table-cell>
</xsl:template>

<!-- p + code 
<xsl:template match ="p[code]">
   <fo:block font-size="11pt" 
            font-family="sans-serif" 
            line-height="11pt"
            space-after.optimum="0pt"
            space-before.optimum="0pt"
            text-align="start">
     <xsl:apply-templates/> 
   </fo:block>
</xsl:template>
-->

<xsl:template match="img">
  <fo:block>
    <fo:external-graphic src="file:build/docs/images/{@src}"/>
  </fo:block>
</xsl:template>

<!-- faqs -->
<xsl:template match ="faqs">
   <fo:block font-size="18pt" 
            font-family="sans-serif" 
            line-height="24pt"
            space-before.optimum="15pt"
            space-after.optimum="15pt"
            background-color="blue"
            color="white"
            text-align="center"
            >
     <xsl:attribute name="id">
     <xsl:value-of select="translate(.,' ),-.(','____')"/>
     </xsl:attribute>
     <xsl:value-of select="@title"/>
   </fo:block>
    <xsl:apply-templates/> 
</xsl:template>

<!-- faq -->
<xsl:template match ="faq">
    <xsl:apply-templates/> 
</xsl:template>

<!-- q in faq -->
<xsl:template match="q">
   <fo:block font-size="14pt" 
            font-family="sans-serif" 
            line-height="18pt"
            keep-with-next.within-page="always"
            space-before.optimum="10pt"
            space-after.optimum="9pt"
            text-align="start"
            padding-top="3pt">     
      <xsl:apply-templates/> 
    </fo:block>
</xsl:template>

<!-- a in faq -->
<xsl:template match ="a">
      <xsl:apply-templates/> 
</xsl:template>


<!-- jump (links) -->
<xsl:template match ="jump">
  <fo:basic-link color="blue">
     <xsl:attribute name="external-destination">
       <xsl:if test="starts-with(@href,'apidocs')">
         <xsl:value-of select="concat('http://xml.apache.org/xalan-j/',@href)"/>
       </xsl:if>
       <xsl:if test="not(starts-with(@href,'apidocs'))">
         <xsl:value-of select="@href"/>
       </xsl:if>    
     </xsl:attribute>  
   <xsl:value-of select="./text()"/>
     <!--xsl:apply-templates/--> 
   </fo:basic-link>
</xsl:template>

  <xsl:template match="link">
    <xsl:if test="string-length(@anchor)=0">
      <xsl:if test="string-length(@idref)=0">
        <xsl:apply-templates/>
      </xsl:if>
      <xsl:if test="string-length(@idref)>0">
        <fo:basic-link color="blue" internal-destination="{@idref}">
        <xsl:value-of select="./text()"/></fo:basic-link>
      </xsl:if>
    </xsl:if>

    <xsl:if test="string-length(@anchor)>0">
      <xsl:if test="string-length(@idref)=0">
        <fo:basic-link color="blue"
                      internal-destination="{concat(ancestor::chapter/@id,'_',@anchor)}">
        <xsl:value-of select="./text()"/></fo:basic-link>
      </xsl:if>
      <xsl:if test="string-length(@idref)>0">
        <fo:basic-link color="blue" internal-destination="{@idref}_{@anchor}">
        <xsl:value-of select="./text()"/></fo:basic-link>
      </xsl:if>
    </xsl:if>
  </xsl:template>


<xsl:template match="resource-ref">
  <xsl:variable name="xref" select="@idref"/>
  <xsl:variable name="href"
          select="document($resourceFile)/resources/resource[@id=$xref]/@location"/>
  <xsl:variable name="label"
          select="document($resourceFile)/resources/resource[@id=$xref]/@title"/>
  <fo:basic-link color="blue">
     <xsl:attribute name="external-destination">
       <xsl:if test="starts-with($href,'apidocs')">
         <xsl:value-of select="concat('http://xml.apache.org/xalan-j/',$href)"/>
       </xsl:if>
       <xsl:if test="not(starts-with($href,'apidocs'))">
         <xsl:value-of select="$href"/>
       </xsl:if>    
     </xsl:attribute>  
     <xsl:value-of select="$label"/>
     <!--xsl:apply-templates/--> 
   </fo:basic-link>        
  </xsl:template>

  <xsl:template match="human-resource-ref">
    <xsl:variable name="resourceFile" 
          select="./xalan/resources.xml"/>  
    <xsl:variable name="ref"  select="@idref"/>
    <xsl:variable name="mailto"
          select="document($resourceFile)/resources/human-resource[@id=$ref]/@mailto"/>
    <xsl:variable name="name"
          select="document($resourceFile)/resources/human-resource[@id=$ref]/@name"/>
  <fo:basic-link color="blue" external-destination="mailto:{$mailto}">
     <xsl:value-of select="$name"/>
     <!--xsl:apply-templates/--> 
   </fo:basic-link>          
  </xsl:template>

<xsl:template match ="source"> 
   <fo:block font-size="10pt" 
            font-family="Courier" 
            text-align="start"
            white-space-collapse="false">
     <xsl:apply-templates/> 
   </fo:block>

</xsl:template>
  <xsl:template match ="br">
  <fo:block></fo:block>
</xsl:template>

<!-- code -->
<xsl:template match ="*/code">
   <fo:inline font-size="10pt" 
            font-family="Courier">
     <xsl:apply-templates/> 
   </fo:inline>
</xsl:template>

<!-- ul (unordered list) -->
<xsl:template match ="ul">
  <fo:list-block start-indent="1cm" 
                 provisional-distance-between-starts="12pt" 
                 font-family="sans-serif" 
                 font-size="11pt" 
                 line-height="11pt">
     <xsl:apply-templates/> 
   </fo:list-block>
</xsl:template>         
          
<!-- ol (ordered list) -->
<xsl:template match ="ol">
  <fo:list-block start-indent="1cm" 
                 provisional-distance-between-starts="12pt" 
                 font-family="sans-serif" 
                 font-size="11pt" 
                 line-height="11pt">
     <xsl:apply-templates/> 
   </fo:list-block>
</xsl:template>


<!-- li (list item) in unordered list -->
<xsl:template match ="ul/li">
    <fo:list-item>
      <fo:list-item-label>
        <fo:block><fo:inline font-family="Symbol">&#183;</fo:inline></fo:block>
      </fo:list-item-label>
      <fo:list-item-body>
        <fo:block space-after.optimum="4pt"
              text-align="start"
              padding-top="3pt">
          <xsl:apply-templates/> 
       </fo:block>
      </fo:list-item-body>
    </fo:list-item>
</xsl:template>

<!-- li (list item) in ordered list -->
<xsl:template match ="ol/li">
    <fo:list-item>
      <fo:list-item-label>
        <fo:block>
          <xsl:number level="multiple" count="li" format="1"/>)
        </fo:block>
      </fo:list-item-label>
      <fo:list-item-body>
        <fo:block space-after.optimum="4pt"
              text-align="start"
              padding-top="3pt">
          <xsl:apply-templates/> 
       </fo:block>
      </fo:list-item-body>
    </fo:list-item>
</xsl:template>

<!-- temporary kludge for definition list gloss with label-item pairs ) -->

<xsl:template match="gloss">
  <xsl:apply-templates/>
</xsl:template>
<xsl:template match="label">
   <fo:block font-size="11pt" 
            font-family="sans-serif" 
            line-height="13pt"
            space-after.optimum="3pt"
            space-before.optimum="6pt"
            text-align="start">
     <xsl:apply-templates/> 
   </fo:block>
</xsl:template> 
<xsl:template match="item">
   <fo:block font-size="11pt" 
            font-family="sans-serif" 
            line-height="13pt"
            space-after.optimum="6pt"
            space-before.optimum="0pt"
            margin-left="24pt"
            text-align="start">
     <xsl:apply-templates/>
     <fo:block></fo:block> 
   </fo:block>
</xsl:template> 

<!-- end body -->

</xsl:stylesheet>
