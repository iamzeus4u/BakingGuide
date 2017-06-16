package xyz.jovialconstruct.zeus.bakingguide.data;

import android.net.Uri;

import net.simonvt.schematic.annotation.ContentProvider;
import net.simonvt.schematic.annotation.ContentUri;
import net.simonvt.schematic.annotation.InexactContentUri;
import net.simonvt.schematic.annotation.TableEndpoint;

@ContentProvider(authority = RecipeProvider.AUTHORITY, database = RecipeDatabase.class)
public final class RecipeProvider {

    static final String AUTHORITY = "xyz.jovialconstruct.zeus.bakingguide.data.RecipeProvider";

    private static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    private static Uri buildUri(String... paths) {
        Uri.Builder builder = BASE_CONTENT_URI.buildUpon();
        for (String path : paths) {
            builder.appendPath(path);
        }
        return builder.build();
    }
    @TableEndpoint(table = RecipeDatabase.RECIPES)
    public static class Recipes {
        private static final String PATH = "recipes";

        @ContentUri(
                path = PATH,
                type = "vnd.android.cursor.dir/recipe",
                defaultSort = RecipeColumns.NAME + " ASC")
        public static final Uri CONTENT_URI = buildUri(PATH);

        @InexactContentUri(
                name = RecipeColumns.ID,
                path = PATH + "/#",
                type = "vnd.android.cursor.item/recipe",
                whereColumn = RecipeColumns.ID,
                pathSegment = 1)
        public static Uri withId(int id) {
            return buildUri(PATH, String.valueOf(id));
        }
    }
}
