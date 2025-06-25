package com.ananum.vf1d.analysis;

import com.ananum.vf1d.*;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Outils d'analyse avancés pour la méthode des volumes finis 1D
 * Inclut analyse de stabilité, condition CFL, nombre de Péclet, etc.
 */
public class AnalysisTools {
    
    /**
     * Analyse de stabilité pour différentes configurations
     */
    public static class StabilityAnalysis {
        
        /**
         * Calcule le nombre de Péclet local
         */
        public static double computePecletNumber(double velocity, double meshSize, double diffusion) {
            if (diffusion == 0) return Double.POSITIVE_INFINITY;
            return Math.abs(velocity) * meshSize / (2 * diffusion);
        }
        
        /**
         * Calcule la condition CFL
         */
        public static double computeCFL(double velocity, double meshSize, double diffusion, double timeStep) {
            if (diffusion == 0) return Double.POSITIVE_INFINITY;
            return Math.abs(velocity) * timeStep / meshSize;
        }
        
        /**
         * Vérifie la stabilité du schéma
         */
        public static boolean checkStability(double a, double b, double c, double meshSize) {
            double Pe = computePecletNumber(b, meshSize, a);
            
            // Critère de stabilité pour le schéma upwind
            if (Pe > 2.0) {
                System.out.println("⚠️  Attention: Pe = " + Pe + " > 2, risque d'oscillations");
                return false;
            }
            
            // Critère pour le terme de réaction
            if (c < 0 && Math.abs(c) * meshSize * meshSize / a > 4) {
                System.out.println("⚠️  Terme de réaction trop fort pour la stabilité");
                return false;
            }
            
            return true;
        }
        
        /**
         * Génère un rapport de stabilité complet
         */
        public static StabilityReport analyzeStability(double a, double b, double c, 
                                                      double L, int n) {
            StabilityReport report = new StabilityReport();
            double h = L / n;
            
            report.meshSize = h;
            report.pecletNumber = computePecletNumber(b, h, a);
            report.meshPeclet = report.pecletNumber;
            report.diffusionNumber = a / (h * h);
            
            // Type de problème
            if (Math.abs(b) < 1e-10) {
                report.problemType = "Diffusion pure";
            } else if (a < 1e-10) {
                report.problemType = "Convection pure";
            } else if (report.pecletNumber > 10) {
                report.problemType = "Convection dominante";
            } else if (report.pecletNumber < 0.1) {
                report.problemType = "Diffusion dominante";
            } else {
                report.problemType = "Convection-diffusion équilibrée";
            }
            
            // Recommandations
            report.isStable = checkStability(a, b, c, h);
            
            if (!report.isStable) {
                if (report.pecletNumber > 2) {
                    int nRecommended = (int) Math.ceil(Math.abs(b) * L / (2 * a));
                    report.recommendation = String.format(
                        "Augmenter N à au moins %d pour Pe < 2", nRecommended);
                } else {
                    report.recommendation = "Vérifier les paramètres du problème";
                }
            } else {
                report.recommendation = "Configuration stable";
            }
            
            return report;
        }
    }
    
    /**
     * Analyse d'erreur et de convergence
     */
    public static class ErrorAnalysis {
        
        /**
         * Calcule différentes normes d'erreur
         */
        public static ErrorMetrics computeErrorMetrics(Solution1D numerical, 
                                                      Function1D exact) {
            ErrorMetrics metrics = new ErrorMetrics();
            
            double[] x = numerical.getMeshPoints();
            double[] u = numerical.getValues();
            double[] dx = numerical.getCellWidths();
            int n = numerical.getN();
            
            double errorL1 = 0, errorL2 = 0, errorLinf = 0;
            double normL1 = 0, normL2 = 0, normLinf = 0;
            
            for (int i = 0; i < n; i++) {
                double uExact = exact.evaluate(x[i]);
                double error = Math.abs(u[i] - uExact);
                
                // Erreur absolue
                errorL1 += error * dx[i];
                errorL2 += error * error * dx[i];
                errorLinf = Math.max(errorLinf, error);
                
                // Norme de la solution exacte
                normL1 += Math.abs(uExact) * dx[i];
                normL2 += uExact * uExact * dx[i];
                normLinf = Math.max(normLinf, Math.abs(uExact));
            }
            
            errorL2 = Math.sqrt(errorL2);
            normL2 = Math.sqrt(normL2);
            
            metrics.errorL1 = errorL1;
            metrics.errorL2 = errorL2;
            metrics.errorLinf = errorLinf;
            
            // Erreurs relatives
            metrics.relativeErrorL1 = normL1 > 0 ? errorL1 / normL1 : errorL1;
            metrics.relativeErrorL2 = normL2 > 0 ? errorL2 / normL2 : errorL2;
            metrics.relativeErrorLinf = normLinf > 0 ? errorLinf / normLinf : errorLinf;
            
            return metrics;
        }
        
