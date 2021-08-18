package test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;


public class TimeSeries {
    //-------------------------------Members-------------------------------//
    private final LinkedList<String[]> _data;
    private int _numOfLines;
    private final int _numOfFeatures;

    //-------------------------------Methods-------------------------------//
    //----Ctor---//
    public TimeSeries(String csvFileName) {
        _data = new LinkedList<>();
        readCSV(csvFileName);
        _numOfFeatures = _data.getFirst().length;
        _numOfLines = _data.size();
    }
    //----Getters----//


    public int get_numOfLines() {
        return _numOfLines;
    }

    public int get_numOfFeatures() {
        return _numOfFeatures;
    }

    //----reads a csv file into the data----//
    private void readCSV(String csvFileName) {
        //validity check
        if (csvFileName == null) {
            return;
        }

        String line;
        try {
            //parsing a CSV file into BufferedReader class constructor
            BufferedReader br = new BufferedReader(new FileReader(csvFileName));
            while ((line = br.readLine()) != null)   //read a line
            {
                String[] sLine = line.split(",");//split by ","
                _data.add(sLine);//add the vector into the list
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //----returns the value given a feature's index and a line's index----//
    public float getValue(int feature, int index) {
        String[] line = _data.get(index);
        return Float.parseFloat(line[feature]);
    }

    //----returns the value given a feature's name and a line's index----//
    public float getValue(String feature, int index) {
        return getValue(featureIndex(feature), index);
    }

    //----returns data in a transpose format----//
    public float[][] getData() {
        //transform the list into a string matrix
        String[][] sArr = _data.toArray(new String[0][0]);
        float[][] data = new float[_numOfFeatures][_numOfLines - 1];
        //copy the strint matrix in a transpose format
        for (int i = 0; i < _numOfLines - 1; ++i) {
            for (int j = 0; j < _numOfFeatures; ++j) {
                data[j][i] = Float.parseFloat(sArr[i + 1][j]);
            }
        }
        return data;
    }

    //----feature to index----//
    public int featureIndex(String f) {
        String[] first = _data.getFirst();
        for (int i = 0; i < first.length; ++i) {
            if (f.intern().equals(first[i].intern())) {
                return i;
            }
        }
        return -1;
    }

    //----returns feature names----//
    public String[] getFeatures() {
        return _data.getFirst();
    }


}