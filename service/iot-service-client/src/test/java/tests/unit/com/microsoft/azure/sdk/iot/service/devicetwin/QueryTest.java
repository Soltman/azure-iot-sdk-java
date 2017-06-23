/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.unit.com.microsoft.azure.sdk.iot.service.devicetwin;

import com.microsoft.azure.sdk.iot.service.IotHubConnectionString;
import com.microsoft.azure.sdk.iot.service.auth.IotHubServiceSasToken;
import com.microsoft.azure.sdk.iot.service.devicetwin.Query;
import com.microsoft.azure.sdk.iot.service.devicetwin.QueryResponse;
import com.microsoft.azure.sdk.iot.service.devicetwin.QueryType;
import com.microsoft.azure.sdk.iot.service.transport.http.HttpMethod;
import com.microsoft.azure.sdk.iot.service.transport.http.HttpRequest;
import com.microsoft.azure.sdk.iot.service.transport.http.HttpResponse;
import mockit.*;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

import static org.junit.Assert.*;

/*
    Unit Tests for Query
    Coverage - method : 100%, line : 100%
 */
public class QueryTest
{
    private static final QueryType DEFAULT_QUERY_TYPE = QueryType.TWIN;
    private static final int DEFAULT_PAGE_SIZE = 100;
    private static final String DEFAULT_QUERY = "select * from devices";

    @Test
    public void constructorSucceeds() throws IllegalArgumentException
    {
        //arrange
        final String sqlQuery = DEFAULT_QUERY;

        //act
        Query testQuery = Deencapsulation.newInstance(Query.class, sqlQuery, DEFAULT_PAGE_SIZE, DEFAULT_QUERY_TYPE);

        //assert
        assertNotNull(Deencapsulation.getField(testQuery, "pageSize"));
        assertEquals(DEFAULT_PAGE_SIZE, (int) Deencapsulation.getField(testQuery, "pageSize"));

        assertEquals(sqlQuery, Deencapsulation.getField(testQuery, "query"));

        assertEquals(DEFAULT_QUERY_TYPE, Deencapsulation.getField(testQuery, "requestQueryType"));
        assertEquals(QueryType.UNKNOWN, Deencapsulation.getField(testQuery, "responseQueryType"));

        assertNull(Deencapsulation.getField(testQuery, "requestContinuationToken"));
        assertNull(Deencapsulation.getField(testQuery, "responseContinuationToken"));

        assertNull(Deencapsulation.getField(testQuery, "queryResponse"));
    }

    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsOnNullQuery() throws IllegalArgumentException
    {
        //arrange
        final String sqlQuery = null;

        //act
        Query testQuery = Deencapsulation.newInstance(Query.class, String.class, DEFAULT_PAGE_SIZE, DEFAULT_QUERY_TYPE);
    }

    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsOnEmptyQuery() throws IllegalArgumentException
    {
        //arrange
        final String sqlQuery = "";

        //act
        Query testQuery = Deencapsulation.newInstance(Query.class, sqlQuery, DEFAULT_PAGE_SIZE, DEFAULT_QUERY_TYPE);
    }

    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsOnInvalidQuery() throws IllegalArgumentException
    {
        //arrange
        final String sqlQuery = "invalid query";

        //act
        Query testQuery = Deencapsulation.newInstance(Query.class, sqlQuery, DEFAULT_PAGE_SIZE, DEFAULT_QUERY_TYPE);
    }

    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsNegativePageSize() throws IllegalArgumentException
    {
        //arrange
        final String sqlQuery = DEFAULT_QUERY;

        //act
        Query testQuery = Deencapsulation.newInstance(Query.class, sqlQuery, -1, DEFAULT_QUERY_TYPE);

    }

    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsZeroPageSize() throws IllegalArgumentException
    {
        //arrange
        final String sqlQuery = DEFAULT_QUERY;

        //act
        Query testQuery = Deencapsulation.newInstance(Query.class, sqlQuery, 0, DEFAULT_QUERY_TYPE);

    }

    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsOnUnknownQueryType() throws IllegalArgumentException
    {
        //arrange
        final String sqlQuery = DEFAULT_QUERY;

        //act
        Query testQuery = Deencapsulation.newInstance(Query.class, sqlQuery, DEFAULT_PAGE_SIZE, QueryType.UNKNOWN);
    }

