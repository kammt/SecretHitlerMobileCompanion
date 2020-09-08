# Secret Hitler Mobile Companion <img src="/app/src/main/res/mipmap-xxxhdpi/ic_launcher.png" width="10%" align="left">
An Android App intended to make the board game experience of Secret Hitler more easy and fun. 

## Overview and Features
- **Keep track of your Game Events.** You might know that moment - you have entered Hitler zone and now want to see who can be trusted the most. But what did that person do again? Did he play a liberal or a fascist policy? What did he claim? By entering all events into the App, you can now easily see them in a Card-based list and even filter to only show events including certain players!
- **Make it accessible to everyone.** It might be quite counterproductive to leave your phone in the middle of the table for everyone to look at. That's why there is the option to start a Web Server. That way, only one person uses the app to add events, while everyone else can see it through their web browser.
- **Set the mood.** Who has once played the online game SecretHitler.io will probably remember the sounds used on the website. For everyone that wants to play the sounds during their "offline" session - you are in luck! The host device will, if enabled, play all important sounds, including when a policy has been enacted or when a player has been executed.
- **Minimal permissions used.** The following permissions are required:
  - *FOREGROUND_SERVICE* - to be able to run the Web Server in a Foreground Service, thus making it more stable
  - *INTERNET* - To be able to start and use the Web Server
  - *ACCESS_WIFI_STATE* - To be able to retrieve the device's IP address
  - *ACCESS_NETWORK_STATE* - To be able to determine the type of connection (WiFi, mobile hotspot etc.)
- **Free and Open Source.** Licensed under the <a href="LICENSE">GPLv3 license</a>

## Screenshots
<img src="/project-images/gamelog.png" width="30%"> <img src="/project-images/addLegSession.png" width="30%"> <img src="/project-images/playerBlurring.png" width="30%"> 
<img src="/project-images/serverStatus.png" width="30%"><img src="/project-images/webserver.png" width="30%">

## License and Attribution

- Image and sound files have been used from the SecretHitlerIO project (https://github.com/cozuya/secret-hitler, licensed under the <a href="https://creativecommons.org/licenses/by-nc-sa/4.0/">Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International license</a>). Sound files used in this project are licensed under <a href="https://creativecommons.org/publicdomain/zero/1.0/">zero (cc0)</a>, <a href="https://creativecommons.org/licenses/by/3.0/">by</a>, and <a href="https://creativecommons.org/licenses/by-nc/3.0/">by-nc</a>. 
- The following libraries are being used in this project:
  - NanoHttpd (https://github.com/NanoHttpd/nanohttpd, licensed under the <a href="https://github.com/NanoHttpd/nanohttpd/blob/master/LICENSE.md">BSD 3-Clause license</a>)
  - QRGen (https://github.com/kenglxn/QRGen, licensed under the <a href="https://choosealicense.com/licenses/apache-2.0/">Apache 2.0 license</a>)
  - LeakCanary (https://github.com/square/leakcanary, licensed under the <a href="https://choosealicense.com/licenses/apache-2.0/">Apache 2.0 license</a>)
  - FlexboxLayout (https://github.com/google/flexbox-layout, licensed under the <a href="https://choosealicense.com/licenses/apache-2.0/">Apache 2.0 license</a>)
- This project uses the following fonts:
  - Germania One, designed by John Vargas Beltrán, licensed under the <a href="https://scripts.sil.org/cms/scripts/page.php?site_id=nrsi&id=OFL">Open Font license</a>
  - Comfortaa, designed by Johan Aakerlund, Cyreal, licensed under the <a href="https://scripts.sil.org/cms/scripts/page.php?site_id=nrsi&id=OFL">Open Font license</a>
- The purpose of this app is to extend on the board game Secret Hitler. Secret Hitler is designed by Max Temkin (Cards Against Humanity, Humans vs. Zombies) Mike Boxleiter (Solipskier, TouchTone), Tommy Maranges (Philosophy Bro) and illustrated by Mackenzie Schubert (Letter Tycoon, Penny Press).
The game is licensed as per the <a href="https://creativecommons.org/licenses/by-nc-sa/4.0/">Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International license</a>.
- This App is licensed under the GPLv3 license. (See the <a href="LICENSE">LICENSE file</a> for more information).
