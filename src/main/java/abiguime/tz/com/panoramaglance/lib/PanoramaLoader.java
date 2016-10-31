package abiguime.tz.com.panoramaglance.lib;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Path;
import android.os.AsyncTask;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.view.animation.Animation;
import android.widget.ImageView;

import com.jakewharton.disklrucache.DiskLruCache;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


/**
 * Created by abiguime on 2016/10/31.
 */

public class PanoramaLoader {

    private final Context ctx;
    static PanoramaLoader loader;

    /**/
    LruCache<String, Bitmap> lruCache;
    DiskLruCache diskcache;

    public static PanoramaLoader getInstance (Context ctx) {
        if (loader ==  null)
            loader = new PanoramaLoader(ctx);
        return loader;
    }

    public PanoramaLoader(Context ctx) {
        this.ctx = ctx;

        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

        int maxSize = maxMemory/8; // 200MB // 1GB
        lruCache = new LruCache<String, Bitmap>(maxSize) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getByteCount();
            }
        };
        try {
            Log.d("xxx", (((PanoramaApp)ctx.getApplicationContext()).getPconfig().getCacheDir().getAbsolutePath()));
            diskcache = DiskLruCache.open(
                    ((PanoramaApp)ctx.getApplicationContext()).getPconfig().getCacheDir(),
                    getAppVersion(ctx),
                    1, // 一个snapshot几个文件
                    100 * 1024 * 1024 // 100MB
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getAppVersion(Context context) {
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return info.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return 1;
    }

    /* 加载图片*/
    public void loadInto (String url, final ImageView imageView) {

        loadImage(url, new PanoramaCallBack() {
            @Override
            public void surprise(Bitmap bmp) {
                imageView.setImageBitmap(bmp);
            }
        });
    }

    /* 加载图片 - 加载实现动画 */
    public void loadIntoWithAnimation (String url, final ImageView imageView, final Animation animation) {
        loadImage(url, new PanoramaCallBack() {
            @Override
            public void surprise(Bitmap bmp) {
                imageView.setImageBitmap(bmp);
            //    imageView.setAnimation(animation);
            }
        });
    }

    /* 加载图片 - 调用回调接口 */
    public void loadImage (final String url, final PanoramaCallBack callBack) {

        /* 在另个子线程运行本操作 */

        (new AsyncTask<String, Void, Bitmap>(){

            @Override
            protected Bitmap doInBackground(String... strings) {

                String link = strings[0];
                Bitmap res = null;
                // 从缓存获取图片
                res = loadFromCache (link);
                if (res != null){
                    //////
                    return res;
                }
                res = loadFromSdKa(link);
                if (res == null) {
                    res = loadFromNet (link);
                }
                return res;
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                if (bitmap == null) {
//                    bitmap = BitmapFactory.decodeResource(ctx.getResources(), R.drawable.def);
                     return;
                }
                lruCache.put(getDigestHexToString(url), bitmap);
                callBack.surprise(bitmap);
            }
        }).execute(url);
    }

    private Bitmap loadFromNet(String url) {

        String name = getDigestHexToString (url);
        Bitmap bmp = null;
        InputStream bis = null;

        try {
            URL link = new URL(url);
            HttpURLConnection urlConnection = (HttpURLConnection) link.openConnection();
            if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                bis =  urlConnection.getInputStream();
//                Utils.CopyStream(bis, bos); // 把文件保存到本--->
                DiskLruCache.Editor editor = diskcache.edit(name);
                if (editor != null) {
                    OutputStream os = editor.newOutputStream(0);
                    Utils.CopyStream(bis, os);
                    editor.commit();
                }
                diskcache.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bis != null) {
                try {
                    bis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        bmp = loadFromSdKa(url);
        return bmp;
    }


    private Bitmap loadFromSdKa(String url) {

        /*DiskLruCache*/
        String name = getDigestHexToString (url);
        try {
            if (diskcache.get(name) == null)
                return null;
            else {
                InputStream is = diskcache.get(name).getInputStream(0);
                return BitmapFactory.decodeStream(is);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Bitmap loadFromCache(String url) {
        //
        String name = getDigestHexToString (url);
        Bitmap bm = lruCache.get(name);
        return bm;
    }

    private String getDigestHexToString(String url) {
        StringBuffer hexString = new StringBuffer();
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(url.getBytes());
            byte[] hash = md.digest();

            for (int i = 0; i < hash.length; i++) {
                if ((0xff & hash[i]) < 0x10) {
                    hexString.append("0"
                            + Integer.toHexString((0xFF & hash[i])));
                } else {
                    hexString.append(Integer.toHexString(0xFF & hash[i]));
                }
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return hexString.toString();
    }

}
