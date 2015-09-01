# Getting Started with Music Synthesizer for Android #

The following steps will get you started working with the Music Synthesizer for Android code in a Unix-like environment. The following environment variables are used:
  * `SYNTH_PATH` - The folder which contains the Music Synthesizer source code.
  * `PROTO_PATH` - The folder which contains the Protocol Buffers.

## Android IDE & SDKs ##
Please download and set up the Java & Android IDE & SDKs if you have not done so already.

1.  [Java Development Kit](http://www.oracle.com/technetwork/java/javase/downloads/index.html) (JDK7 recommended)
2.  One of the following:
   - [Eclipse IDE for Java Developers](https://eclipse.org/downloads/packages/) with the [Android Standard Development Kit Tools](https://developer.android.com/sdk/index.html#Other)  (API17 recommended)
   - [Android Studio](https://developer.android.com/sdk/installing/index.html?pkg=studio)  _NOTE:_ Android Studio is not yet supported by this project. However, the core, test, and j2se packages can be built using Ant. So the desktop tools in the j2se package can still be built without Eclipse.

## Clone the Code ##
Using Git, clone the Music Synthesizer for Android source code:

    git clone https://github.com/matthewcordaro/music-synthesizer-for-android.git
    
Set your `SYNTH_PATH` variable.  Example:

    export SYNTH_PATH=$HOME/music-synthesizer-for-android

## Download & Install Protocol Buffers ##
[Download](https://developers.google.com/protocol-buffers/docs/downloads) the Google Protocol Buffers. An overview can be found [here](https://developers.google.com/protocol-buffers/docs/overview). The source code can be found [here](https://github.com/google/protobuf).

### Windows ###
Download the pre built Windows `protoc` compiler and install it in `$SYNTH_PATH/core/bin`.

Set your `PROTO_PATH` variable. Example:

    export PROTO_PATH=$HOME/protobuf-{VERSION}

### Debian (Ubuntu) ###
With elevated (`sudo`) privileges run the following commands to download & install necessary software & libraries:

    dpkg --add-architecture i386
    apt-get update
    apt-get install lib32z1 libc6-i386 lib32stdc++6 lib32gcc1 lib32ncurses5 g++ maven

Extract the buffers:

    tar -xzvf protobuf-*.tar.gz

Set your `PROTO_PATH` variable. Example:

    export PROTO_PATH=$HOME/protobuf-{VERSION}

Build the `protoc` compiler by running the following commands:
```
cd $PROTO_PATH
./configure --prefix=$PROTO_PATH
make
make check
make install
mkdir $SYNTH_PATH/core/bin
cp $PROTO_PATH/bin/protoc $SYNTH_PATH/core/bin/
```

Build the protocol buffer runtime libraries jar.
```
cd $PROTO_PATH/java
mvn test
mvn install
mvn package
mkdir $SYNTH_PATH/core/lib
cp $PROTO_PATH/java/target/protobuf-java-{VERSION}.jar $SYNTH_PATH/core/lib/libprotobuf.jar
```

## Testing Music Synthesizer for Android core components ##
To make sure everything so far is installed correctly, run the tests and make sure they all build and pass.
```
cd $SYNTH_PATH
ant test
```

## Android Native-Code Development Kit (NDK) ##
The synthesizer engine is written in C++ for higher performance, and uses OpenSL ES to output sound. You will need to install the [Android NDK](https://developer.android.com/ndk). _NOTE:_ Currently only NDK 9d and earlier is supported. Download NDK 9 @ http://dl.google.com/android/ndk/android-ndk-r9-linux-x86.tar.bz2.

### Compiling the Synthesizer Engine ###
You can either manually run the NDK compile, or set up your Eclipse project to run it automatically:
  - Manual Building 

    Make sure that ndk-build is on your path, go into the android subdirectory and run:

        ndk-build

  - Integrated Eclipse Building 

    Edit `android/.externalToolBuilders/NDK Builder.launch` to make sure that `ATTR\_LOCATION` points to a valid location for the ndk-build binary. Example:
    
        ${HOME}/install/android-ndk-{VERSION}/ndk-build

The result of the ndk-build step is to create a libsynth.so file containing the shared library. Example: 

    android/libs/armeabi-{VERSION}/libsynth.so

The shared library build depends on the target architecture (unlike Java code). The default is __armeabi-v7a__, and can be changed by editing `APP\_ABI` in the `android/jni/Application.mk` file.

___Note:___ Code built for armeabi will run on ARM v7 devices, but more slowly. It might make sense to set `APP\_ABI` to `all` so that it will run on more devices, but at the expense of slowing the compile cycle and potentially bloating the APK file size.

## Make the Environment Variables Persistent ##
You will probably want to add the `SYNTH_PATH` & `PROTO_PATH` variables to the OS so you can build / make life easier after a reboot.

### Windows ###
1.  Goto Control Panel > System and Security > System.
2.  Click on "Advanced System Settings".
3.  Click on "Environment Variables".
4.  Add the variables by clicking on "New" under "User Variables" or "System Variables" depending on your preference.

### Debian (Ubuntu) ###
Append the `export X_PATH=path/to/something` to the end of your `~/.profile`, `/etc/profile`, or `/etc/environment` depending on your preference.

## Setting up Music Synthesizer in Eclipse ##
1. Make a new workspace.
2. Import the project. _File > Import > Android > Existing Android Code Into Workspace_. Follow on-screen instructions.

Note: You will probably get errors on import (duplicate entry 'src', empty ${project\_loc}, and maybe others). You can ignore these (although it would be great to clean them up).