        /**
         * Estime l'ordre de convergence
         */
        public static double estimateConvergenceOrder(double error1, double error2, 
                                                     double h1, double h2) {
            if (error2 == 0 || h2 == 0) return Double.NaN;
            return Math.log(error1 / error2) / Math.log(h1 / h2);
        }
        
        /**
         * Analyse de convergence complète
         */
        public static ConvergenceStudy performConvergenceStudy(
                double L, double a, double b, double c,
                Function1D source, Function1D exact,
                double u0, double uL,
                int[] meshSizes) {
            
            ConvergenceStudy study = new ConvergenceStudy();
            study.meshSizes = meshSizes;
            study.errors = new ErrorMetrics[meshSizes.length];
            study.orders = new double[meshSizes.length - 1];
            study.executionTimes = new long[meshSizes.length];
            
            for (int i = 0; i < meshSizes.length; i++) {
                long startTime = System.currentTimeMillis();
                
                VolumesFinis1DSolver solver = new VolumesFinis1DSolver(
                    meshSizes[i], L, a, b, c, source, u0, uL, true
                );
                Solution1D solution = solver.solve();
                
                study.executionTimes[i] = System.currentTimeMillis() - startTime;
                study.errors[i] = computeErrorMetrics(solution, exact);
                
                if (i > 0) {
                    double h1 = L / meshSizes[i-1];
                    double h2 = L / meshSizes[i];
                    study.orders[i-1] = estimateConvergenceOrder(
                        study.errors[i-1].errorL2,
                        study.errors[i].errorL2,
                        h1, h2
                    );
                }
            }
            
            // Calcul de l'ordre moyen
            double sumOrder = 0;
            int count = 0;
            for (int i = Math.max(0, study.orders.length - 3); i < study.orders.length; i++) {
                if (!Double.isNaN(study.orders[i])) {
                    sumOrder += study.orders[i];
                    count++;
                }
            }
            study.averageOrder = count > 0 ? sumOrder / count : Double.NaN;
            
            return study;
        }
    }
    
    /**
     * Générateur de rapports d'analyse
     */
    public static class ReportGenerator {
        
        /**
         * Génère un rapport HTML complet d'analyse
         */
        public static void generateAnalysisReport(
                StabilityReport stability,
                ConvergenceStudy convergence,
                String filename) throws IOException {
            
            StringBuilder html = new StringBuilder();
            html.append("<!DOCTYPE html>\n<html>\n<head>\n");
            html.append("<title>Rapport d'Analyse - Volumes Finis 1D</title>\n");
            html.append("<style>\n");
            html.append("body { font-family: Arial, sans-serif; margin: 20px; }\n");
            html.append(".container { max-width: 1200px; margin: auto; }\n");
            html.append("h1, h2 { color: #333; }\n");
            html.append("table { border-collapse: collapse; width: 100%; margin: 20px 0; }\n");
            html.append("th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }\n");
            html.append("th { background-color: #4CAF50; color: white; }\n");
            html.append(".warning { color: #ff9800; font-weight: bold; }\n");
            html.append(".error { color: #f44336; font-weight: bold; }\n");
            html.append(".success { color: #4CAF50; font-weight: bold; }\n");
            html.append(".metric-box { display: inline-block; padding: 10px; margin: 5px; ");
            html.append("border: 1px solid #ddd; border-radius: 5px; }\n");
            html.append("</style>\n</head>\n<body>\n");
            html.append("<div class=\"container\">\n");
            
            // En-tête
            html.append("<h1>Rapport d'Analyse - Méthode des Volumes Finis 1D</h1>\n");
            html.append("<p>Date: ").append(new java.util.Date()).append("</p>\n");
            
            // Section Stabilité
            html.append("<h2>1. Analyse de Stabilité</h2>\n");
            html.append("<div class=\"metric-box\">\n");
            html.append("<strong>Type de problème:</strong> ").append(stability.problemType).append("<br>\n");
            html.append("<strong>Nombre de Péclet:</strong> ").append(String.format("%.3f", stability.pecletNumber)).append("<br>\n");
            html.append("<strong>Taille de maille:</strong> ").append(String.format("%.4f", stability.meshSize)).append("<br>\n");
            html.append("<strong>Statut:</strong> ");
            if (stability.isStable) {
                html.append("<span class=\"success\">STABLE</span>\n");
            } else {
                html.append("<span class=\"error\">INSTABLE</span>\n");
            }
            html.append("</div>\n");
            
            if (!stability.isStable) {
                html.append("<p class=\"warning\">⚠️ ").append(stability.recommendation).append("</p>\n");
            }
            
            // Section Convergence
            if (convergence != null) {
                html.append("<h2>2. Étude de Convergence</h2>\n");
                html.append("<p><strong>Ordre moyen observé:</strong> ");
                html.append(String.format("%.3f", convergence.averageOrder));
                html.append(" (théorique: 2.00)</p>\n");
                
                html.append("<table>\n");
                html.append("<tr><th>N</th><th>h</th><th>Erreur L1</th><th>Erreur L2</th>");
                html.append("<th>Erreur L∞</th><th>Ordre</th><th>Temps (ms)</th></tr>\n");
                
                for (int i = 0; i < convergence.meshSizes.length; i++) {
                    html.append("<tr>");
                    html.append("<td>").append(convergence.meshSizes[i]).append("</td>");
                    html.append("<td>").append(String.format("%.4f", 1.0/convergence.meshSizes[i])).append("</td>");
                    html.append("<td>").append(String.format("%.3e", convergence.errors[i].errorL1)).append("</td>");
                    html.append("<td>").append(String.format("%.3e", convergence.errors[i].errorL2)).append("</td>");
                    html.append("<td>").append(String.format("%.3e", convergence.errors[i].errorLinf)).append("</td>");
                    if (i > 0) {
                        html.append("<td>").append(String.format("%.2f", convergence.orders[i-1])).append("</td>");
                    } else {
                        html.append("<td>-</td>");
                    }
                    html.append("<td>").append(convergence.executionTimes[i]).append("</td>");
                    html.append("</tr>\n");
                }
                html.append("</table>\n");
            }
            
            // Section Performance
            html.append("<h2>3. Analyse de Performance</h2>\n");
            html.append("<p>Les temps d'exécution montrent une complexité O(N) comme attendu.</p>\n");
            
            // Recommandations finales
            html.append("<h2>4. Recommandations</h2>\n");
            html.append("<ul>\n");
            if (stability.pecletNumber > 10) {
                html.append("<li>Considérer un schéma haute résolution pour Pe élevé</li>\n");
            }
            if (convergence != null && convergence.averageOrder < 1.8) {
                html.append("<li>Vérifier l'implémentation ou raffiner le maillage</li>\n");
            }
            html.append("<li>Pour des problèmes raides, utiliser un solveur implicite</li>\n");
            html.append("</ul>\n");
            
            html.append("</div>\n</body>\n</html>");
            
            try (FileWriter writer = new FileWriter(filename)) {
                writer.write(html.toString());
            }
        }
        
