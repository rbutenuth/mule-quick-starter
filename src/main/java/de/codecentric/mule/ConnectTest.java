package de.codecentric.mule;

import java.net.Socket;

public class ConnectTest {

	public static void main(String[] args) throws Exception {
		Socket s = new Socket("localhost", 6666);
		s.close();
		System.out.println("connected to 6666");
	}

}
