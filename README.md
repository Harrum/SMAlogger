# SMAlogger
This is a Java translation of the C++ SBFSpot program, https://sbfspot.codeplex.com/. It can read data from a SMA solar inverter such
as power production, device temperature and a whole lot more.

It only works for ethernet connections, bluetooth is not supported in this version. Also logging to various solar sites as well as csv 
and sql are removed for now.

The plan is to transform this project into to some form of api so it can be uses in a more flexible way. The current version does
basically the same as the original SBFspot project.

# Usage
To use this project you need Java version 8 and a SMA solar inverter, no additionally libraries are needed. Use the argument -v[1-5] 
to set the level of logging and -d[1-5] to set the level of debug information.

Major props to the creators of the original SBFspot project.

# Disclaimer
A user of SBFspot software acknowledges that he or she is receiving this software on an "as is" basis and the user is not relying on the accuracy or functionality of the software for any purpose. The user further acknowledges that any use of this software will be at his own risk and the copyright owner accepts no responsibility whatsoever arising from the use or application of the software.

SMA, Speedwire are registered trademarks of SMA Solar Technology AG
