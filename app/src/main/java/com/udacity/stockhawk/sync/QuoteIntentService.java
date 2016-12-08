/*
 * Copyright (c) 2016. Libero Strategies, LLC - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and Confidential
 */

package com.udacity.stockhawk.sync;

import android.app.IntentService;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.widget.RemoteViews;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.widget.StockWidgetProvider;
import com.udacity.stockhawk.widget.StockWidgetRemoteViewsService;

import timber.log.Timber;


public class QuoteIntentService extends IntentService {
    public static final String ACTION_DATA_UPDATED =
            "com.udacity.stockhawk.sync.ACTION_DATA_UPDATED";

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
                Timber.d(symbol1 + " Price [" + closingPrice1 + "]");
            } while (cursor.moveToNext());
        }

        // Perform this loop procedure for each widget
        // update each of the widgets with the remote adapter
        for (int appWidgetId : appWidgetIds) {
            // Here we setup the intent which points to the StackViewService which will
            // provide the views for this collection.
            Intent launchWidgetIntent = new Intent(this, StockWidgetRemoteViewsService.class);
            launchWidgetIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            // When intents are compared, the extras are ignored, so we need to embed the extras
            // into the data so that the extras will not be ignored.
            launchWidgetIntent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
            RemoteViews rv = new RemoteViews(this.getPackageName(), R.layout.widget_collection);

            // Create an Intent to launch main activity.
//            Intent mainIntent = new Intent(this, StockHawkApp.class);
//            mainIntent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
//            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, mainIntent, 0);
//            rv.setOnClickPendingIntent(R.id.swipe_refresh, pendingIntent);

            rv.setRemoteAdapter(appWidgetId, R.id.widget_list, launchWidgetIntent);
            rv.setRemoteAdapter(R.id.widget_list, launchWidgetIntent);
            // The empty view is displayed when the collection has no items. It should be a sibling
            // of the collection view.
            rv.setEmptyView(R.id.widget_list, R.id.widget_empty);
            // Here we setup the a pending intent template. Individuals items of a collection
            // cannot setup their own pending intents, instead, the collection as a whole can
            // setup a pending intent template, and the individual items can set a fillInIntent
            // to create unique before on an item to item basis.
/*            Intent toastIntent = new Intent(getApplicationContext(), StockWidgetProvider.class);
//            toastIntent.setAction(StockWidgetProvider.TOAST_ACTION);
            toastIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
            PendingIntent toastPendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, toastIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
*/
//            PendingIntent clickPendingIntent = PendingIntent.getBroadcast(this, 0, launchWidgetIntent,
//                    PendingIntent.FLAG_UPDATE_CURRENT);
//            rv.setPendingIntentTemplate(R.id.widget_list, clickPendingIntent);
            appWidgetManager.updateAppWidget(appWidgetId, rv);
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_list);

            cursor.close();
        }
        updateWidgets();
    }

    private void updateWidgets() {
        // Setting the package ensures that only components in our app will receive the broadcast
        Intent dataUpdatedIntent = new Intent(ACTION_DATA_UPDATED)
                .setPackage(getPackageName());
        sendBroadcast(dataUpdatedIntent);
    }
}
