import org.junit.Test;

import com.ananum.vf1d.Function1D;
import com.ananum.vf1d.Solution1D;
import com.ananum.vf1d.VolumesFinis1DSolver;

import org.junit.Before;
import static org.junit.Assert.*;

/**
 * Tests unitaires pour la méthode des volumes finis 1D
 */
public class VolumesFinis1DTest {
    
    private static final double TOLERANCE = 1e-10;
    
    @Before
    public void setUp() {
        System.out.println("\n=== Test Volumes Finis 1D ===");
    }
    
    /**
     * Test avec solution constante
     */
    @Test
    public void testSolutionConstante() {
        System.out.println("Test: Solution constante u(x) = 1");
        
        // u'' = 0, u(0) = 1, u(1) = 1 => u(x) = 1
        double L = 1.0;
        double a = 1.0, b = 0.0, c = 0.0;
        Function1D source = x -> 0.0;
        double u0 = 1.0, uL = 1.0;
        
        VolumesFinis1DSolver solver = new VolumesFinis1DSolver(
            20, L, a, b, c, source, u0, uL, true
        );
        
        Solution1D solution = solver.solve();
        double[] values = solution.getValues();
        
        // Vérification: toutes les valeurs doivent être égales à 1
        for (int i = 0; i < values.length; i++) {
            assertEquals("La solution doit être constante égale à 1", 
                        1.0, values[i], 1e-8);
        }
        
        System.out.println("✓ Test réussi");
    }
    
    /**
     * Test avec solution linéaire
     */
    @Test
    public void testSolutionLineaire() {
        System.out.println("Test: Solution linéaire u(x) = x");
        
        // u'' = 0, u(0) = 0, u(1) = 1 => u(x) = x
        double L = 1.0;
        double a = 1.0, b = 0.0, c = 0.0;
        Function1D source = x -> 0.0;
        double u0 = 0.0, uL = 1.0;
        
        VolumesFinis1DSolver solver = new VolumesFinis1DSolver(
            50, L, a, b, c, source, u0, uL, true
        );
        
        Solution1D solution = solver.solve();
        Function1D exact = x -> x;
        
        double error = solver.computeL2Error(solution, exact);
        System.out.println("Erreur L2: " + error);
        
        assertTrue("L'erreur doit être très faible pour une solution linéaire", 
                  error < 1e-6);
        
        System.out.println("✓ Test réussi");
    }
    
    /**
     * Test avec solution polynomiale
     */
    @Test
    public void testSolutionPolynomiale() {
        System.out.println("Test: Solution polynomiale u(x) = x²(1-x)");
        
        double L = 1.0;
        double a = 1.0, b = 0.0, c = 0.0;
        Function1D exact = x -> x * x * (1 - x);
        Function1D source = x -> -2 * (1 - 3 * x);  // -u''
        double u0 = 0.0, uL = 0.0;
        
        VolumesFinis1DSolver solver = new VolumesFinis1DSolver(
            100, L, a, b, c, source, u0, uL, true
        );
        
        Solution1D solution = solver.solve();
        double error = solver.computeL2Error(solution, exact);
        System.out.println("Erreur L2: " + error);
        
        assertTrue("L'erreur doit être inférieure à 1e-3", error < 1e-3);
        
        System.out.println("✓ Test réussi");
    }
    
    /**
     * Test de convergence
     */
    @Test
    public void testConvergence() {
        System.out.println("Test: Ordre de convergence");
        
        double L = 1.0;
        double a = 1.0, b = 0.0, c = 0.0;
        Function1D exact = x -> Math.sin(Math.PI * x);
        Function1D source = x -> Math.PI * Math.PI * Math.sin(Math.PI * x);
        double u0 = 0.0, uL = 0.0;
        
        int[] meshSizes = {10, 20, 40, 80};
        double[] errors = new double[meshSizes.length];
        
        System.out.println("N\tErreur L2\tOrdre");
        
        for (int i = 0; i < meshSizes.length; i++) {
            VolumesFinis1DSolver solver = new VolumesFinis1DSolver(
                meshSizes[i], L, a, b, c, source, u0, uL, true
            );
            
            Solution1D solution = solver.solve();
            errors[i] = solver.computeL2Error(solution, exact);
            
            if (i > 0) {
                double order = Math.log(errors[i-1] / errors[i]) / Math.log(2.0);
                System.out.printf("%d\t%.3e\t%.2f\n", meshSizes[i], errors[i], order);
                
                // L'ordre doit être proche de 2
                assertTrue("L'ordre de convergence doit être > 1.8", order > 1.8);
            } else {
                System.out.printf("%d\t%.3e\t-\n", meshSizes[i], errors[i]);
            }
        }
        
        System.out.println("✓ Test réussi");
    }
    
