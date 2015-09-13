package com.example.gveapp;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.LineGraphView;
import com.jjoe64.graphview.GraphView.GraphViewData;
import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.Toast;

public class RouteGraph extends Activity {

    String m_sFilename = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

		/*
         * Toast.makeText(getApplicationContext(), "onCreate",
		 * Toast.LENGTH_SHORT).show();
		 */

        setContentView(R.layout.activity_route_graph);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            // Get data via the key
            String Filename = extras.getString("Filename");
            if (Filename != null) {
                m_sFilename = Filename;
            }
        }

        ArrayList<GraphViewData> AltitudeList = new ArrayList<GraphViewData>();
        ArrayList<GraphViewData> SpeedList = new ArrayList<GraphViewData>();

        try {
            // open the file for reading
            InputStream instream = new FileInputStream(m_sFilename);

            // if file the available for reading
            if (instream != null) {
                // prepare the file for reading
                InputStreamReader inputreader = new InputStreamReader(instream);
                BufferedReader buffreader = new BufferedReader(inputreader);

                String line = null;

                int i = 0;
                // read every line of the file into the line-variable, on line
                // at the time
                do {
                    line = buffreader.readLine();

                    String y[] = line.split(",");

                    i++;
                    AltitudeList.add(new GraphViewData(i, Float
                            .parseFloat(y[2])));
                    SpeedList.add(new GraphViewData(i, 3.6f * Float
                            .parseFloat(y[3])));

                    // do something with the line
                } while (line != null);

                instream.close();

            }
        } catch (Exception ex) {
        }

        int size = AltitudeList.size();
        GraphViewData[] AltitudeArray = new GraphViewData[size];
        GraphViewData[] SpeedArray = new GraphViewData[size];
        for (int i = 0; i < size; i++) {
            AltitudeArray[i] = AltitudeList.get(i);
            SpeedArray[i] = SpeedList.get(i);
        }

        // altitude
        GraphViewSeries exampleSeries = new GraphViewSeries(AltitudeArray);
        GraphView graphView = new LineGraphView(this // context
                , "altitude (m)" // heading
        );

        SetGraphView(graphView, exampleSeries);
        LinearLayout layout = (LinearLayout) findViewById(R.id.subLayout1);
        layout.addView(graphView);
        layout.setBackgroundColor(Color.TRANSPARENT);

        // speed
        GraphViewSeries exampleSeries2 = new GraphViewSeries(SpeedArray);
        GraphView graphView2 = new LineGraphView(this // context
                , "speed (km/h)" // heading
        );
        SetGraphView(graphView2, exampleSeries2);
        LinearLayout layout2 = (LinearLayout) findViewById(R.id.subLayout2);
        layout2.addView(graphView2);
        layout2.setBackgroundColor(Color.TRANSPARENT);
    }

    private void SetGraphView(GraphView gv, GraphViewSeries gvs) {
        gv.addSeries(gvs);
        gv.setScrollable(true);
        gv.setScalable(true);

        gv.getGraphViewStyle().setTextSize(14.0f);
        gv.getGraphViewStyle().setVerticalLabelsColor(Color.RED);
        gv.getGraphViewStyle().setHorizontalLabelsColor(Color.RED);
        gv.getGraphViewStyle().setGridColor(Color.LTGRAY);
        gv.getGraphViewStyle().setNumHorizontalLabels(0);
        gv.getGraphViewStyle().setNumVerticalLabels(5);
        // gv.getGraphViewStyle().setVerticalLabelsWidth(13);
    }


}
