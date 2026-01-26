# QuizDemo API Guide

Ce document explique comment utiliser les endpoints de l'API REST pour tester le projet.

> **Note :** Assurez-vous que le serveur est lancé sur le port `8080`.

## 1. Authentification (`/api/auth`)

### 🔐 Se connecter (Login)
Authentifie un utilisateur et retourne un **token** de session.

```bash
# Exemple pour l'utilisateur 'alice'
curl -X POST "http://localhost:8080/api/auth/login?username=alice&password=password123"
```

**Réponse attendue :**
```json
{
  "success": true,
  "token": "a1b2c3d4-e5f6-...",
  "authority": "USER",
  "error": null
}
```

### 🔍 Vérifier un token (Verify)
Vérifie si une session est toujours active.

```bash
# Remplacez <TOKEN> par le token obtenu lors du login
curl "http://localhost:8080/api/auth/verify?token=<TOKEN>"
```

### 🚪 Se déconnecter (Logout)
Invalide le token de session.

```bash
curl -X POST "http://localhost:8080/api/auth/logout?token=<TOKEN>"
```

### 📝 S'inscrire (Register)
Crée un nouvel utilisateur.

```bash
curl -X POST "http://localhost:8080/api/auth/register?username=toto&password=tata"
```

---

## 2. Quiz (`/api/quiz`)

Ces endpoints gèrent la logique du jeu.

### ❓ Nouvelle question
Génère une question et la stocke en session.

```bash
curl "http://localhost:8080/api/quiz/next"
```

### 💡 Répondre
Vérifie la réponse pour la question en cours.

```bash
# Exemple : réponse 42
curl -X POST "http://localhost:8080/api/quiz/reply?guess=42"
```

---

## 👥 Utilisateurs de test par défaut

| Username | Password     | Rôle  |
|----------|--------------|-------|
| `alice`  | `password123`| USER  |
| `bob`    | `secret456`  | USER  |
| `admin`  | `admin`      | ADMIN |
