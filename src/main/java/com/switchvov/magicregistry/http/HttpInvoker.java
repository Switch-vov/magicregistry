package com.switchvov.magicregistry.http;

import com.switchvov.magicregistry.utils.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Interface for http invoke.
 *
 * @author switch
 * @since 2024/3/20
 */
public interface HttpInvoker {
    Logger log = LoggerFactory.getLogger(HttpInvoker.class);

    HttpInvoker Default = new OkHttpInvoker(500);

    String post(String requestString, String url);

    String get(String url);

    static <T> T httpGet(String url, Class<T> clazz) {
        log.debug(" =====> httpGet:{}", url);
        String respJson = Default.get(url);
        log.debug(" =====> response:{}", respJson);
        return JsonUtils.fromJson(respJson, clazz);
    }

    static <T> T httpPost(String requestString, String url, Class<T> clazz) {
        log.debug(" =====> httpPost:{}", url);
        String respJson = Default.post(requestString, url);
        log.debug(" =====> response:{}", respJson);
        return JsonUtils.fromJson(respJson, clazz);
    }

}
