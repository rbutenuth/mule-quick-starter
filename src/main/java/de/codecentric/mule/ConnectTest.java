package de.codecentric.mule;

import java.net.Socket;

public class ConnectTest {

	public static void main(String[] args) throws Exception {
		Socket s = new Socket("localhost", 57310);
		s.close();
		System.out.println("connected to 57310");
	}

}
