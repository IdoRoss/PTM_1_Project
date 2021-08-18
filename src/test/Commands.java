package test;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.StandardSocketOptions;
import java.util.ArrayList;
import java.util.List;

public class Commands
{

    // the default IO to be used in all commands
    DefaultIO dio;

    public Commands(DefaultIO dio)
    {
        this.dio = dio;
    }

    // you may add other helper classes here

    private SharedState sharedState = new SharedState();

    private boolean isInRange(int i, List<int[]> anomalies)
    {
        for (int[] val : anomalies)
        {
            if (i >= val[0] && i <= val[1])
            {
                return true;
            }
        }
        return false;
    }

    //-----------------------------------------Default IO interface-----------------------------------------//
    public interface DefaultIO
    {
        public String readText();

        public void write(String text);

        public default void writeLine(String text)
        {
            write(text);
            write(System.lineSeparator());
        }

        public float readVal();

        public void write(float val);

        public default TimeSeries readCSV()
        {
            try
            {
                FileWriter csvOut = new FileWriter("myCsv.csv");
                String line;
                while (!(line = this.readText()).intern().equals("done".intern()))
                {
                    csvOut.write(line + System.lineSeparator());
                }
                csvOut.close();
            } catch (IOException e)
            {
                e.printStackTrace();
            }
            TimeSeries ts = new TimeSeries("myCsv.csv");

            return ts;
        }
    }

    //-----------------------------------------the shared state of all commands-----------------------------------------//
    private class SharedState
    {
        //Members
        private TimeSeries train;
        private TimeSeries test;
        private SimpleAnomalyDetector detector;
        private List<AnomalyReport> reports;

        public SharedState()
        {
            detector = new SimpleAnomalyDetector();
            detector.correlationThreshold = 0.9f;
        }
    }

    //-----------------------------------------Command abstract class-----------------------------------------//
    public abstract class Command
    {
        protected String description;

        public Command(String description)
        {
            this.description = description;
        }

        public abstract void execute();
    }

    // Command class for example:
    public class ExampleCommand extends Command
    {

        public ExampleCommand()
        {
            super("this is an example of command");
        }

        @Override
        public void execute()
        {
            dio.write(description);
        }
    }

    //--------------------------------------------------My Commands--------------------------------------------------//
    //Command 1:
    public class UploadCSV extends Command
    {

        public UploadCSV()
        {
            super("1. upload a time series csv file");
        }

        @Override
        public void execute()
        {
            dio.writeLine("Please upload your local train CSV file.");
            sharedState.train = dio.readCSV();
            dio.writeLine("Upload complete.");
            dio.writeLine("Please upload your local test CSV file.");
            sharedState.test = dio.readCSV();
            dio.writeLine("Upload complete.");
        }
    }

    //Command 2:
    public class Settings extends Command
    {

        public Settings()
        {
            super("2. algorithm settings");
        }

        @Override
        public void execute()
        {
            dio.write("The current correlation threshold is " + sharedState.detector.correlationThreshold + System.lineSeparator());
            dio.write("Type a new threshold" + System.lineSeparator());
            float ct = Float.parseFloat(dio.readText());
            if (ct > 1 || ct < 0)
            {
                dio.write("please choose a value between 0 and 1." + System.lineSeparator());
                this.execute();
            } else
            {
                sharedState.detector.correlationThreshold = ct;
            }
        }
    }

    //Command 3:
    public class Detect extends Command
    {

        public Detect()
        {
            super("3. detect anomalies");
        }

        @Override
        public void execute()
        {
            sharedState.detector.learnNormal(sharedState.train);
            sharedState.reports = sharedState.detector.detect(sharedState.test);
            dio.write("anomaly detection complete." + System.lineSeparator());
        }
    }

    //Command 4:
    public class Display extends Command
    {

        public Display()
        {
            super("4. display results");
        }

        @Override
        public void execute()
        {
            for (AnomalyReport report : sharedState.reports)
            {
                dio.write(report.timeStep + "\t" + report.description + System.lineSeparator());
            }
            dio.write("Done." + System.lineSeparator());
        }
    }

    //Command 5:
    public class UploadAnomalies extends Command
    {

        public UploadAnomalies()
        {
            super("5. upload anomalies and analyze results");
        }

        @Override
        public void execute()
        {
            //read amonalies
            dio.write("Please upload your local anomalies file." + System.lineSeparator());
            List<Point> anomalies = new ArrayList<>();
            String line;
            while (!(line = dio.readText()).intern().equals("done".intern()))
            {
                String[] tmp = line.split(",");
                Point p = new Point(Integer.parseInt(tmp[0]), Integer.parseInt(tmp[1]));
                anomalies.add(p);
            }
            dio.write("Upload complete." + System.lineSeparator());
            //calc paramaters
            int testSize = sharedState.test.get_numOfLines();
            int n = 0;
            int p = 0;
            int tp = 0;
            int fp = 0;
            int numOfAnomalies = 0;
            //calc p and n
            for (Point point : anomalies)
            {
                numOfAnomalies += (point.y - point.x) + 1;
            }
            p = anomalies.size();
            n = testSize - numOfAnomalies;
            //calc tp fp
            boolean isTP = false;
            ArrayList<Point> reportsGrouped = groupReportsByRange();
            for (Point rep : reportsGrouped)
            {
                for (Point ano : anomalies)
                {
                    if (intersection(rep, ano))
                    {
                        isTP = true;
                        break;
                    }
                }
                if (isTP)
                {
                    ++tp;
                } else
                {
                    ++fp;
                }
                isTP = false;
            }
            float truePositiveRate = (float) tp / (float) p;
            float falseAlarmRate = (float) fp / (float) n;

            truePositiveRate = (float) Math.floor(truePositiveRate * 1000.f) / 1000.f;
            falseAlarmRate = (float) Math.floor(falseAlarmRate * 1000.f) / 1000.f;

            dio.write("True Positive Rate: " + truePositiveRate + System.lineSeparator());
            dio.write("False Positive Rate: " + falseAlarmRate + System.lineSeparator());
        }

        private ArrayList<Point> groupReportsByRange()
        {
            ArrayList<Point> ranges = new ArrayList<>();
            long start = 0, end = 0;
            boolean jump = true;
            for (AnomalyReport anomalyReport : sharedState.reports)
            {
                if (jump)
                {
                    start = anomalyReport.timeStep;
                    jump = false;
                } else if (anomalyReport.timeStep != end + 1)
                {
                    ranges.add(new Point(start, end));
                    start = anomalyReport.timeStep;
                    jump = true;
                }
                end = anomalyReport.timeStep;
            }
            if (!jump)
            {
                ranges.add(new Point(start, end));
            }
            return ranges;
        }

        private boolean intersection(Point a, Point b)
        {
            return (b.x <= a.x && a.y <= b.y) || (a.x <= b.x && b.y <= a.y) || (a.y >= b.x && a.y <= b.y) || (a.x >= b.x && a.x <= b.y);
        }
    }
}
