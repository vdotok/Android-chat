VdoTok-Android-Chat
===================
Git Installation
==============================
* Please follow this [link](https://git-scm.com/book/en/v2/Getting-Started-Installing-Git) for GIT installation

IDE Installation & Build Guide
==============================
* Android Studio 4.1.2 or later (Stable Version)
* [Click here](https://developer.android.com/studio?gclid=Cj0KCQjwhr2FBhDbARIsACjwLo2fEHdB3l3eqRlhIvySYNx1-3XjDmuX1eSCbaCI7zU8FKHFkGBcVyMaAtSjEALw_wcB&gclsrc=aw.ds#downloads) to download and install Android Studio

<b>Code setup:</b>
* Open <b>Android Studio</b>
* Click on <b>Get from Version Control</b> or in case the Welcome page doesn't open, go to <b>File Menu->New</b> and select <b>Project From Version Control</b>
* Select <b>Repository URL</b> from left menu
* Select <b>Git</b> from <b>Version control</b> dropdown menu
* Paste this URL https://github.com/vdotok/Android-chat.git in URL section
* Click on <b>Clone</b> button and wait for build.gradle file. You can see the progress at the bottom of <b>Android Studio</b>
* go to file menu/app menu <b>Tools -> SDK Manager</b>
* Click all checkboxes from <b>API Level 21 and onward</b>
* Click <b>Apply</b> and download relevant packages
* Let the <b>Android Studio</b> install the components

Bravo! You’ve successfully configured the project in Android Studio.

<b>Download Connect Lib:</b>
* Use the following link to get the latest aar: https://sdk.vdotok.com/Android-SDKs/connect.aar 

<b>Configure Lib:</b>
* In <b> Android Studio File Explorer</b>, select <b>Project</b>
<img width="498" alt="123636395-41e08980-d836-11eb-8643-429d6e5510d5" src="https://user-images.githubusercontent.com/86282129/123811571-cb628b00-d90c-11eb-9584-b5a8f12957dc.png">

* Go to <b>VdoTok-Chat -> App -> libs</b>
* Add the downloaded <b>connect.aar</b> file in the libs folder
<img width="243" alt="Screenshot 2021-12-02 at 4 51 28 PM" src="https://user-images.githubusercontent.com/88875529/144416883-ceac6a20-99e6-4085-ae79-3d2ca746fb7b.png">
<li> After this, click on gradle sync icon <img width="21" alt="Screenshot 2021-12-02 at 4 43 51 PM" src="https://user-images.githubusercontent.com/88875529/144415902-78883f01-f5be-4f99-a6e3-d9ea44a71936.png"> from the toolbar to sync project.</li></br>

<b>Project Signup:</b>
* Register your account on [VdoTok](https://www.vdotok.com/) to get the <b>Project ID</b> and <b>API Service URL</b>
* From file explorer, double-click on <b>vdotok-chat -> app -> src -> main -> java -> com -> vdotok -> chat -> utils -> ApplicationConstants</b>, and replace <b>SDK_PROJECT_ID</b> with your own <b>Project Id</b>
* From file explorer, double-click on <b>vdotok-chat -> Network -> src -> main -> java -> com ->vdotok -> network -> utils -> Constants</b>, and replace <b>BASE_URL</b> with your own <b>API Service URL</b>

<b>Device Setting:</b>
* To connect a device, enable <b>“developer mode”</b> and <b>“USB debugging”</b> by following the device-specific steps provided [here.](https://developer.android.com/studio/debug/dev-options)
* Some devices need <b>“Install Via USB”</b> option below the <b>“USB debugging”</b> settings to be enabled in order to install the application in the device via USB.
* Click [here](https://support.mobiledit.com/portal/en/kb/articles/how-to-enable-usb-debugging) for additional device specific settings.


<b>Build Project:</b>
* Connect your phone with system in a <b>File-sharing Mode</b>
* You can find your phone name in <b>running devices</b> list, as described in the below image
* Select your device and click on <b>Play</b> button<img width="24" alt="Screenshot 2021-09-21 at 1 19 15 PM" src="https://user-images.githubusercontent.com/86282129/134136764-72c0f47e-6ecb-4c62-a562-804b68042fe5.png">
* After running some automated commands and building gradle, your app will be installed on your connected device
  <img width="1012" alt="Screenshot 2021-06-29 at 6 59 36 PM" src="https://user-images.githubusercontent.com/86282129/123811062-5bec9b80-d90c-11eb-96e1-ee50dee125c5.png">




