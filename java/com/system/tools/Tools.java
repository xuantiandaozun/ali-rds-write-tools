package com.system.tools;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import com.aliyun.auth.credentials.Credential;
import com.aliyun.auth.credentials.provider.StaticCredentialProvider;
import com.aliyun.sdk.service.rds20140815.AsyncClient;
import com.aliyun.sdk.service.rds20140815.models.DescribeDBInstancesRequest;
import com.aliyun.sdk.service.rds20140815.models.DescribeDBInstancesResponse;
import com.aliyun.sdk.service.rds20140815.models.DescribeDBInstancesResponseBody.DBInstance;
import com.aliyun.sdk.service.rds20140815.models.DescribeRegionsRequest;
import com.aliyun.sdk.service.rds20140815.models.DescribeRegionsResponse;
import com.aliyun.sdk.service.rds20140815.models.ModifySecurityIpsRequest;
import com.aliyun.sdk.service.rds20140815.models.ModifySecurityIpsResponse;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import darabonba.core.client.ClientOverrideConfiguration;

public class Tools
{

        /**
         * 查询阿里云可用区域
         * 
         * @return
         */
        public static String getRegion(String key, String secret)
        {
                StaticCredentialProvider provider = StaticCredentialProvider
                                .create(Credential.builder().accessKeyId(key).accessKeySecret(secret).build());
                AsyncClient client = AsyncClient.builder().region("cn-chengdu").credentialsProvider(provider)
                                .overrideConfiguration(ClientOverrideConfiguration.create()
                                                .setEndpointOverride("rds.aliyuncs.com"))
                                .build();
                DescribeRegionsRequest describeRegionsRequest = DescribeRegionsRequest.builder().acceptLanguage("zh-CN")
                                .build();

                CompletableFuture<DescribeRegionsResponse> response = client.describeRegions(describeRegionsRequest);
                try
                {
                        DescribeRegionsResponse resp = response.get();
                        if (resp.getStatusCode() == 200)
                        {
                                String res = JSONUtil.toJsonStr(resp.getBody());
                                client.close();
                                return res;
                        }

                }
                catch (Exception e)
                {
                        e.printStackTrace();
                }
                return null;

        }

        /**
         * 查询RDS列表
         * 
         * @param key
         * @param secret
         * @return
         */
        public static String getRdsList(String key, String secret, String reginJson)
        {
                StaticCredentialProvider provider = StaticCredentialProvider
                                .create(Credential.builder().accessKeyId(key).accessKeySecret(secret).build());
                AsyncClient client = AsyncClient.builder().region("cn-chengdu").credentialsProvider(provider)
                                .overrideConfiguration(ClientOverrideConfiguration.create()
                                                .setEndpointOverride("rds.aliyuncs.com"))
                                .build();

                List<JSONObject> array = JSONUtil.toList(reginJson, JSONObject.class);
                List<DBInstance> dbInstance = new ArrayList<>();
                for (JSONObject object : array)
                {
                        String regionId = object.getStr("regionId");

                        DescribeDBInstancesRequest describeDBInstancesRequest = DescribeDBInstancesRequest.builder()
                                        .pageSize(100).regionId(regionId).build();
                        CompletableFuture<DescribeDBInstancesResponse> response = client
                                        .describeDBInstances(describeDBInstancesRequest);
                        try
                        {
                                DescribeDBInstancesResponse resp = response.get();
                                List<DBInstance> mList = resp.getBody().getItems().getDBInstance();
                                dbInstance.addAll(mList);
                        }
                        catch (InterruptedException e)
                        {
                                e.printStackTrace();
                        }
                        catch (ExecutionException e)
                        {
                                e.printStackTrace();
                        }

                }
                client.close();

                return JSONUtil.toJsonStr(dbInstance);
        }

        /**
         * 修改数据库白名单
         * 
         * @param item
         * @return
         */
        public static String updateW(String rdisItem, String ip)
        {
                JSONObject obj = JSONUtil.parseObj(rdisItem);

                String dbId = obj.getStr("DBInstanceId");
                String regId = obj.getStr("regionId");
                String ask = obj.getStr("access_key");
                String secret = obj.getStr("secret");
                String regionEndpoint = obj.getStr("regionEndpoint");

                StaticCredentialProvider provider = StaticCredentialProvider
                                .create(Credential.builder().accessKeyId(ask).accessKeySecret(secret).build());
                AsyncClient client = AsyncClient.builder().region(regId).credentialsProvider(provider)
                                .overrideConfiguration(ClientOverrideConfiguration.create()
                                                .setEndpointOverride(regionEndpoint))
                                .build();

                ModifySecurityIpsRequest modifySecurityIpsRequest = ModifySecurityIpsRequest.builder()
                                .DBInstanceId(dbId).securityIps(ip).DBInstanceIPArrayName("aadev").modifyMode("Cover")
                                .build();

                CompletableFuture<ModifySecurityIpsResponse> response = client
                                .modifySecurityIps(modifySecurityIpsRequest);

                ModifySecurityIpsResponse resp = null;
                try
                {
                        resp = response.get();
                }
                catch (InterruptedException e)
                {

                        e.printStackTrace();
                }
                catch (ExecutionException e)
                {
                        e.printStackTrace();
                }
                client.close();

                return JSONUtil.toJsonStr(resp);
        }

}