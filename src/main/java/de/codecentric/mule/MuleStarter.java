package de.codecentric.mule;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class MuleStarter {

	public static void main(String[] args) {
		Configuration config = new Configuration(args);

		if (config.isStop()) {
			try (Socket s = new Socket("localhost", config.getPort())) {
				InputStream is = s.getInputStream();
				int b = is.read();
				is.close();
				System.out.println("stopped Mule, value was: " + b);
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
			// For all applications, check is a Maven build is necessary, after that
			// synchronize files/directories
			FileSynchronizer synchronizer = new FileSynchronizer(config);
			if (config.isUpdate()) {
				addDeployedApps(config);
			}
			try {
				synchronizer.synchronizeApplications();
			} catch (IOException e) {
				System.err.println("Synchronization failed: " + e.getMessage());
				System.exit(1);
			}
		}

		if (config.isRun()) {
			try {
				startMule(config);
			} catch (Exception e) {
				System.err.println("Start of Mule server failed: " + e.getMessage());
				System.exit(1);
			}
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
		Map<String, String> env = new HashMap<>(System.getenv());
		if (!env.containsKey("MULE_HOME")) {
			env.put("MULE_HOME", config.getMuleHomePath());
		}
		Process p = Runtime.getRuntime().exec(cmdarray, environment(env), config.getMuleHome() /* working dir */);
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
		waitOnSocketForShutdown(config, p);
		stdout.join();
		stderr.join();
	}

	private static String[] environment(Map<String, String> env) {
		List<String> envList = new ArrayList<>();
		for (Entry<String, String> e : env.entrySet()) {
			envList.add(e.getKey() + "=" + e.getValue());
		}
		return envList.toArray(new String[0]);
	}

	private static void waitOnSocketForShutdown(Configuration config, Process p) throws Exception {
		System.out.println("Wait for socket connection on port " + config.getPort());
		try (ServerSocket socket = new ServerSocket(config.getPort()); Socket s = socket.accept()) {
			p.destroy();
			p.waitFor();
			System.out.println("Mule has stopped.");
			try (OutputStream os = s.getOutputStream()) {
				os.write(42);
				os.flush();
			}
		}
	}
}