    /**
     * Test avec convection dominante
     */
    @Test
    public void testConvectionDominante() {
        System.out.println("Test: Convection dominante (Péclet élevé)");
        
        double L = 1.0;
        double a = 0.01, b = 1.0, c = 0.0;  // Pe = 100
        Function1D source = x -> 0.0;
        double u0 = 0.0, uL = 1.0;
        
        VolumesFinis1DSolver solver = new VolumesFinis1DSolver(
            200, L, a, b, c, source, u0, uL, true
        );
        
        Solution1D solution = solver.solve();
        double[] values = solution.getValues();
        
        // Vérification de la stabilité (pas d'oscillations)
        boolean stable = true;
        for (int i = 1; i < values.length - 1; i++) {
            if (values[i] < values[i-1] || values[i] > values[i+1] + 0.1) {
                stable = false;
                break;
            }
        }
        
        assertTrue("La solution doit être stable (monotone)", stable);
        
        // Vérification des conditions aux limites
        assertEquals("Condition en x=0", u0, solution.interpolate(0), 0.01);
        assertEquals("Condition en x=L", uL, solution.interpolate(L), 0.01);
        
        System.out.println("✓ Test réussi - Solution stable");
    }
    
    /**
     * Test avec terme de réaction
     */
    @Test
    public void testAvecReaction() {
        System.out.println("Test: Avec terme de réaction");
        
        double L = 1.0;
        double a = 1.0, b = 0.0, c = -Math.PI * Math.PI;  // c = -π²
        Function1D exact = x -> Math.sin(Math.PI * x);
        Function1D source = x -> 0.0;  // -u'' + cu = 0
        double u0 = 0.0, uL = 0.0;
        
        VolumesFinis1DSolver solver = new VolumesFinis1DSolver(
            100, L, a, b, c, source, u0, uL, true
        );
        
        Solution1D solution = solver.solve();
        double error = solver.computeL2Error(solution, exact);
        System.out.println("Erreur L2: " + error);
        
        assertTrue("L'erreur doit être inférieure à 1e-3", error < 1e-3);
        
        System.out.println("✓ Test réussi");
    }
    
    /**
     * Test avec maillage non uniforme
     */
    @Test
    public void testMailleNonUniforme() {
        System.out.println("Test: Maillage non uniforme");
        
        double L = 1.0;
        double a = 1.0, b = 1.0, c = 0.0;
        Function1D source = x -> 0.0;
        double u0 = 0.0, uL = 1.0;
        
        // Test avec maillage uniforme
        VolumesFinis1DSolver solver1 = new VolumesFinis1DSolver(
            50, L, a, b, c, source, u0, uL, true
        );
        Solution1D solution1 = solver1.solve();
        
        // Test avec maillage non uniforme
        VolumesFinis1DSolver solver2 = new VolumesFinis1DSolver(
            50, L, a, b, c, source, u0, uL, false
        );
        Solution1D solution2 = solver2.solve();
        
        // Les deux solutions doivent être proches
        double maxDiff = 0;
        for (int i = 0; i < 50; i++) {
            double x = i * L / 49.0;
            double diff = Math.abs(solution1.interpolate(x) - solution2.interpolate(x));
            maxDiff = Math.max(maxDiff, diff);
        }
        
        System.out.println("Différence maximale: " + maxDiff);
        assertTrue("Les solutions doivent être proches", maxDiff < 0.1);
        
        System.out.println("✓ Test réussi");
    }
    
    /**
     * Test de performance
     */
    @Test
    public void testPerformance() {
        System.out.println("Test: Performance");
        
        double L = 1.0;
        double a = 1.0, b = 0.5, c = 1.0;
        Function1D source = x -> Math.sin(2 * Math.PI * x);
        double u0 = 0.0, uL = 0.0;
        
        int[] sizes = {100, 500, 1000, 2000};
        
        System.out.println("N\tTemps (ms)");
        
        for (int n : sizes) {
            VolumesFinis1DSolver solver = new VolumesFinis1DSolver(
                n, L, a, b, c, source, u0, uL, true
            );
            
            long startTime = System.currentTimeMillis();
            solver.solve();
            long endTime = System.currentTimeMillis();
            
            System.out.printf("%d\t%d\n", n, endTime - startTime);
            
            // Le temps ne doit pas exploser
            assertTrue("Le temps de calcul doit rester raisonnable", 
                      endTime - startTime < 5000);
        }
        
        System.out.println("✓ Test réussi");
    }
    
    /**
     * Test de validation globale
     */
    @Test
    public void testValidationGlobale() {
        System.out.println("Test: Validation globale");
        
        // Test plusieurs configurations
        testConfiguration(1.0, 0.0, 0.0, "Diffusion pure");
        testConfiguration(0.1, 1.0, 0.0, "Convection-diffusion");
        testConfiguration(1.0, 0.0, 1.0, "Diffusion-réaction");
        testConfiguration(1.0, 1.0, 1.0, "Équation complète");
        
        System.out.println("✓ Tous les tests de validation réussis");
    }
    
    /**
     * Helper pour tester une configuration
     */
    private void testConfiguration(double a, double b, double c, String description) {
        System.out.println("  Configuration: " + description);
        
        double L = 1.0;
        Function1D source = x -> 1.0;
        double u0 = 0.0, uL = 0.0;
        
        VolumesFinis1DSolver solver = new VolumesFinis1DSolver(
            50, L, a, b, c, source, u0, uL, true
        );
        
        Solution1D solution = solver.solve();
        double[] values = solution.getValues();
        
        // Vérifications de base
        assertNotNull("La solution ne doit pas être null", values);
        assertEquals("Le nombre de valeurs doit correspondre", 50, values.length);
        
        // Vérification de la stabilité
        for (double val : values) {
            assertTrue("Les valeurs doivent être finies", Double.isFinite(val));
            assertTrue("Les valeurs doivent être bornées", Math.abs(val) < 1000);
        }
    }
}
