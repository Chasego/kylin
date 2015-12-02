---
layout: docs
title:  Release Notes
categories: gettingstarted
permalink: /docs/release_notes.html
version: v1.0
since: v0.7.1
---

To download latest release, please visit: [http://kylin.apache.org/download/](http://kylin.apache.org/download/), there are source code package, binary package, ODBC driver and installation guide avaliable.

Any problem or issue, please report to Apache Kylin JIRA project: [https://issues.apache.org/jira/browse/KYLIN](https://issues.apache.org/jira/browse/KYLIN)

or send to Apache Kylin mailing list:   
* User relative: [user@kylin.apache.org](mailto:user@kylin.apache.org)
* Development relative: [dev@kylin.apache.org](mailto:dev@kylin.apache.org)

## v1.0-incubating - 2015-09-06
_Tag:_ [kylin-1.0-incubating](https://github.com/apache/kylin/tree/kylin-1.0-incubating)

__New Feature__

    * [KYLIN-591] - Leverage Zeppelin to interactive with Kylin

__Bug__

    * [KYLIN-404] - Can't get cube source record size.
    * [KYLIN-626] - JDBC error for float and double values
    * [KYLIN-751] - Max on negative double values is not working
    * [KYLIN-757] - Cache wasn't flushed in cluster mode
    * [KYLIN-780] - Upgrade Calcite to 1.0
    * [KYLIN-805] - Drop useless Hive intermediate table and HBase tables in the last step of cube build/merge
    * [KYLIN-889] - Support more than one HDFS files of lookup table
    * [KYLIN-897] - Update CubeMigrationCLI to copy data model info
    * [KYLIN-898] - "CUBOID_CACHE" in Cuboid.java never flushes
    * [KYLIN-911] - NEW segments not DELETED when cancel BuildAndMerge Job
    * [KYLIN-912] - $KYLIN_HOME/tomcat/temp folder takes much disk space after long run
    * [KYLIN-914] - Scripts shebang should use /bin/bash
    * [KYLIN-915] - appendDBName in CubeMetadataUpgrade will return null
    * [KYLIN-921] - Dimension with all nulls cause BuildDimensionDictionary failed due to FileNotFoundException
    * [KYLIN-923] - FetcherRunner will never run again if encountered exception during running
    * [KYLIN-929] - can not sort cubes by [Source Records] at cubes list page
    * [KYLIN-934] - Negative number in SUM result and Kylin results not matching exactly Hive results
    * [KYLIN-935] - always loading when try to view the log of the sub-step of cube build job
    * [KYLIN-936] - can not see job step log 
    * [KYLIN-940] - NPE when close the null resouce
    * [KYLIN-945] - Kylin JDBC - Get Connection from DataSource results in NullPointerException
    * [KYLIN-946] - [UI] refresh page show no results when Project selected as [--Select All--]
    * [KYLIN-949] - Query cache doesn't work properly for prepareStatement queries

__Improvement__

    * [KYLIN-568] - job support stop/suspend function so that users can manually resume a job
    * [KYLIN-717] - optimize OLAPEnumerator.convertCurrentRow()
    * [KYLIN-792] - kylin performance insight [dashboard]
    * [KYLIN-838] - improve performance of job query
    * [KYLIN-842] - Add version and commit id into binary package
    * [KYLIN-844] - add backdoor toggles to control query behavior 
    * [KYLIN-857] - backport coprocessor improvement in 0.8 to 0.7
    * [KYLIN-866] - Confirm with user when he selects empty segments to merge
    * [KYLIN-867] - Hybrid model for multiple realizations/cubes
    * [KYLIN-880] -  Kylin should change the default folder from /tmp to user configurable destination
    * [KYLIN-881] - Upgrade Calcite to 1.3.0
    * [KYLIN-883] - Using configurable option for Hive intermediate tables created by Kylin job
    * [KYLIN-893] - Remove the dependency on quartz and metrics
    * [KYLIN-922] - Enforce same code style for both intellij and eclipse user
    * [KYLIN-926] - Make sure Kylin leaves no garbage files in local OS and HDFS/HBASE
    * [KYLIN-933] - friendly UI to use data model
    * [KYLIN-938] - add friendly tip to page when rest request failed

__Task__

    * [KYLIN-884] - Restructure docs and website
    * [KYLIN-907] - Improve Kylin community development experience
    * [KYLIN-954] - Release v1.0 (formerly v0.7.3)
    * [KYLIN-863] - create empty segment when there is no data in one single streaming batch
    * [KYLIN-908] - Help community developer to setup develop/debug environment
    * [KYLIN-931] - Port KYLIN-921 to 0.8 branch

## v0.7.2-incubating - 2015-07-21
_Tag:_ [kylin-0.7.2-incubating](https://github.com/apache/kylin/tree/kylin-0.7.2-incubating)

__Main Changes:__  
Critical bug fixes after v0.7.1 release, please go with this version directly for new case and upgrade to this version for existing deployment.

__Bug__  

    * [KYLIN-514] - Error message is not helpful to user when doing something in Jason Editor window
    * [KYLIN-598] - Kylin detecting hive table delim failure
    * [KYLIN-660] - Make configurable of dictionary cardinality cap
    * [KYLIN-765] - When a cube job is failed, still be possible to submit a new job
    * [KYLIN-814] - Duplicate columns error for subqueries on fact table
    * [KYLIN-819] - Fix necessary ColumnMetaData order for Calcite (Optic)
    * [KYLIN-824] - Cube Build fails if lookup table doesn't have any files under HDFS location
    * [KYLIN-829] - Cube "Actions" shows "NA"; but after expand the "access" tab, the button shows up
    * [KYLIN-830] - Cube merge failed after migrating from v0.6 to v0.7
    * [KYLIN-831] - Kylin report "Column 'ABC' not found in table 'TABLE' while executing SQL", when that column is FK but not define as a dimension
    * [KYLIN-840] - HBase table compress not enabled even LZO is installed
    * [KYLIN-848] - Couldn't resume or discard a cube job
    * [KYLIN-849] - Couldn't query metrics on lookup table PK
    * [KYLIN-865] - Cube has been built but couldn't query; In log it said "Realization 'CUBE.CUBE_NAME' defined under project PROJECT_NAME is not found
    * [KYLIN-873] - cancel button does not work when [resume][discard] job
    * [KYLIN-888] - "Jobs" page only shows 15 job at max, the "Load more" button was disappeared

__Improvement__

    * [KYLIN-159] - Metadata migrate tool 
    * [KYLIN-199] - Validation Rule: Unique value of Lookup table's key columns
    * [KYLIN-207] - Support SQL pagination
    * [KYLIN-209] - Merge tail small MR jobs into one
    * [KYLIN-210] - Split heavy MR job to more small jobs
    * [KYLIN-221] - Convert cleanup and GC to job 
    * [KYLIN-284] - add log for all Rest API Request
    * [KYLIN-488] - Increase HDFS block size 1GB
    * [KYLIN-600] - measure return type update
    * [KYLIN-611] - Allow Implicit Joins
    * [KYLIN-623] - update Kylin UI Style to latest AdminLTE
    * [KYLIN-727] - Cube build in BuildCubeWithEngine does not cover incremental build/cube merge
    * [KYLIN-752] - Improved IN clause performance
    * [KYLIN-773] - performance is slow list jobs
    * [KYLIN-839] - Optimize Snapshot table memory usage 

__New Feature__

    * [KYLIN-211] - Bitmap Inverted Index
    * [KYLIN-285] - Enhance alert program for whole system
    * [KYLIN-467] - Validataion Rule: Check duplicate rows in lookup table
    * [KYLIN-471] - Support "Copy" on grid result

__Task__

    * [KYLIN-7] - Enable maven checkstyle plugin
    * [KYLIN-885] - Release v0.7.2
    * [KYLIN-812] - Upgrade to Calcite 0.9.2

## v0.7.1-incubating (First Apache Release) - 2015-06-10  
_Tag:_ [kylin-0.7.1-incubating](https://github.com/apache/kylin/tree/kylin-0.7.1-incubating)

Apache Kylin v0.7.1-incubating has rolled out on June 10, 2015. This is also the first Apache release after join incubating. 

__Main Changes:__

* Package renamed from com.kylinolap to org.apache.kylin
* Code cleaned up to apply Apache License policy
* Easy install and setup with bunch of scripts and automation
* Job engine refactor to be generic job manager for all jobs, and improved efficiency
* Support Hive database other than 'default'
* JDBC driver avaliable for client to interactive with Kylin server
* Binary pacakge avaliable download 

__New Feature__

    * [KYLIN-327] - Binary distribution 
    * [KYLIN-368] - Move MailService to Common module
    * [KYLIN-540] - Data model upgrade for legacy cube descs
    * [KYLIN-576] - Refactor expansion rate expression

__Task__

    * [KYLIN-361] - Rename package name with Apache Kylin
    * [KYLIN-531] - Rename package name to org.apache.kylin
    * [KYLIN-533] - Job Engine Refactoring
    * [KYLIN-585] - Simplify deployment
    * [KYLIN-586] - Add Apache License header in each source file
    * [KYLIN-587] - Remove hard copy of javascript libraries
    * [KYLIN-624] - Add dimension and metric info into DataModel
    * [KYLIN-650] - Move all document from github wiki to code repository (using md file)
    * [KYLIN-669] - Release v0.7.1 as first apache release
    * [KYLIN-670] - Update pom with "incubating" in version number
    * [KYLIN-737] - Generate and sign release package for review and vote
    * [KYLIN-795] - Release after success vote

__Bug__

    * [KYLIN-132] - Job framework
    * [KYLIN-194] - Dict & ColumnValueContainer does not support number comparison, they do string comparison right now
    * [KYLIN-220] - Enable swap column of Rowkeys in Cube Designer
    * [KYLIN-230] - Error when create HTable
    * [KYLIN-255] - Error when a aggregated function appear twice in select clause
    * [KYLIN-383] - Sample Hive EDW database name should be replaced by "default" in the sample
    * [KYLIN-399] - refreshed segment not correctly published to cube
    * [KYLIN-412] - No exception or message when sync up table which can't access
    * [KYLIN-421] - Hive table metadata issue
    * [KYLIN-436] - Can't sync Hive table metadata from other database rather than "default"
    * [KYLIN-508] - Too high cardinality is not suitable for dictionary!
    * [KYLIN-509] - Order by on fact table not works correctly
    * [KYLIN-517] - Always delete the last one of Add Lookup page buttom even if deleting the first join condition
    * [KYLIN-524] - Exception will throw out if dimension is created on a lookup table, then deleting the lookup table.
    * [KYLIN-547] - Create cube failed if column dictionary sets false and column length value greater than 0
    * [KYLIN-556] - error tip enhance when cube detail return empty
    * [KYLIN-570] - Need not to call API before sending login request
    * [KYLIN-571] - Dimensions lost when creating cube though Joson Editor
    * [KYLIN-572] - HTable size is wrong
    * [KYLIN-581] - unable to build cube
    * [KYLIN-583] - Dependency of Hive conf/jar in II branch will affect auto deploy
    * [KYLIN-588] - Error when run package.sh
    * [KYLIN-593] - angular.min.js.map and angular-resource.min.js.map are missing in kylin.war
    * [KYLIN-594] - Making changes in build and packaging with respect to apache release process
    * [KYLIN-595] - Kylin JDBC driver should not assume Kylin server listen on either 80 or 443
    * [KYLIN-605] - Issue when install Kylin on a CLI which does not have yarn Resource Manager
    * [KYLIN-614] - find hive dependency shell fine is unable to set the hive dependency correctly
    * [KYLIN-615] - Unable add measures in Kylin web UI
    * [KYLIN-619] - Cube build fails with hive+tez
    * [KYLIN-620] - Wrong duration number
    * [KYLIN-621] - SecurityException when running MR job
    * [KYLIN-627] - Hive tables' partition column was not sync into Kylin
    * [KYLIN-628] - Couldn't build a new created cube
    * [KYLIN-629] - Kylin failed to run mapreduce job if there is no mapreduce.application.classpath in mapred-site.xml
    * [KYLIN-630] - ArrayIndexOutOfBoundsException when merge cube segments 
    * [KYLIN-638] - kylin.sh stop not working
    * [KYLIN-639] - Get "Table 'xxxx' not found while executing SQL" error after a cube be successfully built
    * [KYLIN-640] - sum of float not working
    * [KYLIN-642] - Couldn't refresh cube segment
    * [KYLIN-643] - JDBC couldn't connect to Kylin: "java.sql.SQLException: Authentication Failed"
    * [KYLIN-644] - join table as null error when build the cube
    * [KYLIN-652] - Lookup table alias will be set to null
    * [KYLIN-657] - JDBC Driver not register into DriverManager
    * [KYLIN-658] - java.lang.IllegalArgumentException: Cannot find rowkey column XXX in cube CubeDesc
    * [KYLIN-659] - Couldn't adjust the rowkey sequence when create cube
    * [KYLIN-666] - Select float type column got class cast exception
    * [KYLIN-681] - Failed to build dictionary if the rowkey's dictionary property is "date(yyyy-mm-dd)"
    * [KYLIN-682] - Got "No aggregator for func 'MIN' and return type 'decimal(19,4)'" error when build cube
    * [KYLIN-684] - Remove holistic distinct count and multiple column distinct count from sample cube
    * [KYLIN-691] - update tomcat download address in download-tomcat.sh
    * [KYLIN-696] - Dictionary couldn't recognize a value and throw IllegalArgumentException: "Not a valid value"
    * [KYLIN-703] - UT failed due to unknown host issue
    * [KYLIN-711] - UT failure in REST module
    * [KYLIN-739] - Dimension as metrics does not work with PK-FK derived column
    * [KYLIN-761] - Tables are not shown in the "Query" tab, and couldn't run SQL query after cube be built

__Improvement__

    * [KYLIN-168] - Installation fails if multiple ZK
    * [KYLIN-182] - Validation Rule: columns used in Join condition should have same datatype
    * [KYLIN-204] - Kylin web not works properly in IE
    * [KYLIN-217] - Enhance coprocessor with endpoints 
    * [KYLIN-251] - job engine refactoring
    * [KYLIN-261] - derived column validate when create cube
    * [KYLIN-317] - note: grunt.json need to be configured when add new javascript or css file
    * [KYLIN-324] - Refactor metadata to support InvertedIndex
    * [KYLIN-407] - Validation: There's should no Hive table column using "binary" data type
    * [KYLIN-445] - Rename cube_desc/cube folder
    * [KYLIN-452] - Automatically create local cluster for running tests
    * [KYLIN-498] - Merge metadata tables 
    * [KYLIN-532] - Refactor data model in kylin front end
    * [KYLIN-539] - use hbase command to launch tomcat
    * [KYLIN-542] - add project property feature for cube
    * [KYLIN-553] - From cube instance, couldn't easily find the project instance that it belongs to
    * [KYLIN-563] - Wrap kylin start and stop with a script 
    * [KYLIN-567] - More flexible validation of new segments
    * [KYLIN-569] - Support increment+merge job
    * [KYLIN-578] - add more generic configuration for ssh
    * [KYLIN-601] - Extract content from kylin.tgz to "kylin" folder
    * [KYLIN-616] - Validation Rule: partition date column should be in dimension columns
    * [KYLIN-634] - Script to import sample data and cube metadata
    * [KYLIN-636] - wiki/On-Hadoop-CLI-installation is not up to date
    * [KYLIN-637] - add start&end date for hbase info in cubeDesigner
    * [KYLIN-714] - Add Apache RAT to pom.xml
    * [KYLIN-753] - Make the dependency on hbase-common to "provided"
    * [KYLIN-758] - Updating port forwarding issue Hadoop Installation on Hortonworks Sandbox.
    * [KYLIN-779] - [UI] jump to cube list after create cube
    * [KYLIN-796] - Add REST API to trigger storage cleanup/GC

__Wish__

    * [KYLIN-608] - Distinct count for ii storage

