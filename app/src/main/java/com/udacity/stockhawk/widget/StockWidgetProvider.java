/*
 * Copyright (c) 2016. Libero Strategies, LLC - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and Confidential
 */

package com.udacity.stockhawk.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.widget.RemoteViews;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.sync.QuoteIntentService;

import timber.log.Timber;

/**
 * Created by pink on 12/6/2016.
 */

public class StockWidgetProvider extends AppWidgetProvider {
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Timber.d("onUpdate(), appWidgetIds.length=" + appWidgetIds.length);
//        context.startService(new Intent(context, QuoteIntentService.class));
        // update each of the widgets with the remote adapter
        for (int i = 0; i < appWidgetIds.length; ++i) {
            Timber.d("appWidgetid=" + i);

            // Get the stored stock data.
            Cursor cursor = context.getContentResolver().query(
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
                Intent launchWidgetIntent = new Intent(context, StockWidgetRemoteViewsService.class);
                launchWidgetIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
                // When intents are compared, the extras are ignored, so we need to embed the extras
                // into the data so that the extras will not be ignored.
                launchWidgetIntent.setData(Uri.parse(launchWidgetIntent.toUri(Intent.URI_INTENT_SCHEME)));
                RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.widget_collection);

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
        }

        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        Timber.d("onAppWidgetOptionsChanged()");
        context.startService(new Intent(context, QuoteIntentService.class));
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (QuoteIntentService.ACTION_DATA_UPDATED.equals(intent.getAction())) {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(
                    new ComponentName(context, getClass()));
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_list);
        }
        super.onReceive(context, intent);
    }
}
