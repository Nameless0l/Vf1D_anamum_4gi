package com.ananum.integration;

import java.util.Scanner;
import java.util.HashMap;
import java.util.Map;
import com.ananum.vf1d.integration.VolumesFinis1DMethode;
import com.ananum.vf1d.integration.VolumesFinis1DMethodeEnhanced;

/**
 * Programme principal unifié mis à jour avec support des fonctionnalités avancées
 */
public class ProgrammePrincipalUnifieUpdate {
    
    private static Map<String, MethodeResolution> methodes = new HashMap<>();
    private static boolean useEnhancedVersion = true;
    
    static {
        // Enregistrement des méthodes disponibles
        methodes.put("DF1D", null); // À implémenter par le groupe Différences Finies 1D
        methodes.put("DF2D", null); // À implémenter par le groupe Différences Finies 2D
        
        // Volumes Finis 1D - deux versions disponibles
        if (useEnhancedVersion) {
            methodes.put("VF1D", new VolumesFinis1DMethodeEnhanced());
        } else {
            methodes.put("VF1D", new VolumesFinis1DMethode());
        }
        
        methodes.put("VF2D", null); // À implémenter par le groupe Volumes Finis 2D
    }
    
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║   Programme de Résolution d'Équations Différentielles       ║");
        System.out.println("║                    Version Unifiée 2.0                       ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");
        
        boolean continuer = true;
        
        while (continuer) {
            System.out.println("\n┌─── MENU PRINCIPAL ───┐");
            System.out.println("│ 1. Résolution standard│");
            System.out.println("│ 2. Mode avancé VF1D  │");
            System.out.println("│ 3. Comparaison       │");
            System.out.println("│ 4. Configuration     │");
            System.out.println("│ 0. Quitter          │");
            System.out.println("└─────────────────────┘");
            
            System.out.print("\nVotre choix: ");
            int choixMenu = scanner.nextInt();
            scanner.nextLine();
            
            switch (choixMenu) {
                case 1:
                    resolutionStandard(scanner);
                    break;
                case 2:
                    modeAvanceVF1D(scanner);
                    break;
                case 3:
                    comparaisonMethodes(scanner);
                    break;
                case 4:
                    configuration(scanner);
                    break;
                case 0:
                    continuer = false;
                    System.out.println("\nAu revoir!");
                    break;
                default:
                    System.out.println("Choix invalide!");
            }
        }
        
