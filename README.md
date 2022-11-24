# FCM Notifications with image

This example presents a solution on how to receive [Firebase notifications](https://firebase.google.com/docs/cloud-messaging) with **image** received while the app is in **foreground**.

Notifications received in app background are sent to system tray and handled by [Firebase SDK](https://firebase.google.com/docs/cloud-messaging/android/receive) as usual.

The examle utilizes [Glide](https://github.com/bumptech/glide) for image fetching and caching, as well as coroutines for handling the background work with a timeout of **5 seconds**, which is the approximate life of the Notification Service (FCM SDK has the same timeout when handling foreground).

If you're interested in how FCM used to work:

[My old Pluralsight tutorial (outdated)](https://www.pluralsight.com/guides/push-notifications-with-firebase-cloud-messaging)

![Screenshot_1669296547](https://user-images.githubusercontent.com/19796939/203799724-f557e8b0-ef19-46bb-b1b5-ef4b381e6552.png)
