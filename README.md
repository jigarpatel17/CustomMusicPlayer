# FCM Notification

* Project demonstrating send and receive notifications on Android device using FireBase cloud messaging service.

* Create a new project in FireBase console to set up for send and receive notification.


* here is a link of FireBase to create add project in fireBase 
   https://console.firebase.google.com/u/0/

- Add channel_id into your app manifest.xml file like this 

`<meta-data
            android:name="com.google.firebase.messaging.default_notification_channel_id"
            android:value="@string/default_notification_channel_id"/>`
            
* Add MessgaService to your app manifest.xml file like this
 
  `<service
             android:name="com.mindinventory.fcmnotification.messages.MyFirebaseMessagingService">
             <intent-filter>
                 <action android:name="com.google.firebase.MESSAGING_EVENT"/>
             </intent-filter>
         </service>`
         
 * Also add MyFirebaseInstanceIDService into your app manifest.xml file for register token to server and get updated token from server .
 
 `<service
             android:name="com.mindinventory.fcmnotification.messages.MyFirebaseInstanceIDService">
             <intent-filter>
                 <action android:name="com.google.firebase.INSTANCE_ID_EVENT"/>
             </intent-filter>
         </service>`
         
* After you created or add required data in app console you can open messaging screen from this link.  
https://console.firebase.google.com/u/0/project/exampleplacepicker-154714/notification


* You can receive message of notification in Main Activity using following code

----------------------------------------------------------------
        if (getIntent().getExtras() != null) {
 
            for (String key : getIntent().getExtras().keySet()) {
    
                Object value = getIntent().getExtras().get(key);
                Log.d(TAG, "Key: " + key + " Value: " + value);
            }
        }
 ----------------------------------------------------------------
 
 
 
![Screenshots](http://full/path/to/img.jpg "Mind Inventory")
