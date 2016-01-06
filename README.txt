Open Source Overview
============================
Application name:
SmarTrAC

Version number:
1.0

Application description:
SmartTrAC is a smart phone application that collects highly-detailed activity and travel behavior data with minimal user burden, providing a compelling alternative to the traditional diary-based method typically used to collect individual travel and activity information.


Primary functions:
*Automatically detects and classifies daily activity and travel episodes using smartphone GPS and accelerometer data.

*allows users to view, correct, and augments the automatically detected and classified information.

*provides annotated and aggregated activity/trip details.

Installation and removal instructions:
There is no installer for this app only source code is provided. 

License information
-------------------
This software is licensed under the The MIT License (MIT) - http://opensource.org/licenses/MIT


System Requirements
-------------------------
This software is compatible with Android devices running Android 4.1 or higher.

If new users want to use the database upload function, they need to initiate their own cloud storage service and update the code to point to their own storage. During the field test, the developers used the Amazon S3 service and have since closed their account. The database upload function is not usable unless developers who use the code set up their own cloud stoarage service and update the code to point to their own storage.

in order to use google maps, the developers need to have their own google map api keys. We were using debug keys and those keys are associated with our programmers. Anyone who wants to use the code to generate apk on their machine should use their own google map api keys in order for the google map feature to work correctly. Google Maps has detailed developer instructions for it at https://developers.google.com/maps/documentation/android-api/start?hl=en


Documentation
-------------
Documentation can be found in the '\user guide' directory or is viewable online at http://smartrac.umn.edu/for-users

Web sites
---------
SmarTrAC software is distributed through the USDOT's JPO Open Source Application Development Portal (OSADP)
http://itsforge.net/ 

For more information on SmarTrAC visit http://smartrac.umn.edu/
