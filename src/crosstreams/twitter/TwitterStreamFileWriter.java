/*
 * Cross Project - Real-time Story Detection Across Multiple Massive Streams  
 * Webpage: http://demeter.inf.ed.ac.uk/cross/index.html
 * Contact: miles@inf.ed.ac.uk
 * University of Glasgow / University of Edinburgh
 * http://www.gla.ac.uk/, http://www.ed.ac.uk/home
 * *
 * The Original Code is TwitterStreamFileWriter.java.
 *
 * The Original Code is Copyright (C) 2013 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Richard McCreadie <richard.mccreadie@gla.ac.uk> (original author)
 */

package crosstreams.twitter;

import java.io.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterException;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.conf.ConfigurationBuilder;

/**
 * This class downloads tweets from the Twitter streaming API. You will need a twitter account to download tweets.
 * Normal users can only download tweets from the Spritzer stream (1% of total). Some user accounts were granted
 * access to a larger percentage known as the Gardenhose (5% of total). New users can no longer request Gardenhose
 * access. 
 * 
 * Run the main method for instructions.
 * 
 * Note that this class uses a modified version of the Status interface and the JSON implementation StatusJSONImpl
 * These classes were modified to provide a getJSON() method such that the raw JSON can be written to a file.
 * @author Richard McCreadie
 *
 */
public class TwitterStreamFileWriter {
	
