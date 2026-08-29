/*
 * Copyright (C) By the Author
 * Author    Yura Krymlov
 * Created   2026-08
 */

package org.swisseph;

import static org.junit.Assert.assertEquals;
import static org.swisseph.api.ISweObjects.BU;
import static org.swisseph.api.ISweObjects.CH;
import static org.swisseph.api.ISweObjects.GU;
import static org.swisseph.api.ISweObjects.KE;
import static org.swisseph.api.ISweObjects.LG;
import static org.swisseph.api.ISweObjects.MA;
import static org.swisseph.api.ISweObjects.NE;
import static org.swisseph.api.ISweObjects.PL;
import static org.swisseph.api.ISweObjects.RA;
import static org.swisseph.api.ISweObjects.SA;
import static org.swisseph.api.ISweObjects.SK;
import static org.swisseph.api.ISweObjects.SY;
import static org.swisseph.api.ISweObjects.UR;
import static org.swisseph.app.SweAyanamsa.TRUE_CITRA;
import static org.swisseph.app.SweHouseSystem.KOCH;
import static org.swisseph.app.SweHouseSystem.PLACIDUS;
import static org.swisseph.app.SweHouseSystem.WHOLE_SIGN;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.swisseph.api.ISweGeoLocation;
import org.swisseph.api.ISweHouseSystem;
import org.swisseph.api.ISweObjects;
import org.swisseph.api.ISweObjectsOptions;
import org.swisseph.app.SweGeoLocation;
import org.swisseph.app.SweJulianDate;
import org.swisseph.app.SweObjects;
import org.swisseph.app.SweObjectsOptions;

/**
 * The swe-java-lib reference fixture, run against the <b>Android</b> native library.
 * <p>
 * This is the on-device twin of SwetestRefChartsTest in swe-java-lib. It cannot drive
 * swetest64.exe - there is no such executable on Android - so every expected value below
 * is a literal captured from it on the desktop, with the same ephemeris files the app
 * ships in assets/ephe.
 * <p>
 * One place and time of day, eleven epochs: <b>4 April, 17:50:40 local, time zone
 * 5:30 East, 81&deg;08'E 16&deg;10'N (Machilipatnam, India)</b>. Scope is lagna, planets,
 * ayanamsa and house cusps - nothing else.
 * <p>
 * The epoch list stops at 1800 on purpose: assets/ephe carries only the sepl_18/semo_18
 * block, which covers 1800..2399. Asking for the year 1000 here would make Swiss Ephemeris
 * fall back to the analytic Moshier theory and the comparison would be meaningless. Add
 * sepl_00/_06/_12 and semo_00/_06/_12 to assets if the older epochs are wanted.
 *
 * @author Yura Krymlov
 * @version 1.0, 2026-08
 */
@RunWith(AndroidJUnit4.class)
public class InstrSweRefChartsTest extends AndroidTest {

    /** swetest prints 7 decimals of a degree, so half of the last digit is the bound */
    static final double DELTA = 1e-6;

    /**
     * The pure Java port is Swiss Ephemeris 2.01.00 while the .so is 2.10.03. Positions
     * drift by tens of arc seconds and the lagna by a few arc minutes - see
     * swe-java-lib/etc/difference.txt.
     */
    static final double DELTA_JAVA = 300. / 3600.;

    // Machilipatnam: 81 E 08' 00", 16 N 10' 00"
    static final ISweGeoLocation GEO_MACHILIPATNAM = new SweGeoLocation(81 + 8 / 60., 16 + 10 / 60., 0.);

    static final float TIME_ZONE = 5.5f;
    static final double LOCAL_TIME = 17 + 50 / 60. + 40 / 3600.;   // 17:50:40

    // column layout of REF
    static final int C_AYANAMSA = 1, C_SUN = 2, C_MEAN_NODE = 12,
            C_TRUE_NODE = 13, C_ASC = 14, C_MC = 15, C_ARMC = 16, C_VERTEX = 17;

    /** swetest body order Sun..Pluto mapped onto ISweObjects indices */
    static final int[] SWETEST_TO_OBJECT = {SY, CH, BU, SK, MA, GU, SA, UR, NE, PL};

