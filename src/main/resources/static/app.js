const formulaire = document.getElementById('form-analyse');
const champ = document.getElementById('lien');
const resultat = document.getElementById('resultat');
const niveau = document.getElementById('niveau');
const score = document.getElementById('score');
const raisons = document.getElementById('raisons');
const erreur = document.getElementById('erreur');

function afficherErreur(message) {
    erreur.textContent = message;
    erreur.hidden = false;
}

formulaire.addEventListener('submit', async (event) => {
    event.preventDefault();
    resultat.hidden = true;
    erreur.hidden = true;
    try {
        const reponse = await fetch('/api/analyze?url=' + encodeURIComponent(champ.value));
        const donnees = await reponse.json();
        if (!reponse.ok) {
            afficherErreur(donnees.erreur || 'Requête refusée.');
            return;
        }
        niveau.textContent = donnees.level;
        niveau.className = donnees.level.toLowerCase();
        score.textContent = donnees.score;
        raisons.replaceChildren();
        const liste = donnees.reasons.length > 0 ? donnees.reasons : ['Aucun signal suspect détecté.'];
        for (const texte of liste) {
            const element = document.createElement('li');
            element.textContent = texte;
            raisons.appendChild(element);
        }
        resultat.hidden = false;
    } catch {
        afficherErreur('Le service ne répond pas.');
    }
});
