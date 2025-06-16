package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import static javax.swing.JOptionPane.*;

@SuppressWarnings("unused")
public class UIUtils {
    //funzioni di supporto

    /**
     * <b>Funzioni per la creazione di componenti Swing come:
     * <br><code>JLabel, JTextArea, JButton, JPasswordField</code></b>
     */
    public static JLabel creaLabel(String messaggio, int percX, int percY, int percWidth, int percHeight, int fontSize, Color colore) {
        JLabel label = new JLabel(messaggio);
        label.setFont(new Font("Arial", Font.ITALIC, fontSize));
        label.setForeground(colore);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setBounds(getX(percX), getY(percY), getX(percWidth), getY(percHeight));
        label.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                resizeFontToFit(label);
            }
        });
        return label;
    }

    public static JTextArea creaArea(String messaggio, int percX, int percY, int percWidth, int percHeight, int fontSize) {
        JTextArea area = new JTextArea(messaggio);
        area.setFont(new Font("Arial", Font.BOLD, fontSize));
        area.setForeground(Color.black);
        area.setBackground(Color.white);
        area.setBounds(getX(percX), getY(percY), getX(percWidth), getY(percHeight));
        return area;
    }

    public static JButton creabottone(String messaggio, int percX, int percY, int percWidth, int percHeight, int fontSize) {
        JButton button = new JButton(messaggio);
        button.setFont(new Font("Arial", Font.BOLD, fontSize));
        button.setBounds(getX(percX), getY(percY), getX(percWidth), getY(percHeight));
        return button;
    }

    public static JPasswordField creaPassField(int percX, int percY, int percWidth, int percHeight, int fontSize) {
        JPasswordField area = new JPasswordField(15);
        area.setFont(new Font("Arial", Font.BOLD, fontSize));
        area.setForeground(Color.black);
        area.setBackground(Color.white);
        area.setBounds(getX(percX), getY(percY), getX(percWidth), getY(percHeight));
        return area;
    }
    //funzioni di ridimensionamento

    /**
     * <b>Funzione che riceve un <code>JLabel</code> (componente Swing) <br>e ne ridimensiona il testo per stare nella dimensione corretta</b>
     *
     * @param label <code>JLabel</code> da modificare
     */
    private static void resizeFontToFit(JLabel label) {
        String text = label.getText();
        if (text == null || text.isEmpty()) return;

        int labelWidth = label.getWidth();
        int labelHeight = label.getHeight();
        if (labelWidth <= 0 || labelHeight <= 0) return;

        Font font = label.getFont();
        int size = 1;
        FontMetrics fm;
        do {
            font = font.deriveFont((float) size);
            fm = label.getFontMetrics(font);
            int textWidth = fm.stringWidth(text);
            int textHeight = fm.getHeight();
            if (textWidth > labelWidth || textHeight > labelHeight) break;
            size++;
        } while (true);

        label.setFont(font.deriveFont((float) (size - 1)));
    }

    /**
     * <b>Funzioni che con una percentuale di schermo restituiscono il suo valore in pixel in base alla
     * risoluzione attuale, questo serve in modo che in tutti i dispositivi, indipendentemente dalla
     * risoluzione, si ottenga una giusta posizione dei componenti GUI </b>
     *
     * @param percSchermo percentuale di schermo in pixel che si vuole ottenere
     * @return restituisce un <code>int</code>il valore in pixel
     */
    public static int getX(float percSchermo) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        return (int) (screenSize.width * (percSchermo / 100));
    }

    public static int getY(float percSchermo) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        return (int) ((screenSize.height * 0.95) * (percSchermo / 100));
    }

    /**
     * <b>Formula inversa di <code>getX, getY</code></b>
     *
     * @param percInversa valore in pixel per sapere la percentuale
     * @return valore in percentuale dello schermo in base ai pixel inseriti
     */
    public static int getPercX(float percInversa) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        return (int) ((percInversa * 100) / screenSize.width);
    }

    public static int getPercY(float percInversa) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        return (int) ((percInversa * 100) / screenSize.height);
    }


    public static void mostraErrore(String titolo, Object messaggio) {
        JOptionPane.showMessageDialog(null, messaggio, titolo, ERROR_MESSAGE);
    }

    public static void mostraInformazioni(String titolo, Object messaggio) {
        JOptionPane.showMessageDialog(null, messaggio, titolo, INFORMATION_MESSAGE);
    }

    public static String input(String titolo, Object messaggio) {
        return JOptionPane.showInputDialog(null, messaggio, titolo, QUESTION_MESSAGE);
    }

    public static int sceltaOKorNo(String title, Object messaggio) {
        return JOptionPane.showConfirmDialog(null, messaggio, title, OK_CANCEL_OPTION, PLAIN_MESSAGE);
    }

    public static int sceltaYN(String title, Object messaggio) {
        return JOptionPane.showConfirmDialog(null, messaggio, title, YES_NO_OPTION, PLAIN_MESSAGE);
    }
    /**
     * Creates a progress bar for displaying health/stats
     */
    public static JProgressBar creaProgressBar(int min, int max, int value) {
        JProgressBar progressBar = new JProgressBar(min, max);
        progressBar.setValue(value);
        if (value < (progressBar.getMaximum() - progressBar.getMinimum()) / 2)
            progressBar.setBackground(Color.red);

        return progressBar;
    }


}
