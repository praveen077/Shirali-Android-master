# Project Title
### Welcome to ShiraLi - The Next Gen of Jewish Music Has Arrived!
- The First Ever Jewish Music Streaming App. With Shirali In The Palm Of Your Hand You Instantly Gain Access To Thousands Of Premium Songs And Ready Made Playlists For Just About Any Occasion!

## Environment And Tools:

  - Android studio 3.0.1
  - Java jdk and jre.

## Dependency libraries list:

- Google Play-services(11.0.4),
- Mixpanel-android(5.2.1),
- Retrofit2:retrofit(2.2.0),
- Retrofit2:converter-gson(2.2.0),
- Glide(3.7.0),
- Firebase-messaging(11.0.4),
- Stripe(4.1.5),
- Socket(0.4.1)
- OneSignal([3.8.3, 3.99.99]),
- Page Indicator(2.4.1),
- Sticky Header(0.4.2)

## Installation of dependencies:
- Requires Android 4.1 and up version

##### Note: Add build genarate librery.
-  CircleCI is used to create build for testing and Create release apk from android studio for deploy on play store.

## Project Folder structure (app):
###### All files in app which contain all our code in following packages

- activity    :We have created this directory and it contain all activities.
- adapter     :We have created this directory and it contain all Adapters.
- controls    :We have created this directory and it contain player controls class.
- fragment    :We have created this directory and it contain all fragment classes.
- interfaces  :We have created this directory and it contain all interfaces.
- model	      :We have created this directory and it contain all data model classes.
- network     :We have created this directory and it contain retrofit interface.
- recevier    :We have created this directory and it contain all receviers.
- service     :We have created this directory and it contain all service classes.
- Util	      :We have created this directory and it contain all commonly used methods and code.
- view        :We have created this directory and it contain all custom view.
- widget      :We have created this directory and it contain all external classes.
- Res-Drawable:Android Studio generated directory and it contain all images which we are using in our application.
- Res-layout  :Android Studio generated directory and it contain all xml classes
- Res-values  :Android Studio generated directory and it contain all values classes
- Res-mipmap  :Android Studio generated directory and it contain all app icons.

## Switching between Production and Staging
###### Open shirali-android/app/src/main/java/com/shirali/util/Constants.java 
you can find static variable called private static String baseServerLiveUrl for staging and production 
- If we want to run app in Production mode, Comment with Line Comment of staging urls and PROJECT_TOKEN:

Ex: //Production
    public static String baseServerLiveUrl = "https://d1lfer5iumgfim.cloudfront.net/v2/";
    public static String socketUrl = "https://d15cf3tnir8i53.cloudfront.net";
    public static String songUrl = "https://d15cf3tnir8i53.cloudfront.net/v2/song/getSignedUrl";
    public static String PROJECT_TOKEN = "xyz";

    //Staging
//  public static String baseServerLiveUrl = "http://stagmapi.shiraliapp.com/v2/";
//  public static String socketUrl = "http://stagmapi.shiraliapp.com";
//  public static String songUrl = "http://stagmapi.shiraliapp.com/v2/song/getSignedUrl";
//  public static String PROJECT_TOKEN = "xyz";

- If we want to run app in Staging mode, Comment with Line Comment of production urls and PROJECT_TOKEN:
 
Ex: //Production
//  public static String baseServerLiveUrl = "https://d1lfer5iumgfim.cloudfront.net/v2/";
//  public static String socketUrl = "https://d15cf3tnir8i53.cloudfront.net";
//  public static String songUrl = "https://d15cf3tnir8i53.cloudfront.net/v2/song/getSignedUrl";
//  public static String PROJECT_TOKEN = "xyz";

    //Staging
    public static String baseServerLiveUrl = "http://stagmapi.shiraliapp.com/v2/";
    public static String socketUrl = "http://stagmapi.shiraliapp.com";
    public static String songUrl = "http://stagmapi.shiraliapp.com/v2/song/getSignedUrl";
    public static String PROJECT_TOKEN = "xyz";

## End point Server details

- Production :https://d1lfer5iumgfim.cloudfront.net/v2/
- Staging    :http://stagmapi.shiraliapp.com/v2/

## Stripe Live and Text key details

- Stripe with live key.
     public static String stripe_Publish_Live_Key = "live_xyz";

- Stripe with test key.
     public static String stripe_test_key = "test_xyz";

## Run the project:

- Open project in android studio 3.0.1 and up.

## Support:
Contact Us: support@shiraliapp.com
###### Privacy & Policy:
https://www.shiraliapp.com/privacy
###### Terms & Conditions: 
https://www.shiraliapp.com/terms
###### License:
Copyright © 2017 ShiraLi Technology, Inc. All rights reserved
