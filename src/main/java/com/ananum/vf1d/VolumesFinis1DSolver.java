package com.ananum.vf1d;

import java.util.Arrays;

/**
 * Solveur pour l'équation aU'' + bU' + cU = f sur [0,L]
 * avec conditions de Dirichlet par la méthode des volumes finis
 */
public class VolumesFinis1DSolver {
    private final int n;              // Nombre de volumes de contrôle
    private final double L;           // Longueur du domaine
    private final double a, b, c;     // Coefficients de l'équation
    private final Function1D sourceFunction;
    private final double u0, uL;      // Conditions aux limites
    private final boolean uniforme;   // Type de maillage
    
    public VolumesFinis1DSolver(int n, double L, double a, double b, double c,
                               Function1D sourceFunction, double u0, double uL, boolean uniforme) {
        this.n = n;
        this.L = L;
        this.a = a;
        this.b = b;
        this.c = c;
        this.sourceFunction = sourceFunction;
        this.u0 = u0;
        this.uL = uL;
        this.uniforme = uniforme;
    }
    
    /**
     * Résout le système par volumes finis
     */
    public Solution1D solve() {
        // Discrétisation du domaine
        double[] x = createMesh();
        double[] dx = createCellWidths(x);
        
        // Construction du système linéaire
        SparseMatrix1D matrix = buildMatrix(dx);
        double[] rhs = buildRightHandSide(x, dx);
        
        // Résolution
        double[] solution = matrix.solveGaussSeidel(rhs, 1e-10, 10000);
        
        return new Solution1D(solution, x, dx, n);
    }
    
    /**
     * Création du maillage (centres des volumes)
     */
    private double[] createMesh() {
        double[] x = new double[n];
        
        if (uniforme) {
            double h = L / n;
            for (int i = 0; i < n; i++) {
                x[i] = (i + 0.5) * h;
            }
        } else {
            // Maillage non uniforme (raffiné aux bords)
            for (int i = 0; i < n; i++) {
                double xi = (double)(i + 0.5) / n;
                // Transformation pour raffiner aux bords
                x[i] = L * (xi - 0.5 * Math.sin(2 * Math.PI * xi) / (2 * Math.PI));
            }
        }
        
        return x;
    }
    
    /**
     * Calcul des largeurs des volumes
     */
    private double[] createCellWidths(double[] x) {
        double[] dx = new double[n];
        
        // Premier volume
        dx[0] = 2 * x[0];
        
        // Volumes intérieurs
        for (int i = 1; i < n - 1; i++) {
            dx[i] = 0.5 * (x[i+1] - x[i-1]);
        }
        
        // Dernier volume
        dx[n-1] = 2 * (L - x[n-1]);
        
        return dx;
    }
    
    /**
     * Construction de la matrice du système
     */
    private SparseMatrix1D buildMatrix(double[] dx) {
        SparseMatrix1D matrix = new SparseMatrix1D(n);
        
        for (int i = 0; i < n; i++) {
            double dxi = dx[i];
            
            // Distances aux interfaces
            double dxe = (i < n-1) ? 0.5 * (dx[i] + dx[i+1]) : 0.5 * dx[i];
            double dxw = (i > 0) ? 0.5 * (dx[i-1] + dx[i]) : 0.5 * dx[i];
            
            // Coefficients pour les flux diffusifs et convectifs
            double ae = a / dxe + Math.max(-b, 0);  // Upwind pour la convection
            double aw = a / dxw + Math.max(b, 0);
            double ap = ae + aw + c * dxi;
            
            if (i == 0) {
                // Premier volume (avec condition limite gauche)
                matrix.addElement(i, i, ap + aw);
                if (i < n-1) matrix.addElement(i, i+1, -ae);
            }
            else if (i == n-1) {
                // Dernier volume (avec condition limite droite)
                matrix.addElement(i, i-1, -aw);
                matrix.addElement(i, i, ap + ae);
            }
            else {
                // Volumes intérieurs
                matrix.addElement(i, i-1, -aw);
                matrix.addElement(i, i, ap);
                matrix.addElement(i, i+1, -ae);
            }
        }
        
        return matrix;
    }
    
    /**
     * Construction du second membre
     */
    private double[] buildRightHandSide(double[] x, double[] dx) {
        double[] rhs = new double[n];
        
        for (int i = 0; i < n; i++) {
            // Terme source intégré sur le volume
            rhs[i] = sourceFunction.evaluate(x[i]) * dx[i];
            
            // Conditions aux limites
            if (i == 0) {
                double dxw = 0.5 * dx[0];
                double aw = a / dxw + Math.max(b, 0);
                rhs[i] += aw * u0;
            }
            if (i == n-1) {
                double dxe = 0.5 * dx[n-1];
                double ae = a / dxe + Math.max(-b, 0);
                rhs[i] += ae * uL;
            }
        }
        
        return rhs;
    }
    
    /**
     * Calcul de l'erreur L2 par rapport à une solution exacte
     */
    public double computeL2Error(Solution1D numerical, Function1D exact) {
        double error = 0;
        double[] x = numerical.getMeshPoints();
        double[] dx = numerical.getCellWidths();
        double[] u = numerical.getValues();
        
        for (int i = 0; i < n; i++) {
            double diff = u[i] - exact.evaluate(x[i]);
            error += diff * diff * dx[i];
        }
        
        return Math.sqrt(error / L);
    }
}



/**
 * Classe représentant la solution
 */


/**
 * Matrice creuse tridiagonale optimisée
 */
