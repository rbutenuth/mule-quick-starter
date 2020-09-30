package de.codecentric.mule;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class MuleStarter {

	public static void main(String[] args) throws Exception {
		Configuration config = new Configuration(new File("C:\\java\\mule\\workspace4"), new File("C:/java/mule/mule-enterprise-standalone-4.3.0"));
		System.out.println(config.getWorkspaceDir().getAbsolutePath());
		System.out.println(config.getMuleHome().getAbsolutePath());
		FileSynchronizer synchronizer = new FileSynchronizer(config);
		synchronizer.syncFiles("bmi-description-system");
		startMule(config);
	}

	private static void startMule(Configuration config) throws Exception {
		String[] cmdarray = new String[] {
				config.getWrapperPath(),
			"-c",	
			config.getWrapperConfPath(), //
			"set.MULE_APP='mule_ee'",	
			"set.MULE_APP_LONG='Mule Enterprise Edition'",	
			"set.MULE_HOME=" + config.getMuleHomePath() + "", //
			"set.MULE_BASE=" + config.getMuleHomePath() + "", //
			"set.MULE_LIB=", // 
			"wrapper.working.dir=" + config.getMuleHomeBinPath()	
			// wrapper.app.parameter.1= 
		};

		for (String s: cmdarray) {
			System.out.println(s);
		}
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
