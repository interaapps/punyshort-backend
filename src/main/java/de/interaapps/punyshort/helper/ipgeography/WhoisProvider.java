package de.interaapps.punyshort.helper.ipgeography;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class WhoisProvider {
    public static String getWhois(String ipRegistry, String ip) {
        StringBuilder result = new StringBuilder();
        try (Socket socket = new Socket(ipRegistry, 43)) { // IANA WHOIS server
            // Send the IP address to the WHOIS server
            OutputStream out = socket.getOutputStream();
            PrintWriter writer = new PrintWriter(out, true);
            writer.println(ip);

            // Read the response from the server
            InputStream in = socket.getInputStream();
            Scanner scanner = new Scanner(in);
            while (scanner.hasNextLine()) {
                result.append(scanner.nextLine()).append("\n");
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
        return result.toString();
    }

    public static void main(String[] args) {
        String ip = "";  // Replace with your IP
        /*
        String whoisData = getWhois("whois.arin.net", ip);
        System.out.println("WHOIS data for " + ip + ":\n" + whoisData);
         */

        IPGeographyProvider ipGeographyProvider = new IPLocateProvider();
        System.out.println(ipGeographyProvider.fetch(ip));
    }
}
