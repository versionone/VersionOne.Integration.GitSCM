## Installation

The following instructions use examples for Windows. The service is also expected to run under Linux and OSX. The instructions should be sufficiently similar for those familiar with those operating systems.

### 1. Determine Install Location

VersionOne Integration for Git can be installed on any server with network access to both VersionOne and Git. Exact placement should be determined by your internal software management requirements.

### 2. Extract Files

Download VersionOne Integration for Git and extract it into a folder of your choice.

### 3. Configure

Instructions for configuring VersionOne Integration for Git are located in the [Configuration](configuration.html) section. The default configuration provided with the integration is a working sample. Provided the integration server can reach the VersionOne SaaS environment and GitHub, you can proceed to test the integration prior to configuration for the local environment.

### 4. Start integration

To test the integration the first time, we suggest running it in console mode. Open a command prompt window. Navigate to your installation folder and find the `bin` directory. Run the following command:

```
v1git.bat console
```

The following will appear for a first time run using the sample configuration:

```
wrapper  | --> Wrapper Started as Console
wrapper  | Launching a JVM...
jvm 1    | Wrapper (Version 3.2.3) http://wrapper.tanukisoftware.org
jvm 1    |   Copyright 1999-2006 Tanuki Software, Inc.  All Rights Reserved.
jvm 1    |
jvm 1    | 2013-07-30 08:59:45 GitIntegration [INFO] Git integration service is starting...
jvm 1    | 2013-07-30 08:59:45 GitIntegration [INFO] Loading configuration...
jvm 1    | 2013-07-30 08:59:45 GitIntegration [INFO] Configuration loaded successfully
jvm 1    | 2013-07-30 08:59:45 GitIntegration [INFO] Connecting to VersionOne as 'admin'...
jvm 1    | 2013-07-30 08:59:49 GitIntegration [INFO] Connection to VersionOne server established successfully
jvm 1    | 2013-07-30 08:59:49 GitIntegration [WARN] ./repos couldn't be reset, possibly due to this being the first time the service has been run
jvm 1    | 2013-07-30 08:59:49 GitIntegration [INFO] Creating 3 Git service(s)...
jvm 1    | 2013-07-30 08:59:52 GitIntegration [INFO] Initializing Git Service for git://github.com/edkennard/Integration.Git.Test1.git...
jvm 1    | 2013-07-30 08:59:52 GitIntegration [INFO] Connection to Git repository established successfully
jvm 1    | 2013-07-30 08:59:53 GitIntegration [INFO] Initializing Git Service for git://github.com/edkennard/Integration.Git.Test2.git...
jvm 1    | 2013-07-30 08:59:53 GitIntegration [INFO] Connection to Git repository established successfully
jvm 1    | 2013-07-30 08:59:53 GitIntegration [INFO] Initializing Git Service for git://github.com/edkennard/Integration.Git.Test3.git...
jvm 1    | 2013-07-30 08:59:53 GitIntegration [INFO] Connection to Git repository established successfully
jvm 1    | 2013-07-30 08:59:53 GitIntegration [INFO] Git services created successfully
```

The output will proceed as the integration checks for new changes. With the sample configuration, you may see some warnings since the workitems mentioned in the sample GitHub repository do not have corresponding items in VersionOne. This is simply a test for normal error handling.

If you did not configure the integration with your own settings, you will need to do so at this time. Simple press `Control-C` to halt the integration. Edit the `conf\configuration.xml` per instructions in [Configuration](configuration.html). Restart the integration to proceed with testing.

### 5. Test the integration

To ensure the integration is working, commit and push a	change to your Git repository that includes a VersionOne identifier (i.e. `S-01001 testing the git integration`). The next time the integration polls Git, you should see something similar	to the following:

```
jvm 1    | 2013-07-30 08:59:53 GitIntegration [INFO] Checking for new changes...
jvm 1    | 2013-07-30 08:59:53 GitIntegration [INFO] Checking git://github.com/edkennard/Integration.Git.Test2.git
jvm 1    | 2013-07-30 08:59:57 GitIntegration [INFO] Using existing ChangeSet:11691 for commit 711b80fa0afc48e1810423cf2423e077d09994d4
jvm 1    | 2013-07-30 08:59:57 GitIntegration [INFO] Saved ChangeSet:11691:20725 to D-01001 for commit 711b80fa0afc48e1810423cf2423e077d09994d4 by Ed Kennard on Thu Nov 01 21:21:35 EDT 2012 successfully
```

### 6. Install as a Windows Service

To install the integration as a service, run the following:

```
v1git install
```

The service is installed to run under the `Local Service` account. `Local Service` must be given access privileges to the directory where VersionOne Integration for Git was installed so the integration can store its state and write log files. Follow the steps below to change the security on the installation directory:

* Right click the installation folder from Windows Explorer.
* Select `properties`.
* Select the `Security` tab.
* Click the `Add` button.
* Enter `Local Service` and click `OK`.
* Click the `Allow` checkbox for the `Full Control` row .
* Click `OK` to save the changes.
