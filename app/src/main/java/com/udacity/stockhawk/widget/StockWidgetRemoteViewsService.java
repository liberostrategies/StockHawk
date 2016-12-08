/*
 * Copyright (c) 2016. Libero Strategies, LLC - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and Confidential
 */

package com.udacity.stockhawk.widget;

import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;

import timber.log.Timber;

/**
 * RemoteViewsService controlling the data being shown in the scrollable stock widget
 * Created by pink on 12/7/2016.
 */

public class StockWidgetRemoteViewsService extends RemoteViewsService {
    private static final String[] STOCK_COLUMNS = {
            Contract.Quote.TABLE_NAME + "." + Contract.Quote._ID,
            Contract.Quote.COLUMN_SYMBOL,
            Contract.Quote.COLUMN_PRICE
    };

    // Indices must match the projection.
    static final int INDEX_STOCK_ID = 0;
    static final int INDEX_STOCK_SYMBOL = 1;
    static final int INDEX_STOCK_PRICE = 2;

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactory() {
            private Cursor data = null;

            @Override
            public int getCount() {
                return data == null ? 0 : data.getCount();
            }

            @Override
            public void onCreate() {
                Timber.d("onCreate()");
            }

            @Override
            public void onDataSetChanged() {
                if (data != null) {
                    data.close();
                }
                // This method is called by the app hosting the widget (e.g., the launcher)
                // However, our ContentProvider is not exported so it doesn't have access to the
                // data. Therefore we need to clear (and finally restore) the calling identity so
                // that calls use our process and permission
                final long identityToken = Binder.clearCallingIdentity();
                data = getContentResolver().query(Contract.Quote.uri,
                        STOCK_COLUMNS,
                        null,
                        null,
                        Contract.Quote.COLUMN_SYMBOL + " ASC");
                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy() {
                if (data != null) {
                    data.close();
                    data = null;
                }
            }

            @Override
            public RemoteViews getViewAt(int i) {
                data.moveToPosition(i);
                String symbol = data.getString(INDEX_STOCK_SYMBOL);
                String price = data.getString(INDEX_STOCK_PRICE);

                RemoteViews views = new RemoteViews(getPackageName(), R.layout.widget_list_item);
                views.setTextViewText(R.id.widget_symbol, symbol);
                views.setTextViewText(R.id.widget_closing_price, price);
                Timber.d("getViewAt, i=" + i + ", " + symbol + ", " + price);

                final Intent fillInIntent = new Intent();
                fillInIntent.setData(Contract.Quote.uri);
                views.setOnClickFillInIntent(R.id.widget_list_item_frame, fillInIntent);

                return views;
            }

            @Override
            public RemoteViews getLoadingView() {
                return null;
//                return new RemoteViews(getPackageName(), R.layout.widget_list_item);
            }

            @Override
            public int getViewTypeCount() {
                return 2;
            }

            @Override
            public long getItemId(int i) {
                if (data.moveToPosition(i))
                    return data.getLong(Contract.Quote.POSITION_ID);
                return i;
            }

            @Override
            public boolean hasStableIds() {
                return false;
            }
        };
    }
}
