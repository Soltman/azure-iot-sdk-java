/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package samples.com.microsoft.azure.sdk.iot;

import com.microsoft.azure.sdk.iot.service.devicetwin.DeviceTwin;
import com.microsoft.azure.sdk.iot.service.devicetwin.Query;
import com.microsoft.azure.sdk.iot.service.devicetwin.DeviceTwinDevice;
import com.microsoft.azure.sdk.iot.service.devicetwin.Pair;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

/** Manages device twin operations on IotHub */
public class DeviceTwinSample
{
    private static final String iotHubConnectionString = "HostName=aziot-prmathur.azure-devices.net;SharedAccessKeyName=iothubowner;SharedAccessKey=m81kcZh4/3bjZyla+ChvFHxeezVgiyYK9iguWWAK7jg=";
    private static final String deviceId = "prmathur-test";
    private static String sqlQuery = "select * from devices";

    /**
     * @param args
     * @throws IOException
     * @throws URISyntaxException
     */
    public static void main(String[] args) throws Exception
    {
        System.out.println("Starting sample...");
        DeviceTwin twinClient = DeviceTwin.createFromConnectionString(iotHubConnectionString);

        DeviceTwinDevice device = new DeviceTwinDevice(deviceId);

        try
        {
            // Manage complete twin
            System.out.println("Getting device twin");
            twinClient.getTwin(device);
            System.out.println(device);

            //Update Twin Tags and Desired Properties
            Set<Pair> tags = new HashSet<Pair>();
            tags.add(new Pair("HomeID", UUID.randomUUID()));
            device.setTags(tags);

            Set<Pair> desProp = new HashSet<Pair>();
            int temp = new Random().nextInt(100);
            desProp.add(new Pair("temp", temp));
            device.setDesiredProperties(desProp);

            System.out.println("Updating device twin");
            twinClient.updateTwin(device);

            System.out.println("Getting device twin");
            twinClient.getTwin(device);
            System.out.println(device);

            //Query twin
            System.out.println("Started Querying twin");

            Query twinQuery = twinClient.queryTwin(sqlQuery, 3);
            while (twinClient.hasNextDeviceTwin(twinQuery))
            {
                DeviceTwinDevice d = twinClient.getNextDeviceTwin(twinQuery);
                System.out.println(d);
            }
        }
        catch (IotHubException e)
        {
            System.out.println(e.getMessage());
        }
        catch (IOException e)
        {
            System.out.println(e.getMessage());
        }
        
        System.out.println("Shutting down sample...");
    }
    

}
