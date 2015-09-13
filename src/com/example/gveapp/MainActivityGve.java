package com.example.gveapp;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.location.*;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.text.Html;
import android.text.SpannedString;
import android.util.Log;
import android.view.*;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
/*
 import android.R.menu;
 import android.R.layout;
 import android.R;
 */

public class MainActivityGve extends FragmentActivity implements
        LocationListener {
    private static final int PICKFILE_RESULT_CODE = 1;
    private static Boolean m_bEnableGPS = true;
    private static Boolean m_bEnableRoute = false;
    private LocationManager m_lm = null;
    private GoogleMap m_googleMap = null;
    private Boolean m_bFirstTime = true;
    private ArrayList<Location> m_all_geo_points;
    private String sAppPath = "/sdcard/Android/data/com.example.gveapp/";
    private String m_sRoute = null;
    private Menu m_menu;
    private TextView m_tv = null;
    private TextView m_tv2 = null;
    private Configuration m_Configuration = null;
    private float m_Speed = 0.0f;
    private float m_StartTime = 0.0f;
    private float m_TotTime = 0.0f;
    private float m_TotDist = 0.0f;
    private float m_AvgSpeed = 0.0f;
    private boolean m_bDoDraw = false;
    private List<Float> m_SpeedList = null;
    private boolean m_bIsVisible = false; // is app visible or not?
    private boolean m_bIsZoomingIn = false; // has user done a zoomin of the map?

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("GVE", "111111111");
        setContentView(R.layout.main_layout);

        Log.d("GVE", "222222222");
        appendLog(LOGFILETYPE.LOG, "New log file");

        m_bDoDraw = false;

        // weather
        // String s = getWeatherData("Hillerod,DK");
        // Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();

        // geopoints
        m_all_geo_points = new ArrayList<Location>();
        m_all_geo_points.clear();

        // speed list for avg
        m_SpeedList = new ArrayList<Float>();
        m_SpeedList.clear();

        // google maps
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.the_map);

        if (mapFragment != null) {
            m_googleMap = mapFragment.getMap();
        }

        if (m_googleMap != null) {
            // Toast.makeText(getApplicationContext(), "NOT NULL",
            // Toast.LENGTH_LONG).show();
            m_bFirstTime = true;

            appendLog(LOGFILETYPE.LOG, "First time in onCreate");
        } else {
            appendLog(LOGFILETYPE.LOG, "googleMap is null");
            Toast.makeText(getApplicationContext(), "googleMap is null",
                    Toast.LENGTH_LONG).show();
        }

        // speed
        m_tv = (TextView) findViewById(R.id.km_text);
        m_tv2 = (TextView) findViewById(R.id.km_text2);

        m_TotTime = 0.0f;
        m_TotDist = 0.0f;
        m_StartTime = (float) System.currentTimeMillis() / 1000; // seconds
        m_Configuration = getResources().getConfiguration();

        ShowSpeed();

        // gps
        m_lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // Creating a criteria object to retrieve provider
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);

        // Getting the name of the best provider
        String provider = m_lm.getBestProvider(criteria, true);
        // Toast.makeText(getApplicationContext(), provider,
        // Toast.LENGTH_LONG).show();

        LocationProvider lp = m_lm.getProvider(LocationManager.GPS_PROVIDER);

        Location location = m_lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        onLocationChanged(location);
        m_bFirstTime = true;

        m_lm.requestLocationUpdates(lp.getName(), 2000, 10, this);

        appendLog(LOGFILETYPE.LOG, "LocationProvider: " + lp.getName());
        m_bDoDraw = true;

        SetTitleBackgroundColor(Color.GREEN);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        this.m_menu = menu;
        return true;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        m_Configuration = newConfig;
        ShowSpeed();

		/*
         * // if (m_bToastLevel > 1) { // Checks the orientation of the screen
		 * if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
		 * appendLog(LOGFILETYPE.LOG, "onConfigurationChanged landscape"); //
		 * Toast.makeText(getApplicationContext(), //
		 * "onConfigurationChanged landscape", // Toast.LENGTH_LONG).show(); }
		 * else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT)
		 * { appendLog(LOGFILETYPE.LOG, "onConfigurationChanged portrait"); //
		 * Toast.makeText(getApplicationContext(), //
		 * "onConfigurationChanged portrait", Toast.LENGTH_LONG).show(); } else
		 * { appendLog(LOGFILETYPE.LOG, "onConfigurationChanged else"); //
		 * Toast.makeText(getApplicationContext(), //
		 * "onConfigurationChanged else", Toast.LENGTH_LONG).show(); } }
		 */
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        MenuItem mi = null;

        // respond to menu item selection
        switch (item.getItemId()) {
            case R.id.action_routegraph:
                try {

                    Intent i = new Intent(getApplicationContext(),
                            FileBrowser.class);
                    i.putExtra("Path", sAppPath);
                    i.putExtra("Filter", "Route");
                    startActivityForResult(i, 0);

				/*
                 * ArrayList<Location> list_of_geopoints = readFromFile();
				 * drawPrimaryLinePath( list_of_geopoints );
				 *
				 * appendLog(LOGFILETYPE.LOG, "readFromFile, size " +
				 * list_of_geopoints.size());
				 */
                } catch (Exception e) {

                }
                return true;
            case R.id.action_weather:
                try {
                    Intent i = new Intent(getApplicationContext(),
                            WeatherActivity.class);
                    LatLngBounds bounds = m_googleMap.getProjection()
                            .getVisibleRegion().latLngBounds;

                    i.putExtra("Latitude", bounds.getCenter().latitude);
                    i.putExtra("Longitude", bounds.getCenter().longitude);

                    startActivity(i);
                /*
                 * ArrayList<Location> list_of_geopoints = readFromFile();
				 * drawPrimaryLinePath( list_of_geopoints );
				 *
				 * appendLog(LOGFILETYPE.LOG, "readFromFile, size " +
				 * list_of_geopoints.size());
				 */
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(),
                            "Retrieving weather info failed", Toast.LENGTH_LONG)
                            .show();
                }
                return true;
			/*
			 * case R.id.action_routegraph: try { Intent i = new
			 * Intent(getApplicationContext(), RouteGraph.class);
			 * startActivity(i); } catch (Exception e) {
			 * Toast.makeText(getApplicationContext(), "exception",
			 * Toast.LENGTH_LONG).show(); } return true;
			 */
