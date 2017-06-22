/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.deps.serializer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.util.LinkedList;
import java.util.List;

public class QueryResponseParser
{
    private Gson gson;

    public enum TYPE
    {
        UNKNOWN("unknown"),
        TWIN("twin"),
        DEVICE_JOB("deviceJob"),
        JOB_RESPONSE("jobResponse"),
        RAW("raw"),
        JSON("json");

        private final String type;

        TYPE(String type)
        {
            this.type = type;
        }

        public String getValue()
        {
            return type;
        }
    }
    private TYPE type;

    private JsonObject[] jsonItems = null;

    /**
     * CONSTRUCTOR
     * Create an instance of the QueryResponseParser using the information in the provided json.
     *
     * @param json is the string that contains a valid json with the QueryResponse.
     * @throws IllegalArgumentException if the json is null, empty, or not valid.
     */
    public QueryResponseParser(String json, TYPE type) throws IllegalArgumentException
    {
        //Codes_SRS_QUERY_RESPONSE_PARSER_25_001: [The constructor shall create an instance of the QueryResponseParser.]
        gson = new GsonBuilder().disableHtmlEscaping().create();

        //Codes_SRS_QUERY_RESPONSE_PARSER_25_003: [If the provided json is null, empty, or not valid, the constructor shall throws IllegalArgumentException.]
        ParserUtility.validateStringUTF8(json);

        if (type != null && type == TYPE.UNKNOWN)
        {
            //Codes_SRS_QUERY_RESPONSE_PARSER_25_005: [**If the provided `type` is `UNKNOWN` the constructor shall throws IllegalArgumentException.**]**
          throw new IllegalArgumentException("type cannot be unknown");
        }

        try
        {
            this.jsonItems = gson.fromJson(json, JsonObject[].class);
        }
        catch (Exception malformed)
        {
            //Codes_SRS_QUERY_RESPONSE_PARSER_25_004: [**If the provided json do not contains a valid array of json items the constructor shall throws IllegalArgumentException.**]**
            throw new IllegalArgumentException("Malformed json:" + malformed);
        }

        //Codes_SRS_QUERY_RESPONSE_PARSER_25_002: [The constructor shall save the type provided.]
        this.type =  type;
    }

    /**
     * Getter for the type of response.
     *
     * @return string type.
     */
    public String getType()
    {
        //Codes_SRS_QUERY_RESPONSE_PARSER_25_007: [The getType shall return the string stored in type enum.]
        return this.type.getValue();
    }

    /**
     * Getter for Json Items from Json Array
     * @return the array of json as string
     */
    public List<String> getJsonItems()
    {
        List<String> jsonElements = new LinkedList<>();

        for (JsonObject json : this.jsonItems)
        {
            jsonElements.add(gson.toJson(json));
        }
        //Codes_SRS_QUERY_RESPONSE_PARSER_25_008: [The getJsonItems shall return the list of json items as strings .]
        return jsonElements;
    }

    /**
     * Getter for Twin Parser collection from Query Response
     * @return A list of twin parser objects
     * @throws IllegalStateException if this API is called when response was of a type other than twin
     * @throws IllegalArgumentException if twin parser cannot parse
     */
    public List<TwinParser> getTwins() throws IllegalStateException, IllegalArgumentException
    {
        if (this.type.compareTo(TYPE.TWIN) == 0)
        {
            //Codes_SRS_QUERY_RESPONSE_PARSER_25_012: [The getTwins shall throw IllegalArgumentException if the twin array from the json cannot be parsed]
            List<TwinParser> twinParsers = new LinkedList<>();

            for (JsonObject twin : this.jsonItems)
            {
                TwinParser twinParser = new TwinParser();
                twinParser.enableTags();
                twinParser.updateTwin(gson.toJson(twin));
                twinParsers.add(twinParser);
            }
            //Codes_SRS_QUERY_RESPONSE_PARSER_25_010: [The getTwins shall return the collection of twin parsers as retrieved and parsed from json.]
            return twinParsers;
        }
        else
        {
            //Codes_SRS_QUERY_RESPONSE_PARSER_25_011: [The getTwins shall throw IllegalStateException if the type represented by json is not "twin"]
            throw new IllegalStateException("Json does not contain twin type");
        }
    }

    /**
     * Getter for device jobs collection obtained from Query Response
     * @return A list of deviceJobs objects
     * @throws IllegalStateException if this API is called when response was of a type other than device jobs
     * @throws IllegalArgumentException if device jobs parser cannot parse
     */
    public List getDeviceJobs() throws IllegalStateException, IllegalArgumentException
    {
        if (this.type.compareTo(TYPE.DEVICE_JOB) == 0)
        {
            // placeholder for creating device jobs and return the collection
            //Codes_SRS_QUERY_RESPONSE_PARSER_25_013: [The getDeviceJobs shall return the collection of device jobs parsers as retrieved and parsed from json.]
            //Codes_SRS_QUERY_RESPONSE_PARSER_25_015: [The getDeviceJobs shall throw IllegalArgumentException if the items array from the json cannot be parsed]
            return null;
        }
        else
        {
            //Codes_SRS_QUERY_RESPONSE_PARSER_25_014: [The getDeviceJobs shall throw IllegalStateException if the type represented by json is not "deviceJobs"]
            throw new IllegalStateException("Json does not contain device jobs type");
        }
    }

    /**
     * Getter for jobs collection obtained from Query Response
     * @return A list of jobs objects
     * @throws IllegalStateException if this API is called when response was of a type other than jobs
     * @throws IllegalArgumentException if jobs parser cannot parse
     */
    public List getJobs() throws IllegalStateException, IllegalArgumentException
    {
        if (this.type.compareTo(TYPE.JOB_RESPONSE) == 0)
        {
            // placeholder for creating jobs response and return the collection
            //Codes_SRS_QUERY_RESPONSE_PARSER_25_016: [The getJobs shall return the collection of jobs parsers as retrieved and parsed from json.]
            //Codes_SRS_QUERY_RESPONSE_PARSER_25_018: [The getJobs shall throw IllegalArgumentException if the jobs array from the json cannot be parsed]
            return null;
        }
        else
        {
            //Codes_SRS_QUERY_RESPONSE_PARSER_25_017: [The getJobs shall throw IllegalStateException if the type represented by json is not "jobResponse"]
            throw new IllegalStateException("Json does not contain Jobs type");
        }
    }

    /**
     * Getter for raw data collection obtained from Query Response
     * @return A list of raw data objects as string
     * @throws IllegalStateException if this API is called when response was of a type other than raw
     */
    public List<String> getRawData() throws IllegalStateException
    {
        if (this.type.compareTo(TYPE.RAW) == 0)
        {
            //Codes_SRS_QUERY_RESPONSE_PARSER_25_019: [The getRawData shall return the collection of raw data json as string as retrieved and parsed from json.]
            //Codes_SRS_QUERY_RESPONSE_PARSER_25_021: [The getRawData shall throw IllegalArgumentException if the raw data array from the json cannot be parsed]
            List<String> rawJsons = new LinkedList<>();

            for (JsonObject json : this.jsonItems)
            {
                rawJsons.add(gson.toJson(json));
            }
            return rawJsons;
        }
        else
        {
            //Codes_SRS_QUERY_RESPONSE_PARSER_25_020: [The getRawData shall throw IllegalStateException if the type represented by json is not "raw"]
            throw new IllegalStateException("Json does not contain raw type");
        }
    }

}
