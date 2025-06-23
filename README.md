# Méthode des Volumes Finis 1D

## Description

Implémentation de la méthode des volumes finis pour résoudre l'équation différentielle ordinaire du second ordre :

```
aU'' + bU' + cU = f(x)
```

sur le domaine `[0, L]` avec conditions aux limites de Dirichlet : `U(0) = u0` et `U(L) = uL`.

## Structure du Projet

```
cm.ananum.vf1d/
├── VolumesFinis1DSolver.java    # Solveur principal
├── VolumesFinis1DMain.java      # Programme avec interface utilisateur
├── VolumesFinis1DTest.java      # Tests unitaires
└── integration/
    └── VolumesFinis1DMethode.java # Interface pour l'intégration

cm.ananum.integration/
├── MethodeResolution.java        # Interface commune
├── ParametresEntree.java         # Paramètres unifiés
├── ResultatResolution.java       # Format de sortie
└── ProgrammePrincipalUnifie.java # Programme principal unifié
```

## Utilisation

### 1. Exécution Standalone

```bash
javac cm/ananum/vf1d/*.java
java cm.ananum.vf1d.VolumesFinis1DMain
```

### 2. Intégration dans le Programme Principal

```java
// Création des paramètres
ParametresEntree params = new ParametresEntree();
params.a = 1.0;  // Coefficient de diffusion
params.b = 0.5;  // Coefficient de convection
params.c = 0.0;  // Coefficient de réaction
params.xMin = 0.0;
params.xMax = 1.0;
params.nx = 100;
params.conditionsLimites.u0 = 0.0;
params.conditionsLimites.uL = 1.0;
params.fonctionSource = "sin(pi*x)";
params.mailleUniforme = true;

// Résolution
VolumesFinis1DMethode methode = new VolumesFinis1DMethode();
ResultatResolution resultat = methode.resoudre(params);

// Affichage
resultat.afficherResume();
```

### 3. Utilisation Directe du Solveur

```java
// Définition du problème
double L = 1.0;
double a = 1.0, b = 0.0, c = 0.0;
Function1D source = x -> Math.sin(Math.PI * x);
double u0 = 0.0, uL = 0.0;

// Création et résolution
VolumesFinis1DSolver solver = new VolumesFinis1DSolver(
    100, L, a, b, c, source, u0, uL, true
);
Solution1D solution = solver.solve();

// Accès aux résultats
double[] values = solution.getValues();
double[] meshPoints = solution.getMeshPoints();
```

## Caractéristiques

### Méthode Numérique

- **Discrétisation** : Volumes de contrôle avec points au centre
- **Flux Diffusifs** : Approximation par différences centrées
- **Flux Convectifs** : Schéma upwind pour la stabilité
- **Système Linéaire** : Matrice tridiagonale
- **Résolution** : Gauss-Seidel itératif ou Thomas direct

### Options

- **Maillage uniforme** : Distribution régulière des volumes
- **Maillage non uniforme** : Raffinement aux bords (optionnel)
- **Tolérance** : 1e-10 par défaut
- **Itérations max** : 10000 par défaut

### Performances

- Complexité : O(N) pour N volumes
- Mémoire : O(N) pour le stockage tridiagonal
- Convergence : Ordre 2 en espace

## Tests et Validation

### Tests Unitaires

```bash
# Compilation avec JUnit
javac -cp junit-4.13.2.jar:. cm/ananum/vf1d/*.java

# Exécution des tests
java -cp junit-4.13.2.jar:hamcrest-core-1.3.jar:. \
     org.junit.runner.JUnitCore cm.ananum.vf1d.VolumesFinis1DTest
```

### Cas de Test Inclus

1. **Solution constante** : Vérification de l'exactitude
2. **Solution linéaire** : Test de précision machine
3. **Solution polynomiale** : Validation de l'ordre
4. **Convergence** : Vérification de l'ordre 2
5. **Convection dominante** : Test de stabilité (Pe élevé)
6. **Avec réaction** : Équation complète
7. **Maillage non uniforme** : Comparaison des maillages

### Problèmes Tests Prédéfinis

1. **Test Polynomial** : `u(x) = x²(L-x)`, solution exacte
2. **Test Trigonométrique** : `u(x) = sin(πx)`, convergence
3. **Test Convection-Diffusion** : Nombre de Péclet variable
4. **Analyse de Convergence** : Ordre numérique complet

## Exemples de Résultats

### Convergence pour u(x) = sin(πx)

```
N     Erreur L2    Ordre
--------------------------------
10    1.234e-02    -
20    3.156e-03    1.97
40    7.932e-04    1.99
80    1.985e-04    2.00
160   4.964e-05    2.00
```

### Performance

```
N      Temps (ms)
-----------------
100    5
500    12
1000   28
2000   75
```

## Format de Sortie

### Export CSV

Les résultats peuvent être exportés au format CSV :

```csv
# Résultats - Volumes Finis 1D
# Temps de calcul: 25 ms
# Itérations: 156
x,u
0.005000,0.015707
0.015000,0.047112
...
```

### Visualisation

Le programme inclut une visualisation ASCII simple :

```
  Max = 1.000000
  |                    ****                  
  |                 ***    ***               
  |               **          **             
  |             **              **           
  |           **                  **         
  |         **                      **       
  |       **                          **     
  |     **                              **   
  |   **                                  **   
  | **                                      ** 
  +---------------------------------------------> x
  Min = 0.000000
  0                                           L
```

## Intégration avec le Programme Principal

Pour integrer, voici l'interface à implémenter dans le programme principal :

```java
public class ResolveurPrincipal {
    private Map<String, MethodeResolution> methodes;
  
    public ResolveurPrincipal() {
        methodes = new HashMap<>();
        methodes.put("VF1D", new VolumesFinis1DMethode());
        // Ajouter les autres méthodes ici
    }
  
    public void resoudre(String methode, ParametresEntree params) {
        MethodeResolution solver = methodes.get(methode);
        if (solver != null) {
            ResultatResolution resultat = solver.resoudre(params);
            // Traitement des résultats
        }
    }
}
```

## Contact et Support

Groupe VF 1D :

- @Nameless
- @
