package abiguime.tz.com.panoramaglance.lib;

import android.app.Application;

/**
 * Created by abiguime on 2016/10/31.
 */

public class PanoramaApp extends Application {

    PanoramaConfig pconfig;

    @Override
    public void onCreate() {
        super.onCreate();
        config();
    }

    public void config () {
        pconfig = PanoramaConfig.getInstance(this);
    }

    public PanoramaConfig getPconfig() {
        return pconfig;
    }
}
