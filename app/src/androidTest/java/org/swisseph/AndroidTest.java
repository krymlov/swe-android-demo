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
import static org.swisseph.app.SweObjectsOptions.LAHIRI_AYANAMSA;
import static java.util.Objects.requireNonNull;
import static java.util.TimeZone.getTimeZone;

import android.content.Context;
import android.content.res.AssetManager;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
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
        File ephe = getExternalFilesDir(EPHE_PATH);
        System.out.println("AndroidTest - setUp()... EPHE files in: " + ephe);
        Context context = getInstrumentation().getTargetContext();
        extractEpheOnce(context, ephe);
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

    protected IKundali newChennaiKundali(ISwissEph swissEph, boolean completeBuild) {
        SweObjects sweObjects = new SweObjects(swissEph, new SweJulianDate(newCalendar(
                getTimeZone("Asia/Calcutta"))), GEO_CHENNAI, LAHIRI_AYANAMSA, completeBuild);
        if (completeBuild) sweObjects.completeBuild();
        return new Kundali(KUNDALI_8_KARAKAS, sweObjects);
    }

    /**
     * Place : Chennai, Tamil Nadu, India<br>
     * Location : 13.09, 80.28 Time Zone : IST (+05:30)
     */
    public static final ISweGeoLocation GEO_CHENNAI = new SweGeoLocation(80 + (16 / 60.), 13 + (5 / 60.), 6.7);


    protected IKundali newKyivKundali(ISwissEph swissEph, boolean completeBuild) {
        SweObjects sweObjects = new SweObjects(swissEph, new SweJulianDate(newCalendar(
                getTimeZone("Europe/Kiev"))), GEO_KYIV, LAHIRI_AYANAMSA, completeBuild);
        if (completeBuild) sweObjects.completeBuild();
        return new Kundali(KUNDALI_8_KARAKAS, sweObjects);
    }

    /**
     * Place : Kyiv, Ukraine<br>
     * Location : 50°27'N, 30°31'E. Time Zone : (+02:00)
     */
    public static final ISweGeoLocation GEO_KYIV = new SweGeoLocation(30 + (31 / 60.), 50 + (26 / 60.), 180);

    protected static SwissEph newSwissEph(String ephePath) {
        SwissEph swissEph = new SwissEph(StringUtils.isBlank(ephePath) ?
                ephePath : getExternalFilesDir(ephePath).getAbsolutePath());
        System.out.println("AndroidTest - created new SwissEph: " + swissEph);
        return swissEph;
    }

    protected static SwissEph newSwissEph() {
        return newSwissEph(EPHE_PATH);
    }

    protected static SwephNative newSwephExp(String ephePath) {
        SwephNative swephNative = new SwephNative(StringUtils.isBlank(ephePath) ?
                ephePath : getExternalFilesDir(ephePath).getAbsolutePath());
        System.out.println("AndroidTest - created new SwephNative: " + swephNative);
        return swephNative;
    }

    protected static SwephNative newSwephExp() {
        return newSwephExp(EPHE_PATH);
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

    static File getExternalFilesDir(String folderName) {
        Context context = getInstrumentation().getTargetContext();
        File filesDir = context.getExternalFilesDir(null);
        if (null == filesDir) throw new NotImplementedException("Shared storage is not available!");
        filesDir = new File(filesDir, folderName);
        if (!filesDir.exists()) filesDir.mkdirs();
        return filesDir;
    }

}
