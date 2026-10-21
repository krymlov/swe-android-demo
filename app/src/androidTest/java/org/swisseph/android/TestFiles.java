/*
 * Copyright (C) By the Author
 * Author    Yura Krymlov
 * Created   2026-10
 */

package org.swisseph.android;

import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;

import android.content.Context;
import android.content.res.AssetManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import dalvik.system.DexFile;

/**
 * Files the desktop tests of swe-java-api and swe-java-lib take from their working directory
 * ({@code ephe/}, {@code src/test/java}) and the compiled-class scan they do through the code
 * source, answered on a device. The synced copies of those tests (src/androidTest/synced, made
 * by swisseph-2026/tools/android/sync_demo_tests.py) call this class instead.
 * <p>
 * The folders come from the assets of the test APK and are extracted once per process into the
 * files directory of the app under test, so each run sees the files of the APK just installed.
 */
public final class TestFiles {
    private static final String ROOT = "junit";
    private static final Map<String, File> EXTRACTED = new HashMap<>();

    private TestFiles() {
    }

    /** @return absolute path of the extracted {@code ephe} folder (sepl/semo 00..18, fixed stars) */
    public static String ephePath() {
        return dir("ephe").getAbsolutePath();
    }

    /** @return the test sources of one project, as the coverage tests walk them on the desktop */
    public static Path sources(String project) {
        return dir("src/" + project).toPath();
    }

    /** @return an asset folder of the test APK, extracted into the app's files directory */
    public static synchronized File dir(String asset) {
        File done = EXTRACTED.get(asset);
        if (null != done) return done;

        final AssetManager assets = getInstrumentation().getContext().getAssets();
        final Context target = getInstrumentation().getTargetContext();
        final File dest = new File(new File(target.getFilesDir(), ROOT), asset);
        try {
            delete(dest);
            extract(assets, asset, dest);
        } catch (IOException e) {
            throw new UncheckedIOException("cannot extract assets/" + asset + " to " + dest, e);
        }
        EXTRACTED.put(asset, dest);
        return dest;
    }

    /**
     * Names of the classes under a package prefix in the app under test and in the test APK,
     * which is what a walk over the compiled-classes folder finds on the desktop.
     */
    @SuppressWarnings("deprecation")
    public static synchronized List<String> classNames(String prefix) {
        final TreeSet<String> names = new TreeSet<>();
        for (String apk : new String[]{getInstrumentation().getTargetContext().getPackageCodePath(),
                getInstrumentation().getContext().getPackageCodePath()}) {
            try {
                final DexFile dex = new DexFile(apk);
                try {
                    for (Enumeration<String> e = dex.entries(); e.hasMoreElements(); ) {
                        final String name = e.nextElement();
                        if (name.startsWith(prefix)) names.add(name);
                    }
                } finally {
                    dex.close();
                }
            } catch (IOException e) {
                throw new UncheckedIOException("cannot list the classes of " + apk, e);
            }
        }
        return new ArrayList<>(names);
    }

    private static void extract(AssetManager assets, String asset, File dest) throws IOException {
        final String[] children = assets.list(asset);
        if (null == children || children.length == 0) {
            // a file: list() of a file is empty; a missing asset fails in open()
            final File parent = dest.getParentFile();
            if (null != parent && !parent.isDirectory() && !parent.mkdirs()) {
                throw new IOException("cannot create " + parent);
            }
            try (InputStream in = assets.open(asset); OutputStream out = new FileOutputStream(dest)) {
                final byte[] buffer = new byte[64 * 1024];
                for (int read; (read = in.read(buffer)) > 0; ) out.write(buffer, 0, read);
            }
            return;
        }
        if (!dest.isDirectory() && !dest.mkdirs()) throw new IOException("cannot create " + dest);
        for (String child : children) extract(assets, asset + '/' + child, new File(dest, child));
    }

    private static void delete(File file) throws IOException {
        final File[] children = file.listFiles();
        if (null != children) for (File child : children) delete(child);
        if (file.exists() && !file.delete()) throw new IOException("cannot delete " + file);
    }
}
