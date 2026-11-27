package com.highlands.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Locale;

// Màu, chữ và các hàm vẽ dùng chung (giống admin.css của bản web)
public final class Theme {

    public static final Color SIDEBAR_TEXT = new Color(0xF9E9DF);
    public static final Color SIDEBAR_HOVER = new Color(0x98212A);
    public static final Color BRAND = new Color(0xB22830);
    public static final Color BRAND_DARK = new Color(0x8E1F27);
    public static final Color TOPBAR = new Color(0xB22830);
    public static final Color BG = new Color(0xFAF6F0);
    public static final Color LINE = new Color(0xE8DCD0);
    public static final Color VIEN_O = new Color(0xD8C7B8);
    public static final Color MUTED = new Color(0x75645A);
    public static final Color TEXT = new Color(0x352B26);
    public static final Color OK = new Color(0x16, 0xA3, 0x4A);
    public static final Color ERR = new Color(0xDC, 0x26, 0x26);
    public static final Color XANH = new Color(0x8E1F27);
    public static final Color HOVER = new Color(0xFCF0E9);
    public static final Color CHON = new Color(0xF7E4DA);

    public static final Color DISABLED_BG = new Color(0xEDD2CE);
    public static final Color DISABLED_TEXT = new Color(0x76534F);

    // Màu badge: {nền, chữ}
    public static final Color[] BADGE_ON = {new Color(0xDC, 0xFC, 0xE7), new Color(0x15, 0x80, 0x3D)};
    public static final Color[] BADGE_SOON = {new Color(0xFE, 0xF3, 0xC7), new Color(0xB4, 0x53, 0x09)};
    public static final Color[] BADGE_OFF = {new Color(0xE5, 0xE7, 0xEB), MUTED};
    public static final Color[][] MAU_DANH_MUC = {
            {new Color(0xF7E4DA), new Color(0x8E1F27)},
            {new Color(0xF7EBD5), new Color(0x79501E)},
            {new Color(0xEBEFDF), new Color(0x4F6235)},
            {new Color(0xF5DFDF), new Color(0x923C43)},
            {new Color(0xEEE3D8), new Color(0x70513B)},
            {new Color(0xF2E9DD), new Color(0x6A5945)},
    };

    private static String tenFont = Font.DIALOG;
    private static final NumberFormat SO = NumberFormat.getIntegerInstance(Locale.forLanguageTag("vi-VN"));

    private Theme() {
    }

