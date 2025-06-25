package com.ananum.vf1d.integration;

import com.ananum.vf1d.*;
import com.ananum.vf1d.visualization.GraphGenerator;
import com.ananum.vf1d.analysis.AnalysisTools;
import com.ananum.integration.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Version améliorée de la méthode volumes finis 1D avec visualisation et analyse
 */
public class VolumesFinis1DMethodeEnhanced implements MethodeResolution {
    
    private boolean generateGraphs = true;
    private boolean performAnalysis = true;
    private Map<String, Object> additionalResults = new HashMap<>();
    
    @Override
    public ResultatResolution resoudre(ParametresEntree params) {
        // Vérification des paramètres
        validateParameters(params);
        
        // Analyse de stabilité préliminaire
        if (performAnalysis) {
            AnalysisTools.StabilityReport stability = AnalysisTools.StabilityAnalysis.analyzeStability(
                params.a, params.b, params.c, params.xMax - params.xMin, params.nx
            );
            
            if (!stability.isStable) {
                System.out.println("⚠️  Attention: Configuration potentiellement instable!");
                System.out.println("   " + stability.recommendation);
            }
            
            additionalResults.put("stability", stability);
        }
        
        // Création de la fonction source
        Function1D sourceFunction = createSourceFunction(params.fonctionSource);
        
        // Domaine
        double L = params.xMax - params.xMin;
        
        // Choix du solveur selon la taille
        VolumesFinis1DSolver solver;
        if (params.nx > 500) {
            solver = new OptimizedVolumesFinis1DSolver(
                params.nx, L, 
                params.a, params.b, params.c,
                sourceFunction,
                params.conditionsLimites.u0,
                params.conditionsLimites.uL,
                params.mailleUniforme
            );
            System.out.println("Utilisation du solveur optimisé pour N = " + params.nx);
        } else {
            solver = new VolumesFinis1DSolver(
                params.nx, L, 
                params.a, params.b, params.c,
                sourceFunction,
                params.conditionsLimites.u0,
                params.conditionsLimites.uL,
                params.mailleUniforme
            );
        }
        
        // Résolution avec mesure du temps
        long startTime = System.currentTimeMillis();
        Solution1D sol = solver.solve();
        long endTime = System.currentTimeMillis();
        
        // Construction du résultat standard
        ResultatResolution resultat = new ResultatResolutionEnhanced();
        resultat.solution = sol.getValues();
        resultat.x = translateMesh(sol.getMeshPoints(), params.xMin);
        resultat.tempsCalcul = endTime - startTime;
        
        // Ajout des informations supplémentaires
        if (solver instanceof OptimizedVolumesFinis1DSolver) {
            resultat.iterations = ((OptimizedVolumesFinis1DSolver) solver).getIterations();
        }
        
        // Génération des graphiques si demandé
        if (generateGraphs) {
            try {
                String baseFilename = "vf1d_solution_" + System.currentTimeMillis();
                
                // Graphique de la solution
                GraphGenerator.generateSolutionPlot(
                    sol, 
                    "Solution VF1D - N=" + params.nx, 
                    baseFilename + ".html"
                );
                
                ((ResultatResolutionEnhanced) resultat).graphFile = baseFilename + ".html";
                System.out.println("✓ Graphique généré: " + baseFilename + ".html");
                
            } catch (IOException e) {
                System.err.println("Erreur lors de la génération des graphiques: " + e.getMessage());
            }
        }
        
        // Analyse d'erreur si solution exacte disponible
        if (params instanceof ParametresEntreeEnhanced) {
            ParametresEntreeEnhanced paramsEnh = (ParametresEntreeEnhanced) params;
            if (paramsEnh.solutionExacte != null) {
                Function1D exact = createExactFunction(paramsEnh.solutionExacte);
                AnalysisTools.ErrorMetrics errors = 
                    AnalysisTools.ErrorAnalysis.computeErrorMetrics(sol, exact);
                
                ((ResultatResolutionEnhanced) resultat).errorMetrics = errors;
                System.out.println("Erreur L2: " + String.format("%.3e", errors.errorL2));
            }
        }
        
        return resultat;
    }
    
    @Override
    public String getNomMethode() {
        return "Volumes Finis 1D (Enhanced)";
    }
    
    @Override
    public int getDimension() {
        return 1;
    }
    
    /**
     * Effectue une étude de convergence complète
     */
    public AnalysisTools.ConvergenceStudy performConvergenceStudy(
            ParametresEntree baseParams,
            Function1D exactSolution,
            int[] meshSizes) {
        
        double L = baseParams.xMax - baseParams.xMin;
        Function1D source = createSourceFunction(baseParams.fonctionSource);
        
        return AnalysisTools.ErrorAnalysis.performConvergenceStudy(
            L, baseParams.a, baseParams.b, baseParams.c,
            source, exactSolution,
            baseParams.conditionsLimites.u0,
            baseParams.conditionsLimites.uL,
            meshSizes
        );
    }
    
