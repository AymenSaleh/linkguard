# LinkGuard

[![CI/CD](https://github.com/AymenSaleh/linkguard/actions/workflows/ci.yml/badge.svg?branch=main)](https://github.com/AymenSaleh/linkguard/actions/workflows/ci.yml)
[![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=AymenSaleh_linkguard&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=AymenSaleh_linkguard)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=AymenSaleh_linkguard&metric=coverage)](https://sonarcloud.io/summary/new_code?id=AymenSaleh_linkguard)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=AymenSaleh_linkguard&metric=security_rating)](https://sonarcloud.io/summary/new_code?id=AymenSaleh_linkguard)

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
.\mvnw.cmd clean verify
java -jar target\linkguard-0.0.1-SNAPSHOT.jar
```

`verify` exécute aussi les tests et produit le rapport de couverture JaCoCo : `target\site\jacoco\index.html`.

Interface : http://localhost:8080 · API : `GET /api/analyze?url=<lien>`

## Docker

```powershell
docker build -t linkguard:local .
docker run -d -p 8080:8080 linkguard:local
```

Image durcie : JRE 21 sur Alpine (pas de JDK), correctifs de l'OS appliqués au build (`apk upgrade`), exécution sans root (utilisateur `linkguard`, uid 100).

## Pipeline CI/CD (`.github/workflows/ci.yml`)

Déclenché par un push sur `main` ou sur une branche `feature/**`, et par une pull request vers `main`.

| Étape | Outil | Fait échouer le pipeline si… | Rapport |
|---|---|---|---|
| 1. Secrets dans les commits | Gitleaks | un secret est détecté | artefact `gitleaks-report` (SARIF) |
| 2. Compilation, tests, couverture | Maven + JaCoCo | un test échoue | `target/site/jacoco` |
| 3. Analyse statique (SAST) | SonarQube Cloud | le Quality Gate échoue (sur les PR et sur `main`) | tableau de bord SonarQube Cloud et commentaire sur la PR |
| 4. Construction de l'image | Docker | la construction échoue | — |
| 5. Scan de l'image | Trivy | une vulnérabilité MEDIUM ou plus, corrigeable, est trouvée | artefact `trivy-report` (toutes sévérités) |
| 6. Test du conteneur | curl | l'application ne répond pas | — |
| 7. Publication (sur `main` uniquement) | Docker Hub | — | `aymensaleh/linkguard`, tags `<sha du commit>` et `latest` |

Secrets GitHub utilisés (aucune valeur dans le code) : `DOCKERHUB_USERNAME`, `DOCKERHUB_TOKEN`, `SONAR_TOKEN`, `SONAR_ORGANIZATION`, `SONAR_PROJECT_KEY`.

## Résultats (27 novembre 2026)

- **Gitleaks** : aucune fuite. Un faux secret de test a fait échouer le pipeline, puis l'historique a été réécrit.
- **Trivy** : 0 vulnérabilité MEDIUM ou plus corrigeable après durcissement de l'image (76 avant). Image compressée : de 216,96 Mo à 91,13 Mo.
- **SonarQube Cloud** (`main`) : Quality Gate **Passed** · Security **A** · Reliability **A** · Maintainability **A** · couverture **94,4 %** · duplication **0,0 %**.

## Touche personnelle : chaîne d'approvisionnement et gouvernance

Point de départ : sur la PR #6, SonarQube Cloud a fait échouer le Quality Gate (Security C) à cause de deux actions tierces référencées par un tag (`gitleaks-action@v3`, `login-action@v4`). Un tag peut être déplacé vers du code malveillant, comme ce fut le cas pour `trivy-action` en mars 2026 (CVE-2026-33634). Et la PR rouge restait fusionnable.

1. **Toutes les actions sont épinglées par SHA complet**, avec la version en commentaire, y compris celles de GitHub. Les SHA ont été vérifiés sur les dépôts officiels, et les actions composites (`trivy-action`, puis `setup-trivy`) épinglent elles aussi leurs dépendances.
2. **Politique du dépôt** « Require actions to be pinned to a full-length commit SHA » : toute action non épinglée fait échouer le workflow avant son démarrage.
3. **Ruleset `protect-main`** sur `main` : pull request obligatoire ; checks obligatoires `build-test-push` (source : GitHub Actions) et `SonarCloud Code Analysis` (source : SonarQube Cloud) ; historique linéaire (rebase uniquement) ; ni force push ni suppression. Vérifié : un push direct sur `main` est refusé (`GH013`).

Autres choix de sécurité : token SonarQube Cloud passé par variable d'environnement (règle Sonar S7636) et doté d'une date d'expiration ; version du scanner SonarQube figée dans `pom.xml` ; analyse automatique de SonarQube Cloud désactivée au profit de l'analyse dans le pipeline (couverture et Quality Gate bloquant).

## Workflow Git

Une branche `feature/...` par fonctionnalité, une pull request vers `main`, puis « Rebase and merge » (seule méthode autorisée).
Messages de commit typés : `feat:`, `fix:`, `test:`, `chore:`, `docs:`.
