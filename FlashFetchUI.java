import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FlashFetchUI {
    private static final Color BG = new Color(248, 245, 239);
    private static final Color SURFACE = new Color(255, 255, 255);
    private static final Color SURFACE_ALT = new Color(245, 246, 248);
    private static final Color ACCENT = new Color(22, 138, 120);
    private static final Color ACCENT_DARK = new Color(16, 92, 82);
    private static final Color TEXT = new Color(30, 33, 36);
    private static final Color MUTED = new Color(108, 112, 118);

    private JFrame frame;
    private JPanel contentPanel;
    private CardLayout contentLayout;

    private JTextField searchField;
    private JList<OSItem> osList;
    private DefaultListModel<OSItem> osListModel;
    private JList<OSDatabase.OSEntry> releaseList;
    private DefaultListModel<OSDatabase.OSEntry> releaseModel;
    private JLabel osTitle;
    private JLabel osSubtitle;
    private JLabel downloadDirLabel;

    private DefaultListModel<Main.DownloadTask> activeModel;
    private DefaultListModel<Main.DownloadTask> completedModel;
    private JList<Main.DownloadTask> activeList;
    private JList<Main.DownloadTask> completedList;
    private Timer refreshTimer;

    private String currentFamily = "Windows";

    public void show() {
        setupLookAndFeel();
        frame = new JFrame("FlashFetch");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setMinimumSize(new Dimension(1150, 720));
        frame.setLocationRelativeTo(null);
        frame.setContentPane(buildRoot());
        frame.setVisible(true);

        refreshDownloads();
        refreshTimer = new Timer(450, e -> refreshDownloads());
        refreshTimer.start();
    }

    private void setupLookAndFeel() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            // Keep default LAF
        }

        UIManager.put("control", BG);
        UIManager.put("info", BG);
        UIManager.put("nimbusBase", ACCENT);
        UIManager.put("nimbusBlueGrey", new Color(220, 224, 229));
        UIManager.put("nimbusFocus", ACCENT);
        UIManager.put("text", TEXT);
    }

    private JPanel buildRoot() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG);

        JPanel sidebar = buildSidebar();
        root.add(sidebar, BorderLayout.WEST);

        contentLayout = new CardLayout();
        contentPanel = new JPanel(contentLayout);
        contentPanel.setBackground(BG);
        contentPanel.add(buildCatalogView(), "catalog");
        contentPanel.add(buildDownloadsView(), "downloads");
        contentPanel.add(buildSettingsView(), "settings");
        contentPanel.add(buildAboutView(), "about");

        root.add(contentPanel, BorderLayout.CENTER);
        return root;
    }

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setBackground(new Color(18, 38, 35));
        sidebar.setPreferredSize(new Dimension(220, 0));
        sidebar.setLayout(new BorderLayout());

        JPanel brand = new JPanel(new BorderLayout());
        brand.setBackground(new Color(18, 38, 35));
        brand.setBorder(new EmptyBorder(24, 18, 24, 18));
        JLabel title = new JLabel("FlashFetch");
        title.setForeground(new Color(236, 240, 238));
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));
        JLabel subtitle = new JLabel("ISO concierge");
        subtitle.setForeground(new Color(174, 188, 184));
        subtitle.setFont(subtitle.getFont().deriveFont(Font.PLAIN, 12f));
        brand.add(title, BorderLayout.NORTH);
        brand.add(subtitle, BorderLayout.SOUTH);
        sidebar.add(brand, BorderLayout.NORTH);

        JPanel nav = new JPanel();
        nav.setBackground(new Color(18, 38, 35));
        nav.setLayout(new GridLayout(0, 1, 8, 8));
        nav.setBorder(new EmptyBorder(10, 12, 10, 12));
        nav.add(navButton("Catalog", "catalog"));
        nav.add(navButton("Downloads", "downloads"));
        nav.add(navButton("Settings", "settings"));
        nav.add(navButton("About", "about"));
        sidebar.add(nav, BorderLayout.CENTER);

        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(new Color(18, 38, 35));
        footer.setBorder(new EmptyBorder(12, 16, 18, 16));
        JLabel hint = new JLabel("Tip: Use search to filter");
        hint.setForeground(new Color(140, 156, 152));
        hint.setFont(hint.getFont().deriveFont(Font.PLAIN, 11f));
        footer.add(hint, BorderLayout.CENTER);
        sidebar.add(footer, BorderLayout.SOUTH);

        return sidebar;
    }

    private JButton navButton(String text, String card) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setBackground(new Color(28, 54, 50));
        button.setForeground(new Color(236, 240, 238));
        button.setBorder(new EmptyBorder(12, 14, 12, 14));
        button.setFont(button.getFont().deriveFont(Font.BOLD, 13f));
        button.addActionListener(e -> contentLayout.show(contentPanel, card));
        return button;
    }

    private JPanel buildCatalogView() {
        JPanel panel = new JPanel(new BorderLayout(16, 16));
        panel.setBackground(BG);
        panel.setBorder(new EmptyBorder(18, 18, 18, 18));

        panel.add(buildHero(), BorderLayout.NORTH);
        panel.add(buildCatalogBody(), BorderLayout.CENTER);

        return panel;
    }

    private JPanel buildHero() {
        GradientPanel hero = new GradientPanel(
            new Color(246, 242, 233),
            new Color(222, 239, 234)
        );
        hero.setLayout(new BorderLayout(16, 12));
        hero.setBorder(new EmptyBorder(18, 20, 18, 20));

        JPanel left = new JPanel(new GridLayout(0, 1, 4, 4));
        left.setOpaque(false);
        JLabel title = new JLabel("Find the perfect OS");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 22f));
        title.setForeground(TEXT);
        JLabel blurb = new JLabel("Curated downloads with instant resume and clean metadata.");
        blurb.setForeground(MUTED);
        left.add(title);
        left.add(blurb);

        JPanel right = new JPanel(new BorderLayout(8, 8));
        right.setOpaque(false);
        searchField = new JTextField();
        searchField.setFont(searchField.getFont().deriveFont(14f));
        searchField.setBorder(new EmptyBorder(10, 12, 10, 12));
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { refreshOsList(); }
            @Override public void removeUpdate(DocumentEvent e) { refreshOsList(); }
            @Override public void changedUpdate(DocumentEvent e) { refreshOsList(); }
        });
        JLabel searchLabel = new JLabel("Search");
        searchLabel.setForeground(MUTED);
        right.add(searchLabel, BorderLayout.NORTH);
        right.add(searchField, BorderLayout.CENTER);

        hero.add(left, BorderLayout.WEST);
        hero.add(right, BorderLayout.EAST);

        return hero;
    }

    private JPanel buildCatalogBody() {
        JPanel body = new JPanel(new BorderLayout(16, 16));
        body.setOpaque(false);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setBorder(new EmptyBorder(0, 0, 0, 0));
        tabs.addTab("Windows", null);
        tabs.addTab("Linux", null);
        tabs.addTab("macOS", null);
        tabs.addTab("BSD", null);
        tabs.addTab("Other", null);
        tabs.addChangeListener(e -> {
            currentFamily = tabs.getTitleAt(tabs.getSelectedIndex());
            refreshOsList();
        });

        JPanel left = new JPanel(new BorderLayout(8, 8));
        left.setOpaque(false);
        left.add(tabs, BorderLayout.NORTH);

        osListModel = new DefaultListModel<>();
        osList = new JList<>(osListModel);
        osList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        osList.setCellRenderer(new OSItemRenderer());
        osList.setBackground(SURFACE);
        osList.setFixedCellHeight(48);
        osList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) updateDetails();
        });
        JScrollPane osScroll = new JScrollPane(osList);
        osScroll.setBorder(BorderFactory.createLineBorder(new Color(220, 224, 228)));
        osScroll.getViewport().setBackground(SURFACE);
        left.add(osScroll, BorderLayout.CENTER);

        JPanel right = new JPanel(new BorderLayout(12, 12));
        right.setBackground(SURFACE);
        right.setBorder(new EmptyBorder(16, 16, 16, 16));

        JPanel header = new JPanel(new GridLayout(0, 1, 4, 4));
        header.setOpaque(false);
        osTitle = new JLabel("Pick a system");
        osTitle.setFont(osTitle.getFont().deriveFont(Font.BOLD, 18f));
        osSubtitle = new JLabel("Choose a release to reveal versions");
        osSubtitle.setForeground(MUTED);
        header.add(osTitle);
        header.add(osSubtitle);

        releaseModel = new DefaultListModel<>();
        releaseList = new JList<>(releaseModel);
        releaseList.setCellRenderer(new ReleaseRenderer());
        releaseList.setFixedCellHeight(52);
        releaseList.setBackground(SURFACE_ALT);
        JScrollPane releaseScroll = new JScrollPane(releaseList);
        releaseScroll.setBorder(BorderFactory.createLineBorder(new Color(220, 224, 228)));
        releaseScroll.getViewport().setBackground(SURFACE_ALT);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        actions.setOpaque(false);
        JButton downloadButton = new AccentButton("Download");
        downloadButton.addActionListener(this::handleDownload);
        JButton openFolder = new OutlineButton("Open Folder");
        openFolder.addActionListener(e -> Main.openDownloadFolder());
        actions.add(downloadButton);
        actions.add(openFolder);

        right.add(header, BorderLayout.NORTH);
        right.add(releaseScroll, BorderLayout.CENTER);
        right.add(actions, BorderLayout.SOUTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, right);
        split.setResizeWeight(0.45);
        split.setBorder(null);
        split.setOpaque(false);
        split.setBackground(BG);

        body.add(split, BorderLayout.CENTER);
        refreshOsList();

        return body;
    }

    private JPanel buildDownloadsView() {
        JPanel panel = new JPanel(new BorderLayout(16, 16));
        panel.setBackground(BG);
        panel.setBorder(new EmptyBorder(18, 18, 18, 18));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("Download Queue");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));
        title.setForeground(TEXT);
        JLabel hint = new JLabel("Pause, resume, or cancel without losing progress");
        hint.setForeground(MUTED);
        header.add(title, BorderLayout.NORTH);
        header.add(hint, BorderLayout.SOUTH);
        panel.add(header, BorderLayout.NORTH);

        activeModel = new DefaultListModel<>();
        activeList = new JList<>(activeModel);
        activeList.setCellRenderer(new DownloadRenderer());
        activeList.setFixedCellHeight(66);
        activeList.setBackground(SURFACE);

        completedModel = new DefaultListModel<>();
        completedList = new JList<>(completedModel);
        completedList.setCellRenderer(new DownloadRenderer());
        completedList.setFixedCellHeight(66);
        completedList.setBackground(SURFACE);

        JPanel lists = new JPanel(new GridLayout(1, 2, 16, 16));
        lists.setOpaque(false);
        lists.add(buildListCard("Active", activeList));
        lists.add(buildListCard("Completed", completedList));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        actions.setOpaque(false);
        JButton pauseResume = new OutlineButton("Pause / Resume");
        pauseResume.addActionListener(e -> togglePause());
        JButton cancel = new OutlineButton("Cancel");
        cancel.addActionListener(e -> cancelSelected());
        JButton openFolder = new OutlineButton("Open Folder");
        openFolder.addActionListener(e -> Main.openDownloadFolder());
        JButton clear = new OutlineButton("Clear Completed");
        clear.addActionListener(e -> completedModel.clear());
        actions.add(pauseResume);
        actions.add(cancel);
        actions.add(openFolder);
        actions.add(clear);

        panel.add(lists, BorderLayout.CENTER);
        panel.add(actions, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildSettingsView() {
        JPanel panel = new JPanel(new BorderLayout(16, 16));
        panel.setBackground(BG);
        panel.setBorder(new EmptyBorder(18, 18, 18, 18));

        JLabel title = new JLabel("Preferences");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));
        title.setForeground(TEXT);
        panel.add(title, BorderLayout.NORTH);

        JPanel form = new JPanel();
        form.setBackground(SURFACE);
        form.setBorder(new EmptyBorder(16, 16, 16, 16));
        form.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        downloadDirLabel = new JLabel(Main.getDownloadDirectory());
        downloadDirLabel.setForeground(MUTED);
        JButton browse = new OutlineButton("Change Folder");
        browse.addActionListener(e -> chooseDirectory());

        addRow(form, gbc, 0, new JLabel("Download directory"), downloadDirLabel, browse);

        JSpinner concurrent = new JSpinner(new SpinnerNumberModel(Main.getConcurrentDownloads(), 1, 5, 1));
        addRow(form, gbc, 1, new JLabel("Concurrent downloads"), concurrent, null);

        JSpinner timeout = new JSpinner(new SpinnerNumberModel(Main.getDownloadTimeout() / 1000, 10, 120, 5));
        addRow(form, gbc, 2, new JLabel("Timeout (seconds)"), timeout, null);

        JCheckBox retry = new JCheckBox("Auto retry on failure");
        retry.setSelected(Main.isAutoRetry());
        retry.setOpaque(false);
        addRow(form, gbc, 3, new JLabel("Retry"), retry, null);

        JCheckBox checksum = new JCheckBox("Verify checksums when available");
        checksum.setSelected(Main.isChecksumVerification());
        checksum.setOpaque(false);
        addRow(form, gbc, 4, new JLabel("Checksum"), checksum, null);

        JButton save = new AccentButton("Save Settings");
        save.addActionListener(e -> {
            Main.setConcurrentDownloads((Integer) concurrent.getValue());
            Main.setDownloadTimeout(((Integer) timeout.getValue()) * 1000);
            Main.setAutoRetry(retry.isSelected());
            Main.setChecksumVerification(checksum.isSelected());
            JOptionPane.showMessageDialog(frame, "Settings saved.");
        });

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.setOpaque(false);
        footer.add(save);

        panel.add(form, BorderLayout.CENTER);
        panel.add(footer, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildAboutView() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG);
        panel.setBorder(new EmptyBorder(24, 24, 24, 24));

        GradientPanel card = new GradientPanel(new Color(242, 238, 230), new Color(218, 230, 238));
        card.setBorder(new EmptyBorder(20, 20, 20, 20));
        card.setLayout(new GridLayout(0, 1, 6, 6));

        JLabel title = new JLabel("FlashFetch UI");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 22f));
        JLabel body = new JLabel("A focused Swing front end for the FlashFetch ISO library.");
        body.setForeground(MUTED);
        JLabel detail = new JLabel("Built for speed, clarity, and a clean download flow.");
        detail.setForeground(MUTED);

        card.add(title);
        card.add(body);
        card.add(detail);

        panel.add(card, BorderLayout.NORTH);
        return panel;
    }

    private JPanel buildListCard(String title, JList<Main.DownloadTask> list) {
        JPanel card = new JPanel(new BorderLayout(8, 8));
        card.setBackground(SURFACE);
        card.setBorder(new EmptyBorder(12, 12, 12, 12));
        JLabel label = new JLabel(title);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 14f));
        JScrollPane scroll = new JScrollPane(list);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(220, 224, 228)));
        scroll.getViewport().setBackground(SURFACE);
        card.add(label, BorderLayout.NORTH);
        card.add(scroll, BorderLayout.CENTER);
        return card;
    }

    private void addRow(JPanel form, GridBagConstraints gbc, int row, JComponent label, JComponent field, JComponent action) {
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.15;
        form.add(label, gbc);
        gbc.gridx = 1; gbc.weightx = 0.55;
        form.add(field, gbc);
        gbc.gridx = 2; gbc.weightx = 0.3;
        if (action != null) form.add(action, gbc);
    }

    private void refreshOsList() {
        String filter = searchField == null ? "" : searchField.getText().trim().toLowerCase();
        osListModel.clear();
        for (OSItem item : buildOsItems(currentFamily, filter)) {
            osListModel.addElement(item);
        }
        if (!osListModel.isEmpty()) {
            osList.setSelectedIndex(0);
        }
    }

    private List<OSItem> buildOsItems(String family, String filter) {
        List<OSItem> items = new ArrayList<>();
        if ("Linux".equals(family)) {
            for (OSDatabase.Category category : OSDatabase.getLinuxCategories()) {
                List<OSItem> group = new ArrayList<>();
                for (String distro : category.distros) {
                    String label = OSDatabase.getDisplayName(distro);
                    if (!filter.isEmpty() && !label.toLowerCase().contains(filter)) continue;
                    group.add(new OSItem(false, distro, label));
                }
                if (!group.isEmpty()) {
                    items.add(new OSItem(true, null, category.displayName));
                    items.addAll(group);
                }
            }
            return items;
        }

        String[][] menu = getMenuForFamily(family);
        String pendingHeader = null;
        for (String[] entry : menu) {
            if ("---".equals(entry[0])) {
                pendingHeader = "";
                continue;
            }
            String label = entry[1];
            if (!filter.isEmpty() && !label.toLowerCase().contains(filter)) continue;
            if (pendingHeader != null) {
                items.add(new OSItem(true, null, ""));
                pendingHeader = null;
            }
            items.add(new OSItem(false, entry[0], label));
        }
        return items;
    }

    private String[][] getMenuForFamily(String family) {
        if ("Windows".equals(family)) return OSDatabase.getWindowsMenu();
        if ("macOS".equals(family)) return OSDatabase.getMacOSMenu();
        if ("BSD".equals(family)) return OSDatabase.getBSDMenu();
        return OSDatabase.getOtherMenu();
    }

    private void updateDetails() {
        OSItem item = osList.getSelectedValue();
        if (item == null || item.header) {
            releaseModel.clear();
            osTitle.setText("Pick a system");
            osSubtitle.setText("Choose a release to reveal versions");
            return;
        }

        osTitle.setText(item.label);
        osSubtitle.setText("Available builds and architectures");
        releaseModel.clear();
        for (OSDatabase.OSEntry entry : OSDatabase.getDownloads(item.id)) {
            releaseModel.addElement(entry);
        }
        if (!releaseModel.isEmpty()) releaseList.setSelectedIndex(0);
    }

    private void handleDownload(ActionEvent e) {
        OSItem item = osList.getSelectedValue();
        OSDatabase.OSEntry entry = releaseList.getSelectedValue();
        if (item == null || item.header || entry == null) {
            JOptionPane.showMessageDialog(frame, "Select a release first.");
            return;
        }

        String filename = buildFileName(entry);
        File defaultFile = new File(Main.getDownloadDirectory(), filename);
        File target = promptForTarget(defaultFile);
        if (target == null) return;

        boolean resume = false;
        boolean overwrite = false;
        if (target.exists()) {
            String[] options = {"Overwrite", "Resume", "Cancel"};
            int choice = JOptionPane.showOptionDialog(frame,
                "File already exists. What do you want to do?",
                "Existing file",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[1]
            );
            if (choice == 2 || choice == JOptionPane.CLOSED_OPTION) return;
            overwrite = choice == 0;
            resume = choice == 1;
        }

        Main.startDownloadTask(entry.name + " " + entry.version, entry.url, target.getAbsolutePath(), entry.checksum, resume, overwrite);
        contentLayout.show(contentPanel, "downloads");
    }

    private File promptForTarget(File defaultFile) {
        String[] options = {"Download", "Choose Location", "Cancel"};
        int choice = JOptionPane.showOptionDialog(frame,
            "Save to: " + defaultFile.getAbsolutePath(),
            "Download",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            options[0]
        );
        if (choice == 2 || choice == JOptionPane.CLOSED_OPTION) return null;
        if (choice == 0) return defaultFile;

        JFileChooser chooser = new JFileChooser(defaultFile.getParentFile());
        chooser.setSelectedFile(defaultFile);
        int result = chooser.showSaveDialog(frame);
        if (result == JFileChooser.APPROVE_OPTION) return chooser.getSelectedFile();
        return null;
    }

    private String buildFileName(OSDatabase.OSEntry entry) {
        String base = entry.name + "_" + entry.version + "_" + entry.arch + ".iso";
        return base.replaceAll("[<>:\"/\\\\|?*]", "_");
    }

    private void refreshDownloads() {
        List<Main.DownloadTask> queue = Main.getDownloadQueue();
        activeModel.clear();
        for (Main.DownloadTask task : queue) {
            if (!"completed".equals(task.status) && !"canceled".equals(task.status)) {
                activeModel.addElement(task);
            }
        }

        completedModel.clear();
        for (Main.DownloadTask task : Main.getCompletedDownloads()) {
            completedModel.addElement(task);
        }
    }

    private void togglePause() {
        Main.DownloadTask task = activeList.getSelectedValue();
        if (task == null) return;
        if ("paused".equals(task.status) || "failed".equals(task.status)) {
            Main.resumeDownload(task);
        } else {
            Main.pauseDownload(task);
        }
    }

    private void cancelSelected() {
        Main.DownloadTask task = activeList.getSelectedValue();
        if (task == null) return;
        int choice = JOptionPane.showConfirmDialog(frame, "Cancel this download?", "Cancel", JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            Main.cancelDownload(task, false);
        }
    }

    private void chooseDirectory() {
        JFileChooser chooser = new JFileChooser(new File(Main.getDownloadDirectory()));
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int result = chooser.showOpenDialog(frame);
        if (result == JFileChooser.APPROVE_OPTION) {
            File dir = chooser.getSelectedFile();
            Main.setDownloadDirectory(dir.getAbsolutePath());
            downloadDirLabel.setText(Main.getDownloadDirectory());
        }
    }

    private static class OSItem {
        final boolean header;
        final String id;
        final String label;

        OSItem(boolean header, String id, String label) {
            this.header = header;
            this.id = id;
            this.label = label;
        }

        @Override
        public String toString() { return label; }
    }

    private static class GradientPanel extends JPanel {
        private final Color left;
        private final Color right;

        GradientPanel(Color left, Color right) {
            this.left = left;
            this.right = right;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setPaint(new GradientPaint(0, 0, left, getWidth(), getHeight(), right));
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static class AccentButton extends JButton {
        AccentButton(String text) {
            super(text);
            setForeground(Color.WHITE);
            setBackground(ACCENT);
            setFocusPainted(false);
            setBorder(new EmptyBorder(10, 18, 10, 18));
        }
    }

    private static class OutlineButton extends JButton {
        OutlineButton(String text) {
            super(text);
            setForeground(ACCENT_DARK);
            setBackground(SURFACE_ALT);
            setFocusPainted(false);
            setBorder(new EmptyBorder(10, 16, 10, 16));
        }
    }

    private static class OSItemRenderer extends JPanel implements ListCellRenderer<OSItem> {
        private final JLabel label = new JLabel();

        OSItemRenderer() {
            setLayout(new BorderLayout());
            setBorder(new EmptyBorder(6, 12, 6, 12));
            label.setFont(label.getFont().deriveFont(Font.PLAIN, 13f));
            add(label, BorderLayout.CENTER);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends OSItem> list, OSItem value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            if (value.header) {
                label.setText(value.label.isEmpty() ? "" : value.label);
                label.setFont(label.getFont().deriveFont(Font.BOLD, 11f));
                label.setForeground(MUTED);
                setBackground(list.getBackground());
                setOpaque(true);
                return this;
            }

            label.setText(value.label);
            label.setFont(label.getFont().deriveFont(Font.PLAIN, 13f));
            label.setForeground(TEXT);
            setBackground(isSelected ? new Color(229, 242, 239) : list.getBackground());
            setOpaque(true);
            return this;
        }
    }

    private static class ReleaseRenderer extends JPanel implements ListCellRenderer<OSDatabase.OSEntry> {
        private final JLabel title = new JLabel();
        private final JLabel meta = new JLabel();

        ReleaseRenderer() {
            setLayout(new GridLayout(0, 1, 2, 2));
            setBorder(new EmptyBorder(6, 10, 6, 10));
            title.setFont(title.getFont().deriveFont(Font.BOLD, 13f));
            meta.setForeground(MUTED);
            add(title);
            add(meta);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends OSDatabase.OSEntry> list, OSDatabase.OSEntry value,
                                                      int index, boolean isSelected, boolean cellHasFocus) {
            title.setText(value.name + " " + value.version);
            meta.setText(value.arch);
            setBackground(isSelected ? new Color(232, 236, 244) : list.getBackground());
            setOpaque(true);
            return this;
        }
    }

    private static class DownloadRenderer extends JPanel implements ListCellRenderer<Main.DownloadTask> {
        private final JLabel title = new JLabel();
        private final JLabel meta = new JLabel();
        private final JProgressBar progress = new JProgressBar();

        DownloadRenderer() {
            setLayout(new BorderLayout(8, 4));
            setBorder(new EmptyBorder(8, 10, 8, 10));
            title.setFont(title.getFont().deriveFont(Font.BOLD, 13f));
            meta.setForeground(MUTED);
            progress.setStringPainted(false);
            progress.setForeground(ACCENT);
            progress.setBorderPainted(false);

            JPanel top = new JPanel(new BorderLayout());
            top.setOpaque(false);
            top.add(title, BorderLayout.WEST);
            top.add(meta, BorderLayout.EAST);
            add(top, BorderLayout.NORTH);
            add(progress, BorderLayout.SOUTH);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends Main.DownloadTask> list, Main.DownloadTask value,
                                                      int index, boolean isSelected, boolean cellHasFocus) {
            title.setText(value.name);
            meta.setText(value.status + "  " + value.getProgress() + "%");
            progress.setValue(value.getProgress());
            setBackground(isSelected ? new Color(235, 244, 241) : list.getBackground());
            setOpaque(true);
            return this;
        }
    }
}
