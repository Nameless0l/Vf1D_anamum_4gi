package com.ananum.vf1d;

import java.util.Scanner;

/**
 * Programme principal pour la méthode des volumes finis 1D
 */
public class VolumesFinis1DMain {
    
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("=== Méthode des Volumes Finis 1D ===");
        System.out.println("Résolution de: aU'' + bU' + cU = f");
        System.out.println("sur [0, L] avec U(0) = u0, U(L) = uL\n");
        
        // Saisie des paramètres
        ProblemParameters params = inputProblemParameters(scanner);
        
        // Choix du problème test ou personnalisé
        System.out.println("\nChoisir le type de problème:");
        System.out.println("1. Problème personnalisé");
        System.out.println("2. Test 1: Solution polynomiale");
        System.out.println("3. Test 2: Solution trigonométrique");
        System.out.println("4. Test 3: Solution exponentielle");
        System.out.println("5. Analyse de convergence");
        
        System.out.print("Votre choix: ");
        int choice = scanner.nextInt();
        
        switch (choice) {
            case 1:
                solveCustomProblem(params);
                break;
            case 2:
                solvePolynomialTest();
                break;
            case 3:
                solveTrigonometricTest();
                break;
            case 4:
                solveExponentialTest();
                break;
            case 5:
                performConvergenceAnalysis();
                break;
            default:
                System.out.println("Choix invalide!");
        }
        
