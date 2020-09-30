package de.codecentric.mule;

import java.io.File;

public class Configuration {
	private File workspaceDir;
	private File muleHome;

	public Configuration(File workspaceDir, File muleHome) {
		this.workspaceDir = workspaceDir;
		this.muleHome = muleHome;
	}

	public File getWorkspaceDir() {
		return workspaceDir;
	}
	
	public File getMuleHome() {
		return muleHome;
	}
	
	/**
	 * @param application Name of application (directory name in workspace)
	 * @return workspace/application
	 */
	public File getWorkspaceAppTarget(String application) {
		return file(workspaceDir, application, "target");
	}
	
	/**
	 * @param application Name of application
	 * @return mule/apps/application
	 */
	public File getServerMuleApp(String application) {
		return file(getServerApps(), application);
	}

	/**
	 * @return The "apps" directory of the Mule server.
	 */
	public File getServerApps() {
		return file(muleHome, "apps");
	}
	
	public String getMuleHomePath() {
		return getMuleHome().getAbsolutePath().toString();
	}

	public String getMuleHomeBinPath() {
		return path(getMuleHome(), "bin");
	}

	public String getWrapperPath() {
		return path(getMuleHome(), "lib", "boot", "exec", "wrapper-windows-x86-64.exe");
	}

	public String getWrapperConfPath() {
		return path(getMuleHome(), "conf", "wrapper.conf");
	}

	private String path(File base, String... elements) {
		return file(base, elements).getAbsolutePath().toString();
	}

	private File file(File base, String... elements) {
		File f = base;
		for (String e : elements) {
			f = new File(f, e);
		}
		return f;
	}
}
