package com.mycompany.app;
import com.microsoft.azure.sdk.iot.device.*;
import com.google.gson.Gson;

import java.io.*;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
/**
 * Hello world!
 *
 */
public class App
{
	private static String connString = "HostName=myTeam.azure-devices.net;DeviceId=myTeam.azure-devices.net;SharedAccessKey=。。。";
	private static IotHubClientProtocol protocol = IotHubClientProtocol.MQTT;
	private static String deviceId = "myTeam.azure-devices.net";
	private static DeviceClient client;

	private static int idx = 0;
	public static String[] trainid = {"train1","train2","train3","train4","train5"};

	public static void main( String[] args ) throws IOException, URISyntaxException, InterruptedException {
		  client = new DeviceClient(connString, protocol);
		  client.open();

		  MessageSender sender1 = new MessageSender();
		  MessageSender sender2 = new MessageSender();
		  MessageSender sender3 = new MessageSender();
		  MessageSender sender4 = new MessageSender();
		  MessageSender sender5 = new MessageSender();
		  ExecutorService executor = Executors.newFixedThreadPool(5);
		  Random rand = new Random();
		  executor.execute(sender1);
		  Thread.sleep(rand.nextInt(10)*1000);
		  executor.execute(sender2);
		  Thread.sleep(rand.nextInt(10)*1000);
		  executor.execute(sender3);
		  Thread.sleep(rand.nextInt(10)*1000);
		  executor.execute(sender4);
		  Thread.sleep(rand.nextInt(10)*1000);
		  executor.execute(sender5);
		  Thread.sleep(rand.nextInt(10)*1000);






		  System.out.println("Press ENTER to exit.");
		  System.in.read();
		  executor.shutdownNow();
		  client.closeNow();

		}



    private static class EventCallback implements IotHubEventCallback {
    	  public void execute(IotHubStatusCode status, Object context) {
    	    System.out.println("IoT Hub responded to message with status: " + status.name());

    	    if (context != null) {
    	      synchronized (context) {
    	        context.notify();
    	      }
    	    }
    	  }
    	}

    private static class Bean {
  	  public String rideId;
  	  public String trainId;
  	  public int passengerCount;
  	  public String eventType;
  	  public String deviceTime;

  	  public String serialize() {
  	    Gson gson = new Gson();
  	    return gson.toJson(this);
  	  }
  }

    private static class MessageSender implements Runnable {

    	public static String trainStart(String trainid,String rid,String event,String renshu,String dt){

    		Bean b = new Bean();
    		b.rideId = rid;
    		b.trainId = trainid;
    		b.eventType = event;
    		b.passengerCount = Integer.parseInt(renshu);
    		return b.serialize();
    	}

    	  public void run()  {
//    		  System.out.println(idx++);
    	    try {

    	      Random rand = new Random();
    	      String trainid = App.trainid[App.idx];
    	      App.idx++;

    	      while (true) {

    	        String rid = java.util.UUID.randomUUID().toString();
    	        String renshu = String.valueOf(rand.nextInt(40));
    	        Date d = new Date();
    	        SimpleDateFormat format0 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


    	        String msgStr = trainStart(trainid,rid,"RideStart",renshu,format0.format(d));

//    	        String msgStr = telemetryDataPoint.serialize();
    	        Message msg = new Message(msgStr);
//    	        msg.setProperty("temperatureAlert", (currentTemperature > 30) ? "true" : "false");
    	        msg.setMessageId(java.util.UUID.randomUUID().toString());
    	        System.out.println("Sending: " + msgStr);

    	        Object lockobj = new Object();
    	        EventCallback callback = new EventCallback();
    	        client.sendEventAsync(msg, callback, lockobj);
    	        synchronized (lockobj) {
    	          lockobj.wait();
    	        }
    	        Thread.sleep(rand.nextInt(10)*1000);


    	        for(int i=0;i<rand.nextInt(4);i++){
	    	        msgStr = trainStart(trainid,rid,"PhotoTriggered",renshu,format0.format(d));

	//    	        String msgStr = telemetryDataPoint.serialize();
	    	         msg = new Message(msgStr);
	//    	        msg.setProperty("temperatureAlert", (currentTemperature > 30) ? "true" : "false");
	    	        msg.setMessageId(java.util.UUID.randomUUID().toString());
	    	        System.out.println("Sending: " + msgStr);

	    	         lockobj = new Object();
	    	         callback = new EventCallback();
	    	        client.sendEventAsync(msg, callback, lockobj);
	    	        synchronized (lockobj) {
	    	          lockobj.wait();
	    	        }
	    	        Thread.sleep(rand.nextInt(10)*1000);
    	        }

    	        msgStr = trainStart(trainid,rid,"RideEnd",renshu,format0.format(d));

//    	        String msgStr = telemetryDataPoint.serialize();
    	         msg = new Message(msgStr);
//    	        msg.setProperty("temperatureAlert", (currentTemperature > 30) ? "true" : "false");
    	        msg.setMessageId(java.util.UUID.randomUUID().toString());
    	        System.out.println("Sending: " + msgStr);

    	         lockobj = new Object();
    	         callback = new EventCallback();
    	        client.sendEventAsync(msg, callback, lockobj);
    	        synchronized (lockobj) {
    	          lockobj.wait();
    	        }
    	        Thread.sleep(rand.nextInt(10)*1000);

    	      }
    	    } catch (InterruptedException e) {
    	      System.out.println("Finished.");
    	    }
    	  }
    	}
}
