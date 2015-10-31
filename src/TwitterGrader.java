import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Dictionary;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Pattern;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.temboo.Library.Twitter.OAuth.FinalizeOAuth;
import com.temboo.Library.Twitter.OAuth.FinalizeOAuth.FinalizeOAuthInputSet;
import com.temboo.Library.Twitter.OAuth.FinalizeOAuth.FinalizeOAuthResultSet;
import com.temboo.Library.Twitter.OAuth.InitializeOAuth;
import com.temboo.Library.Twitter.OAuth.InitializeOAuth.InitializeOAuthInputSet;
import com.temboo.Library.Twitter.OAuth.InitializeOAuth.InitializeOAuthResultSet;
import com.temboo.Library.Twitter.Timelines.UserTimeline;
import com.temboo.Library.Twitter.Timelines.UserTimeline.UserTimelineInputSet;
import com.temboo.Library.Twitter.Timelines.UserTimeline.UserTimelineResultSet;
import com.temboo.core.TembooException;
import com.temboo.core.TembooSession;

public class TwitterGrader {
	
	public static void main(String[] args) {
		Scanner in = new Scanner(System.in);
		JsonParser jsonParser = new JsonParser();
		final int MAX_NUM_TWEETS = 30;
		String consumer_secret = "8UkVJougL6OfQ8tLnRzpKjxl01rf3YCng5IfBshAsvP6oaAJYS";
		String consumer_key = "Z0u3dhGVQdPIHGTiqDwNakKay";
		String authorization_url;
		String callbackID;
		String oauth_token_secret;
		String access_token;
		String access_token_secret;
		String screen_name;

		//==================== TEMBOO SESSION CREATOR =====================
		TembooSession session = null;
		try {
			session = new TembooSession("ajm482", "CS275MoverAJM482", "sIpirQut2SAsaV35c8kdRIzjkL2KG3o0");
		} catch (TembooException e) {
			System.out.println("Couldn't create temboo session");
			e.printStackTrace();
		}

		/*
		//====================== INITIALIZE OAUTH ==========================
		InitializeOAuthResultSet initializeOAuthResults = initialize_OAuth(session, consumer_key, consumer_secret);
		authorization_url = initializeOAuthResults.get_AuthorizationURL();
		oauth_token_secret = initializeOAuthResults.get_OAuthTokenSecret();
		
		
		//==================== ASK FOR ACCESS ====================
		System.out.println("Visit the following website and click Allow, pressing enter here after you have done so.\n" + authorization_url); 
		callbackID = in.nextLine().trim(); 
		callbackID = initializeOAuthResults.get_CallbackID().trim(); 
				
		
		//====================== FINALIZE OAUTH ==========================
		FinalizeOAuthResultSet finalizeOAuthResults = finalize_OAuth(session, consumer_key, consumer_secret, oauth_token_secret, callbackID);
		access_token = finalizeOAuthResults.get_AccessToken();
		access_token_secret = finalizeOAuthResults.get_AccessTokenSecret();
		
		
		//====================== HARDCODING AFTER OAUTH ==========================
		System.out.println("access token " + access_token);
		System.out.println("access token secret " + access_token_secret);
		System.out.println("callback id " + callbackID);
		System.out.println("oauth token secret " + oauth_token_secret);
		*/
		access_token = "3092494198-DdPwTPLjwqLMaFfiQwL5U6nt0V4eBJxNGYTAAb4";
		access_token_secret = "eio2okYPcYYYTEH4ybzDRn9ISCo5R0rDLx6YdSXs5ZKB8";
		callbackID = "89f3c8d3-cd9d-4964-88a1-fe29a50f7130";
		oauth_token_secret = "Y6JL5st4Jzu0vEbVbo9nHzD4lXkc94s3";	
		
		//====================== ASK FOR USERNAME ==========================
		System.out.println("Who are we grading today?"); 
		screen_name = in.nextLine().trim(); 
		System.out.println("This could take a minute...");
		
		
		//====================== GET USER TIMELINE ==========================
		UserTimelineResultSet userTimelineResults = get_usertimeline(session, access_token, access_token_secret, consumer_key, consumer_secret, screen_name);
		
		JsonArray user_timeline_array = (JsonArray) jsonParser.parse(userTimelineResults.get_Response());
		
		int poly_syl_words = 0;
		
		// Limititing number of tweets
		int num_sentances;
		if(user_timeline_array.size() > MAX_NUM_TWEETS) {
			num_sentances = MAX_NUM_TWEETS;
		} else {
			num_sentances = user_timeline_array.size();
		}
		
		for(int i = 0; i < num_sentances; i++) {
			// Getting the text from each tweet in the array
			JsonObject tweet = user_timeline_array.get(i).getAsJsonObject();
			String tweet_text = tweet.get("text").getAsString().trim();
			
			// Iterate through a string array of the text and count syllables of words
			String[] tweet_text_array = tweet_text.split(" ");
			for(int j = 0; j < tweet_text_array.length; j++) {
				tweet_text_array[j] = clean_text(tweet_text_array[j]);
				int syllable_count = wordnik_counter(jsonParser, tweet_text_array[j].trim());
				// Counting polysyllabic words
				if(syllable_count > 2) {
					poly_syl_words++;
				}
			}
		}
		
		//====================== CALCULATE GRADE ==========================
		double grade = calc_grade(poly_syl_words, num_sentances);
		System.out.print("Grade Level: " + grade);
	}
	
	
	
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~ AUX METHODS ~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	public static InitializeOAuthResultSet initialize_OAuth(TembooSession session, String consumer_key, String consumer_secret) {
		// ================== INITIALIZE OAUTH ====================
		// Instantiate the Choreo, using a previously instantiated TembooSession

		InitializeOAuth initializeOAuthChoreo = new InitializeOAuth(session);

		// Get an InputSet object for the choreo
		InitializeOAuthInputSet initializeOAuthInputs = initializeOAuthChoreo.newInputSet();

		// Set inputs
		initializeOAuthInputs.set_ConsumerSecret(consumer_secret);
		initializeOAuthInputs.set_ConsumerKey(consumer_key);

		// Execute Choreo
		InitializeOAuthResultSet initializeOAuthResults = null;
		try {
			initializeOAuthResults = initializeOAuthChoreo.execute(initializeOAuthInputs);
		} catch (TembooException e) {
			System.out.println("Unable to intialize OAuth");
			e.printStackTrace();
			System.exit(1);
		}
		return initializeOAuthResults;
	}
	
