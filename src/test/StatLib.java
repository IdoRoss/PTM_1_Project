package test;


public class StatLib {


    // simple average
    public static float avg(float[] x) {
        float average = 0;
        for (int i = 0; i < x.length; ++i)
            average += x[i];

        return average / x.length;
    }

    // returns the variance of X and Y
    public static float var(float[] x) {
        float var = 0;
        float average = avg(x);

        for (int i = 0; i < x.length; ++i) {
            var += (x[i] - average) * (x[i] - average);
        }

        return var / x.length;
    }

    // returns the covariance of X and Y
    public static float cov(float[] x, float[] y) {
        float cov = 0;

        float avgx = avg(x);
        float avgy = avg(y);

        for (int i = 0; i < x.length; ++i) {
            cov += (x[i] - avgx) * (y[i] - avgy);
        }

        return cov / (x.length);
    }


    // returns the Pearson correlation coefficient of X and Y
    public static float pearson(float[] x, float[] y) {
        return (float) (cov(x, y) / (Math.sqrt(var(x)) * Math.sqrt(var(y))));
    }

    // performs a linear regression and returns the line equation
    public static Line linear_reg(Point[] points) {

        float x[] = new float[points.length];
        float y[] = new float[points.length];

        for (int i = 0; i < points.length; ++i) {
            x[i] = points[i].x;
            y[i] = points[i].y;
        }

        float a = cov(x, y) / var(x);
        float b = avg(y) - a * avg(x);

        return new Line(a, b);
    }

    // returns the deviation between point p and the line equation of the points
    public static float dev(Point p, Point[] points) {
        return dev(p, linear_reg(points));
    }

    // returns the deviation between point p and the line
    public static float dev(Point p, Line l) {

        return Math.abs(p.y - l.f(p.x));
    }

}
