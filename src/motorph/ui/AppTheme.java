package motorph.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public final class AppTheme {

    public static final Color BRAND_BLUE      = new Color(10,  56, 130);
    public static final Color BRAND_BLUE_DARK = new Color(6,   38,  90);  

    public static final Color BRAND_RED       = new Color(206, 17,  38);  
    public static final Color BRAND_DARK_RED  = new Color(160, 10,  25); 

    public static final Color BRAND_GOLD      = new Color(240, 180,  20);  

    public static final Color SIDEBAR_BG      = new Color(10,  56, 130);   
    public static final Color SIDEBAR_HOVER   = new Color(20,  76, 160);  

    public static final Color CONTENT_BG      = new Color(242, 245, 252); 
    public static final Color CARD_WHITE      = Color.WHITE;
    public static final Color TEXT_PRIMARY    = new Color(15,  25,  60);  
    public static final Color TEXT_MUTED      = new Color(110, 125, 160);
    public static final Color BORDER_LIGHT    = new Color(210, 220, 240);  
    public static final Color TABLE_STRIPE    = new Color(245, 248, 255);  
    public static final Color TABLE_HEADER_BG = new Color(10,  56, 130); 
    public static final Color ROW_SELECTED    = new Color(220, 230, 255); 
    public static final Color GREEN_TEXT      = new Color(22,  120,  60);
    public static final Color AMBER_TEXT      = new Color(180, 120,   0);
    public static final Color RED_TEXT        = new Color(206,  17,  38);  
    public static final Color BLUE_BTN        = new Color(10,   56, 130);  

    public static final Font FONT_TITLE   = new Font("Segoe UI", Font.BOLD,  22);
    public static final Font FONT_HEADING = new Font("Segoe UI", Font.BOLD,  15);
    public static final Font FONT_BODY    = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_SMALL   = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_BOLD_SM = new Font("Segoe UI", Font.BOLD,  12);
    public static final Font FONT_MONO    = new Font("Consolas",  Font.PLAIN, 13);

    private AppTheme() {}

    public static JButton primaryButton(String text) {
        return styledButton(text, BRAND_BLUE, BRAND_BLUE_DARK);
    }

    public static JButton dangerButton(String text) {
        return styledButton(text, BRAND_RED, BRAND_DARK_RED);
    }

    public static JButton actionButton(String text, Color color) {
        return styledButton(text, color, color.darker());
    }

    private static JButton styledButton(String text, Color bg, Color hover) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BOLD_SM);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(8, 18, 8, 18));
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { if (btn.isEnabled()) btn.setBackground(hover); }
            public void mouseExited(MouseEvent e)  { if (btn.isEnabled()) btn.setBackground(bg); }
        });
        return btn;
    }

    public static JPanel pageHeader(String title, String subtitle) {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(CARD_WHITE);
        header.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_LIGHT),
            new EmptyBorder(18, 24, 18, 24)
        ));
        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));

        JLabel lTitle = new JLabel(title);
        lTitle.setFont(FONT_TITLE);
        lTitle.setForeground(TEXT_PRIMARY);

        JLabel lSub = new JLabel(subtitle);
        lSub.setFont(FONT_BODY);
        lSub.setForeground(TEXT_MUTED);

        text.add(lTitle);
        text.add(Box.createVerticalStrut(3));
        text.add(lSub);
        header.add(text, BorderLayout.WEST);
        return header;
    }

    public static JPanel card() {
        JPanel p = new JPanel();
        p.setBackground(CARD_WHITE);
        p.setBorder(BorderFactory.createLineBorder(BORDER_LIGHT, 1));
        return p;
    }

    public static JLabel boldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(FONT_BOLD_SM);
        l.setForeground(TEXT_PRIMARY);
        return l;
    }

    public static JLabel mutedLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(FONT_BODY);
        l.setForeground(TEXT_MUTED);
        return l;
    }

    public static void styleTable(JTable table) {
        table.setRowHeight(40);
        table.setFont(FONT_BODY);
        table.setSelectionBackground(ROW_SELECTED);
        table.setSelectionForeground(TEXT_PRIMARY);
        table.setShowVerticalLines(false);
        table.setGridColor(new Color(235, 237, 245));
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setFillsViewportHeight(true);
        table.getTableHeader().setFont(FONT_BOLD_SM);
        table.getTableHeader().setBackground(TABLE_HEADER_BG);
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setPreferredSize(new Dimension(0, 40));
    }

    public static void styleCombo(JComboBox<?> combo) {
        combo.setFont(FONT_BODY);
        combo.setBackground(CARD_WHITE);
        combo.setPreferredSize(new Dimension(165, 32));
    }
}