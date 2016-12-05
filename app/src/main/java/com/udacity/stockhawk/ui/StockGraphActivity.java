/*
 * Copyright (c) 2016. Libero Strategies, LLC - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and Confidential
 */

package com.udacity.stockhawk.ui;

import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;

import timber.log.Timber;

/**
 * Created by pink on 12/2/2016.
 */

public class StockGraphActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stock_graph);

        // Use Dates as Labels.
        GraphView graph = (GraphView) findViewById(R.id.graph);

        // Query db for historic values and store in arrays.
        int numSymbols = 1;
        String[] symbols = new String[numSymbols];
        symbols[0] = getIntent().getStringExtra(MainActivity.SYMBOL_EXTRA);//"AAPL";
        ((TextView)findViewById(R.id.symbol_text)).setText(symbols[0]);

        // A cursor is your primary interface to the query results.
        Cursor cursor = getContentResolver().query(
                Contract.Quote.makeUriForStock(symbols[0]),
                null, // leaving "columns" null just returns all the columns.
                null, //Contract.Quote.COLUMN_SYMBOL, // cols for "where" clause
                null, //new String[] {symbols[0]}, // values for "where" clause
                null  // sort order == by DATE ASCENDING
        );

        int numTickers = cursor.getCount();
        Timber.d("numTickers [" + numTickers + "]");

        // Populate the dates and quotes arrays.
        ArrayList dates = new ArrayList<Date>();
        ArrayList quotes = new ArrayList<Double>();
        final String COMMA = ",";
        final String NEW_LINE = "\n";
        cursor.moveToFirst();
        do {
            String history = cursor.getString(Contract.Quote.POSITION_HISTORY);
            Timber.d("History [" + history + "]");
            int idxPoint = 0;
            do {
                idxPoint = history.indexOf(NEW_LINE);
                String point = history.substring(0, idxPoint);
                Timber.d("point[" + point + "]");
                int idxComma = point.indexOf(COMMA);
                String dateLong = point.substring(0, idxComma);
                dates.add(new Date(Long.parseLong(dateLong)));
                String quote = point.substring(2 + idxComma);
                quotes.add(Double.parseDouble(quote));
                Timber.d("dateLong <" + dateLong + "> quote <<" + quote + ">>");
                history = history.substring(idxPoint + 1);
                Timber.d("new history [" + history + "]");
            } while (history.length() > 0);
        } while( cursor.moveToNext() );
        cursor.close();

// you can directly pass Date objects to DataPoint-Constructor
// this will convert the Date to double via Date#getTime()
        int numDates = dates.size();
        DataPoint[] dataPoints = new DataPoint[numDates];
        // Chronological plot
        for (int i=0; i<numDates; i++) {
            dataPoints[i] = new DataPoint((Date) dates.get(i), Double.valueOf((Double) quotes.get(i)));
        }
        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(dataPoints);
        series.setColor(Color.GREEN);
        series.setThickness(8);
        series.setBackgroundColor(Color.GREEN);
        series.setDrawBackground(true);

        graph.addSeries(series);

        // TO DO: Set decimal places in Y axis. Not working.
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMinimumFractionDigits(3);
        nf.setMinimumIntegerDigits(2);
        DefaultLabelFormatter dfl = new DefaultLabelFormatter(nf, nf);
        graph.getGridLabelRenderer().setLabelFormatter(dfl);

// set date label formatter
        DateAsXAxisLabelFormatter dateFormatter = new DateAsXAxisLabelFormatter(this);
        graph.getGridLabelRenderer().setLabelFormatter(dateFormatter);
        graph.getGridLabelRenderer().setNumHorizontalLabels(4); // only 4 because of the space

// set manual x bounds to have nice steps
        // Chronological plot. Set max as the earliest date and min as the most recent date.
        graph.getViewport().setMaxX(((Date)dates.get(0)).getTime());
        graph.getViewport().setMinX(((Date)dates.get(numDates-1)).getTime());
        graph.getViewport().setXAxisBoundsManual(true);

// as we use dates as labels, the human rounding to nice readable numbers
// is not necessary
        graph.getGridLabelRenderer().setHumanRounding(false);
    }
}
