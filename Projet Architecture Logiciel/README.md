# Projet Architecture Logiciel

## Présentation
Ce projet met en place une architecture orientée microservices moderne et hautement sécurisée. Il comprend un système complet d'authentification (basé sur des tokens paramétrés en base), d'autorisations administrables par rôles (RBAC), une file de messagerie pour la distribution des événements asynchrones, le tout orchestré derrière un reverse-proxy centralisé faisant office d'API Gateway.

## Répartition du Travail
L'équipe s'est répartie le travail sur les différentes briques de l'architecture :
- **Ion CAZACU** : **Système de Messagerie**. (Création du microservice de notification, intégration de RabbitMQ pour l'écoute des événements d'enregistrement, gestion des Dead Letter Queues, et configuration de l'envoi des e-mails avec MailHog).
- **Sofiane OUAHRANI KHALDI** : **Logger / Service d'Authentification**. (Logique métier du service d'identité, gestion des entités et base de données H2, gestion asynchrone des sessions/tokens, et moteur de validation interne des rôles d'accès - RBAC).
- **Virgile LASSAGNE** : **Mise en service du projet et Infrastructure**. (Orchestration Docker et Docker Compose complète, persistance des volumes de données, conception et configuration du Reverse Proxy Nginx en tant qu'API Gateway vérifiant les jetons via `auth_request` en direct pour verrouiller les microservices terminaux).

## Technologies Utilisées
- **Backend / Microservices** : Java 17, Spring Boot, Spring Data JPA / Hibernate
- **Base de Données** : H2 Database (persistée via un volume Docker)
- **Infrastructure & Réseau** : Nginx (Reverse Proxy & API Gateway interne)
- **Message Broker** : RabbitMQ
- **Outil de test Email** : MailHog (Serveur SMTP local)
- **Déploiement / Conteneurisation** : Docker, Docker Compose

## Architecture Fonctionnelle
L'API Gateway (Nginx) constitue l'unique point d'entrée du système (Port `8080`). 
Avant chaque requête vers un service métier (exemple `service-a` ou `service-b`), Nginx interroge de manière transparente le service **Logger** (`/api/auth/validate?requiredRole=...`) pour confirmer :
1. Que le jeton de session est valide et actif.
2. Que l'utilisateur courant possède le Rôle suffisant pour accéder à l'endpoint demandé.
Si l'accès est validé, Nginx transfère la requête avec succès au microservice final. Dans le cas contraire, une erreur `401 Unauthorized` ou `403 Forbidden` propre est renvoyée automatiquement.

## Démarrage Rapide

Assurez-vous d'avoir Docker et Docker Compose installés, ouvrez un terminal à la racine du projet puis exécutez la commande suivante :

```bash
docker-compose down -v && docker-compose build --no-cache && docker-compose up -d
```

Cette commande va compiler les microservices, forger les images et lancer l'ensemble de l'infrastructure.

## Tester le projet
Un guide exhaustif d'utilisation, détaillant le cheminement complet d'un utilisateur (de son inscription jusqu'à la manipulation des rôles par un administrateur), est présent dans le fichier **`utilisation.txt`** à la racine de ce dossier.