    @Test
    public void continueQuerySetsToken() throws IllegalArgumentException
    {
        //arrange
        final String testToken = UUID.randomUUID().toString();
        Query testQuery = Deencapsulation.newInstance(Query.class, DEFAULT_QUERY, DEFAULT_PAGE_SIZE, DEFAULT_QUERY_TYPE);

        //act
        Deencapsulation.invoke(testQuery, "continueQuery", testToken);

        //assert
        assertEquals(testToken, Deencapsulation.getField(testQuery, "requestContinuationToken"));
    }

    @Test
    public void continueQuerySetsPageSize() throws IllegalArgumentException
    {
        //arrange
        final String testToken = UUID.randomUUID().toString();
        final int testPageSize = 10;

        Query testQuery = Deencapsulation.newInstance(Query.class, DEFAULT_QUERY, DEFAULT_PAGE_SIZE, DEFAULT_QUERY_TYPE);

        //act
        Deencapsulation.invoke(testQuery, "continueQuery", testToken, testPageSize);

        //assert
        assertEquals(testToken, Deencapsulation.getField(testQuery, "requestContinuationToken"));
        assertEquals(testPageSize, (int) Deencapsulation.getField(testQuery, "pageSize"));
    }

    @Test (expected = IllegalArgumentException.class)
    public void continueQueryThrowsOnZeroPageSize() throws IllegalArgumentException
    {
        //arrange
        final String testToken = UUID.randomUUID().toString();
        final int testPageSize = 0;

        Query testQuery = Deencapsulation.newInstance(Query.class, DEFAULT_QUERY, DEFAULT_PAGE_SIZE, DEFAULT_QUERY_TYPE);

        //act
        Deencapsulation.invoke(testQuery, "continueQuery", testToken, testPageSize);
    }

    @Test (expected = IllegalArgumentException.class)
    public void continueQueryThrowsOnNegativePageSize() throws IllegalArgumentException
    {
        //arrange
        final String testToken = UUID.randomUUID().toString();
        final int testPageSize = -10;

        Query testQuery = Deencapsulation.newInstance(Query.class, DEFAULT_QUERY, DEFAULT_PAGE_SIZE, DEFAULT_QUERY_TYPE);

        //act
        Deencapsulation.invoke(testQuery, "continueQuery", testToken, testPageSize);
    }

    @Test
    public void sendQueryRequestSucceeds(@Mocked IotHubConnectionString mockIotHubConnectionString,
                                         @Mocked URL mockUrl,
                                         @Mocked HttpResponse mockHttpResponse,
                                         @Mocked HttpRequest mockHttpRequest,
                                         @Mocked QueryResponse mockedQueryResponse,
                                         @Mocked IotHubServiceSasToken mockedSasToken,
                                         @Mocked HttpMethod mockHttpMethod) throws IllegalArgumentException, IOException
    {
        //arrange
        final String testToken = UUID.randomUUID().toString();
        final Map<String, String> testHeaderResponseMap = new HashMap<>();
        testHeaderResponseMap.put("x-ms-continuation", testToken);
        testHeaderResponseMap.put("x-ms-item-type", DEFAULT_QUERY_TYPE.getValue());

        Query testQuery = Deencapsulation.newInstance(Query.class, DEFAULT_QUERY, DEFAULT_PAGE_SIZE, DEFAULT_QUERY_TYPE);

        new NonStrictExpectations()
        {
            {
                mockHttpResponse.getHeaderFields();
                result = testHeaderResponseMap;
            }
        };

        //act
        Deencapsulation.invoke(testQuery, "sendQueryRequest",  mockIotHubConnectionString, mockUrl, mockHttpMethod, (long)0);

        //assert
        new Verifications()
        {
            {
                new HttpRequest(mockUrl, mockHttpMethod, (byte[]) any);
                times = 1;
                mockHttpRequest.setHeaderField("x-ms-max-item-count", String.valueOf(DEFAULT_PAGE_SIZE));
                times = 1;
                mockHttpRequest.setHeaderField("x-ms-continuation", anyString);
                times = 0;
                mockHttpRequest.setHeaderField(anyString, anyString);
                minTimes = 5;
            }
        };

        assertEquals(testToken, Deencapsulation.getField(testQuery, "responseContinuationToken"));
        assertEquals(DEFAULT_QUERY_TYPE, Deencapsulation.getField(testQuery, "responseQueryType"));
    }

