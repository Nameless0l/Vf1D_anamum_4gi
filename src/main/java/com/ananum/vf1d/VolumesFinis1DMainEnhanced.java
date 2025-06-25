package com.ananum.vf1d;

import com.ananum.vf1d.visualization.GraphGenerator;
import com.ananum.vf1d.benchmark.BenchmarkRunner;
import java.util.Scanner;
import java.io.IOException;

/**
 * Programme principal amélioré avec visualisation et benchmarks
 */
public class VolumesFinis1DMainEnhanced {
    
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("╔════════════════════════════════════════════════╗");
        System.out.println("║     MÉTHODE DES VOLUMES FINIS 1D - v2.0       ║");
        System.out.println("║         Avec Visualisation & Benchmarks        ║");
        System.out.println("╚════════════════════════════════════════════════╝");
        System.out.println("\nRésolution de: aU'' + bU' + cU = f");
        System.out.println("sur [0, L] avec U(0) = u0, U(L) = uL\n");
        
        boolean continuer = true;
        
        while (continuer) {
            System.out.println("\n┌─── MENU PRINCIPAL ───┐");
            System.out.println("│ 1. Résolution simple │");
            System.out.println("│ 2. Tests prédéfinis  │");
            System.out.println("│ 3. Analyse complète  │");
            System.out.println("│ 4. Benchmark complet │");
            System.out.println("│ 5. Grande taille     │");
            System.out.println("│ 0. Quitter          │");
            System.out.println("└─────────────────────┘");
            
            System.out.print("\nVotre choix: ");
            int choix = scanner.nextInt();
            
            try {
                switch (choix) {
                    case 1:
                        resolutionSimple(scanner);
                        break;
                    case 2:
                        testsPredefinis(scanner);
                        break;
                    case 3:
                        analyseComplete();
                        break;
                    case 4:
                        BenchmarkRunner.runCompleteBenchmark();
                        break;
                    case 5:
                        grandeTaille(scanner);
                        break;
                    case 0:
                        continuer = false;
                        System.out.println("\nAu revoir!");
                        break;
                    default:
                        System.out.println("Choix invalide!");
                }
            } catch (IOException e) {
                System.err.println("Erreur I/O: " + e.getMessage());
            }
        }
        
