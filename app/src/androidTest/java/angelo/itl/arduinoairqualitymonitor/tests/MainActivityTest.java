package angelo.itl.arduinoairqualitymonitor.tests;

/**
 * Created by Angelo on 11/12/2016.
 */

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import angelo.itl.arduinoairqualitymonitor.activity.main.MainActivity;
import angelo.itl.arduinoairqualitymonitor.R;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.anything;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class MainActivityTest {


    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(
            MainActivity.class);

    @Test
    public void test_Switch(){
        //onView(withId(R.id.connect_button)).perform(click());
        //pressBack();
        //onView(withId(R.id.bluetooth_switch)).perform(click());
        //onData(anything()).inAdapterView(withId(R.id.paired_devices)).atPosition(1).perform(click());
        //onView(withContentDescription("open")).perform(click());
        //onView(withText("Valores")).perform(click());
        //onView(withId(R.id.bluetooth_progress_bar)).check(matches(ViewMatchers.isDisplayed()));
    }



    /*
    @Before
    public void initValidString() {
        // Specify a valid string.
        mStringToBetyped = "Espresso";
    }

    @Test
    public void changeText_sameActivity() {

        /* Type text and then press the button.
        onView(withId(R.id.editTextUserInput))
                .perform(typeText(mStringToBetyped), closeSoftKeyboard());
        onView(withId(R.id.changeTextBt)).perform(click());

        // Check that the text was changed.
        onView(withId(R.id.textToBeChanged))
                .check(matches(withText(mStringToBetyped)));
    }*/
}