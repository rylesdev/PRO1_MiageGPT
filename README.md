# MiageGPT

MiageGPT est un agent conversationnel développé en Java/JavaFX, conçu pour répondre aux questions des étudiants MIAGE de l'Université Paris 1 Panthéon-Sorbonne. Le projet a été réalisé en partenariat avec l'Association MIAGE Sorbonne (AMS), développé en équipe selon la méthode Agile Scrum.

L'application permet aux étudiants d'obtenir des réponses rapides sur les cours, les débouchés, les stages et la vie associative MIAGE, via une interface graphique intuitive.

## Stack technique

- Java 21 / JavaFX
- API Groq (LLM)
- Agile Scrum

## Téléchargement

Une release est disponible directement sur GitHub, permettant de lancer l'application sans compiler le projet.

Rendez-vous dans l'onglet **Releases** du repository et téléchargez la dernière version du fichier `MiageGPT-SNAPSHOT.jar`.

Java 21+ doit être installé sur votre machine pour exécuter le fichier.

## Prérequis

- Java 21+
- Une clé API Groq (gratuite)

## Configuration de la clé API

Au premier lancement, une fenêtre de configuration s'affiche automatiquement. Voici les étapes à suivre :

1. Cliquez sur le lien https://console.groq.com/keys dans la fenêtre
2. Créez un compte ou connectez-vous sur le site Groq
3. Cliquez sur **Create API Key** et copiez la clé générée (elle commence par `gsk_`)
4. Collez la clé dans le champ de la fenêtre de configuration
5. Cliquez sur **Confirmer**

Aux lancements suivants, deux options sont disponibles :

- **Garder la clé actuelle** : se connecter automatiquement avec la dernière clé utilisée
- **Utiliser cette clé** : saisir une nouvelle clé API
