import java.util.*;
import java.io.*;

class ColorationGraphe {
    // Déclaration des attributs : graphe et affactation des couleurs avec leur correspondance via le tableau COULEUR
    private Map<Integer, List<Integer>> graphe;
    private Map<Integer, Integer> affectationCouleurs;
    private static final String[] COULEURS = {"Rouge", "Vert", "Bleu", "Jaune", "Orange", "Violet", "Rose", "Cyan", "Marron", "Gris"};

    // Constructeur qui initialise les attributs
    public ColorationGraphe(Map<Integer, List<Integer>> graphe) {
        this.graphe = graphe;
        this.affectationCouleurs = new HashMap<>();
    }

    public Map<Integer, Integer> getAffectationCouleurs() {
        return affectationCouleurs;
    }

    // Méthode pour obtenir le nom de la couleur à partir du numéro
    // Utilisée pour la génération du fichier CSV pour savoir quel sommet a quelle couleur
    private String obtenirCouleurNom(int couleur) {
        // Retourne le nom de la couleur si elle est valide (numéro compris entre 1 et 10) sinon renvoie le message
        if (couleur >= 1 && couleur <= COULEURS.length) {
            return COULEURS[couleur - 1];
        } else {
            return "Couleur non disponible";
        }
    }

    // Méthode vérifiant que la couleur peut être affectée au sommet 
    private boolean couleurValide(int sommet, int couleur) {
        // Vérifie que le sommet n'a pas de voisin ayant la même couleur que lui
        for (int voisin : graphe.get(sommet)) {
            if (affectationCouleurs.getOrDefault(voisin, -1) == couleur) {
                return false;
            }
        }
        return true;
    }

    // Méthode de coloration par backtracking
    // int sommet est l'indice par lequel on commence à colorier
    public boolean colorationBacktracking(int sommet, int maxCouleurs) {
        // Renvoie true si tous les sommets sont colorés
        if (sommet == graphe.size())
            return true;
        // Essaye de colorier le sommet avec chaque couleur 
        for (int couleur = 1; couleur <= maxCouleurs; couleur++) {
            if (couleurValide(sommet, couleur)) {
                affectationCouleurs.put(sommet, couleur); // Affecte la couleur au sommet 
                // Passe au sommet suivant 
                if (colorationBacktracking(sommet + 1, maxCouleurs))
                    return true; // La solution à été trouvée
                affectationCouleurs.remove(sommet); // Annule si la solution n'est pas possible
            }
        }
        return false; // Retourne si aucune solution n'est trouvée
    }

    // Méthode de coloration First Fit
    public void colorationFirstFit() {
        for (int sommet : graphe.keySet()) {
            Set<Integer> couleursUtilisees = new HashSet<>();
            // Vérifie pour chaque sommet les couleurs utilisées par les voisins 
            for (int voisin : graphe.get(sommet)) {
                if (affectationCouleurs.containsKey(voisin)) {
                    couleursUtilisees.add(affectationCouleurs.get(voisin));
                }
            }
            int couleur = 1;
            // Cherche la première couleur non utilisée par les voisins en cherchant dans couleursutilisee si elle est présente ou non
            while (couleursUtilisees.contains(couleur))
                couleur++;
            // Affecte la couleur au sommet une fois que la couleur est trouvée
            affectationCouleurs.put(sommet, couleur);
        }
    }

    // Méthode firstFit prenant en paramètre une liste pour ordonner la coloration
    private void colorationFirstFitAvecOrdre(List<Integer> ordre) {
        // Parcours des sommets dans un ordre spécifique
        for (int sommet : ordre) {
            // Rassemble les couleurs utilisées par les voisins 
            Set<Integer> couleursUtilisees = new HashSet<>();
            // Même principe que FirstFit
            for (int voisin : graphe.get(sommet)) {
                if (affectationCouleurs.containsKey(voisin)) {
                    couleursUtilisees.add(affectationCouleurs.get(voisin));
                }
            }
            int couleur = 1;
            while (couleursUtilisees.contains(couleur))
                couleur++;
            affectationCouleurs.put(sommet, couleur);
        }
    }

    // Algorithme Largest First
    public void colorationLargestFirst() {
        // Créé une liste de tous les sommets du graphe 
        List<Integer> sommets = new ArrayList<>(graphe.keySet());
        // Trie les sommets en fonction de leur nombre de voisins (degré) : ceux ayant le plus grand nombre passent en premier
        sommets.sort((a, b) -> Integer.compare(graphe.get(b).size(), graphe.get(a).size()));
        // Appelle de la méthode FirstFit ordonnée pour attribuer rapidement les couleurs après avoir défini l'ordre de coloration
        colorationFirstFitAvecOrdre(sommets);
    }

