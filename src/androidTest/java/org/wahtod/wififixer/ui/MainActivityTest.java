package org.wahtod.wififixer.ui;


import android.support.test.espresso.ViewInteraction;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wahtod.wififixer.R;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class MainActivityTest {

    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity.class);

    private static Matcher<View> childAtPosition(
            final Matcher<View> parentMatcher, final int position) {

        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("Child at position " + position + " in parent ");
                parentMatcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                ViewParent parent = view.getParent();
                return parent instanceof ViewGroup && parentMatcher.matches(parent)
                        && view.equals(((ViewGroup) parent).getChildAt(position));
            }
        };
    }

    @Test
    public void mainActivityTest() {
        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ViewInteraction appCompatTextView = onView(
                allOf(withId(R.id.logText), withText("..."),
                        childAtPosition(
                                allOf(withId(R.id.SCROLLER),
                                        childAtPosition(
                                                withId(R.id.logwrapper),
                                                0)),
                                0)));
        appCompatTextView.perform(scrollTo(), replaceText("...2018-01-14 22:40:56: \n\n********************\nService Build:1200\n********************\n\n2018-01-14 22:40:56: Strict Mode Extant\n2018-01-14 22:40:56: Prefs Change:Perf_Mode:false\n2018-01-14 22:40:56: Prefs Change:StateNotif:true\n2018-01-14 22:40:56: Prefs Change:WAKELOCK_DISABLE:false\n2018-01-14 22:40:56: Prefs Change:HTTP:false\n2018-01-14 22:40:56: Prefs Change:WiFiLock:true\n2018-01-14 22:40:56: Prefs Change:SCREEN:true\n2018-01-14 22:40:56: Prefs Change:DBMFLOOR:false\n2018-01-14 22:40:56: Prefs Change:MULTI:true\n2018-01-14 22:40:56: Prefs Change:WIDGET:false\n2018-01-14 22:40:56: Loading Settings\n2018-01-14 22:40:56: Debug Disabled\n2018-01-14 22:40:56: Service Started\n2018-01-14 22:40:56: Prefs Change:HASWIDGET:false\n2018-01-14 22:40:56: Start Intent\n2018-01-14 22:40:57: Prefs Change:STATNOTIFD:true\n2018-01-14 22:40:57: Prefs Change:SLPDEF:true\n2018-01-14 22:40:57: WIFI_STATE_ENABLED\n2018-01-14 22:40:57: Disabled network re-enabled:\"Guest\"\n2018-01-14 22:40:57: Disabled network re-enabled:\"VerizonWiFiAccess\"\n2018-01-14 22:40:57: Disabled network re-enabled:\"TFBC-PUBLC\"\n2018-01-14 22:40:57: Disabled network re-enabled:\"bcm-wifi\"\n2018-01-14 22:40:57: Disabled network re-enabled:\"bcm-guest\"\n2018-01-14 22:40:57: Disabled network re-enabled:\"WholeFoodsMarket\"\n2018-01-14 22:40:57: Disabled network re-enabled:\"xfinitywifi\"\n"));

        ViewInteraction appCompatTextView2 = onView(
                allOf(withId(R.id.logText), withText("...2018-01-14 22:40:56: \n\n********************\nService Build:1200\n********************\n\n2018-01-14 22:40:56: Strict Mode Extant\n2018-01-14 22:40:56: Prefs Change:Perf_Mode:false\n2018-01-14 22:40:56: Prefs Change:StateNotif:true\n2018-01-14 22:40:56: Prefs Change:WAKELOCK_DISABLE:false\n2018-01-14 22:40:56: Prefs Change:HTTP:false\n2018-01-14 22:40:56: Prefs Change:WiFiLock:true\n2018-01-14 22:40:56: Prefs Change:SCREEN:true\n2018-01-14 22:40:56: Prefs Change:DBMFLOOR:false\n2018-01-14 22:40:56: Prefs Change:MULTI:true\n2018-01-14 22:40:56: Prefs Change:WIDGET:false\n2018-01-14 22:40:56: Loading Settings\n2018-01-14 22:40:56: Debug Disabled\n2018-01-14 22:40:56: Service Started\n2018-01-14 22:40:56: Prefs Change:HASWIDGET:false\n2018-01-14 22:40:56: Start Intent\n2018-01-14 22:40:57: Prefs Change:STATNOTIFD:true\n2018-01-14 22:40:57: Prefs Change:SLPDEF:true\n2018-01-14 22:40:57: WIFI_STATE_ENABLED\n2018-01-14 22:40:57: Disabled network re-enabled:\"Guest\"\n2018-01-14 22:40:57: Disabled network re-enabled:\"VerizonWiFiAccess\"\n2018-01-14 22:40:57: Disabled network re-enabled:\"TFBC-PUBLC\"\n2018-01-14 22:40:57: Disabled network re-enabled:\"bcm-wifi\"\n2018-01-14 22:40:57: Disabled network re-enabled:\"bcm-guest\"\n2018-01-14 22:40:57: Disabled network re-enabled:\"WholeFoodsMarket\"\n2018-01-14 22:40:57: Disabled network re-enabled:\"xfinitywifi\"\n"),
                        childAtPosition(
                                allOf(withId(R.id.SCROLLER),
                                        childAtPosition(
                                                withId(R.id.logwrapper),
                                                0)),
                                0),
                        isDisplayed()));
        appCompatTextView2.perform(closeSoftKeyboard());

    }
}