    @Test
    public void sendQueryRequestSetsHeadersIfFound(@Mocked IotHubConnectionString mockIotHubConnectionString,
                                                   @Mocked URL mockUrl,
                                                   @Mocked HttpResponse mockHttpResponse,
                                                   @Mocked HttpRequest mockHttpRequest,
                                                   @Mocked QueryResponse mockedQueryResponse,
                                                   @Mocked IotHubServiceSasToken mockedSasToken,
                                                   @Mocked HttpMethod mockHttpMethod) throws IllegalArgumentException, IOException
    {
        //arrange
        final String testToken = UUID.randomUUID().toString();
        final int newPageSize = 5;
        final Map<String, String> testHeaderResponseMap = new HashMap<>();
        testHeaderResponseMap.put("x-ms-continuation", testToken);
        testHeaderResponseMap.put("x-ms-item-type", DEFAULT_QUERY_TYPE.getValue());

        Query testQuery = Deencapsulation.newInstance(Query.class, DEFAULT_QUERY, DEFAULT_PAGE_SIZE, DEFAULT_QUERY_TYPE);

        new NonStrictExpectations()
        {
            {
                mockHttpResponse.getHeaderFields();
                result = testHeaderResponseMap;
            }
        };

        //act
        Deencapsulation.invoke(testQuery, "continueQuery", testToken, newPageSize);
        Deencapsulation.invoke(testQuery, "sendQueryRequest",  mockIotHubConnectionString, mockUrl, mockHttpMethod, (long)0);

        //assert
        new Verifications()
        {
            {
                new HttpRequest(mockUrl, mockHttpMethod, (byte[]) any);
                times = 1;
                mockHttpRequest.setHeaderField("x-ms-max-item-count", String.valueOf(newPageSize));
                times = 1;
                mockHttpRequest.setHeaderField("x-ms-continuation", testToken);
                times = 1;
                mockHttpRequest.setHeaderField(anyString, anyString);
                minTimes = 5;
            }
        };

        assertEquals(testToken, Deencapsulation.getField(testQuery, "responseContinuationToken"));
        assertEquals(DEFAULT_QUERY_TYPE, Deencapsulation.getField(testQuery, "responseQueryType"));
    }

    @Test
    public void sendQueryRequestDoesNotSetNullHeaders(@Mocked IotHubConnectionString mockIotHubConnectionString,
                                                      @Mocked URL mockUrl,
                                                      @Mocked HttpResponse mockHttpResponse,
                                                      @Mocked HttpRequest mockHttpRequest,
                                                      @Mocked QueryResponse mockedQueryResponse,
                                                      @Mocked IotHubServiceSasToken mockedSasToken,
                                                      @Mocked HttpMethod mockHttpMethod) throws IllegalArgumentException, IOException
    {

        //arrange
        final String testToken = UUID.randomUUID().toString();
        final Map<String, String> testHeaderResponseMap = new HashMap<>();
        testHeaderResponseMap.put("x-ms-continuation", testToken);
        testHeaderResponseMap.put("x-ms-item-type", DEFAULT_QUERY_TYPE.getValue());

        Query testQuery = Deencapsulation.newInstance(Query.class, DEFAULT_QUERY, DEFAULT_PAGE_SIZE, DEFAULT_QUERY_TYPE);

        new NonStrictExpectations()
        {
            {
                mockHttpResponse.getHeaderFields();
                result = testHeaderResponseMap;
            }
        };

        //act
        Deencapsulation.invoke(testQuery, "continueQuery", String.class);
        Deencapsulation.invoke(testQuery, "sendQueryRequest",  mockIotHubConnectionString, mockUrl, mockHttpMethod, (long)0);

        //assert
        new Verifications()
        {
            {
                new HttpRequest(mockUrl, mockHttpMethod, (byte[]) any);
                times = 1;
                mockHttpRequest.setHeaderField("x-ms-max-item-count", String.valueOf(DEFAULT_PAGE_SIZE));
                times = 1;
                mockHttpRequest.setHeaderField("x-ms-continuation", anyString);
                times = 0;
                mockHttpRequest.setHeaderField(anyString, anyString);
                minTimes = 5;
            }
        };

        assertEquals(testToken, Deencapsulation.getField(testQuery, "responseContinuationToken"));
        assertEquals(DEFAULT_QUERY_TYPE, Deencapsulation.getField(testQuery, "responseQueryType"));
    }

