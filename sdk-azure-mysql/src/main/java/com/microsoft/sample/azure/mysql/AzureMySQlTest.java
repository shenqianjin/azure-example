package com.microsoft.sample.azure.mysql;


import com.microsoft.azure.PagedList;
import com.microsoft.azure.credentials.AzureCliCredentials;
import com.microsoft.azure.management.mysql.v2020_01_01.*;
import com.microsoft.azure.management.mysql.v2020_01_01.implementation.FirewallRuleInner;
import com.microsoft.azure.management.mysql.v2020_01_01.implementation.MySQLManager;
import com.microsoft.azure.management.mysql.v2020_01_01.implementation.PerformanceTierPropertiesInner;
import com.microsoft.rest.LogLevel;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class AzureMySQlTest {

    public static void main(String[] args) throws IOException {
        final AtomicInteger count = new AtomicInteger(0);
        /*MySQLManagementClientImpl client = new MySQLManagementClientImpl(AzureCliCredentials.create())
//                .withSubscriptionId("685ba005-af8d-4b04-8f16-a7bf38b2eb5a")
                ;
//        client.subscriptionId();
//        client.checkNameAvailabilitys();
        // list mysql server
        PagedList<ServerInner> serverInners = client.servers().list();
        // client.servers()
        System.out.println(serverInners.size());
        //
//        client.servers().c
//        client.virtualNetworkRules().
        List<DatabaseInner> databaseInners = client.databases().listByServer("qianjinshen", "petstore4qianjin");
        System.out.println(databaseInners.size());
        List<FirewallRuleInner> firewallRuleInners = client.firewallRules().listByServer("qianjinshen", "petstore4qianjin");
        System.out.println(firewallRuleInners.size());*/
        AzureCliCredentials credentials = AzureCliCredentials.create();
        MySQLManager mySQLManager = MySQLManager.configure()
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
                .authenticate(credentials, "685ba005-af8d-4b04-8f16-a7bf38b2eb5a")
//                .withDefaultSubscription()
        ;
        List<String> regions = Arrays.asList("australiacentral", "australiacentral2", "australiaeast", "australiasoutheast", "brazilsouth", "canadacentral", "canadaeast", "centralindia",
                "centralus", "eastasia", "eastus2", "eastus", "francecentral", "francesouth", "germanywestcentral", "japaneast", "japanwest", "koreacentral",
                "koreasouth", "northcentralus", "northeurope", "southafricanorth", "southafricawest", "southcentralus", "southindia", "southeastasia",
                "norwayeast", "switzerlandnorth", "uaenorth", "uksouth", "ukwest", "westcentralus", "westeurope", "westindia", "westus", "westus2",
                "centraluseuap", "eastus2euap");
        performanceTierProperties(mySQLManager, regions);
        //create(mySQLManager);
//        listServers(mySQLManager);
//        updateSSL(mySQLManager);
//        listFirewalls(mySQLManager);
//        createOrUpdateFirewall(mySQLManager);
        //findAndDeleteFirewall(mySQLManager);
    }

    public static void performanceTierProperties(MySQLManager manager, List<String> regions) {
        List<String> resultList = new ArrayList<>();
        for (String region : regions) {
            StringBuilder builder = new StringBuilder();
            builder.append(region).append(" : *** size: ");
            List<PerformanceTierPropertiesInner> propertiesInnerList = manager.locationBasedPerformanceTiers().inner().list(region);
            builder.append(propertiesInnerList.size()).append(" --> ");
            for (PerformanceTierPropertiesInner inner : propertiesInnerList) {
                builder.append(inner.id()).append(",");
            }

            resultList.add(builder.toString());
        }
        for (String result : resultList) {
            System.out.println(result);
        }
    }

    public static void create(MySQLManager manager) {
        Sku sku = new Sku().withName("B_Gen5_1");
        ServerPropertiesForDefaultCreate properties = new ServerPropertiesForDefaultCreate();
        properties.withAdministratorLogin("qianjin")
                .withAdministratorLoginPassword("a222222@")
                .withVersion(ServerVersion.FIVE_FULL_STOP_SEVEN);
        manager.servers().define("qianjin-test-02")
                .withRegion("brazilsouth").withExistingResourceGroup("qianjin123")
                //.withProperties(properties)./*withSku(sku).*/create();
                .withProperties(properties).withSku(sku).create();
    }

    public static void listServers(MySQLManager manager) {
        PagedList<Server> servers = manager.servers().list();
        servers.stream().forEach(e -> System.out.println(e.name() + " " + e.resourceGroupName()));
        System.out.println(servers.size());
    }

    public static void updateSSL(MySQLManager manager) {
//        PagedList<Server> servers = manager.servers().list();
//        servers.stream().forEach(e -> System.out.println(e.name() + " " + e.resourceGroupName()));
//        System.out.println(servers.size());
//        Server server = managerager.servers().getByResourceGroup("qianjinshen", "qianjinshen-mysql-01");
        ServerUpdateParameters parameters = new ServerUpdateParameters();
        parameters.withSslEnforcement(SslEnforcementEnum.ENABLED);
        manager.servers().inner().update("qianjinshen", "qianjinshen-mysql-01", parameters);
        System.out.println(String.format("updated: %s", parameters));
    }

    public static void listFirewalls(MySQLManager manager) {
        List<FirewallRuleInner> servers = manager.firewallRules().inner().listByServer("qianjinshen", "qianjinshen-mysql-01");
        servers.stream().forEach(e -> System.out.println(e.name() + " " + e.id() + "\n"));
        System.out.println(servers.size());
    }

    public static void createOrUpdateFirewall(MySQLManager manager) {
        String ip = "167.220.255.104";
        FirewallRuleInner rule = new FirewallRuleInner();
        rule.withStartIpAddress(ip);
        rule.withEndIpAddress(ip);
        FirewallRuleInner result = manager.firewallRules().inner().createOrUpdate("qianjinshen", "qianjinshen-mysql-01", "allow-" + ip.replaceAll("\\.", "-"), rule);
        System.out.println(result.id() + "  " + result.name());
    }

    public static void findAndDeleteFirewall(MySQLManager manager) {
        // AllowAllWindowsAzureIps
        String ruleName = "allow----------------";
        FirewallRuleInner rule = manager.firewallRules().inner().get("qianjinshen", "qianjinshen-mysql-01", ruleName);
        System.out.println(rule.name() + " " + rule.id() + "\n");
        manager.firewallRules().inner().delete("qianjinshen", "qianjinshen-mysql-01", ruleName);
        System.out.println("done ....");
    }
}
