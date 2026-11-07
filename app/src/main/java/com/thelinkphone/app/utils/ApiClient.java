package com.thelinkphone.app.utils;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Dns;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static final String BASE_URL = MyConst.API_BASE_URL;
    private static Retrofit retrofit = null;

    public static Retrofit getClient() {

        if (retrofit == null) {
            HttpLoggingInterceptor interceptor=new HttpLoggingInterceptor();
            interceptor.setLevel(HttpLoggingInterceptor.Level.NONE);

            OkHttpClient okHttpClient = new OkHttpClient.Builder()

                    .dns(new Dns() {
                        @Override
                        public List<InetAddress> lookup(String hostname) throws UnknownHostException {
                            List<InetAddress> addresses = Dns.SYSTEM.lookup(hostname);
                            List<InetAddress> ipv4Only = new ArrayList<>();
                            for (InetAddress addr : addresses) {
                                if (addr instanceof Inet4Address) {
                                    ipv4Only.add(addr);
                                }
                            }
                            return ipv4Only.isEmpty() ? addresses : ipv4Only;
                        }
                    })

                    .addInterceptor(interceptor)
                    .addInterceptor(chain -> {
                        okhttp3.Request request = chain.request().newBuilder()
                                .header("Accept", "application/json")
                                .build();
                        return chain.proceed(request);
                    })
                    .connectTimeout(5, java.util.concurrent.TimeUnit.SECONDS)
                    .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                    .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
