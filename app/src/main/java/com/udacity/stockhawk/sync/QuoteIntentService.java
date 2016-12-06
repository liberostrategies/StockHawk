package com.udacity.stockhawk.sync;

import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.database.Cursor;
import android.widget.RemoteViews;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.ui.MainActivity;
import com.udacity.stockhawk.widget.StockWidgetProvider;

import timber.log.Timber;

import static com.udacity.stockhawk.R.id.price;


public class QuoteIntentService extends IntentService {

    public QuoteIntentService() {
        super(QuoteIntentService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // Retrieve all of the Today widget ids: these are the widgets we need to update
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this,
                StockWidgetProvider.class));

        Timber.d("Intent handled");
        QuoteSyncJob.getQuotes(getApplicationContext());

        // Get the stored stock data.
        Cursor cursor = getContentResolver().query(
                Contract.Quote.uri,
                null, // leaving "columns" null just returns all the columns.
                null, //Contract.Quote.COLUMN_SYMBOL, // cols for "where" clause
                null, //new String[] {symbols[0]}, // values for "where" clause
                null  // sort order == by DATE ASCENDING
        );

        int numTickers = cursor.getCount();
        Timber.d("numTickers [" + numTickers + "]");

        // Parse data fields.
        String symbol1 = "";
        String closingPrice1 = "";
        if (cursor.moveToFirst()) {
            do {
                closingPrice1 = cursor.getString(Contract.Quote.POSITION_PRICE);
                symbol1 = cursor.getString(Contract.Quote.POSITION_SYMBOL);
                Timber.d(symbol1 + " Price [" + price + "]");
            } while (cursor.moveToNext());
        }
        cursor.close();

        // Perform this loop procedure for each widget
        for (int appWidgetId : appWidgetIds) {
            // Display data in widget.
            RemoteViews views = new RemoteViews(getPackageName(), R.layout.widget_small);

            // Add the data to the RemoteViews
//        views.setImageViewResource(R.id.widget_icon, weatherArtResourceId);
            // Content Descriptions for RemoteViews were only added in ICS MR1
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
//            setRemoteContentDescription(views, description);
//        }
            views.setTextViewText(R.id.widget_symbol, symbol1);
            views.setTextViewText(R.id.widget_closing_price, closingPrice1);

            // Create an Intent to launch MainActivity
            Intent launchIntent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, launchIntent, 0);
            views.setOnClickPendingIntent(R.id.widget_small_fragment, pendingIntent);

            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, views);

        }
    }
}
