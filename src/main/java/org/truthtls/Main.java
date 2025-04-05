package org.truthtls;

public class Main {
    public static void main(String[] args) {
        System.out.println("TruthTLS - Starting TLS client...");
        
        Client client = new Client();
        client.connect();
    }
}