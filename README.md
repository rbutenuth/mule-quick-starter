# Mule Quick Starter

A tool for faster startup of Mule applications from AnypointStudio. 

# Introduction

With the switch from AnypointStudio 6 (Mule 3) to AnypointStudio 7 (Mule 4), MuleSoft dropped the feature to start a Mule application without Maven.
That's a pity because the direct start - without Maven - is a lot faster. When you start a Mule 4 application from AnypointStudio 7 and have a look 
at the console output with a stop watch in your hand, you see nearly half of the time is needed by the Maven build, before the server even begins
to start. 

What's going on behind the scenes? The Maven build creates a jar from your application. The jar contains all your Mule configuration files (src/main/mule), 
resources (src/main/resources), compiled Java classes and all referenced (libraries, connectors). Usually the configuration files, resources, and classes
are just a few K bytes, but the jars often total to 100 MBytes. Even when you use a fast development machine (with SSD), this takes some time.

The big jar is copied to the server. After the server has done it's initial startup, it begins to deploy all applications by unpacking that jar, 
deleting it and mark the deployed application by creating of an anchor file. So the big jar is created, copied, and unpacked. Some opportunities
to speed things up a little bit.

So what is really needed? When you deploy an application the first time, you need the full Maven build. Maven does not only create the final jar,
before it packs is, it collects everything for the jar in the target directory of the application. When you develop your application, you will change
the xml files and resources quite often. When you use Java, the same is true for the Java source files. In all these cases, Maven is not needed.
The build mechanism of Eclipse will to the job: All resources will be copied to the target, Java sources will be compiled and the resulting class
files will be copied to the target directory.

A Maven build is only necessary when you add or remove a dependency by changing the pom.xml file. So why not omit the Maven build in these cases?
That's the idea for the Mule Quick Starter: It does only the steps necessary - updating changed files from the workspace to the Mule server - before
starting a Mule server installed on your local disk (or SSD). It would be great to have this feature as part of the AnypointStudio (I proposed this
at Connect in London fall 2019), but unfortunately so far we don't have this feature. So I built this small tool. The following paragraphs describe
the typical use cases.

# Usage

You can add the project to your workspace (just clone the git repository) or you can build the jar and start it from the command line. It has one
main class (de.codecentric.mule.MuleStarter) and is configured by command line parameters and environment variables. The variables are:

* MULE_HOME: Directory where your Mule server is installed (don't forget to install the license when using the enterprise version).
  You can override this with the command line option -m followed by the directory. The default is ../../mule-enterprise-standalone-4.3.0, which
  will work when you install your server as a sibling of your workspace.
* MULE_WORKSPACE: Directory of the workspace you are using within AnypointStudio. You can override this with the command line option -w followed
  by the directory. The default is ../, which will work when you start from AnypointStudio.

The server is not started via the Tanuki service wrapper. When the wrapper detects a pipe instead of a console, it will not flush your log 
output immediately (to increase performance). When you want do see the log output immediately, add the line

```
wrapper.console.flush=true
```

to the wrapper.conf file of your server. 

## Copying applications and start the server

When you want to start one or more Mule applications from AnypointStudio, use the options -c to copy / update the applications from the workspace
to the apps directory of the server. Next is -r to run the server. When you do it the first time, it will take the same time as when you start
with the default run configuration from AnypointStudio because a Maven build will be run. But this will be ommited on further starts, making
them faster.

Be careful to add a list of all applications you want to start at the end of the command line. Applications not listed there will be deleted
from the apps directory!.

## Stopping the server

Don't stop the server by clicking on the red square in AnypointStudio. This will kill the JDK with the MuleStarter, but not the Tanukis service
wrapper and the Mule server. Unfortunately AnypointStudio does a hard kill on the Java process, so there is no chance to run finalizer threads
to kill the service wrapper. To stop, call the starter class with the option -s. This will do a connect on a socket of the starter which 
does a clean stop of the Mule server. (The port is configurable by -p, default is 4712.) You can add the -s option to your start configuration,
in this case a still running server will be stopped before the new one will be started.

In case the -s does not work for some reason, you have to kill the service wrapper with tools of your operating system (Windows: Task manager,
Linux: kill/pkill). Or just use the starter with the option -k, which does the same.
  
## Redeploy changes from the workspace

You don't want to start the server for every small change. This is not necessary, as Mule does support hot deployment. The server checks the 
anchor files of the applications every few seconds, on changes of the anchor file it will redeploy the application. With the option -u the
MuleStarter will update all files from the workspace to server apps directory. This is similar to the -c option, but works without giving
a list of all applications: It will update all applications specified on the command line and all already deployed applications. The
list of deployed applications is determined by looking for anchor files. 

## Debugging

I already added support for debugging, but currently it is not usable. MuleSoft has documented how to start a Mule server for remote
debugging, it worked for Mule 3, but it does not work for Mule 4. I already opened a support case, but currently I wait for an answer.
So the -d switch is useless so far.

## Summary of command line options

* -w <workspace-directory>, default is ../ or value of the environment variable MULE_WORKSPACE (if set)
* -m <mule-home-directory>, default is ../../mule-enterprise-standalone-4.3.0/ or value of the environment variable MULE_HOME (if set).
* -r run Mule applications
* -d debug Mule applications (does currently not work)
* -p port for communicating with the starter for clean termination, default 4712
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

Currently this works/has been tested on Windows and Linux. I have no Apple computer to test it there, you only have to adapt the path to the service
wrapper, the pkill command, and the way how to call Maven. You will find all of this in the class OperatingSystem. (I am waiting for pull request.)

# Summary

This whole tool is just a workaround. I would be glad if it is no longer necessary because MuleSoft adds the functionality to AnypointStudio 7. 
It has its quirks, and currently debugging is not possible. But it may make your development a little bit faster by saving time for deployments.