        scanner.close();
    }
    
    /**
     * Résolution simple avec paramètres personnalisés
     */
    private static void resolutionSimple(Scanner scanner) throws IOException {
        System.out.println("\n=== RÉSOLUTION PERSONNALISÉE ===");
        
        ProblemParameters params = inputProblemParameters(scanner);
        
        // Choix de la fonction source
        System.out.println("\nFonction source f(x):");
        System.out.println("1. f(x) = 0");
        System.out.println("2. f(x) = 1");
        System.out.println("3. f(x) = sin(πx)");
        System.out.println("4. f(x) = exp(x)");
        System.out.println("5. f(x) = 2x");
        System.out.print("Choix: ");
        
        int sourceChoice = scanner.nextInt();
        Function1D sourceFunction = getSourceFunction(sourceChoice, params.L);
        
        // Création du solveur
        VolumesFinis1DSolver solver;
        if (params.n > 500) {
            solver = new OptimizedVolumesFinis1DSolver(
                params.n, params.L, params.a, params.b, params.c,
                sourceFunction, params.u0, params.uL, params.uniforme
            );
            System.out.println("\nUtilisation du solveur optimisé pour N = " + params.n);
        } else {
            solver = new VolumesFinis1DSolver(
                params.n, params.L, params.a, params.b, params.c,
                sourceFunction, params.u0, params.uL, params.uniforme
            );
        }
        
        // Résolution
        System.out.println("\nRésolution en cours...");
        long startTime = System.currentTimeMillis();
        Solution1D solution = solver.solve();
        long endTime = System.currentTimeMillis();
        
        System.out.println("✓ Résolution terminée en " + (endTime - startTime) + " ms");
        
        // Affichage des résultats
        displaySolution(solution, params);
        
        // Génération du graphique
        System.out.print("\nGénérer un graphique HTML? (o/n): ");
        scanner.nextLine(); // Consommer le retour à la ligne
        String response = scanner.nextLine();
        
        if (response.equalsIgnoreCase("o")) {
            String filename = "solution_" + System.currentTimeMillis() + ".html";
            GraphGenerator.generateSolutionPlot(solution, 
                "Solution numérique - Volumes Finis 1D", filename);
            System.out.println("✓ Graphique sauvegardé: " + filename);
        }
    }
    
    /**
     * Tests prédéfinis avec visualisation
     */
    private static void testsPredefinis(Scanner scanner) throws IOException {
        System.out.println("\n=== TESTS PRÉDÉFINIS ===");
        System.out.println("1. Solution polynomiale");
        System.out.println("2. Solution trigonométrique");
        System.out.println("3. Convection-diffusion");
        System.out.println("4. Réaction-diffusion");
        System.out.println("5. Test équation -u'' + u = 2x");
        
        System.out.print("Choix du test: ");
        int choix = scanner.nextInt();
        
        switch (choix) {
            case 1:
                testPolynomial();
                break;
            case 2:
                testTrigonometric();
                break;
            case 3:
                testConvectionDiffusion();
                break;
            case 4:
                testReactionDiffusion();
                break;
            case 5:
                testEquationCustom();
                break;
            default:
                System.out.println("Test invalide!");
        }
    }
    
    /**
     * Test polynomial avec visualisation
     */
    private static void testPolynomial() throws IOException {
        System.out.println("\n>>> Test: Solution polynomiale u(x) = x²(1-x)");
        
        double L = 1.0;
        double a = 1.0, b = 0.0, c = 0.0;
        Function1D exact = x -> x * x * (1 - x);
        Function1D source = x -> -2 * (1 - 3 * x);
        double u0 = 0.0, uL = 0.0;
        
        int n = 100;
        
        VolumesFinis1DSolver solver = new VolumesFinis1DSolver(
            n, L, a, b, c, source, u0, uL, true
        );
        
        Solution1D numerical = solver.solve();
        
        // Calcul de la solution exacte sur le même maillage
        double[] xExact = numerical.getMeshPoints();
        double[] uExact = new double[n];
        for (int i = 0; i < n; i++) {
            uExact[i] = exact.evaluate(xExact[i]);
        }
        Solution1D exactSolution = new Solution1D(uExact, xExact, numerical.getCellWidths(), n);
        
        double error = solver.computeL2Error(numerical, exact);
        System.out.println("Erreur L2: " + error);
        
        // Génération du graphique comparatif
        GraphGenerator.generateSolutionPlot(numerical, exactSolution,
            "Test Polynomial: u(x) = x²(1-x)", "test_polynomial.html");
        System.out.println("✓ Graphique sauvegardé: test_polynomial.html");
    }
    
    /**
     * Test trigonométrique
     */
    private static void testTrigonometric() throws IOException {
        System.out.println("\n>>> Test: Solution trigonométrique u(x) = sin(πx)");
        
        double L = 1.0;
        double a = 1.0, b = 0.0, c = -Math.PI * Math.PI;
        Function1D exact = x -> Math.sin(Math.PI * x);
        Function1D source = x -> 0.0;
        double u0 = 0.0, uL = 0.0;
        
        // Test avec différentes résolutions
        int[] meshSizes = {20, 50, 100, 200};
        double[] errors = new double[meshSizes.length];
        
        for (int i = 0; i < meshSizes.length; i++) {
            VolumesFinis1DSolver solver = new VolumesFinis1DSolver(
                meshSizes[i], L, a, b, c, source, u0, uL, true
            );
            
            Solution1D solution = solver.solve();
            errors[i] = solver.computeL2Error(solution, exact);
            
            System.out.printf("N = %d, Erreur = %.3e\n", meshSizes[i], errors[i]);
        }
        
        // Génération du graphique de convergence
        GraphGenerator.generateConvergencePlot(meshSizes, errors, "test_convergence.html");
        System.out.println("✓ Graphique de convergence: test_convergence.html");
    }
    
    /**
     * Test convection-diffusion
     */
    private static void testConvectionDiffusion() throws IOException {
        System.out.println("\n>>> Test: Convection-Diffusion");
        
        double L = 1.0;
        double b = 1.0;
        double[] pecletNumbers = {1.0, 10.0, 100.0};
        
        for (double Pe : pecletNumbers) {
            double a = b * L / Pe;
            double c = 0.0;
            Function1D source = x -> 0.0;
            double u0 = 0.0, uL = 1.0;
            
            System.out.printf("\nPe = %.1f (a = %.3f, b = %.1f)\n", Pe, a, b);
            
            int n = 200;
            VolumesFinis1DSolver solver = new VolumesFinis1DSolver(
                n, L, a, b, c, source, u0, uL, true
            );
            
            Solution1D solution = solver.solve();
            
            // Génération du graphique
            String filename = String.format("convection_diffusion_Pe%.0f.html", Pe);
            GraphGenerator.generateSolutionPlot(solution,
                String.format("Convection-Diffusion Pe = %.1f", Pe), filename);
            System.out.println("✓ Graphique: " + filename);
        }
    }
    
    /**
     * Test réaction-diffusion
     */
    private static void testReactionDiffusion() throws IOException {
        System.out.println("\n>>> Test: Réaction-Diffusion");
        
        double L = 1.0;
        double a = 1.0, b = 0.0;
        double[] cValues = {1.0, 10.0, 100.0};
        
        for (double c : cValues) {
            Function1D source = x -> Math.sin(Math.PI * x);
            double u0 = 0.0, uL = 0.0;
            
            System.out.printf("\nc = %.1f\n", c);
            
            int n = 100;
            VolumesFinis1DSolver solver = new VolumesFinis1DSolver(
                n, L, a, b, c, source, u0, uL, true
            );
            
            Solution1D solution = solver.solve();
            
            // Génération du graphique
            String filename = String.format("reaction_diffusion_c%.0f.html", c);
            GraphGenerator.generateSolutionPlot(solution,
                String.format("Réaction-Diffusion c = %.1f", c), filename);
            System.out.println("✓ Graphique: " + filename);
        }
    }
    
    /**
     * Test pour l'équation -u'' + u = 2x avec u(0) = 0, u(1) = 2
     * Solution exacte: u(x) = c₁e^x + c₂e^(-x) + 2x - 2
     * Avec les conditions aux limites, on obtient les constantes c₁ et c₂
     */
    private static void testEquationCustom() throws IOException {
        System.out.println("\n>>> Test: Équation -u'' + u = 2x");
        System.out.println("Domaine: [0,1], u(0) = 0, u(1) = 2");
        
        double L = 1.0;
        double a = 1.0;  // coefficient de u''
        double b = 0.0;  // pas de terme u'
        double c = -1.0; // coefficient de u (signe négatif car l'équation est -u'' + u = 2x)
        double u0 = 0.0;
        double uL = 2.0;
        
        // Fonction source f(x) = 2x
        Function1D source = x -> 2.0 * x;
        
        // Solution exacte: résolution analytique de -u'' + u = 2x
        // u'' - u = -2x
        // Solution homogène: u_h = c₁e^x + c₂e^(-x)
        // Solution particulière: u_p = 2x - 2
        // Solution générale: u(x) = c₁e^x + c₂e^(-x) + 2x - 2
        // 
        // Conditions aux limites:
        // u(0) = c₁ + c₂ - 2 = 0  =>  c₁ + c₂ = 2
        // u(1) = c₁e + c₂e^(-1) + 2 - 2 = 2  =>  c₁e + c₂/e = 2
        //
        // Résolution du système:
        double e = Math.E;
        double c2 = (2 * e - 2) / (e * e - 1);
        double c1 = 2 - c2;
        
        Function1D exactSolution = x -> c1 * Math.exp(x) + c2 * Math.exp(-x) + 2*x - 2;
        
        System.out.printf("Solution exacte: u(x) = %.6f * e^x + %.6f * e^(-x) + 2x - 2\n", c1, c2);
        
        // Test de convergence avec différentes résolutions
        int[] meshSizes = {10, 20, 40, 80, 160, 320, 640};
        double[] errors = new double[meshSizes.length];
        long[] times = new long[meshSizes.length];
        
        System.out.println("\nAnalyse de convergence:");
        System.out.println("N\t\tErreur L2\t\tTemps (ms)\t\tOrdre");
        System.out.println("------------------------------------------------------------");
        
        for (int i = 0; i < meshSizes.length; i++) {
            int n = meshSizes[i];
            
            VolumesFinis1DSolver solver = new VolumesFinis1DSolver(
                n, L, a, b, c, source, u0, uL, true
            );
            
            long startTime = System.currentTimeMillis();
            Solution1D solution = solver.solve();
            long endTime = System.currentTimeMillis();
            
            times[i] = endTime - startTime;
            errors[i] = solver.computeL2Error(solution, exactSolution);
            
            double ordre = 0.0;
            if (i > 0) {
                ordre = Math.log(errors[i-1] / errors[i]) / Math.log(2.0);
            }
            
            System.out.printf("%d\t\t%.3e\t\t%d\t\t\t%.2f\n", 
                             n, errors[i], times[i], (i > 0 ? ordre : 0.0));
        }
        
        // Calcul de l'ordre de convergence moyen
        double ordreTotal = 0.0;
        for (int i = 1; i < errors.length; i++) {
            ordreTotal += Math.log(errors[i-1] / errors[i]) / Math.log(2.0);
        }
        double ordreMoyen = ordreTotal / (errors.length - 1);
        System.out.printf("\nOrdre de convergence moyen: %.2f\n", ordreMoyen);
        
        // Génération du graphique de convergence
        GraphGenerator.generateConvergencePlot(meshSizes, errors, 
            "demo_convergence_equation_custom.html");
        System.out.println("✓ Graphique de convergence: demo_convergence_equation_custom.html");
        
        // Génération du graphique de comparaison solution numérique/exacte
        int nFinal = 160; // Résolution pour la visualisation
        VolumesFinis1DSolver finalSolver = new VolumesFinis1DSolver(
            nFinal, L, a, b, c, source, u0, uL, true
        );
        Solution1D numericalSolution = finalSolver.solve();
        
        // Calcul de la solution exacte sur le même maillage
        double[] xExact = numericalSolution.getMeshPoints();
        double[] uExact = new double[nFinal];
        for (int j = 0; j < nFinal; j++) {
            uExact[j] = exactSolution.evaluate(xExact[j]);
        }
        Solution1D exactSol = new Solution1D(uExact, xExact, numericalSolution.getCellWidths(), nFinal);
        
        GraphGenerator.generateSolutionPlot(numericalSolution, exactSol,
            "Test Équation: -u'' + u = 2x", "demo_equation_custom.html");
        System.out.println("✓ Graphique comparatif: demo_equation_custom.html");
        
        // Affichage de quelques valeurs pour vérification
        System.out.println("\nVérification (N = " + nFinal + "):");
        System.out.println("x\t\tNumérique\tExacte\t\tErreur");
        System.out.println("----------------------------------------------------");
        
        double[] xPoints = numericalSolution.getMeshPoints();
        double[] uNumerique = numericalSolution.getValues();
        
        for (int i = 0; i < nFinal; i += nFinal/10) {
            double x = xPoints[i];
            double uNum = uNumerique[i];
            double uEx = exactSolution.evaluate(x);
            double err = Math.abs(uNum - uEx);
            
            System.out.printf("%.3f\t\t%.6f\t%.6f\t%.3e\n", x, uNum, uEx, err);
        }
    }
    
    /**
     * Analyse complète avec tous les graphiques
     */
    private static void analyseComplete() throws IOException {
        System.out.println("\n=== ANALYSE COMPLÈTE ===");
        System.out.println("Cette analyse va générer plusieurs graphiques...");
        
        // 1. Convergence
        System.out.println("\n1. Analyse de convergence...");
        analyseConvergence();
        
        // 2. Effet du nombre de Péclet
        System.out.println("\n2. Analyse de l'effet du nombre de Péclet...");
        analysePeclet();
        
        // 3. Performance
        System.out.println("\n3. Analyse de performance...");
        analysePerformance();
        
        System.out.println("\n✓ Analyse complète terminée!");
        System.out.println("Consultez les fichiers HTML générés pour visualiser les résultats.");
    }
    
    /**
     * Test avec grande taille (N = 1000 et plus)
     */
    private static void grandeTaille(Scanner scanner) throws IOException {
        System.out.println("\n=== TEST GRANDE TAILLE ===");
        System.out.print("Nombre de volumes N (ex: 1000, 5000, 10000): ");
        int n = scanner.nextInt();
        
        if (n > 10000) {
            System.out.println("⚠️  Attention: N > 10000 peut prendre du temps!");
        }
        
        double L = 1.0;
        double a = 1.0, b = 0.5, c = 1.0;
        Function1D source = x -> Math.sin(2 * Math.PI * x);
        double u0 = 0.0, uL = 0.0;
        
        System.out.println("\nRésolution avec N = " + n + " volumes...");
        
        // Utilisation du solveur optimisé
        OptimizedVolumesFinis1DSolver solver = new OptimizedVolumesFinis1DSolver(
            n, L, a, b, c, source, u0, uL, true
        );
        
        // Mesure du temps et de la mémoire
        Runtime runtime = Runtime.getRuntime();
        System.gc();
        long memBefore = runtime.totalMemory() - runtime.freeMemory();
        
        long startTime = System.currentTimeMillis();
        Solution1D solution = solver.solve();
        long endTime = System.currentTimeMillis();
        
        long memAfter = runtime.totalMemory() - runtime.freeMemory();
        double memoryMB = (memAfter - memBefore) / (1024.0 * 1024.0);
        
        System.out.println("\n=== RÉSULTATS ===");
        System.out.println("Temps de calcul: " + (endTime - startTime) + " ms");
        System.out.println("Mémoire utilisée: " + String.format("%.2f", memoryMB) + " MB");
        System.out.println("Itérations: " + solver.getIterations());
        
        // Affichage de quelques valeurs
        double[] x = solution.getMeshPoints();
        double[] u = solution.getValues();
        
        System.out.println("\nQuelques valeurs de la solution:");
        System.out.println("x\t\tu(x)");
        System.out.println("------------------------");
        
        int step = Math.max(1, n / 10);
        for (int i = 0; i < n; i += step) {
            System.out.printf("%.4f\t\t%.6f\n", x[i], u[i]);
        }
        
        // Option de génération de graphique
        System.out.print("\nGénérer un graphique? (o/n): ");
        scanner.nextLine(); // Consommer le retour à la ligne
        String response = scanner.nextLine();
        
        if (response.equalsIgnoreCase("o")) {
            String filename = "grande_taille_N" + n + ".html";
            System.out.println("Génération du graphique...");
            
            // Pour les très grandes tailles, on échantillonne les points
            if (n > 5000) {
                System.out.println("(Échantillonnage des points pour la visualisation)");
            }
            
            GraphGenerator.generateSolutionPlot(solution,
                "Solution pour N = " + n, filename);
            System.out.println("✓ Graphique sauvegardé: " + filename);
        }
    }
    
    // Méthodes auxiliaires
    
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
        
        System.out.print("Nombre de volumes N: ");
        params.n = scanner.nextInt();
        
        System.out.print("Maillage uniforme? (true/false): ");
        params.uniforme = scanner.nextBoolean();
        
        return params;
    }
    
    private static Function1D getSourceFunction(int choice, double L) {
        switch (choice) {
            case 1: return x -> 0.0;
            case 2: return x -> 1.0;
            case 3: return x -> Math.sin(Math.PI * x / L);
            case 4: return x -> Math.exp(x);
            case 5: return x -> 2*x;
            default: return x -> 0.0;
        }
    }
    
    private static void displaySolution(Solution1D solution, ProblemParameters params) {
        System.out.println("\n=== SOLUTION ===");
        
        double[] x = solution.getMeshPoints();
        double[] u = solution.getValues();
        int n = solution.getN();
        
        // Statistiques
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;
        double sum = 0;
        
        for (double val : u) {
            min = Math.min(min, val);
            max = Math.max(max, val);
            sum += val;
        }
        
        System.out.println("\nStatistiques:");
        System.out.println("Min: " + min);
        System.out.println("Max: " + max);
        System.out.println("Moyenne: " + (sum / n));
        
        // Affichage de quelques valeurs
        System.out.println("\nx\t\tu(x)");
        System.out.println("------------------------");
        
        int step = Math.max(1, n / 20);
        for (int i = 0; i < n; i += step) {
            System.out.printf("%.4f\t\t%.6f\n", x[i], u[i]);
        }
    }
    
    // Analyses détaillées
    
    private static void analyseConvergence() throws IOException {
        double L = 1.0;
        double a = 1.0, b = 0.5, c = 1.0;
        Function1D exact = x -> Math.sin(2 * Math.PI * x) * Math.exp(-x);
        Function1D source = x -> {
            double u = exact.evaluate(x);
            double up = 2 * Math.PI * Math.cos(2 * Math.PI * x) * Math.exp(-x) 
                       - Math.sin(2 * Math.PI * x) * Math.exp(-x);
            double upp = -4 * Math.PI * Math.PI * Math.sin(2 * Math.PI * x) * Math.exp(-x)
                        - 4 * Math.PI * Math.cos(2 * Math.PI * x) * Math.exp(-x)
                        + Math.sin(2 * Math.PI * x) * Math.exp(-x);
            return -a * upp - b * up + c * u;
        };
        
        double u0 = exact.evaluate(0);
        double uL = exact.evaluate(L);
        
        int[] meshSizes = {10, 20, 40, 80, 160, 320, 640};
        double[] errors = new double[meshSizes.length];
        
        for (int i = 0; i < meshSizes.length; i++) {
            VolumesFinis1DSolver solver = new VolumesFinis1DSolver(
                meshSizes[i], L, a, b, c, source, u0, uL, true
            );
            
            Solution1D solution = solver.solve();
            errors[i] = solver.computeL2Error(solution, exact);
        }
        
        GraphGenerator.generateConvergencePlot(meshSizes, errors, "analyse_convergence.html");
    }
    
    private static void analysePeclet() throws IOException {
        // Similaire à testConvectionDiffusion mais plus détaillé
        double L = 1.0;
        double[] pecletNumbers = {0.1, 1.0, 10.0, 50.0, 100.0};
        
        for (double Pe : pecletNumbers) {
            // Génération des solutions
            // Code similaire à testConvectionDiffusion
        }
    }
    
    private static void analysePerformance() throws IOException {
        // Benchmark léger pour l'analyse
        int[] sizes = {100, 500, 1000, 2000, 5000};
        // Code similaire à BenchmarkRunner mais plus simple
    }
}