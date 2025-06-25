package com.ananum.vf1d.visualization;

import com.ananum.vf1d.Solution1D;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * Générateur de graphiques HTML/JavaScript pour visualiser les solutions
 */
public class GraphGenerator {
    
    /**
     * Génère un graphique HTML de la solution
     */
    public static void generateSolutionPlot(Solution1D solution, String title, String filename) 
            throws IOException {
        generateSolutionPlot(solution, null, title, filename);
    }
    
    /**
     * Génère un graphique HTML comparant solution numérique et exacte
     */
    public static void generateSolutionPlot(Solution1D solution, Solution1D exact, 
                                           String title, String filename) throws IOException {
        double[] x = solution.getMeshPoints();
        double[] u = solution.getValues();
        
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n<html>\n<head>\n");
        html.append("<title>").append(title).append("</title>\n");
        html.append("<script src=\"https://cdn.jsdelivr.net/npm/chart.js\"></script>\n");
        html.append("<style>\n");
        html.append("body { font-family: Arial, sans-serif; margin: 20px; }\n");
        html.append(".container { width: 90%; margin: auto; }\n");
        html.append(".chart-container { position: relative; height: 500px; margin: 20px 0; }\n");
        html.append("</style>\n</head>\n<body>\n");
        html.append("<div class=\"container\">\n");
        html.append("<h1>").append(title).append("</h1>\n");
        html.append("<div class=\"chart-container\">\n");
        html.append("<canvas id=\"solutionChart\"></canvas>\n");
        html.append("</div>\n</div>\n");
        
        // Script Chart.js
        html.append("<script>\n");
        html.append("const ctx = document.getElementById('solutionChart').getContext('2d');\n");
        html.append("const chart = new Chart(ctx, {\n");
        html.append("  type: 'line',\n");
        html.append("  data: {\n");
        html.append("    labels: [");
        
        // Labels (x values)
        for (int i = 0; i < x.length; i++) {
            if (i > 0) html.append(", ");
            html.append(String.format("%.4f", x[i]));
        }
        html.append("],\n");
        
        html.append("    datasets: [{\n");
        html.append("      label: 'Solution numérique',\n");
        html.append("      data: [");
        
        // Numerical solution data
        for (int i = 0; i < u.length; i++) {
            if (i > 0) html.append(", ");
            html.append(String.format("%.6f", u[i]));
        }
        html.append("],\n");
        html.append("      borderColor: 'rgb(75, 192, 192)',\n");
        html.append("      backgroundColor: 'rgba(75, 192, 192, 0.2)',\n");
        html.append("      borderWidth: 2,\n");
        html.append("      pointRadius: 3\n");
        html.append("    }");
        
        // Add exact solution if provided
        if (exact != null) {
            html.append(",\n    {\n");
            html.append("      label: 'Solution exacte',\n");
            html.append("      data: [");
            
            double[] uExact = exact.getValues();
            for (int i = 0; i < uExact.length; i++) {
                if (i > 0) html.append(", ");
                html.append(String.format("%.6f", uExact[i]));
            }
            html.append("],\n");
            html.append("      borderColor: 'rgb(255, 99, 132)',\n");
            html.append("      backgroundColor: 'rgba(255, 99, 132, 0.2)',\n");
            html.append("      borderWidth: 2,\n");
            html.append("      pointRadius: 0,\n");
            html.append("      borderDash: [5, 5]\n");
            html.append("    }");
        }
        
        html.append("]\n  },\n");
        html.append("  options: {\n");
        html.append("    responsive: true,\n");
        html.append("    maintainAspectRatio: false,\n");
        html.append("    plugins: {\n");
        html.append("      title: {\n");
        html.append("        display: true,\n");
        html.append("        text: '").append(title).append("',\n");
        html.append("        font: { size: 16 }\n");
        html.append("      },\n");
        html.append("      legend: {\n");
        html.append("        display: true,\n");
        html.append("        position: 'top'\n");
        html.append("      }\n");
        html.append("    },\n");
        html.append("    scales: {\n");
        html.append("      x: {\n");
        html.append("        display: true,\n");
        html.append("        title: {\n");
        html.append("          display: true,\n");
        html.append("          text: 'x'\n");
        html.append("        }\n");
        html.append("      },\n");
        html.append("      y: {\n");
        html.append("        display: true,\n");
        html.append("        title: {\n");
        html.append("          display: true,\n");
        html.append("          text: 'u(x)'\n");
        html.append("        }\n");
        html.append("      }\n");
        html.append("    }\n");
        html.append("  }\n");
        html.append("});\n");
        html.append("</script>\n");
        html.append("</body>\n</html>");
        
        try (FileWriter writer = new FileWriter(filename)) {
            writer.write(html.toString());
        }
    }
    
