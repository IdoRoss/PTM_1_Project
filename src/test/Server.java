package test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

public class Server
{

    public interface ClientHandler
    {
        public void handleClient(InputStream inFromClient, OutputStream outToClient);
    }

    volatile boolean stop;

    public Server()
    {
        stop = false;
    }


    private void startServer(int port, ClientHandler ch)
    {
        ServerSocket server = null;
        try
        {
            server = new ServerSocket(port);
            server.setSoTimeout(1000);
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        while (!this.stop)
        {
            try
            {
                Socket aClient = server.accept();
                try
                {
                    ch.handleClient(aClient.getInputStream(), aClient.getOutputStream());
                    aClient.close();
                } catch (IOException e)
                {
                    e.printStackTrace();
                }
            }  catch (SocketTimeoutException e)
            {
            } catch (SocketException e)
            {
                e.printStackTrace();
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        try
        {
            server.close();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    // runs the server in its own thread
    public void start(int port, ClientHandler ch)
    {
        new Thread(() -> startServer(port, ch)).start();
    }

    public void stop()
    {
        stop = true;
    }
}
