/*
 * Copyright (C) By the Author
 * Author    Yura Krymlov
 * Created   2020-01
 */

package org.swisseph;

import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static org.apache.commons.io.FilenameUtils.concat;
import static org.apache.commons.io.FilenameUtils.getName;
import static org.jyotisa.app.KundaliOptions.KUNDALI_8_KARAKAS;
import static org.swisseph.api.ISweConstants.EPHE_PATH;
import static org.swisseph.app.SweObjectsOptions.LAHIRI_CITRAPAKSA;
import static java.util.Objects.requireNonNull;
import static java.util.TimeZone.getTimeZone;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.apache.commons.io.IOUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.jyotisa.api.IKundali;
import org.jyotisa.app.Kundali;
import org.swisseph.api.ISweGeoLocation;
import org.swisseph.app.SweGeoLocation;
import org.swisseph.app.SweJulianDate;
import org.swisseph.app.SweObjects;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.TimeZone;

import swisseph.SwissEph;

/**
 * @author Yura Krymlov
 * @version 1.0, 2021-11
 */
@RunWith(AndroidJUnit4.class)
public abstract class AndroidTest {
    protected static final ThreadLocal<ISwissEph> SWISS_EPHS = new ThreadLocal<>();
    protected static final ThreadLocal<ISwissEph> SWEPH_EXPS = new ThreadLocal<>();

    @BeforeClass
    public static void setUp() throws IOException {
        System.out.println("AndroidTest - setUp()... extract EPHE files if any");
        Context context = getInstrumentation().getTargetContext();
        extractEpheOnce(context, getEpheFolder());
    }

    @AfterClass
    public static void tearDown() {
        System.out.println("AndroidTest - tearDown()... close ISwissEph instance if any");
        closeSwissEph();
        closeSwephExp();
    }

    protected Calendar newCalendar(TimeZone timeZone) {
        Calendar calendar = Calendar.getInstance(timeZone);
        calendar.set(Calendar.HOUR_OF_DAY, 12);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar;
    }

    protected IKundali newChennaiKundali(ISwissEph swissEph) {
        return new Kundali(KUNDALI_8_KARAKAS, new SweObjects(swissEph, new SweJulianDate(
                newCalendar(getTimeZone("Asia/Calcutta"))), GEO_CHENNAI, LAHIRI_CITRAPAKSA).completeBuild());
    }

    /**
     * Place : Chennai, Tamil Nadu, India<br>
     * Location : 13.09, 80.28 Time Zone : IST (+05:30)
     */
    public static final ISweGeoLocation GEO_CHENNAI = new SweGeoLocation(80 + (16 / 60.), 13 + (5 / 60.), 6.7);


    protected IKundali newKyivKundali(ISwissEph swissEph) {
        return new Kundali(KUNDALI_8_KARAKAS, new SweObjects(swissEph, new SweJulianDate(
                newCalendar(getTimeZone("Europe/Kiev"))), GEO_KYIV, LAHIRI_CITRAPAKSA).completeBuild());
    }

    /**
     * Place : Kyiv, Ukraine<br>
     * Location : 50°27'N, 30°31'E. Time Zone : (+02:00)
     */
    public static final ISweGeoLocation GEO_KYIV = new SweGeoLocation(30 + (31 / 60.), 50 + (26 / 60.), 180);

    protected static SwissEph newSwissEph() {
        SwissEph swissEph = new SwissEph(getEpheFolder().getAbsolutePath());
        System.out.println("AndroidTest - created new SwissEph: " + swissEph);
        return swissEph;
    }

    protected static SwephNative newSwephExp() {
        SwephNative swephNative = new SwephNative(getEpheFolder().getAbsolutePath());
        System.out.println("AndroidTest - created new SwephNative: " + swephNative);
        return swephNative;
    }

    public static ISwissEph getSwissEph() {
        ISwissEph swissEph = SWISS_EPHS.get();

        if (null == swissEph) {
            swissEph = newSwissEph();
            SWISS_EPHS.set(swissEph);
        }

        System.out.println("AndroidTest - get ISwissEph: " + swissEph);
        return swissEph;
    }

    public static ISwissEph getSwephExp() {
        ISwissEph swissEph = SWEPH_EXPS.get();

        if (null == swissEph) {
            swissEph = newSwephExp();
            SWEPH_EXPS.set(swissEph);
        }

        System.out.println("AndroidTest - get ISwissEph: " + swissEph);
        return swissEph;
    }

    public static void closeSwissEph() {
        try (ISwissEph swissEph = SWISS_EPHS.get()) {
            if (null == swissEph) return;
            System.out.println("AndroidTest - close ISwissEph: " + swissEph);
            SWISS_EPHS.remove();
        }
    }

    public static void closeSwephExp() {
        try (ISwissEph swissEph = SWEPH_EXPS.get()) {
            if (null == swissEph) return;
            System.out.println("AndroidTest - close ISwissEph: " + swissEph);
            SWEPH_EXPS.remove();
        }
    }

    static void extractEpheOnce(Context context, File epheDest) throws IOException {
        if (null == epheDest || requireNonNull(epheDest.listFiles()).length > 0) return;

        final AssetManager assetManager = context.getAssets();
        for (String epheFile : assetManager.list(EPHE_PATH)) {
            File epheFileDest = new File(epheDest, getName(epheFile));
            String epheFilePath = concat(EPHE_PATH, epheFile);

            System.out.println("AndroidTest - extract EPHE file to: " + epheFilePath);

            InputStream in = assetManager.open(epheFilePath);
            OutputStream out = new FileOutputStream(epheFileDest);
            IOUtils.copyLarge(in, out);

            IOUtils.closeQuietly(out);
            IOUtils.closeQuietly(in);
        }
    }

    static File getEpheFolder() {
        Context context = getInstrumentation().getTargetContext();
        File storage = Environment.getExternalStorageDirectory();

        if (null == storage || !storage.exists() || !storage.canWrite()) {
            storage = context.getFilesDir();
        }

        if (null == storage) return null;
        if (!storage.exists()) return null;
        if (!storage.canRead()) return null;
        if (!storage.canWrite()) return null;

        File homeFolder = new File(storage, EPHE_PATH);
        if (!homeFolder.exists()) homeFolder.mkdirs();

        return homeFolder;
    }

}
