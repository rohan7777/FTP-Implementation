import java.net.*;
import java.io.*;

public class Server {
    public static void main(String args[]) throws Exception {
        int sPort = 8000;    //The server will be listening on this port number
        ServerSocket serverSocket;   //serversocket used to listen on port number 8000
        Server server = new Server();
        serverSocket = new ServerSocket(sPort, 10);
        int clientNum = 1;
        System.out.println("The server is listening for connections ");
        while (true) {
            try {
                //server.run();
                new Handler(serverSocket.accept(),clientNum).start();
                System.out.println("Client " + clientNum++ + " is connected!");
            } catch (Exception e) {
                System.out.println(e);
            }
        }
    }
    //public void Server() {}

    public static class Handler extends Thread {
        Socket localSocket = null; //socket for the connection with the client
        int clientNumber;
        ObjectOutputStream objectOutputStream;  //stream write to the socket
        ObjectInputStream objectInputStream;    //stream read from the socket
        String message;    //message received from the client
        String MESSAGE;    //uppercase message send to the client

        public Handler(Socket localSocket, int no){
            this.localSocket = localSocket;
            this.clientNumber = no;
        }

        public void run() {
            try {
                final String pathOfFileServer = "F:\\UF Acad\\Sem 1\\Computer Networks\\Project\\FTPServer\\";
                System.out.println("Connection received from " + localSocket.getInetAddress().getHostName());
                InputStream inputStream = localSocket.getInputStream();
                DataInputStream clientData = new DataInputStream(inputStream);
                objectOutputStream = new ObjectOutputStream(localSocket.getOutputStream());                    //initialize Input and Output streams
                objectOutputStream.flush();
                objectInputStream = new ObjectInputStream(localSocket.getInputStream());
                try {
                    while (true) {
                        message = (String) objectInputStream.readObject();                            //receive the message sent from the client
                        String[] inputParamArr = message.split("\\s");
                        final File folder = new File(pathOfFileServer);
                        if (message.toString().toLowerCase().equals("dir")) {
                            System.out.println("Client "+clientNumber + " issued \"dir\" command.");
                            MESSAGE = "\nPlease find the list of files on the server below - \n";
                            for (final File fileEntry : folder.listFiles()) {
                                MESSAGE += fileEntry.getName() + "\n";
                            }
                        } else if (inputParamArr[0].toLowerCase().equals("get")) {
                            System.out.println("Client "+clientNumber + " issued \"get\" command.");
                            String filePath = pathOfFileServer + inputParamArr[1];
                            boolean check = new File(pathOfFileServer, inputParamArr[1]).exists();
                            if (check) {
                                try {
                                    long start = System.currentTimeMillis();
                                    File myFile = new File(filePath);
                                    byte[] mybytearray = new byte[(int) myFile.length()];
                                    //Create IO streams
                                    FileInputStream fileInputStream = new FileInputStream(myFile);
                                    BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
                                    DataInputStream dataInputStream = new DataInputStream(bufferedInputStream);
                                    dataInputStream.readFully(mybytearray, 0, mybytearray.length);
                                    OutputStream outputStream = localSocket.getOutputStream();
                                    //Sending filename and filesize
                                    DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
                                    dataOutputStream.writeLong(mybytearray.length);
                                    dataOutputStream.write(mybytearray, 0, mybytearray.length);
                                    dataOutputStream.flush();
                                    long finish = System.currentTimeMillis();
                                    System.out.println("Done.\nTime taken ->" + Long.toString(finish - start));
                                } catch (Exception e) {
                                    System.err.println(e);
                                }
                            } else {
                                System.out.println("File not found");
                                MESSAGE = "File not found.";
                                sendMessage(MESSAGE);
                            }
                        }else if(inputParamArr[0].toLowerCase().equals("upload")){
                            String fileSavePath = pathOfFileServer + inputParamArr[1];
                            try {
                                //InputStream inputStream = localSocket.getInputStream();
                                DataInputStream dataInputStream = new DataInputStream(inputStream);
                                long fileSize = dataInputStream.readLong();
                                long x = fileSize;
                                String fileName = inputParamArr[1];
                                OutputStream outputStream = new FileOutputStream(fileSavePath);
                                byte[] buffer = new byte[1024];
                                int bytesRead;
                                while (fileSize > 0 && (bytesRead = dataInputStream.read(buffer, 0, (int) Math.min(buffer.length, fileSize))) != -1) {
                                    outputStream.write(buffer, 0, bytesRead);
                                    fileSize -= bytesRead;
                                }
                                outputStream.flush();
                                System.out.println("File " + fileSavePath + " downloaded (" + x + " bytes read)");
                            } finally {
                                /*if (fileOutputStream != null) fileOutputStream.close();
                                if (bufferedOutputStream != null) bufferedOutputStream.close();*/
                            }


                        }
                        else {
                            MESSAGE = message.toUpperCase();        //Capitalize all letters in the message
                        }
                        sendMessage(MESSAGE);                    //send MESSAGE back to the client
                        MESSAGE = "";
                    }
                } catch (EOFException e) {
                    System.err.println("Client "+ clientNumber + " disconnected.");
                } catch (SocketException s) {
                    System.err.println("Client " + clientNumber + " disconnected.");
                } catch (ClassNotFoundException classnot) {
                    System.err.println("Data received in unknown format");
                }
            } catch (IOException ioException) {
                ioException.printStackTrace();
            } finally {
                try {                                    //Close connections
                    objectInputStream.close();
                    objectOutputStream.close();
                    //serverSocket.close();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        }
        void sendMessage(String msg) {                                        //send a message to the output stream
            try {
                objectOutputStream.writeObject(msg);
                objectOutputStream.flush();
                // System.out.println("Send message: " + msg);
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }
}