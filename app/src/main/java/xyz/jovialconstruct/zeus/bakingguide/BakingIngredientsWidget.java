package xyz.jovialconstruct.zeus.bakingguide;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.widget.RemoteViews;

import xyz.jovialconstruct.zeus.bakingguide.adapters.IngredientsListWidgetService;
import xyz.jovialconstruct.zeus.bakingguide.data.RecipeColumns;
import xyz.jovialconstruct.zeus.bakingguide.data.RecipeProvider;

/**
 * Implementation of App Widget functionality.
 */
public class BakingIngredientsWidget extends AppWidgetProvider {

    private static String mRecipeName;
    private static int mRecipeServings;

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {
        Resources resources = context.getResources();
        Cursor mCursor = context.getContentResolver().query(RecipeProvider.Recipes.CONTENT_URI, null, null, null, null);
        if (!(mCursor == null || mCursor.getCount() == 0)) {
            if (mCursor.moveToPosition(1)) {
                int id = mCursor.getInt(mCursor.getColumnIndex(RecipeColumns.ID));
                mRecipeName = mCursor.getString(mCursor.getColumnIndex(RecipeColumns.NAME));
                mRecipeServings = mCursor.getInt(mCursor.getColumnIndex(RecipeColumns.SERVINGS));
            }
            mCursor.close();
        }
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.baking_ingredients_widget);

        views.setTextViewText(R.id.ingredient_widget_title, String.format(context.getString(R.string.ingredient_widget_title), mRecipeName));
        views.setTextViewText(R.id.ingredient_widget_servings, resources.getQuantityString(R.plurals.servings, mRecipeServings, mRecipeServings));
        Intent intent = new Intent(context, IngredientsListWidgetService.class);
        views.setRemoteAdapter(R.id.ingredient_widget_list_view, intent);
        views.setEmptyView(R.id.ingredient_widget_list_view, R.id.ingredient_empty_view);
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

