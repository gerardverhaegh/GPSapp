package com.example.gveapp;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.example.gveapp.model.Weather;
import org.json.JSONException;

/*
 * Copyright (C) 2013 Surviving with Android (http://www.survivingwithandroid.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

public class WeatherActivity extends Activity {
	private TextView cityText;
	private TextView condDescr;
	private TextView temp;
	private TextView press;
	private TextView windSpeed;
	private TextView windDeg;

	private TextView hum;
	private ImageView imgView;

	private double m_dLat = 0;
	private double m_dLon = 0;

	private char sDegreeCentigrade = (char) 0x00B0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_weather);
		String city = "London,UK";

		cityText = (TextView) findViewById(R.id.cityText);
		condDescr = (TextView) findViewById(R.id.condDescr);
		temp = (TextView) findViewById(R.id.temp);
		hum = (TextView) findViewById(R.id.hum);
		press = (TextView) findViewById(R.id.press);
		windSpeed = (TextView) findViewById(R.id.windSpeed);
		windDeg = (TextView) findViewById(R.id.windDeg);
		imgView = (ImageView) findViewById(R.id.condIcon);

		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			// Get data via the key
			double Lat = extras.getDouble("Latitude");
			// if (Lat != null)
			{
				m_dLat = Lat;
			}

			double Lon = extras.getDouble("Longitude");
			// if (Lon != null)
			{
				m_dLon = Lon;
			}
		}

		// double lat = m_dLat;
		// double lon = m_dLon;
		JSONWeatherTask task = new JSONWeatherTask();
		task.execute(new String[] { m_dLat + "", m_dLon + "" });
	}

	/*
	 * @Override public boolean onCreateOptionsMenu(Menu menu) { // Inflate the
	 * menu; this adds items to the action bar if it is present.
	 * getMenuInflater().inflate(R.menu.main_menu, menu); return true; }
	 */

	private class JSONWeatherTask extends AsyncTask<String, Void, Weather> {
		@Override
		protected Weather doInBackground(String... params) {
			Weather weather = new Weather();

			try {
				String data = ((new WeatherHttpClient()).getWeatherData(
						params[0], params[1]));
				weather = JSONWeatherParser.getWeather(data);

				// Let's retrieve the icon
				weather.iconData = ((new WeatherHttpClient())
						.getImage(weather.currentCondition.getIcon()));

			} catch (JSONException e) {
				e.printStackTrace();
			}
			return weather;
		}

		@Override
		protected void onPostExecute(Weather weather) {
			super.onPostExecute(weather);

            try {
                if (weather.iconData != null && weather.iconData.length > 0) {
                    Bitmap img = BitmapFactory.decodeByteArray(weather.iconData, 0,
                            weather.iconData.length);
                    imgView.setImageBitmap(img);
                }

                cityText.setText(weather.location.getCity() + " ("
                        + weather.location.getCountry() + ")");

                condDescr.setText(weather.currentCondition.getCondition() + " ("
                        + weather.currentCondition.getDescr() + ")");

                temp.setText("  "
                        + Math.round((weather.temperature.getTemp() - 273.15))
                        + " " + sDegreeCentigrade + "C");

                hum.setText("  "
                        + Math.round(weather.currentCondition.getHumidity()) + " %");

                press.setText("  "
                        + Math.round(weather.currentCondition.getPressure())
                        + " hPa");

                windSpeed.setText("  " + Math.round(1.6f * weather.wind.getSpeed())
                        + " m/s");

                windDeg.setText("  " + degToCompass(weather.wind.getDeg()));
            }
            catch(Exception e)
            {
                Toast.makeText(getApplicationContext(), "No weather information available currently", Toast.LENGTH_LONG).show();
                finish();
            }
		}

		private String degToCompass(float deg) {
			int val = (int) Math.round((deg - 11.25) / 22.5);
			String arr[] = { "N", "NNE", "NE", "ENE", "E", "ESE", "SE", "SSE",
					"S", "SSW", "SW", "WSW", "W", "WNW", "NW", "NNW" };
			return arr[val % 16];
		}
	}
}
