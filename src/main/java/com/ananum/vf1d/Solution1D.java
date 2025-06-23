package com.ananum.vf1d;

public class Solution1D {
    private final double[] values;
    private final double[] meshPoints;
    private final double[] cellWidths;
    private final int n;
    
    public Solution1D(double[] values, double[] meshPoints, double[] cellWidths, int n) {
        this.values = values;
        this.meshPoints = meshPoints;
        this.cellWidths = cellWidths;
        this.n = n;
    }
    
    public double[] getValues() { return values; }
    public double[] getMeshPoints() { return meshPoints; }
    public double[] getCellWidths() { return cellWidths; }
    public int getN() { return n; }
    
    public double getValue(int i) {
        return values[i];
    }
    
    public double interpolate(double x) {
        // Trouve l'intervalle contenant x
        int i = 0;
        while (i < n-1 && x > meshPoints[i]) i++;
        
        if (i == 0) return values[0];
        if (i == n) return values[n-1];
        
        // Interpolation linéaire
        double x0 = meshPoints[i-1];
        double x1 = meshPoints[i];
        double t = (x - x0) / (x1 - x0);
        
        return values[i-1] * (1 - t) + values[i] * t;
    }
}
