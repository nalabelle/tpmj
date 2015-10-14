HOW TO compile the Windows IFXTPM driver
by Luis F. G. Sarmenta 
20070208


References:
-----------

How to use JNI ...
http://java.sun.com/docs/books/jni/html/start.html

How to create the .lib file from ifxtpm.dll without having it originally ...
http://support.microsoft.com/default.aspx?scid=kb;en-us;q131313


Steps for recompiling things, in case you make any changes ...
---------

0) Create the .def file from ifxtpm.dll and produce ifxtpm.lib, as described in the article above.
(Note: the TPM/J distribution should already include a pre-made ifxtpm.lib file
in the c\IFXTPM directory, so this step should not be necessary.)

1) Write and compile src/.../drivers/win32/Win32IFXTPMDriver.java

2) Cd to c/IFXTPM directory, set classpath appropriately, then run 

javah -jni edu.mit.csail.tpmj.drivers.win32.Win32IFXTPMDriver

3) Rename the file that is generated to IFXTPMJNIProxy.h

4) Write IFXTPMJNIProxy.c (excerpt and adapt header file from TSS specs)

5) Compile

cl -Ic:\java\jdk\include -Ic:\java\jdk\include\win32 -MD -LD IFXTPMJNIProxy.c -FeIFXTPMJNIProxy.dll -linkifxtpm.lib

(Using ifxtpm.lib created beforehand.)
(Substitute the path to your jdk as appropriate.)

Note: The Sun documentation says to include -MD here to make sure that multithreaded library is included.
When I did this, I got an error when the DLL was loading (missing MSVCR80.dll).
So I'm just removing it and adding "synchronized" on the Java methods to make sure
that only one thread calls this at a time.

Note 2: When I compiled it with the -MD option with VC 2003 (instead of VC 2005), and it
ran without looking for MSVCR80.dll.


6) Copy IFTPMJNIProxy.dll to tpmj/lib

