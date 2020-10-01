package de.codecentric.mule;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Configuration {
	private OperatingSystem os;
	private File workspaceDir;
	private File muleHome;
	private List<String> applications;
	private boolean run;
	private boolean withDebug;
	private int port;
	private boolean stop;
	private boolean kill;

	public Configuration(String[] args) throws IOException {
		os = OperatingSystem.determineOperatingSystem();
		applications = new ArrayList<>();
		workspaceDir = new File("..").getAbsoluteFile().getCanonicalFile();
		muleHome = new File(new File(workspaceDir, ".."), "mule-enterprise-standalone-4.3.0").getCanonicalFile();
		port = 4712;
		int i = 0;
		while (i < args.length) {
			if ("-m".equals(args[i])) {
				muleHome = directory(args, i);
				i += 2;
			} else if ("-w".equals(args[i])) {
				workspaceDir = directory(args, i);
				i += 2;
			} else if ("-r".equals(args[i])) {
				run = true;
				i++;
			} else if ("-d".equals(args[i])) {
				withDebug = true;
				i++;
			} else if ("-p".equals(args[i])) {
				port = parseInteger(args, i);
				i += 2;
			} else if ("-k".equals(args[i])) {
				kill = true;
				i++;
			} else if ("-s".equals(args[i])) {
				stop = true;
				i++;
			} else {
				applications.add(args[i]);
				i++;
			}
		}
		checkApplicationDirectories();
		if (withDebug) {
			run = true;
		}
		if (stop) {
			run = false;
			withDebug = false;
		}
	}

	public boolean isRun() {
		return run;
	}

	public boolean isWithDebug() {
		return withDebug;
	}

	public boolean isStop() {
		return stop;
	}

	public boolean isKill() {
		return kill;
	}

	public int getPort() {
		return port;
	}

	public File getWorkspaceDir() {
		return workspaceDir;
	}

	public File getMuleHome() {
		return muleHome;
	}

	public List<String> getApplications() {
		return applications;
	}

	public List<String> getMuleCommandWithArguments() {
		List<String> cmdList = new ArrayList<>();
		cmdList.add(getWrapperPath());
		cmdList.add("-c");
		cmdList.add(getWrapperConfPath());
		cmdList.add("set.MULE_APP='mule_ee'");
		cmdList.add("set.MULE_APP_LONG='Mule Enterprise Edition'");
		cmdList.add("set.MULE_HOME=" + getMuleHomePath() + "");
		cmdList.add("set.MULE_BASE=" + getMuleHomePath() + "");
		cmdList.add("set.MULE_LIB=");
		cmdList.add("wrapper.working.dir=" + getMuleHomeBinPath());
		if (withDebug) {
			cmdList.add("wrapper.app.parameter.1=-debug");
		}
		return cmdList;
	}

	/**
	 * @param application Name of application (directory name in workspace)
	 * @return workspace/application
	 */
	public File getWorkspaceApp(String application) {
		return file(workspaceDir, application);
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
		return path(getMuleHome(), "lib", "boot", "exec", os.getWrapperExecutable());
	}

	public String getMavenExecutable() {
		return os.getMavenExecutable();
	}

	public String getWrapperConfPath() {
		return path(getMuleHome(), "conf", "wrapper.conf");
	}

	private String path(File base, String... elements) {
		return file(base, elements).getAbsolutePath().toString();
	}

	private int parseInteger(String[] args, int i) {
		checkForArgument(args, i);
		return Integer.parseInt(args[i + 1]);
	}

	private File file(File base, String... elements) {
		File f = base;
		for (String e : elements) {
			f = new File(f, e);
		}
		return f;
	}

	private File directory(String[] args, int i) {
		checkForArgument(args, i);
		File d = new File(args[i + 1]);
		if (!d.isDirectory()) {
			throw new IllegalArgumentException("Directory " + d.getAbsolutePath() + " does not exist.");
		}
		return d;
	}

	private void checkForArgument(String[] args, int i) {
		if (i + 1 >= args.length) {
			throw new IllegalArgumentException("Missing argument for " + args[i]);
		}
	}

	private void checkApplicationDirectories() {
		for (String application : applications) {
			File appDir = new File(workspaceDir, application);
			if (!appDir.isDirectory()) {
				throw new IllegalArgumentException("Not an application directory: " + appDir.getAbsolutePath());
			}
			File artifactJson = new File(appDir, "mule-artifact.json");
			if (!artifactJson.isFile()) {
				throw new IllegalArgumentException(
						"mule-artifact.json missing in directory: " + appDir.getAbsolutePath());
			}
		}
	}
}