    /**
     * swetest64.exe -b4.4.<year> -ut12:20:40 -eswe -true -sid27 -fPl \
     *               -p0123456789mt -house81.133333,16.166667,<hsys>
     * <p>
     * year, ayanamsa, Sun..Pluto, mean node, true node, Ascendant, MC, ARMC, Vertex
     */
    static final double[][] REF = {
            {1800, 21.0525129, 353.4438504, 106.3731815, 10.6476790, 321.5275654, 290.2229898, 63.8852871, 102.2711281, 153.2018176, 205.6341831, 312.7310691, 7.2392418, 6.5494799, 167.5300049, 77.0948645, 98.8708421, 320.6879982},
            {1900, 22.4540568, 351.8325698, 53.9898425, 334.5610300, 35.7273504, 334.7140915, 228.3230896, 252.5058191, 229.8915176, 62.0087817, 52.4779946, 231.7549596, 230.2311916, 165.9246621, 75.5002940, 98.6601554, 319.6352080},
            {1970, 23.4277207, 350.8931505, 328.6069281, 3.1474393, 7.9344396, 26.6938484, 189.7792299, 15.1192788, 162.9958511, 217.1838573, 152.1394310, 316.9074209, 318.1538506, 164.9924568, 74.5666073, 98.7031868, 318.5692373},
            {1990, 23.7087388, 350.7688344, 106.3914754, 6.6451193, 304.3855963, 294.0344921, 69.4415930, 270.8824051, 255.8436525, 260.8273157, 203.5409071, 289.8008681, 291.0795686, 164.8617785, 74.4291706, 98.8588966, 317.9963243},
            {2000, 23.8405921, 351.1984226, 348.0661286, 324.3720069, 333.3794791, 15.2649731, 16.1384917, 22.0727328, 295.9623746, 282.4218973, 228.9485161, 96.2211495, 96.8851660, 165.2732618, 74.8159428, 99.4216206, 316.8406992},
            {2010, 23.9889353, 350.6406761, 239.3885972, 8.9164670, 10.6800917, 99.6465618, 324.0749178, 156.2491893, 333.6201637, 303.8615895, 251.4298972, 262.6944610, 262.5698120, 164.7311802, 74.2919177, 99.0138611, 317.4263045},
            {2030, 24.2682104, 350.5182389, 8.2838241, 9.4500597, 304.7946297, 2.9663926, 212.6245661, 28.5572511, 51.0239045, 346.6959341, 287.4630374, 235.5881987, 233.9655726, 164.6006780, 74.1548486, 99.1679889, 316.8600122},
            {2050, 24.5466224, 350.3888804, 138.8455416, 7.7333444, 13.3865131, 261.2859675, 90.1288808, 282.8389293, 143.0612541, 29.7923993, 315.3026300, 208.4820763, 206.8825857, 164.4697563, 74.0174557, 99.3208030, 316.2986116},
            {2070, 24.8241143, 350.2639267, 267.2879944, 3.3841860, 305.8063635, 67.8078769, 343.1738407, 171.4108988, 238.6748794, 73.4264214, 338.3699293, 181.3760946, 180.7951876, 164.3394557, 73.8807153, 99.4733175, 315.7408700},
            {2090, 25.1010641, 350.1396280, 40.1001142, 356.4189233, 16.0434976, 338.3428115, 234.7241067, 42.3885713, 318.6401700, 117.6706011, 358.6432905, 154.2702536, 154.7841658, 164.2092284, 73.7440645, 99.6253512, 315.1873862},
            {2100, 25.2420155, 349.5824290, 292.8236022, 335.6780707, 30.0353336, 54.4805645, 173.5541703, 178.7367752, 355.9934023, 139.9557620, 8.1344543, 320.7438817, 321.9630173, 163.6681354, 73.2209130, 99.2108411, 315.7945205},
    };

