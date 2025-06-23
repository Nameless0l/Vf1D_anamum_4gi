package com.ananum.integration;

import java.util.Scanner;
import com.ananum.vf1d.integration.VolumesFinis1DMethode;

public class ProgrammePrincipalUnifie {
    
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("=== Programme de Résolution d'Équations Différentielles ===");
        System.out.println("Méthodes disponibles:");
        System.out.println("1. Différences Finies 1D");
        System.out.println("2. Différences Finies 2D");
        System.out.println("3. Volumes Finis 1D");
        System.out.println("4. Volumes Finis 2D");
        
        System.out.print("\nChoisir la méthode: ");
        int choix = scanner.nextInt();
        scanner.nextLine(); // Consommer le retour à la ligne
        
        MethodeResolution methode = null;
        
        switch (choix) {
            case 3:
                methode = new VolumesFinis1DMethode();
                break;
            // Les autres cas seront implémentés par les autres groupes
            default:
                System.out.println("Méthode non encore implémentée");
                return;
        }
        
        if (methode != null) {
            System.out.println("\n=== " + methode.getNomMethode() + " ===");
            
            // Saisie des paramètres
            ParametresEntree params = saisirParametres(scanner, methode.getDimension());
            
            try {
                // Résolution
                System.out.println("\nRésolution en cours...");
                ResultatResolution resultat = methode.resoudre(params);
                
                // Affichage des résultats
                System.out.println("\n=== Résultats ===");
                resultat.afficherResume();
                
                // Affichage de quelques valeurs
                afficherSolution(resultat, methode.getDimension());
                
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
        
        scanner.close();
    }
    
    /**
     * Saisie des paramètres selon la dimension
     */
    private static ParametresEntree saisirParametres(Scanner scanner, int dimension) {
        ParametresEntree params = new ParametresEntree();
        params.conditionsLimites = new ParametresEntree.ConditionLimite();
        
        if (dimension == 1) {
            System.out.println("\nÉquation: aU'' + bU' + cU = f");
            
            System.out.print("Coefficient a (diffusion): ");
            params.a = scanner.nextDouble();
            
            System.out.print("Coefficient b (convection): ");
            params.b = scanner.nextDouble();
            
            System.out.print("Coefficient c (réaction): ");
            params.c = scanner.nextDouble();
            
            System.out.print("Domaine [xMin, xMax] - xMin: ");
            params.xMin = scanner.nextDouble();
            
            System.out.print("xMax: ");
            params.xMax = scanner.nextDouble();
            
            System.out.print("Condition u(xMin) = ");
            params.conditionsLimites.u0 = scanner.nextDouble();
            
            System.out.print("Condition u(xMax) = ");
            params.conditionsLimites.uL = scanner.nextDouble();
            
            System.out.print("Nombre de points/volumes N: ");
            params.nx = scanner.nextInt();
            scanner.nextLine(); // Consommer le retour à la ligne
            
            System.out.print("Expression de f(x) [0, 1, x, sin(x), sin(pi*x), exp(x)]: ");
            params.fonctionSource = scanner.nextLine();
            
            System.out.print("Maillage uniforme? (true/false): ");
            params.mailleUniforme = scanner.nextBoolean();
            
        } else {
            // Pour 2D (à implémenter par les autres groupes)
            System.out.println("Saisie 2D à implémenter...");
        }
        
        return params;
    }
    
    /**
     * Affichage de la solution
     */
    private static void afficherSolution(ResultatResolution resultat, int dimension) {
        if (dimension == 1) {
            System.out.println("\nSolution (premiers et derniers points):");
            System.out.println("x\t\tu(x)");
            System.out.println("------------------------");
            
            int n = resultat.solution.length;
            int nShow = Math.min(5, n/2);
            
            // Premiers points
            for (int i = 0; i < nShow; i++) {
                System.out.printf("%.4f\t\t%.6f\n", resultat.x[i], resultat.solution[i]);
            }
            
            if (n > 2 * nShow) {
                System.out.println("...");
            }
            
            // Derniers points
            for (int i = Math.max(nShow, n - nShow); i < n; i++) {
                System.out.printf("%.4f\t\t%.6f\n", resultat.x[i], resultat.solution[i]);
            }
        }
    }
    
    /**
     * Export des résultats dans un fichier
     */
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
            System.out.println("Résultats exportés dans: " + filename);
            
        } catch (Exception e) {
            System.err.println("Erreur lors de l'export: " + e.getMessage());
        }
    }
}
