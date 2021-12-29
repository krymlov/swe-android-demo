package org.swisseph;

import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static org.apache.commons.io.FilenameUtils.concat;
import static org.apache.commons.io.FilenameUtils.getName;
import static java.util.Objects.requireNonNull;
import static swisseph.SweMini.swe_mini;

import android.Manifest;
import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.GrantPermissionRule;

import org.apache.commons.io.IOUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class InstrSweMiniTest {
    public static final String EPHE = "ephe";

    @Rule
    public GrantPermissionRule mRuntimePermissionRule = GrantPermissionRule
            .grant(Manifest.permission.WRITE_EXTERNAL_STORAGE);

    @Test
    public void test_swe_mini() throws IOException {
        Context context = getInstrumentation().getTargetContext();
        final File epheFolder = getAppHomeFolder(context);
        extractEphe(context, epheFolder);

        ISwissEph sweph = new SwephNative(epheFolder.getAbsolutePath());
        swe_mini(sweph, 1, 1, 2022);
        sweph.swe_close();
    }

    private void extractEphe(Context context, File epheDest) throws IOException {
        if (null == epheDest || requireNonNull(epheDest.listFiles()).length > 0) return;

        final AssetManager assetManager = context.getAssets();
        for (String epheFile : assetManager.list(EPHE)) {
            InputStream in = assetManager.open(concat(EPHE, epheFile));
            File epheFileDest = new File(epheDest, getName(epheFile));
            OutputStream out = new FileOutputStream(epheFileDest);
            IOUtils.copyLarge(in, out);
            IOUtils.closeQuietly(out);
            IOUtils.closeQuietly(in);
        }
    }

    public File getAppHomeFolder(Context context) {
        File storage = Environment.getExternalStorageDirectory();

        if (null == storage || !storage.exists() || !storage.canWrite()) {
            storage = context.getFilesDir();
        }

        if (null == storage) return null;
        if (!storage.exists()) return null;
        if (!storage.canRead()) return null;
        if (!storage.canWrite()) return null;

        File homeFolder = new File(storage, EPHE);
        if (!homeFolder.exists()) homeFolder.mkdirs();

        return homeFolder;
    }
}