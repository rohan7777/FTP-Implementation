import java.net.*;
import java.io.*;

public class Client {
    final String pathOfFileClient = "F:\\UF Acad\\Sem 1\\Computer Networks\\Project\\FTPClient\\";
    Socket clientSocket;           //socket connect to the server
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
            clientSocket = new Socket("localhost", 8000);
            System.out.println("Connected to localhost in port 8000");
            //initialize inputStream and outputStream
            objectOutputStream = new ObjectOutputStream(clientSocket.getOutputStream());
            objectOutputStream.flush();
            objectInputStream = new ObjectInputStream(clientSocket.getInputStream());

            //get Input from standard input
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
            while(true)	{
                System.out.print("Hello, please input a sentence: ");
                message = bufferedReader.readLine();				//read a sentence from the standard input
                sendMessage(message); 								//Send the sentence to the server
                String[] inputCommand = message.split("\\s");
                if(inputCommand[0].toLowerCase().equals("get")){
                    //MESSAGE = (String) objectInputStream.readObject();
                    if (true){
                        fileSavePath = pathOfFileClient + inputCommand[1];
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
                        } finally {
                            if (fileOutputStream != null) fileOutputStream.close();
                            if (bufferedOutputStream != null) bufferedOutputStream.close();
                        }
                    }
                }
                else if (inputCommand[0].toLowerCase().equals("exit")){
                    objectInputStream.close();
                    objectOutputStream.close();
                    break;
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
            System.out.println("Send message: " + msg);
        }
        catch(IOException ioException){
            ioException.printStackTrace();
        }
    }
}