	public static FinalizeOAuthResultSet finalize_OAuth(TembooSession session, String consumer_key, String consumer_secret, String oauth_token_secret, String callbackID) {
		//====================== FINALIZE OAUTH ==========================

		// Instantiate the Choreo, using a previously instantiated TembooSession

		FinalizeOAuth finalizeOAuthChoreo = new FinalizeOAuth(session);

		// Get an InputSet object for the choreo
		FinalizeOAuthInputSet finalizeOAuthInputs = finalizeOAuthChoreo.newInputSet();
		
		// Set inputs
		finalizeOAuthInputs.set_CallbackID(callbackID);
		finalizeOAuthInputs.set_OAuthTokenSecret(oauth_token_secret);
		finalizeOAuthInputs.set_ConsumerSecret(consumer_secret);
		finalizeOAuthInputs.set_ConsumerKey(consumer_key);

		// Execute Choreo
		FinalizeOAuthResultSet finalizeOAuthResults = null;
		try {
			finalizeOAuthResults = finalizeOAuthChoreo.execute(finalizeOAuthInputs);
		} catch (TembooException e) {
			System.out.println("Error finalizing OAuth");
			e.printStackTrace();
			System.exit(1);
		}
		return finalizeOAuthResults;
	}
	
	public static UserTimelineResultSet get_usertimeline(TembooSession session, String access_token, String access_token_secret, String consumer_key, String consumer_secret, String screen_name) {
		//====================== GET USER TIMELINE ==========================
		// Instantiate the Choreo, using a previously instantiated TembooSession

		UserTimeline userTimelineChoreo = new UserTimeline(session);

		// Get an InputSet object for the choreo
		UserTimelineInputSet userTimelineInputs = userTimelineChoreo.newInputSet();

		// Set inputs
		userTimelineInputs.set_ScreenName(screen_name);
		userTimelineInputs.set_AccessToken(access_token);
		userTimelineInputs.set_AccessTokenSecret(access_token_secret);
		userTimelineInputs.set_ConsumerSecret(consumer_secret);
		userTimelineInputs.set_ConsumerKey(consumer_key);

		// Execute Choreo
		UserTimelineResultSet userTimelineResults = null;
		try {
			userTimelineResults = userTimelineChoreo.execute(userTimelineInputs);
		} catch (TembooException e) {
			System.out.print("Error getting User Timeline");
			e.printStackTrace();
			System.exit(1);
		}
		return userTimelineResults;
	}
	
