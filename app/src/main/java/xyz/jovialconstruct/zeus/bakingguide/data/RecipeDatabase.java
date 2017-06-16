package xyz.jovialconstruct.zeus.bakingguide.data;

import net.simonvt.schematic.annotation.Database;
import net.simonvt.schematic.annotation.Table;

@Database(version = RecipeDatabase.VERSION)
public final class RecipeDatabase {
    private RecipeDatabase() {
    }

    public static final int VERSION = 1;

    @Table(RecipeColumns.class)
    public static final String RECIPES = "recipes";
}
