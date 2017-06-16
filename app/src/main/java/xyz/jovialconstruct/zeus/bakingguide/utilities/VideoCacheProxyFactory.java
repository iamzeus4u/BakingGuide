package xyz.jovialconstruct.zeus.bakingguide.utilities;

import android.content.Context;

import com.danikula.videocache.HttpProxyCacheServer;

/**
 * Created by zeus on 02/06/2017.
 */

public class VideoCacheProxyFactory {

    private static HttpProxyCacheServer sharedProxy;

    private VideoCacheProxyFactory() {
    }

    public static HttpProxyCacheServer getProxy(Context context) {
        return sharedProxy == null ? (sharedProxy = newProxy(context)) : sharedProxy;
    }

    private static HttpProxyCacheServer newProxy(Context context) {
        return new HttpProxyCacheServer(context);
    }
}