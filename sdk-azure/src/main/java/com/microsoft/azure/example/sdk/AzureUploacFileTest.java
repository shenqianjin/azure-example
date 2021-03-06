package com.microsoft.azure.example.sdk;


import com.microsoft.azure.credentials.AzureCliCredentials;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.appservice.DeployOptions;
import com.microsoft.azure.management.appservice.DeployType;
import com.microsoft.azure.management.appservice.WebApp;
import com.microsoft.rest.LogLevel;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

public class AzureUploacFileTest {

    public static void main(String[] args) throws IOException {
        final AtomicInteger count = new AtomicInteger(0);
        final Azure azure = Azure.configure()
                .withLogLevel(LogLevel.BODY_AND_HEADERS)
                .withInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        count.addAndGet(1);
                        final Request request = chain.request();
                        final Response response = chain.proceed(request);
                        return chain.proceed(chain.request());
                    }
                })
                .authenticate(AzureCliCredentials.create())
                .withDefaultSubscription();
        oneDeploy(azure);
    }

    private static void oneDeploy(Azure azure) {
        final WebApp webApp = azure.webApps().getByResourceGroup("qianjinshen", "qianjinshen-jboss-mysql-01");
        upload(webApp, "startup.sh");
        upload(webApp, "mysql-datasource-commands.cli");
        upload(webApp, "mysql-module.xml");
        upload(webApp, "mysql-connector-java-8.0.13.jar");
    }

    private static void upload(WebApp webApp, String file) {
        webApp.deploy(DeployType.STATIC,
                new File("C:\\Users\\qianjinshen\\workspace\\migrate-Java-EE-app-to-azure\\.prep\\initial-mysql\\agoncal-application-petstore-ee7\\.scripts\\" + file),
                new DeployOptions().withRestartSite(false).withPath("upload/" + file));
        System.out.println("uploaded -> " + file);


    }
}
