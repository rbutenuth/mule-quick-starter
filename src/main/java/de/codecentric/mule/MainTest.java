package de.codecentric.mule;

public class MainTest {

	public static void main(String[] args) throws Exception {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				System.out.println("Shutdown-Hook");
			}
		});
		System.out.println("wait...");
		System.in.read();
	}
}
