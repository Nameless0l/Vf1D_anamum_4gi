package com.ananum.integration;

public interface MethodeResolution {
    /**
     * Résout le problème avec les paramètres donnés
     */
    ResultatResolution resoudre(ParametresEntree params);
    
    /**
     * Retourne le nom de la méthode
     */
    String getNomMethode();
    
    /**
     * Retourne la dimension du problème (1D ou 2D)
     */
    int getDimension();
}