    // Algorithme Smallest Last
    public void colorationSmallestLast() {
        // Créé une liste vide pour définir l'ordre de coloration des sommets
        List<Integer> ordre = new ArrayList<>();
        // Liste des sommets n'étant pas encore coloriés 
        Set<Integer> sommetsRestants = new HashSet<>(graphe.keySet());
        // Tant qu'il reste des sommets à colorier
        while (!sommetsRestants.isEmpty()) {
            // Trouve le sommet ayant le moins de voisin
            int sommet = sommetsRestants.stream().min(Comparator.comparingInt(n -> graphe.get(n).size())).orElseThrow();
            // Ajoute le sommet à l'ordre de coloration et le retire de la liste des sommets restants
            ordre.add(sommet);
            sommetsRestants.remove(sommet);
        }
        // Inverse l'ordre des sommets pour colorier du plus petit au plus grand degré puis appelle FirstFit avec ordre
        Collections.reverse(ordre);
        colorationFirstFitAvecOrdre(ordre);
    }

    // Algorithme DSatur
    public void colorationDSatur() {
        // Stocke la saturation (nombre de voisins coloriés) de chaque sommet
        Map<Integer, Integer> saturation = new HashMap<>();
        // Initialise la saturation de chaque sommet à 0
        for (int sommet : graphe.keySet())
            saturation.put(sommet, 0);
        // Tant qu'il reste des sommets à colorier
        while (affectationCouleurs.size() < graphe.size()) {
            // Trouve le sommet avec la plus grande saturation
            int sommet = graphe.keySet().stream().filter(n -> !affectationCouleurs.containsKey(n)).max(Comparator.comparingInt(n -> saturation.get(n))).orElseThrow();
            // Parcours les voisins et recense les couleurs utilisées
            Set<Integer> couleursUtilisees = new HashSet<>();
            for (int voisin : graphe.get(sommet)) {
                if (affectationCouleurs.containsKey(voisin)) {
                    couleursUtilisees.add(affectationCouleurs.get(voisin));
                }
            }
            int couleur = 1;
            while (couleursUtilisees.contains(couleur))
                couleur++;
            affectationCouleurs.put(sommet, couleur);

            // Met à jour la saturation des voisin du sommet 
            for (int voisin : graphe.get(sommet)) {
                if (!affectationCouleurs.containsKey(voisin)) {
                    saturation.put(voisin, saturation.get(voisin) + 1);
                }
            }
        }
    }

    // Méthode pour enregistrer le graphe produit dans un fichier csv à ouvrir avec Cytoscape
    /*public void enregistrerGrapheDansCSV(String nomFichier) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(nomFichier))) {
            // Entête
            writer.write("Sommet,Couleur,AttributCouleur\n");
            // Ecrit chaque sommet avec sa couleur affectée en reprenant la correspondance dans COULEUR
            for (Map.Entry<Integer, Integer> entree : affectationCouleurs.entrySet()) {
                writer.write(entree.getKey() + "," +entree.getValue()+","+obtenirCouleurNom(entree.getValue()));
                writer.newLine();
            }
            System.out.println("Le graphe coloré a été enregistré dans " + nomFichier);
        } catch (IOException e) {
            System.out.println("Erreur lors de l'enregistrement du fichier : " + e.getMessage());
        }
    }*/
}
 
// Réponses aux questions 
/*
 * Jusqu’à combien de nœuds pouvons-nous trouver la solution optimale ?
 * → Cela dépend du graphe, mais l’algorithme exact (backtracking) devient
 * impraticable pour des graphes au-delà de 30-40 sommets en raison de sa
 * complexité exponentielle.
 * 
 * Performance des méthodes approximatives sur de plus gros graphes ?
 * → Les méthodes heuristiques (DSatur, First Fit, etc.) sont plus rapides et
 * peuvent colorier des graphes de plusieurs centaines de sommets en un temps
 * raisonnable, mais elles peuvent produire des solutions sous-optimales.
 * 
 * En bonus : test avec une grille de Sudoku ?
 * → Un Sudoku peut être vu comme un graphe où chaque case est un sommet et
 * chaque contrainte (ligne, colonne, région) définit les arêtes. Un solveur de
 * Sudoku via coloration de graphe est envisageable !
 */