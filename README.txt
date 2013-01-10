### Twitter Stream Crawler Readme - 8th Jan 2013 ###
This is a java crawler that continually crawls tweets from the Twitter streaming API and saves them 
to a series of files. The Twitter streaming API provides a continual stream of tweets in (close-to) 
real-time as they are posted. For most users, this will amount to 1% of the total stream, or about
4.6 million tweets a day at the time this readme was written (known as the spritzer stream). The
crawler is built upon Twitter4J (http://twitter4j.org/en/index.html). 

Installation:
Extract the software from the zip file to a location of your choice.

Dependencies:
This software is written in Java and requires Java 1.5 or greater be installed. The latest version of
Java can be downloaded for free from http://www.java.com/en/download/index.jsp

Running the Crawler:
For ease of use, a pre-prepared runnable jar file is included, called TwitterStreamFileCrawler.jar
To run the crawler in its most basic form, open a terminal in folder where you extracted the software
and run the following command:

	java -jar TwitterStreamFileCrawler.jar ./ MYUSERNAME MYPASSWORD

where MYUSERNAME and MYPASSWORD are replaced with a valid twitter username and password. This will
connect to the Twitter streaming API, login using the specified username and password and then start
downloading tweets from the stream. Tweets will be stored in gzip format (http://www.gzip.org/) in
the same directory as the software. The files that the software creates follow a fixed naming scheme
as follows:

	<UNIXDateOfFileCreation>.json.gz
	<UNIXDateOfFileCreation>.log.gz
	
The json file contains each tweet downloaded in JSON format, one tweet per line. The written files are
stored in UTF-8 format. The log file lists any other messages received from the Twitter streaming API,
such as tweet deleted messages and errors.
  
Configuring the number of tweets to save per file:
By default, the crawler will write 1 million tweets to a single json file. Once 1 million tweets have been
written, a new file (with a new date) will be created. This is to avoid creating a very large single file
that would be difficult to work with and never complete (as new tweets would forever be getting added to
it. Notably, the software will flush after every tweet received such that even if the crawler crashes none
of the tweets downloaded before that point are lost. However, depending on your environment, you may wish
to change the size of the written files. To do so, add a fourth parameter to the command line when running
the software as follows:

	java -jar TwitterStreamFileCrawler.jar ./ MYUSERNAME MYPASSWORD 10000
	
Here the software will move to a new file every 10,000 tweets, rather than for every million.

Proxy Servers:
Some users may be behind a proxy server. The crawler supports the use of a proxy. To configure a proxy,
two system properties need to be set. In particular, http.proxyhost and http.proxyport need to
be set to the host and port of your proxy server. On unix machines this can be set on the command line as 
follows:

	java -Dhttp.proxyhost="MYPROXYHOST" -Dhttp.proxyport="MYPROXYPORT" -jar TwitterStreamFileCrawler.jar ./ MYUSERNAME MYPASSWORD

which will use the proxy server at MYPROXYHOST:MYPROXYPORT.

Logs and Error Reporting
As with any piece of software, error reporting is important - for example there is not a lot the software can
do if you run out of disk space to save the tweets! The crawler can report errors in a variety of ways. Errors
received from Twitter itself will be reported in the log files, however, these are very rare and automatically
recoverable and hence can be ignored. Errors experienced by Twitter4J may periodically occur and will be reported
on the ERR line of the terminal on which the software is running. For example a common Twitter4J error appears
when the connection to the API is closed. Most of these type of errors are also recovered from automatically 
(e.g. Twitter4J just automatically reconnects and continues), hence these most often can be ignored. 

However, the crawler provides a third method for reporting back to you - via email, and this needs to be configured,
otherwise it will be deactivated. The idea is that the software should send you an email when a critical error or some
unexpected behaviour has been detected. To configure email reporting, you need to do two things:

	1) Fill in the missing details in the javamail.conf file which can be found in the same directory as the software
	   At minimum, you need to set mail.host to the address of your email server. 
	2) You need to provide your email address and the location of the javamail.conf file on the command line when
	   running the software, an example on unix is shown below:
	
	java -Demail="MYEMAIL@ADDRESS" -Demailconf="./javamail.conf" -jar TwitterStreamFileCrawler.jar ./ MYUSERNAME MYPASSWORD
	
Currently, if this is configured correctly, it will send you an email when the crawler starts, any time when a tweet fails
to be written (e.g. when the disk is full) and any time when a file is written that contains less than the expected number
of tweets (e.g. an error as caused the crawler to reset). You can also set the emailonvalidate property to "true",
which will send you an email when each file is successfully written (this can be useful as a periodic check that everything
is still ok). 

Legal Stuff:
The Twitter stream crawler was built on top of Twitter4j v2.2.5 by Richard McCreadie at the University of Glasgow as part of 
the Cross Project (EPSRC, grant number EP/J020664/1). The Original Code for the TwitterStreamFileWriter class is Copyright (C) 
2013 the University of Glasgow. All Rights Reserved. Twitter4J is copyright to Yusuke Yamamoto released under the Apache License
Version 2.0. 

Contact:  Richard McCreadie <richard.mccreadie@gla.ac.uk> (original author) 