/*            case R.id.action_enableGPS:
                m_bEnableGPS = !m_bEnableGPS;

                mi = m_menu.findItem(R.id.action_enableGPS);
                if (m_bEnableGPS) {
                    mi.setTitle(getString(R.string.action_disableGPS));

                    LocationProvider lp = m_lm
                            .getProvider(LocationManager.GPS_PROVIDER);
                    m_lm.requestLocationUpdates(lp.getName(), 1000, 0, this);
                } else {
                    mi.setTitle(getString(R.string.action_enableGPS));

                    m_lm.removeUpdates(this);
                }

                m_sRoute = "Route_" + GetDateTimeString();
                return true;*/
            case R.id.action_route:
                m_bEnableRoute = !m_bEnableRoute;

                mi = m_menu.findItem(R.id.action_route);
                if (m_bEnableRoute) {
                    mi.setTitle(getString(R.string.action_stop_route));
                } else {
                    mi.setTitle(getString(R.string.action_start_route));
                }

                m_sRoute = "Route_" + GetDateTimeString();

                SetTitleBackgroundColor(m_bEnableRoute ? Color.RED : Color.GREEN);

                return true;
            case R.id.action_exit:
                m_lm.removeUpdates(this);
                System.exit(1);

			/*
			 * Intent intent = new Intent(Intent.ACTION_MAIN);
			 * intent.addCategory(Intent.CATEGORY_HOME);
			 * intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			 * startActivity(intent);
			 */
                return true;

            case R.id.action_MAP_TYPE_HYBRID:
                m_googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                return true;
            case R.id.action_MAP_TYPE_None:
                m_googleMap.setMapType(GoogleMap.MAP_TYPE_NONE);
                return true;
            case R.id.action_MAP_TYPE_NORMAL:
                m_googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                return true;
            case R.id.action_MAP_TYPE_SATELLITE:
                m_googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                return true;
            case R.id.action_MAP_TYPE_TERRAIN:
                m_googleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void SetTitleBackgroundColor(int color) {
        // set title background color
        View titleView = getWindow().findViewById(android.R.id.title);
        if (titleView != null) {
            ViewParent parent = titleView.getParent();
            if (parent != null && (parent instanceof View)) {
                View parentView = (View) parent;
                parentView.setBackgroundColor(color);
            }
        }
    }

