package com.jegarn.https_api;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;
import com.zhy.http.okhttp.https.HttpsUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.security.Key;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.util.Enumeration;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.OkHttpClient;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.sslRequest();
    }

    protected void sslRequest() {

        // wiki: http://blog.csdn.net/lmj623565791/article/details/48129405

        InputStream certificates = getResources().openRawResource(R.raw.server);
        InputStream pkcs12File = getResources().openRawResource(R.raw.client);
        String password = "export111111";
        InputStream bksFile = this.pkcs12ToBks(pkcs12File, password);
        HttpsUtils.SSLParams sslParams = HttpsUtils.getSslSocketFactory(new InputStream[]{certificates}, bksFile, password);

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(10000L, TimeUnit.MILLISECONDS)
                .readTimeout(10000L, TimeUnit.MILLISECONDS)
                .sslSocketFactory(sslParams.sSLSocketFactory, sslParams.trustManager)
                .build();
        OkHttpUtils.initClient(okHttpClient);

        String url = "https://jegarn.com:7080/index.php";
        OkHttpUtils
                .get()
                .url(url)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(String response, int id) {
                        System.out.println(response);
                    }
                });
    }

    protected InputStream pkcs12ToBks(InputStream pkcs12Stream, String pkcs12Password) {
        final char[] password = pkcs12Password.toCharArray();
        try {
            KeyStore pkcs12 = KeyStore.getInstance("PKCS12");
            pkcs12.load(pkcs12Stream, password);
            Enumeration<String> aliases = pkcs12.aliases();
            String alias;
            if (aliases.hasMoreElements()) {
                alias = aliases.nextElement();
            } else {
                throw new Exception("pkcs12 file not contain a alias");
            }
            Certificate certificate = pkcs12.getCertificate(alias);
            final Key key = pkcs12.getKey(alias, password);
            KeyStore bks = KeyStore.getInstance("BKS");
            bks.load(null, password);
            bks.setKeyEntry(alias, key, password, new Certificate[]{certificate});
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            bks.store(out, password);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
