/*
 * Copyright (c) 2016. Libero Strategies, LLC - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and Confidential
 */

package com.udacity.stockhawk.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.udacity.stockhawk.sync.QuoteIntentService;

import timber.log.Timber;

/**
 * Created by pink on 12/6/2016.
 */

public class StockWidgetProvider extends AppWidgetProvider {
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Timber.d("onUpdate()");
        context.startService(new Intent(context, QuoteIntentService.class));
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        Timber.d("onAppWidgetOptionsChanged()");
        context.startService(new Intent(context, QuoteIntentService.class));
    }

}