    /**
     * Génère un graphique de convergence
     */
    public static void generateConvergencePlot(int[] meshSizes, double[] errors, 
                                              String filename) throws IOException {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n<html>\n<head>\n");
        html.append("<title>Analyse de Convergence</title>\n");
        html.append("<script src=\"https://cdn.jsdelivr.net/npm/chart.js\"></script>\n");
        html.append("<style>\n");
        html.append("body { font-family: Arial, sans-serif; margin: 20px; }\n");
        html.append(".container { width: 90%; margin: auto; }\n");
        html.append(".chart-container { position: relative; height: 500px; margin: 20px 0; }\n");
        html.append("table { border-collapse: collapse; margin: 20px 0; }\n");
        html.append("th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }\n");
        html.append("th { background-color: #f2f2f2; }\n");
        html.append("</style>\n</head>\n<body>\n");
        html.append("<div class=\"container\">\n");
        html.append("<h1>Analyse de Convergence - Méthode des Volumes Finis 1D</h1>\n");
        
        // Table of results
        html.append("<table>\n<tr><th>N</th><th>h</th><th>Erreur L2</th><th>Ordre</th></tr>\n");
        for (int i = 0; i < meshSizes.length; i++) {
            double h = 1.0 / meshSizes[i];
            String order = i > 0 ? String.format("%.2f", 
                Math.log(errors[i-1] / errors[i]) / Math.log(2.0)) : "-";
            html.append(String.format("<tr><td>%d</td><td>%.4f</td><td>%.3e</td><td>%s</td></tr>\n",
                meshSizes[i], h, errors[i], order));
        }
        html.append("</table>\n");
        
        html.append("<div class=\"chart-container\">\n");
        html.append("<canvas id=\"convergenceChart\"></canvas>\n");
        html.append("</div>\n</div>\n");
        
        // Script for log-log plot
        html.append("<script>\n");
        html.append("const ctx = document.getElementById('convergenceChart').getContext('2d');\n");
        html.append("const chart = new Chart(ctx, {\n");
        html.append("  type: 'line',\n");
        html.append("  data: {\n");
        html.append("    labels: [");
        
        // h values
        for (int i = 0; i < meshSizes.length; i++) {
            if (i > 0) html.append(", ");
            html.append(1.0 / meshSizes[i]);
        }
        html.append("],\n");
        
        html.append("    datasets: [{\n");
        html.append("      label: 'Erreur L2',\n");
        html.append("      data: [");
        
        // Error values
        for (int i = 0; i < errors.length; i++) {
            if (i > 0) html.append(", ");
            html.append(errors[i]);
        }
        html.append("],\n");
        html.append("      borderColor: 'rgb(75, 192, 192)',\n");
        html.append("      backgroundColor: 'rgba(75, 192, 192, 0.2)',\n");
        html.append("      borderWidth: 2,\n");
        html.append("      pointRadius: 5\n");
        html.append("    },\n");
        
        // Reference line for order 2
        html.append("    {\n");
        html.append("      label: 'Ordre 2 (référence)',\n");
        html.append("      data: [");
        
        double c = errors[0] * meshSizes[0] * meshSizes[0];
        for (int i = 0; i < meshSizes.length; i++) {
            if (i > 0) html.append(", ");
            html.append(c / (meshSizes[i] * meshSizes[i]));
        }
        html.append("],\n");
        html.append("      borderColor: 'rgb(255, 99, 132)',\n");
        html.append("      borderWidth: 2,\n");
        html.append("      borderDash: [5, 5],\n");
        html.append("      pointRadius: 0\n");
        html.append("    }]\n");
        html.append("  },\n");
        html.append("  options: {\n");
        html.append("    responsive: true,\n");
        html.append("    maintainAspectRatio: false,\n");
        html.append("    scales: {\n");
        html.append("      x: {\n");
        html.append("        type: 'logarithmic',\n");
        html.append("        display: true,\n");
        html.append("        title: {\n");
        html.append("          display: true,\n");
        html.append("          text: 'h (taille de maille)'\n");
        html.append("        }\n");
        html.append("      },\n");
        html.append("      y: {\n");
        html.append("        type: 'logarithmic',\n");
        html.append("        display: true,\n");
        html.append("        title: {\n");
        html.append("          display: true,\n");
        html.append("          text: 'Erreur L2'\n");
        html.append("        }\n");
        html.append("      }\n");
        html.append("    },\n");
        html.append("    plugins: {\n");
        html.append("      title: {\n");
        html.append("        display: true,\n");
        html.append("        text: 'Convergence de la méthode (échelle log-log)',\n");
        html.append("        font: { size: 16 }\n");
        html.append("      }\n");
        html.append("    }\n");
        html.append("  }\n");
        html.append("});\n");
        html.append("</script>\n");
        html.append("</body>\n</html>");
        
        try (FileWriter writer = new FileWriter(filename)) {
            writer.write(html.toString());
        }
    }
    
