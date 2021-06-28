# Android-chat
vdotok-Android-chat
Readme

Installation
============

Requirements
* Android Studio 4.1.2 or later
* you can follow the link to download and install Android Stuiod
    * “https://developer.android.com/studio?gclid=Cj0KCQjwhr2FBhDbARIsACjwLo2fEHdB3l3eqRlhIvySYNx1-3XjDmuX1eSCbaCI7zU8FKHFkGBcVyMaAtSjEALw_wcB&gclsrc=aw.ds#downloads”
* Android SDKs 21(Lollipop) or later
    * For downloading Android SDKs
    * Open Android Studio
    * Goto file menu Tools and select “SDK Manager”
    * Checkmark api level 21 and onward 
    * click apply and download respective packages

Project Signup and Project ID
* Follow the link below register your self for chat server and get the project Id
* https://www.kuchtohoga.com/norgic/chatSDK/

To Download Chat Lib
* Follow the link below and download “norgic-chatsdk-v1.0.0.aar” file
* https://sdk.vdotok.com/Android-SDKs/

Code setup
* Copy and Paste Github URL “https://github.com/RazaNorgic/VDOTOK-Android-chat”
* Click on “Code” button
* From HTTPS section copy repo URL
* Open Android Studio
* Click on “Get from Version Control”
* Select “Repository URL” from left menu
* Select “Git” from Version control dropdown menu
* Paste copied URL in URL section
* click on clone button and wait for build .gradle files you can see the progress on bottom of android studio
* let the android studio install the components, 
* Hurrraaaa you Just configure the project in android studio

Device Setting
* In order to connect you device with android studio you need to enable developer mode
* For enabling developer mode and usb debug you may follow the device specific steps
* you can follow the step described in link below to enable developer options and usb debugging
* https://developer.android.com/studio/debug/dev-options

Configure Lib
* From Android Studio file menu click on File->New->New Module-> Import .JAR/.AAR Package and click on next
* Select downloaded “norgic-chatsdk-v1.0.0.aar” .AAR file and click Finish
* From Android Studio File Explorer  select project

￼
* And then go to ChattApp -> app -> src -> build.gradle and past following line in Dependencies  section
* “implementation project(path: ':norgic-chatsdk-v1.0.0’)”
￼
* Click on “sync now” Button from top right corner
* From File explorer open chattApp -> app -> src -> main -> java -> com -> norgic -> vdotokchat -> utils -> ApplicationConstants replace “SDK_TENANT_ID” with your own TenantID 

Build Project 
* Connect your phone with system in file sharing mode
* You can find your phone name in running devices list like describe in below image
* select your device and click on Play button
* After running some automated commands and building gradle your app will install on you connected device
￼