    /** PLACIDUS cusps 1..12, one row per epoch */
    static final double[][] CUSPS_PLACIDUS = {
            {167.5300049, 197.3695325, 227.3525953, 257.0948645, 287.2166606, 317.8146615, 347.5300049, 17.3695325, 47.3525953, 77.0948645, 107.2166606, 137.8146615},   // 1800
            {165.9246621, 195.7647619, 225.7552707, 255.5002940, 285.6154948, 316.2058200, 345.9246621, 15.7647619, 45.7552707, 75.5002940, 105.6154948, 136.2058200},   // 1900
            {164.9924568, 194.8318597, 224.8211283, 254.5666073, 284.6837670, 315.2751379, 344.9924568, 14.8318597, 44.8211283, 74.5666073, 104.6837670, 135.2751379},   // 1970
            {164.8617785, 194.6998097, 224.6840494, 254.4291706, 284.5522031, 315.1483779, 344.8617785, 14.6998097, 44.6840494, 74.4291706, 104.5522031, 135.1483779},   // 1990
            {165.2732618, 195.1065439, 225.0725281, 254.8159428, 284.9599758, 315.5737607, 345.2732618, 15.1065439, 45.0725281, 74.8159428, 104.9599758, 135.5737607},   // 2000
            {164.7311802, 194.5677920, 224.5470593, 254.2919177, 284.4208509, 315.0217607, 344.7311802, 14.5677920, 44.5470593, 74.2919177, 104.4208509, 135.0217607},   // 2010
            {164.6006780, 194.4358409, 224.4101875, 254.1548486, 284.2896890, 314.8952763, 344.6006780, 14.4358409, 44.4101875, 74.1548486, 104.2896890, 134.8952763},   // 2030
            {164.4697563, 194.3034555, 224.2729435, 254.0174557, 284.1581804, 314.7683821, 344.4697563, 14.3034555, 44.2729435, 74.0174557, 104.1581804, 134.7683821},   // 2050
            {164.3394557, 194.1716827, 224.1363331, 253.8807153, 284.0273240, 314.6421230, 344.3394557, 14.1716827, 44.1363331, 73.8807153, 104.0273240, 134.6421230},   // 2070
            {164.2092284, 194.0399918, 223.9998224, 253.7440645, 283.8965338, 314.5159236, 344.2092284, 14.0399918, 43.9998224, 73.7440645, 103.8965338, 134.5159236},   // 2090
            {163.6681354, 193.5025694, 223.4757210, 253.2209130, 283.3577374, 313.9643151, 343.6681354, 13.5025694, 43.4757210, 73.2209130, 103.3577374, 133.9643151},   // 2100
    };

    /** KOCH cusps 1..12, one row per epoch */
    static final double[][] CUSPS_KOCH = {
            {167.5300049, 198.2655207, 227.6556709, 257.0948645, 286.2071093, 316.3105037, 347.5300049, 18.2655207, 47.6556709, 77.0948645, 106.2071093, 136.3105037},   // 1800
            {165.9246621, 196.6678325, 226.0667718, 255.5002940, 284.6147186, 314.7093423, 345.9246621, 16.6678325, 46.0667718, 75.5002940, 104.6147186, 134.7093423},   // 1900
            {164.9924568, 195.7330682, 225.1307646, 254.5666073, 283.6816353, 313.7777882, 344.9924568, 15.7330682, 45.1307646, 74.5666073, 103.6816353, 133.7777882},   // 1970
            {164.8617785, 195.5951099, 224.9872278, 254.4291706, 283.5443037, 313.6464945, 344.8617785, 15.5951099, 44.9872278, 74.4291706, 103.5443037, 133.6464945},   // 1990
            {165.2732618, 195.9807906, 225.3524679, 254.8159428, 283.9309352, 314.0550321, 345.2732618, 15.9807906, 45.3524679, 74.8159428, 103.9309352, 134.0550321},   // 2000
            {164.7311802, 195.4571914, 224.8438039, 254.2919177, 283.4072379, 313.5154081, 344.7311802, 15.4571914, 44.8438039, 74.2919177, 103.4072379, 133.5154081},   // 2010
            {164.6006780, 195.3193621, 224.7005302, 254.1548486, 283.2704075, 313.3845020, 344.6006780, 15.3193621, 44.7005302, 74.1548486, 103.2704075, 133.3845020},   // 2030
            {164.4697563, 195.1811466, 224.5569385, 254.0174557, 283.1332856, 313.2532358, 344.4697563, 15.1811466, 44.5569385, 74.0174557, 103.1332856, 133.2532358},   // 2050
            {164.3394557, 195.0435638, 224.4139961, 253.8807153, 282.9968217, 313.1226058, 344.3394557, 15.0435638, 44.4139961, 73.8807153, 102.9968217, 133.1226058},   // 2070
            {164.2092284, 194.9061009, 224.2711798, 253.7440645, 282.8604239, 312.9920221, 344.2092284, 14.9061009, 44.2711798, 73.7440645, 102.8604239, 132.9920221},   // 2090
            {163.6681354, 194.3842264, 223.7642068, 253.2209130, 282.3371311, 312.4527094, 343.6681354, 14.3842264, 43.7642068, 73.2209130, 102.3371311, 132.4527094},   // 2100
    };

