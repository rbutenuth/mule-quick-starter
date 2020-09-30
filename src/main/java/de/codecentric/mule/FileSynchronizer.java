package de.codecentric.mule;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

public class FileSynchronizer {
	private Configuration config;

	public FileSynchronizer(Configuration config) {
		this.config = config;
	}

	// repository/ -> repository/
	// META-INF/ -> META-INF

	public void syncFiles(String application) throws IOException {
		File appTargetDir = config.getWorkspaceAppTarget(application);
		File muleAppDir = config.getServerMuleApp(application);
		SynchronizeUtil util = new SynchronizeUtil();

		// class files (and resources) to application root directory
		util.syncFileOrDirectory(new File(appTargetDir, "classes"), muleAppDir);
		// the repository with all the jars
		util.syncFileOrDirectory(new File(appTargetDir, "repository"), new File(muleAppDir, "repository"));
		// META-INF
		util.syncFileOrDirectory(new File(appTargetDir, "META-INF"), new File(muleAppDir, "META-INF"));
				
		if (util.haveDectectedChanges()) {
			touchAnchorFile(application);
		}
	}

	private void touchAnchorFile(String application) throws IOException {
		final String content = "Delete this file while Mule is running to remove the artifact in a clean way.";
		String name = application + "-anchor.txt";
		File anchorFile = new File(config.getServerApps(), name);
		if (anchorFile.isFile()) {
			anchorFile.setLastModified(System.currentTimeMillis());
		} else {
			Writer w = new FileWriter(anchorFile);
			w.append(content);
			w.close();
		}
	}
}
