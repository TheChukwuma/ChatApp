import java.io.*;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

public class ClientHandler implements Runnable{

    public static Set<ClientHandler> clientHandlers = new HashSet<>();
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String clientUsername;

    public ClientHandler(Socket socket){
        try{
            this.socket = socket;
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.clientUsername = bufferedReader.readLine();
            clientHandlers.add(this);
            broadcastMessage("SERVER: " + clientUsername + " has joined the group chat!");

        }catch(IOException e){
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }


    @Override
    public void run() {
        String messageFromClient;

        while(socket.isConnected()){
            try {
                messageFromClient = bufferedReader.readLine();
                if(messageFromClient == null) throw new IOException();
                broadcastMessage(messageFromClient);
            }catch(IOException e){
                closeEverything(socket, bufferedReader, bufferedWriter);
                break;
            }
        }

    }

    private void broadcastMessage(String messageToSend) {
        for (ClientHandler clientHandler : clientHandlers){
        try{
            if(!clientHandler.clientUsername.equals(clientUsername)){
                clientHandler.bufferedWriter.write(messageToSend);
                clientHandler.bufferedWriter.newLine();
                clientHandler.bufferedWriter.flush();
            }
        }catch (IOException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
        }
    }

    public void removeClientHandler(){
        clientHandlers.remove(this);
        broadcastMessage("SERVER: " + clientUsername + " has left the chat");
    }

    public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter){
        removeClientHandler();
        try{
            if(bufferedReader != null){
                bufferedReader.close();
            }
            if (bufferedWriter != null){
                bufferedWriter.close();
            }
            if (socket != null){
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
