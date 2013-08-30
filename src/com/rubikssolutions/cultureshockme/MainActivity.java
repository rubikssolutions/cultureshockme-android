package com.rubikssolutions.cultureshockme;

import java.util.Timer;
import java.util.TimerTask;

import org.jsoup.Jsoup;
import org.jsoup.select.Elements;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
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
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;


public class MainActivity extends Activity {
	private static String API_URL = "http://culture-shock.me/ajax/?act=get_stories_more";
	private static final String TAG = "MainActivity";

	int amountToDisplayAtOnce = 2;

	public static String[] allStories;
	public static String[] allAuthors;
	public static String[] allLocations;
	public static String[] allBackgrounds;
	public static String[] allProfiles;
	public static String[] allFlags;
	
	int currentPage = 0;

	int viewId = 1;

	boolean loading = true;
	static Button loadMoreButton;
	
	public ImageLoader imageLoader;
	public ImageLoaderConfiguration imageLoaderConfig;
	public DisplayImageOptions optionsProfile;
	public static DisplayImageOptions optionsFlag;
	public DisplayImageOptions optionsBackground;

	private ProgressBar bar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
		setContentView(R.layout.activity_main);
		
		// Setup the spinning progressbar
		bar = (ProgressBar) this.findViewById(R.id.progressBar);
		
		// Calculate position to center it
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		bar.setPadding(0, (int) (dm.heightPixels/2f) - (int) (dm.heightPixels/8f), 0, 0);
		Log.d("windowsize", "windowsize : " + ((dm.heightPixels/2f) - (dm.heightPixels/8f)));
		
		setupImageLoader();

