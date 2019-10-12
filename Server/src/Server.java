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
                new Handler(serverSocket.accept(),clientNum).start();
                System.out.println("Client " + clientNum++ + " is connected!");
            } catch (Exception e) {
                System.out.println(e);
            }
        }
    }

    public static class Handler extends Thread {
        Socket localSocket = null;              //socket for the connection with the client
        int clientNumber;
        ObjectOutputStream objectOutputStream;  //stream write to the socket
        ObjectInputStream objectInputStream;    //stream read from the socket
        String message, MESSAGE;

        public Handler(Socket localSocket, int no){
            this.localSocket = localSocket;
            this.clientNumber = no;
        }

        public void run() {
            try {
                final String pathOfFileServer = "F:\\UF Acad\\Sem 1\\Computer Networks\\Project\\FTPServer\\";  //Path of the FTP file server
                System.out.println("Connection received from " + localSocket.getInetAddress().getHostName());   // Print out the client's address
                InputStream inputStream = localSocket.getInputStream();
                DataInputStream clientData = new DataInputStream(inputStream);
                objectOutputStream = new ObjectOutputStream(localSocket.getOutputStream());                    //initialize Input and Output streams
                objectOutputStream.flush();
                objectInputStream = new ObjectInputStream(localSocket.getInputStream());
                String auth="";
                while (!auth.equals("Connected!")) {                                    //Authenticate the client
                    message = (String) objectInputStream.readObject();
                    String[] userDetails = message.split("\\s");
                    if(!(userDetails[0].equals("user") && userDetails[1].equals("pass"))){
                        auth = "Incorrect username and password!";
                        sendMessage(auth);
                    }else{
                        System.out.println("Client " + clientNumber + " authenticated!");
                        auth = "Connected!";
                        sendMessage(auth);
                    }
                }
                try {
                    while (true) {
                        message = (String) objectInputStream.readObject();                            //receive the request sent from the client
                        String[] inputParamArr = message.split("\\s");
                        final File folder = new File(pathOfFileServer);
                        if (message.toString().toLowerCase().equals("dir")) {                       //Process dir request
                            System.out.println("Client "+clientNumber + " issued \"dir\" command.");
                            MESSAGE = "\nPlease find the list of files on the server below - \n";
                            for (final File fileEntry : folder.listFiles()) {
                                MESSAGE += fileEntry.getName() + "\n";
                            }
                            sendMessage(MESSAGE);
                        } else if (inputParamArr[0].toLowerCase().equals("get")) {                 //Process get request
                            System.out.println("Client "+clientNumber + " issued \"get\" command.");
                            String filePath = pathOfFileServer + inputParamArr[1];
                            boolean check = new File(pathOfFileServer, inputParamArr[1]).exists();
                            if (check) {
                                try {
                                    MESSAGE = "File found.";
                                    sendMessage(MESSAGE);
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

                                } catch (Exception e) {
                                    System.err.println(e);
                                }
                            } else {
                                System.out.println("File not found");
                                MESSAGE = "File not found.";
                                sendMessage(MESSAGE);
                            }
                        }else if(inputParamArr[0].toLowerCase().equals("upload")){          //Handle upload request
                            String fileSavePath = pathOfFileServer + inputParamArr[1];
                            try {
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
                            } catch (Exception e){

                            }
                        }
                        MESSAGE = "";
                    }
                    } catch (EOFException | SocketException e) {
                        System.err.println("Client "+ clientNumber + " disconnected.");
                    } catch (ClassNotFoundException classnot) {
                        System.err.println("Data received in unknown format");
                    }
            } catch (Exception e) {
                System.out.println("Client " + clientNumber + " disconnected.");
            } finally {
                try {                                    //Close connections
                    objectInputStream.close();
                    objectOutputStream.close();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        }
        void sendMessage(String msg) {                                        //send a message to the output stream
            try {
                objectOutputStream.writeObject(msg);
                objectOutputStream.flush();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }
}