package abiguime.tz.com.panoramaglance.lib;

import android.content.Context;
import android.os.Environment;

import java.io.File;

/**
 * Created by abiguime on 2016/10/31.
 */

public class PanoramaConfig {

    /* - 缓存文件夹
    *   - 缓存文件最大大小
    *   - 默认的加载后的动画
    *   - 图片下载后？要不要秀秀：
    *       - 彩色 -> 黑白 */

    private static String CACHEDIR = "panoramaCache";
    private File cacheDir;

    public PanoramaConfig(Context ctx) {
        // 初始化缓存目录
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
        {
            cacheDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        } else {
            cacheDir = Environment.getDataDirectory();
        }
        if (cacheDir != null) {
          cacheDir = new File(cacheDir.getAbsolutePath()+File.separator+CACHEDIR);
        }
        if (!cacheDir.exists()) {
            cacheDir.mkdirs();
        }
     }


    public static PanoramaConfig getInstance (Context ctx) {
        return new PanoramaConfig(ctx);
    }

    public File getCacheDir() {
        return cacheDir;
    }
}