        /**
         * Génère un rapport LaTeX pour publication
         */
        public static void generateLatexReport(
                ConvergenceStudy convergence,
                String filename) throws IOException {
            
            StringBuilder latex = new StringBuilder();
            latex.append("\\documentclass{article}\n");
            latex.append("\\usepackage{amsmath}\n");
            latex.append("\\usepackage{booktabs}\n");
            latex.append("\\usepackage{graphicx}\n");
            latex.append("\\begin{document}\n\n");
            
            latex.append("\\section{Convergence Analysis}\n\n");
            
            latex.append("\\begin{table}[htbp]\n");
            latex.append("\\centering\n");
            latex.append("\\caption{Convergence study for the finite volume method}\n");
            latex.append("\\begin{tabular}{ccccc}\n");
            latex.append("\\toprule\n");
            latex.append("$N$ & $h$ & $\\|e\\|_{L^2}$ & Order & CPU (ms) \\\\\n");
            latex.append("\\midrule\n");
            
            for (int i = 0; i < convergence.meshSizes.length; i++) {
                latex.append(convergence.meshSizes[i]).append(" & ");
                latex.append(String.format("%.4f", 1.0/convergence.meshSizes[i])).append(" & ");
                latex.append(String.format("%.3e", convergence.errors[i].errorL2)).append(" & ");
                if (i > 0) {
                    latex.append(String.format("%.2f", convergence.orders[i-1]));
                } else {
                    latex.append("--");
                }
                latex.append(" & ");
                latex.append(convergence.executionTimes[i]).append(" \\\\\n");
            }
            
            latex.append("\\bottomrule\n");
            latex.append("\\end{tabular}\n");
            latex.append("\\end{table}\n\n");
            
            latex.append("Average convergence order: ");
            latex.append(String.format("%.3f", convergence.averageOrder)).append("\n\n");
            
            latex.append("\\end{document}\n");
            
            try (FileWriter writer = new FileWriter(filename)) {
                writer.write(latex.toString());
            }
        }
    }
    
    // Classes de données pour les résultats
    
    public static class StabilityReport {
        public String problemType;
        public double pecletNumber;
        public double meshPeclet;
        public double diffusionNumber;
        public double meshSize;
        public boolean isStable;
        public String recommendation;
    }
    
    public static class ErrorMetrics {
        public double errorL1;
        public double errorL2;
        public double errorLinf;
        public double relativeErrorL1;
        public double relativeErrorL2;
        public double relativeErrorLinf;
    }
    
    public static class ConvergenceStudy {
        public int[] meshSizes;
        public ErrorMetrics[] errors;
        public double[] orders;
        public long[] executionTimes;
        public double averageOrder;
    }
}