## Installation

These installation instructions assume that Git is already installed, configured, and working properly.

### 1. Determine Install Location

VersionOne Integration for Git can be installed on any server with network access to both VersionOne and Git. Exact placement should be determined by your internal software management requirements.

### 2. Extract Files

Download VersionOne Integration for Git and extract it into a folder of your choice.

### 3. Configure

Instructions for configuring V1Git are located below in the Configuration section.

### 4. Start integration
Open up the command prompt, navigate to your installation folder, and run the following command:

```
RunGitIntegration.bat
```

You should see output similar to the following:

```
2011-06-09 16:35:59 GitIntegration [INFO] Git integration service is starting.
2011-06-09 16:35:59 GitIntegration [INFO] Loading configuration...
2011-06-09 16:35:59 GitIntegration [INFO] Configuration loaded.
2011-06-09 16:35:59 GitIntegration [INFO] Creating service...
2011-06-09 16:36:08 GitIntegration [INFO] Connection to VersionOne server established.
2011-06-09 16:36:08 GitIntegration [INFO] Initialize Git Service
2011-06-09 16:36:08 GitIntegration [INFO] Clone Repository
2011-06-09 16:36:08 GitIntegration [INFO] Fetch Repository
2011-06-09 16:37:45 GitIntegration [INFO] Connection to Git server established.
2011-06-09 16:37:45 GitIntegration [INFO] Service created.
2011-06-09 16:37:45 GitIntegration [INFO] Processing new changes...
2011-06-09 16:37:45 GitIntegration [INFO] Fetch Repository
2011-06-09 16:37:47 GitIntegration [INFO] Completed.
```

The last 3 lines are repeated each time the integration checks the repository for changes.

### 5. Test the integration

To ensure the integration is working, commit and push a	change to your Git repository that includes a VersionOne identifier (i.e. `S-01001 testing the git integration`). The next time the integration polls Git, you should see something similar	to the following:

```
2011-06-09 16:45:45 GitIntegration [INFO] Fetch Repository
2011-06-09 16:45:47 GitIntegration [INFO] Changeset ChangeSet:263587:733085 by Jerry Odenwelder on Thu Jun 09 16:45:32 EDT 2011 was saved.
2011-06-09 16:45:48 GitIntegration [INFO] Completed.
```

### 6. Install as a Windows Service

The download package contains a sub-directory called `service`. This directory contains the batch files necessary to install and uninstall the service depending on your platform.

* Use InstallService_x32.bat to install the integration as a service on a 32 bit Windows operating system.
* Use InstallService_x64.bat to install the integration as a service on a 64 bit Windows operating system.

The service is installed to run under the `Local Service` account. Local Service must be given access privileges to the	directory where VersionOne Integration for Git was installed so the integration can store its state and write log files. Follow the steps below to change the security on the installation directory:

* Right click the installation folder from Windows Explorer.
* Select `properties`.
* Select the `Security` tab.
* Click the `Add` button.
* Enter `Local Service` and click `OK`.
* Click the `Allow` checkbox for the `Full Control` row .
* Click `OK` to save the changes.
