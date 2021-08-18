package test;

import java.util.*;

public class SimpleAnomalyDetector implements TimeSeriesAnomalyDetector {
    private ArrayList<CorrelatedFeatures> _cFeatures;
    public float correlationThreshold = 0.9f;

    //--------------------------------------Learn Method--------------------------------------//
    @Override
    public void learnNormal(TimeSeries ts) {
        //maintain a list of features that haven't been paired yet
        _cFeatures = new ArrayList<>();
        float[][] data = ts.getData();
        LinkedList<String> features = new LinkedList<>();
        Collections.addAll(features, ts.getFeatures());
        //for each feature, find its correlated pair and build the correlation
        while (!features.isEmpty()) {
            //get the first feature
            String f1 = features.pollFirst();
            //find its pair
            String f2 = correlatedWith(f1, ts);
            features.remove(f1);
            if (f2 == null) {
                continue;
            }
            features.remove(f2);
            //find the correlation
            float correlation = StatLib.pearson(data[ts.featureIndex(f1)], data[ts.featureIndex(f2)]);
            //find the array of points that the 2 features construct
            Point[] featuresP = featuresPoints(f1, f2, ts);
            //find the linear recreation
            Line l = StatLib.linear_reg(featuresP);
            //find the max distance from a point to the line
            float threshold = maxThreshold(featuresP, l) * 1.1f;
            //add the feature
            _cFeatures.add(new CorrelatedFeatures(f1, f2, correlation, l, threshold));
        }
    }

    //----return which feature is correlated with the given feature----//
    private String correlatedWith(String f1, TimeSeries ts) {
        String[] features = ts.getFeatures();
        int numOfFeatures = features.length;
        float[][] data = ts.getData();
        int index1 = ts.featureIndex(f1);
        float[] pearson = new float[numOfFeatures];
        //find the pearson of each feature with f1
        for (int i = 0; i < numOfFeatures; ++i) {
            if (i == index1) {
                pearson[i] = 0;
                continue;
            }
            pearson[i] = StatLib.pearson(data[index1], data[i]);
        }
        //find the index of the feature with max pearson and return that feature
        int maxI = maxIndex(pearson);
        if (maxI < 0) {
            return null;
        }
        return features[maxI];
    }

    //----find the index of the max abs value above threshold----//
    private int maxIndex(float[] arr) {
        float maxVal = 0;
        int maxI = 0;
        //find the max value index
        for (int i = 0; i < arr.length; ++i) {
            if (Math.abs(arr[i]) > maxVal) {
                maxVal = Math.abs(arr[i]);
                maxI = i;
            }
        }
        if (maxVal > correlationThreshold) {
            return maxI;
        } else return -1;
    }

    //----returns the array of points conducted by the feature values----//
    private Point[] featuresPoints(String f1, String f2, TimeSeries ts) {
        int index1 = ts.featureIndex(f1);
        int index2 = ts.featureIndex(f2);
        float[][] data = ts.getData();
        int len = data[0].length;
        Point[] p = new Point[len];
        //add each value of a feature into a point array
        for (int i = 0; i < len; ++i) {
            p[i] = new Point(data[index1][i], data[index2][i]);
        }
        return p;
    }

    //----finds a threshold----//
    private float maxThreshold(Point[] points, Line line) {
        float max = 0;
        for (Point point : points) {
            float tmp = StatLib.dev(point, line);
            if (max < tmp) {
                max = tmp;
            }
        }
        return max;
    }

    //--------------------------------------Detect Method--------------------------------------//
    @Override
    public List<AnomalyReport> detect(TimeSeries ts) {
        LinkedList<AnomalyReport> reports = new LinkedList<>();
        //for each 2 correlated features
        for (CorrelatedFeatures features : _cFeatures) {
            //build the point array
            Point[] points = featuresPoints(features.feature1, features.feature2, ts);
            //for every point
            for (int i = 0; i < points.length; ++i) {
                //check if that point's distance exceeds the threshold
                if (StatLib.dev(points[i], features.lin_reg) > features.threshold) {
                    //if so, add it to the report list
                    reports.add(new AnomalyReport(features.feature1 + "-" + features.feature2, i + 1));
                }
            }
        }
        return reports;
    }

    //--------------------------------------Returns the learn method's results--------------------------------------//
    public List<CorrelatedFeatures> getNormalModel() {

        return _cFeatures;
    }
}
