package com.rubikssolutions.cultureshockme;

import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
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
	private static final int DISPLAY_AT_ONCE = 2;

	private static String[] allStories;
	private static String[] allAuthors;
	private static String[] allLocations;
	private static String[] allBackgrounds;
	private static String[] allProfiles;
	private static String[] allFlags;

	private Elements backgroundElements;
	private Elements flageElements;
	private Elements pictureElements;
	private Elements textElements;
	private Elements authorElements;
	private Elements countryElements;

	private int currentPage = 0;

	private int viewId = 1;

	private boolean loading = true;
	static Button loadMoreButton;

	private ImageLoader loader;
	private ImageLoaderConfiguration imageLoaderConfig;
	private DisplayImageOptions optionsProfile;
	private DisplayImageOptions optionsFlag;
	private DisplayImageOptions optionsBackground;

	private ProgressBar bar;

	private RelativeLayout wrapper;
	private RelativeLayout inflatedView;
	private OnClickListener mapsListener;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Setup listener
		mapsListener = new OnClickListener() {

			@Override
			public void onClick(View view) {
				String location = ((TextView) view).getText().toString();
				Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse("http://maps.google.com/?q="
						+ location));
				startActivity(intent);
			}
		};

		wrapper = (RelativeLayout) findViewById(R.id.mainFeedView);

		// Setup the spinning progressbar
		bar = (ProgressBar) this.findViewById(R.id.progressBar);

		// Calculate position to center it
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		bar.setPadding(0, (int) (dm.heightPixels / 2f) - (int) (dm.heightPixels / 8f), 0, 0);
		Log.d("windowsize", "windowsize : " + ((dm.heightPixels / 2f) - (dm.heightPixels / 8f)));

		setupImageLoader();

		// Create the arrays to hold ALL
		allStories = new String[DISPLAY_AT_ONCE];
		allAuthors = new String[DISPLAY_AT_ONCE];
		allLocations = new String[DISPLAY_AT_ONCE];
		allBackgrounds = new String[DISPLAY_AT_ONCE];
		allProfiles = new String[DISPLAY_AT_ONCE];
		allFlags = new String[DISPLAY_AT_ONCE];

		// Configure the buttons
		ImageButton infoButton = (ImageButton) findViewById(R.id.button_info);
		infoButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent infoScreenIntent = new Intent(MainActivity.this, InfoActivity.class);
				MainActivity.this.startActivity(infoScreenIntent);
			}
		});

		loadMoreButton = (Button) findViewById(R.id.buttonLoadMoreStories);
		loadMoreButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				buttonClickedToLoadMore();
			}
		});

		getNewStoriesFromServer();
		currentPage += DISPLAY_AT_ONCE;
	}

	private void buttonClickedToLoadMore() {
		if (loading == false) {
			loading = true;
			API_URL = "http://culture-shock.me/ajax/?act=get_stories_more&limit=" + currentPage;
			Log.i(TAG, currentPage + "== current page");
			Log.i(TAG, API_URL);
			getNewStoriesFromServer();
			addPage();
		}
	}

	private void setupImageLoader() {
		// Universal image loader
		optionsProfile = new DisplayImageOptions.Builder().showStubImage(R.drawable.profile_default)
				.imageScaleType(ImageScaleType.EXACTLY_STRETCHED).showImageOnFail(R.drawable.profile_default)
				.cacheOnDisc(true).bitmapConfig(Bitmap.Config.RGB_565).build();

		optionsFlag = new DisplayImageOptions.Builder().imageScaleType(ImageScaleType.EXACTLY_STRETCHED)
				.bitmapConfig(Bitmap.Config.RGB_565).build();

		optionsBackground = new DisplayImageOptions.Builder().showStubImage(R.drawable.image_loading)
				.imageScaleType(ImageScaleType.EXACTLY_STRETCHED).cacheOnDisc(true).bitmapConfig(Bitmap.Config.RGB_565)
				.build();

		imageLoaderConfig = new ImageLoaderConfiguration.Builder(getApplicationContext())
				.defaultDisplayImageOptions(DisplayImageOptions.createSimple()).denyCacheImageMultipleSizesInMemory()
				.build();

		loader = ImageLoader.getInstance();
		loader.init(imageLoaderConfig);
	}

	private void addPage() {
		for (int i = 0; i < DISPLAY_AT_ONCE; i++) {
			inflatedView = (RelativeLayout) View.inflate(this, R.layout.add_story, null);
			inflatedView.setId(viewId);
			RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
					RelativeLayout.LayoutParams.WRAP_CONTENT);
			lp.addRule(RelativeLayout.BELOW, (viewId - 1));
			((TextView) inflatedView.findViewById(R.id.viewAuthorText)).setText(Html.fromHtml(allAuthors[i]));
			((TextView) inflatedView.findViewById(R.id.viewLocationText)).setText(Html.fromHtml(allLocations[i]));
			((TextView) inflatedView.findViewById(R.id.viewLocationText)).setOnClickListener(mapsListener);
			loader.displayImage("assets://flags/" + allFlags[i].toUpperCase(Locale.getDefault()) + ".png",
					((ImageView) inflatedView.findViewById(R.id.viewLocationPin)), optionsFlag);
			loader.displayImage(allProfiles[i], ((ImageView) inflatedView.findViewById(R.id.viewProfilePicture)),
					optionsProfile);
			loader.displayImage(allBackgrounds[i], ((ImageView) inflatedView.findViewById(R.id.viewBackgroundPicture)),
					optionsBackground);
			((TextView) inflatedView.findViewById(R.id.viewStoryText)).setText(Html.fromHtml(allStories[i]));
			wrapper.addView(inflatedView, lp);
			viewId++;
		}
		currentPage += DISPLAY_AT_ONCE;
	}

	private void getNewStoriesFromServer() {
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
							new JsoupLoader().execute().get();
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

	private class JsoupLoader extends AsyncTask<String, Void, String[]> {

		@Override
		protected String[] doInBackground(String... params) {
			try {
				Document jsoupDocument = Jsoup.connect(API_URL).get();
				backgroundElements = jsoupDocument.select("[style^=background-image:url(']");
				authorElements = jsoupDocument.select("[class=user_link]");
				countryElements = jsoupDocument.select("[class=browse_story_location with_countryflag_icon]");
				flageElements = jsoupDocument.select("[style*=flags/mini]");
				textElements = jsoupDocument.select("H3");
				pictureElements = jsoupDocument.select("img");
			} catch (Exception e) {
				Log.e(TAG, "Failed to get jsoupDocument", e);
			}
			return null;
		}
	}

	private class BackgroundLoader extends AsyncTask<String, Void, String[]> {

		@Override
		protected String[] doInBackground(String... params) {
			int backgroundCounter = 0;
			for (int i = 0; i < (DISPLAY_AT_ONCE * 2); i++) {
				try {
					String uRlString = backgroundElements.get(i).toString().substring(73);
					if (uRlString.startsWith("http")) {
						uRlString = uRlString.substring(0, uRlString.length() - 10);
						allBackgrounds[backgroundCounter] = uRlString;
						backgroundCounter++;
					} else if (uRlString.startsWith("'")) {
						// Not the background image
						backgroundCounter++;
					}
				} catch (Exception e) {
					Log.e(TAG, "error fetching BACKGROUND from server", e);
				}
			}
			return allBackgrounds;
		}

		@Override
		protected void onPostExecute(String[] result) {
			loading = false;
			if (currentPage == DISPLAY_AT_ONCE) {
				API_URL = "http://culture-shock.me/ajax/?act=get_stories_more&limit=" + currentPage;
				addPage();
				getNewStoriesFromServer();
			}
			Log.i(TAG, "Current page: " + currentPage);
			bar.setPadding(0, 0, 0, 0);
			bar.setVisibility(View.INVISIBLE);
		}
	}

	private class FlagLoader extends AsyncTask<ImageView, Void, String[]> {
		@Override
		protected String[] doInBackground(ImageView... params) {
			try {
				for (int i = 0; i < DISPLAY_AT_ONCE; i++) {
					String imageCode = flageElements.get(i).toString().substring(125, 127);
					Log.d(TAG, "Flag code: " + imageCode);
					allFlags[i] = imageCode;
				}
			} catch (Exception e) {
				Log.e(TAG, "Profile - error connecting to server", e);
			}
			return allFlags;
		}
	}

	private class ProfilePictureLoader extends AsyncTask<ImageView, Void, String[]> {
		@Override
		protected String[] doInBackground(ImageView... params) {
			try {
				for (int i = 0; i < DISPLAY_AT_ONCE; i++) {
					allProfiles[i] = pictureElements.get(i).absUrl("src");
				}
			} catch (Exception e) {
				Log.e(TAG, "Profile - error connecting to server", e);
			}
			return allProfiles;
		}
	}

	private class StoryLoader extends AsyncTask<String, Void, String[]> {
		protected String[] doInBackground(String... urls) {
			try {
				for (int i = 0; i < DISPLAY_AT_ONCE; i++) {
					allStories[i] = textElements.get(i).text();
				}
			} catch (Exception e) {
				Log.e(TAG, "error fetching TEXT from server", e);
			}
			return allStories;
		}
	}

	private class AuthorLoader extends AsyncTask<String, Void, String[]> {
		protected String[] doInBackground(String... urls) {
			try {
				for (int i = 0; i < DISPLAY_AT_ONCE; i++) {
					allAuthors[i] = "<big>" + "<i>" + authorElements.get(i).text() + "</i>" + "</big>" + "<br />";
					allLocations[i] = countryElements.get(i).text();
					Log.d("LOCATION", countryElements.get(i).text());
				}
			} catch (Exception e) {
				Log.e(TAG, "error fetching AUTHOR from server", e);
			}
			return allAuthors;
		}
	}
}