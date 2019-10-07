import java.net.*;
import java.io.*;

public class Client {
    final String pathOfFileClient = "F:\\UF Acad\\Sem 1\\Computer Networks\\Project\\FTPClient\\";
    Socket localSocket;           //socket connect to the server
    ObjectOutputStream objectOutputStream;         //stream write to the socket
    ObjectInputStream objectInputStream;          //stream read from the socket
    String message;                //message send to the server
    String MESSAGE;                //capitalized message read from the server ss
    String fileSavePath;
    FileInputStream fileInputStream = null;
    BufferedInputStream bufferedInputStream = null;
    OutputStream outputStream = null;
    FileOutputStream fileOutputStream = null;
    BufferedOutputStream bufferedOutputStream = null;
    int current,bytesRead;
    public static void main(String args[]) {
        Client client = new Client();
        client.run();
    }

    //public void Client() {}

    void run() {
        try{
            //create a socket to connect to the server
            localSocket = new Socket("localhost", 8000);
            System.out.println("Connected to localhost in port 8000");
            //initialize inputStream and outputStream
            objectOutputStream = new ObjectOutputStream(localSocket.getOutputStream());
            objectOutputStream.flush();
            objectInputStream = new ObjectInputStream(localSocket.getInputStream());

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
                        InputStream is = localSocket.getInputStream();
                        fileOutputStream = new FileOutputStream(fileSavePath);
                        bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
                        bytesRead = is.read(mybytearray,0,mybytearray.length);
                        current = bytesRead;

                        do {
                            bytesRead =
                                    is.read(mybytearray, current, (mybytearray.length-current));
                            if(bytesRead >= 0) current += bytesRead;
                        } while(bytesRead > -1);

                        bufferedOutputStream.write(mybytearray, 0 , current);
                        bufferedOutputStream.flush();
                        System.out.println("File " + fileSavePath
                                + " downloaded (" + current + " bytes read)");
                    }
                    finally {
                        if (fileOutputStream != null) fileOutputStream.close();
                        if (bufferedOutputStream != null) bufferedOutputStream.close();
                        //if (sock != null) sock.close();
                    }

                }

                MESSAGE = (String) objectInputStream.readObject();					//Receive the upperCase sentence from the server
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
                objectInputStream.close();
                objectOutputStream.close();
                localSocket.close();
            }
            catch(IOException ioException){
                ioException.printStackTrace();
            }
        }
    }

    void sendMessage(String msg) {									//send a message to the output stream
        try{														//stream write the message
            objectOutputStream.writeObject(msg);
            objectOutputStream.flush();
            System.out.println("Send message: " + msg);
        }
        catch(IOException ioException){
            ioException.printStackTrace();
        }
    }
}
