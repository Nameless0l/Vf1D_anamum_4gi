package com.ananum.integration;

public class ResultatResolution {
    public double[] solution;       
    public double[] x, y;           
    public double tempsCalcul;      
    public int iterations;          
    public double erreur;           
    
    public void afficherResume() {
        System.out.println("Temps de calcul: " + tempsCalcul + " ms");
        System.out.println("Nombre de points: " + solution.length);
        if (iterations > 0) {
            System.out.println("Itérations: " + iterations);
        }
        if (erreur > 0) {
            System.out.println("Erreur estimée: " + erreur);
        }
    }
}