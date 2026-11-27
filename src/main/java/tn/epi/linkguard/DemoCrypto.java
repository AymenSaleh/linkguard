package tn.epi.linkguard;

import java.security.GeneralSecurityException;

import javax.crypto.Cipher;

// DEMO SonarQube (branche de repetition, jamais fusionnee) : DES est un algorithme de chiffrement faible
class DemoCrypto {

    Cipher chiffreur() throws GeneralSecurityException {
        return Cipher.getInstance("DES");
    }
}