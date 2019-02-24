import java.net.MalformedURLException;
import java.util.Date;

import org.ektorp.CouchDbConnector;
import org.ektorp.CouchDbInstance;
import org.ektorp.http.HttpClient;
import org.ektorp.http.StdHttpClient;
import org.ektorp.impl.StdCouchDbInstance;

import twitter4j.FilterQuery;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.conf.ConfigurationBuilder;

public class MyStream {
		
	public static void main(String[] args) throws MalformedURLException {
		
    	
    	ConfigurationBuilder cb = new ConfigurationBuilder();
    	cb.setDebugEnabled(true)
    	  .setOAuthConsumerKey("FCnsAxA5xeV9EdCMWCwurvBFB")
    	  .setOAuthConsumerSecret("OFXYHFaXLnY7oQXcY7BokOpkXi0lkQNJ2k8vnwtdGqiQRmnKN3")
    	  .setOAuthAccessToken("1065377680181194752-qtRfIGp40vHUhpJLHVvkQuwIuBZDbo")
    	  .setOAuthAccessTokenSecret("D5ZuAcg27lFM50VVWWkJ8Pq7l85r5rJ2hCRwFfav3vmVA");

    	
    	//connect to couchdb
    	HttpClient httpClient = new StdHttpClient.Builder()
				.url("http://115.146.92.94:5984/")
				.username("admin")
				.password("62397917")
				.build();
		
		CouchDbInstance dbInstance = new StdCouchDbInstance(httpClient);
		CouchDbConnector db = dbInstance.createConnector("collector", false);
		
		StatusListener listener = new StatusListener() {

            public void onStatus(Status status) {
            	String content = status.getText();
            	
            	Boolean NAB1 = content.contains(" NAB ");
            	Boolean NAB2 = content.toLowerCase().contains("national australia bank");
            	Boolean NAB3 = content.contains("@NAB");
            	Boolean CBA1 = content.contains(" CBA ");
            	Boolean CBA2 = content.toLowerCase().contains("commonwealth bank");
            	Boolean CBA3 = content.contains("@CommBank ");
            	Boolean ANZ1 = content.contains(" ANZ ");
            	Boolean ANZ2 = content.toLowerCase().contains("australia and new zealand banking");
            	Boolean ANZ3 = content.contains("@ANZ_AU ");
            	Boolean WBC1 = content.contains(" WBC ");
            	Boolean WBC2 = content.toLowerCase().contains("westpac");
            	
            	if (NAB1||NAB2|| NAB3|| CBA1|| CBA2|| CBA3|| ANZ1|| ANZ2|| ANZ3|| WBC1|| WBC2) {
            		String id = String.valueOf(status.getId());
            		String user = String.valueOf(status.getUser().getScreenName());
            		String cont = String.valueOf(status.getText());
            		String location = String.valueOf(status.getUser().getLocation());
            		
            		Date tweetTime = status.getCreatedAt();
                    String[] timeSplit = tweetTime.toString().split(" ");
                    String time = timeSplit[5] + " " + timeSplit[1] + " " + timeSplit[2];
                    
                    Boolean nab = false;
                    Boolean cba = false;
                    Boolean anz = false;
                    Boolean wbc = false;
                                        
                    if (NAB1||NAB2||NAB3) {
                    	nab = true;
                    }
                    
                    if (CBA1||CBA2||CBA3) {
                    	cba = true;
                    }
                    
                    if (ANZ1||ANZ2||ANZ3) {
                    	anz = true;
                    }
                    
                    if (WBC1||WBC2) {
                    	wbc = true;
                    }
            		
            		System.out.println("@" + status.getUser().getScreenName() + " - " + status.getText() + "   City: " + status.getUser().getLocation());
            		try {
            			MyTweet t1 = new MyTweet(user, cont, location, time, nab, cba, anz, wbc);
                		db.create(id, t1);
            		}catch(org.ektorp.UpdateConflictException ue) {
            			
            		}
            	}
            }

            public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
                System.out.println("Got a status deletion notice id:" + statusDeletionNotice.getStatusId());
            }

            public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
                System.out.println("Got track limitation notice:" + numberOfLimitedStatuses);
            }

            public void onScrubGeo(long userId, long upToStatusId) {
                System.out.println("Got scrub_geo event userId:" + userId + " upToStatusId:" + upToStatusId);
            }

            public void onException(Exception ex) {
                ex.printStackTrace();
            }

			@Override
			public void onStallWarning(StallWarning warning) {
				System.out.println("Got stall warning:" + warning);
				
			}
        };

        FilterQuery fq = new FilterQuery();
        double[][] locations = { {113, -45}, {154, -11}};
        fq.locations(locations);
        fq.language(new String[]{"en"});
        
        TwitterStream twitterStream = new TwitterStreamFactory(cb.build()).getInstance();
        twitterStream.addListener(listener);
        twitterStream.filter(fq);
        
        System.out.println("OMG");
    }
}
