package de.codecentric.mule;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class MuleStarter {

	public static void main(String[] args) throws Exception {
		Configuration config = new Configuration(args);
		System.out.println("workspace: " + config.getWorkspaceDir().getAbsolutePath());
		System.out.println("Mule home: " + config.getMuleHome().getAbsolutePath());
		System.out.println();

		// For all applications, check is a Maven build is necessary, after that synchronize files/directories
		FileSynchronizer synchronizer = new FileSynchronizer(config);
		synchronizer.synchronizeApplications();
		

//		startMule(config);
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
		waitOnSocketForShutdown();
		p.destroy();
		int exitCode = p.waitFor();
		stdout.join();
		stderr.join();
		if (exitCode != 0) {
			System.out.println("exit code of Mule: " + exitCode);
		}
	}

	private static void waitOnSocketForShutdown() throws IOException {
		System.out.println("Wait for socket connection on port 4712...");
		ServerSocket socket = new ServerSocket(4712);
		Socket s = socket.accept();
		s.close();
		socket.close();
		System.out.println("Terminating");
	}
}
