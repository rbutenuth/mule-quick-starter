package de.codecentric.mule;

import java.net.Socket;

public class MuleStopper {

	public static void main(String[] args) throws Exception {
		Socket s = new Socket("localhost", 4712);
		s.close();
	}
}