        scanner.close();
    }
    
    /**
     * Saisie des paramètres du problème
     */
    private static ProblemParameters inputProblemParameters(Scanner scanner) {
        ProblemParameters params = new ProblemParameters();
        
        System.out.print("Coefficient a (diffusion): ");
        params.a = scanner.nextDouble();
        
        System.out.print("Coefficient b (convection): ");
        params.b = scanner.nextDouble();
        
        System.out.print("Coefficient c (réaction): ");
        params.c = scanner.nextDouble();
        
        System.out.print("Longueur du domaine L: ");
        params.L = scanner.nextDouble();
        
        System.out.print("Condition limite u(0) = ");
        params.u0 = scanner.nextDouble();
        
        System.out.print("Condition limite u(L) = ");
        params.uL = scanner.nextDouble();
        
        System.out.print("Nombre de volumes de contrôle N: ");
        params.n = scanner.nextInt();
        
        System.out.print("Maillage uniforme? (true/false): ");
        params.uniforme = scanner.nextBoolean();
        
        return params;
    }
    
    /**
     * Résolution d'un problème personnalisé
     */
    private static void solveCustomProblem(ProblemParameters params) {
        // Fonction source par défaut
        Function1D sourceFunction = x -> Math.sin(Math.PI * x / params.L);
        
        VolumesFinis1DSolver solver = new VolumesFinis1DSolver(
            params.n, params.L, params.a, params.b, params.c,
            sourceFunction, params.u0, params.uL, params.uniforme
        );
        
        System.out.println("\nRésolution en cours...");
        long startTime = System.currentTimeMillis();
        Solution1D solution = solver.solve();
        long endTime = System.currentTimeMillis();
        
        System.out.println("Résolution terminée en " + (endTime - startTime) + " ms");
        
        displaySolution(solution, params);
    }
    
    /**
     * Test 1: Solution polynomiale u(x) = x²(L-x)
     */
    private static void solvePolynomialTest() {
        System.out.println("\n=== Test 1: Solution polynomiale u(x) = x²(L-x) ===");
        
        double L = 1.0;
        double a = 1.0, b = 0.0, c = 0.0;
        
        Function1D exact = x -> x * x * (L - x);
        Function1D source = x -> -2 * (L - 3 * x);  // f = -a*u''
        
        // Conditions aux limites exactes
        double u0 = exact.evaluate(0);
        double uL = exact.evaluate(L);
        
        int[] meshSizes = {10, 20, 40, 80};
        
        System.out.println("\nAnalyse de convergence:");
        System.out.println("N\tErreur L2\tOrdre");
        System.out.println("--------------------------------");
        
        double[] errors = new double[meshSizes.length];
        
        for (int i = 0; i < meshSizes.length; i++) {
            VolumesFinis1DSolver solver = new VolumesFinis1DSolver(
                meshSizes[i], L, a, b, c, source, u0, uL, true
            );
            
            Solution1D solution = solver.solve();
            errors[i] = solver.computeL2Error(solution, exact);
            
            if (i > 0) {
                double order = Math.log(errors[i-1] / errors[i]) / Math.log(2.0);
                System.out.printf("%d\t%.3e\t%.2f\n", meshSizes[i], errors[i], order);
            } else {
                System.out.printf("%d\t%.3e\t-\n", meshSizes[i], errors[i]);
            }
        }
    }
    
    /**
     * Test 2: Solution trigonométrique
     */
    private static void solveTrigonometricTest() {
        System.out.println("\n=== Test 2: Solution trigonométrique u(x) = sin(πx) ===");
        
        double L = 1.0;
        double a = 1.0, b = 0.0, c = 0.0;
        
        Function1D exact = x -> Math.sin(Math.PI * x);
        Function1D source = x -> Math.PI * Math.PI * Math.sin(Math.PI * x);
        
        double u0 = 0.0;
        double uL = 0.0;
        
        int n = 50;
        
        VolumesFinis1DSolver solver = new VolumesFinis1DSolver(
            n, L, a, b, c, source, u0, uL, true
        );
        
        Solution1D solution = solver.solve();
        double error = solver.computeL2Error(solution, exact);
        
        System.out.println("Nombre de volumes: " + n);
        System.out.println("Erreur L2: " + error);
        
        // Affichage de quelques valeurs
        System.out.println("\nComparaison solution numérique vs exacte:");
        System.out.println("x\tNumérique\tExacte\t\tErreur");
        System.out.println("------------------------------------------------");
        
        double[] x = solution.getMeshPoints();
        double[] u = solution.getValues();
        
        for (int i = 0; i < n; i += n/10) {
            double uExact = exact.evaluate(x[i]);
            System.out.printf("%.3f\t%.6f\t%.6f\t%.3e\n", 
                x[i], u[i], uExact, Math.abs(u[i] - uExact));
        }
    }
    
    /**
     * Test 3: Solution exponentielle avec convection
     */
    private static void solveExponentialTest() {
        System.out.println("\n=== Test 3: Solution avec convection-diffusion ===");
        
        double L = 1.0;
        double a = 0.1, b = 1.0, c = 0.0;  // Péclet = bL/a = 10
        
        // Solution exacte pour convection-diffusion
        double Pe = b * L / a;
        Function1D exact = x -> (Math.exp(Pe * x / L) - 1) / (Math.exp(Pe) - 1);
        Function1D source = x -> 0.0;
        
        double u0 = 0.0;
        double uL = 1.0;
        
        int n = 100;
        
        VolumesFinis1DSolver solver = new VolumesFinis1DSolver(
            n, L, a, b, c, source, u0, uL, true
        );
        
        Solution1D solution = solver.solve();
        
        System.out.println("Nombre de Péclet: " + Pe);
        System.out.println("Nombre de volumes: " + n);
        
        // Visualisation du profil
        visualizeSolution(solution, exact);
    }
    
    /**
     * Analyse de convergence complète
     */
    private static void performConvergenceAnalysis() {
        System.out.println("\n=== Analyse de Convergence Complète ===");
        
        // Problème test avec solution manufacturée
        double L = 1.0;
        double a = 1.0, b = 2.0, c = 1.0;
        
        Function1D exact = x -> Math.sin(2 * Math.PI * x) + x * (L - x);
        Function1D source = x -> {
            double u = exact.evaluate(x);
            double up = 2 * Math.PI * Math.cos(2 * Math.PI * x) + L - 2 * x;
            double upp = -4 * Math.PI * Math.PI * Math.sin(2 * Math.PI * x) - 2;
            return -a * upp - b * up + c * u;
        };
        
        double u0 = exact.evaluate(0);
        double uL = exact.evaluate(L);
        
        int[] meshSizes = {10, 20, 40, 80, 160, 320};
        double[] errors = new double[meshSizes.length];
        double[] h_values = new double[meshSizes.length];
        
        System.out.println("\nMaillage uniforme:");
        System.out.println("N\th\t\tErreur L2\tOrdre");
        System.out.println("------------------------------------------------");
        
        for (int i = 0; i < meshSizes.length; i++) {
            VolumesFinis1DSolver solver = new VolumesFinis1DSolver(
                meshSizes[i], L, a, b, c, source, u0, uL, true
            );
            
            Solution1D solution = solver.solve();
            errors[i] = solver.computeL2Error(solution, exact);
            h_values[i] = L / meshSizes[i];
            
            if (i > 0) {
                double order = Math.log(errors[i-1] / errors[i]) / 
                              Math.log(h_values[i-1] / h_values[i]);
                System.out.printf("%d\t%.3e\t%.3e\t%.2f\n", 
                    meshSizes[i], h_values[i], errors[i], order);
            } else {
                System.out.printf("%d\t%.3e\t%.3e\t-\n", 
                    meshSizes[i], h_values[i], errors[i]);
            }
        }
        
        // Test avec maillage non uniforme
        System.out.println("\nMaillage non uniforme:");
        System.out.println("N\tErreur L2");
        System.out.println("------------------------");
        
        for (int n : new int[]{20, 40, 80}) {
            VolumesFinis1DSolver solver = new VolumesFinis1DSolver(
                n, L, a, b, c, source, u0, uL, false
            );
            
            Solution1D solution = solver.solve();
            double error = solver.computeL2Error(solution, exact);
            
            System.out.printf("%d\t%.3e\n", n, error);
        }
    }
    
    /**
     * Affichage de la solution
     */
    private static void displaySolution(Solution1D solution, ProblemParameters params) {
        System.out.println("\n=== Solution ===");
        System.out.println("x\t\tu(x)");
        System.out.println("------------------------");
        
        double[] x = solution.getMeshPoints();
        double[] u = solution.getValues();
        int n = solution.getN();
        
        // Affiche 20 points maximum
        int step = Math.max(1, n / 20);
        
        for (int i = 0; i < n; i += step) {
            System.out.printf("%.4f\t\t%.6f\n", x[i], u[i]);
        }
        
        // Valeurs aux bords
        System.out.println("\nValeurs aux bords:");
        System.out.printf("u(0) = %.6f (imposé: %.6f)\n", 
            solution.interpolate(0), params.u0);
        System.out.printf("u(L) = %.6f (imposé: %.6f)\n", 
            solution.interpolate(params.L), params.uL);
    }
    
    /**
     * Visualisation simple en mode texte
     */
    private static void visualizeSolution(Solution1D solution, Function1D exact) {
        System.out.println("\n=== Profil de la solution ===");
        
        double[] x = solution.getMeshPoints();
        double[] u = solution.getValues();
        int n = solution.getN();
        
        // Trouve min et max pour l'échelle
        double minVal = Double.MAX_VALUE;
        double maxVal = Double.MIN_VALUE;
        
        for (double val : u) {
            minVal = Math.min(minVal, val);
            maxVal = Math.max(maxVal, val);
        }
        
        // Affichage graphique simple
        int width = 60;
        int height = 20;
        
        System.out.println("\n  Max = " + maxVal);
        
        for (int j = height; j >= 0; j--) {
            System.out.print("  |");
            
            for (int i = 0; i < n; i += Math.max(1, n/width)) {
                double val = u[i];
                double normalized = (val - minVal) / (maxVal - minVal);
                
                if (normalized * height >= j) {
                    System.out.print("*");
                } else {
                    System.out.print(" ");
                }
            }
            
            System.out.println();
        }
        
        System.out.print("  +");
        for (int i = 0; i < width; i++) System.out.print("-");
        System.out.println("> x");
        System.out.println("  Min = " + minVal);
        System.out.println("  0" + " ".repeat(width-3) + "L");
    }
}

class ProblemParameters {
    double a, b, c;     
    double L;           
    double u0, uL;      
    int n;              
    boolean uniforme;   
}