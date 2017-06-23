/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service.devicetwin;

import com.microsoft.azure.sdk.iot.deps.serializer.QueryResponseParser;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class QueryResponse implements Iterator<Object>
{
    private ListIterator<?> responseElementsIterator;

    public QueryResponse(QueryType queryType, String jsonString) throws IOException
    {
        QueryResponseParser responseParser;
        List<?> responseElements;

        if (queryType == QueryType.TWIN)
        {
            responseElements = new QueryResponseParser(jsonString, QueryResponseParser.TYPE.TWIN).getTwins();
        }
        else if (queryType == QueryType.JOB_RESPONSE)
        {
            responseParser = new QueryResponseParser(jsonString, QueryResponseParser.TYPE.JOB_RESPONSE);
            responseElements = responseParser.getJobs();
        }
        else if (queryType == QueryType.DEVICE_JOB)
        {
            responseParser = new QueryResponseParser(jsonString, QueryResponseParser.TYPE.DEVICE_JOB);
            responseElements = responseParser.getDeviceJobs();
        }
        else if (queryType == QueryType.RAW)
        {
            responseParser = new QueryResponseParser(jsonString, QueryResponseParser.TYPE.RAW);
            responseElements = responseParser.getRawData();
        }
        else if (queryType == QueryType.JSON)
        {
            responseParser = new QueryResponseParser(jsonString, QueryResponseParser.TYPE.JSON);
            responseElements = responseParser.getJsonItems();
        }
        else
        {
            throw new IOException("Unknown queryType could not be parsed");
        }
        this.responseElementsIterator = responseElements.listIterator();
    }

    @Override
    public boolean hasNext()
    {
        return this.responseElementsIterator.hasNext();
    }

    @Override
    public Object next()
    {
        return this.responseElementsIterator.next();
    }
}