    @Test
    public void sendQueryRequestSetsResContinuationTokenOnlyIfFound(@Mocked IotHubConnectionString mockIotHubConnectionString,
                                                                    @Mocked URL mockUrl,
                                                                    @Mocked HttpResponse mockHttpResponse,
                                                                    @Mocked HttpRequest mockHttpRequest,
                                                                    @Mocked QueryResponse mockedQueryResponse,
                                                                    @Mocked IotHubServiceSasToken mockedSasToken,
                                                                    @Mocked HttpMethod mockHttpMethod) throws IllegalArgumentException, IOException
    {

        //arrange
        final String testResponseToken = UUID.randomUUID().toString();
        final Map<String, String> testHeaderResponseMap = new HashMap<>();
        testHeaderResponseMap.put("x-ms-continuation", testResponseToken);
        testHeaderResponseMap.put("x-ms-item-type", DEFAULT_QUERY_TYPE.getValue());

        Query testQuery = Deencapsulation.newInstance(Query.class, DEFAULT_QUERY, DEFAULT_PAGE_SIZE, DEFAULT_QUERY_TYPE);

        new NonStrictExpectations()
        {
            {
                mockHttpResponse.getHeaderFields();
                result = testHeaderResponseMap;
            }
        };

        //act
        Deencapsulation.invoke(testQuery, "sendQueryRequest", mockIotHubConnectionString, mockUrl, mockHttpMethod, (long) 0);

        //assert
        assertEquals(testResponseToken, Deencapsulation.getField(testQuery, "responseContinuationToken"));
    }

    @Test
    public void sendQueryRequestDoesNotSetResContinuationTokenIfNotFound(@Mocked IotHubConnectionString mockIotHubConnectionString,
                                                                         @Mocked URL mockUrl,
                                                                         @Mocked HttpResponse mockHttpResponse,
                                                                         @Mocked HttpRequest mockHttpRequest,
                                                                         @Mocked QueryResponse mockedQueryResponse,
                                                                         @Mocked IotHubServiceSasToken mockedSasToken,
                                                                         @Mocked HttpMethod mockHttpMethod) throws IllegalArgumentException, IOException
    {
        //arrange
        final String testResponseToken = UUID.randomUUID().toString();
        final Map<String, String> testHeaderResponseMap = new HashMap<>();
        testHeaderResponseMap.put("x-ms-item-type", DEFAULT_QUERY_TYPE.getValue());

        Query testQuery = Deencapsulation.newInstance(Query.class, DEFAULT_QUERY, DEFAULT_PAGE_SIZE, DEFAULT_QUERY_TYPE);

        new NonStrictExpectations()
        {
            {
                mockHttpResponse.getHeaderFields();
                result = testHeaderResponseMap;
            }
        };

        //act
        Deencapsulation.invoke(testQuery, "sendQueryRequest", mockIotHubConnectionString, mockUrl, mockHttpMethod, (long) 0);

        //assert
        assertNull(Deencapsulation.getField(testQuery, "responseContinuationToken"));
    }

