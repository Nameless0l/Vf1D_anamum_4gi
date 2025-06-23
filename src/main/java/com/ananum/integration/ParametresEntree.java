package com.ananum.integration;

public class ParametresEntree {
    // Coefficients de l'équation
    public double a, b, c;          // Pour 1D: aU'' + bU' + cU = f
    public double kxx, kyy;         // Pour 2D: div(K∇U) = f avec K = diag(kxx, kyy)
    
    // Domaine
    public double xMin = 0.0, xMax = 1.0;
    public double yMin = 0.0, yMax = 1.0;
    
    // Conditions aux limites (Dirichlet)
    public ConditionLimite conditionsLimites;
    
    // Discrétisation
    public int nx, ny;              // Nombre de points/volumes
    public boolean mailleUniforme = true;
    
    // Fonction source
    public String fonctionSource;   // Expression de f(x) ou f(x,y)
    
    // Options
    public boolean anisotrope = false;  // Pour 2D
    
    public static class ConditionLimite {
        public double u0, uL;       // Pour 1D: u(xMin), u(xMax)
        public String uBord;        // Pour 2D: expression sur le bord
    }
}