	public static String clean_text(String word) {
		//====================== CLEAN WORDS ==========================
		// Removes URLs and user tags first
		if(word.contains("/") || word.contains("@")) {
			return "";
		} else {
			// If not a user name or tag
			// Trims and removes all non alphabetic characters
			String clean_word = word.trim();
			clean_word = clean_word.replaceAll("[^a-zA-Z ]", "");
			//System.out.println("old word: " + word + "    " + "clean word: " + clean_word);
			return clean_word;
		}
	}
	
	public static int wordnik_counter(JsonParser jsonParser, String word) {
		//====================== COUNT SYLLABLES ==========================
		// If string is empty, we return 0
		if(word == "" || word == "\n" || word == "\t" || word.length() == 0) {
			return 0;
		} else {
			String api_key = "a2a73e7b926c924fad7001ca3111acd55af2ffabf50eb4ae5";
			String wordnik_url_string = "http://api.wordnik.com/v4/word.json/" + word + "/hyphenation?useCanonical=false&limit=50&api_key=" + api_key;
			
			// Setting URL, exits if malformed
			URL wordnik_url = null;
			try {
				wordnik_url = new URL(wordnik_url_string);
			} catch (MalformedURLException e) {
				System.out.println("Malformed wordnik URL");
				e.printStackTrace();
				System.exit(1);
			}
			
			// Sending URL connection request
			HttpURLConnection wordnik_request = null;
			try {
				wordnik_request = (HttpURLConnection)wordnik_url.openConnection();
				wordnik_request.connect();
			} catch (IOException e) {
				System.out.println("Error connecting to wordnik");
				e.printStackTrace();
			}
			
			// Pull JSON
			JsonElement syllable_root = null;
			try {
				syllable_root = jsonParser.parse(new InputStreamReader((InputStream)wordnik_request.getContent()));
			} catch (JsonIOException | JsonSyntaxException | IOException e) {
				System.out.println("Error getting wordnik json");
				e.printStackTrace();
				System.exit(1);
			}
			
			// Parse JSON array and count syllables
			try {
				JsonArray syllable_array = syllable_root.getAsJsonArray();
				if(syllable_array.size() == 0) {
					//Words that aren't empty but return 0 are counted as 1
					return 1;
				} else {
					return syllable_array.size();	
				}
			} catch(IllegalStateException e) {
				return 0;
			}

		}
	}
	
	public static double calc_grade(int num_poly, int num_sentances) {
		//====================== CALCULATE GRADE ==========================
		// Using SMOG equation
		double grade = 1.0430 * Math.sqrt(num_poly * (30/num_sentances)) + 3.1291;
		return grade;
	}
}
