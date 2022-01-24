package org.swisseph;

import static swisseph.SweConst.SEFLG_NONUT;
import static swisseph.SweConst.SEFLG_SIDEREAL;
import static swisseph.SweConst.SEFLG_SPEED;
import static swisseph.SweConst.SEFLG_SWIEPH;
import static swisseph.SweConst.SEFLG_TRUEPOS;
import static swisseph.SweMini.swe_mini;

import android.Manifest;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.GrantPermissionRule;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.jyotisa.api.IKundali;
import org.jyotisa.gochara.rasi.RasiLagnaGochara;
import org.swisseph.app.SweRuntimeException;

import swisseph.SweConst;
import swisseph.SweDate;
import swisseph.SwissEph;
import swisseph.TransitCalculator;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class InstrSweMiniTest extends AndroidTest {
    public static final int N_MAX_ITER = 333;

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

    @Test
    public void test_swe_calc_ut() {
        final StringBuilder serr = new StringBuilder();
        final double julDay = new SweDate().getJulDay();

        final int iflag = SEFLG_SIDEREAL |   // sidereal zodiac
                SEFLG_SWIEPH  |   // fastest method, requires data files
                SEFLG_TRUEPOS |   // true position of the planet versus the apparent position
                SEFLG_NONUT   |   // calculate the position of the planet without considering the nutation
                SEFLG_SPEED;      // to determine retrograde vs direct motion

        try (SwissEph swissEph = newSwissEph(); SwephNative swephNative = newSwephExp()) {
            for (int ipl = SweConst.SE_SUN; ipl <= SweConst.SE_TRUE_NODE; ipl++) {
                final double[] xx1 = new double[6];
                serr.setLength(0);

                // swe_calc_ut of Java port (Thomas Mach)
                int res1 = swissEph.swe_calc_ut(julDay, ipl, iflag, xx1, serr);
                if (res1 == SweConst.ERR) throw new SweRuntimeException(serr.toString());

                final double[] xx2 = new double[6];
                // swe_calc_ut of JNI port - native C library
                int res2 = swephNative.swe_calc_ut(julDay, ipl, iflag, xx2, serr);
                if (res2 == SweConst.ERR) throw new SweRuntimeException(serr.toString());

                Assert.assertArrayEquals(xx1, xx2, 0.001);
            }
        }
    }

    @Test
    public void test_java_swe_get_transit() {
        double transitUT = swe_get_transit(newChennaiKundali(getSwissEph()));
        System.out.println("JAVA SwissEph transit UT: " + transitUT);
    }

    @Test
    public void test_jni_swe_get_transit() {
        double transitUT = swe_get_transit(newChennaiKundali(getSwephExp()));
        System.out.println("JNI SwephNative transit UT: " + transitUT);
    }

    static double swe_get_transit(final IKundali kundali) {
        double transitUT = 0d;

        for (int i = 0; i < N_MAX_ITER; i++) {
            final boolean backwards = i % 2 == 0;
            TransitCalculator transitCalc = new RasiLagnaGochara(kundali).createTransitCalc(i);
            transitUT = TransitCalculator.getTransitUT(transitCalc, kundali.sweJulianDate().julianDay(), backwards);
        }

        return transitUT;
    }
}