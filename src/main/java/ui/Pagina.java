package ui;
import javax.swing.*;
import java.awt.*;

public class Pagina extends JPanel {
    String header;
    JPanel content;
    JButton btnSinistra;
    JButton btnDestra;
    JScrollPane scrollPane;

    public Pagina(String header, JPanel content, JButton btnSinistra, JButton btnDestra, JScrollPane scrollPane) {
        this.header = header;
        this.content = content;
        this.btnSinistra = btnSinistra;
        this.btnDestra = btnDestra;
        this.scrollPane = scrollPane;

        this.setLayout(new BorderLayout());
        JLabel headerLabel = new JLabel();
        headerLabel.setBackground(new Color(200,130,100));
        headerLabel.setText(header);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 20));
        headerLabel.setHorizontalAlignment(SwingConstants.CENTER);
        headerLabel.setPreferredSize(new Dimension(UIUtils.getX(100), UIUtils.getY(5)));

        this.btnDestra.setPreferredSize(new Dimension(UIUtils.getX(10), UIUtils.getY(50)));
        this.btnSinistra.setPreferredSize(new Dimension(UIUtils.getX(10), UIUtils.getY(50)));
        if(content != null)
            this.content.setPreferredSize(new Dimension(UIUtils.getX(40), UIUtils.getY(50)));
        else if(scrollPane != null)
            this.scrollPane.setPreferredSize(new Dimension(UIUtils.getX(40), UIUtils.getY(50)));
        this.setBackground(new Color(255,255,255));

        this.btnDestra.setBackground(new Color(255,110,100));
        this.btnSinistra.setBackground(new Color(255,110,100));
        if(content != null)
            this.content.setBackground(new Color(230,230,230));
        else if(scrollPane != null)
            this.scrollPane.setBackground(new Color(230,230,230));
        this.add(headerLabel, BorderLayout.NORTH);
        if(content != null)
            this.add(content, BorderLayout.CENTER);
        else if(scrollPane != null)
            this.add(scrollPane, BorderLayout.CENTER);
        this.add(btnSinistra, BorderLayout.WEST);
        this.add(btnDestra, BorderLayout.EAST);

    }



}