    @Test
    public void sendQueryRequestSetsItemOnlyIfFound(@Mocked IotHubConnectionString mockIotHubConnectionString,
                                                    @Mocked URL mockUrl,
                                                    @Mocked HttpResponse mockHttpResponse,
                                                    @Mocked HttpRequest mockHttpRequest,
                                                    @Mocked QueryResponse mockedQueryResponse,
                                                    @Mocked IotHubServiceSasToken mockedSasToken,
                                                    @Mocked HttpMethod mockHttpMethod) throws IllegalArgumentException, IOException
    {
        //arrange
        final Map<String, String> testHeaderResponseMap = new HashMap<>();
        testHeaderResponseMap.put("x-ms-item-type", DEFAULT_QUERY_TYPE.getValue());

        Query testQuery = Deencapsulation.newInstance(Query.class, DEFAULT_QUERY, DEFAULT_PAGE_SIZE, DEFAULT_QUERY_TYPE);

        new NonStrictExpectations()
        {
            {
                mockHttpResponse.getHeaderFields();
                result = testHeaderResponseMap;
            }
        };

        //act
        Deencapsulation.invoke(testQuery, "sendQueryRequest", mockIotHubConnectionString, mockUrl, mockHttpMethod, (long) 0);

        //assert
        assertNull(Deencapsulation.getField(testQuery, "responseContinuationToken"));
        assertEquals(DEFAULT_QUERY_TYPE, Deencapsulation.getField(testQuery, "responseQueryType"));
    }

    @Test (expected = IOException.class)
    public void sendQueryRequestThrowsIfItemNotFound(@Mocked IotHubConnectionString mockIotHubConnectionString,
                                                         @Mocked URL mockUrl,
                                                         @Mocked HttpResponse mockHttpResponse,
                                                         @Mocked HttpRequest mockHttpRequest,
                                                         @Mocked QueryResponse mockedQueryResponse,
                                                         @Mocked IotHubServiceSasToken mockedSasToken,
                                                         @Mocked HttpMethod mockHttpMethod) throws IllegalArgumentException, IOException
    {
        //arrange
        final Map<String, String> testHeaderResponseMap = new HashMap<>();

        Query testQuery = Deencapsulation.newInstance(Query.class, DEFAULT_QUERY, DEFAULT_PAGE_SIZE, DEFAULT_QUERY_TYPE);

        new NonStrictExpectations()
        {
            {
                mockHttpResponse.getHeaderFields();
                result = testHeaderResponseMap;
            }
        };

        //act
        Deencapsulation.invoke(testQuery, "sendQueryRequest", mockIotHubConnectionString, mockUrl, mockHttpMethod, (long) 0);
    }

    @Test (expected = IOException.class)
    public void sendQueryRequestThrowsIfTypeUnknown(@Mocked IotHubConnectionString mockIotHubConnectionString,
                                                    @Mocked URL mockUrl,
                                                    @Mocked HttpResponse mockHttpResponse,
                                                    @Mocked HttpRequest mockHttpRequest,
                                                    @Mocked QueryResponse mockedQueryResponse,
                                                    @Mocked IotHubServiceSasToken mockedSasToken,
                                                    @Mocked HttpMethod mockHttpMethod) throws IllegalArgumentException, IOException
    {
        //arrange
        final Map<String, String> testHeaderResponseMap = new HashMap<>();
        testHeaderResponseMap.put("x-ms-item-type", QueryType.UNKNOWN.getValue());

        Query testQuery = Deencapsulation.newInstance(Query.class, DEFAULT_QUERY, DEFAULT_PAGE_SIZE, DEFAULT_QUERY_TYPE);

        new NonStrictExpectations()
        {
            {
                mockHttpResponse.getHeaderFields();
                result = testHeaderResponseMap;
            }
        };

        //act
        Deencapsulation.invoke(testQuery, "sendQueryRequest", mockIotHubConnectionString, mockUrl, mockHttpMethod, (long) 0);
    }

