package xyz.jovialconstruct.zeus.bakingguide.data;

import net.simonvt.schematic.annotation.AutoIncrement;
import net.simonvt.schematic.annotation.DataType;
import net.simonvt.schematic.annotation.NotNull;
import net.simonvt.schematic.annotation.PrimaryKey;

import static net.simonvt.schematic.annotation.DataType.Type.INTEGER;
import static net.simonvt.schematic.annotation.DataType.Type.TEXT;

/**
 * Created by zeus on 03/05/2017.
 */


public interface RecipeColumns {
    @DataType(INTEGER)
    @PrimaryKey
    @AutoIncrement
    String _ID = "_id";
    @DataType(INTEGER)
    @NotNull
    String ID = "id";
    @DataType(TEXT)
    @NotNull
    String NAME = "name";
    @DataType(INTEGER)
    @NotNull
    String SERVINGS = "servings";
    @DataType(TEXT)
    @NotNull
    String IMAGE = "image";
    @DataType(TEXT)
    @NotNull
    String INGREDIENTS_JSON = "ingredients";
    @DataType(TEXT)
    @NotNull
    String STEPS_JSON = "steps";
}
