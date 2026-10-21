/*
 * Copyright (C) By the Author
 * Author    Yura Krymlov
 * Created   2026-08
 */
package swisseph;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;

/**
 * Shared fixture for every {@link SwephExp} test: loads the native library once and points it
 * at the ephemeris files, then leaves a handful of constants and a couple of tiny helpers that
 * the rest of the suite builds on.
 * <p>
 * This project declares the 106 native methods only - there is no {@code SweConst}, no
 * {@code ISwissEph}, nothing but the JNI boundary. So the constants below are read directly
 * off {@code swephexp.h} rather than imported, and are kept to the handful the tests actually
 * need.
 * <p>
 * Scope of the whole suite: each native method is called with valid arguments and the result
 * is checked for internal consistency (round trips, agreement between related functions,
 * physically sane bounds) and for correct error signalling. Cross-checking the astronomy
 * itself against an external reference is swe-java-lib's job (etc/difference.txt,
 * SwetestCrossCheckTest) - duplicating that here would just be the same numbers twice.
 *
 * @author Yura Krymlov
 * @version 1.0, 2026-08
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.MethodName.class)
public abstract class ASwephTest {

    // ---------------------------------------------------------------- planets, swephexp.h
    static final int SE_SUN = 0;
    static final int SE_MOON = 1;
    static final int SE_MERCURY = 2;
    static final int SE_VENUS = 3;
    static final int SE_MARS = 4;
    static final int SE_JUPITER = 5;
    static final int SE_SATURN = 6;
    static final int SE_URANUS = 7;
    static final int SE_NEPTUNE = 8;
    static final int SE_PLUTO = 9;
    static final int SE_MEAN_NODE = 10;
    static final int SE_TRUE_NODE = 11;
    static final int SE_MEAN_APOG = 12;
    static final int SE_ECL_NUT = -1;

    // ------------------------------------------------------------------------- calc flags
    static final int SEFLG_JPLEPH = 1;
    static final int SEFLG_SWIEPH = 2;
    static final int SEFLG_MOSEPH = 4;
    static final int SEFLG_HELCTR = 8;
    static final int SEFLG_TRUEPOS = 16;
    static final int SEFLG_NONUT = 64;
    static final int SEFLG_SPEED = 256;
    static final int SEFLG_EQUATORIAL = 2 * 1024;
    static final int SEFLG_TOPOCTR = 32 * 1024;
    static final int SEFLG_SIDEREAL = 64 * 1024;

    // ---------------------------------------------------------------------------- calendar
    static final int SE_JUL_CAL = 0;
    static final int SE_GREG_CAL = 1;

    // -------------------------------------------------------------------------- ayanamsas
    static final int SE_SIDM_FAGAN_BRADLEY = 0;
    static final int SE_SIDM_LAHIRI = 1;

    // ------------------------------------------------------------------------- rise/transit
    static final int SE_CALC_RISE = 1;
    static final int SE_CALC_SET = 2;

    // ------------------------------------------------------------------------------ fixture
    /** 1 Jan 2000, 12:00 TT - the usual J2000.0 reference instant */
    static final double J2000 = 2451545.0;

    /** Machilipatnam, India - longitude first, east positive, as swephexp.h expects */
    static final double GEOLON = 81 + 8 / 60.;
    static final double GEOLAT = 16 + 10 / 60.;
    static final double GEOALT = 0.;

    static final double DELTA = 1e-9;

    @BeforeAll
    void loadLibraryAndSetEphemerisPath() {
        SwephExp.loadSweCurrentLibrary();
        SwephExp.swe_set_ephe_path(org.swisseph.android.TestFiles.ephePath());
    }

    /** {@code swe_calc()} into a fresh array, failing the test on error. */
    static double[] calc(double tjdET, int planet, int flags) {
        double[] xx = new double[6];
        StringBuilder serr = new StringBuilder();
        int ret = SwephExp.swe_calc(tjdET, planet, flags, xx, serr);
        if (ret < 0) {
            throw new AssertionError("swe_calc(" + planet + ") failed: " + serr);
        }
        return xx;
    }

    static double normDeg(double x) {
        double d = x % 360.;
        return d < 0 ? d + 360. : d;
    }
}
