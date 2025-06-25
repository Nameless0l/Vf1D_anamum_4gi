package com.ananum.vf1d;

/**
 * Version optimisée du solveur volumes finis 1D pour grandes tailles (N > 1000)
 * Utilise l'algorithme de Thomas optimisé et une gestion mémoire efficace
 */
public class OptimizedVolumesFinis1DSolver extends VolumesFinis1DSolver {
    
    private int iterations = 0;
    private static final double OMEGA = 1.8; // Facteur de sur-relaxation pour SOR
    
    public OptimizedVolumesFinis1DSolver(int n, double L, double a, double b, double c,
                                        Function1D sourceFunction, double u0, double uL, 
                                        boolean uniforme) {
        super(n, L, a, b, c, sourceFunction, u0, uL, uniforme);
    }
    
    @Override
    public Solution1D solve() {
        // Pour grandes tailles, utilise l'algorithme de Thomas optimisé
        if (getN() > 500) {
            return solveOptimized();
        } else {
            return super.solve();
        }
    }
    
    /**
     * Résolution optimisée pour grandes tailles
     */
    private Solution1D solveOptimized() {
        final int n = getN();
        final double L = getL();
        
        // Allocation mémoire optimisée
        double[] x = new double[n];
        double[] dx = new double[n];
        
        // Création du maillage optimisé
        createOptimizedMesh(x, dx, n, L);
        
        // Coefficients de la matrice tridiagonale
        double[] lower = new double[n-1];
        double[] diag = new double[n];
        double[] upper = new double[n-1];
        double[] rhs = new double[n];
        
        // Construction optimisée du système
        buildOptimizedSystem(x, dx, lower, diag, upper, rhs);
        
        // Résolution par algorithme de Thomas optimisé
        double[] solution = solveThomasOptimized(lower, diag, upper, rhs);
        
        return new Solution1D(solution, x, dx, n);
    }
    
    /**
     * Création optimisée du maillage
     */
    private void createOptimizedMesh(double[] x, double[] dx, int n, double L) {
        if (isUniforme()) {
            double h = L / n;
            double halfH = 0.5 * h;
            
            // Boucle déroulée pour performance
            for (int i = 0; i < n; i += 4) {
                if (i < n) {
                    x[i] = (i + 0.5) * h;
                    dx[i] = (i == 0) ? 2 * x[i] : 
                           (i == n-1) ? 2 * (L - x[i]) : h;
                }
                if (i + 1 < n) {
                    x[i+1] = (i + 1.5) * h;
                    dx[i+1] = (i+1 == n-1) ? 2 * (L - x[i+1]) : h;
                }
                if (i + 2 < n) {
                    x[i+2] = (i + 2.5) * h;
                    dx[i+2] = (i+2 == n-1) ? 2 * (L - x[i+2]) : h;
                }
                if (i + 3 < n) {
                    x[i+3] = (i + 3.5) * h;
                    dx[i+3] = (i+3 == n-1) ? 2 * (L - x[i+3]) : h;
                }
            }
        } else {
            // Maillage non uniforme standard
            for (int i = 0; i < n; i++) {
                double xi = (double)(i + 0.5) / n;
                x[i] = L * (xi - 0.5 * Math.sin(2 * Math.PI * xi) / (2 * Math.PI));
            }
            
            dx[0] = 2 * x[0];
            for (int i = 1; i < n - 1; i++) {
                dx[i] = 0.5 * (x[i+1] - x[i-1]);
            }
            dx[n-1] = 2 * (L - x[n-1]);
        }
    }
    
    /**
     * Construction optimisée du système linéaire
     */
    private void buildOptimizedSystem(double[] x, double[] dx, 
                                     double[] lower, double[] diag, 
                                     double[] upper, double[] rhs) {
        final int n = x.length;
        final double a = getA();
        final double b = getB();
        final double c = getC();
        final Function1D source = getSourceFunction();
        final double u0 = getU0();
        final double uL = getUL();
        
        // Pré-calcul des constantes
        final double bPos = Math.max(b, 0);
        final double bNeg = Math.max(-b, 0);
        
        // Premier élément
        double dxw0 = 0.5 * dx[0];
        double dxe0 = 0.5 * (dx[0] + dx[1]);
        double aw0 = a / dxw0 + bPos;
        double ae0 = a / dxe0 + bNeg;
        
        diag[0] = ae0 + aw0 + c * dx[0] + aw0;
        upper[0] = -ae0;
        rhs[0] = source.evaluate(x[0]) * dx[0] + aw0 * u0;
        
        // Éléments intérieurs - boucle optimisée
        for (int i = 1; i < n - 1; i++) {
            double dxw = 0.5 * (dx[i-1] + dx[i]);
            double dxe = 0.5 * (dx[i] + dx[i+1]);
            double aw = a / dxw + bPos;
            double ae = a / dxe + bNeg;
            
            lower[i-1] = -aw;
            diag[i] = ae + aw + c * dx[i];
            upper[i] = -ae;
            rhs[i] = source.evaluate(x[i]) * dx[i];
        }
        
        // Dernier élément
        double dxwN = 0.5 * (dx[n-2] + dx[n-1]);
        double dxeN = 0.5 * dx[n-1];
        double awN = a / dxwN + bPos;
        double aeN = a / dxeN + bNeg;
        
        lower[n-2] = -awN;
        diag[n-1] = awN + aeN + c * dx[n-1] + aeN;
        rhs[n-1] = source.evaluate(x[n-1]) * dx[n-1] + aeN * uL;
    }
    