    /** WHOLE_SIGN cusps 1..12, one row per epoch */
    static final double[][] CUSPS_WHOLE_SIGN = {
            {150.0000000, 180.0000000, 210.0000000, 240.0000000, 270.0000000, 300.0000000, 330.0000000, 0.0000000, 30.0000000, 60.0000000, 90.0000000, 120.0000000},   // 1800
            {150.0000000, 180.0000000, 210.0000000, 240.0000000, 270.0000000, 300.0000000, 330.0000000, 0.0000000, 30.0000000, 60.0000000, 90.0000000, 120.0000000},   // 1900
            {150.0000000, 180.0000000, 210.0000000, 240.0000000, 270.0000000, 300.0000000, 330.0000000, 0.0000000, 30.0000000, 60.0000000, 90.0000000, 120.0000000},   // 1970
            {150.0000000, 180.0000000, 210.0000000, 240.0000000, 270.0000000, 300.0000000, 330.0000000, 0.0000000, 30.0000000, 60.0000000, 90.0000000, 120.0000000},   // 1990
            {150.0000000, 180.0000000, 210.0000000, 240.0000000, 270.0000000, 300.0000000, 330.0000000, 0.0000000, 30.0000000, 60.0000000, 90.0000000, 120.0000000},   // 2000
            {150.0000000, 180.0000000, 210.0000000, 240.0000000, 270.0000000, 300.0000000, 330.0000000, 0.0000000, 30.0000000, 60.0000000, 90.0000000, 120.0000000},   // 2010
            {150.0000000, 180.0000000, 210.0000000, 240.0000000, 270.0000000, 300.0000000, 330.0000000, 0.0000000, 30.0000000, 60.0000000, 90.0000000, 120.0000000},   // 2030
            {150.0000000, 180.0000000, 210.0000000, 240.0000000, 270.0000000, 300.0000000, 330.0000000, 0.0000000, 30.0000000, 60.0000000, 90.0000000, 120.0000000},   // 2050
            {150.0000000, 180.0000000, 210.0000000, 240.0000000, 270.0000000, 300.0000000, 330.0000000, 0.0000000, 30.0000000, 60.0000000, 90.0000000, 120.0000000},   // 2070
            {150.0000000, 180.0000000, 210.0000000, 240.0000000, 270.0000000, 300.0000000, 330.0000000, 0.0000000, 30.0000000, 60.0000000, 90.0000000, 120.0000000},   // 2090
            {150.0000000, 180.0000000, 210.0000000, 240.0000000, 270.0000000, 300.0000000, 330.0000000, 0.0000000, 30.0000000, 60.0000000, 90.0000000, 120.0000000},   // 2100
    };

    private static int[] date(int year) {
        return new int[]{year, 4, 4, 17, 50};
    }

    private ISweObjects chart(ISwissEph swissEph, int year, ISweHouseSystem hsys, boolean trueNode) {
        ISweObjectsOptions options = new SweObjectsOptions.Builder()
                .ayanamsa(TRUE_CITRA).houseSystem(hsys).trueNode(trueNode).build();
        return new SweObjects(swissEph, new SweJulianDate(date(year), TIME_ZONE, LOCAL_TIME),
                GEO_MACHILIPATNAM, options).completeBuild();
    }

    // ================================================================= ayanamsa

    @Test
    public void test_ayanamsa_matches_swetest() {
        for (double[] row : REF) {
            int year = (int) row[0];
            ISweObjects o = chart(getSwephExp(), year, PLACIDUS, false);
            assertEquals("ayanamsa " + year, row[C_AYANAMSA], o.ayanamsa(), DELTA);
        }
    }

    // ================================================================== planets

    @Test
    public void test_planets_match_swetest() {
        for (double[] row : REF) {
            int year = (int) row[0];
            ISweObjects o = chart(getSwephExp(), year, PLACIDUS, false);
            for (int i = 0; i < SWETEST_TO_OBJECT.length; i++) {
                assertEquals("year " + year + " body " + i,
                        row[C_SUN + i], o.longitudes()[SWETEST_TO_OBJECT[i]], DELTA);
            }
        }
    }

