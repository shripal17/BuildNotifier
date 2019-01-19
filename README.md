# Build Notifier
Mainly made with AOSP ROM Developers in mind

We need to regularly check SSH to see build status of our ROMs.

But with this app and it's complementary script, you will be direcly notified with necessary details and most importantly <b>BUILD LOGS!</b>

You can view, sort, search and delete these notifications from the app and also view/save the Build Logs for each build from within the app!

Written in 100% Kotlin

## Getting Started
1. Download the jar file and the sample build script to your AOSP ROM Working Directory
2. Put your Device Token (can be obtained from app -> Settings -> Device Token at the top of build script, should look something like this:

`DEVICE_TOKEN=YOUR_DEVICE_TOKEN_COMES_HERE`

3. Modify the build script according to your build steps. You can name each step 

4. Run the script and relax!

## How It Works
- Uses Firebase to push notifications from your working directory to your device
- A Kotlin Script checks if each command fails or succeeds, uploads build logs to Firebase Storage
- Sends notification via Firebase to your device along with the details
- App receives the notification
- Loads the build logs (if available) on notification click

## Features
- View/Save Build Logs Directly
- Build Logs are Formatted for better readability (keywords like note, error, warning are highlighted)
- View, Sort, Search and Delete Notifications 
- Smooth and crisp animations in the app
- Dynamic theming
- User can select his/her favorite colors as Primary/Accent Colors throughout the app
- Dark theme enabled by default!

## Building
### What you will need:
- A google-services.json file generated from a firebase project (to be placed in *app* folder)
- Optional: credentials.gradle if using AdMob
Template for the `credentials.gradle` file is as follows:
```groovy
ext {
    ADMOB_APP_ID = '"ADMOB_APP_ID_HERE"'
    ADMOB_AD_UNIT_ID = '"ADMOB_BANNER_AD_UNIT_ID_HERE"'
    ADMOB_REWARDED_AD_UNIT_ID = '"ADMOB_REWARDED_AD_UNIT_ID_HERE"'
}
```
- Android Studio 3.4 Beta1+
- Kotlin Compiler or IntelliJ IDEA for compiling the jar file

### Build
Building the app is regular procedure for Android Studio

## License 
```
Copyright 2019 Shripal Jain

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
   ```