    /**
     * Génère un graphique de benchmark de performance
     */
    public static void generateBenchmarkPlot(List<BenchmarkResult> results, 
                                           String filename) throws IOException {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n<html>\n<head>\n");
        html.append("<title>Benchmark Performance</title>\n");
        html.append("<script src=\"https://cdn.jsdelivr.net/npm/chart.js\"></script>\n");
        html.append("<style>\n");
        html.append("body { font-family: Arial, sans-serif; margin: 20px; }\n");
        html.append(".container { width: 90%; margin: auto; }\n");
        html.append(".chart-container { position: relative; height: 500px; margin: 20px 0; }\n");
        html.append("</style>\n</head>\n<body>\n");
        html.append("<div class=\"container\">\n");
        html.append("<h1>Benchmark de Performance - Volumes Finis 1D</h1>\n");
        html.append("<div class=\"chart-container\">\n");
        html.append("<canvas id=\"benchmarkChart\"></canvas>\n");
        html.append("</div>\n</div>\n");
        
        html.append("<script>\n");
        html.append("const ctx = document.getElementById('benchmarkChart').getContext('2d');\n");
        html.append("const chart = new Chart(ctx, {\n");
        html.append("  type: 'line',\n");
        html.append("  data: {\n");
        html.append("    labels: [");
        
        // N values
        for (int i = 0; i < results.size(); i++) {
            if (i > 0) html.append(", ");
            html.append(results.get(i).n);
        }
        html.append("],\n");
        
        html.append("    datasets: [{\n");
        html.append("      label: 'Temps de calcul (ms)',\n");
        html.append("      data: [");
        
        // Time values
        for (int i = 0; i < results.size(); i++) {
            if (i > 0) html.append(", ");
            html.append(results.get(i).timeMs);
        }
        html.append("],\n");
        html.append("      borderColor: 'rgb(54, 162, 235)',\n");
        html.append("      backgroundColor: 'rgba(54, 162, 235, 0.2)',\n");
        html.append("      borderWidth: 2,\n");
        html.append("      pointRadius: 5,\n");
        html.append("      yAxisID: 'y1'\n");
        html.append("    },\n");
        html.append("    {\n");
        html.append("      label: 'Mémoire utilisée (MB)',\n");
        html.append("      data: [");
        
        // Memory values
        for (int i = 0; i < results.size(); i++) {
            if (i > 0) html.append(", ");
            html.append(String.format("%.2f", results.get(i).memoryMB));
        }
        html.append("],\n");
        html.append("      borderColor: 'rgb(255, 159, 64)',\n");
        html.append("      backgroundColor: 'rgba(255, 159, 64, 0.2)',\n");
        html.append("      borderWidth: 2,\n");
        html.append("      pointRadius: 5,\n");
        html.append("      yAxisID: 'y2'\n");
        html.append("    }]\n");
        html.append("  },\n");
        html.append("  options: {\n");
        html.append("    responsive: true,\n");
        html.append("    maintainAspectRatio: false,\n");
        html.append("    interaction: {\n");
        html.append("      mode: 'index',\n");
        html.append("      intersect: false\n");
        html.append("    },\n");
        html.append("    scales: {\n");
        html.append("      x: {\n");
        html.append("        display: true,\n");
        html.append("        title: {\n");
        html.append("          display: true,\n");
        html.append("          text: 'Nombre de volumes (N)'\n");
        html.append("        }\n");
        html.append("      },\n");
        html.append("      y1: {\n");
        html.append("        type: 'linear',\n");
        html.append("        display: true,\n");
        html.append("        position: 'left',\n");
        html.append("        title: {\n");
        html.append("          display: true,\n");
        html.append("          text: 'Temps (ms)'\n");
        html.append("        }\n");
        html.append("      },\n");
        html.append("      y2: {\n");
        html.append("        type: 'linear',\n");
        html.append("        display: true,\n");
        html.append("        position: 'right',\n");
        html.append("        title: {\n");
        html.append("          display: true,\n");
        html.append("          text: 'Mémoire (MB)'\n");
        html.append("        },\n");
        html.append("        grid: {\n");
        html.append("          drawOnChartArea: false\n");
        html.append("        }\n");
        html.append("      }\n");
        html.append("    },\n");
        html.append("    plugins: {\n");
        html.append("      title: {\n");
        html.append("        display: true,\n");
        html.append("        text: 'Performance vs Taille du problème',\n");
        html.append("        font: { size: 16 }\n");
        html.append("      }\n");
        html.append("    }\n");
        html.append("  }\n");
        html.append("});\n");
        html.append("</script>\n");
        html.append("</body>\n</html>");
        
        try (FileWriter writer = new FileWriter(filename)) {
            writer.write(html.toString());
        }
    }
    
