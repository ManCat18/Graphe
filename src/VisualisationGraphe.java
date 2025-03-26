import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;


class GraphePanel extends JPanel {
    private Map<Integer, List<Integer>> graphe;
    private Map<Integer, Integer> affectationCouleurs;
    private ColorationGraphe colorationGraphe;
    private static final Color[] COULEURS = { Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW, Color.ORANGE,
            Color.MAGENTA, Color.PINK, Color.CYAN, Color.DARK_GRAY, Color.GRAY };
    private static final int RAYON = 20;
    private Map<Integer, Point> positions;

    private List<JButton> boutonsColoration = new ArrayList<>(); 
    
    public GraphePanel() {
        JFrame frame = new JFrame("Visualisation du Graphe");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(700, 700);

        frame.add(this, BorderLayout.CENTER);

        JPanel plbt = new JPanel();
        plbt.setLayout(new GridLayout(2, 3, 10, 10)); // Organisation en grille propre

        JPanel panelBoutons = new JPanel(new BorderLayout()); // Panneau principal pour organiser
        panelBoutons.add(plbt, BorderLayout.CENTER); // Ajout du panel des boutons

        frame.add(panelBoutons, BorderLayout.NORTH); // Ajout du panneau principal dans la fenêtre


        Random random = new Random();
        int sommet = 2 + random.nextInt(5);

        JButton generer = new JButton("Générer le graphe");
        JButton backtracking = new JButton("Méthode backtracking");
        JButton firstfit = new JButton("Méthode firstfit");
        JButton largestfit = new JButton("Méthode largestfit");
        JButton smallestlast = new JButton("Méthode smallestlast");
        JButton dsatur = new JButton("Méthode DSatur");

        boutonsColoration.add(generer);
        boutonsColoration.add(backtracking);
        boutonsColoration.add(firstfit);
        boutonsColoration.add(largestfit);
        boutonsColoration.add(smallestlast);
        boutonsColoration.add(dsatur);

        plbt.add(generer);
        plbt.add(backtracking);
        plbt.add(firstfit);
        plbt.add(largestfit);
        plbt.add(smallestlast);
        plbt.add(dsatur);

        frame.add(plbt, BorderLayout.NORTH);
       
        generer.addActionListener(e -> genererGrapheAleatoire(sommet));
        backtracking.addActionListener(e -> appliquerColoration("backtracking", backtracking));
        firstfit.addActionListener(e -> appliquerColoration("firstFit", firstfit));
        largestfit.addActionListener(e -> appliquerColoration("largestFirst", largestfit));
        smallestlast.addActionListener(e -> appliquerColoration("smallestLast", smallestlast));
        dsatur.addActionListener(e -> appliquerColoration("dsatur", dsatur));

        frame.setVisible(true);
        
    }

    private void appliquerColoration(String methode, JButton boutonClique) {
        if (colorationGraphe == null) {
            JOptionPane.showMessageDialog(this, "Veuillez d'abord générer un graphe !");
            return;
        }

        // ✅ Réinitialiser les couleurs des boutons
        for (JButton bouton : boutonsColoration) {
            bouton.setBackground(null);
        }

        // ✅ Appliquer la coloration
        switch (methode) {
            case "backtracking":
                colorationGraphe.colorationBacktracking(0, 6);
                break;
            case "firstFit":
                colorationGraphe.colorationFirstFit();
                break;
            case "largestFirst":
                colorationGraphe.colorationLargestFirst();
                break;
            case "smallestLast":
                colorationGraphe.colorationSmallestLast();
                break;
            case "dsatur":
                colorationGraphe.colorationDSatur();
                break;
        }

        // ✅ Mettre le bouton sélectionné en orange
        boutonClique.setBackground(Color.ORANGE);

        this.affectationCouleurs = colorationGraphe.getAffectationCouleurs(); // ✅ Récupère les nouvelles couleurs
        repaint(); // ✅ Redessine le graphe avec la nouvelle coloration
    }

    private void genererGrapheAleatoire(int nombreSommets) {
        Random random = new Random();
        Map<Integer, List<Integer>> graphe = new HashMap<>();

        // Créé les sommets
        for (int i = 0; i < nombreSommets; i++) {
            graphe.put(i, new ArrayList<>());
        }

        Set<Integer> sommetsAjoutes = new HashSet<>();
        sommetsAjoutes.add(0); // Ajouter le sommet 0 par défaut
        // Crée un graphe plus dense (ajoute plus d'arêtes)
        for (int i = 0; i < nombreSommets; i++) {
            for (int j = i + 1; j < nombreSommets; j++) {
                if (random.nextBoolean()) { // Ajoute une arête avec une certaine probabilité
                    graphe.get(i).add(j);
                    graphe.get(j).add(i);
                    sommetsAjoutes.add(i);
                }
            }
        }

        this.colorationGraphe = new ColorationGraphe(graphe);
        this.affectationCouleurs = colorationGraphe.getAffectationCouleurs();
        //this.setColorationGraphe(colorationGraphe);
        this.setGraphe(graphe);
        genererPositions();
    }

    public void setGraphe(Map<Integer, List<Integer>> graphe) {
        this.graphe = graphe;
        genererPositions();
        repaint();
    }
    
    public void setColorationGraphe(ColorationGraphe colorationGraphe2) {
        this.colorationGraphe = colorationGraphe2;
        repaint();
    }

    private void genererPositions() {
        positions = new java.util.HashMap<>();
        Random rand = new Random();
        for (Integer sommet : graphe.keySet()) {
            positions.put(sommet, new Point(50 + rand.nextInt(400), 50 + rand.nextInt(400)));
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.BLACK);

        // Dessiner les arêtes
        for (Map.Entry<Integer, List<Integer>> entry : graphe.entrySet()) {
            int sommet = entry.getKey();
            Point p1 = positions.get(sommet);
            for (Integer voisin : entry.getValue()) {
                Point p2 = positions.get(voisin);
                g.drawLine(p1.x, p1.y, p2.x, p2.y);
            }
        }

        // Dessiner les sommets
        for (Map.Entry<Integer, Point> entry : positions.entrySet()) {
            int sommet = entry.getKey();
            Point p = entry.getValue();
            int couleurIndex = affectationCouleurs.getOrDefault(sommet, -1) - 1;
            Color couleur = (couleurIndex >= 0 && couleurIndex < COULEURS.length) ? COULEURS[couleurIndex]
                    : Color.BLACK;
            g.setColor(couleur);
            g.fillOval(p.x - RAYON / 2, p.y - RAYON / 2, RAYON, RAYON);
            g.setColor(Color.BLACK);
            g.drawOval(p.x - RAYON / 2, p.y - RAYON / 2, RAYON, RAYON);
            g.drawString(String.valueOf(sommet), p.x - 5, p.y - 10);
        }
    }
}

public class VisualisationGraphe {
    public static void main(String[] args) {
        /*Random random = new Random();
        int rand = 5 + random.nextInt(5); // pour modifier le nb de sommets 
        Map<Integer, List<Integer>> graphe = ColorationGraphe.genererGrapheAleatoire(rand);
        ColorationGraphe coloration = new ColorationGraphe(graphe);
        //coloration.colorationBacktracking(0,4);
        //coloration.colorationFirstFit();
        //coloration.colorationLargestFirst();
        //coloration.colorationSmallestLast();
        //coloration.colorationDSatur();

        JFrame frame = new JFrame("Visualisation du Graphe");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 500);
        //frame.add(new GraphePanel(graphe, coloration.getAffectationCouleurs()));

        frame.setVisible(true);*/
        SwingUtilities.invokeLater(GraphePanel::new);
    }
}
