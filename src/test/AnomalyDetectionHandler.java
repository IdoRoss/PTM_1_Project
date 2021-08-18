package test;


import test.Commands.DefaultIO;
import test.Server.ClientHandler;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class AnomalyDetectionHandler implements ClientHandler
{
    @Override
    public void handleClient(InputStream inFromClient, OutputStream outToClient)
    {
        SocketIO socketIO = new SocketIO(inFromClient, outToClient);
        CLI cli = new CLI(socketIO);
        cli.start();
        socketIO.close();
    }

    public class SocketIO implements DefaultIO
    {
        Scanner in;
        PrintStream out;

        public SocketIO(InputStream inFromClient, OutputStream outToClient)
        {
            this.in = new Scanner(inFromClient);
            this.out = new PrintStream(outToClient);
        }
        @Override
        public String readText()
        {
            return in.nextLine();
        }
        @Override
        public void write(String text)
        {
            out.print(text);
        }
        @Override
        public float readVal()
        {
            return in.nextFloat();
        }
        @Override
        public void write(float val)
        {
            out.print(val);
        }

        public void close()
        {
            in.close();
            out.close();
        }
    }
}
