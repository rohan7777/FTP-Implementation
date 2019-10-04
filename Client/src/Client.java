import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

public class Client {
    final String pathOfFileClient = "F:\\UF Acad\\Sem 1\\Computer Networks\\Project\\FTPClient\\";
    Socket requestSocket;           //socket connect to the server
    ObjectOutputStream out;         //stream write to the socket
    ObjectInputStream in;          //stream read from the socket
    String message;                //message send to the server
    String MESSAGE;                //capitalized message read from the server ss
    String fileSavePath;
    FileInputStream fis = null;
    BufferedInputStream bis = null;
    OutputStream os = null;
    FileOutputStream fos = null;
    BufferedOutputStream bos = null;
    int current,bytesRead;
    public static void main(String args[]) {
        Client client = new Client();
        client.run();
    }

    //public void Client() {}

    void run() {
        try{
            //create a socket to connect to the server
            requestSocket = new Socket("localhost", 8000);
            System.out.println("Connected to localhost in port 8000");
            //initialize inputStream and outputStream
            out = new ObjectOutputStream(requestSocket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(requestSocket.getInputStream());

            //get Input from standard input
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
            while(true)	{
                System.out.print("Hello, please input a sentence: ");
                message = bufferedReader.readLine();				//read a sentence from the standard input
                sendMessage(message); 								//Send the sentence to the server
                String[] inputCommand = message.split("\\s");
                if(inputCommand[0].toLowerCase().equals("get")){
                    message = inputCommand[0]+"<>"+inputCommand[1];
                    fileSavePath = pathOfFileClient + inputCommand[1];
                    try {


                        // receive file
                        byte [] mybytearray  = new byte [999999999];
                        InputStream is = requestSocket.getInputStream();
                        fos = new FileOutputStream(fileSavePath);
                        bos = new BufferedOutputStream(fos);
                        bytesRead = is.read(mybytearray,0,mybytearray.length);
                        current = bytesRead;

                        do {
                            bytesRead =
                                    is.read(mybytearray, current, (mybytearray.length-current));
                            if(bytesRead >= 0) current += bytesRead;
                        } while(bytesRead > -1);

                        bos.write(mybytearray, 0 , current);
                        bos.flush();
                        System.out.println("File " + fileSavePath
                                + " downloaded (" + current + " bytes read)");
                    }
                    finally {
                        if (fos != null) fos.close();
                        if (bos != null) bos.close();
                        //if (sock != null) sock.close();
                    }

                }

                MESSAGE = (String)in.readObject();					//Receive the upperCase sentence from the server
                System.out.println("Receive message: " + MESSAGE);	//show the message to the user
            }
        }
        catch (ConnectException e) {
            System.err.println("Connection refused. You need to initiate a server first.");
        }
        catch ( ClassNotFoundException e ) {
            System.err.println("Class not found");
        }
        catch(UnknownHostException unknownHost){
            System.err.println("You are trying to connect to an unknown host!");
        }
        catch(IOException ioException){
            ioException.printStackTrace();
        }
        finally{													//Close connections
            try{
                in.close();
                out.close();
                requestSocket.close();
            }
            catch(IOException ioException){
                ioException.printStackTrace();
            }
        }
    }

    void sendMessage(String msg) {									//send a message to the output stream
        try{														//stream write the message
            out.writeObject(msg);
            out.flush();
            System.out.println("Send message: " + msg);
        }
        catch(IOException ioException){
            ioException.printStackTrace();
        }
    }
}
