


import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.security.MessageDigest;
import java.util.*;

public class Server {
    int sPort = 8000;    //The server will be listening on this port number
    ServerSocket sSocket;   //serversocket used to listen on port number 8000
    Socket connection = null; //socket for the connection with the client
    String message;    //message received from the client
    String MESSAGE;    //uppercase message send to the client
    String ListOfDir;
    ObjectOutputStream out;  //stream write to the socket
    ObjectInputStream in;    //stream read from the socket

    public static void main(String args[]) {
        Server s = new Server();
        s.run();
    }
    //public void Server() {}
    void run() {
        try {

            sSocket = new ServerSocket(sPort, 10);										//create a serversocket
            System.out.println("Waiting for connection");								//Wait for connection
            connection = sSocket.accept();												//accept a connection from the client
            System.out.println("Connection received from " + connection.getInetAddress().getHostName());
            out = new ObjectOutputStream(connection.getOutputStream());					//initialize Input and Output streams
            out.flush();
            in = new ObjectInputStream(connection.getInputStream());

            try {
                while(true) {
                    message = (String)in.readObject();							//receive the message sent from the client
                    System.out.println("Receive message: " + message);			//show the message to the user

                    if(message.toString().toLowerCase().equals("dir")){
                        final String pathOfFileServer = "F:\\UF Acad\\Sem 1\\Computer Networks\\Project\\FTPServer";
                        MESSAGE = "\nPlease find the list of files on the server below - \n";
                        final File folder = new File(pathOfFileServer);
                        for (final File fileEntry : folder.listFiles()) {
                            System.out.println(fileEntry.getName());
                            MESSAGE += fileEntry.getName() + "\n";
                        }
                    }
                    else{
                        MESSAGE = message.toUpperCase();		//Capitalize all letters in the message
                    }
                    sendMessage(MESSAGE);					//send MESSAGE back to the client




                }
            }
            catch(ClassNotFoundException classnot){
                System.err.println("Data received in unknown format");
            }
        }
        catch(IOException ioException){
            ioException.printStackTrace();
        }
        finally{
            try{									//Close connections
                in.close();
                out.close();
                sSocket.close();
            }
            catch(IOException ioException){
                ioException.printStackTrace();
            }
        }
    }

    void sendMessage(String msg) {										//send a message to the output stream
        try{
            out.writeObject(msg);
            out.flush();
            System.out.println("Send message: " + msg);
        }
        catch(IOException ioException){
            ioException.printStackTrace();
        }
    }
}
