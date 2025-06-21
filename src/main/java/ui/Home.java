//TODO
/*
    -> evasione, critico, effetti visivi ( check + o meno se statistiche aumentate)
    -> aggiungere più tipologie di skills
 */


package ui;

import logic.*;
import logic.entita.Entita;
import logic.entita.MaxValues;
import logic.entita.UpgradePoints;
import logic.enums.PassiveEffect;
import logic.enums.TypeOfSkill;
import logic.skills.*;
import logic.livelli.Level;
import logic.livelli.Round;
import logic.save.GameSave;
import logic.save.GameSaveUtil;
import lombok.extern.slf4j.Slf4j;


import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static logic.enums.ReturnValues.*;
import static logic.enums.PassiveEffect.*;
import static logic.enums.Target.*;
import static ui.UIUtils.*;

/**
 * Main game window class that handles the UI and game flow
 */
@Slf4j
public class Home extends JFrame {
    private boolean reset = false;
    // Game state variables
    private Level livelloCorrente;
    private int indiceRoundCorrente;
    private List<Entita> nemiciCorrenti;
    private Entita eroe;

    // UI components
    private JPanel panelContainer;
    private CardLayout cardLayout;
    private JScrollPane scrollPane;
    private final JTextArea logArea = creaArea("", 0, 30, 100, 20, 20);


    /**
     * Initializes and shows the main game window
     */
    public void start() {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
            } catch (Exception ignored) {
            }

            JFrame frame = new JFrame("gioco");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(UIUtils.getX(100), UIUtils.getY(100));

            cardLayout = new CardLayout();
            panelContainer = new JPanel(cardLayout);
            panelContainer.setBounds(0, UIUtils.getY(0), UIUtils.getX(100), UIUtils.getY(100));

