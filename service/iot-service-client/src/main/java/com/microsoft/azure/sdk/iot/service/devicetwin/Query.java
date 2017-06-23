/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service.devicetwin;

import com.microsoft.azure.sdk.iot.deps.serializer.ParserUtility;
import com.microsoft.azure.sdk.iot.deps.serializer.QueryRequestParser;
import com.microsoft.azure.sdk.iot.service.IotHubConnectionString;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import com.microsoft.azure.sdk.iot.service.transport.http.HttpMethod;
import com.microsoft.azure.sdk.iot.service.transport.http.HttpResponse;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/*
    Sql style query IotHub for twin, jobs, device jobs or raw data
 */
public class Query
{
    private static final String CONTINUATION_TOKEN_KEY = "x-ms-continuation";
    private static final String ITEM_TYPE_KEY = "x-ms-item-type";
    private static final String PAGE_SIZE_KEY = "x-ms-max-item-count";

    private int pageSize;
    private String query;

    private String requestContinuationToken;
    private String responseContinuationToken;

    private QueryType requestQueryType;
    private QueryType responseQueryType;

    private QueryResponse queryResponse;

    Query(String query, int pageSize, QueryType requestQueryType) throws IllegalArgumentException
    {
        ParserUtility.validateQuery(query);

        if (pageSize <= 0)
        {
            throw new IllegalArgumentException("Page Size cannot be zero or negative");
        }

        if (requestQueryType == QueryType.UNKNOWN)
        {
            throw new IllegalArgumentException("Cannot process a unknown type query");
        }

        this.pageSize = pageSize;
        this.query = query;
        this.requestContinuationToken = null;
        this.responseContinuationToken = null;
        this.requestQueryType = requestQueryType;
        this.responseQueryType = QueryType.UNKNOWN;
        this.queryResponse = null;
    }

    void continueQuery(String continuationToken)
    {
        this.requestContinuationToken = continuationToken;
    }

    void continueQuery(String continuationToken, int pageSize) throws IllegalArgumentException
    {
        if (pageSize <= 0)
        {
            throw new IllegalArgumentException("Page Size cannot be zero or negative");
        }

        this.pageSize = pageSize;
        this.requestContinuationToken = continuationToken;
    }

    QueryResponse sendQueryRequest(IotHubConnectionString iotHubConnectionString,
                                   URL url,
                                   HttpMethod method,
                                   long timeoutInMs) throws IOException, IotHubException
    {
        Map<String, String> queryHeaders = new HashMap<>();

        if (this.requestContinuationToken != null)
        {
            queryHeaders.put(CONTINUATION_TOKEN_KEY, requestContinuationToken);
        }
        queryHeaders.put(PAGE_SIZE_KEY, String.valueOf(pageSize));

        DeviceOperations.setHeaders(queryHeaders);

        QueryRequestParser requestParser = new QueryRequestParser(this.query);

        HttpResponse httpResponse = DeviceOperations.request(iotHubConnectionString, url, method, requestParser.toJson().getBytes(), null, timeoutInMs);

        this.responseContinuationToken = null;
        Map<String, String> headers = httpResponse.getHeaderFields();
        for (Map.Entry<String, String> header : headers.entrySet())
        {
            switch (header.getKey())
            {
                case CONTINUATION_TOKEN_KEY:
                    this.responseContinuationToken = header.getValue();
                    break;
                case ITEM_TYPE_KEY:
                    this.responseQueryType = QueryType.fromString(header.getValue());
                    break;
                default:
                    break;
            }
        }

        if (this.responseQueryType == null || this.responseQueryType == QueryType.UNKNOWN)
        {
            throw new IOException("Query response type is not defined by IotHub");
        }

        if (this.requestQueryType != this.responseQueryType)
        {
            throw new IOException("Query response does not match query request");
        }

        this.queryResponse = new QueryResponse(this.responseQueryType, new String(httpResponse.getBody()));
        return this.queryResponse;
    }

    String getContinuationToken()
    {
        return this.responseContinuationToken;
    }

    boolean hasNext()
    {
        return queryResponse.hasNext();
    }

    Object next()
    {
        return queryResponse.next();
    }
}
