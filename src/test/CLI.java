package test;

import java.util.ArrayList;

import test.Commands.Command;
import test.Commands.DefaultIO;

public class CLI
{

    ArrayList<Command> commands;
    DefaultIO dio;
    Commands c;

    public CLI(DefaultIO dio)
    {
        this.dio = dio;
        c = new Commands(dio);
        commands = new ArrayList<>();
        commands.add(c.new UploadCSV());
        commands.add(c.new Settings());
        commands.add(c.new Detect());
        commands.add(c.new Display());
        commands.add(c.new UploadAnomalies());
        // example: commands.add(c.new ExampleCommand());
        // implement
    }

    public void start()
    {
        while (true)
        {
            dio.write("Welcome to the Anomaly Detection Server."+System.lineSeparator());
            dio.write("Please choose an option:"+System.lineSeparator());
            for (Command command : commands)
            {
                dio.write(command.description+System.lineSeparator());
            }
            dio.write("6. exit"+System.lineSeparator());
            int input = Integer.parseInt(dio.readText());
            if(input == 6)
            {
                break;
            }
            commands.get(input - 1).execute();
        }
    }
}
