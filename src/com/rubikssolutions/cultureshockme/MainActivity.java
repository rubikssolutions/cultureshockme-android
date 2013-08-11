package com.rubikssolutions.cultureshockme;

import java.util.Timer;
import java.util.TimerTask;

import org.jsoup.Jsoup;
import org.jsoup.select.Elements;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

/*
 * TODO
 * Add loading images (or ideally a progress bar)
 */

public class MainActivity extends Activity {
	private static String API_URL = "http://culture-shock.me/ajax/?act=get_stories_more&limit=0";
	private static final String TAG = "MainActivity";
	private static final String LOADING = "Currently loading...";
	private static final String DONE_LOADING = "Show me more stories!";

	int amountToDisplayAtOnce = 4;

	String[] allStories;
	String[] allAuthors;
	String[] allBackgrounds;
	String[] allProfiles;
	
	int currentPage = 0;

	int viewId = 1;

	boolean loading = true;
	boolean loadMoreButtonIsEnabled = false;
	Button loadMoreButton;
	
	ImageLoader imageLoader;
	ImageLoaderConfiguration imageLoaderConfig;
	DisplayImageOptions displayImageOptions;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Universal image loader
		displayImageOptions = new DisplayImageOptions.Builder()
		.showStubImage(R.drawable.ic_launcher)
		.imageScaleType(ImageScaleType.EXACTLY_STRETCHED)
		.showImageOnFail(R.drawable.button_info)
		
//		.cacheInMemory(true)
		.cacheOnDisc(true)
		
		.bitmapConfig(Bitmap.Config.RGB_565).build();
		
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		
		imageLoaderConfig = new ImageLoaderConfiguration.Builder(
				getApplicationContext()).defaultDisplayImageOptions(
				DisplayImageOptions.createSimple()).defaultDisplayImageOptions(displayImageOptions)
				.build();

		imageLoader = ImageLoader.getInstance();
		imageLoader.init(imageLoaderConfig);

		// Create the arrays to hold ALL
		allStories = new String[amountToDisplayAtOnce];
		allAuthors = new String[amountToDisplayAtOnce];
		allBackgrounds = new String[amountToDisplayAtOnce];
		allProfiles = new String[amountToDisplayAtOnce];

