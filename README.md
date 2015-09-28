# Music Synthesizer for Android #
## Getting Started ##
The following steps will get you started with Music Synthesizer for Android code in a Unix-like environment.  Read each step carefully.  Also check out the [notes section](#notes) for further details on the project. The following environment variables are used:
  * `SYNTH_PATH` - The folder which contains the Music Synthesizer source code.
  * `PROTO_PATH` - The folder which contains the Protocol Buffers.

## Downloads ##
The following is a list of IDEs, SDKs, libraries, other necessary software.  Download and extract them.

1.  The [Java Development Kit](http://www.oracle.com/technetwork/java/javase/downloads/index.html). (JDK7 recommended)
2.  One of the following IDEs running the newest Android API.

    - [Eclipse for Java Developers](https://eclipse.org/downloads/packages/) with the [Android Standard Development Kit Tools](https://developer.android.com/sdk/index.html#Other).
    - [Android Studio](https://developer.android.com/sdk/installing/index.html?pkg=studio) (Not yet fully supported by this project.)

3.  The [Android Native-Code Development Kit (NDK)](https://developer.android.com/ndk).  _Tip:_ If you extract it to `$HOME/android-ndk` (leaving out the version number) a future step will be easier.
  
4. The [Google Protocol Buffers](https://developers.google.com/protocol-buffers/docs/downloads). _Tip:_ If you extract it to `$HOME/protobuf` (leaving out the version number) a future step will be easier.

5.  Debian (Ubuntu) Only

    Run the following commands to download and install necessary software and libraries:

        sudo dpkg --add-architecture i386
        sudo apt-get update
        sudo apt-get install git lib32z1 libc6-i386 lib32stdc++6 lib32gcc1 lib32ncurses5 g++ maven default-jre default-jdk

## Clone the Code ##
Using Git, clone the Music Synthesizer for Android source code and set your `SYNTH_PATH` variable:

    cd $HOME
    git clone https://github.com/matthewcordaro/music-synthesizer-for-android.git
    export SYNTH_PATH=$HOME/music-synthesizer-for-android

## Make the Protocol Buffers - Debian (Ubuntu) Only ##

1. Set your `PROTO_PATH` variable. Example:
    ```
    export PROTO_PATH=$HOME/protobuf
    ```

2. Build the protocol buffer binary by running the following commands:
    ```
    cd $PROTO_PATH
    ./configure --prefix=$PROTO_PATH
    make
    make check
    make install
    ```

3. Build the protocol buffer runtime library by running the following commands:
    ```
    cd $PROTO_PATH/java
    mvn test
    mvn install
    mvn package
    ```

## Install the Protocol Buffers ##
### Windows ###
Install the pre built Windows `protoc` compiler in `$SYNTH_PATH/core/bin`.

### Debian (Ubuntu) ###
Copy the binary and runtime library to the project folder by running the following commands:
```
mkdir $SYNTH_PATH/core/bin $SYNTH_PATH/core/lib
cp $PROTO_PATH/bin/protoc $SYNTH_PATH/core/bin/
cp $PROTO_PATH/java/target/protobuf-java.jar $SYNTH_PATH/core/lib/libprotobuf.jar
```

## Test Core Components ##
To make sure everything is installed correctly so far, run the ant tests.  If they do not all pass, verify that you have done the above steps properly.
```
cd $SYNTH_PATH
ant test
```

## Make the Environment Variables Persistent ##
You will probably want to add the `SYNTH_PATH` and `PROTO_PATH` environment variables to the OS so life is a little easier. You may want to include both the NDK and SDK Tools on your `PATH` as well.

### Windows ###
1.  Goto Control Panel > System and Security > System.
2.  Click on "Advanced System Settings".
3.  Click on "Environment Variables".
4.  Add the variables by clicking on "New" under "User Variables" or "System Variables" depending on your preference.

### Debian (Ubuntu) ###
Append the `export X_PATH=path/to/something` to the end of your `~/.profile`, `/etc/profile`, or `/etc/environment` depending on your preference.

## Compile the Synthesizer Engine ##
Now we use the NDK to build the shared libraries. (`libsynth.so` files that will be found in `$SYNTH_PATH/android/libs/*/`.)  You can either manually run the NDK compile, or have your Eclipse project run it automatically.
  - For automatic Eclipse building, if your NDK is saved to `$HOME/android-ndk` then Eclipse should automatically work and you can skip this step. Otherwise, edit `$SYNTH_PATH/android/.externalToolBuilders/NDK Builder.launch` to make sure that `ATTR\_LOCATION` points to a valid location for the ndk-build binary, such as `${HOME}/android-ndk/ndk-build`.

  - For manual building, make sure that the NDK folder is in your `PATH` variable and run:
    ```
    cd $SYNTH_PATH/android
    ndk-build
    ```

_Tip:_ If you run into any errors, try `ndk-build clean` then `ndk-build` before searching for a fix.

## Setting up Music Synthesizer in Eclipse ##
1. Make a new workspace.
2. Import the project. _File > Import > Android > Existing Android Code Into Workspace_. Follow on-screen instructions.

_Tip:_ If you receive many errors that state "X cannot be resolved to a type" then it is likely your NDK generated Java files are not linked properly. Go to _Project > Properties > Resource > Linked Resources_ and click on Linked Resources tab. Finally edit the core-gen variable to point to `PROJECT_LOC/core-gen` or similar.

##  Notes ##
- The synthesizer engine is written in C++ for performance, and uses OpenSL ES to output sound.
-  A NDK build depends on the target architecture (unlike Java code). It can be changed by editing `APP\_ABI` in the `$SYNTH_PATH/android/jni/Application.mk` file. We have defaulted `APP\_ABI` to `all` so that it will run on all possible devices. However, this slows the compile cycle and increases the APK file size. If you are routinely editing code for a single device, you may want to change this to the proper architecture. See [here](https://developer.android.com/ndk/guides/arch.html) for more information. _Note:_ Code built for `armeabi` will run on _ARM v7_ devices, but more slowly.
-  The core, test, and j2se packages can be built using Ant. So the desktop tools in the j2se package can still be built without an IDE.
-  In Eclipse you will probably get errors on import (duplicate entry 'src', empty ${project\_loc}, and maybe others). You can ignore these (although it would be great to clean them up).