            try {
                panelContainer.add(inizio(), "Home");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            frame.add(panelContainer);
            cardLayout.show(panelContainer, "Home");

            frame.setVisible(true);
        });
    }
    //  LIVELLI  <- INIZIO -> STATS -> PASSIVA -> CREAPASSIVA/SKILL -> DOMAIN -> CREA DOMAIN -> NUOVO PG
    /**
     * Creates and returns the initial game panel
     */
    public Pagina inizio() throws IOException {
        JPanel panel = new JPanel();
        panel.setLayout(null);
        panel.setBackground(Color.lightGray);

        // Load saved game state
        File file = new File("save.json");
        if (!file.exists() || GameSaveUtil.caricaDaFile("save.json").getPersonaggio() == null) {
            GameSave save = new GameSave(prePg(), List.of(0));
            GameSaveUtil.salvaSuFile(save, "save.json");
        }
        //CARICAMENTO DATI
        GameSave save = GameSaveUtil.caricaDaFile("save.json");
        eroe = save.getPersonaggio();
        if(eroe.getDomain() != null && !eroe.getDomain_acquisiti().contains(eroe.getDomain()))
            eroe.getDomain_acquisiti().add(eroe.getDomain());

        panel.revalidate();
        panel.repaint();

        //CONFIGURAZIONE LOG DI GIOCO
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);
        logArea.setEditable(false);
        scrollPane = new JScrollPane(logArea,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED
        );
        scrollPane.setPreferredSize(new Dimension(400, 200));
        //panel.add(scrollPane, BorderLayout.CENTER);

        var btnSin = new JButton("< Livelli");
        btnSin.addActionListener(_ -> {
            panelContainer.add(sceltaLvl(), "livelli");
            cardLayout.show(panelContainer, "livelli");
        });
        var btnDs = new JButton("Stats >");
        btnDs.addActionListener(_ -> {
            panelContainer.add(statsPagina(eroe), "stats");
            cardLayout.show(panelContainer, "stats");
        });

        panel.add(creaLabel(
                "A SINISTRA si trovano i livelli",
                33, 20, 20, 10, 18, null
        ));
        panel.add(creaLabel(
                "<html>A DESTRA si trovano rispettivamente: <br>   Statistiche > <br>  Passiva >> <br> Creazione Skill/Passiva >>> <br> Dominio >>>> <br> Creazione Dominio>>>>> <br> Nuovo Personaggio</html>",
                23, 40, 45, 30, 18, null
        ));

        return new Pagina("Benvenuto", panel, btnSin, btnDs, null);
    }

    //PAGINE-PAGES
    /**
     * Creates a new domain customization panel where users can configure and save their domain properties.
     * The customizable properties include domain name, damage, activation probability, duration, cooldown,
     * and three passive effects. This method adds the panel to the provided container.
     *

     */
    public Pagina creaDomain(){
        var btnSin = new JButton("< Domain");
        btnSin.addActionListener(_ -> {
            panelContainer.add(mostraDomainPagina(eroe), "Domain");
            cardLayout.show(panelContainer, "Domain");
        });
        var btnDs = new JButton("Resetta/crea salvataggio >");
        btnDs.addActionListener(_ -> {
            panelContainer.add(creaEroe(), "creaEroe");
            cardLayout.show(panelContainer, "creaEroe");
        });

        if(eroe.getDomain() == null && Objects.equals(eroe.getNome(), "crea un nuovo account"))
            return new Pagina("CREA UN NUOVO PERSONAGGIO", new JPanel(), btnSin, btnDs, null);
        if(eroe.getLvl() < 10)
            return new Pagina("Raggiungi Livello 10 e vedrai", new JPanel(), btnSin, btnDs, null);;
        AtomicInteger punti = new AtomicInteger(70);
        //per comboBox
        Integer[] probabilita = {10, 20, 30, 40, 50, 60, 70, 80, 90, 100};

        JComboBox<Integer> comboBox = new JComboBox<>(probabilita);

        JComboBox<PassiveEffect> effectCombo = new JComboBox<>(
                effetti().toArray(new PassiveEffect[0])
        );
        JComboBox<PassiveEffect> effectCombo1 = new JComboBox<>(
                effetti().toArray(new PassiveEffect[0])
        );
        JComboBox<PassiveEffect> effectCombo2 = new JComboBox<>(
                effetti().toArray(new PassiveEffect[0])
        );
        //slider per il danno
        // Crea uno slider che va da 0 a 300, valore iniziale 100
        JSlider sliderDanno = new JSlider(1, 300, 1);
        // Mostra i valori min/max e le tacche
        sliderDanno.setMajorTickSpacing(50); // tacche grandi ogni 25
        sliderDanno.setMinorTickSpacing(10);  // tacche piccole ogni 5
        sliderDanno.setPaintTicks(true);     // mostra tacche
        sliderDanno.setPaintLabels(true);    // mostra numeri

        //slider per la durata
        JSlider sliderDurata = new JSlider(1, 10, 1);
        // Mostra i valori min/max e le tacche
        sliderDurata.setMajorTickSpacing(1); // tacche grandi ogni 25
        sliderDurata.setPaintTicks(true);     // mostra tacche
        sliderDurata.setPaintLabels(true);    // mostra numeri

        //slider per la durata
        JSlider sliderCooldown = new JSlider(3, 10, 3);
        // Mostra i valori min/max e le tacche
        sliderCooldown.setMajorTickSpacing(1); // tacche grandi ogni 25
        sliderCooldown.setPaintTicks(true);     // mostra tacche
        sliderCooldown.setPaintLabels(true);    // mostra numeri



        JPanel creaDomain = new JPanel();
        creaDomain.setLayout(new GridLayout(10, 2));
        creaDomain.setBounds(UIUtils.getX(15), UIUtils.getY(20), UIUtils.getX(29), UIUtils.getY(50));
        creaDomain.setBackground(Color.white);

        //display dei punti
        creaDomain.add(new JLabel("Crea il tuo domain: "));
        var labelPunti = creaLabel("Punti rimanenti: " + punti.get() + " / 70", 1, 20, 10, 10, 18, Color.black);
        creaDomain.add(labelPunti);

        //nome
        creaDomain.add(new JLabel("Nome del dominio: "));
        var areaNome = creaArea("", 1, 20, 10, 10, 20);
        creaDomain.add(areaNome);

        //danno
        creaDomain.add(new JLabel("Danno del dominio: "));
        creaDomain.add(sliderDanno);

        //chance
        creaDomain.add(new JLabel("Probabilità di attivazione: "));
        creaDomain.add(comboBox);

        //durata
        creaDomain.add(new JLabel("Durata"));
        creaDomain.add(sliderDurata);

        //cooldown
        creaDomain.add(new JLabel("Cooldown"));
        creaDomain.add(sliderCooldown);

        //passive1
        creaDomain.add(new JLabel("Effetto passivo 1: "));
        creaDomain.add(effectCombo);

        //passive2
        creaDomain.add(new JLabel("Effetto passivo 2: "));
        creaDomain.add(effectCombo1);

        //passive3
        creaDomain.add(new JLabel("Effetto passivo 3: "));
        creaDomain.add(effectCombo2);

        //AGGIORNAMENTO PUNTI IN TEMPO REALE:
        Runnable aggiornaPunti = () -> {
            int nuovoTot = 70;
            nuovoTot -= sliderDanno.getValue() / 10;
            nuovoTot -= sliderDurata.getValue() * 2;
            nuovoTot += sliderCooldown.getValue() * 2;
            nuovoTot -= Integer.parseInt(Objects.requireNonNull(comboBox.getSelectedItem()).toString()) / 5;
            nuovoTot = parsePuntiEffetti(nuovoTot, (PassiveEffect) effectCombo.getSelectedItem());
            nuovoTot = parsePuntiEffetti(nuovoTot, (PassiveEffect) effectCombo1.getSelectedItem());
            nuovoTot = parsePuntiEffetti(nuovoTot, (PassiveEffect) effectCombo2.getSelectedItem());

            punti.set(nuovoTot);
            labelPunti.setText("Punti rimanenti: " + punti.get() + " / 70");
        };
        //bottone per salvare
        creaDomain.add(new JLabel("bottone per salvare"));
        JButton creaDomainSalva = creabottone("Salva", 1, 70, 10, 10, 14);
        creaDomainSalva.addActionListener(_ -> {
            aggiornaPunti.run();
            if (punti.get() <= 0) {
                mostraErrore("ciccione", "errore: non abbastanza punti, sei un ciccione");
                cardLayout.show(panelContainer, "inizio");
            } else {

                Domain domainNuovo = new Domain(
                        areaNome.getText(),
                        sliderDanno.getValue(),
                        sliderCooldown.getValue(),
                        sliderCooldown.getValue(),
                        sliderDurata.getValue(),
                        sliderDurata.getValue(),
                        comboBox.getSelectedItem() == null ? 10 : (Integer) comboBox.getSelectedItem(),
                        false,
                        AOE,        //type of target
                        List.of(    //Passive effects
                                (PassiveEffect)Objects.requireNonNull(effectCombo.getSelectedItem()),
                                (PassiveEffect)Objects.requireNonNull(effectCombo1.getSelectedItem()),
                                (PassiveEffect)Objects.requireNonNull(effectCombo2.getSelectedItem())
                        ),
                        0
                );
                areaNome.setText("");
                if(eroe.getDomain() != null && !eroe.getDomain_acquisiti().contains(eroe.getDomain())) {
                    eroe.getDomain_acquisiti().add(eroe.getDomain());
                    try {
                        GameSaveUtil.salvaSuFile(new GameSave(eroe, List.of(0)), "save.json");
                        panelContainer.add(inizio(), "inizio");
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                eroe.getDomain_acquisiti().add(domainNuovo);
                cardLayout.show(panelContainer, "Domain");
            }
        });
        creaDomain.add(creaDomainSalva);


        //quando cambio aggiorno i punti
        sliderDanno.addChangeListener(_ -> aggiornaPunti.run());
        sliderDurata.addChangeListener(_ -> aggiornaPunti.run());
        sliderCooldown.addChangeListener(_ -> aggiornaPunti.run());
        comboBox.addActionListener(_ -> aggiornaPunti.run());
        effectCombo.addActionListener(_ -> aggiornaPunti.run());
        effectCombo1.addActionListener(_ -> aggiornaPunti.run());
        effectCombo2.addActionListener(_ -> aggiornaPunti.run());


        JPanel pannelloFinale = new JPanel(new GridLayout(1, 2));
        pannelloFinale.add(creaDomain);
        pannelloFinale.add(sceltaDomain());

        return new Pagina("Crea Domain", pannelloFinale, btnSin, btnDs, null);
    }
    public Pagina statsPagina(Entita entita) {
        var btnSin = new JButton("< Inizio");
        btnSin.addActionListener(_ -> {
            try {
                panelContainer.add(inizio(), "home");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            cardLayout.show(panelContainer, "home");
        });
        var btnDs = new JButton("Passiva >");
        btnDs.addActionListener(_ -> {
            panelContainer.add(mostraSkillsPagina(entita), "passiva");
            cardLayout.show(panelContainer, "passiva");
        });
        JPanel pannello = new JPanel(new GridLayout(1, 2));
        pannello.add(stats(entita));
        pannello.add(puntiLivelli(entita));

        return new Pagina("Statistiche di " + entita.getNome(), pannello, btnSin, btnDs, null);
    }
    public Pagina mostraSkillsPagina(Entita entita){
        var btnSin = new JButton("< Stats");
        btnSin.addActionListener(_ -> {
            panelContainer.add(statsPagina(entita), "stats");
            cardLayout.show(panelContainer, "stats");
        });
        var btnDs = new JButton("Crea passiva/skill >");
        btnDs.addActionListener(_ -> {
            panelContainer.add(creaSkill(), "creaSkill");
            cardLayout.show(panelContainer, "creaSkill");
        });
        if(eroe.getPassiva() == null)
            return new Pagina("Passiva : CREA UN NUOVO PERSONAGGIO", null, btnSin, btnDs, null);
        return new Pagina("Passiva : " + entita.getPassiva().getNome(), null, btnSin, btnDs, mostraSkills(eroe));
    }
    public Pagina mostraDomainPagina(Entita entita){
        var btnSin = new JButton("< Crea passiva/skill");
        btnSin.addActionListener(_ -> {
            panelContainer.add(creaSkill(), "creaSkill");
            cardLayout.show(panelContainer, "creaSkill");
        });
        var btnDs = new JButton("Crea Domain >");
        btnDs.addActionListener(_ -> {
            panelContainer.add(creaDomain(), "creaDomain");
            cardLayout.show(panelContainer, "creaDomain");
        });
        if(entita.getDomain() == null)
            return new Pagina("Domain : CREA UN NUOVO PERSONAGGIO", null, btnSin, btnDs, null);
        if(entita.getLvl() < 10)
            return new Pagina("Raggiungi livello 10 e vedrai", new JPanel(), btnSin, btnDs, null);
        return new Pagina("Domain : " + entita.getDomain().getNome(), mostraDomain(entita), btnSin, btnDs, null);
    }
    /**
     * Creates UI for creating a new hero character
     */
    public Pagina creaEroe() {
        JPanel stats = new JPanel();
        stats.setBounds(UIUtils.getX(70), UIUtils.getY(60), UIUtils.getX(25), UIUtils.getY(15));
        stats.setLayout(new GridLayout(4, 2, 12, UIUtils.getY(1)));
        stats.setBackground(Color.white);

        // Add name input
        stats.add(new JLabel("Nome: "));
        JTextArea nome = creaArea("", 1, 20, 10, 10, 20);
        stats.add(nome);

        // Add special attack input
        stats.add(new JLabel("Attacco Speciale: "));
        JTextArea nomeMossa = creaArea("", 1, 20, 10, 10, 20);
        stats.add(nomeMossa);

        stats.add(new JLabel("Nome Passiva: "));
        JTextArea nomePassiva = creaArea("", 1, 20, 10, 10, 20);
        stats.add(nomePassiva);

        // Create a hero button
        JButton crea = new JButton("Crea");
        crea.addActionListener(_ -> {
            String name = nome.getText();
            Entita eroe = new Entita(
                    name,
                    30,
                    0,
                    10,
                    0,
                    1,
                    1,
                    3,
                    0,
                    5,
                    new UpgradePoints(0,0,0,0,0),
                    new MaxValues(
                            30,
                            1,
                            3,
                            0,
                            0,
                            5,
                            0
                    ),
                    new SkillAttacco(
                            nomeMossa.getText(),
                            15,
                            5,
                            null),
                    new Passiva (nomePassiva.getText(), List.of()),
                    null,
                    new ArrayList<>(),
                    new ArrayList<>());
            GameSave save = new GameSave(eroe, List.of(0));

            try {
                GameSaveUtil.salvaSuFile(save, "save.json");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        stats.add(crea);

        var btnSin = new JButton("< Crea Domain");
        btnSin.addActionListener(_ -> {
            panelContainer.add(creaDomain(), "creaDomain");
            cardLayout.show(panelContainer, "creaDomain");
        });
        var btnDs = new JButton("Home >");
        btnDs.addActionListener(_ -> {
            try {
                panelContainer.add(inizio(), "home");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            cardLayout.show(panelContainer, "home");
        });

        return new Pagina("Crea/resetta personaggio", stats, btnSin, btnDs, null);
    }
    /**
     * Creates a panel for creating and managing skills.
     * Allows users to create new skills, manage passive abilities, and save/cancel skills.
     *
     */
    public Pagina creaSkill() {
        if (eroe.getPassive_acquisite() == null)
            eroe.setPassive_acquisite(new ArrayList<>());
        // Create main panel with grid layout
        var panel = new JPanel(new GridLayout(7, 2));
        panel.setBounds(UIUtils.getX(45), UIUtils.getY(70), UIUtils.getX(24), UIUtils.getY(20));

        // Add skill name input
        panel.add(new JLabel("Skill: "));
        JTextField skill = new JTextField();
        panel.add(skill);

        // Add passive ability name input
        panel.add(new JLabel("Nome passiva: "));
        JTextField nomePassiva = new JTextField();
        panel.add(nomePassiva);

        // Add skill type selection
        panel.add(new JLabel("Attivazione: "));
        JComboBox<TypeOfSkill> tipi = new JComboBox<>(TypeOfSkill.values());
        panel.add(tipi);

        // Add create skill button
        panel.add(new JLabel("Premi per creare la skill: "));
        JButton crea = creabottone("Crea Skill", UIUtils.getX(45), UIUtils.getY(80), UIUtils.getX(24), UIUtils.getY(5), 14);
        crea.addActionListener(_ -> {
            if (skill.getText().isEmpty()) {
                mostraErrore("Skill vuoto", "Inserisci un nome per il nuovo skill");
                return;
            }
            // Create new skill and add to hero's passive
            String effect = skill.getText();
            Skill nuovaSkill = new Skill(
                    effect,
                    false,
                    (TypeOfSkill) tipi.getSelectedItem()
            );
            eroe.getPassiva().getSkills().add(nuovaSkill);

            // Save game state and refresh UI
            try {
                GameSaveUtil.salvaSuFile(new GameSave(eroe, List.of(0)), "save.json");
                panelContainer.add(mostraSkillsPagina(eroe), "skills");
                cardLayout.show(panelContainer, "skills");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        });
        panel.add(crea);

        // Add clear passive button
        panel.add(new JLabel("Bottone per pulire questa passiva: "));
        var cancella = new JButton("Cancella passiva");
        cancella.addActionListener(_ -> {
            if (eroe.getPassiva().getSkills().isEmpty()) {
                mostraErrore("Nessun skill disponibile", "Non ci sono skill da cancellare");
                return;
            }
            if (sceltaYN("ARE YOU SURE?", "Vuoi pulire la passiva?") == 0) {
                // Clear passive name and skills
                eroe.getPassiva().setNome("");
                eroe.getPassiva().getSkills().clear();
                try {
                    GameSaveUtil.salvaSuFile(new GameSave(eroe, List.of(0)), "save.json");
                    panelContainer.add(mostraSkillsPagina(eroe), "skills");
                    cardLayout.show(panelContainer, "skills");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

        });
        panel.add(cancella);

        // Add active passive selector
        panel.add(new JLabel("Passiva attiva: "));
        JComboBox<Passiva> passive = new JComboBox<>();
        if (!eroe.getPassive_acquisite().isEmpty())
            passive = new JComboBox<>(eroe.getPassive_acquisite().toArray(new Passiva[0]));
        JComboBox<Passiva> finalPassive = passive;
        passive.addActionListener(_ -> {
            // Update hero's active passive
            eroe.setPassiva((Passiva) finalPassive.getSelectedItem());
            try {
                GameSaveUtil.salvaSuFile(new GameSave(eroe, List.of(0)), "save.json");
                panelContainer.add(mostraSkillsPagina(eroe), "skills");
                cardLayout.show(panelContainer, "skills");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        panel.add(passive);

        // Add a save passive button
        panel.add(new JLabel("Bottone per salvare questa passiva: "));
        var savePassive = new JButton("Salva passiva");
        savePassive.addActionListener(_ -> {
            // Add passive if new
            if (!eroe.getPassive_acquisite().contains(eroe.getPassiva()) && eroe.getPassiva() != null) {
                if (!nomePassiva.getText().isEmpty())
                    eroe.getPassiva().setNome(nomePassiva.getText());
                eroe.getPassive_acquisite().add(eroe.getPassiva());
            }
            try {
                GameSaveUtil.salvaSuFile(new GameSave(eroe, List.of(0)), "save.json");
                panelContainer.add(mostraSkillsPagina(eroe), "skills");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            cardLayout.show(panelContainer, "Home");
        });

        panel.add(savePassive);
        var btnSin = new JButton("< Passiva");
        btnSin.addActionListener(_ -> {
            panelContainer.add(mostraSkillsPagina(eroe), "passiva");
            cardLayout.show(panelContainer, "passiva");
        });
        var btnDs = new JButton("Domain >");
        btnDs.addActionListener(_ -> {
            panelContainer.add(mostraDomainPagina(eroe), "domain");
            cardLayout.show(panelContainer, "domain");
        });
        return new Pagina("Crea passiva/skill", panel, btnSin, btnDs, null);
    }
    /**
     * Creates the level selection panel
     */
    public JPanel sceltaLvl() {
        svuotaLog();

        var panel = new JPanel();
        panel.setLayout(new GridLayout(5, 3));

        // Create buttons for each level
        for (Level lvl : livelli()) {

            var bottone = creabottone("lvl " + lvl.getLvl() + " lvl racc." + lvl.getLivello_raccomandato(), 1, 10, 10, 10, 20);
            bottone.addActionListener(_ -> {
                try {
                    avviaPartita(lvl);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            panel.add(bottone);
        }
        var bottoneRit = creabottone("Ritorna al menu principale", 1, 10, 10, 10, 20);
        bottoneRit.addActionListener(_ -> cardLayout.show(panelContainer,"Home"));
        panel.add(bottoneRit);

        return panel;
    }

    //PANNELLI PER INFORMAZIONI O CREAZIONE
    /**
     * Adjusts the given points according to the specified passive effect.
     * Reduces or increases the points based on the effect type.
     * Throws an IllegalStateException for unknown effects.
     *
     * @param punti The initial points to be modified.
     * @param passiveEffect The passive effect that influences the adjustment.
     * @return The adjusted point value after applying the effect.
     */
    public int parsePuntiEffetti(int punti, PassiveEffect passiveEffect){
        switch (passiveEffect) {
            case POISON -> punti -= 10;
            case BLEEDING -> punti -= 13;
            case BURNING -> punti -= 15;
            case DEF_DEBUFF -> punti -= 25;
            case ATK_DEBUFF -> punti -= 20;
            case OVERALL_DEBUFF -> punti -= 35;
            case NOEFFECT -> punti += 1;

            case null -> System.out.println("nullo in creaDomain");
            default -> throw new IllegalStateException("Unexpected value: " + passiveEffect);
        }
        System.out.println(punti + "dopo");
        return punti;
    }
    /**
     * Creates a panel showing entity stats and skills
     */
    public JPanel stats(Entita entita) {
        if(entita == null)
            return null;
        JPanel stats = new JPanel();
        stats.setBounds(UIUtils.getX(70), UIUtils.getY(10), UIUtils.getX(15), UIUtils.getY(20));
        stats.setLayout(new GridLayout(12, 1));
        stats.setBackground(Color.white);

        entita.levelUp();

        // Create health bar with color coding
        var barraHp = creaProgressBar(0, (int) entita.getMaxValues().getMaxHp(), (int) entita.getHp());
        if (barraHp.getValue() < (barraHp.getMaximum() - barraHp.getMinimum()) / 2)
            barraHp.setBackground(Color.red);
        else if (barraHp.getValue() < (barraHp.getMaximum() / 100 * 70))
            barraHp.setBackground(Color.orange);
        else
            barraHp.setBackground(Color.green);

        // Add stat labels🟩 🟥
        stats.add(new JLabel("Nome: " + entita.getNome()));
        stats.add(new JLabel("Livello: " + entita.getLvl()));
        stats.add(new JLabel("Hp: " + entita.getHp() + "/" + entita.getMaxValues().getMaxHp() + (entita.getHp()<entita.getMaxValues().getMaxHp() ? "🟥": (entita.getHp()==entita.getMaxValues().getMaxHp() ? "":"🟩"))));
        stats.add(barraHp);
        stats.add(new JLabel("Exp: " + entita.getXp()));
        stats.add(new JLabel("Def: " + entita.getDef()+ (entita.getDef()<entita.getMaxValues().getMaxDef() ? "🟥": (entita.getDef()==entita.getMaxValues().getMaxDef() ? "":"🟩"))));
        stats.add(new JLabel("Attacco: " + entita.getAtk() + (entita.getAtk()<entita.getMaxValues().getMaxAtk() ? "🟥": (entita.getAtk()==entita.getMaxValues().getMaxAtk() ? "":"🟩"))));
        //MODIFICARE
        stats.add(new JLabel("Evasione: " + entita.getEvasione() + (entita.getEvasione()<entita.getMaxValues().getMaxEva() ? "🟥": (entita.getEvasione()==entita.getMaxValues().getMaxEva() ? "":"🟩"))));
        stats.add(new JLabel("Possibilità di critico: " + entita.getCritico() + (entita.getCritico()<entita.getMaxValues().getMaxCrit() ? "🟥": (entita.getCritico()==entita.getMaxValues().getMaxCrit() ? "":"🟩"))));
        stats.add(new JLabel("Attacco Speciale: " + entita.getSpecialAtk().getNomeAttacco()));
        stats.add(new JLabel("Danno Speciale: " + entita.getSpecialAtk().getDanno() + (entita.getSpecialAtk().getDanno()<entita.getMaxValues().getMaxSp() ? "🟥": (entita.getSpecialAtk().getDanno()==entita.getMaxValues().getMaxSp() ? "":"🟩"))));

        return stats;
    }

    /**
     * Creates a panel with domain-related information and a dropdown for selecting passive effects.
     * Adds the created panel to the provided parent panel.
     *
     */
    public JPanel sceltaDomain() {

        System.out.println(eroe == null);
        if(eroe == null || eroe.getDomain() == null) {
            return null;
        }
        if(eroe.getDomain_acquisiti() == null)
            eroe.setDomain_acquisiti(List.of(eroe.getDomain()));
        System.out.println("ciao");

        JPanel stats = new JPanel();
        stats.setBounds(UIUtils.getX(86), UIUtils.getY(10), UIUtils.getX(13), UIUtils.getY(15));
        stats.setLayout(new GridLayout(3, 2));
        stats.setBackground(Color.white);

        JComboBox<Domain> domains = new JComboBox<>(
                eroe.getDomain_acquisiti().toArray(new Domain[0])
        );

        // Nel pannello:
        stats.add(new JLabel("Domini in possesso: "));
        stats.add(domains);

        JButton salvaDomain = creabottone("scegli", 0, 0, 0, 0, 14);

        salvaDomain.addActionListener(_ -> {
            Domain selezionato = (Domain) domains.getSelectedItem();
            eroe.setDomain(selezionato);
            try {
                GameSaveUtil.salvaSuFile(new GameSave(eroe, List.of(0)), "save.json");
                panelContainer.add(mostraDomainPagina(eroe), "Domain");
                cardLayout.show(panelContainer, "Domain");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        });

        stats.add(salvaDomain);
        return stats;
    }


    /**
     * Displays skills information in a panel for the given entity
     */

    public JScrollPane mostraSkills(Entita entita)  {
        // Crea un'area di testo per visualizzare e aggiungere testo
        JTextArea textArea = new JTextArea();
        textArea.setBounds(UIUtils.getX(45), UIUtils.getY(50), UIUtils.getX(24), UIUtils.getY(20));
        textArea.setLineWrap(true); // Abilita gli a capo automatici
        textArea.setWrapStyleWord(true); // Spezza le linee solo tra le parole
        textArea.setEditable(false); // Rendi il testo non modificabile dall'utente
        textArea.setOpaque(true);

        // Aggiungi uno scroll pane che contiene l'area di testo
        JScrollPane scrollPane = new JScrollPane(
                textArea,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        );

        scrollPane.setBounds(UIUtils.getX(45), UIUtils.getY(50), UIUtils.getX(24), UIUtils.getY(20));
        // Aggiungi lo scroll pane al pannello principale

        // Popola l'area di testo con le skill dell'entità
        if (entita.getPassiva() != null && !entita.getPassiva().getSkills().isEmpty()) {
            textArea.append("Nome passiva: " +  eroe.getPassiva().getNome() + ":" + "\n------------------");
            String condizione = "";
            for (Skill skill : entita.getPassiva().getSkills()) {
                //↓
                String kit = skill.getEffect().toUpperCase();
                var parts = kit.split(" ");
                var condizioneNuova = "";

                if(parts.length <= 6)
                    condizioneNuova = "\n↓ Effetti base (sempre attivi)" + "\n" ;
                else {
                    String seScrivere = Objects.equals(parts[5], "ATTACCANTE") ? "": parts[5];
                    condizioneNuova = "\n↓ SE " + seScrivere + " " + parts[6] + " " + parts[7] + " " + parts[8] + "\n";
                }
                if(!Objects.equals(condizione, condizioneNuova)){
                    condizione = condizioneNuova;
                    textArea.append(condizione);
                }
                String seScrivere = Objects.equals(parts[0], "ATTACCANTE") ? "": parts[0];
                if(parts.length <= 6) {
                    textArea.append("  ->" + seScrivere + " " + parts[1] +" "+ parts[2] +" "+ parts[3] + "% " + skill.getTipoDiUtilizzo() + "\n");
                }
                else if(parts.length == 9) {
                    textArea.append("  ->" + seScrivere + " " + parts[1] + " " + parts[2] + " " + parts[3] + "% " + (!Objects.equals(parts[1], "HP")?
                                   (skill.isAttivo() ? "✅":"❌"):(skill.isAttivo() ? "🔥":"❌"))+ "\n");
                }
            }
        } else {
            textArea.setText("Nessuna skill disponibile...");
        }

        return scrollPane;
    }

    /**
     * Mostra le stats del domain
     */
    public JPanel mostraDomain(Entita entita) {

        JPanel stats = new JPanel(new GridLayout(8, 2));
        stats.setBounds(UIUtils.getX(70), UIUtils.getY(30), UIUtils.getX(15), UIUtils.getY(20));
        stats.setBackground(Color.white);
        if(entita.getDomain() == null){
            stats.add(new JLabel("Nessun dominio"));
            return null;
        }
        stats.add(new JLabel("Nome : " + entita.getDomain().getNome()));
        stats.add(new JLabel("Danno : " + entita.getDomain().getDanno()));
        stats.add(new JLabel("Probabilità di attivazione : " + entita.getDomain().getChance()));
        stats.add(new JLabel("Tipo di target : " + entita.getDomain().getTarget()));
        stats.add(new JLabel("Tipo di effetto : " + entita.getDomain().getPassiveEffects()));
        stats.add(new JLabel(entita.getDomain().isActive()? "Domain ATTIVO" : "Domain NON ATTIVO"));
        stats.add(new JLabel(entita.getDomain().getCooldown() <= 0? "PRONTO" : "IN COOLDOWN (" + entita.getDomain().getCooldown() + ")"));
        return stats;
    }


    /**
     * Displays Domain name and damage to every enemy
     */
    public JPanel pannelloDomain(){
        //PANNELLO DOPO L'ATTIVAZIONE DEL DOMAIN
        Domain dom = eroe.getDomain();
        JPanel domainPanel = new JPanel(new GridLayout(3, 1));
        domainPanel.add(creaLabel("ESPANSIONE DEL DOMINIO", 1, 20, 10, 10, 18, Color.black));
        domainPanel.add(creaLabel(dom == null? "domain is null": dom.getNome(), 1, 20, 10, 10, 18, Color.black));
        JPanel risultatoDomainDamage = new JPanel(new GridLayout(1, nemiciCorrenti.size()));
        for(Entita nemico : nemiciCorrenti) {
            risultatoDomainDamage.add(creaLabel(
                    dom == null ?
                            "no domain": "  DANNO "+ (  dom.getDanno() - nemico.getDef() <= 0 ? 0 : dom.getDanno() - nemico.getDef())   + " A "+ nemico.getNome() + "   ",
                    0,0,0,0,1,Color.black)
            );
        }
        domainPanel.add(risultatoDomainDamage);
        return domainPanel;
    }

    /**
     * Creates UI for leveling up stats and applying stat points
     */
    public JPanel puntiLivelli(Entita entita) {


        JPanel stats = new JPanel(new GridLayout(9, 3));
        stats.setBounds(UIUtils.getX(45), UIUtils.getY(20), UIUtils.getX(24), UIUtils.getY(30));
        stats.setBackground(Color.white);

        // Add header text
        stats.add(new JLabel("Per ogni punto aumenta lo scaling"));
        stats.add(new JLabel("livello attuale : " + entita.getLvl()));
        stats.add(new JLabel(""));

        UpgradePoints points = entita.getPoints();

        // Show available points
        stats.add(new JLabel("Punti livello : "));
        var ptLvl = creaLabel(entita.getPuntiLivello() + "", 1, 20, 10, 10, 10, null);
        stats.add(ptLvl);
        stats.add(new JLabel(""));

        // Attack points
        stats.add(new JLabel("Attacco : "));
        var ptAtk = creaLabel(points.getPunti_atk() + "", 1, 20, 10, 10, 10, null);
        stats.add(ptAtk);
        JButton aumentaScaleAtk = creabottone("Aumenta scaling atk", 1, 20, 10, 10, 14);
        aumentaScaleAtk.addActionListener(_ -> {
            if (entita.getPuntiLivello() <= 0)
                return;

            entita.setPuntiLivello(entita.getPuntiLivello() - 1);
            points.setPunti_atk(points.getPunti_atk() + 1);

            entita.setAtk(entita.getAtk() * 1.06F);
            entita.getMaxValues().setMaxAtk(entita.getAtk());
            try {
                GameSaveUtil.salvaSuFile(new GameSave(eroe, List.of(0)), "save.json");
                panelContainer.add(statsPagina(eroe), "stats");
                cardLayout.show(panelContainer, "stats");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }



        });
        stats.add(aumentaScaleAtk);

        // Defense points
        stats.add(new JLabel("Difesa : "));
        var ptDef = creaLabel(points.getPunti_def() + "", 1, 20, 10, 10, 10, null);
        stats.add(ptDef);
        JButton aumentaScaleDef = creabottone("Aumenta scaling def", 1, 20, 10, 10, 10);
        aumentaScaleDef.addActionListener(_ -> {
            if (entita.getPuntiLivello() <= 0)
                return;

            points.setPunti_def(points.getPunti_def() + 1);
            entita.setPuntiLivello(entita.getPuntiLivello() - 1);

            entita.setDef(entita.getDef() * 1.05F);
            entita.getMaxValues().setMaxDef(entita.getDef());

            try {
                GameSaveUtil.salvaSuFile(new GameSave(eroe, List.of(0)), "save.json");
                panelContainer.add(statsPagina(eroe), "stats");
                cardLayout.show(panelContainer, "stats");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        stats.add(aumentaScaleDef);

        // HP points
        stats.add(new JLabel("Hp : "));
        var ptHp = creaLabel(points.getPunti_hp() + "", 1, 20, 10, 10, 10, null);
        stats.add(ptHp);
        JButton aumentaScaleHp = creabottone("Aumenta scaling hp", 1, 20, 10, 10, 10);
        aumentaScaleHp.addActionListener(_ -> {
            if (entita.getPuntiLivello() <= 0)
                return;

            entita.setPuntiLivello(entita.getPuntiLivello() - 1);
            points.setPunti_hp(points.getPunti_hp() + 1);

            entita.setHp(entita.getHp() * 1.04F);
            entita.getMaxValues().setMaxHp(entita.getHp());
            try {
                GameSaveUtil.salvaSuFile(new GameSave(eroe, List.of(0)), "save.json");
                panelContainer.add(statsPagina(eroe), "stats");
                cardLayout.show(panelContainer, "stats");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        stats.add(aumentaScaleHp);
        //------------
        stats.add(new JLabel("Possibilità di critico: "));
        var aumentoCritico = creaLabel(eroe.getCritico() + "%", 1, 20, 10, 10, 10, null);
        stats.add(aumentoCritico);
        JButton aumentaCrit = creabottone("Aumenta possibilità di crit del 0.5%", 1, 20, 10, 10, 14);
        aumentaCrit.addActionListener(_ -> {
            if (entita.getPuntiLivello() <= 0)
                return;
            entita.setPuntiLivello(entita.getPuntiLivello() - 1);
            entita.setCritico(entita.getCritico() + 0.8F);
            entita.getMaxValues().setMaxCrit(entita.getCritico());

            try {
                GameSaveUtil.salvaSuFile(new GameSave(eroe, List.of(0)), "save.json");
                panelContainer.add(statsPagina(eroe), "stats");
                cardLayout.show(panelContainer, "stats");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        });
        stats.add(aumentaCrit);//🟩 🟥

        stats.add(new JLabel("Possibilità di evasione colpi: "));
        var aumentoEva = creaLabel(eroe.getEvasione() + "%", 1, 20, 10, 10, 10, null);
        stats.add(aumentoEva);
        JButton aumentoEvasione = creabottone("Aumenta evasione (max 70)", 1, 20, 10, 10, 14);
        aumentoEvasione.addActionListener(_ -> {
            if (entita.getPuntiLivello() <= 0 || eroe.getEvasione() >= 65)
                return;
            entita.setPuntiLivello(entita.getPuntiLivello() - 1);
            entita.setEvasione(entita.getEvasione() + 0.8F);
            entita.getMaxValues().setMaxEva(entita.getEvasione());

            try {
                GameSaveUtil.salvaSuFile(new GameSave(eroe, List.of(0)), "save.json");
                panelContainer.add(statsPagina(eroe), "stats");
                cardLayout.show(panelContainer, "stats");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        });
        stats.add(aumentoEvasione);

        if(entita.getSpecialAtk() != null) {
            stats.add(new JLabel("Danno Attacco Speciale: "));
            var ptSp = creaLabel(points.getPunti_atkSp() + "", 1, 20, 10, 10, 10, null);
            stats.add(ptSp);
            JButton aumentaScaleSp = creabottone("Aumenta scaling sp", 1, 20, 10, 10, 10);
            aumentaScaleSp.addActionListener(_ -> {
                if (entita.getPuntiLivello() <= 0)
                    return;
                entita.setPuntiLivello(entita.getPuntiLivello() - 1);
                points.setPunti_atkSp(points.getPunti_atkSp() + 1);

                entita.getSpecialAtk().setDanno(entita.getSpecialAtk().getDanno() * 1.05F);
                entita.getMaxValues().setMaxSp(entita.getSpecialAtk().getDanno());
                try {
                    GameSaveUtil.salvaSuFile(new GameSave(eroe, List.of(0)), "save.json");
                    panelContainer.add(statsPagina(eroe), "stats");
                    cardLayout.show(panelContainer, "stats");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            stats.add(aumentaScaleSp);
        }

        if(entita.getDomain() != null) {
            stats.add(new JLabel("Danno dominio : "));
            var ptDom = creaLabel(points.getPunti_domain() + "", 1, 20, 10, 10, 10, null);
            stats.add(ptDom);
            JButton aumentaScaleDom = creabottone("Aumenta scaling dominio", 1, 20, 10, 10, 10);
            aumentaScaleDom.addActionListener(_ -> {
                if (entita.getPuntiLivello() <= 0)
                    return;
                entita.setPuntiLivello(entita.getPuntiLivello() - 1);
                points.setPunti_domain(points.getPunti_domain() + 1);

                entita.getDomain().setDanno(entita.getDomain().getDanno() * 1.05F);
                entita.getMaxValues().setMaxDomain(entita.getDomain().getDanno());

                try {
                    GameSaveUtil.salvaSuFile(new GameSave(eroe, List.of(0)), "save.json");
                    panelContainer.add(statsPagina(eroe), "stats");
                    cardLayout.show(panelContainer, "stats");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            });
            stats.add(aumentaScaleDom);
        }
        return stats;
    }



    //DOMAINS
    /**
     * BLEEDING based domain
     * @return the domain
     */
    public Domain malevolent_Kitchen(){
        return new Domain(
                "Malevolent Shrine",
                150,
                0,
                5,
                2,
                2,
                100,
                false,
                AOE,        //type of target
                List.of(    //Passive effects
                        BLEEDING,
                        DEF_DEBUFF
                ),
                0
        );
    }
    /**
     * BURNING based domain
     * @return the domain
     */
    public Domain carta_o_niente(){
        return new Domain(
                "Progetto o niente",
                130,
                0,
                5,
                5,
                2,
                50,
                false,
                AOE,        //type of target
                List.of(    //Passive effects
                        BURNING,
                        OVERALL_DEBUFF
                ),
                0
        );
    }

    //PERSONAGGIO DI BASE
    public Entita prePg(){
        return new Entita(
                "crea un nuovo account",
                30,
                0,
                10,
                0,
                1,
                1,
                3,
                0,
                0,
                new UpgradePoints(0,0,0,0,0),
                new MaxValues(
                        30,
                        1,
                        3,
                        15,
                        0,
                        0,
                        0

                ),
                new SkillAttacco(
                        "crea un nuovo account",
                        15,
                        5,
                        null),
                null,
                null,
                List.of(),
                List.of()
        );

    }

    //NEMICI
    public Entita dummy(){
        return new Entita(
               "Dummy",
                1000,
                0,
                10,
                1,
                2,
                2,
                0,
                0,
                new MaxValues(1000,2,2,10,0,0,0),
                new SkillAttacco(
                        "dummy",
                        10,
                        2,
                        null
                ),
                null
        );

    }
    public Entita negro(int xp){
        return new Entita(
                "Stronzo",
                10,
                xp,
                10,
                0,
                1,
                3,
                0,
                0,
                new MaxValues(
                        10,
                        1,
                        3,
                        10,
                        0,
                        0,
                        0
                ),
                new SkillAttacco(
                        "pugno forte",
                        10,
                        2,
                        BURNING
                ),
                null
        );
    }
    public Entita bianco(int xp) {
        return new Entita(
                "Stronzo",
                10,
                xp,
                10,
                0,
                1,
                3,
                0,
                0,
                new MaxValues(
                        10,
                        1,
                        3,
                        10,
                        0,
                        0,
                        0
                ),
                new SkillAttacco(
                        "pugno forte",
                        10,
                        2,
                        BURNING
                ),
                null
        );
    }
    public Entita palmeri(int xp) {
        return new Entita(
                "Palnegri",
                21,
                xp,
                10,
                0,
                2.5F,
                3F,
                70,
                5,
                new MaxValues(
                        21,
                        2.5F,
                        3F,
                        10,
                        20,
                        5,
                        0
                ),
                new SkillAttacco(
                        "fetore",
                        10,
                        2,
                        POISON
                ),
                null
        );
    }
    public Entita evan(int xp) {
        return new Entita(
                "Evan",
                25,
                xp,
                10,
                0,
                5F,
                2.5F,
                0,
                10,
                new MaxValues(
                        25,
                        5F,
                        2.5F,
                        8,
                        0,
                        10,
                        0
                ),
                new SkillAttacco(
                        "010 -> prigione",
                        8,
                        2,
                        null
                ),
                null
        );
    }
    public Entita tanta(int xp) {
        return new Entita(
                "Tanta",
                18,
                xp,
                10,
                0,
                0,
                5F,
                30,
                30,
                new MaxValues(
                        18,
                        0,
                        5F,
                        10,
                        30,
                        30,
                        0
                ),
                new SkillAttacco(
                        "Ballo del triangolo",
                        10,
                        2,
                        null
                ),
                null
        );

    }
    public Entita mario(int xp) {
        return new Entita(
                "Marione",
                25,
                xp,
                10,
                0,
                5F,
                2.3F,
                0,
                50,
                new MaxValues(
                        25,
                        5F,
                        2.5F,
                        14,
                        0,
                        50,
                        0
                ),
                new SkillAttacco(
                        "Sito web",
                        14F,
                        2,
                        null
                ),
                null
        );
    }

    //BOSS
    public Entita moraRiccardo(int xp) {
        return new Entita(
                "|Mora Riccardo|",
                80,
                xp,
                10,
                0,
                4F,
                4F,
                30,
                10,
                new MaxValues(
                        80,
                        4,
                        4,
                        20,
                        30,
                        10,
                        0
                ),
                new SkillAttacco(
                        "1 ora di carta, solo dopo il pc",
                        20,
                        2,
                        null
                ),
                carta_o_niente()
        );
    }

    //LIVELLI E SCELTA
    /**
     * List of <code>Level</code>
     * @return the List
     */
    public List<Level> livelli() {
        return List.of(
                new Level(-1, -1, 10, List.of(
                        new Round(1,10,List.of(
                                dummy(),
                                dummy(),
                                dummy()
                        )),
                        new Round(2,10,List.of(
                                dummy(),
                                dummy(),
                                dummy()
                        )),
                        new Round(3,10,List.of(
                                dummy(),
                                dummy(),
                                dummy()
                        ))
                )),
                new Level(1, 1, 20, List.of(
                        new Round(1, 10, List.of(
                                bianco(20),
                                bianco(20)
                        ))
                )),
                new Level(2, 3, 60, List.of(
                        new Round(1, 10, List.of(
                                bianco(200),
                                negro(200)
                        )),
                        new Round(2, 10, List.of(
                                palmeri(300)
                        ))
                )),
                new Level(3, 20, 250, List.of(
                        new Round(1, 10, List.of(
                                bianco(400),
                                negro(400),
                                palmeri(450)
                        )),
                        new Round(2, 10, List.of(
                                palmeri(500),
                                evan(550)
                        )),
                        new Round(3, 10, List.of(
                                tanta(1000)
                        ))
                )),
                new Level(4, 23, 1000, List.of(
                        new Round(1, 10, List.of(
                                negro(1000),
                                negro(1000),
                                negro(1000),
                                negro(1000)
                        )),
                        new Round(2, 10, List.of(
                                palmeri(1000),
                                evan(1000)
                        )),
                        new Round(3, 10, List.of(
                                tanta(2000),
                                evan(2000)
                        ))
                )),
                new Level(5, 40, 3000, List.of(
                        new Round(1, 10, List.of(
                                negro(2000),
                                negro(2000),
                                negro(2000),
                                negro(2000)
                        )),
                        new Round(2, 10, List.of(
                                tanta(10000),
                                evan(10000)
                        )),
                        new Round(3, 10, List.of (
                                palmeri(20000)
                        ))
                )),
                new Level(6, 50, 10000, List.of(
                        new Round(1, 10, List.of(
                                negro(30000)
                        )),
                        new Round(2, 10, List.of(
                                evan(50000)
                        )),
                        new Round(3, 10, List.of (
                                tanta(100000)
                        )),
                        new Round(4, 10, List.of(
                                palmeri(300000)
                        ))
                )),
                new Level(7, 70, 300000, List.of(
                        new Round(1, 20, List.of(
                                moraRiccardo(1000000)
                        ))
                )),
                new Level(8, 60, 200000, List.of(
                        new Round(1, 20, List.of(
                                mario(800000),
                                mario(800000)
                        )),
                        new Round(1, 20, List.of(
                                palmeri(1300000)
                        ))
                )),
                new Level(9, 75, 500000, List.of(
                        new Round(1, 20, List.of(
                                mario(5000000),
                                mario(5000000)
                        )),
                        new Round(1, 20, List.of(
                                palmeri(10000000)
                        ))
                )),
                new Level(10, 95, 3000000, List.of(
                        new Round(1, 20, List.of(
                                mario(100000000),
                                mario(100000000),
                                negro(100000000)
                        )),
                        new Round(1, 20, List.of(
                                evan(900000000)
                        ))
                ))

        );
    }
    


    //PARTE DELLA BATTAGLIA
    /**
     * Starts a new game on the selected level
     */
    public void avviaPartita(Level lvl) throws IOException {
        this.livelloCorrente = lvl;
        this.indiceRoundCorrente = 0;
        this.eroe = GameSaveUtil.caricaDaFile("save.json").getPersonaggio();
        this.nemiciCorrenti = new ArrayList<>(lvl.getRound().getFirst().getNemici());
    
        mostraTurno();
    }

    /**
     * Displays the current round state and updates based on actions
     */
    public void mostraTurno() throws IOException {
        panelContainer.removeAll();

        JPanel panel = new JPanel();
        panel.setLayout(null);
        panel.setBackground(Color.lightGray);

        JLabel roundLabel = creaLabel(
                "ROUND " + (indiceRoundCorrente + 1) + " / " + livelloCorrente.getRound().size(),
                0, 0, 100, 10, 30, Color.black
        );
        panel.add(roundLabel);

        // Configure log area
        logArea.setEditable(false);
        logArea.setBackground(new Color(235, 235, 235));

        scrollPane = new JScrollPane(logArea);
        scrollPane.setBounds(UIUtils.getX(0), UIUtils.getY(35), UIUtils.getX(100), UIUtils.getY(15));
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        panel.add(scrollPane);
        // Show enemy panel
        JPanel pannelloNemici = new JPanel(new GridLayout(1, nemiciCorrenti.size()));
        pannelloNemici.setBounds(UIUtils.getX(0), UIUtils.getY(10), UIUtils.getX(100), UIUtils.getY(25));
        for (Entita nemico : nemiciCorrenti) {
            nemico.levelUp();
            pannelloNemici.add(stats(nemico));
        }
        panel.add(pannelloNemici);

        // Attack panel with buttons
        JPanel attackPanel = new JPanel(new GridLayout(2, nemiciCorrenti.size()));
        attackPanel.setBounds(UIUtils.getX(0), UIUtils.getY(50), UIUtils.getX(50), UIUtils.getY(35));

        // Add attack buttons for each enemy
        for (int i = 0; i < nemiciCorrenti.size(); i++) {
            Entita nemico = nemiciCorrenti.get(i);
            JPanel panelAttacchi = new JPanel(new GridLayout(2, 1));

            // Normal attack button
            JButton attaccoNormale = creabottone(
                    "Attacca " + nemico.getNome(), 1, 20, 10, 10, 20
            );
            if (nemico.getHp() - (eroe.getAtk() - nemico.getDef()) <= 0) {
                attaccoNormale.setForeground(Color.red);
                attaccoNormale.setBackground(new Color(255, 200, 200));
            }else if(eroe.getAtk() - nemico.getDef() <= 0){
                attaccoNormale.setForeground(Color.blue);
                attaccoNormale.setBackground(new Color(200, 200, 255));
            }
            else {
                attaccoNormale.setForeground(Color.black);
                attaccoNormale.setBackground(Color.white);
            }

            int idx = i;
            attaccoNormale.addActionListener(_ -> {
                try {
                    azioneTurno(idx, false);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            });
            panelAttacchi.add(attaccoNormale);

            // Special attack button
            JButton attaccoSpeciale = creabottone(
                    "Speciale su " + nemico.getNome(), 1, 20, 10, 10, 20
            );
            if (nemico.getHp() - (eroe.getSpecialAtk().getDanno() - nemico.getDef()) <= 0) {
                attaccoSpeciale.setForeground(Color.red);
                attaccoSpeciale.setBackground(new Color(255, 200, 200));
            }
            else if(eroe.getSpecialAtk().getDanno() - nemico.getDef() <= 0){
                attaccoSpeciale.setForeground(Color.blue);
                attaccoSpeciale.setBackground(new Color(200, 200, 255));
            }
            else {
                attaccoSpeciale.setForeground(Color.black);
                attaccoSpeciale.setBackground(Color.white);
            }
            attaccoSpeciale.addActionListener(_ -> {
                try {
                    azioneTurno(idx, true);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            });
            panelAttacchi.add(attaccoSpeciale);
            panel.add(scrollPane);
            attackPanel.add(panelAttacchi);
        }
        //AGGIUNTA DEL PANNELLO DEL DOMAIN
        if(eroe.getDomain() != null) {
        panelContainer.add(pannelloDomain(), "Domain");
        }

        //BOTTONE DEL DOMAIN E ATTIVAZIONE
        if (!nemiciCorrenti.isEmpty()) {
            CombatSystem combatSystem = new CombatSystem(eroe, nemiciCorrenti.getFirst());
            Domain domain = combatSystem.getAttaccante().getDomain();
            if (domain != null) {

                JButton domainb = creabottone("ESPANSIONE DEL DOMINIO", 1, 20, 10, 10, 18);
                if(domain.isActive()) {
                    domainb.setForeground(Color.red);
                    domainb.setBackground(new Color(255, 200, 200));
                }
                else if(domain.getCooldown() != 0) {
                    domainb.setForeground(Color.blue);
                    domainb.setBackground(new Color(200, 200, 255));
                }
                else{
                    domainb.setForeground(Color.GREEN);
                    domainb.setBackground(new Color(200, 255, 200));
                }

                domainb.addActionListener(_ -> {
                    // Controlla cooldown e stato attivo
                    if (domain.getCooldown() != 0 || domain.isActive()) {
                        return;
                    }

                    // Mostra interfaccia dominio
                    cardLayout.show(panelContainer, "Domain");
                    panelContainer.revalidate();
                    panelContainer.repaint();

                    // Timer per animazione e logica dominio
                    new Timer(1000, e -> {
                        ((Timer) e.getSource()).stop();

                        // Gestione stati dominio
                        var result = combatSystem.domainExpansion(nemiciCorrenti);
                        switch (result) {
                            case NULL -> {
                                log.error("ERRORE: DOMAIN NULLO ANCHE SE NON POTREBBE");
                                return;
                            }
                            case FINE_DOMAIN ->{
                                aggiungiLog("Finito Domain di " + combatSystem.getAttaccante().getNome());
                                return;
                            }
                            case ATTIVO -> {
                                aggiungiLog("DOMAIN attivo ancora per " + combatSystem.getAttaccante().getDomain().getDurata() + " turni");
                                aggiungiLog("DOMAIN disponibile tra " + combatSystem.getAttaccante().getDomain().getCooldown() + " turni");
                                return;
                            }
                            case COOLDOWN -> {
                                return;
                            }
                        }

                        // Log espansione dominio
                        aggiungiLog("DOMAIN EXPANSION di " + combatSystem.getAttaccante().getNome() +
                                " - NOME DOMINIO: " + combatSystem.getAttaccante().getDomain().getNome());

                        // Rimozione nemici sconfitti
                        nemiciCorrenti.removeIf(nemico -> nemico.getHp() <= 0);

                        // Controllo completamento round
                        try {
                            if (nemiciCorrenti.isEmpty()) {
                                avanzaRound();
                                return;
                            }
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
        
                        // Aggiornamento turno
                        try {
                            mostraTurno();
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                    }).start();
                });
                attackPanel.add(domainb);
            }
        }

        // Hero panel showing stats and skills
        JPanel pannelloEroe = new JPanel(new GridLayout(2, 2));
        pannelloEroe.setBounds(UIUtils.getX(50), UIUtils.getY(50), UIUtils.getX(50), UIUtils.getY(35));
        pannelloEroe.add(mostraSkills(eroe));
        pannelloEroe.add(stats(eroe));
        pannelloEroe.add(new JLabel("Mosse speciali : " + eroe.getSpecialAtk().getUtilizzi()));
        if(eroe.getDomain() != null)
            pannelloEroe.add(mostraDomain(eroe));

        panel.add(pannelloEroe);
        panel.add(attackPanel);

        panelContainer.add(panel, "partita");
        cardLayout.show(panelContainer, "partita");
        panelContainer.revalidate();
        panelContainer.repaint();
    }

    /**
     * Handles a turn action between hero and enemy
     *
     * @param indiceNemico Index of the targeted enemy
     * @param speciale     True if using special attack
     */
    public void azioneTurno(int indiceNemico, boolean speciale) throws IOException {
        Entita nemicoSelezionato = nemiciCorrenti.get(indiceNemico);
        if(nemicoSelezionato == null) {
            avanzaRound();
            return;
        }
        // Hero attacks

        CombatSystem combatSystem = new CombatSystem(eroe, nemicoSelezionato);
        aggiungiLog(combatSystem.attivaSkills(nemiciCorrenti));
        if (speciale) {
            switch (combatSystem.applicaEffect()){
                case NULL -> System.out.println("nullo");
                case SUCCESS -> System.out.println("successo Passive");
            }
            if (eroe.getSpecialAtk().getUtilizzi() <= 0) {
                aggiungiLog(combatSystem.attaccoBase(false));
            } else {
                aggiungiLog(combatSystem.attaccoSpeciale());
            }
        } else {
            aggiungiLog(combatSystem.attaccoBase(false));
        }
        if(combatSystem.getAttaccante().getDomain() != null) {
            if(combatSystem.getAttaccante().getDomain().isActive()){
                aggiungiLog("⏱️DOMAIN attivo ancora per  " + combatSystem.getAttaccante().getDomain().getDurata()+ " turni");
                if(eroe.getDomain().getCooldown() != 0){aggiungiLog("⌛️DOMAIN disponibile tra " + combatSystem.getAttaccante().getDomain().getCooldown() + " turni");}
            }
        }
        nemiciCorrenti.removeIf(nemico -> nemico.getHp() <= 0);
        // Enemy counterattacks
        attacchiNemiciSequenziali(new ArrayList<>(nemiciCorrenti), 0 , combatSystem);
        try {
            if (nemiciCorrenti.isEmpty()) {
                avanzaRound();
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }


    }
    /**
     * Advances to the next round or ends level if complete
     */
    private void avanzaRound() throws IOException {

        indiceRoundCorrente++;
        if (indiceRoundCorrente < livelloCorrente.getRound().size()) {
            if(eroe.getPassiva() != null) {
                for (Skill skill : eroe.getPassiva().getSkills()) {
                    skill.setAttivo(false);
                }
            }
            // Load next round enemies
            nemiciCorrenti = new ArrayList<>(livelloCorrente.getRound().get(indiceRoundCorrente).getNemici());
            mostraTurno();
        } else {
            // Level complete
            new CombatSystem(eroe, null).disattivaSkills();
            // Update hero stats
            Domain dom = eroe.getDomain();
            if(dom != null) {
                dom.setCooldown(0);
                dom.setActive(false);
                dom.setDurata(dom.getDurataMax());
                System.out.println("PR Domains : " + eroe.getDomain_acquisiti());
                eroe.setDomain(dom.evoluzione(eroe));
                System.out.println("Domains : " + eroe.getDomain_acquisiti());
            }
            eroe.getSpecialAtk().setUtilizzi(5);
            eroe.setXp(eroe.getXp() + livelloCorrente.getXp());
            eroe.levelUp();
            eroe.setHp(eroe.getMaxValues().getMaxHp());

            // Add skills based on level
            if (eroe.getLvl() >= 10) {
                if (dom == null && eroe.getDomain_acquisiti().isEmpty()) {
                    eroe.setDomain(malevolent_Kitchen());
                }
            }

            // Save game state
            GameSave save1 = new GameSave(eroe, List.of(1));
            GameSaveUtil.salvaSuFile(save1, "save.json");
            // Show victory messages
            mostraInformazioni("successo", "Vittoria! Hai vinto il livello " + livelloCorrente.getLvl() + "!");
            panelContainer.add(inizio(), "Home");
            cardLayout.show(panelContainer, "Home");

        }
    }

    public void gestisciTurnoDomain() {

        Domain domain = eroe.getDomain();
        if (domain == null) return;

        //se è attivo applico i debuff e modifico cooldown e durata
        if(domain.isActive()) {
            reset = true;
            CombatSystem domainEffects = new CombatSystem(eroe, null);
            domainEffects.applicaEffectDomain(nemiciCorrenti);

            domain.setDurata(domain.getDurata() - 1);
            if(domain.getDurata() <= 0) {
                domain.setActive(false);
                domain.setCooldown(domain.getCooldown());
                domain.setDurata(domain.getDurataMax()); // Ripristina per prossimo uso
            }
        } else if(domain.getCooldown() > 0) {
            if(reset){
                for(Entita entita : nemiciCorrenti){
                    MaxValues maxValues = entita.getMaxValues();
                    entita.setAtk(maxValues.getMaxAtk());
                    entita.setDef(maxValues.getMaxDef());
                }
                reset = false;
            }
            domain.setCooldown(domain.getCooldown() - 1);

        }

    }
    /**
     * Adds a message to the combat log
     */
    public void aggiungiLog(String testo) {
        logArea.append(testo + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
        scrollPane.revalidate();
        scrollPane.repaint();
    }

    /**
     * Clears the combat log
     */
    public void svuotaLog() {
        if (scrollPane == null) {
            return;
        }
        logArea.setText("");
        scrollPane.revalidate();
        scrollPane.repaint();
    }
    public void fineTurno(CombatSystem combatSystem) {
        if (eroe.getHp() <= 0) {
            if(combatSystem.disattivaSkills() == NULL)
                log.info("ATTENZIONE: Skill disabilitate durante il combattimento");
            mostraErrore("Hai perso!", "la prossima volta andra meglio");
            try {
                panelContainer.add(inizio(), "Home");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            cardLayout.show(panelContainer, "Home");
        } else {
            try {
                aggiungiLog("--------Fine turno-------");
                mostraTurno();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Handles sequential enemy attacks
     *
     * @param nemici    List of enemies
     * @param current   Current enemy index
     */
    public void attacchiNemiciSequenziali(List<Entita> nemici, int current, CombatSystem combatSystem) throws IOException {
        // End if hero defeated
        if (eroe.getHp() <= 0) {
            fineTurno(combatSystem);
            return;
        }

        // End if no more enemies
        if (current >= nemici.size()) {
            System.out.println("FINE TURNO");
            gestisciTurnoDomain();
            fineTurno(combatSystem);
            return;
        }

        // Process current enemy attack
        Entita nemico = nemici.get(current);
        CombatSystem combatNemico = new CombatSystem(nemico, eroe);

        // 80% chance of basic attack, 20% special
        double casuale = Math.random() * 10;
        if (casuale <= 8) {
            aggiungiLog(combatNemico.attaccoBase(false));
        } else {
            aggiungiLog(combatNemico.attaccoSpeciale());
        }

        // Handle enemy defeat
        if (nemico.getHp() <= 0) {
            nemici.remove(current);
            mostraTurno();

            if (current >= nemici.size()) {
                fineTurno(combatSystem);
                return;
            }
        } else {
            current++;
            mostraTurno();
        }

        // Queue next enemy attack
        int nextCurrent = current;

        if (!nemici.isEmpty()) {
            new Timer(400, e -> {
                ((Timer) e.getSource()).stop();
                try {
                    attacchiNemiciSequenziali(nemici, nextCurrent, combatSystem);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }).start();
        }
    }

}