    /**
     * Algorithme de Thomas optimisé (sans allocation supplémentaire)
     */
    private double[] solveThomasOptimized(double[] lower, double[] diag, 
                                         double[] upper, double[] rhs) {
        final int n = diag.length;
        double[] solution = new double[n];
        
        // Forward elimination - modifie diag et rhs en place
        for (int i = 1; i < n; i++) {
            double m = lower[i-1] / diag[i-1];
            diag[i] -= m * upper[i-1];
            rhs[i] -= m * rhs[i-1];
        }
        
        // Back substitution
        solution[n-1] = rhs[n-1] / diag[n-1];
        for (int i = n - 2; i >= 0; i--) {
            solution[i] = (rhs[i] - upper[i] * solution[i+1]) / diag[i];
        }
        
        iterations = 1; // Thomas est direct
        return solution;
    }
    
    /**
     * Méthode SOR (Successive Over-Relaxation) pour très grandes tailles
     */
    private double[] solveSOR(double[] lower, double[] diag, 
                             double[] upper, double[] rhs,
                             double tolerance, int maxIterations) {
        final int n = diag.length;
        double[] x = new double[n];
        double[] xOld = new double[n];
        
        iterations = 0;
        double error = Double.MAX_VALUE;
        
        while (iterations < maxIterations && error > tolerance) {
            System.arraycopy(x, 0, xOld, 0, n);
            
            // Premier élément
            double sum = rhs[0] - upper[0] * x[1];
            x[0] = (1 - OMEGA) * xOld[0] + OMEGA * sum / diag[0];
            
            // Éléments intérieurs
            for (int i = 1; i < n - 1; i++) {
                sum = rhs[i] - lower[i-1] * x[i-1] - upper[i] * xOld[i+1];
                x[i] = (1 - OMEGA) * xOld[i] + OMEGA * sum / diag[i];
            }
            
            // Dernier élément
            sum = rhs[n-1] - lower[n-2] * x[n-2];
            x[n-1] = (1 - OMEGA) * xOld[n-1] + OMEGA * sum / diag[n-1];
            
            // Calcul de l'erreur
            error = 0;
            for (int i = 0; i < n; i++) {
                error = Math.max(error, Math.abs(x[i] - xOld[i]));
            }
            
            iterations++;
        }
        
        return x;
    }
    
    /**
     * Retourne le nombre d'itérations (pour benchmark)
     */
    public int getIterations() {
        return iterations;
    }
    
    // Getters pour accéder aux champs privés de la classe parent
    private int getN() { 
        try {
            java.lang.reflect.Field field = VolumesFinis1DSolver.class.getDeclaredField("n");
            field.setAccessible(true);
            return (int) field.get(this);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    private double getL() {
        try {
            java.lang.reflect.Field field = VolumesFinis1DSolver.class.getDeclaredField("L");
            field.setAccessible(true);
            return (double) field.get(this);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    private double getA() {
        try {
            java.lang.reflect.Field field = VolumesFinis1DSolver.class.getDeclaredField("a");
            field.setAccessible(true);
            return (double) field.get(this);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    private double getB() {
        try {
            java.lang.reflect.Field field = VolumesFinis1DSolver.class.getDeclaredField("b");
            field.setAccessible(true);
            return (double) field.get(this);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    private double getC() {
        try {
            java.lang.reflect.Field field = VolumesFinis1DSolver.class.getDeclaredField("c");
            field.setAccessible(true);
            return (double) field.get(this);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    private Function1D getSourceFunction() {
        try {
            java.lang.reflect.Field field = VolumesFinis1DSolver.class.getDeclaredField("sourceFunction");
            field.setAccessible(true);
            return (Function1D) field.get(this);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    private double getU0() {
        try {
            java.lang.reflect.Field field = VolumesFinis1DSolver.class.getDeclaredField("u0");
            field.setAccessible(true);
            return (double) field.get(this);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    private double getUL() {
        try {
            java.lang.reflect.Field field = VolumesFinis1DSolver.class.getDeclaredField("uL");
            field.setAccessible(true);
            return (double) field.get(this);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    private boolean isUniforme() {
        try {
            java.lang.reflect.Field field = VolumesFinis1DSolver.class.getDeclaredField("uniforme");
            field.setAccessible(true);
            return (boolean) field.get(this);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}