    // Gọi một lần lúc khởi động, trước khi tạo cửa sổ
    public static void caiDat() {
        try {
            javax.swing.plaf.metal.MetalLookAndFeel.setCurrentTheme(new javax.swing.plaf.metal.OceanTheme() {
                @Override protected javax.swing.plaf.ColorUIResource getPrimary1() { return new javax.swing.plaf.ColorUIResource(BRAND_DARK); }
                @Override protected javax.swing.plaf.ColorUIResource getPrimary2() { return new javax.swing.plaf.ColorUIResource(BRAND); }
                @Override protected javax.swing.plaf.ColorUIResource getPrimary3() { return new javax.swing.plaf.ColorUIResource(CHON); }
                @Override protected javax.swing.plaf.ColorUIResource getSecondary1() { return new javax.swing.plaf.ColorUIResource(MUTED); }
                @Override protected javax.swing.plaf.ColorUIResource getSecondary2() { return new javax.swing.plaf.ColorUIResource(VIEN_O); }
                @Override protected javax.swing.plaf.ColorUIResource getSecondary3() { return new javax.swing.plaf.ColorUIResource(BG); }
            });
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {
        }
        String[] co = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        for (String ten : new String[]{"Segoe UI", "Roboto", "Noto Sans", "DejaVu Sans"}) {
            if (Arrays.asList(co).contains(ten)) {
                tenFont = ten;
                break;
            }
        }
        Font f = font(Font.PLAIN, 13);
        for (Object k : UIManager.getLookAndFeelDefaults().keySet().toArray()) {
            if (k != null && k.toString().endsWith(".font")) UIManager.put(k, f);
        }
        UIManager.put("Panel.background", BG);
        UIManager.put("Label.foreground", TEXT);
        UIManager.put("Button.foreground", TEXT);
        UIManager.put("Button.background", Color.WHITE);
        UIManager.put("Button.select", CHON);
        UIManager.put("Button.disabledText", DISABLED_TEXT);
        UIManager.put("ToggleButton.select", CHON);
        for (String type : new String[]{"Button", "ToggleButton", "CheckBox", "RadioButton", "Slider"}) {
            UIManager.put(type + ".gradient", java.util.List.of(0.3f, 0.0f, Color.WHITE, HOVER, CHON));
        }
        UIManager.put("CheckBox.disabledText", DISABLED_TEXT);
        UIManager.put("RadioButton.disabledText", DISABLED_TEXT);
        UIManager.put("CheckBox.foreground", TEXT);
        UIManager.put("RadioButton.foreground", TEXT);
        UIManager.put("List.selectionBackground", CHON);
        UIManager.put("List.selectionForeground", TEXT);
        UIManager.put("Table.selectionBackground", CHON);
        UIManager.put("Table.selectionForeground", TEXT);
        for (String type : new String[]{"TextField", "PasswordField", "TextArea", "FormattedTextField"}) {
            UIManager.put(type + ".foreground", TEXT);
            UIManager.put(type + ".background", Color.WHITE);
            UIManager.put(type + ".caretForeground", BRAND);
            UIManager.put(type + ".selectionBackground", CHON);
            UIManager.put(type + ".selectionForeground", TEXT);
            UIManager.put(type + ".inactiveForeground", MUTED);
        }
        UIManager.put("OptionPane.messageForeground", TEXT);
        UIManager.put("ProgressBar.foreground", BRAND);
        UIManager.put("ProgressBar.background", HOVER);
        UIManager.put("OptionPane.background", Color.WHITE);
        UIManager.put("OptionPane.messageFont", f);
        UIManager.put("OptionPane.buttonFont", f);
        UIManager.put("ScrollBar.width", 12);
        UIManager.put("ComboBox.background", Color.WHITE);
        UIManager.put("ComboBox.selectionBackground", CHON);
        UIManager.put("ComboBox.selectionForeground", TEXT);
        UIManager.put("TextField.selectionBackground", CHON);
        UIManager.put("Button.focus", new Color(0, 0, 0, 0));
        UIManager.put("ToolTip.background", BG);
        UIManager.put("ToolTip.foreground", TEXT);
        // Chữ trên hộp thoại hệ thống
        UIManager.put("OptionPane.yesButtonText", "Có");
        UIManager.put("OptionPane.noButtonText", "Không");
        UIManager.put("OptionPane.cancelButtonText", "Hủy");
        UIManager.put("OptionPane.okButtonText", "OK");
    }

    public static String mauHtml(Color mau) {
        return String.format("#%06X", mau.getRGB() & 0xFFFFFF);
    }

    public static Font font(int style, float size) {
        return new Font(tenFont, style, Math.round(size));
    }

    // 25000 -> "25.000"
    public static String so(Object v) {
        if (v == null) return "0";
        return SO.format(((Number) v).longValue());
    }

    // 25000 -> "25.000đ"
    public static String tien(Object v) {
        if (v == null) return "-";
        return so(v) + "đ";
    }

    public static void khuDangCua(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    }

    // Vẽ nhãn bo tròn (badge) giữa hoặc đầu ô
    public static void veBadge(Graphics2D g, Rectangle o, String chu, Color nen, Color mau, boolean giua) {
        khuDangCua(g);
        Font f = font(Font.BOLD, 11);
        g.setFont(f);
        FontMetrics fm = g.getFontMetrics();
        int w = fm.stringWidth(chu) + 18, h = 20;
        int x = giua ? o.x + (o.width - w) / 2 : o.x + 12;
        int y = o.y + (o.height - h) / 2;
        g.setColor(nen);
        g.fillRoundRect(x, y, w, h, h, h);
        g.setColor(mau);
        g.drawString(chu, x + 9, y + (h - fm.getHeight()) / 2 + fm.getAscent());
    }

    // Kiểu bảng giống thẻ bảng trên web: dòng cao, kẻ ngang mảnh, tiêu đề chữ nhỏ in hoa
    public static void kieuBang(JTable t, int chieuCaoDong) {
        t.setRowHeight(chieuCaoDong);
        t.setShowVerticalLines(false);
        t.setShowHorizontalLines(true);
        t.setGridColor(LINE);
        t.setIntercellSpacing(new Dimension(0, 1));
        t.setSelectionBackground(CHON);
        t.setSelectionForeground(TEXT);
        t.setForeground(TEXT);
        t.setBackground(Color.WHITE);
        t.setFont(font(Font.PLAIN, 13));
        t.setFillsViewportHeight(true);
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        t.setAutoCreateRowSorter(false);
        t.setFocusable(false);

        JTableHeader h = t.getTableHeader();
        h.setReorderingAllowed(false);
        h.setPreferredSize(new Dimension(0, 38));
        h.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean sel, boolean focus, int row, int col) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, false, false, row, col);
                l.setText(value == null ? "" : value.toString().toUpperCase(Locale.forLanguageTag("vi")));
                l.setFont(font(Font.BOLD, 11));
                l.setForeground(BRAND_DARK);
                l.setBackground(BG);
                l.setOpaque(true);
                l.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, LINE), new EmptyBorder(0, 14, 0, 14)));
                l.setHorizontalAlignment(canhLeCot(table, col));
                return l;
            }
        });
    }

    // Renderer nào cài interface này thì tiêu đề cột căn theo (trái / giữa / phải)
    public interface CoCanhLe {
        int canhLe();
    }

    static int canhLeCot(JTable t, int col) {
        Object r = t.getColumnModel().getColumn(col).getCellRenderer();
        if (r instanceof CoCanhLe c) return c.canhLe();
        if (r instanceof DefaultTableCellRenderer d) return d.getHorizontalAlignment();
        return SwingConstants.LEFT;
    }

    // Ô nhập có viền bo, đệm trong giống input trên web
    public static <T extends JComponent> T kieuO(T c) {
        c.setFont(font(Font.PLAIN, 13));
        c.setBorder(BorderFactory.createCompoundBorder(new VienBo(VIEN_O, 8), new EmptyBorder(6, 10, 6, 10)));
        c.setBackground(Color.WHITE);
        return c;
    }

    // Combo phẳng: viền bo, mũi tên vẽ tay, không viền đôi
    public static JComboBox<?> kieuCombo(JComboBox<?> c) {
        c.setUI(new javax.swing.plaf.basic.BasicComboBoxUI() {
            @Override
            protected JButton createArrowButton() {
                JButton b = new JButton() {
                    @Override
                    protected void paintComponent(Graphics g0) {
                        Graphics2D g = (Graphics2D) g0.create();
                        khuDangCua(g);
                        g.setColor(MUTED);
                        g.setStroke(new BasicStroke(1.6f));
                        int cx = getWidth() / 2, cy = getHeight() / 2;
                        g.drawPolyline(new int[]{cx - 4, cx, cx + 4}, new int[]{cy - 2, cy + 2, cy - 2}, 3);
                        g.dispose();
                    }
                };
                b.setBorder(BorderFactory.createEmptyBorder());
                b.setContentAreaFilled(false);
                b.setFocusable(false);
                b.setPreferredSize(new Dimension(28, 28));
                return b;
            }

            @Override
            public void paintCurrentValueBackground(Graphics g, Rectangle r, boolean focus) {
                g.setColor(Color.WHITE);
                g.fillRect(r.x, r.y, r.width, r.height);
            }
        });
        c.setFont(font(Font.PLAIN, 13));
        c.setBackground(Color.WHITE);
        c.setBorder(BorderFactory.createCompoundBorder(new VienBo(VIEN_O, 8), new EmptyBorder(0, 4, 0, 2)));
        c.setPreferredSize(new Dimension(220, 36));
        c.setFocusable(true);
        return c;
    }

    // Thanh cuộn mảnh màu xám (thay thanh cuộn xanh mặc định của Java)
    public static JScrollPane kieuCuon(JScrollPane sp) {
        for (JScrollBar sb : new JScrollBar[]{sp.getVerticalScrollBar(), sp.getHorizontalScrollBar()}) {
            sb.setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
                @Override
                protected void configureScrollBarColors() {
                    thumbColor = VIEN_O;
                    trackColor = Color.WHITE;
                }

                @Override
                protected JButton createDecreaseButton(int o) {
                    return nutRong();
                }

                @Override
                protected JButton createIncreaseButton(int o) {
                    return nutRong();
                }

                @Override
                protected void paintTrack(Graphics g, JComponent c, Rectangle r) {
                    g.setColor(c.getParent() != null ? c.getParent().getBackground() : Color.WHITE);
                    g.fillRect(r.x, r.y, r.width, r.height);
                }

                @Override
                protected void paintThumb(Graphics g0, JComponent c, Rectangle r) {
                    if (r.isEmpty()) return;
                    Graphics2D g = (Graphics2D) g0.create();
                    khuDangCua(g);
                    g.setColor(isThumbRollover() ? MUTED : thumbColor);
                    g.fillRoundRect(r.x + 3, r.y + 3, r.width - 6, r.height - 6, 6, 6);
                    g.dispose();
                }
            });
            sb.setPreferredSize(sb.getOrientation() == JScrollBar.VERTICAL ? new Dimension(12, 0) : new Dimension(0, 12));
            sb.setOpaque(false);
        }
        sp.setCorner(ScrollPaneConstants.UPPER_RIGHT_CORNER, null);
        return sp;
    }

    private static JButton nutRong() {
        JButton b = new JButton();
        b.setPreferredSize(new Dimension(0, 0));
        b.setMinimumSize(new Dimension(0, 0));
        b.setMaximumSize(new Dimension(0, 0));
        return b;
    }

    public static JLabel nhan(String chu) {
        JLabel l = new JLabel(chu);
        l.setFont(font(Font.PLAIN, 12));
        l.setForeground(MUTED);
        return l;
    }

    public static JLabel tieuDe(String chu, float co) {
        JLabel l = new JLabel(chu);
        l.setFont(font(Font.BOLD, co));
        l.setForeground(TEXT);
        return l;
    }

    // Viền bo góc (dùng cho ô nhập, combo)
    public static final class VienBo extends javax.swing.border.AbstractBorder {
        private final Color mau;
        private final int bo;

        public VienBo(Color mau, int bo) {
            this.mau = mau;
            this.bo = bo;
        }

        @Override
        public void paintBorder(Component c, Graphics g0, int x, int y, int w, int h) {
            Graphics2D g = (Graphics2D) g0.create();
            khuDangCua(g);
            g.setColor(c.hasFocus() || (c instanceof JComboBox<?> cb && cb.isPopupVisible()) ? BRAND : mau);
            g.drawRoundRect(x, y, w - 1, h - 1, bo, bo);
            g.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(2, 2, 2, 2);
        }
    }
}