        scanner.close();
    }
    
    /**
     * Résolution standard (compatible avec toutes les méthodes)
     */
    private static void resolutionStandard(Scanner scanner) {
        System.out.println("\n=== RÉSOLUTION STANDARD ===");
        System.out.println("Méthodes disponibles:");
        
        int index = 1;
        Map<Integer, String> indexToMethod = new HashMap<>();
        
        for (Map.Entry<String, MethodeResolution> entry : methodes.entrySet()) {
            if (entry.getValue() != null) {
                System.out.println(index + ". " + entry.getKey() + " - " + 
                                 entry.getValue().getNomMethode());
                indexToMethod.put(index, entry.getKey());
                index++;
            }
        }
        
        System.out.print("\nChoisir la méthode: ");
        int choix = scanner.nextInt();
        scanner.nextLine();
        
        String methodKey = indexToMethod.get(choix);
        if (methodKey == null) {
            System.out.println("Choix invalide!");
            return;
        }
        
        MethodeResolution methode = methodes.get(methodKey);
        
        // Saisie des paramètres
        ParametresEntree params = saisirParametres(scanner, methode.getDimension());
        
        try {
            System.out.println("\nRésolution en cours...");
            long startTime = System.currentTimeMillis();
            
            ResultatResolution resultat = methode.resoudre(params);
            
            long endTime = System.currentTimeMillis();
            System.out.println("Temps total: " + (endTime - startTime) + " ms");
            
            // Affichage des résultats
            System.out.println("\n=== RÉSULTATS ===");
            resultat.afficherResume();
            
            // Options supplémentaires pour VF1D Enhanced
            if (resultat instanceof VolumesFinis1DMethodeEnhanced.ResultatResolutionEnhanced) {
                VolumesFinis1DMethodeEnhanced.ResultatResolutionEnhanced resEnhanced = 
                    (VolumesFinis1DMethodeEnhanced.ResultatResolutionEnhanced) resultat;
                
                if (resEnhanced.graphFile != null) {
                    System.out.println("\n✓ Graphique généré: " + resEnhanced.graphFile);
                    System.out.println("  Ouvrez ce fichier dans votre navigateur pour visualiser");
                }
            }
            
            // Export des résultats
            System.out.print("\nExporter les résultats? (o/n): ");
            String export = scanner.nextLine();
            if (export.equalsIgnoreCase("o")) {
                exporterResultats(resultat, methode.getNomMethode());
            }
            
        } catch (Exception e) {
            System.err.println("Erreur lors de la résolution: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Mode avancé spécifique à VF1D
     */
    private static void modeAvanceVF1D(Scanner scanner) {
        System.out.println("\n=== MODE AVANCÉ VF1D ===");
        
        if (!useEnhancedVersion) {
            System.out.println("⚠️  Le mode avancé nécessite la version Enhanced.");
            System.out.print("Activer la version Enhanced? (o/n): ");
            if (scanner.nextLine().equalsIgnoreCase("o")) {
                useEnhancedVersion = true;
                methodes.put("VF1D", new VolumesFinis1DMethodeEnhanced());
            } else {
                return;
            }
        }
        
        System.out.println("\nOptions disponibles:");
        System.out.println("1. Étude de convergence automatique");
        System.out.println("2. Analyse de stabilité");
        System.out.println("3. Test grande taille (N > 1000)");
        System.out.println("4. Benchmark complet");
        
        System.out.print("Votre choix: ");
        int choix = scanner.nextInt();
        scanner.nextLine();
        
        switch (choix) {
            case 1:
                etudeConvergence(scanner);
                break;
            case 2:
                analyseStabilite(scanner);
                break;
            case 3:
                testGrandeTaille(scanner);
                break;
            case 4:
                runBenchmark();
                break;
            default:
                System.out.println("Choix invalide!");
        }
    }
    
    /**
     * Comparaison entre méthodes
     */
    private static void comparaisonMethodes(Scanner scanner) {
        System.out.println("\n=== COMPARAISON DES MÉTHODES ===");
        System.out.println("Cette fonctionnalité compare les différentes méthodes disponibles.");
        
        // Pour l'instant, seul VF1D est implémenté
        System.out.println("\n⚠️  Actuellement, seule la méthode VF1D est implémentée.");
        System.out.println("Les autres groupes doivent implémenter leurs méthodes pour");
        System.out.println("permettre la comparaison.");
        
        // Exemple de ce qui sera possible
        System.out.println("\nExemple de comparaison future:");
        System.out.println("┌─────────────┬──────────┬─────────┬──────────┐");
        System.out.println("│ Méthode     │ Temps(ms)│ Erreur  │ Mémoire  │");
        System.out.println("├─────────────┼──────────┼─────────┼──────────┤");
        System.out.println("│ VF1D        │ 45       │ 1.2e-3  │ 2.1 MB   │");
        System.out.println("│ DF1D        │ -        │ -       │ -        │");
        System.out.println("│ VF1D (opt)  │ 32       │ 1.2e-3  │ 1.8 MB   │");
        System.out.println("└─────────────┴──────────┴─────────┴──────────┘");
    }
    
    /**
     * Configuration du programme
     */
    private static void configuration(Scanner scanner) {
        System.out.println("\n=== CONFIGURATION ===");
        System.out.println("1. Utiliser VF1D Enhanced: " + (useEnhancedVersion ? "OUI" : "NON"));
        System.out.println("2. Génération automatique de graphiques: " + 
                         (useEnhancedVersion ? "ACTIVÉE" : "Version standard"));
        
        System.out.print("\nModifier la configuration? (o/n): ");
        if (scanner.nextLine().equalsIgnoreCase("o")) {
            System.out.print("Utiliser la version Enhanced de VF1D? (o/n): ");
            useEnhancedVersion = scanner.nextLine().equalsIgnoreCase("o");
            
            // Mise à jour de la méthode
            if (useEnhancedVersion) {
                methodes.put("VF1D", new VolumesFinis1DMethodeEnhanced());
            } else {
                methodes.put("VF1D", new VolumesFinis1DMethode());
            }
            
            System.out.println("✓ Configuration mise à jour");
        }
    }
    
    /**
     * Étude de convergence automatique
     */
    private static void etudeConvergence(Scanner scanner) {
        System.out.println("\n>>> Étude de Convergence Automatique");
        
        // Configuration du problème test
        System.out.println("Problème test: -u'' + u = sin(2πx), u(0)=u(1)=0");
        System.out.println("Solution exacte connue");
        
        System.out.print("Nombre de maillages à tester [5]: ");
        String input = scanner.nextLine();
        int nbTests = input.isEmpty() ? 5 : Integer.parseInt(input);
        
        // Paramètres de base
        ParametresEntree baseParams = new ParametresEntree();
        baseParams.a = 1.0;
        baseParams.b = 0.0;
        baseParams.c = 1.0;
        baseParams.xMin = 0.0;
        baseParams.xMax = 1.0;
        baseParams.conditionsLimites = new ParametresEntree.ConditionLimite();
        baseParams.conditionsLimites.u0 = 0.0;
        baseParams.conditionsLimites.uL = 0.0;
        baseParams.fonctionSource = "sin(2*pi*x)";
        baseParams.mailleUniforme = true;
        
        VolumesFinis1DMethodeEnhanced methode = 
            (VolumesFinis1DMethodeEnhanced) methodes.get("VF1D");
        
        System.out.println("\nN\tErreur L2\tOrdre\tTemps (ms)");
        System.out.println("----------------------------------------");
        
        int n = 10;
        for (int i = 0; i < nbTests; i++) {
            baseParams.nx = n;
            ResultatResolution res = methode.resoudre(baseParams);
            
            // Affichage simplifié (idéalement, calculer l'erreur réelle)
            System.out.printf("%d\t-\t\t-\t%d\n", n, res.tempsCalcul);
            
            n *= 2;
        }
        
        System.out.println("\n✓ Analyse terminée");
    }
    
    /**
     * Analyse de stabilité
     */
    private static void analyseStabilite(Scanner scanner) {
        System.out.println("\n>>> Analyse de Stabilité");
        
        System.out.print("Coefficient de diffusion a [1.0]: ");
        String input = scanner.nextLine();
        double a = input.isEmpty() ? 1.0 : Double.parseDouble(input);
        
        System.out.print("Coefficient de convection b [1.0]: ");
        input = scanner.nextLine();
        double b = input.isEmpty() ? 1.0 : Double.parseDouble(input);
        
        System.out.print("Nombre de points N [100]: ");
        input = scanner.nextLine();
        int n = input.isEmpty() ? 100 : Integer.parseInt(input);
        
        double L = 1.0;
        double h = L / n;
        double Pe = Math.abs(b) * h / (2 * a);
        
        System.out.println("\n=== Résultats de l'Analyse ===");
        System.out.println("Taille de maille h = " + String.format("%.4f", h));
        System.out.println("Nombre de Péclet local = " + String.format("%.3f", Pe));
        
        if (Pe > 2) {
            System.out.println("\n⚠️  ATTENTION: Pe > 2");
            System.out.println("Risque d'oscillations numériques!");
            int nMin = (int) Math.ceil(Math.abs(b) * L / (2 * a));
            System.out.println("Recommandation: utiliser au moins N = " + nMin);
        } else {
            System.out.println("\n✓ Configuration stable (Pe < 2)");
        }
    }
    
    /**
     * Test grande taille
     */
    private static void testGrandeTaille(Scanner scanner) {
        System.out.println("\n>>> Test Grande Taille");
        
        System.out.print("Nombre de volumes N [5000]: ");
        String input = scanner.nextLine();
        int n = input.isEmpty() ? 5000 : Integer.parseInt(input);
        
        if (n > 10000) {
            System.out.println("⚠️  N > 10000 peut nécessiter beaucoup de mémoire!");
            System.out.print("Continuer? (o/n): ");
            if (!scanner.nextLine().equalsIgnoreCase("o")) {
                return;
            }
        }
        
        // Configuration
        ParametresEntree params = new ParametresEntree();
        params.a = 1.0;
        params.b = 0.5;
        params.c = 1.0;
        params.nx = n;
        params.xMin = 0.0;
        params.xMax = 1.0;
        params.conditionsLimites = new ParametresEntree.ConditionLimite();
        params.conditionsLimites.u0 = 0.0;
        params.conditionsLimites.uL = 0.0;
        params.fonctionSource = "sin(2*pi*x)";
        params.mailleUniforme = true;
        
        System.out.println("\nRésolution avec N = " + n + " volumes...");
        
        Runtime runtime = Runtime.getRuntime();
        System.gc();
        long memBefore = runtime.totalMemory() - runtime.freeMemory();
        
        long startTime = System.currentTimeMillis();
        
        MethodeResolution methode = methodes.get("VF1D");
        ResultatResolution resultat = methode.resoudre(params);
        
        long endTime = System.currentTimeMillis();
        long memAfter = runtime.totalMemory() - runtime.freeMemory();
        
        System.out.println("\n=== Résultats ===");
        System.out.println("Temps de calcul: " + (endTime - startTime) + " ms");
        System.out.println("Mémoire utilisée: " + 
                         String.format("%.2f", (memAfter - memBefore) / (1024.0 * 1024.0)) + " MB");
        System.out.println("Temps par point: " + 
                         String.format("%.3f", (endTime - startTime) * 1000.0 / n) + " μs");
    }
    
    /**
     * Lance le benchmark complet
     */
    private static void runBenchmark() {
        System.out.println("\n>>> Lancement du Benchmark Complet");
        System.out.println("Cette opération peut prendre plusieurs minutes...\n");
        
        try {
            // Appel direct au BenchmarkRunner
            com.ananum.vf1d.benchmark.BenchmarkRunner.runCompleteBenchmark();
        } catch (Exception e) {
            System.err.println("Erreur lors du benchmark: " + e.getMessage());
        }
    }
    
    // Méthodes utilitaires existantes...
    
    private static ParametresEntree saisirParametres(Scanner scanner, int dimension) {
        ParametresEntree params = new ParametresEntree();
        params.conditionsLimites = new ParametresEntree.ConditionLimite();
        
        if (dimension == 1) {
            System.out.println("\nÉquation: aU'' + bU' + cU = f");
            
            System.out.print("Coefficient a (diffusion) [1.0]: ");
            String input = scanner.nextLine();
            params.a = input.isEmpty() ? 1.0 : Double.parseDouble(input);
            
            System.out.print("Coefficient b (convection) [0.0]: ");
            input = scanner.nextLine();
            params.b = input.isEmpty() ? 0.0 : Double.parseDouble(input);
            
            System.out.print("Coefficient c (réaction) [0.0]: ");
            input = scanner.nextLine();
            params.c = input.isEmpty() ? 0.0 : Double.parseDouble(input);
            
            System.out.print("Domaine [xMin, xMax] - xMin [0.0]: ");
            input = scanner.nextLine();
            params.xMin = input.isEmpty() ? 0.0 : Double.parseDouble(input);
            
            System.out.print("xMax [1.0]: ");
            input = scanner.nextLine();
            params.xMax = input.isEmpty() ? 1.0 : Double.parseDouble(input);
            
            System.out.print("Condition u(xMin) [0.0]: ");
            input = scanner.nextLine();
            params.conditionsLimites.u0 = input.isEmpty() ? 0.0 : Double.parseDouble(input);
            
            System.out.print("Condition u(xMax) [0.0]: ");
            input = scanner.nextLine();
            params.conditionsLimites.uL = input.isEmpty() ? 0.0 : Double.parseDouble(input);
            
            System.out.print("Nombre de points/volumes N [100]: ");
            input = scanner.nextLine();
            params.nx = input.isEmpty() ? 100 : Integer.parseInt(input);
            
            System.out.println("\nFonctions source disponibles:");
            System.out.println("  0, 1, x, x^2, sin(x), cos(x), sin(pi*x), exp(x)");
            System.out.print("Expression de f(x) [0]: ");
            input = scanner.nextLine();
            params.fonctionSource = input.isEmpty() ? "0" : input;
            
            System.out.print("Maillage uniforme? (o/n) [o]: ");
            input = scanner.nextLine();
            params.mailleUniforme = !input.equalsIgnoreCase("n");
            
        } else {
            System.out.println("Configuration 2D à implémenter...");
        }
        
        return params;
    }
    
    private static void exporterResultats(ResultatResolution resultat, String methode) {
        try {
            String filename = methode.replace(" ", "_") + "_" + 
                             System.currentTimeMillis() + ".csv";
            
            java.io.PrintWriter writer = new java.io.PrintWriter(filename);
            
            // En-tête
            writer.println("# Résultats - " + methode);
            writer.println("# Temps de calcul: " + resultat.tempsCalcul + " ms");
            if (resultat.iterations > 0) {
                writer.println("# Itérations: " + resultat.iterations);
            }
            
            // Données
            writer.println("x,u");
            for (int i = 0; i < resultat.solution.length; i++) {
                writer.printf("%.6f,%.6e\n", resultat.x[i], resultat.solution[i]);
            }
            
            writer.close();
            System.out.println("✓ Résultats exportés dans: " + filename);
            
        } catch (Exception e) {
            System.err.println("Erreur lors de l'export: " + e.getMessage());
        }
    }
}