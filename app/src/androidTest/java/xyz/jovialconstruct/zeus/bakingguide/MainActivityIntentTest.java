/*
* Copyright (C) 2017 The Android Open Source Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*  	http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package xyz.jovialconstruct.zeus.bakingguide;

import android.app.Activity;
import android.content.Intent;
import android.support.test.espresso.ViewInteraction;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.app.Instrumentation.ActivityResult;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.Intents.intending;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasExtra;
import static android.support.test.espresso.intent.matcher.IntentMatchers.isInternal;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.core.IsNot.not;
import static xyz.jovialconstruct.zeus.bakingguide.MainActivity.RECIPE_ID;

@RunWith(AndroidJUnit4.class)
public class MainActivityIntentTest {
    @Rule
    public IntentsTestRule<MainActivity> mActivityRule = new IntentsTestRule<>(
            MainActivity.class);


    @Before
    public void stubAllExternalIntents() {
        intending(not(isInternal())).respondWith(new ActivityResult(Activity.RESULT_OK, null));
    }

    @Test
    public void recipeCardSelected() {
        ViewInteraction recyclerView = onView(withId(R.id.recipe_recycler_view));
        recyclerView.perform(actionOnItemAtPosition(1, click()));
        intended(allOf(hasExtra(RECIPE_ID, 4)));
    }

    @Test
    public void recipCardSelected() {
        ViewInteraction recyclerView = onView(withId(R.id.recipe_recycler_view));
        recyclerView.perform(actionOnItemAtPosition(1, click()));
        Intent resultData = new Intent();
        resultData.putExtra(RECIPE_ID, 4);
        ActivityResult result = new ActivityResult(Activity.RESULT_OK, resultData);
        intending(allOf(hasExtra(RECIPE_ID, 4))).respondWith(result);

        onView(allOf(withText("INGREDIENTS"))).check(matches(isDisplayed()));

    }
}