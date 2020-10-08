package de.codecentric.mule;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

public class FileSynchronizer {
	private Configuration config;
	private SynchronizeUtil util;

	public FileSynchronizer(Configuration config) throws IOException {
		this.config = config;
		util = new SynchronizeUtil();
		util.addToExpected(config.getServerApps());
	}

	public void synchronizeApplications() throws Exception {
		for (String appName : config.getApplications()) {
			syncFiles(appName);
		}
		util.deleteUnexpectedNodes(config.getServerApps());
	}
	
	public void syncFiles(String application) throws Exception {
		File workspaceAppDir = config.getWorkspaceApp(application);
		File workspaceAppTargetDir = new File(workspaceAppDir, "target");
		File muleAppDir = config.getServerMuleApp(application);
		runMavenWhenPomIsChangedOrMissing(workspaceAppDir, muleAppDir, application);

		// class files (and resources) to application root directory
		util.syncFileOrDirectory(new File(workspaceAppTargetDir, "classes"), muleAppDir);
		// the repository with all the jars
		util.syncFileOrDirectory(new File(workspaceAppTargetDir, "repository"), new File(muleAppDir, "repository"));
		// META-INF
		util.syncFileOrDirectory(new File(workspaceAppTargetDir, "META-INF"), new File(muleAppDir, "META-INF"));
				
		handleAnchorFile(application, util.haveDectectedChanges());
	}
	
	private void runMavenWhenPomIsChangedOrMissing(File workspaceAppDir, File muleAppDir, String application) throws Exception {
		File workspacePom = new File(workspaceAppDir, "pom.xml");
		File muleAppDirPom = new File(new File(new File(new File(muleAppDir, "META-INF"), "mule-src"), application), "pom.xml");
		boolean haveToRunMavenBuild = !muleAppDirPom.exists();
		haveToRunMavenBuild = haveToRunMavenBuild || workspacePom.lastModified() > muleAppDir.lastModified();
		if (haveToRunMavenBuild) {
			runMaven(workspaceAppDir);
		}
	}


	private void runMaven(File workspaceAppDir) throws Exception {
		String[] cmdarray = new String[] { config.getMavenExecutable(), "clean", "package", "-DskipTests=true" };
		Process p = Runtime.getRuntime().exec(cmdarray, null /* String[] env, null defaults to env of current VM */,
				workspaceAppDir /* working dir */);
		TextForwarder stderr = new TextForwarder(p.getErrorStream(), System.err, "stderr");
		stderr.start();
		TextForwarder stdout = new TextForwarder(p.getInputStream(), System.out, "stdout");
		stdout.start();
		int exitCode = p.waitFor();
		stdout.join();
		stderr.join();
		if (exitCode != 0) {
			System.out.println("exit code of Maven: " + exitCode);
		}
	}

	private void handleAnchorFile(String application, boolean touch) throws IOException {
		String name = application + "-anchor.txt";
		File anchorFile = new File(config.getServerApps(), name);
		if (touch) {
			final String content = "Delete this file while Mule is running to remove the artifact in a clean way.";
			if (anchorFile.isFile()) {
				anchorFile.setLastModified(System.currentTimeMillis());
			} else {
				Writer w = new FileWriter(anchorFile);
				w.append(content);
				w.close();
			}
		}
		util.addToExpected(anchorFile);
	}
}
