package mirai.utils;

import org.apache.commons.io.FileUtils;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.mime.HttpMultipartMode;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.net.URIBuilder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static mirai.utils.LogUtils.logError;
import static mirai.utils.LogUtils.logInfo;
import static mirai.utils.LogUtils.logWarn;
import static mirai.utils.MyFileUtils.ERR_STRING;

public class WebUtils {
    private WebUtils() {
    }

    /**
     * 从网站获取字符串，通常是 json 格式数据.
     * <p>
     * 使用指定ua（如果未指定ua）以解除部分网站对java访问的403限制。
     *
     * @param httpUrl      要获取信息的网站
     * @param urlParams    网址附加参数
     * @param headerParams 请求头附加参数
     * @return 反馈的字符串
     */
    public static String getInfoFromUrl(String httpUrl, Map<String, String> urlParams, Map<String, String> headerParams) {
        String result;
        URI uri = null;
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            URIBuilder uriBuilder = new URIBuilder(httpUrl);
            if (urlParams != null) {
                for (Map.Entry<String, String> x : urlParams.entrySet()) {
                    uriBuilder.addParameter(x.getKey(), x.getValue());
                }
            }
            uri = uriBuilder.build();
            HttpGet httpGet = new HttpGet(uri);
            if (headerParams != null) {
                for (Map.Entry<String, String> x : headerParams.entrySet()) {
                    httpGet.setHeader(x.getKey(), x.getValue());
                }
            }
            // 如果没有ua，自动添加可以解除部分网站对java访问的403限制的ua
            if (headerParams == null || !headerParams.containsKey("User-Agent")) {
                httpGet.setHeader("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
            }
            logInfo("Http Get Start\n" + uri);
            result = httpClient.execute(httpGet,
                    response -> EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8));
            logInfo("Http Get Success\n" + uri + "\n" + result);
            return result;
        } catch (IOException | URISyntaxException e) {
            logError("Http Get Failed\n" + uri, e);
            return ERR_STRING;
        }
    }

    /**
     * 上传内容给网站，并获取反馈的信息.
     *
     * @param httpUrl        要上传内容并获取信息的网站
     * @param headerParams   请求头附加参数
     * @param bodyStrParams  请求内容附加字符串参数
     * @param bodyFileParams 请求内容附加文件参数
     * @return 反馈的字符串
     */
    public static String postInfoToUrl(String httpUrl, Map<String, String> headerParams,
                                       Map<String, String> bodyStrParams, Map<String, File> bodyFileParams) {
        String result;
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(httpUrl);
            if (headerParams != null) {
                for (Map.Entry<String, String> x : headerParams.entrySet()) {
                    httpPost.setHeader(x.getKey(), x.getValue());
                }
            }
            // 如果没有ua，自动添加可以解除部分网站对java访问的403限制的ua
            if (headerParams == null || !headerParams.containsKey("User-Agent")) {
                httpPost.setHeader("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
            }
            MultipartEntityBuilder builder = MultipartEntityBuilder.create().setMode(HttpMultipartMode.STRICT);
            if (bodyStrParams != null) {
                for (Map.Entry<String, String> x : bodyStrParams.entrySet()) {
                    builder.addTextBody(x.getKey(), x.getValue());
                }
            }
            if (bodyFileParams != null) {
                for (Map.Entry<String, File> x : bodyFileParams.entrySet()) {
                    builder.addBinaryBody(x.getKey(), x.getValue());
                }
            }
            HttpEntity entity = builder.build();
            httpPost.setEntity(entity);
            logInfo("Http Post Start\n" + httpUrl);
            result = httpClient.execute(httpPost,
                    response -> EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8));
            logInfo("Http Post Success\n" + httpUrl + "\n" + result);
            return result;
        } catch (IOException e) {
            logError("Http Post Failed\n" + httpUrl, e);
            return ERR_STRING;
        }
    }

    /**
     * utf-8 转 unicode.
     *
     * @param utf8Str 要转换的 utf-8 字符串
     * @return 转换完毕的 unicode 字符串
     */
    public static String utf8ToUnicode(String utf8Str) {
        char[] myBuffer = utf8Str.toCharArray();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < utf8Str.length(); i++) {
            Character.UnicodeBlock ub = Character.UnicodeBlock.of(myBuffer[i]);
            if (ub == Character.UnicodeBlock.BASIC_LATIN) {
                //英文及数字等
                sb.append(myBuffer[i]);
            } else if (ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS) {
                //全角半角字符
                int j = (int) myBuffer[i] - 65248;
                sb.append((char) j);
            } else {
                //汉字
                short s = (short) myBuffer[i];
                String hexS = Integer.toHexString(s);
                String unicode = "\\u" + hexS;
                sb.append(unicode.toLowerCase());
            }
        }
        return sb.toString();
    }

    /**
     * unicode 转 utf-8.
     *
     * @param unicodeStr 要转换的 unicode 字符串
     * @return 转换完毕的 utf-8 字符串
     */
    public static String unicodeToUtf8(String unicodeStr) {
        char c;
        int len = unicodeStr.length();
        StringBuilder sb = new StringBuilder();
        int index = 0;
        while (index < len) {
            c = unicodeStr.charAt(index++);
            if (c == '\\') {
                c = unicodeStr.charAt(index++);
                if (c == 'u') {
                    // Read the xxxx
                    int value = 0;
                    for (int i = 0; i < 4; i++) {
                        c = unicodeStr.charAt(index++);
                        if (c >= '0' && c <= '9') {
                            value = (value << 4) + c - '0';
                        } else if (c >= 'a' && c <= 'f') {
                            value = (value << 4) + 10 + c - 'a';
                        } else if (c >= 'A' && c <= 'F') {
                            value = (value << 4) + 10 + c - 'A';
                        } else {
                            throw new IllegalArgumentException("不符合\\uxxxx的格式");
                        }
                    }
                    sb.append((char) value);
                } else {
                    if (c == 't') {
                        c = '\t';
                    } else if (c == 'r') {
                        c = '\r';
                    } else if (c == 'n') {
                        c = '\n';
                    } else if (c == 'f') {
                        c = '\f';
                    }
                    sb.append(c);
                }
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    public static boolean download(String urlString, File downloadFile) {
        try {
            FileUtils.forceMkdirParent(downloadFile);
            FileUtils.copyURLToFile(new URL(urlString), downloadFile);
            return true;
        } catch (FileNotFoundException e) {
            return false;
        } catch (IOException e) {
            //重试一次
            logWarn("下载失败，重试一次，网址：" + urlString, null);
            try {
                FileUtils.forceMkdirParent(downloadFile);
                FileUtils.copyURLToFile(new URL(urlString), downloadFile);
                return true;
            } catch (IOException e1) {
                logError("下载失败，网址：" + urlString, e1);
                return false;
            }
        }
    }

}