    @Test
    public void test_lunar_nodes_match_swetest() {
        for (double[] row : REF) {
            int year = (int) row[0];

            ISweObjects mean = chart(getSwephExp(), year, PLACIDUS, false);
            assertEquals("mean Rahu " + year, row[C_MEAN_NODE], mean.longitudes()[RA], DELTA);
            assertEquals("mean Ketu " + year, (row[C_MEAN_NODE] + 180.) % 360.,
                    mean.longitudes()[KE], DELTA);

            ISweObjects node = chart(getSwephExp(), year, PLACIDUS, true);
            assertEquals("true Rahu " + year, row[C_TRUE_NODE], node.longitudes()[RA], DELTA);
            assertEquals("true Ketu " + year, (row[C_TRUE_NODE] + 180.) % 360.,
                    node.longitudes()[KE], DELTA);
        }
    }

    // ==================================================================== lagna

    @Test
    public void test_lagna_and_ascmc_match_swetest() {
        for (double[] row : REF) {
            int year = (int) row[0];
            ISweObjects o = chart(getSwephExp(), year, PLACIDUS, false);

            assertEquals("lagna " + year, row[C_ASC], o.longitudes()[LG], DELTA);
            assertEquals("ascmc[0] " + year, row[C_ASC], o.ascmc()[0], DELTA);
            assertEquals("MC " + year, row[C_MC], o.ascmc()[1], DELTA);
            assertEquals("ARMC " + year, row[C_ARMC], o.ascmc()[2], DELTA);
            assertEquals("Vertex " + year, row[C_VERTEX], o.ascmc()[3], DELTA);
        }
    }

    // =================================================================== houses

    @Test
    public void test_placidus_cusps_match_swetest() {
        assertCuspsMatch(PLACIDUS, CUSPS_PLACIDUS);
    }

    @Test
    public void test_koch_cusps_match_swetest() {
        assertCuspsMatch(KOCH, CUSPS_KOCH);
    }

    @Test
    public void test_whole_sign_cusps_match_swetest() {
        assertCuspsMatch(WHOLE_SIGN, CUSPS_WHOLE_SIGN);

        // whole sign cusps must land exactly on sign boundaries
        for (double[] row : REF) {
            int year = (int) row[0];
            ISweObjects o = chart(getSwephExp(), year, WHOLE_SIGN, false);
            for (int h = 1; h <= 12; h++) {
                assertEquals("cusp " + h + " of " + year + " is a sign boundary",
                        0., o.cusps()[h] % 30., 1e-9);
            }
        }
    }

    private void assertCuspsMatch(ISweHouseSystem hsys, double[][] expected) {
        for (int r = 0; r < REF.length; r++) {
            int year = (int) REF[r][0];
            ISweObjects o = chart(getSwephExp(), year, hsys, false);
            for (int h = 1; h <= 12; h++) {
                assertEquals(hsys + " " + year + " house " + h,
                        expected[r][h - 1], o.cusps()[h], DELTA);
            }
        }
    }

    // ================================================= native vs pure Java port

    /**
     * The same charts through swisseph.SwissEph. It is an older Swiss Ephemeris, so it is
     * held to a much looser bound - the point is that the pure Java fallback is still in
     * the right place, not that it is exact.
     */
    @Test
    public void test_pure_java_tracks_the_native_library() {
        for (double[] row : REF) {
            int year = (int) row[0];
            ISweObjects n = chart(getSwephExp(), year, PLACIDUS, false);
            ISweObjects j = chart(getSwissEph(), year, PLACIDUS, false);

            for (int i = SY; i <= PL; i++) {
                assertEquals("year " + year + " object " + i,
                        n.longitudes()[i], j.longitudes()[i], DELTA_JAVA);
            }
            assertEquals("lagna " + year, n.longitudes()[LG], j.longitudes()[LG], DELTA_JAVA);
        }
    }

    /**
     * Signs and houses are what a chart is actually read from, and those must agree even
     * though the raw longitudes do not.
     */
    @Test
    public void test_pure_java_agrees_on_signs_and_houses() {
        for (double[] row : REF) {
            int year = (int) row[0];
            ISweObjects n = chart(getSwephExp(), year, WHOLE_SIGN, false);
            ISweObjects j = chart(getSwissEph(), year, WHOLE_SIGN, false);

            for (int i = LG; i <= PL; i++) {
                if (i == LG) continue;   // the lagna itself can sit next to a boundary
                assertEquals("year " + year + " sign of " + i, n.signs()[i], j.signs()[i]);
                assertEquals("year " + year + " house of " + i, n.houses()[i], j.houses()[i]);
            }
        }
    }
}