    /**
     * Active/désactive la génération de graphiques
     */
    public void setGenerateGraphs(boolean generate) {
        this.generateGraphs = generate;
    }
    
    /**
     * Active/désactive l'analyse automatique
     */
    public void setPerformAnalysis(boolean analyze) {
        this.performAnalysis = analyze;
    }
    
    /**
     * Validation des paramètres d'entrée
     */
    private void validateParameters(ParametresEntree params) {
        if (params.nx <= 0) {
            throw new IllegalArgumentException("Le nombre de volumes doit être positif");
        }
        
        if (params.xMax <= params.xMin) {
            throw new IllegalArgumentException("xMax doit être supérieur à xMin");
        }
        
        if (params.a < 0) {
            throw new IllegalArgumentException("Le coefficient de diffusion doit être positif");
        }
        
        if (params.conditionsLimites == null) {
            throw new IllegalArgumentException("Les conditions aux limites doivent être spécifiées");
        }
    }
    
    /**
     * Crée la fonction source à partir de l'expression
     */
    private Function1D createSourceFunction(String expression) {
        // Gestion des expressions mathématiques courantes
        expression = expression.toLowerCase().trim();
        
        // Cas constants
        if (expression.equals("0") || expression.equals("zero")) {
            return x -> 0.0;
        }
        if (expression.equals("1") || expression.equals("un")) {
            return x -> 1.0;
        }
        
        // Cas polynomiaux
        if (expression.equals("x")) {
            return x -> x;
        }
        if (expression.equals("x^2") || expression.equals("x²")) {
            return x -> x * x;
        }
        if (expression.equals("x^3")) {
            return x -> x * x * x;
        }
        
        // Cas trigonométriques
        if (expression.equals("sin(x)")) {
            return x -> Math.sin(x);
        }
        if (expression.equals("cos(x)")) {
            return x -> Math.cos(x);
        }
        if (expression.equals("sin(pi*x)") || expression.equals("sin(πx)")) {
            return x -> Math.sin(Math.PI * x);
        }
        if (expression.equals("sin(2*pi*x)") || expression.equals("sin(2πx)")) {
            return x -> Math.sin(2 * Math.PI * x);
        }
        
        // Cas exponentiels
        if (expression.equals("exp(x)") || expression.equals("e^x")) {
            return x -> Math.exp(x);
        }
        if (expression.equals("exp(-x)") || expression.equals("e^(-x)")) {
            return x -> Math.exp(-x);
        }
        
        // Cas combinés
        if (expression.contains("gaussian") || expression.contains("gauss")) {
            return x -> Math.exp(-x * x);
        }
        
        // Par défaut, essayer de parser comme constante
        try {
            double val = Double.parseDouble(expression);
            return x -> val;
        } catch (NumberFormatException e) {
            System.out.println("⚠️  Expression '" + expression + "' non reconnue, utilisation de f(x) = 0");
            return x -> 0.0;
        }
    }
    
    /**
     * Crée la fonction exacte pour les tests
     */
    private Function1D createExactFunction(String expression) {
        // Similaire à createSourceFunction mais pour la solution exacte
        return createSourceFunction(expression);
    }
    
    /**
     * Translate le maillage si xMin != 0
     */
    private double[] translateMesh(double[] mesh, double xMin) {
        if (xMin == 0) return mesh;
        
        double[] translated = new double[mesh.length];
        for (int i = 0; i < mesh.length; i++) {
            translated[i] = mesh[i] + xMin;
        }
        return translated;
    }
    
    /**
     * Classe étendue pour les résultats avec informations supplémentaires
     */
    public static class ResultatResolutionEnhanced extends ResultatResolution {
        public String graphFile;
        public AnalysisTools.ErrorMetrics errorMetrics;
        public Map<String, Object> additionalData = new HashMap<>();
        
        @Override
        public void afficherResume() {
            super.afficherResume();
            
            if (errorMetrics != null) {
                System.out.println("Erreur L1: " + String.format("%.3e", errorMetrics.errorL1));
                System.out.println("Erreur L2: " + String.format("%.3e", errorMetrics.errorL2));
                System.out.println("Erreur L∞: " + String.format("%.3e", errorMetrics.errorLinf));
            }
            
            if (graphFile != null) {
                System.out.println("Graphique: " + graphFile);
            }
        }
    }
    
    /**
     * Classe étendue pour les paramètres avec options supplémentaires
     */
    public static class ParametresEntreeEnhanced extends ParametresEntree {
        public String solutionExacte;  // Expression de la solution exacte si connue
        public boolean genererGraphiques = true;
        public boolean analyseStabilite = true;
        public int[] maillesConvergence;  // Pour étude de convergence automatique
    }
}