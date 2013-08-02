package com.rubikssolutions.cultureshockme;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {
	private static final String API_URL = "http://culture-shock.me/ajax/?act=get_stories_more";
	private static final String TAG = "MainActivity";

	TextView[] storyViews;
	TextView[] authorTextViews;
	ImageView[] flagImageViews;
	ImageView[] backgroundImageViews;
	ImageView[] profileImageViews;

	int counter = 0;
	
	int deviceWidth = 240;

	/*
	 * pulls the first x stories and displays them.
	 * Higher values throw an OutOfMemoryError on my crappy phone,
	 * but work fine on the Genymotion emulator.
	 * 
	 * Potential max is 12 for now I think.
	 */
	int amountToDisplayAtOnce = 4; 
	int amountToGetTotal = 12;
	
	String[] allStories;
	String[] allAuthors;
	Bitmap[] allBackgrounds;
	Bitmap[] allFlags;
	Bitmap[] allProfiles;
	
	int currentPage = amountToDisplayAtOnce;
	
	boolean loading = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		deviceWidth = dm.widthPixels;
		
		addViews();
		
		new ProfilePictureLoader().execute();
		new StoryLoader().execute();
		new AuthorLoader().execute();
		new FlagLoader().execute(); 
		new BackgroundLoader().execute();		
	}
	
	private void addViews () {		
		View myLayout = findViewById(R.id.mainBottomView);
		
		// Create the arrays
		storyViews = new TextView[amountToDisplayAtOnce];
		authorTextViews = new TextView[amountToDisplayAtOnce];
		flagImageViews = new ImageView[amountToDisplayAtOnce];
		backgroundImageViews = new ImageView[amountToDisplayAtOnce];
		profileImageViews = new ImageView[amountToDisplayAtOnce];
		
		// Create the arrays to hold ALL
		allStories = new String[amountToGetTotal];
		allAuthors = new String[amountToGetTotal];
		allBackgrounds = new Bitmap[amountToGetTotal];
		allFlags = new Bitmap[amountToGetTotal];
		allProfiles = new Bitmap[amountToGetTotal];

		// Set up the LayoutParams
		LinearLayout.LayoutParams backgroundParams = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.MATCH_PARENT);
		LinearLayout.LayoutParams layoutParamsBottomPadding = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		layoutParamsBottomPadding.setMargins(0, 0, 0, 15);
		LinearLayout.LayoutParams layoutParamsNoPadding = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		LinearLayout.LayoutParams layoutParamsProfile = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		
		// Add all the views
		for (int i = 0; i < amountToDisplayAtOnce; i++) {
			// Create the new viws
			TextView text = new TextView(this);
			TextView author = new TextView(this);
			ImageView flag = new ImageView(this);
			ImageView background = new ImageView(this);
			ImageView profile = new ImageView(this);
			
			// Add them to the arrays
			storyViews[i] = text;
			authorTextViews[i] = author;
			flagImageViews[i] = flag;
			backgroundImageViews[i] = background; 
			profileImageViews[i] = profile; 
			
			// Set the layout parameters
			profile.setLayoutParams(layoutParamsProfile);
			author.setLayoutParams(layoutParamsNoPadding);
			flag.setLayoutParams(layoutParamsNoPadding);
			flag.setPadding(0, 0, 0, 5);
			background.setLayoutParams(backgroundParams);
			background.setAdjustViewBounds(true);			
			text.setLayoutParams(layoutParamsBottomPadding);

			// Set the background color
			author.setBackgroundColor(Color.WHITE);
			background.setBackgroundColor(Color.WHITE);
			text.setBackgroundColor(Color.WHITE);
			
			// The order in which to display the items
			((LinearLayout) myLayout).addView(profile);
			((LinearLayout) myLayout).addView(flag);
			((LinearLayout) myLayout).addView(author);
			((LinearLayout) myLayout).addView(background);
			((LinearLayout) myLayout).addView(text);
		}
		
		// Configure the button
		Button loadMoreButton = (Button) findViewById(R.id.buttonLoadMoreStories);
		loadMoreButton.setText("Load more!");
		loadMoreButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (loading == false) {
					addPage(true, true, true, true, true);
				}
			}
		});
	}
	
	private TextView[] addPage(boolean profile, boolean flag, boolean author,
			boolean background, boolean text) {
		int storiesPerPage = 4;
		View myLayout = findViewById(R.id.mainBottomView);
		
		// Set up the LayoutParams
		LinearLayout.LayoutParams backgroundParams = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.MATCH_PARENT);
		LinearLayout.LayoutParams layoutParamsBottomPadding = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		layoutParamsBottomPadding.setMargins(0, 0, 0, 15);
		LinearLayout.LayoutParams layoutParamsNoPadding = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		LinearLayout.LayoutParams layoutParamsProfile = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		
		TextView[] textViews = new TextView[storiesPerPage];
		ImageView[] imageViews = new ImageView[storiesPerPage];

		for (int i = 0; i < textViews.length; i++) {
			if (profile) {
				ImageView view = new ImageView(this);
				view.setLayoutParams(layoutParamsProfile);
				view.setImageBitmap(allProfiles[getNextView(i)]);
				imageViews[i] = view;
				((LinearLayout) myLayout).addView(view);
			}
			if (flag) {
				ImageView view = new ImageView(this);
				view.setLayoutParams(layoutParamsNoPadding);
				view.setImageBitmap(allFlags[getNextView(i)]);
				imageViews[i] = view;
				((LinearLayout) myLayout).addView(view);
			}
			if (author) {
				TextView view = new TextView(this);
				view.setLayoutParams(layoutParamsNoPadding);
				view.setText(Html.fromHtml(allAuthors[getNextView(i)]));
				view.setTextSize(17);
				textViews[i] = view;
				((LinearLayout) myLayout).addView(view);
			}
			if (background) {
				ImageView view = new ImageView(this);
				view.setLayoutParams(backgroundParams);
				view.setImageBitmap(allBackgrounds[getNextView(i)]);
				imageViews[i] = view;
				((LinearLayout) myLayout).addView(view);
			}
			if (text) {
				TextView view = new TextView(this);
				view.setLayoutParams(layoutParamsBottomPadding);
				view.setText(allStories[getNextView(i)]);
				view.setTextSize(17);
				textViews[i] = view;
				((LinearLayout) myLayout).addView(view);
			}
		}
		currentPage += storiesPerPage + 1;
		return textViews;
	}

	private int getNextView(int itemNumber) {
		if (currentPage + itemNumber >= 12) {
			currentPage = 0;
		}
		return (currentPage + itemNumber);
	}

	class BackgroundLoader extends AsyncTask<String, Void, Bitmap[]> {
		@Override
		protected Bitmap[] doInBackground(String... params) {	
			Bitmap[] backgroundArray = new Bitmap[amountToGetTotal];
			String[] backgroundURLArray = new String[amountToGetTotal];
			
			try {
				Document doc = Jsoup.connect(API_URL).get();
				Elements uRLElements = doc.select("[style^=background-image:url(']");
				int backgroundCounter = 0;
				for (int i = 0; i < (amountToGetTotal * 2); i++) {
					try {
						String uRlString = uRLElements.get(i).toString();
						uRlString = uRlString.substring(73);
						if (uRlString.startsWith("http")) {
							uRlString = uRlString.substring(0, uRlString.length() - 10);
							backgroundURLArray[backgroundCounter] = uRlString;
							backgroundCounter++;
						} else if (uRlString.startsWith("'")) {
							backgroundCounter++;
						}
					} catch (Exception e) {
						Log.e(TAG, "error fetching BACKGROUND from server", e);
					}
				}
			} catch (Exception e) {
				Log.e(TAG, "error connecting to server", e);
			}
			
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inSampleSize = 2;
			for (int i = 0; i < backgroundArray.length; i++) {
				try {
					Bitmap bitmap = BitmapFactory.decodeStream((InputStream)new URL(backgroundURLArray[i]).getContent(), null, options);
					backgroundArray[i] = bitmap; 
					allBackgrounds[i] = backgroundArray[i]; 
				} catch (Exception e) {
					Log.e(TAG, "error fetching BACKGROUND from URL -" + i, e);
				}
			}
			return backgroundArray;
		}
		
		@Override
		protected void onPostExecute(Bitmap[] backgroundArray) {
			for (int i = 0; i < amountToDisplayAtOnce; i++) {
				try {
					backgroundArray[i] = scaleBitmapToDevice(backgroundArray[i]);
					backgroundImageViews[i].setImageBitmap(backgroundArray[i]);
					backgroundImageViews[i].setPadding(8, 0, 8, 0);
				} catch (Exception e) {
					Log.e(TAG, "Can't scale Bitmap, probable cause: picture missing from the story!", e);
				}
			}
			loading = false;
		}
	}
	
	class ProfilePictureLoader extends AsyncTask<String, Void, Bitmap[]> {
		@Override
		protected Bitmap[] doInBackground(String... params) {	
			Bitmap[] profiles = new Bitmap[amountToGetTotal];
			String[] profileURLs = new String[amountToGetTotal];
			try {
				Document doc = Jsoup.connect(API_URL).get();
				Elements pics = doc.select("img");
				for (int i = 0; i < profiles.length; i++) {
					String url = pics.get(i).absUrl("src");
					System.out.println(url);
					profileURLs[i] = url; 
				}
			} catch (Exception e) {
				Log.e(TAG, "error connecting to server", e);
			}
			
			for (int i = 0; i < profiles.length; i++) {
				try {
					Bitmap bitmap = BitmapFactory.decodeStream((InputStream)new URL(profileURLs[i]).getContent());
					profiles[i] = bitmap; 
					allProfiles[i] = profiles[i]; 
				} catch (Exception e) {
					Log.e(TAG, "error fetching BACKGROUND from URL -" + i, e);
				}
			}
			return profiles;
		}
		
		@Override
		protected void onPostExecute(Bitmap[] profiles) {
			for (int i = 0; i < amountToDisplayAtOnce; i++) {
				try {
					profileImageViews[i].setImageBitmap(profiles[i]);
					profileImageViews[i].setPadding(8, 0, 8, 0);
				} catch (Exception e) {
					Log.e(TAG, "Can't scale Bitmap, probable cause: picture missing from the story!", e);
				}
			}
		}
	}

	class StoryLoader extends AsyncTask<String, Void, String[]> {
		protected void onPreExecute() {
			storyViews[0].setTextSize(25f);
			storyViews[0].setText("Loading stories...");
		}
		
		protected String[] doInBackground(String... urls) {
			try {
				Document doc = Jsoup.connect(API_URL).get();
				String[] textArray = new String[amountToGetTotal];
				Elements textElements = doc.select("H3");
				for (int i = 0; i < 12; i++) {
					textArray[i] = textElements.get(i).text();
					allStories[i] = textArray[i];
				}

				return textArray;
			} catch (Exception e) {
				Log.e(TAG, "error fetching TEXT from server", e);
				return null;
			}
		}

		protected void onPostExecute(String[] result) {
			if (result != null) {
				for (int i = 0; i < amountToDisplayAtOnce; i++) {
					storyViews[i].setText(result[i]);
					storyViews[i].setTextSize(17f);
					storyViews[i].setPadding(10, 0, 10, 0);
				}
			}
		}
	}

	class AuthorLoader extends AsyncTask<String, Void, String[]> {
		protected void onPreExecute() {
			authorTextViews[0].setText("Loading authors...");
		}
		
		protected String[] doInBackground(String... urls) {
			try {
				Document doc = Jsoup.connect(API_URL).get();
				String[] authorArray = new String[amountToGetTotal];
				Elements authorElements = doc.select("[class=user_link]");
				Elements countryElements = doc.select("[class=browse_story_location with_countryflag_icon]");
				for (int i = 0; i < authorArray.length; i++) {
					authorArray[i] = "<big>" + "<i>"
							+ authorElements.get(i).text() 
							+ "</i>" + "</big>\n" + "<br />"
							+ countryElements.get(i).text();
					allAuthors[i] = authorArray[i];
				}
				return authorArray;
			} catch (Exception e) {
				Log.e(TAG, "error fetching AUTHOR from server", e);
				return null;
			}
		}

		protected void onPostExecute(String[] result) {
			if (result != null) {
				for (int i = 0; i < amountToDisplayAtOnce; i++) {
					authorTextViews[i].setText(Html.fromHtml(result[i]));
					authorTextViews[i].setTextSize(15f);
					authorTextViews[i].setPadding(10, 0, 10, 0);
				}
			}
		}
	}

	class FlagLoader extends AsyncTask<String, Integer, Bitmap[]> {
		protected Bitmap[] doInBackground(String... params) {
			try {
				Document doc = Jsoup.connect(API_URL).get();
				Bitmap[] flagArray = new Bitmap[amountToGetTotal];
				Elements imageElements = doc.select("[style*=flags/mini]");
				for (int i = 0; i < flagArray.length; i++) {
					String imageCode = imageElements.get(i).toString().substring(125, 131);
					URL imageUrl = new URL("http://culture-shock.me/img/icons/flags/mini/" + imageCode);
					URLConnection conn = imageUrl.openConnection();
					conn.connect();

					InputStream is = conn.getInputStream();
					BufferedInputStream bis = new BufferedInputStream(is);
					flagArray[i] = BitmapFactory.decodeStream(bis);
					allFlags[i] = flagArray[i];
					
					bis.close();
					is.close();
				}
				return flagArray;
			} catch (Exception e) {
				Log.e(TAG, "error fetching FLAG from server", e);
			}
			return null;
		}

		protected void onPostExecute(Bitmap[] result) {
			if (result != null) {
				for (int i = 0; i < amountToDisplayAtOnce; i++) {
					flagImageViews[i].setImageBitmap(result[i]); 
				}
			}
		}
	}

	private Bitmap scaleBitmapToDevice(Bitmap inputBitmap) { 
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		int deviceWidth = dm.widthPixels;
		
		float width = inputBitmap.getWidth();
		float height = inputBitmap.getHeight();
		float ratio = (width / height);
		
		return Bitmap.createScaledBitmap(inputBitmap, deviceWidth, (int)(deviceWidth / ratio), false);
	}
	
//	public class EndlessScrollListener implements OnScrollListener {
//
//		private int visibleThreshold = 3;
//		private int currentPage = 0;
//
//		@Override
//		public void onScroll(AbsListView view, int firstVisibleItem,
//				int visibleItemCount, int totalItemCount) {
//		}
//
//		@Override
//		public void onScrollStateChanged(AbsListView view, int scrollState) {
//			if (scrollState == SCROLL_STATE_IDLE) {
//				if (listView.getLastVisiblePosition() >= listView.getCount()
//						- visibleThreshold) {
//					currentPage++;
//					StoryLoaderWithPages.execute();
//					StoryLoaderWithPages.setPage(currentPage);
//				}
//			}
//		}
//
//	}

}