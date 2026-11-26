# LinkGuard

Application web Spring Boot qui analyse un lien et estime son risque de phishing (`FAIBLE`, `MOYEN`, `ELEVE`), livrée avec un pipeline DevSecOps sur GitHub Actions.

Mini-projet DevSecOps — EPI Sousse, 2026-2027.

## Règles d'analyse

| Signal | Points |
|---|---|
| Adresse IP à la place d'un nom de domaine | +3 |
| Symbole `@` dans le lien | +3 |
| Nom de domaine en punycode (`xn--`) | +2 |
| Plus de 3 points dans le nom d'hôte | +2 |
| Pas de HTTPS | +1 |
| Lien de plus de 75 caractères | +1 |
| Mots suspects (`login`, `verify`, `account`…) | +1 |

Score ≥ 5 : `ELEVE` · score ≥ 3 : `MOYEN` · sinon : `FAIBLE`.
Seuls les liens `http` et `https` sont acceptés ; tout autre lien reçoit une réponse HTTP 400.

## Lancer en local

```powershell
.\mvnw.cmd clean package
java -jar target\linkguard-0.0.1-SNAPSHOT.jar
```

Interface : http://localhost:8080 · API : `GET /api/analyze?url=<lien>`

## Docker

```powershell
docker build -t linkguard:local .
docker run -d -p 8080:8080 linkguard:local
```

## Pipeline CI/CD (`.github/workflows/ci.yml`)

Déclenché par un push sur `main` ou sur une branche `feature/**`, et par une pull request vers `main`.

1. Compilation et tests unitaires (Maven)
2. Construction de l'image Docker
3. Test de l'image dans un conteneur
4. Sur `main` uniquement : publication sur Docker Hub (`aymensaleh/linkguard`, tags `<sha du commit>` et `latest`)

## Workflow Git

Une branche `feature/...` par fonctionnalité, une pull request vers `main`, puis « Rebase and merge ».
Messages de commit typés : `feat:`, `fix:`, `test:`, `chore:`, `docs:`.
