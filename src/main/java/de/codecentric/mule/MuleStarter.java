package de.codecentric.mule;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class MuleStarter {

	public static void main(String[] args) throws Exception {
		Configuration config = new Configuration(args);

		if (config.isStop()) {
			try {
				Socket s = new Socket("localhost", config.getPort());
				s.close();
			} catch (IOException e) {
				System.out.println("Stopping via socket connect failed: " + e.getMessage());
			}
		}

		if (config.isKill()) {
			try {
				Runtime.getRuntime().exec(config.getKillCommand());
			} catch (Exception e) {
				System.out.println("Kill via command failed: " + e.getMessage());
			}
		}

		if (config.isSynchronize()) {
			// For all applications, check is a Maven build is necessary, after that synchronize files/directories
			FileSynchronizer synchronizer = new FileSynchronizer(config);
			if (config.isUpdate()) {
				addDeployedApps(config);
			}
			synchronizer.synchronizeApplications();
		}

		if (config.isRun()) {
			startMule(config);
		}
	}

	private static void addDeployedApps(Configuration config) {
		final String ANCHOR = "-anchor.txt";
		File appDir = config.getServerApps();
		for (String name : appDir.list()) {
			if (name.endsWith(ANCHOR)) {
				String appName = name.substring(0, name.length() - ANCHOR.length());
				config.addApplication(appName);
			}
		}
	}

	private static void startMule(Configuration config) throws Exception {
		String[] cmdarray = config.getMuleCommandWithArguments().toArray(new String[0]);
		
		Process p = Runtime.getRuntime().exec(cmdarray, null /* String[] env, null defaults to env of current VM */,
				config.getMuleHome() /* working dir */);
		TextForwarder stderr = new TextForwarder(p.getErrorStream(), System.err, "stderr");
		stderr.start();
		TextForwarder stdout = new TextForwarder(p.getInputStream(), System.out, "stdout");
		stdout.start();

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				p.destroy();
			}

		});
		waitOnSocketForShutdown(config);
		p.destroy();
		int exitCode = p.waitFor();
		stdout.join();
		stderr.join();
		if (exitCode != 0) {
			System.out.println("exit code of Mule: " + exitCode);
		}
	}

	private static void waitOnSocketForShutdown(Configuration config) throws IOException {
		System.out.println("Wait for socket connection on port " + config.getPort());
		ServerSocket socket = new ServerSocket(config.getPort());
		Socket s = socket.accept();
		s.close();
		socket.close();
	}
}
