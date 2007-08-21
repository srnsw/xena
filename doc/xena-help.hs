<?xml version='1.0' encoding='ISO-8859-1' ?>
<!DOCTYPE helpset
  PUBLIC "-//Sun Microsystems Inc.//DTD JavaHelp HelpSet Version 1.0//EN"
         "http://java.sun.com/products/javahelp/helpset_1_0.dtd">

<helpset version="1.0">
     <title>Xena Help</title>
	
     <maps>
          <mapref location="map.jhm"/>
          <homeID>start</homeID>
     </maps>

    <view mergetype="javax.help.UniteAppendMerge">
         <name>TOC</name>
         <label>TOC</label>
         <type>javax.help.TOCView</type>
         <data>toc.xml</data>
     </view>

 <!--  Probably no need for a help index for Xena Lite
	  <view>
          <name>Index</name>
          <label>Index</label>
          <type>javax.help.IndexView</type>
          <data>index.xml</data>
     	</view> 
-->

 <!-- Probably no need for a full text search for Xena Lite
		<view>
    		<name>Search</name>
    		<label>Xena Word Search</label>
    		<type>javax.help.SearchView</type>
    		<data engine="com.sun.java.help.search.DefaultSearchEngine">
       		JavaHelpSearch
  		</data>
		</view> 
-->

</helpset>
  
