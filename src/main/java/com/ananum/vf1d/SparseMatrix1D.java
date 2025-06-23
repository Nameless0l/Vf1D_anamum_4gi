package com.ananum.vf1d;
import java.util.Arrays;
/**
 * Classe représentant une matrice tridiagonale creuse en 1D
 * Utilisée pour la résolution de systèmes linéaires dans les volumes finis 1D
 */
class SparseMatrix1D {
    private final int n;
    private final double[] diagonal;
    private final double[] upperDiagonal;
    private final double[] lowerDiagonal;
    
    public SparseMatrix1D(int n) {
        this.n = n;
        this.diagonal = new double[n];
        this.upperDiagonal = new double[n-1];
        this.lowerDiagonal = new double[n-1];
    }
    
    public void addElement(int row, int col, double value) {
        if (row == col) {
            diagonal[row] += value;
        } else if (col == row + 1) {
            upperDiagonal[row] += value;
        } else if (col == row - 1) {
            lowerDiagonal[col] += value;
        }
    }
    
    /**
     * Résolution par méthode de Gauss-Seidel
     */
    public double[] solveGaussSeidel(double[] b, double tolerance, int maxIterations) {
        double[] x = new double[n];
        double[] xOld = new double[n];
        
        for (int iter = 0; iter < maxIterations; iter++) {
            System.arraycopy(x, 0, xOld, 0, n);
            
            // Première ligne
            x[0] = (b[0] - upperDiagonal[0] * x[1]) / diagonal[0];
            
            // Lignes intérieures
            for (int i = 1; i < n-1; i++) {
                x[i] = (b[i] - lowerDiagonal[i-1] * x[i-1] - upperDiagonal[i] * x[i+1]) / diagonal[i];
            }
            
            // Dernière ligne
            x[n-1] = (b[n-1] - lowerDiagonal[n-2] * x[n-2]) / diagonal[n-1];
            
            // Test de convergence
            double error = 0;
            for (int i = 0; i < n; i++) {
                error = Math.max(error, Math.abs(x[i] - xOld[i]));
            }
            
            if (error < tolerance) {
                System.out.println("Convergence atteinte en " + (iter+1) + " itérations");
                break;
            }
        }
        
        return x;
    }
    
    /**
     * Résolution par algorithme de Thomas (plus efficace pour tridiagonale)
     */
    public double[] solveThomas(double[] b) {
        double[] c = Arrays.copyOf(upperDiagonal, n-1);
        double[] d = Arrays.copyOf(b, n);
        double[] x = new double[n];
        
        // Élimination avant
        for (int i = 1; i < n; i++) {
            double m = lowerDiagonal[i-1] / diagonal[i-1];
            diagonal[i] -= m * c[i-1];
            d[i] -= m * d[i-1];
        }
        
        // Substitution arrière
        x[n-1] = d[n-1] / diagonal[n-1];
        for (int i = n-2; i >= 0; i--) {
            x[i] = (d[i] - c[i] * x[i+1]) / diagonal[i];
        }
        
        return x;
    }
}