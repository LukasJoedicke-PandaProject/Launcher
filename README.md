# The Launcher for the program "Panda"
This is the launcher for the "Panda" program. 
  It is used to:
 - Authenticate the user / license
 - Keep the tool up to date
 - Keep the tool secure

## Technology
The cool thing about the launcher is, that the communication between Launcher and API is pretty secure due to OTPs.  

If a user is authenticated, the "Panda" Program will get downloaded from an URL. But this URL can only be accessed
by the Launcher. Because of a cool feature from NGINX: `auth_request`. With this, I can generate OTPs for files on my webspace.

Also, the "Panda" program will never be really be on the machine of the user. It is manipulating the Java SystemClassLoader,
to load the program dynamically into memory and execute it from there.