		ImageButton infoButton = (ImageButton) findViewById(R.id.button_info);
		infoButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent infoScreenIntent = new Intent(MainActivity.this,
						InfoScreen.class);
				MainActivity.this.startActivity(infoScreenIntent);
			}
		});

		// Configure the button
		loadMoreButton = (Button) findViewById(R.id.buttonLoadMoreStories);
		loadMoreButton.setText(LOADING);
		loadMoreButton.setBackgroundColor(Color.LTGRAY);
		loadMoreButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (loadMoreButtonIsEnabled) {
					loadMoreButtonIsEnabled = false;
					loadMoreButton.setText(LOADING);
					loadMoreButton.setBackgroundColor(Color.LTGRAY);
					API_URL = "http://culture-shock.me/ajax/?act=get_stories_more&limit="
							+ currentPage;
					Log.i(TAG, currentPage + "== current page");
					Log.i(TAG, API_URL);
					loadMoreStories(true, true, true, true);
					addPage(true, true, true, true);
				}
			}
		});

		loadMoreStories(true, true, true, true);
		currentPage += amountToDisplayAtOnce;

		loading = false;
	}

	private void addPage(boolean profile, boolean author,
			boolean background, boolean text) {
		RelativeLayout wrapper = (RelativeLayout) findViewById(R.id.mainFeedView);
		RelativeLayout inflatedView;

		for (int i = 0; i < amountToDisplayAtOnce; i++) {
					
			inflatedView = (RelativeLayout) View.inflate(this,
					R.layout.add_story, null);
			inflatedView.setId(viewId);
			RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
					RelativeLayout.LayoutParams.WRAP_CONTENT,
					RelativeLayout.LayoutParams.WRAP_CONTENT);
			lp.setMargins(0, 0, 0, 20);
			if (author) {
				TextView view = new TextView(this);
				lp.addRule(RelativeLayout.BELOW, (viewId - 1));
				view.setText(Html.fromHtml(allAuthors[i]));
				view.setTextSize(15);
				view.setPadding(10, 0, 10, 0);
				((TextView) inflatedView.findViewById(R.id.viewAuthorText))
						.setText(Html.fromHtml(allAuthors[i]));
			}
			if (profile) {
				ImageLoader.getInstance().displayImage(allProfiles[i],
						((ImageView) inflatedView
								.findViewById(R.id.viewProfilePicture)),
						displayImageOptions);
			}
			if (background) {
				ImageLoader.getInstance().displayImage(allBackgrounds[i],
						((ImageView) inflatedView
								.findViewById(R.id.viewBackgroundPicture)),
						displayImageOptions);
			}
			if (text) {
				TextView view = new TextView(this);
				view.setText(allStories[i]);
				view.setTextSize(17);
				view.setPadding(10, 0, 10, 0);
				((TextView) inflatedView.findViewById(R.id.viewStoryText))
						.setText(Html.fromHtml(allStories[i]));
			}
			wrapper.addView(inflatedView, lp);
			viewId++;
		}
		currentPage += amountToDisplayAtOnce;
	}

	public void loadMoreStories(final boolean profile, final boolean author,
			final boolean background, final boolean text) {
		final Handler handler = new Handler();
		Timer timer = new Timer();
		TimerTask doAsynchronousTask = new TimerTask() {
			@Override
			public void run() {
				handler.post(new Runnable() {
					public void run() {
						try {
							if (profile) {
								new ProfilePictureLoader().execute();
							}
							if (author) {
								new AuthorLoader().execute();
							}
							if (background) {
								new BackgroundLoader().execute();
							}
							if (text) {
								new StoryLoader().execute();
							}
						} catch (Exception e) {
							Log.e(TAG, "Could not execute storyloader", e);
						}
					}
				});
			}
		};
		timer.schedule(doAsynchronousTask, 0);
	}

	class BackgroundLoader extends AsyncTask<String, Void, String[]> {
		@Override
		protected String[] doInBackground(String... params) {
			try {
				Elements uRLElements = 
						Jsoup.connect(API_URL).get().select("[style^=background-image:url(']");
				int backgroundCounter = 0;
				for (int i = 0; i < (amountToDisplayAtOnce * 2); i++) {
					try {
						String uRlString = uRLElements.get(i).toString();
						uRlString = uRlString.substring(73);
						if (uRlString.startsWith("http")) {
							uRlString = uRlString.substring(0,
									uRlString.length() - 10);
							allBackgrounds[backgroundCounter] = uRlString;
							backgroundCounter++;
						} else if (uRlString.startsWith("'")) {
							backgroundCounter++;
						}
					} catch (Exception e) {
						Log.e(TAG, "error fetching BACKGROUND from server", e);
					}
				}
			} catch (Exception e) {
				Log.e(TAG, "Background - error connecting to server", e);
			}
			return allBackgrounds;
		}
	}

	class ProfilePictureLoader extends AsyncTask<ImageView, Void, String[]> {
		@Override
		protected String[] doInBackground(ImageView... params) {
			try {
				Elements pics = Jsoup.connect(API_URL).get().select("img");
				for (int i = 0; i < amountToDisplayAtOnce; i++) {
					String url = pics.get(i).absUrl("src");
					allProfiles[i] = url; 
				}
			} catch (Exception e) {
				Log.e(TAG, "Profile - error connecting to server", e);
			}
			return allProfiles;
		}
	}

	class StoryLoader extends AsyncTask<String, Void, String[]> {
		protected String[] doInBackground(String... urls) {
			try {
				String[] textArray = new String[amountToDisplayAtOnce];
				Elements textElements = Jsoup.connect(API_URL).get()
						.select("H3");
				for (int i = 0; i < amountToDisplayAtOnce; i++) {
					textArray[i] = textElements.get(i).text();
					allStories[i] = textArray[i];
				}
				return textArray;
			} catch (Exception e) {
				Log.e(TAG, "error fetching TEXT from server", e);
				return null;
			}
		}

		@Override
		protected void onPostExecute(String[] result) {
			loadMoreButtonIsEnabled = true;
			loadMoreButton.setText(DONE_LOADING);
			loadMoreButton.setBackgroundColor(Color.YELLOW);
		}
	}

	class AuthorLoader extends AsyncTask<String, Void, String[]> {
		protected String[] doInBackground(String... urls) {
			try {
				String[] authorArray = new String[amountToDisplayAtOnce];
				Elements authorElements = Jsoup.connect(API_URL).get()
						.select("[class=user_link]");
				Elements countryElements = Jsoup
						.connect(API_URL)
						.get()
						.select("[class=browse_story_location with_countryflag_icon]");
				for (int i = 0; i < amountToDisplayAtOnce; i++) {
					authorArray[i] = "<big>" + "<i>"
							+ authorElements.get(i).text() + "</i>"
							+ "</big>\n" + "<br />"
							+ countryElements.get(i).text();
					allAuthors[i] = authorArray[i];
				}
				return authorArray;
			} catch (Exception e) {
				Log.e(TAG, "error fetching AUTHOR from server", e);
				return null;
			}
		}
	}
}