    @Test (expected = IOException.class)
    public void sendQueryRequestThrowsIfRequestAndResponseTypeDoesNotMatch(@Mocked IotHubConnectionString mockIotHubConnectionString,
                                                                           @Mocked URL mockUrl,
                                                                           @Mocked HttpResponse mockHttpResponse,
                                                                           @Mocked HttpRequest mockHttpRequest,
                                                                           @Mocked QueryResponse mockedQueryResponse,
                                                                           @Mocked IotHubServiceSasToken mockedSasToken,
                                                                           @Mocked HttpMethod mockHttpMethod) throws IllegalArgumentException, IOException
    {
        //arrange
        final Map<String, String> testHeaderResponseMap = new HashMap<>();
        testHeaderResponseMap.put("x-ms-item-type", QueryType.JOB_RESPONSE.getValue());

        Query testQuery = Deencapsulation.newInstance(Query.class, DEFAULT_QUERY, DEFAULT_PAGE_SIZE, DEFAULT_QUERY_TYPE);

        new NonStrictExpectations()
        {
            {
                mockHttpResponse.getHeaderFields();
                result = testHeaderResponseMap;
            }
        };

        //act
        Deencapsulation.invoke(testQuery, "sendQueryRequest", mockIotHubConnectionString, mockUrl, mockHttpMethod, (long) 0);
    }

    @Test
    public void getTokenGets(@Mocked IotHubConnectionString mockIotHubConnectionString,
                             @Mocked URL mockUrl,
                             @Mocked HttpResponse mockHttpResponse,
                             @Mocked HttpRequest mockHttpRequest,
                             @Mocked QueryResponse mockedQueryResponse,
                             @Mocked IotHubServiceSasToken mockedSasToken,
                             @Mocked HttpMethod mockHttpMethod) throws IllegalArgumentException, IOException
    {
        //arrange
        final String testResponseToken = UUID.randomUUID().toString();
        final Map<String, String> testHeaderResponseMap = new HashMap<>();
        testHeaderResponseMap.put("x-ms-continuation", testResponseToken);
        testHeaderResponseMap.put("x-ms-item-type", DEFAULT_QUERY_TYPE.getValue());

        Query testQuery = Deencapsulation.newInstance(Query.class, DEFAULT_QUERY, DEFAULT_PAGE_SIZE, DEFAULT_QUERY_TYPE);

        new NonStrictExpectations()
        {
            {
                mockHttpResponse.getHeaderFields();
                result = testHeaderResponseMap;
            }
        };

        Deencapsulation.invoke(testQuery, "sendQueryRequest", mockIotHubConnectionString, mockUrl, mockHttpMethod, (long) 0);

        //act
        String actualContinuationToken = Deencapsulation.invoke(testQuery, "getContinuationToken");

        //assert
        assertEquals(actualContinuationToken, testResponseToken);
    }

    @Test
    public void hasNextReturnsTrueIfNextExists(@Mocked IotHubConnectionString mockIotHubConnectionString,
                                               @Mocked URL mockUrl,
                                               @Mocked HttpResponse mockHttpResponse,
                                               @Mocked HttpRequest mockHttpRequest,
                                               @Mocked QueryResponse mockedQueryResponse,
                                               @Mocked IotHubServiceSasToken mockedSasToken,
                                               @Mocked HttpMethod mockHttpMethod) throws IllegalArgumentException, IOException
    {
        //arrange
        final Map<String, String> testHeaderResponseMap = new HashMap<>();

        testHeaderResponseMap.put("x-ms-item-type", DEFAULT_QUERY_TYPE.getValue());

        Query testQuery = Deencapsulation.newInstance(Query.class, DEFAULT_QUERY, DEFAULT_PAGE_SIZE, DEFAULT_QUERY_TYPE);

        new NonStrictExpectations()
        {
            {
                mockHttpResponse.getHeaderFields();
                result = testHeaderResponseMap;
                mockedQueryResponse.hasNext();
                result = true;
            }
        };

        Deencapsulation.invoke(testQuery, "sendQueryRequest", mockIotHubConnectionString, mockUrl, mockHttpMethod, (long) 0);

        //act
        boolean hasNext = Deencapsulation.invoke(testQuery, "hasNext");

        //assert
        assertEquals(true, hasNext);
    }


