# Mule Quick Starter

A tool for faster startup of Mule applications from AnypointStudio

## Command line options

* -w <workspace-directory>, default is ../
* -m <mule-home-directory>, default is ../../mule-enterprise-standalone-4.3.0/
* -r run Mule applications
* -d debug Mule applications (does currently not work)
* -p port for communicatingwith the starter for clean termination, default 4712
* -s stop the quick starter by connecting to the port (see -p)
* -k kill the service wrapper via pkill (Linux) / taskkill (Windows)
* -c copy (synchronize) Mule applications from the workspace to the server app directory
* -u Like copy, but list of applications is the union from the command line and the applications already deployed on the server (determined by anchor files)

The options may be followed by a list of application names (the directories in the workspace).
When opton -c is active, these applications will be synchronized from the workspace to the 
apps directory of the server. 

Be careful: All other files in the apps directory (e.g. existing applications) will be deleted.

When the pom.xml of an application has changed or the application does not exist in the apps 
directory, a Maven build is executed.

## Improve log output

You should add the option

```
wrapper.console.flush=true
```

to your wrapper.conf, otherwise the log output of your server is not flushed on every line break.