/*    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager
                .getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }*/

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 0:
                if (resultCode == RESULT_OK) {
                    String sFilename = data.getStringExtra("MESSAGE");

                    // Toast.makeText(getApplicationContext(), sFilename,
                    // Toast.LENGTH_LONG).show();

                    ArrayList<Location> list_of_geopoints = readFromFile(sFilename);

				/*
				 * m_googleMap.stopAnimation(); m_googleMap.clear();
				 */

                    double minLatitude = 1e9;
                    double maxLatitude = -1e9;
                    double minLongitude = 1e9;
                    double maxLongitude = -1e9;

                    // Find the boundaries of the item set
                    for (Location loc : list_of_geopoints) {
                        double lat = loc.getLatitude();
                        double lon = loc.getLongitude();

                        maxLatitude = Math.max(lat, maxLatitude);
                        minLatitude = Math.min(lat, minLatitude);
                        maxLongitude = Math.max(lon, maxLongitude);
                        minLongitude = Math.min(lon, minLongitude);
                    }

                    drawPrimaryLinePath(list_of_geopoints);
                    double distance = calc_distance_speed(list_of_geopoints);

				/*
				 * m_googleMap.animateTo(new GeoPoint( (int)(maxLatitude +
				 * minLatitude)/2, (int)(maxLongitude + minLongitude)/2 ));
				 */

                    Toast.makeText(getApplicationContext(),
                            "distance: " + String.format("%.2f km", distance),
                            Toast.LENGTH_LONG).show();

                    appendLog(LOGFILETYPE.LOG, "readFromFile, size "
                            + list_of_geopoints.size());

                    m_StartTime = (float) System.currentTimeMillis() / 1000;
                    Intent i = new Intent(getApplicationContext(), RouteGraph.class);
                    i.putExtra("Filename", sFilename);
                    startActivity(i);

                }
                break;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        m_bIsVisible = false;
        // m_lm.removeUpdates(this);
    }

    public void onProviderDisabled(String provider) {
        if (m_bEnableGPS) {
            String s = "provider disabled " + provider;
            Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT)
                    .show();
            appendLog(LOGFILETYPE.LOG, s);
        }
    }

    public void onProviderEnabled(String provider) {
        if (m_bEnableGPS) {
            String s = "provider enabled " + provider;
            Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT)
                    .show();
            appendLog(LOGFILETYPE.LOG, s);
        }
    }

    public void onStatusChanged(String provider, int status, Bundle extras) {
        if (m_bEnableGPS) {
            String s = "status changed to " + provider + " [" + status + "]";
            Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT)
                    .show();
            appendLog(LOGFILETYPE.LOG, s);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        m_bIsVisible = true;
        if (m_lm != null) {
            Location location = m_lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            onLocationChanged(location);
            ShowNewLocationInMap(location);
        }
		/*
		 * if (m_bEnableGPS) {
		 * lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 1f,
		 * this); appendLog(LOGFILETYPE.LOG,
		 * "onResume: requestLocationUpdates"); }
		 */

        // m_lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 1f,
        // this);
        // appendLog(LOGFILETYPE.LOG, "onResume: requestLocationUpdates");

		/*
		 * if (isNetworkEnabled) {
		 * locMgr.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, time,
		 * distance, this); }
		 */

        // googleMap.setLocationSource(this);

    }

    private String GetDateTimeString() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String currentDateandTime = sdf.format(new Date());
        return currentDateandTime;
    }

    public void onLocationChanged(Location location) {
        ShowNewLocationInMap(location);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        final int action = ev.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                if (!m_bIsZoomingIn) {
                    m_bIsZoomingIn = true;

                    Handler handler = new Handler();
                    Runnable r = new Runnable() {
                        public void run() {
                            m_bIsZoomingIn = false;
                        }
                    };
                    handler.postDelayed(r, 5000);
                }
            }
        }

        return true;
    }

    private void ShowNewLocationInMap(Location location) {
        if (location != null) {
		/*
         * if (m_bToastLevel > 4) { Toast.makeText(getApplicationContext(),
		 * location.getLatitude() + " " + location.getLongitude(),
		 * Toast.LENGTH_SHORT).show(); }
		 */

            if (m_bEnableRoute) {
                // do this in gpx format !
                appendLog(LOGFILETYPE.ROUTE, location.getLatitude() + ","
                        + location.getLongitude() + "," + location.getAltitude()
                        + "," + location.getSpeed());
            }

            if (m_SpeedList.size() > 5) {
                m_SpeedList.remove(0);
            }

            m_SpeedList.add(3.6f * location.getSpeed());

            m_Speed = calc_avg_speed();
            m_TotTime = (float) System.currentTimeMillis() / 1000 - m_StartTime;

                /*
                 * Toast.makeText(getApplicationContext(), "m_TotTime: " +
                 * m_TotTime, Toast.LENGTH_SHORT).show();
                 */

            if (m_TotTime > 0) {
                m_AvgSpeed = m_TotDist / m_TotTime; // km/s
                m_AvgSpeed *= 3600; // km/h
            } else {
                m_AvgSpeed = 0;
            }

            // do following only if app is visible
            if (m_bIsVisible && m_googleMap != null) {
			/*
			 * googleMap.addMarker(new MarkerOptions() .position( new
			 * LatLng(location.getLatitude(), location .getLongitude()))
			 * .title("my position") .icon(BitmapDescriptorFactory
			 * .defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
			 */

                if (m_bFirstTime) {
				/*
				 * googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
				 * new LatLng(location.getLatitude(), location .getLongitude()),
				 * 15.0f));
				 */

                    m_googleMap.setMyLocationEnabled(true);
                    m_googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

                    appendLog(LOGFILETYPE.LOG, "First time in onLocationChanged");

                    m_StartTime = System.currentTimeMillis() / 1000; // seconds
                    m_bFirstTime = false;
                }

                if (!m_bIsZoomingIn) {
                    m_googleMap
                            .animateCamera(CameraUpdateFactory.newLatLngZoom(
                                    new LatLng(location.getLatitude(), location
                                            .getLongitude()), 15.0f));
                }

                // writeToFile(location);

                if (m_bDoDraw) {
                    if (m_all_geo_points != null) {
                        m_all_geo_points.add(location);

                        if (m_all_geo_points.size() > 1) {
                            // appendLog(LOGFILETYPE.LOG,
                            // "m_all_geo_points.size(): "
                            // +
                            // m_all_geo_points.size());

						/*
						 * int lastIndex = m_all_geo_points.size() - 1; Location
						 * LastLoc = (Location) m_all_geo_points.get(lastIndex);
						 * Location PreLastLoc = (Location)
						 * m_all_geo_points.get(lastIndex - 1);
						 * PreLastLoc.setLatitude(55);
						 * PreLastLoc.setLongitude(12);
						 */
                            // ArrayList dirs = getDirections(
                            // LastLoc.getLatitude(),
                            // LastLoc.getLongitude(), PreLastLoc.getLatitude(),
                            // PreLastLoc.getLongitude() );
                            drawPrimaryLinePath(m_all_geo_points);
                            m_TotDist += calc_distance_speed(m_all_geo_points);

                            if (m_all_geo_points.size() > 1) {
                                m_all_geo_points.remove(0);
                            }
                        }
                    }
                }

                // m_TotTime += location.getTime();

                ShowSpeed();

			/*
			 * mBox = new TextView(context); mBox.setText(Html.fromHtml("<b>" +
			 * title + "</b>" + "<br />" + "<small>" + description + "</small>"
			 * + "<br />" + "<small>" + DateAdded + "</small>"));
			 */
            }
        }
    }

    private float calc_avg_speed() {
        float avg_speed = 0.0f;

        if (m_SpeedList.size() > 0) {
            for (Float f : m_SpeedList) {
                avg_speed += f;
            }

            avg_speed /= m_SpeedList.size();
        }

        return avg_speed;
    }

    private void ShowSpeed() {

        if (m_tv == null) {
            return;
        }

        if (m_tv2 == null) {
            return;
        }

        float roundedSpeed = ((float) ((int) (m_Speed * 10 + 0.5))) / 10;

        String s1 = null;
        if (m_TotTime < 60) {
            s1 = "<font face=\"arial\" color=\"#FF0000\"><big><big><big><b>"
                    + (int) m_TotTime + "</big></big></big></b></font>";

            s1 += "<font face=\"arial\" color=\"#FF0000\"><big>" + " sec"
                    + "</big></font>";
        } else {
            int roundedTotTime = ((int) ((int) (((m_TotTime + 30) / 60) * 1))) / 1;
            s1 = "<font face=\"arial\" color=\"#FF0000\"><big><big><big><b>"
                    + roundedTotTime + "</big></big></big></b></font>";

            s1 += "<font face=\"arial\" color=\"#FF0000\"><big>" + " min"
                    + "</big></font>";
        }
        float roundedTotDist = ((float) ((int) (m_TotDist * 10 + 0.5))) / 10;
        float roundedAvgSpeed = ((float) ((int) (m_AvgSpeed * 10 + 0.5))) / 10;

        String s2 = "<font face=\"arial\" color=\"#FF0000\"><big><big><big><b>"
                + roundedTotDist + "</big></big></big></b></font>";
        s2 += "<font face=\"arial\" color=\"#FF0000\"><big>" + " km"
                + "</big></font>";
        String s3 = "<font face=\"arial\" color=\"#FF0000\"><big><big><big><b>"
                + roundedAvgSpeed + "</big></big></big></b></font>";
        s3 += "<font face=\"arial\" color=\"#FF0000\"><big>" + " km/h"
                + "</big></font>";
        String s4 = "<font face=\"arial\" color=\"red\"><big><big><big><big><big><big><big><big><big>"
                + roundedSpeed
                + "</big></big></big></big></big></big></big></big></big></font>";
        String s5 = "<font face=\"arial\" color=\"red\"><big>" + " km/h"
                + "</big></font>";

        SpannedString ss = new SpannedString(Html.fromHtml(s1 + "<br/>" + s2
                + "<br/>" + s3));
        SpannedString ss2 = new SpannedString(Html.fromHtml(s4 + s5));

        m_tv.setText(ss, TextView.BufferType.SPANNABLE);
        m_tv2.setText(ss2, TextView.BufferType.SPANNABLE);

		/*
		 * switch (m_Configuration.orientation) { case
		 * Configuration.ORIENTATION_LANDSCAPE:
		 *
		 * m_tv.setX((float) m_Configuration.screenWidthDp * 0.1f);
		 * m_tv.setY((float) m_Configuration.screenHeightDp * 0.6f);
		 *
		 * m_tv2.setX((float) m_Configuration.screenWidthDp * 0.2f);
		 * m_tv2.setY((float) m_Configuration.screenHeightDp * 0.6f);
		 *
		 * break; default: case Configuration.ORIENTATION_PORTRAIT:
		 *
		 * m_tv.setX((float) m_Configuration.screenWidthDp * 0.1f);
		 * m_tv.setY((float) m_Configuration.screenHeightDp * 0.7f);
		 *
		 * m_tv2.setX((float) m_Configuration.screenWidthDp * 0.3f);
		 * m_tv2.setY((float) m_Configuration.screenHeightDp * 0.7f);
		 *
		 * break; }
		 */
    }

	/*
	 * @Override public boolean dispatchTouchEvent(MotionEvent event) {
	 * 
	 * if (event.getAction() == MotionEvent.ACTION_UP) { LatLngBounds bounds =
	 * m_googleMap.getProjection() .getVisibleRegion().latLngBounds;
	 * 
	 * Toast.makeText(getApplicationContext(), "bounds: " +
	 * bounds.getCenter().latitude, Toast.LENGTH_SHORT).show(); } return
	 * super.dispatchTouchEvent(event); }
	 */

    private void drawPrimaryLinePath(ArrayList<Location> listLocsToDraw) {
        if (m_googleMap == null) {
            return;
        }

        if (listLocsToDraw.size() < 2) {
            return;
        }

        PolylineOptions options = new PolylineOptions();

        options.color(Color.parseColor("#CCFF0000"));
        options.width(1);
        options.visible(true);

        for (Location locRecorded : listLocsToDraw) {
            options.add(new LatLng(locRecorded.getLatitude(), locRecorded
                    .getLongitude()));
        }

        m_googleMap.addPolyline(options);
    }

	/*
	 * public ArrayList getDirections(double lat1, double lon1, double lat2,
	 * double lon2) { //
	 * http://maps.googleapis.com/maps/api/directions/xml?origin
	 * =55,10&destination=44,12&sensor=false&units=metric String url =
	 * "http://maps.googleapis.com/maps/api/directions/xml?origin=" +lat1 + ","
	 * + lon1 + "&destination=" + lat2 + "," + lon2 +
	 * "&sensor=false&units=metric"; String tag[] = { "lat", "lng" }; ArrayList
	 * list_of_geopoints = new ArrayList(); HttpResponse response = null; try {
	 * HttpClient httpClient = new DefaultHttpClient(); HttpContext localContext
	 * = new BasicHttpContext(); HttpPost httpPost = new HttpPost(url);
	 * 
	 * Toast.makeText(getApplicationContext(), url, Toast.LENGTH_LONG).show();
	 * 
	 * response = httpClient.execute(httpPost, localContext);
	 * 
	 * Toast.makeText(getApplicationContext(), "after response",
	 * Toast.LENGTH_LONG).show();
	 * 
	 * InputStream in = response.getEntity().getContent();
	 * 
	 * Toast.makeText(getApplicationContext(), "after in",
	 * Toast.LENGTH_LONG).show();
	 * 
	 * DocumentBuilder builder =
	 * DocumentBuilderFactory.newInstance().newDocumentBuilder(); Document doc =
	 * builder.parse(in); if (doc != null) { NodeList nl1, nl2; nl1 =
	 * doc.getElementsByTagName(tag[0]); nl2 = doc.getElementsByTagName(tag[1]);
	 * if (nl1.getLength() > 0) { list_of_geopoints = new ArrayList(); for (int
	 * i = 0; i < nl1.getLength(); i++) { Node node1 = nl1.item(i); Node node2 =
	 * nl2.item(i); double lat = Double.parseDouble(node1.getTextContent());
	 * double lng = Double.parseDouble(node2.getTextContent());
	 * list_of_geopoints.add(new GeoPoint((int) (lat * 1E6), (int) (lng *
	 * 1E6))); } } else { // No points found
	 * 
	 * Toast.makeText(getApplicationContext(), "Nothing found",
	 * Toast.LENGTH_LONG).show(); } } } catch (Exception e) {
	 * e.printStackTrace(); Toast.makeText(getApplicationContext(),
	 * "Error caught", Toast.LENGTH_LONG).show(); } return list_of_geopoints; }
	 */

    private double calc_distance_speed(ArrayList<Location> listLocsToDraw) {
        double distance = 0;

        Location locPrev = null;
        for (Location locRecorded : listLocsToDraw) {
            if (locPrev != null) {
                distance += calc_distance_km(locPrev, locRecorded);
            }

            locPrev = locRecorded;
        }

        return distance;
    }

    private double calc_distance_km(Location loc1, Location loc2) {
        return loc1.distanceTo(loc2) / 1000; // km
    }

    private ArrayList<Location> readFromFile(String sFilename)// throws
    // IOException
    {
        ArrayList<Location> list_of_geopoints = new ArrayList<Location>();
        InputStream inputstream = null;

        try {
            // inputstream = new FileInputStream(sAppPath +
            // "Route_20131230_200251.txt");
            inputstream = new FileInputStream(sFilename);
            BufferedReader r = new BufferedReader(new InputStreamReader(
                    inputstream));

            String x = "";
            x = r.readLine();

            // Toast.makeText(getApplicationContext(), "aantal: " + x,
            // Toast.LENGTH_LONG).show();

            while (x != null) {

                String[] separated1 = x.split(" ");
                String[] separated2 = separated1[1].split(",");

                double lat = Double.parseDouble(separated2[0]);
                double lng = Double.parseDouble(separated2[1]);
                Location loc = new Location("temp");
                loc.setLatitude(lat);
                loc.setLongitude(lng);
                list_of_geopoints.add(loc);

                x = r.readLine();
            }

            if (inputstream != null) {
                inputstream.close();
            }
        } catch (Exception e) {
            if (inputstream != null) {
                try {
                    inputstream.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }

            Toast.makeText(getApplicationContext(), "Error happened",
                    Toast.LENGTH_LONG).show();
        }

        // Toast.makeText(getApplicationContext(), "size: " +
        // list_of_geopoints.size(), Toast.LENGTH_LONG).show();
        return list_of_geopoints;
    }

	/*
	 * private void writeToFile(Location loc) { File file = new
	 * File(this.getFilesDir(),"GVElocations");
	 * 
	 * if (m_bToastLevel > 4) { Toast.makeText(getApplicationContext(),
	 * "writeToFile: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show(); }
	 * 
	 * String string = loc.getLatitude() + " - " + loc.getLongitude();
	 * OutputStreamWriter outputStream;
	 * 
	 * try { outputStream = new OutputStreamWriter(openFileOutput(
	 * file.getName() , Context.MODE_APPEND)); outputStream.write(string);
	 * outputStream.flush(); outputStream.close();
	 * 
	 * boolean bExists = file.exists();
	 * 
	 * if (m_bToastLevel > 4) { Toast.makeText(getApplicationContext(),
	 * "fileExists: " + (bExists ? "Exists" : "Does not exist"),
	 * Toast.LENGTH_LONG).show(); }
	 * 
	 * } catch (Exception e) { Toast.makeText(getApplicationContext(),
	 * e.getMessage(), Toast.LENGTH_LONG).show(); } }
	 */

    public void appendLog(LOGFILETYPE lft, String text) {
        String sFilename = null;
        switch (lft) {
            case LOG:
                sFilename = sAppPath + "gveapp_log.txt";
                break;
            case ROUTE:
                sFilename = sAppPath + m_sRoute + ".txt";
                break;
            default:
                sFilename = sAppPath + "gveapp_log.txt";
                break;
        }

        File logFile = new File(sFilename);
        if (!logFile.exists()) {
            try {
                logFile.createNewFile();
            } catch (IOException e) {
                Toast.makeText(getApplicationContext(), e.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        }
        try {
            // BufferedWriter for performance, true to set append to file flag
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile,
                    true));
            buf.append(GetDateTimeString() + " " + text);
            buf.newLine();
            buf.flush();
            buf.close();
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    public enum LOGFILETYPE {
        LOG, ROUTE,
    }

	/*
	 * private static String BASE_URL =
	 * "http://api.openweathermap.org/data/2.5/weather?"; //private static
	 * String IMG_URL = "http://openweathermap.org/img/w/";
	 * 
	 * public String getWeatherData(String location) { HttpURLConnection con =
	 * null ; InputStream is = null;
	 * 
	 * StrictMode.ThreadPolicy policy = new
	 * StrictMode.ThreadPolicy.Builder().permitAll().build();
	 * StrictMode.setThreadPolicy(policy);
	 * 
	 * try { String s = BASE_URL + "&lat=55&lon=12"; con = (HttpURLConnection) (
	 * new URL(s)).openConnection(); con.setRequestMethod("GET");
	 * con.setDoInput(true); con.setDoOutput(true); con.connect();
	 * 
	 * // Let's read the response StringBuffer buffer = new StringBuffer(); is =
	 * con.getInputStream(); BufferedReader br = new BufferedReader(new
	 * InputStreamReader(is)); String line = null; while ( (line =
	 * br.readLine()) != null ) buffer.append(line + "\r\n");
	 * 
	 * is.close(); con.disconnect(); return buffer.toString(); } catch(Exception
	 * e) { e.printStackTrace(); } finally { try { is.close(); } catch(Throwable
	 * t) {} try { con.disconnect(); } catch(Throwable t) {} }
	 * 
	 * return null;
	 * 
	 * }
	 */
}
