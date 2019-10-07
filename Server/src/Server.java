import java.net.*;
import java.io.*;


public class Server {
    int sPort = 8000;    //The server will be listening on this port number
    ServerSocket serverSocket;   //serversocket used to listen on port number 8000
    Socket localSocket = null; //socket for the connection with the client
    String message;    //message received from the client
    String MESSAGE;    //uppercase message send to the client
    String ListOfDir;
    ObjectOutputStream objectOutputStream;  //stream write to the socket
    ObjectInputStream objectInputStream;    //stream read from the socket
    FileInputStream fileInputStream = null;
    BufferedInputStream bufferedInputStream = null;
    OutputStream outputStream = null;
    public static void main(String args[]) {
        Server server = new Server();
        server.run();
    }
    //public void Server() {}
    void run() {
        try {
            final String pathOfFileServer = "F:\\UF Acad\\Sem 1\\Computer Networks\\Project\\FTPServer\\";
            serverSocket = new ServerSocket(sPort, 10);										//create a serversocket
            System.out.println("Waiting for connection");								//Wait for connection
            localSocket = serverSocket.accept();												//accept a connection from the client
            System.out.println("Connection received from " + localSocket.getInetAddress().getHostName());
            objectOutputStream = new ObjectOutputStream(localSocket.getOutputStream());					//initialize Input and Output streams
            objectOutputStream.flush();
            objectInputStream = new ObjectInputStream(localSocket.getInputStream());

            try {
                while(true) {
                    message = (String) objectInputStream.readObject();							//receive the message sent from the client
                    System.out.println("Receive message: " + message);			//show the message to the user
                    String [] inputParamArr = message.split("\\s");
                    if(message.toString().toLowerCase().equals("dir")){

                        MESSAGE = "\nPlease find the list of files on the server below - \n";
                        final File folder = new File(pathOfFileServer);
                        for (final File fileEntry : folder.listFiles()) {
                            System.out.println(fileEntry.getName());
                            MESSAGE += fileEntry.getName() + "\n";
                        }
                    }
                    else if(inputParamArr[0].toLowerCase().equals("get")){
                        String filePath = pathOfFileServer + inputParamArr[1];
                        try {
                            // send file
                            File myFile = new File (filePath);
                            byte [] mybytearray  = new byte [(int)myFile.length()];
                            fileInputStream = new FileInputStream(myFile);
                            bufferedInputStream = new BufferedInputStream(fileInputStream);
                            bufferedInputStream.read(mybytearray,0,mybytearray.length);
                            outputStream = localSocket.getOutputStream();
                            System.out.println("Sending " + filePath + "(" + mybytearray.length + " bytes)");
                            outputStream.write(mybytearray,0,mybytearray.length);
                            outputStream.flush();
                            System.out.println("Done.");
                        }
                        finally {
                            if (bufferedInputStream != null) bufferedInputStream.close();
                            if (outputStream != null) outputStream.close();
                            //if (sock!=null) sock.close();
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
                objectInputStream.close();
                objectOutputStream.close();
                serverSocket.close();
            }
            catch(IOException ioException){
                ioException.printStackTrace();
            }
        }
    }

    void sendMessage(String msg) {										//send a message to the output stream
        try{
            objectOutputStream.writeObject(msg);
            objectOutputStream.flush();
            System.out.println("Send message: " + msg);
        }
        catch(IOException ioException){
            ioException.printStackTrace();
        }
    }
}
