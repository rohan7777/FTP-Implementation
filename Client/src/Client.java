import java.net.*;
import java.io.*;

public class Client {
    final String pathOfFileClient = Client.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
    Socket clientSocket;                                    //Socket for connecting to the server
    ObjectOutputStream objectOutputStream;                  //Outputstream to write objects to the socket
    ObjectInputStream objectInputStream;                    //Objectinputstream to read objects from the socket
    String message,MESSAGE="",fileSavePath;
    FileOutputStream fileOutputStream = null;               //Fileoutputstream to read file from the sockets
    BufferedOutputStream bufferedOutputStream = null;       //
    int bytesRead;

    public Client() throws Exception {
    }

    public static void main(String args[]) throws Exception {
        Client client = new Client();
        client.run();
    }

    void run() {
        try{
            System.out.println("Client started.\n Enter command");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
            boolean check = false,checkFile=false;
            String[] command;
            while (!check) {
                try {
                    command = bufferedReader.readLine().toString().split("\\s");
                    clientSocket = new Socket(command[1], Integer.parseInt(command[2]));
                    System.out.println("Connected to " + command[1] + " in port " + command[2]);
                    if(command[0].toLowerCase().equals("ftpclient"));
                        check=true;
                } catch (Exception e) {
                    System.out.println("Could not connect to the server with given IP and port number, please try again.\n");
                }
            }
            //initialize inputStream and outputStream
            objectOutputStream = new ObjectOutputStream(clientSocket.getOutputStream());
            objectOutputStream.flush();
            objectInputStream = new ObjectInputStream(clientSocket.getInputStream());
            while (!MESSAGE.equals("Connected!")){          // Authenticate user credentials with server
                System.out.println("Enter username ");
                message = bufferedReader.readLine().trim();
                System.out.println("Enter password");
                message += " " + bufferedReader.readLine().trim();
                sendMessage(message);                           // Send credentials to server
                MESSAGE = (String) objectInputStream.readObject();
                if(!MESSAGE.equals("Connected!")){
                    System.out.println("\nIncorrect credentials. Please try again.\n");
                }
            }
            if(MESSAGE.equals("Connected!")){
                System.out.println("Authentication successful!");
                while(true)	{
                    System.out.print("Please enter a command : ");
                    message = bufferedReader.readLine();				//read a sentence from the standard input
                    String[] inputCommand = message.split("\\s");   // Split input command based on whitespaces
                    if(inputCommand[0].toLowerCase().equals("dir") || (inputCommand[0].toLowerCase().equals("get") && inputCommand.length == 2)) {
                        sendMessage(message);                                //Send the sentence to the server
                    }
                    else if(inputCommand[0].toLowerCase().equals("upload")){
                        if(inputCommand.length == 2)
                            checkFile = new File(pathOfFileClient, inputCommand[1]).exists();   //Check if file to be uploaded exists
                        if(checkFile){
                            sendMessage(message);
                            checkFile=false;
                        }
                        else {
                            System.out.println("\nFile to be uploaded not found, please check the file name.\n");
                            inputCommand = new String[]{""};
                        }
                    }
                    if(inputCommand[0].toLowerCase().equals("dir")){
                        MESSAGE = (String) objectInputStream.readObject();					//Receive the upperCase sentence from the server
                        System.out.println("\n" + MESSAGE);	//show the message to the user
                    }
                    else if(inputCommand[0].toLowerCase().equals("get") && inputCommand.length==2){
                        fileSavePath = pathOfFileClient + inputCommand[1];
                        MESSAGE = (String) objectInputStream.readObject();
                        if (MESSAGE.equals("File found.")) {
                            try {
                                //Create IO streams
                                InputStream inputStream = clientSocket.getInputStream();
                                DataInputStream dataInputStream = new DataInputStream(inputStream);
                                long fileSize = dataInputStream.readLong();
                                long x = fileSize;
                                String fileName = inputCommand[1];
                                OutputStream outputStream = new FileOutputStream(fileSavePath);
                                byte[] buffer = new byte[1024];
                                while (fileSize > 0 && (bytesRead = dataInputStream.read(buffer, 0, (int) Math.min(buffer.length, fileSize))) != -1) {
                                    outputStream.write(buffer, 0, bytesRead);
                                    fileSize -= bytesRead;
                                }
                                outputStream.flush();
                                System.out.println("File " + fileSavePath + " downloaded (" + x + " bytes read)");
                                MESSAGE="";
                            } finally {
                                if (fileOutputStream != null) fileOutputStream.close();
                                if (bufferedOutputStream != null) bufferedOutputStream.close();
                            }
                        } else {
                            System.out.println("Requested file is not present on the server.");
                        }
                    }
                    else if(inputCommand[0].toLowerCase().equals("upload")){
                        String filePath = pathOfFileClient + inputCommand[1];
                        try {
                            File myFile = new File(filePath);
                            byte[] mybytearray = new byte[(int) myFile.length()];
                            //Create IO streams
                            FileInputStream fileInputStream = new FileInputStream(myFile);
                            BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
                            DataInputStream dataInputStream = new DataInputStream(bufferedInputStream);
                            dataInputStream.readFully(mybytearray, 0, mybytearray.length);
                            OutputStream outputStream = clientSocket.getOutputStream();
                            //Sending filename and filesize
                            DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
                            dataOutputStream.writeLong(mybytearray.length);
                            dataOutputStream.write(mybytearray, 0, mybytearray.length);
                            dataOutputStream.flush();
                            //System.out.println("Done.\nTime taken ->" + Long.toString(finish - start));
                            System.out.println("File " + inputCommand[1].toString() + " uploaded (" + myFile.length() + " bytes read)");
                        } catch (Exception e) {
                            System.err.println(e);
                        }
                    }
                    else if (inputCommand[0].toLowerCase().equals("exit")){
                        objectInputStream.close();
                        objectOutputStream.close();
                        break;
                    }else {
                        System.out.println("Please enter a valid command. Available commands are \n1.dir\n2.get <filename>\n3.upload <filename>");
                    }
                }
            }
            else {
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
                clientSocket.close();
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
        }
        catch(IOException ioException){
            ioException.printStackTrace();
        }
    }
}