    @Test
    public void hasNextReturnsFalseIfNextDoesNotExists(@Mocked IotHubConnectionString mockIotHubConnectionString,
                                                       @Mocked URL mockUrl,
                                                       @Mocked HttpResponse mockHttpResponse,
                                                       @Mocked HttpRequest mockHttpRequest,
                                                       @Mocked QueryResponse mockedQueryResponse,
                                                       @Mocked IotHubServiceSasToken mockedSasToken,
                                                       @Mocked HttpMethod mockHttpMethod) throws IllegalArgumentException, IOException
    {
        //arrange
        final Map<String, String> testHeaderResponseMap = new HashMap<>();

        testHeaderResponseMap.put("x-ms-item-type", DEFAULT_QUERY_TYPE.getValue());

        Query testQuery = Deencapsulation.newInstance(Query.class, DEFAULT_QUERY, DEFAULT_PAGE_SIZE, DEFAULT_QUERY_TYPE);

        new NonStrictExpectations()
        {
            {
                mockHttpResponse.getHeaderFields();
                result = testHeaderResponseMap;
                mockedQueryResponse.hasNext();
                result = false;
            }
        };

        Deencapsulation.invoke(testQuery, "sendQueryRequest", mockIotHubConnectionString, mockUrl, mockHttpMethod, (long) 0);

        //act
        boolean hasNext = Deencapsulation.invoke(testQuery, "hasNext");

        //assert
        assertEquals(false, hasNext);
    }

    @Test
    public void nextReturnsIfNextExists(@Mocked IotHubConnectionString mockIotHubConnectionString,
                                        @Mocked URL mockUrl,
                                        @Mocked HttpResponse mockHttpResponse,
                                        @Mocked HttpRequest mockHttpRequest,
                                        @Mocked QueryResponse mockedQueryResponse,
                                        @Mocked IotHubServiceSasToken mockedSasToken,
                                        @Mocked HttpMethod mockHttpMethod) throws IllegalArgumentException, IOException
    {
        //arrange
        final Object mockObject = new Object();
        final Map<String, String> testHeaderResponseMap = new HashMap<>();

        testHeaderResponseMap.put("x-ms-item-type", DEFAULT_QUERY_TYPE.getValue());

        Query testQuery = Deencapsulation.newInstance(Query.class, DEFAULT_QUERY, DEFAULT_PAGE_SIZE, DEFAULT_QUERY_TYPE);

        new NonStrictExpectations()
        {
            {
                mockHttpResponse.getHeaderFields();
                result = testHeaderResponseMap;
                mockedQueryResponse.next();
                result = mockObject;
            }
        };

        Deencapsulation.invoke(testQuery, "sendQueryRequest", mockIotHubConnectionString, mockUrl, mockHttpMethod, (long) 0);

        //act
        Object next = Deencapsulation.invoke(testQuery, "next");

        //assert
        assertEquals(mockObject, next);
    }

    @Test (expected = NoSuchElementException.class)
    public void nextThrowsIfNextDoesNotExists(@Mocked IotHubConnectionString mockIotHubConnectionString,
                                              @Mocked URL mockUrl,
                                              @Mocked HttpResponse mockHttpResponse,
                                              @Mocked HttpRequest mockHttpRequest,
                                              @Mocked QueryResponse mockedQueryResponse,
                                              @Mocked IotHubServiceSasToken mockedSasToken,
                                              @Mocked HttpMethod mockHttpMethod) throws IllegalArgumentException, IOException
    {
        //arrange
        final Object mockObject = new Object();
        final Map<String, String> testHeaderResponseMap = new HashMap<>();

        testHeaderResponseMap.put("x-ms-item-type", DEFAULT_QUERY_TYPE.getValue());

        Query testQuery = Deencapsulation.newInstance(Query.class, DEFAULT_QUERY, DEFAULT_PAGE_SIZE, DEFAULT_QUERY_TYPE);

        new NonStrictExpectations()
        {
            {
                mockHttpResponse.getHeaderFields();
                result = testHeaderResponseMap;
                mockedQueryResponse.next();
                result = new NoSuchElementException();
            }
        };

        Deencapsulation.invoke(testQuery, "sendQueryRequest", mockIotHubConnectionString, mockUrl, mockHttpMethod, (long) 0);

        //act
        Object next = Deencapsulation.invoke(testQuery, "next");
    }
}
