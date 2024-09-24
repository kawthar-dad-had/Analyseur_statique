# Analyseur Statique de Code Java

Bienvenue dans le projet **Analyseur Statique de Code Java**. Ce projet est conçu pour analyser des fichiers source Java en utilisant l'API Eclipse JDT et générer un rapport détaillé sur diverses métriques de code, telles que le nombre de classes, de méthodes, et de lignes de code.

## Fonctionnalités

- **Analyse des classes et des méthodes** : Compte le nombre total de classes, de méthodes et de lignes de code dans le projet.
- **Détails des attributs et méthodes** : Affiche les attributs et les méthodes pour chaque classe analysée.
- **Rapport d'analyse** : Génère un rapport d'analyse dans un fichier texte.
- **Statistiques sur le code** : Calcule des statistiques comme le nombre moyen de méthodes par classe et le nombre maximal de paramètres.
- **Graphe d'appel des méthodes** : Visualise les relations entre les méthodes du code analysé.

## Prérequis

- Java JDK 8 ou supérieur
- Maven (pour la gestion des dépendances)
- Eclipse JDT Core (ajouté via Maven)

## Installation

1. **Cloner le dépôt :**
   ```bash
   git clone https://github.com/kawthar-dad-had/Analyseur_statique.git
   cd Analyseur_statique
