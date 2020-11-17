package de.codecentric.mule;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;

class TextForwarder extends Thread {
    private InputStream inputStream;
	private PrintStream destination;

    /**
     * @param inputStream Stream to read.
     * @param destination To which stream the text is forwarded
     * @param name Thread name.
     */
    TextForwarder(InputStream inputStream, PrintStream destination, String name) {
        super(name);
        this.inputStream = inputStream;
        this.destination = destination;
    }

    @Override
	public void run() {
        BufferedReader rd = new BufferedReader(new InputStreamReader(inputStream));
        String line = null;
        try {
            line = rd.readLine();
        }
        catch (IOException e) {
            // ignore
        }
        try {
            while (line != null) {
                destination.println(line);
                line = rd.readLine();
            } // while

            rd.close();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}