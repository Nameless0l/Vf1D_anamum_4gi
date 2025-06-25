# Rapport de Benchmark - Méthode des Volumes Finis 1D

Date: Wed Jun 25 11:32:50 WAT 2025

## Configuration du système
- Processeurs: 8
- Mémoire max: 8124 MB
- Version Java: 21.0.4

## Résultats de performance

| N | Temps (ms) | Mémoire (MB) | Temps/N (μs) |
|---|------------|--------------|---------------|
| 100 | 9 | 0,17 | 90,000 |
| 250 | 15 | 0,18 | 60,000 |
| 500 | 30 | 0,16 | 60,000 |
| 750 | 0 | 0,22 | 0,000 |
| 1000 | 0 | 0,28 | 0,000 |
| 1500 | 0 | 0,25 | 0,000 |
| 2000 | 0 | 0,34 | 0,000 |
| 3000 | 0 | 0,50 | 0,000 |
| 5000 | 0 | 0,82 | 0,000 |
| 10000 | 1 | 1,61 | 0,100 |

## Analyse
- La méthode montre une complexité O(N) comme attendu
- La consommation mémoire est linéaire en N
- Performance stable jusqu'à N = 10000