		// Create the arrays to hold ALL
		allStories = new String[amountToDisplayAtOnce];
		allAuthors = new String[amountToDisplayAtOnce];
		allLocations = new String[amountToDisplayAtOnce];
		allBackgrounds = new String[amountToDisplayAtOnce];
		allProfiles = new String[amountToDisplayAtOnce];
		allFlags = new String[amountToDisplayAtOnce];

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
		loadMoreButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				buttonClickedToLoadMore();
			}
		});

		getNewStoriesFromServer();
		currentPage += amountToDisplayAtOnce;
	}
	
	public void buttonClickedToLoadMore() {
		if (loading == false) {
			loading = true;
			API_URL = "http://culture-shock.me/ajax/?act=get_stories_more&limit="
					+ currentPage;
			Log.i(TAG, currentPage + "== current page");
			Log.i(TAG, API_URL);
			getNewStoriesFromServer();
			addPage();
		}
	}
	
	public void location(View view) {
		String location = ((TextView) view).getText().toString();
		Intent intent = new Intent(
				android.content.Intent.ACTION_VIEW,
				Uri.parse("http://maps.google.com/?q=" + location));
		startActivity(intent);
	}

	private void setupImageLoader() {
		// Universal image loader
		optionsProfile = new DisplayImageOptions.Builder()
		.showStubImage(R.drawable.profile_default)
		.imageScaleType(ImageScaleType.EXACTLY_STRETCHED)
		.showImageOnFail(R.drawable.profile_default)
		.cacheOnDisc(true)
		.bitmapConfig(Bitmap.Config.RGB_565).build();
		
		optionsFlag = new DisplayImageOptions.Builder()
		.imageScaleType(ImageScaleType.EXACTLY_STRETCHED)
		.bitmapConfig(Bitmap.Config.RGB_565).build();
		
		optionsBackground = new DisplayImageOptions.Builder()
		.showStubImage(R.drawable.image_loading)
		.imageScaleType(ImageScaleType.EXACTLY_STRETCHED)
		.cacheOnDisc(true)
		.bitmapConfig(Bitmap.Config.RGB_565).build();
		
		imageLoaderConfig = new ImageLoaderConfiguration.Builder(
				getApplicationContext()).defaultDisplayImageOptions(
				DisplayImageOptions.createSimple())
				.denyCacheImageMultipleSizesInMemory()
				.build();

		imageLoader = ImageLoader.getInstance();
		imageLoader.init(imageLoaderConfig);
	}

	private void addPage() {
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
			lp.addRule(RelativeLayout.BELOW, (viewId - 1));
			((TextView) inflatedView.findViewById(R.id.viewAuthorText))
					.setText(Html.fromHtml(allAuthors[i]));
			((TextView) inflatedView.findViewById(R.id.viewLocationText))
			.setText(Html.fromHtml(allLocations[i]));
			ImageLoader.getInstance().displayImage(allFlags[i],
					((ImageView) inflatedView
							.findViewById(R.id.viewFlag)),
					optionsFlag);
			ImageLoader.getInstance().displayImage(allProfiles[i],
					((ImageView) inflatedView
							.findViewById(R.id.viewProfilePicture)),
					optionsProfile);
			ImageLoader.getInstance().displayImage(allBackgrounds[i],
					((ImageView) inflatedView
							.findViewById(R.id.viewBackgroundPicture)),
					optionsBackground);
			((TextView) inflatedView.findViewById(R.id.viewStoryText))
					.setText(Html.fromHtml(allStories[i]));
			wrapper.addView(inflatedView, lp);
			viewId++;
		}
		currentPage += amountToDisplayAtOnce;
	}

	public void getNewStoriesFromServer() {
		final Handler handler = new Handler();
		Timer timer = new Timer();
		TimerTask doAsynchronousTask = new TimerTask() {
			@Override
			public void run() {
				handler.post(new Runnable() {
					public void run() {
						try {
							loading = true;
							bar.setVisibility(View.VISIBLE);
							new FlagLoader().execute();
							new ProfilePictureLoader().execute();
							new AuthorLoader().execute();
							new StoryLoader().execute();
							new BackgroundLoader().execute();
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
		
		@Override
		protected void onPostExecute(String[] result) {
			loading = false;
			if (currentPage == amountToDisplayAtOnce) {
				API_URL = "http://culture-shock.me/ajax/?act=get_stories_more&limit="
						+ currentPage;
				addPage();
				getNewStoriesFromServer();
			}
			Log.i("PageTag", "pageTag" + currentPage);
			bar.setPadding(0, 0, 0, 0);
			bar.setVisibility(View.INVISIBLE);
		}
	}
	
	class FlagLoader extends AsyncTask<ImageView, Void, String[]> {
		@Override
		protected String[] doInBackground(ImageView... params) {
			try {
				Elements flags = Jsoup.connect(API_URL).get()
						.select("[style*=flags/mini]");
				for (int i = 0; i < amountToDisplayAtOnce; i++) {
					String imageCode = flags.get(i).toString()
							.substring(125, 131);
					String url = "http://culture-shock.me/img/icons/flags/mini/"
							+ imageCode;
					allFlags[i] = url;
				}
			} catch (Exception e) {
				Log.e(TAG, "Profile - error connecting to server", e);
			}
			return allFlags;
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
					Log.d("StoryDebug", "StoryDebug textelement: " + textElements.get(i).text());
					allStories[i] = textArray[i];
				}
				return textArray;
			} catch (Exception e) {
				Log.e(TAG, "error fetching TEXT from server", e);
				return null;
			}
		}
	}

	class AuthorLoader extends AsyncTask<String, Void, String[]> {
		protected String[] doInBackground(String... urls) {
			try {
				Elements authorElements = Jsoup.connect(API_URL).get()
						.select("[class=user_link]");
				Elements countryElements = Jsoup
						.connect(API_URL)
						.get()
						.select("[class=browse_story_location with_countryflag_icon]");
				for (int i = 0; i < amountToDisplayAtOnce; i++) {
					allAuthors[i] = "<big>" + "<i>"
							+ authorElements.get(i).text() + "</i>"
							+ "</big>" + "<br />";
					allLocations[i] = countryElements.get(i).text();
				}
				return null;
			} catch (Exception e) {
				Log.e(TAG, "error fetching AUTHOR from server", e);
				return null;
			}
		}
	}
}