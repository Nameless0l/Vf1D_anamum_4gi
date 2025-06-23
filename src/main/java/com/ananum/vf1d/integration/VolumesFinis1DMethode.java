package com.ananum.vf1d.integration;


import com.ananum.vf1d.VolumesFinis1DSolver;
import com.ananum.integration.*;
import com.ananum.vf1d.Function1D;
import com.ananum.vf1d.Solution1D;

public class VolumesFinis1DMethode implements MethodeResolution {
    
    @Override
    public ResultatResolution resoudre(ParametresEntree params) {
        // Vérification des paramètres
        if (params.nx <= 0) {
            throw new IllegalArgumentException("Le nombre de volumes doit être positif");
        }
        
        // Création de la fonction source
        Function1D sourceFunction = createSourceFunction(params.fonctionSource);
        
        // Domaine
        double L = params.xMax - params.xMin;
        
        // Création du solveur
        VolumesFinis1DSolver solver = new VolumesFinis1DSolver(
            params.nx, L, 
            params.a, params.b, params.c,
            sourceFunction,
            params.conditionsLimites.u0,
            params.conditionsLimites.uL,
            params.mailleUniforme
        );
        
        // Résolution
        long startTime = System.currentTimeMillis();
        Solution1D sol = solver.solve();
        long endTime = System.currentTimeMillis();
        
        // Construction du résultat
        ResultatResolution resultat = new ResultatResolution();
        resultat.solution = sol.getValues();
        resultat.x = translateMesh(sol.getMeshPoints(), params.xMin);
        resultat.tempsCalcul = endTime - startTime;
        
        return resultat;
    }
    
    @Override
    public String getNomMethode() {
        return "Volumes Finis 1D";
    }
    
    @Override
    public int getDimension() {
        return 1;
    }
    
    /**
     * Crée la fonction source à partir de l'expression
     */
    private Function1D createSourceFunction(String expression) {
        // Implémentation simple pour quelques cas courants
        switch (expression.toLowerCase()) {
            case "0":
            case "zero":
                return x -> 0.0;
            case "1":
            case "un":
                return x -> 1.0;
            case "x":
                return x -> x;
            case "sin(x)":
                return x -> Math.sin(x);
            case "sin(pi*x)":
                return x -> Math.sin(Math.PI * x);
            case "exp(x)":
                return x -> Math.exp(x);
            default:
                // Par défaut, une fonction constante
                try {
                    double val = Double.parseDouble(expression);
                    return x -> val;
                } catch (NumberFormatException e) {
                    System.out.println("Expression non reconnue, utilisation de f(x) = 0");
                    return x -> 0.0;
                }
        }
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
}