    /**
     * Génère un graphique de comparaison pour différents nombres de Péclet
     */
    public static void generatePecletComparisonPlot(List<Solution1D> solutions, 
                                                   List<String> labels,
                                                   String filename) throws IOException {
        if (solutions.isEmpty() || solutions.size() != labels.size()) {
            throw new IllegalArgumentException("Les listes solutions et labels doivent avoir la même taille non nulle");
        }
        
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n<html>\n<head>\n");
        html.append("<title>Comparaison Nombres de Péclet</title>\n");
        html.append("<script src=\"https://cdn.jsdelivr.net/npm/chart.js\"></script>\n");
        html.append("<style>\n");
        html.append("body { font-family: Arial, sans-serif; margin: 20px; background-color: #f5f5f5; }\n");
        html.append(".container { max-width: 1200px; margin: auto; background: white; padding: 20px; ");
        html.append("box-shadow: 0 0 10px rgba(0,0,0,0.1); }\n");
        html.append(".chart-container { position: relative; height: 600px; margin: 20px 0; }\n");
        html.append("h1 { color: #333; text-align: center; }\n");
        html.append(".info { background: #e8f5e9; padding: 15px; border-radius: 5px; margin: 10px 0; }\n");
        html.append("</style>\n</head>\n<body>\n");
        html.append("<div class=\"container\">\n");
        html.append("<h1>Comparaison Solutions - Différents Nombres de Péclet</h1>\n");
        html.append("<div class=\"info\">\n");
        html.append("<p><strong>Nombre de Péclet :</strong> Pe = bL/a (rapport convection/diffusion)</p>\n");
        html.append("<p>Pe << 1 : diffusion dominante, Pe >> 1 : convection dominante</p>\n");
        html.append("</div>\n");
        html.append("<div class=\"chart-container\">\n");
        html.append("<canvas id=\"pecletChart\"></canvas>\n");
        html.append("</div>\n</div>\n");
        
        // Génération des données pour Chart.js
        html.append("<script>\n");
        html.append("const ctx = document.getElementById('pecletChart').getContext('2d');\n");
        
        // Utilisation des points de maillage de la première solution
        double[] xPoints = solutions.get(0).getMeshPoints();
        html.append("const xData = [");
        for (int i = 0; i < xPoints.length; i++) {
            if (i > 0) html.append(", ");
            html.append(String.format("%.4f", xPoints[i]));
        }
        html.append("];\n\n");
        
        // Génération des datasets pour chaque solution
        html.append("const datasets = [\n");
        String[] colors = {"#FF6384", "#36A2EB", "#FFCE56", "#4BC0C0", "#9966FF", "#FF9F40"};
        
        for (int s = 0; s < solutions.size(); s++) {
            Solution1D solution = solutions.get(s);
            String label = labels.get(s);
            String color = colors[s % colors.length];
            
            html.append("  {\n");
            html.append("    label: '").append(label).append("',\n");
            html.append("    data: [");
            
            double[] values = solution.getValues();
            for (int i = 0; i < values.length; i++) {
                if (i > 0) html.append(", ");
                html.append(String.format("%.6f", values[i]));
            }
            
            html.append("],\n");
            html.append("    borderColor: '").append(color).append("',\n");
            html.append("    backgroundColor: '").append(color).append("22',\n");
            html.append("    borderWidth: 2,\n");
            html.append("    fill: false,\n");
            html.append("    pointRadius: 0,\n");
            html.append("    pointHoverRadius: 3\n");
            html.append("  }");
            
            if (s < solutions.size() - 1) {
                html.append(",");
            }
            html.append("\n");
        }
        html.append("];\n\n");
        
        // Configuration du graphique
        html.append("const chart = new Chart(ctx, {\n");
        html.append("  type: 'line',\n");
        html.append("  data: {\n");
        html.append("    labels: xData,\n");
        html.append("    datasets: datasets\n");
        html.append("  },\n");
        html.append("  options: {\n");
        html.append("    responsive: true,\n");
        html.append("    maintainAspectRatio: false,\n");
        html.append("    interaction: {\n");
        html.append("      mode: 'index',\n");
        html.append("      intersect: false\n");
        html.append("    },\n");
        html.append("    scales: {\n");
        html.append("      x: {\n");
        html.append("        display: true,\n");
        html.append("        title: {\n");
        html.append("          display: true,\n");
        html.append("          text: 'Position x'\n");
        html.append("        }\n");
        html.append("      },\n");
        html.append("      y: {\n");
        html.append("        display: true,\n");
        html.append("        title: {\n");
        html.append("          display: true,\n");
        html.append("          text: 'Solution u(x)'\n");
        html.append("        }\n");
        html.append("      }\n");
        html.append("    },\n");
        html.append("    plugins: {\n");
        html.append("      title: {\n");
        html.append("        display: true,\n");
        html.append("        text: 'Influence du Nombre de Péclet sur la Solution',\n");
        html.append("        font: { size: 16 }\n");
        html.append("      },\n");
        html.append("      legend: {\n");
        html.append("        display: true,\n");
        html.append("        position: 'top'\n");
        html.append("      },\n");
        html.append("      tooltip: {\n");
        html.append("        mode: 'index',\n");
        html.append("        intersect: false\n");
        html.append("      }\n");
        html.append("    }\n");
        html.append("  }\n");
        html.append("});\n");
        html.append("</script>\n");
        html.append("</body>\n</html>");
        
        try (FileWriter writer = new FileWriter(filename)) {
            writer.write(html.toString());
        }
    }
    
    /**
     * Classe pour stocker les résultats de benchmark
     */
    public static class BenchmarkResult {
        public int n;
        public long timeMs;
        public double memoryMB;
        public int iterations;
        
        public BenchmarkResult(int n, long timeMs, double memoryMB, int iterations) {
            this.n = n;
            this.timeMs = timeMs;
            this.memoryMB = memoryMB;
            this.iterations = iterations;
        }
    }
}