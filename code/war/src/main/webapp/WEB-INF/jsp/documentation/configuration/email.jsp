<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<table class="table-content" width="100%">
	<tr>
		<td class="top-content" colspan="2">
			<span class="top-content-header">configuration</span>
			<span class="date" style="float: right;"><script type="text/javascript">writeDate();</script></span>
		</td>
	</tr>
	<tr>
		<td colspan="2">
			<strong>configuration</strong>&nbsp;
			Internet and file system configurations are not particularly difficult, but database configuration is not trivial and generally would 
			need a developer to administer. Over and above that he/she would need to have knowledge of Spring. Please refer to 
			the <a href="<c:url value="/documentation/configuration.html" />" >quick start</a> to get Ikube running in a Tomcat as the 
			configuration tutorial will use this as a starting point.
		</td>
	</tr>
	<tr>
		<td colspan="2">
			All configuration is done via Spring configuration files. For a first configuration please download the configuration files at 
			<a href="<c:url value="/docs/configuration.jar" />" >configuration.jar</a>. Unpack the jar to the bin folder of Tomcat. 
			Ikube will first check the startup directory or dot directory for a './ikube/spring.xml' file. If this file is found then it will be used for the 
			configuration. This allows the configuration to be external to the war/jar.
			<br><br>
			If the spring.xml file is not found in the bin(startup) directory then the files that are packaged with the ikube-core.jar are used.
		</td>
	</tr>
	<tr>
		<td colspan="2">
			Ikube can index data bases, internet/intranet, file systems and email. For each internet site, database or file share you need to 
			define an indexable. The exact structure of the configuration files is not important, all you need to do is add your configuration file 
			to the $TOMCAT_INSTALL_BIN/ikube directory, and call it spring-client-*.xml replacing the asterisk with a meaningful name for 
			your configuration, perhaps spring-client-custom.xml for example. 
			
			There is a spring-client.xml file that is a working example of a configuration. This file can be modified to add your internet sites 
			and databases.
 		</td>
	</tr>
	
	<tr><td colspan="2">&nbsp;</td></tr>
	
	<tr>
		<th colspan="2">Index context definition parameters</th>
	</tr>
	<tr>
		<td colspan="2">
			The index context is the top level object in the configuration. It will contain the internet site configurations and database 
			configurations, essentially a wrapper for a Lucene index:<br><br>
			
			<img src="<c:url value="/images/index.context.xml.jpg" />" alt="The default index context" /><br><br>
		</td>
	</tr>
	
	<tr>
		<th>Parameter</th>
		<th>Description</th>
	</tr>
	<tr>
		<td>indexName</td>
		<td> 
			This is the name of the index. This must be unique in the configuration, i.e. there can't be two indexes with 
			the name of 'MyIndex'. This will be used for accessing the correct index during the search and all other system 
			related functions.
		</td>
	</tr>
	<tr>
		<td> maxAge</td>
		<td> 
			The maximum age the index can become before a new one is generated. An index is triggered by an index 
			passing the maximum age. This is defined in milli seconds. 
		</td>
	</tr>
	<tr>
		<td>compoundFile</td>
		<td> 
			Whether Lucene should use a compound file or not. During the indexing there will be several files generated by Lucene. At the 
			end the index will be optimized and potentially all the files will be merged into one file. With large indexes this is quite a long process, 
			spanning hours. For more information on why and when to use a compound file please refer to the 
			<a href="http://lucene.apache.org/java/docs/index.html" target="_top">Lucene</a> documentation.
		</td>
	</tr>
	<tr>
		<td> bufferedDocs</td>
		<td> 
			Refer to the <a href="http://lucene.apache.org/java/docs/index.html" target="_top">Lucene</a> documentation for information 
			on the buffered documents. This is the number of documents that will be stored in memory before being committed to the 
			index during indexing. Generally though you don't need to change this, the default is 1000, which is fine for most users.
		</td>
	</tr>
	<tr>
		<td> bufferSize</td>
		<td> 
			Refer to the <a href="http://lucene.apache.org/java/docs/index.html" target="_top">Lucene</a> documentation for information 
			on the buffer size. This is the maximum size of the memory that Lucene can use before it commits the documents. A 
			fail safe for out of memory errors. Once again the default is probably fine for most users at 128 meg.
		</td>
	</tr>
	<tr>
		<td>batchSize</td>
		<td> 
			The number of records to retrieve from the database in each batch. For each batch there is a select on the database, increasing 
			the batch size means less selects and larger result sets. This does not have a very large performance influence unless the batch size 
			is 1 for example as selects are generally fast. However please note that if you have a database that has blobs in it, and you have a batch 
			size of 10 000, the database will materialize the blob in some cases, like Db2, and if the blobs are 5 000 bytes then the database will 
			need 50 000 000 bytes available. In Db2 this will throw a -4477 error if I remember correctly. As well as this the table handler is multi 
			threaded which will compound the effect. The recommended batch size is 1000.
		</td>
	</tr>
	<tr>
		<td>internetBatchSize</td>
		<td> 
			The batch size for the crawler. Each thread will get a batch of urls to read and index. Once again there is no real performance gain 
			between 100 and 1000 in the batch size. The default is 100, this should e fine for most users.
		</td>
	</tr>
	<tr>
		<td> mergeFactor</td>
		<td> 
			Refer to the <a href="http://lucene.apache.org/java/docs/index.html" target="_top">Lucene</a> documentation for information 
			on the merge factor. Essentially it is the number of segments that are kept in memory during the merge, which could be 
			when the index is optimized or committed.
		</td>
	</tr>
	<tr>
		<td> maxFieldLength</td>
		<td>
			Refer to the <a href="http://lucene.apache.org/java/docs/index.html" target="_top">Lucene</a> documentation for information 
			on the maximum field length.
		</td>
	</tr>
	<tr>
		<td>maxReadLength</td>
		<td> 
			The maximum size to read from any resource, could be a blob in the database or a file on the file system. Indexing requires that 
			the data be stored in memory, certainly for PDF files for example. As such this parameter is quite important. In cases where there 
			is a PDF document of several hundred megabytes and 20 threads crawling the database, this would result in an out of  memory error. 
			Unfortunately this also means that there will be some data lost if the files are very big. Lucene can handle readers as input, which 
			can be on the file system but the performance loss is too great, certainly with large volumes is was found not to be practical. This 
			parameter should be set with due care. The recommended size is 10 meg, keeping in mind that this will result in a least 1 gig of 
			memory required for Ikube. In a high volume environment the recommended memory for Ikube would be dependant on the volumes 
			but at least 3 gig.
		</td>
	</tr>
	<tr>
		<td> throttle</td>
		<td> 
			This parameter defines the time each thread will sleep between index items. In the case of the web crawler, 
			 to avoid stressing the server, each thread will sleep between reading urls. In the case of a database index each 
			 thread will sleep between records for this period of time, defined in milli seconds. 
		</td>
	</tr>
	<tr>
		<td>indexDirectoryPath</td>
		<td> 
			The path to the indexes. This path combined with the index name will determine the exact location on the file system where  
			the index is written. This path can be relative, for example ./indexes. In this case Ikube will create the directory that it needs in 
			the dot folder, i.e. where the Jvm was started. In the case of a Tomcat install this folder will be TOMCAT_INSTALL/bin/indexes. 
			In the case of a cluster the path needs to be defined for all the servers, and in the same place as in the default configuration, it is 
			D:/cluster/indexes. All the servers will then write their portion of the index to this position, and consequently they will also all read 
			and search the index at this location. To reiterate, for a single server the relative path ./indexes is fine but for a cluster the path 
			needs to be on the network something like Z:/path/to/indexes. 
		</td>
	</tr>
	<tr>
		<td>indexDirectoryPathBackup</td>
		<td> 
			The path to the backup indexes. As in the above this needs to be defined differently for a single server and a cluster. This is as the 
			name suggests the backup indexes. Generally an index will take several hours if not days and weeks to generate, in the case that the 
			index becomes corrupt or the disk fails for some obscure reason, the backup index will be copied into the index path. This is an important 
			fail over and recovery parameter. Note of course that there would need to be space for two indexes at least, the current one, the backup 
			index and any indexes that are being generated.
		</td>
	</tr>
	<tr>
		<td>indexables</td>
		<td> 
			These are the sources of data that will be indexed, like a web site and a database for example. They are defined as children 
			in the index context. Indexables and their definitions are described below.
		</td>
	</tr>
	
	<tr><td colspan="2">&nbsp;</td></tr>
	
	<tr>
		<th colspan="2">Indexable internet definition parameters</th>
	</tr>
	<tr>
		<td colspan="2">
			This indexable is an internet site or an intranet site. Note that the crawler is multi-threaded but not clusterable.
		</td>
	</tr>
	<tr>
		<th>Parameter</th>
		<th>Description</th>
	</tr>
	<tr>
		<td>name</td>
		<td> 
			This is the name of the indexable, it should be unique within the configuration. It can be an arbitrary string.
		</td>
	</tr>
	<tr>
		<td>url</td>
		<td>
			The url of the site to index. Note that the host fragment of the url will be used as the base url 
			and the point to start the index. All pages and documents that are linked to this host or have the host as the 
			fragment in their url will be indexed.
		</td>
	</tr>
	<tr>
		<td>idFieldName</td>
		<td>
			The name of the field in the Lucene index for the identifier of this url. This is a field that will 
			be searched against when the index is created.
		</td>
	</tr>
	<tr>
		<td>titleFieldName</td>
		<td> 
			As above with the id field name this is the field in the Lucene index that will be searched against 
			for the title of the document. In the case of an HTML page the title tag. In the case of a word document 
			the parser will attempt to extract the title from the document for this field and so on.
		</td>
	</tr>
	<tr>
		<td>contentFieldName</td>
		<td> 
			The name of the lucene content field for the documents. When searching this index the field 
			and search string will be logically something like 'where {contentFieldName} = {searchString}'. 
		</td>
	</tr>
	<tr>
		<td>excludedPattern</td>
		<td> 
			Patterns that will be excluded from the indexing process. If there are files that should not be 
			indexed like images for example this can be used to exclude them from the indexing process.
		</td>
	</tr>
	<tr>
		<td>analyzed</td>
		<td> 
			Whether the data will be analyzed by Lucene before being written to the index. Typically this 
			will be true. For more information on the Lucene parameters please refer to the Lucene documentation.
		</td>
	</tr>
	<tr>
		<td>stored</td>
		<td> 
			Whether to store the data in the index. This will also typically be true as the fragment of text 
			returned by the search results will need the stored data to generate the fragment. However in the 
			case of very large document sets this will increase the index size considerably and my not be necessary.
		</td>
	</tr>
	<tr>
		<td>vectored</td>
		<td> 
			Whether the data from the documents will be vectored by Lucene. Please refer to the Lucene 
			documentation for more details on this parameter.
		</td>
	</tr>
	
	<tr><td colspan="2">&nbsp;</td></tr>
	
	<tr>
		<th colspan="2">Indexable file system definition parameters</th>
	</tr>
	<tr>
		<td colspan="2">
			This indexable definition is for a file share. It can be on the local machine or on the network. For a local directory 
			the path to the folder will be /path/to/folder and on the network would be something like //computer.name/path/to/folder.
		</td>
	</tr>
	<tr>
		<th>Parameter</th>
		<th>Description</th>
	</tr>
	<tr>
		<td>name</td>
		<td>
			The uniqiue name of this indexable in the configuration.
		</td>
	</tr>
	<tr>
		<td>path</td>
		<td>
			The absolute or relative path to the file or folder to index. This can be accross the network 
			provided the drive is mapped to the machine where Ikube is running.
		</td>
	</tr>
	<tr>
		<td>pathFieldName</td>
		<td>
			The name in the Lucene index of the path to the file that is being indexed.
		</td>
	</tr>
	<tr>
		<td>nameFieldName</td>
		<td>
			The name in the Lucene index of the name of the file.
		</td>
	</tr>
	<tr>
		<td>lengthFieldname</td>
		<td>
			The name of the field in the Lucene index of the length of the file, ie. the size of it. 
		</td>
	</tr>
	<tr>
		<td>contentFieldName</td>
		<td>
			The name of the field in the Lucene index for the fiel content. This is typically the important field 
			that will be searched once the index is created.
		</td>
	</tr>
	<tr>
		<td>excludedPattern</td>
		<td>
			Any excluded patterns that would be excluded from the indexing process, like for example 
			exe files and video as Ikube can't index video just yet, although there is some investigation into 
			this at the moment.
		</td>
	</tr>
	<tr>
		<td>lastModifiedFieldName</td>
		<td>
			The name of the field in the Lucene index for the last modified timestamp of the file being indexed.
		</td>
	</tr>
	
	<tr><td colspan="2">&nbsp;</td></tr>
	
	<tr>
		<th colspan="2">Indexable table definition parameters</th>
	</tr>
	<tr>
		<td colspan="2"> 
			The table and database definition is the primary focus of Ikube. Ikube is designed to index databases in arbitrary complex structures. 
			First a table must be defined in the Spring configuration, including the data source as in the following:<br><br>
			
			<img src="<c:url value="/images/geoname.xml.jpg" />" alt="The geospatial table" /><br><br>
			
			The id and the class are for Spring, the rest of the properties of the bean are user defined, described below.
		</td>
	</tr>
	
	<tr>
		<th>Parameter</th>
		<th>Description</th>
	</tr>
	<tr>
		<td>name</td>
		<td> 
			The name of the table. This will be used to generate the sql to access the table.
		</td>
	</tr>
	<tr>
		<td>predicate</td>
		<td> 
			 This is an optional parameter where a predicate can be defined to limit the results. A typical example is 
			 where faq.faqid &lt; 10000. Please see the spring-client.xml file for an example. 
		</td>
	</tr>
	<tr>
		<td>primary</td>
		<td> 
			Whether this table is a top level table. This will determine when the data collected while accessing the table 
			hierarchy will be written to the index. Sub tables are iterated over, the data is collected, and when the logic reaches 
			the top level table the data will be passed to Lucene in the form of a document.
		</td>
	</tr>
	<tr>
		<td>address</td>
		<td> 
			This flag indicates that the table and possibly columns and even sub tables form part of a physical address. Addresses 
			are used for GeoSpatial functionality. Address tables and the data contained in the columns are concatenated, a search 
			is done against the Ikube GeoSpatial index to find the closest match for the address and the latitude and longitude 
			properties for the address are added to the Lucene index. This facilitates searching for results around a point and ordering 
			them according to distance from that point. More information on how to configure the index with geospatial functionality 
			is available on the GeoSpatial page of the documentation(todo when the static ip is configured and the geospatial data 
			is enhanced).
		</td>
	</tr>
	<tr>
		<td>dataSource</td>
		<td> 
			The reference to the datasource where the table is. The datasource must be defined in the Spring configuration, using 
			perhaps C3p0 as the pooled datasource provider. Please see below the shot of the data source definition in the Spring 
			configuration:<br><br>
			
			<img src="<c:url value="/images/geoname.datasource.xml.jpg" />" alt="The geospatial data source" /><br><br>
			
			As you can see the properties for the database are quite self explanatory and common for databases per se.
		</td>
	</tr>
	<tr>
		<td>children</td>
		<td> 
			The children of the table. This is a list of mainly columns but also the sub tables will be defined in the child list 
			for the table. Please note the screen shot below which is of some column definitions for the GeoName table, the 
			id column and the name column:<br><br>
			
			<img src="<c:url value="/images/geoname.columns.xml.jpg" />" alt="The geospatial columns" /><br><br>
		</td>
	</tr>
	<tr>
		<td colspan="2">
			As mentioned previously the sql to access the data is generated from the configuration. Tables can be nested within each 
			other as is normally the case with tables in a relational database. If a table is defined as a primary table and a child table is added 
			to the parent table then Ikube, while iterating over the results from the parent table, select related records from the child table(s) 
			and add the data to the parents' index documents.<br><br>
			
			In the spring-client.xml configuration file is the definition of the 'faq' and 'attachment' tables. These are an example 
			of the table nesting in the configuration.
			
			The result of this is a Lucene document with the following fields and values:<br><br>
					
			&lt;{id=faq.1}, {question=where is Paris}, {answer=In France}, {name=documentOne.doc}, 
			{attachment=Paris and Lyon are both situated in France}&gt;<br><br>
					
			The configuration of tables can be arbitrarily complex, nesting depth can be up to 10 tables or more.<br><br>
		</td>
	</tr>
	
	<tr><td colspan="2">&nbsp;</td></tr>
	
	<tr>
		<td colspan="2">
			Columns are defined and added to the tables as children. Below is a table of parameters that can be defined for columns.
		</td>
	</tr>
	<tr>
		<th colspan="2">Indexable column definition parameters</th>
	</tr>
	<tr>
		<th>Parameter</th>
		<th>Description</th>
	</tr>
	<tr>
		<td>name</td>
		<td> 
			The name of the column.
		</td>
	</tr>
	<tr>
		<td>idColumn</td>
		<td> 
			Whether this is the id column in the table.
		</td>
	</tr>
	<tr>
		<td>nameColumn</td>
		<td> 
			This is used during indexing. For example in the case where there is a blob in the attachments table, and the name of the document 
			is in the 'name' column, this parameter is used to determine the mime type. If the name is document.doc, and there is a blob of the document 
			data then during indexing the .doc suffix will be used to get the correct parser to extract the text from the document, i.e. the Word parser.
		</td>
	</tr>
	<tr>
		<td>fieldName</td>
		<td> 
			 The name of the field in the Lucene index. This allows columns to have separate field names, increasing the flexibility when searching. For 
			 example if there are timestamps for creation and they are defined as separate Lucene fields then searches like timestamp &gt; 12/12/2010 
			 AND timestamp &lt; 12/12/2011 are possible.
		</td>
	</tr>
	<tr>
		<td>address</td>
		<td> 
			 As described above in the address field definition for the table, this flag is used to add the column data to the accumulated data for the 
			 address. The eventual data collected for the address will be used to search the geospatial index to find the co-ordinates. Typically an address 
			 column will be the name and number of a street, the city and the country. 
		</td>
	</tr>
	<tr>
		<td>foreignKey</td>
		<td> 
			  The reference to the foreign key in the 'parent' table. This is used to select the records from the 'child' table referring to the parent id.
		</td>
	</tr>
	<tr>
		<td>analyzed</td>
		<td> 
			A Lucene parameter, whether the data should be analyzed. Generally this is true.
		</td>
	</tr>
	<tr>
		<td>stored</td>
		<td> 
			A Lucene parameter, whether the data should be stored in the index. Generally this is true. Of course if there is very large volumes of data 
			then storing the data could be prohibitively expensive, in terms of disk space and time. The write time of the index is a large proportion of the 
			indexing time.   
		</td>
	</tr>
	<tr>
		<td>vectored</td>
		<td> 
			A Lucene parameter, whether the data should vectored. Generally this is true.
		</td>
	</tr>
	<tr>
		<td colspan="2">Please refer to the default configuration for a complete example of a nested table configuration.</td>
	</tr>
	
	<tr><td colspan="2">&nbsp;</td></tr>
	
	<tr>
		<th colspan="2">Indexable email definition parameters</th>
	</tr>
	<tr>
		<td colspan="2">
			This indexable is to index the content in an email account.
		</td>
	</tr>
	<tr>
		<th>Parameter</th>
		<th>Description</th>
	</tr>
	<tr>
		<td>mailHost</td>
		<td>
			The mail host url to access the mail account.
		</td>
	</tr>
	<tr>
		<td>name</td>
		<td>
			The unique name of the indexable, this can be an arbitrary value.
		</td>
	</tr>
	<tr>
		<td>port</td>
		<td>
			The port to use for accessing the mail account. In the case of Google mail for example this is 995. This 
			has to be gotten from the mail provider.
		</td>
	</tr>
	<tr>
		<td>protocol</td>
		<td>
			The protocol to use. Also with Gmail the protocol is pop3 but is different for Hotmail and others, could be 
			imap for example.
		</td>
	</tr>
	<tr>
		<td>secureSocketLayer</td>
		<td>
			Whether to use secure sockets. Generally this will be true but need not be.
		</td>
	</tr>
	<tr>
		<td>password</td>
		<td>
			The password for the account.
		</td>
	</tr>
	<tr>
		<td>username</td>
		<td>
			The user account. In the case of the default mail account in the configuration this is ikube.ikube@gmail.com.
		</td>
	</tr>
	<tr>
		<td>idField</td>
		<td>
			The id field name in the Lucene index for the identifier of the message. The id is a concatenation of the 
			mail account, the message number and the user name.
		</td>
	</tr>
	<tr>
		<td>contentField</td>
		<td>
			The field name of the content field in the Lucene index. This is where the message data like the content and 
			the header will be added to the index. 
		</td>
	</tr>
	<tr>
		<td>titleField</td>
		<td>
			The name of the title field in the Lucene index.
		</td>
	</tr>
	<tr>
		<td></td>
		<td> 
		</td>
	</tr>
	
	<tr><td colspan="2">&nbsp;</td></tr>
	
	<tr>
		<td colspan="2">Please note the other configuration possibilities for mail notification etc. on the 
		extra page in the links at the top.</td>
	</tr>
</table>
