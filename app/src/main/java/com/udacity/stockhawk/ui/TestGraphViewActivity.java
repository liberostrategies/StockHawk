/*
 * Copyright (c) 2016. Libero Strategies, LLC - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and Confidential
 */

package com.udacity.stockhawk.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.udacity.stockhawk.R;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by pink on 12/2/2016.
 */

public class TestGraphViewActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_graphview);

        // Use Dates as Labels.
        // generate Dates
        Calendar calendar = Calendar.getInstance();
        // 2016-11-28
        calendar.set(2016, 10, 14);
        Date d1 = calendar.getTime();
        calendar.set(2016, 10, 21);
        calendar.set(Calendar.DATE, 21);
        Date d2 = calendar.getTime();
        calendar.set(2016, 10, 28);
        Date d3 = calendar.getTime();

        GraphView graph = (GraphView) findViewById(R.id.graph);

// you can directly pass Date objects to DataPoint-Constructor
// this will convert the Date to double via Date#getTime()
        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(new DataPoint[] {
                new DataPoint(d1, 60.349998),
                new DataPoint(d2, 60.529999),
                new DataPoint(d3, 59.200001)
        });

        graph.addSeries(series);

// set date label formatter
        DateAsXAxisLabelFormatter dateFormatter = new DateAsXAxisLabelFormatter(this);
        graph.getGridLabelRenderer().setLabelFormatter(dateFormatter);
        graph.getGridLabelRenderer().setNumHorizontalLabels(3); // only 4 because of the space

// set manual x bounds to have nice steps
        graph.getViewport().setMinX(d1.getTime());
        graph.getViewport().setMaxX(d3.getTime());
        graph.getViewport().setXAxisBoundsManual(true);

// as we use dates as labels, the human rounding to nice readable numbers
// is not necessary
        graph.getGridLabelRenderer().setHumanRounding(false);
    }
}
