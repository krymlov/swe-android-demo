package org.swisseph;

import static swisseph.SweMini.swe_mini;

import android.Manifest;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.GrantPermissionRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.jyotisa.api.IKundali;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class InstrSweMiniTest extends AndroidTest {

    @Rule
    public GrantPermissionRule mRuntimePermissionRule = GrantPermissionRule
            .grant(Manifest.permission.WRITE_EXTERNAL_STORAGE);

    @Test
    public void test_swe_mini() {
        swe_mini(getSwephExp(), 1, 1, 2022);
    }

    @Test
    public void test_swe_kundali() {
        final IKundali kundali = newChennaiKundali(getSwephExp());
        System.out.println(kundali);
    }

}