	/**
	 * Start crawling tweets
	 * @param args
	 * @throws TwitterException
	 */
	public static void main(String[] args) throws TwitterException {
		
		System.err.println("### Twitter Stream Writer ###");
		System.err.println("Saves tweets from the Spritzer/Gardenhose Stream to a series of files");
		System.err.println("Command: crosstreams.twitter.TwitterStreamFileWriter <saveFolder> <twitterusername> <twitterpassword> <numberoftweetstostoreperfile>(optional)");
		System.err.println("	saveFolder: Where the tweets will be downloaded to");
		System.err.println("	twitterusername: The username of the twitter account to use for downloading tweets");
		System.err.println("	twitterpassword: The password of the twitter account to use for downloading tweets");
		System.err.println("	numberoftweetstostoreperfile: The total number of tweets to write to a file before closing that file and opening a new one (Integer) (defaults=1000000)");
		System.err.println("Optional System Properties (-D):");	
		System.err.println("	http.proxyhost: The proxy host to use if needed");	
		System.err.println("	http.proxyport: The proxy port to use if needed");	
		System.err.println("	email: An email address to send alerts to if an error is encountered");
		System.err.println("	emailconf: An file containing the javax.mail configuration");	
		System.err.println("	emailonvalidate: true/false - should I send an email when a file is correctly validated rather than only when it fails? (default=false)");
		
		if (args.length<=1 || args.length>=5) {
			System.err.println("Example:");
			System.err.println("java -Demail=\"MYEMAIL@HOST.COM\" -Demailconf=\"./javamail.conf\" -Demailonvalidate=\"true\" -jar TwitterStreamFileCrawler.jar ./ MYUSERNAME MYPASSWORD 100000");
			System.err.println("Don't forget to modify ./javamail.conf to contain your email server host");
			System.exit(0);
		}
		
		// user inputs
		String saveFolder = args[0];
		String username = args[1];
		String password = args[2];
		final int numberOfTweetsToStorePerFile;
		if (args.length>2) numberOfTweetsToStorePerFile = Integer.parseInt(args[3]);
		else numberOfTweetsToStorePerFile = 1000000;
		String proxyhost = System.getProperty("http.proxyhost");
		String proxyport = System.getProperty("http.proxyport");
		final String email = System.getProperty("email");
		final String emailconf = System.getProperty("emailconf");
		
		// define the user account in use and proxy settings if needed
		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setDebugEnabled(true);
		if (proxyhost!=null && proxyport!=null) {
			cb.setHttpProxyHost(proxyhost);
			cb.setHttpProxyPort(Integer.parseInt(proxyport));
		}
		cb.setUser(username);
		cb.setPassword(password);
		
		if (!saveFolder.endsWith("/") && !saveFolder.endsWith("\\") ) {
			saveFolder = saveFolder+ System.getProperty("file.separator");
		}
		final String finalSaveFolder = saveFolder;
		 
		// Twitter4J Stream - the type of stream is set automatically, i.e. Gardenhose if you have it, Spritzer otherwise.
        TwitterStream twitterStream = new TwitterStreamFactory(cb.build()).getInstance();
        
        // The status listener is the important bit, this fires when a new tweet arrives.
        StatusListener listener = new StatusListener() {
        	
        	/** The status listener holds a writer to save content to **/
        	BufferedWriter statusWriter = null; // the tweets go here
        	BufferedWriter logWriter = null; // we write any delete requests or error messages here
        	
        	/** We store a fixed number of Tweets in each file **/
        	int numberInThisFile = numberOfTweetsToStorePerFile;
        	int numberPerFile = numberOfTweetsToStorePerFile;
        	
        	
        	String currentFilename;
        	int numerrors=0;
        	
        	/**
        	 * A new tweet has arrived
        	 */
            public void onStatus(Status status) {
            	if (numberInThisFile>=numberPerFile) {
            		// closing and opening of new files
            		try {
						if (statusWriter!=null) {
							
							statusWriter.close();
							logWriter.close();
							validateJSONFile(currentFilename, numberPerFile);
						}
						Long currentTime =System.currentTimeMillis();
						
						currentFilename=finalSaveFolder+currentTime.toString()+".json.gz";
						statusWriter = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(currentFilename)), "UTF-8"));
						logWriter = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(finalSaveFolder+currentTime.toString()+".log.gz")), "UTF-8"));
						numberInThisFile=0;
						numerrors=0;
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
            	}
            	numberInThisFile++;
            	// write the JSON - note that I added the getJSON() method to the Twitter4J status object
            	// this is why the Twitter4j sources are included rather than importing the jar.
                try {
                	Object s = status.getJSON();
					statusWriter.write(status.getJSON().toString()+'\n');
					statusWriter.flush();
				} catch (Exception e) {
					e.printStackTrace();
					numerrors++;
					if (emailconf!=null && email!=null && numerrors<5)Mail.mail(emailconf, email, email, "Twitter Stream Writer Alert - Write Failed", "An IOException was thrown when calling statusWriter.write()."+'\n'+e.getMessage()+'\n'+"The current file will be closed and a new file will be created.");
				}
            }

            public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
            	try {
					logWriter.write("DEL: " + statusDeletionNotice.getStatusId()+ " "+statusDeletionNotice.getUserId()+'\n');
				} catch (IOException e) {
					e.printStackTrace();
				}
            }

            public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
            	try {
					logWriter.write("LIMIT: " + numberOfLimitedStatuses+'\n');
				} catch (IOException e) {
					e.printStackTrace();
				}
            }

            public void onScrubGeo(long userId, long upToStatusId) {
            	try {
					logWriter.write("SCRUBGEO: " + userId+ " "+upToStatusId+'\n');
				} catch (IOException e) {
					e.printStackTrace();
				}
            }

            public void onException(Exception ex) {
            	if (logWriter==null) return;
            	try {
					logWriter.write("ERR: "+ex.getLocalizedMessage()+'\n');
					logWriter.flush();
					if (statusWriter!=null) {
						statusWriter.close();
						statusWriter=null;
						logWriter.close();
						validateJSONFile(currentFilename, numberPerFile);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
                //ex.printStackTrace();
            }
        };
        if (emailconf!=null && email!=null )Mail.mail(emailconf, email, email, "Twitter Stream Writer Info - Writer has started", "The Gardenhose Writer has begun crawling the stream (this email indicates that you will recieve alerts if something goes wrong.");
        twitterStream.addListener(listener);
        twitterStream.sample();
    }
	
	/**
	 * This does a file check to see if there are the right number of tweets in the file. It is run as a separate thread.
	 * You will only see output for this if email is activated.
	 * @param file
	 * @param expectedlines
	 */
	public static void validateJSONFile(String file, int expectedlines) {
		TwitterStreamFileWriter w = new TwitterStreamFileWriter();
		validateThread vt = w.new validateThread(file, expectedlines);
		Thread runner = new Thread(vt, "ValidateThread"); 
		runner.start();
	}
	
	/**
	 * Thread that checks to see if there are the right number of tweets in the file.
	 * You will only see output for this if email is activated.
	 * @author richardm
	 *
	 */
	class validateThread implements Runnable {

		String file;
		int expectedlines;
		
		public validateThread(String file, int expectedlines) {
			this.file = file;
			this.expectedlines = expectedlines;
		}
		
		@Override
		public void run() {
			
			final String email = System.getProperty("email");
			final String emailconf = System.getProperty("emailconf");
			final String emailonvalidate = System.getProperty("emailonvalidate");
			int numlines = 0;
			try {
				BufferedReader br = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(file))));
				String line;
				while ((line=br.readLine())!=null) {
					numlines++;
				}
				br.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (expectedlines!=numlines) {
				if (emailconf!=null && email!=null )Mail.mail(emailconf, email, email, "Twitter Stream Writer Alert - Written File Failed to Validate!", "The Gardenhose Writer has written a file to disk ("+file+") but it contains less than the expected number of tweets. This may be due to an uncaught exception or other error. Crawler is still running, but should be checked.");
			} else {
				if (emailconf!=null && email!=null && emailonvalidate.equalsIgnoreCase("true"))Mail.mail(emailconf, email, email, "Twitter Stream Writer Info - Validation Passed", "The Gardenhose Writer has written a valid file to disk ("+file+") containing "+numlines+" tweets.");
			}
			
		}
		
	}
	
}
