package com.rubikssolutions.cultureshockme;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import android.R.integer;
import android.app.Activity;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {
	private static final String API_URL = "http://culture-shock.me/ajax/?act=get_stories_more";
	private static final String TAG = "MainActivity";

	TextView[] textViewArray;
	TextView[] authorTextViewsArray;
	ImageView[] imageViewsArray;

	int counter = 0;

	int amountOfText = 12; // Should be calculated by pulling data from the
							// server TODO

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		textViewArray = new TextView[amountOfText];
		authorTextViewsArray = new TextView[amountOfText];
		imageViewsArray = new ImageView[amountOfText];

		View myLayout = findViewById(R.id.scrollingLinearView);
		for (int i = 0; i < amountOfText; i++) {
			TextView text = new TextView(this);
			textViewArray[i] = text;
			TextView author = new TextView(this);
			authorTextViewsArray[i] = author;
			ImageView flag = new ImageView(this);
			imageViewsArray[i] = flag;

			text.setLayoutParams(new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.WRAP_CONTENT,
					LinearLayout.LayoutParams.WRAP_CONTENT));
			author.setLayoutParams(new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.WRAP_CONTENT,
					LinearLayout.LayoutParams.WRAP_CONTENT));
			flag.setLayoutParams(new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.WRAP_CONTENT,
					LinearLayout.LayoutParams.WRAP_CONTENT));

			((LinearLayout) myLayout).addView(author);
			((LinearLayout) myLayout).addView(flag);
			((LinearLayout) myLayout).addView(text);
		}
		new StoryLoader().execute();
		new AuthorLoader().execute();
		new FlagLoader().execute();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	class StoryLoader extends AsyncTask<String, Void, String[]> {
		protected String[] doInBackground(String... urls) {
			try {
				Document doc = Jsoup.connect(API_URL).get();
				String[] textArray = new String[amountOfText];

				for (int i = 0; i < textArray.length; i++) {
					Element textElement = doc.select("H3").get(i);
					textArray[i] = textElement.text();
				}

				return textArray;
			} catch (Exception e) {
				Log.e(TAG, "error fetching TEXT from server", e);
				return null;
			}
		}

		protected void onPostExecute(String[] result) {
			for (int i = 0; i < textViewArray.length; i++) {
				textViewArray[i].setText(result[i]);
				textViewArray[i].setTextSize(17f);
			}
		}
	}

	class AuthorLoader extends AsyncTask<String, Void, String[]> {
		protected String[] doInBackground(String... urls) {
			try {
				Document doc = Jsoup.connect(API_URL).get();
				String[] authorArray = new String[amountOfText];

				for (int i = 0; i < authorArray.length; i++) {
					Element authorElement = doc.select("[class=user_link]").get(i);
					authorArray[i] = authorElement.text();
				}

				return authorArray;
			} catch (Exception e) {
				Log.e(TAG, "error fetching AUTHOR from server", e);
				return null;
			}
		}

		protected void onPostExecute(String[] result) {
			for (int i = 0; i < authorTextViewsArray.length; i++) {
				authorTextViewsArray[i].setText(result[i]);
				authorTextViewsArray[i].setTextColor(Color.RED);
				authorTextViewsArray[i].setTextSize(15f);
			}
		}
	}

	class FlagLoader extends AsyncTask<String, Integer, Bitmap[]> {

		protected Bitmap[] doInBackground(String... params) {
			try {
				Document doc = Jsoup.connect(API_URL).get();
				Bitmap[] flagArray = new Bitmap[amountOfText];

				for (int i = 0; i < flagArray.length; i++) {
					Element imageElement = doc.select("[style*=flags/mini]").get(i);
					String imageCode = imageElement.toString().substring(125, 131);

					URL imageUrl = new URL("http://culture-shock.me/img/icons/flags/mini/" + imageCode);
					URLConnection conn = imageUrl.openConnection();
					conn.connect();

					InputStream is = conn.getInputStream();
					BufferedInputStream bis = new BufferedInputStream(is);
					flagArray[i] = BitmapFactory.decodeStream(bis);
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
			for (int i = 0; i < imageViewsArray.length; i++) {
				imageViewsArray[i].setImageBitmap(result[i]);
			}
		}
	}
}