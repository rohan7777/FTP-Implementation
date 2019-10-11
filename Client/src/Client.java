import java.net.*;
import java.io.*;

public class Client {
//    final String pathOfFileClient = "F:\\UF Acad\\Sem 1\\Computer Networks\\Project\\FTPClient\\";
    final String pathOfFileClient = Client.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
    //System.out.println(Server.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
    Socket clientSocket;           //socket connect to the server
    ObjectOutputStream objectOutputStream;         //stream write to the socket
    ObjectInputStream objectInputStream;          //stream read from the socket
    String message,MESSAGE="",fileSavePath;                //message send to the server
    FileOutputStream fileOutputStream = null;
    BufferedOutputStream bufferedOutputStream = null;
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
                    //command = bufferedReader.readLine().toString().split("\\s");
                    command = "fpt localhost 8000".split("\\s");
                    clientSocket = new Socket("localhost", 8000);
                    //clientSocket = new Socket(command[1], Integer.parseInt(command[2]));
                    System.out.println("Connected to" + command[1] + " in port " + command[2]);
                    check=true;

                } catch (IOException e) {
                    System.out.println("Could not connect to the server with given IP and port number, please try again.");
                    e.printStackTrace();
                }
            }

            //initialize inputStream and outputStream
            objectOutputStream = new ObjectOutputStream(clientSocket.getOutputStream());
            objectOutputStream.flush();
            objectInputStream = new ObjectInputStream(clientSocket.getInputStream());

            //get Input from standard input
            while (!MESSAGE.equals("Connected!")){
                System.out.println("Enter username ");
                message = bufferedReader.readLine().trim();
                System.out.println("Enter password");
                message += " " + bufferedReader.readLine().trim();
                sendMessage(message);
                MESSAGE = (String) objectInputStream.readObject();
                if(!MESSAGE.equals("Connected!")){
                    System.out.println("Incorrect credentials. Please try again.");
                }
            }

            if(MESSAGE.equals("Connected!")){
                System.out.println("Authentication successful!");
                while(true)	{
                    System.out.print("Please enter a command : ");
                    message = bufferedReader.readLine();				//read a sentence from the standard input
                    String[] inputCommand = message.split("\\s");
                    if(inputCommand[0].toLowerCase().equals("dir") || (inputCommand[0].toLowerCase().equals("get") && inputCommand.length == 2)) {
                        sendMessage(message);                                //Send the sentence to the server
                    }
                    else if(inputCommand[0].toLowerCase().equals("upload")){
                        if(inputCommand.length == 2)
                            check = new File(pathOfFileClient, inputCommand[1]).exists();
                        if(checkFile){
                            sendMessage(message);
                            checkFile=false;
                        }
                        else {
                            System.out.println("File to be uploaded not found, please check the file name.");
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
//                        if(MESSAGE.equals("File found"))

                        if (MESSAGE.equals("File found.")) {
                            try {
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
                            long start = System.currentTimeMillis();
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
                            long finish = System.currentTimeMillis();
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
                        //MESSAGE = (String) objectInputStream.readObject();					//Receive the upperCase sentence from the server
                        //System.out.println("Receive message: " + MESSAGE);	//show the message to the user
                        System.out.println("Please enter a valid command. Available commands are \n1.dir\n2.get <filename>\n3.upload <filename>");
                    }
                }
            }
            else {
                //MESSAGE = (String) objectInputStream.readObject();					//Receive the upperCase sentence from the server
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

    //public void Client() {}

    void sendMessage(String msg) {									//send a message to the output stream
        try{														//stream write the message
            objectOutputStream.writeObject(msg);
            objectOutputStream.flush();
            //System.out.println("Send message: " + msg);
        }
        catch(IOException ioException){
            ioException.printStackTrace();
        }
    }
}
