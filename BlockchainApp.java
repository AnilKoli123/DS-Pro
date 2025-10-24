import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class BlockchainApp extends JFrame {
    private JTextField senderField, receiverField, amountField;
    private JButton sendButton, clearButton, exportButton;
    private JPanel transactionPanel;
    private JScrollPane scrollPane;
    private JLabel statusLabel, blockCountLabel;
    private ArrayList<Block> blockchain;
    private int blockCount = 0;

    // Modern color scheme
    private static final Color PRIMARY_COLOR = new Color(37, 99, 235); // Modern blue
    private static final Color PRIMARY_DARK = new Color(29, 78, 216);
    private static final Color SECONDARY_COLOR = new Color(99, 102, 241); // Indigo
    private static final Color BACKGROUND_DARK = new Color(17, 24, 39);
    private static final Color BACKGROUND_MEDIUM = new Color(31, 41, 55);
    private static final Color BACKGROUND_LIGHT = new Color(55, 65, 81);
    private static final Color TEXT_PRIMARY = new Color(243, 244, 246);
    private static final Color TEXT_SECONDARY = new Color(156, 163, 175);
    private static final Color SUCCESS_COLOR = new Color(16, 185, 129);
    private static final Color ERROR_COLOR = new Color(239, 68, 68);
    private static final Color BLOCK_BG_1 = new Color(45, 55, 72);
    private static final Color BLOCK_BG_2 = new Color(55, 65, 81);
    private static final Color BORDER_COLOR = new Color(75, 85, 99);

    public BlockchainApp() {
        setTitle("Blockchain Transaction Manager");
        setSize(1100, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Set modern look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        blockchain = new ArrayList<>();
        
        // Main container
        JPanel mainPanel = new JPanel(new BorderLayout(0, 0));
        mainPanel.setBackground(BACKGROUND_DARK);

        // Header panel
        JPanel headerPanel = createHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Content panel with form and transactions
        JPanel contentPanel = new JPanel(new BorderLayout(15, 0));
        contentPanel.setBackground(BACKGROUND_DARK);
        contentPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Left panel: input form
        JPanel formContainer = createFormPanel();
        
        // Right panel: transaction history
        JPanel transactionContainer = createTransactionPanel();

        contentPanel.add(formContainer, BorderLayout.WEST);
        contentPanel.add(transactionContainer, BorderLayout.CENTER);

        mainPanel.add(contentPanel, BorderLayout.CENTER);

        // Footer panel
        JPanel footerPanel = createFooterPanel();
        mainPanel.add(footerPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(BACKGROUND_MEDIUM);
        headerPanel.setBorder(new CompoundBorder(
            new MatteBorder(0, 0, 2, 0, PRIMARY_COLOR),
            new EmptyBorder(20, 30, 20, 30)
        ));

        JLabel titleLabel = new JLabel("Blockchain Transaction Manager");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(TEXT_PRIMARY);

        JLabel subtitleLabel = new JLabel("Secure and Transparent Transaction Ledger");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(TEXT_SECONDARY);

        JPanel titlePanel = new JPanel(new GridLayout(2, 1, 0, 5));
        titlePanel.setBackground(BACKGROUND_MEDIUM);
        titlePanel.add(titleLabel);
        titlePanel.add(subtitleLabel);

        blockCountLabel = new JLabel("Total Blocks: 0");
        blockCountLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        blockCountLabel.setForeground(SUCCESS_COLOR);

        headerPanel.add(titlePanel, BorderLayout.WEST);
        headerPanel.add(blockCountLabel, BorderLayout.EAST);

        return headerPanel;
    }

    private JPanel createFormPanel() {
        JPanel formContainer = new JPanel(new BorderLayout());
        formContainer.setPreferredSize(new Dimension(380, 0));
        formContainer.setBackground(BACKGROUND_MEDIUM);
        formContainer.setBorder(new CompoundBorder(
            new LineBorder(BORDER_COLOR, 1, true),
            new EmptyBorder(25, 25, 25, 25)
        ));

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(BACKGROUND_MEDIUM);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 0, 12, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;

        // Form title
        JLabel formTitle = new JLabel("New Transaction");
        formTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        formTitle.setForeground(TEXT_PRIMARY);
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 25, 0);
        formPanel.add(formTitle, gbc);

        // Sender field
        gbc.gridy++;
        gbc.insets = new Insets(12, 0, 5, 0);
        formPanel.add(createLabel("Sender"), gbc);
        
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 15, 0);
        senderField = createStyledTextField();
        formPanel.add(senderField, gbc);

        // Receiver field
        gbc.gridy++;
        gbc.insets = new Insets(12, 0, 5, 0);
        formPanel.add(createLabel("Receiver"), gbc);
        
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 15, 0);
        receiverField = createStyledTextField();
        formPanel.add(receiverField, gbc);

        // Amount field
        gbc.gridy++;
        gbc.insets = new Insets(12, 0, 5, 0);
        formPanel.add(createLabel("Amount (₹)"), gbc);
        
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 25, 0);
        amountField = createStyledTextField();
        formPanel.add(amountField, gbc);

        // Button panel
        JPanel buttonPanel = new JPanel(new GridLayout(2, 1, 0, 12));
        buttonPanel.setBackground(BACKGROUND_MEDIUM);

        sendButton = createPrimaryButton("Send Transaction");
        sendButton.addActionListener(e -> sendTransaction());
        
        clearButton = createSecondaryButton("Clear Fields");
        clearButton.addActionListener(e -> clearFields());

        buttonPanel.add(sendButton);
        buttonPanel.add(clearButton);

        gbc.gridy++;
        gbc.insets = new Insets(10, 0, 0, 0);
        formPanel.add(buttonPanel, gbc);

        formContainer.add(formPanel, BorderLayout.NORTH);
        return formContainer;
    }

    private JPanel createTransactionPanel() {
        JPanel container = new JPanel(new BorderLayout(0, 10));
        container.setBackground(BACKGROUND_DARK);

        JLabel historyTitle = new JLabel("Transaction History");
        historyTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        historyTitle.setForeground(TEXT_PRIMARY);
        historyTitle.setBorder(new EmptyBorder(0, 0, 10, 0));

        transactionPanel = new JPanel();
        transactionPanel.setLayout(new BoxLayout(transactionPanel, BoxLayout.Y_AXIS));
        transactionPanel.setBackground(BACKGROUND_DARK);
        transactionPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        scrollPane = new JScrollPane(transactionPanel);
        scrollPane.setBackground(BACKGROUND_DARK);
        scrollPane.setBorder(new LineBorder(BORDER_COLOR, 1, true));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getVerticalScrollBar().setBackground(BACKGROUND_LIGHT);

        // Add empty state message
        if (blockCount == 0) {
            JLabel emptyLabel = new JLabel("No transactions yet. Create your first block!");
            emptyLabel.setFont(new Font("Segoe UI", Font.ITALIC, 14));
            emptyLabel.setForeground(TEXT_SECONDARY);
            emptyLabel.setHorizontalAlignment(SwingConstants.CENTER);
            emptyLabel.setBorder(new EmptyBorder(50, 20, 50, 20));
            transactionPanel.add(emptyLabel);
        }

        container.add(historyTitle, BorderLayout.NORTH);
        container.add(scrollPane, BorderLayout.CENTER);

        return container;
    }

    private JPanel createFooterPanel() {
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setBackground(BACKGROUND_MEDIUM);
        footerPanel.setBorder(new CompoundBorder(
            new MatteBorder(2, 0, 0, 0, BORDER_COLOR),
            new EmptyBorder(15, 30, 15, 30)
        ));

        statusLabel = new JLabel("Ready");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(TEXT_SECONDARY);

        exportButton = createSecondaryButton("Export Blockchain");
        exportButton.setPreferredSize(new Dimension(150, 35));
        exportButton.addActionListener(e -> exportBlockchain());

        footerPanel.add(statusLabel, BorderLayout.WEST);
        footerPanel.add(exportButton, BorderLayout.EAST);

        return footerPanel;
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(TEXT_PRIMARY);
        return label;
    }

    private JTextField createStyledTextField() {
        JTextField field = new JTextField(20);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBackground(BACKGROUND_LIGHT);
        field.setForeground(TEXT_PRIMARY);
        field.setCaretColor(TEXT_PRIMARY);
        field.setBorder(new CompoundBorder(
            new LineBorder(BORDER_COLOR, 1, true),
            new EmptyBorder(10, 12, 10, 12)
        ));
        
        // Add focus effects
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                field.setBorder(new CompoundBorder(
                    new LineBorder(PRIMARY_COLOR, 2, true),
                    new EmptyBorder(9, 11, 9, 11)
                ));
            }

            @Override
            public void focusLost(FocusEvent e) {
                field.setBorder(new CompoundBorder(
                    new LineBorder(BORDER_COLOR, 1, true),
                    new EmptyBorder(10, 12, 10, 12)
                ));
            }
        });

        return field;
    }

    private JButton createPrimaryButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBackground(PRIMARY_COLOR);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(12, 20, 12, 20));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Hover effect
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(PRIMARY_DARK);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(PRIMARY_COLOR);
            }
        });

        return button;
    }

    private JButton createSecondaryButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setBackground(BACKGROUND_LIGHT);
        button.setForeground(TEXT_PRIMARY);
        button.setFocusPainted(false);
        button.setBorder(new CompoundBorder(
            new LineBorder(BORDER_COLOR, 1, true),
            new EmptyBorder(10, 18, 10, 18)
        ));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Hover effect
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(BACKGROUND_MEDIUM);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(BACKGROUND_LIGHT);
            }
        });

        return button;
    }

    private void sendTransaction() {
        String sender = senderField.getText().trim();
        String receiver = receiverField.getText().trim();
        String amount = amountField.getText().trim();

        if (sender.isEmpty() || receiver.isEmpty() || amount.isEmpty()) {
            showError("Please fill in all fields.");
            return;
        }

        try {
            Double.parseDouble(amount);
        } catch (NumberFormatException e) {
            showError("Amount must be a valid number.");
            return;
        }

        // Remove empty state message on first transaction
        if (blockCount == 0) {
            transactionPanel.removeAll();
        }

        blockCount++;
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String previousHash = blockCount == 1 ? "0000000000000000" : blockchain.get(blockchain.size() - 1).hash.substring(0, 16);
        
        String blockData = sender + receiver + amount + timestamp + previousHash;
        String hash = generateHash(blockData);
        
        Block block = new Block(blockCount, sender, receiver, amount, timestamp, previousHash, hash);
        blockchain.add(block);

        // Create block UI
        JPanel blockPanel = createBlockPanel(block);
        transactionPanel.add(blockPanel);
        transactionPanel.add(Box.createRigidArea(new Dimension(0, 12)));
        
        transactionPanel.revalidate();
        transactionPanel.repaint();

        // Scroll to bottom
        SwingUtilities.invokeLater(() -> {
            JScrollBar vertical = scrollPane.getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        });

        // Update UI
        blockCountLabel.setText("Total Blocks: " + blockCount);
        statusLabel.setText("Block #" + blockCount + " added successfully");
        statusLabel.setForeground(SUCCESS_COLOR);

        clearFields();
    }

    private JPanel createBlockPanel(Block block) {
        JPanel blockPanel = new JPanel();
        blockPanel.setLayout(new BorderLayout(15, 0));
        blockPanel.setBackground(blockCount % 2 == 1 ? BLOCK_BG_1 : BLOCK_BG_2);
        blockPanel.setBorder(new CompoundBorder(
            new LineBorder(BORDER_COLOR, 1, true),
            new EmptyBorder(15, 15, 15, 15)
        ));
        blockPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 140));

        // Left: Block number
        JPanel numberPanel = new JPanel(new BorderLayout());
        numberPanel.setOpaque(false);
        
        JLabel blockNumber = new JLabel("#" + block.blockNumber);
        blockNumber.setFont(new Font("Segoe UI", Font.BOLD, 24));
        blockNumber.setForeground(SECONDARY_COLOR);
        numberPanel.add(blockNumber, BorderLayout.NORTH);

        // Center: Transaction details
        JPanel detailsPanel = new JPanel(new GridLayout(5, 1, 0, 5));
        detailsPanel.setOpaque(false);

        detailsPanel.add(createDetailLabel("Sender: ", block.sender, TEXT_SECONDARY, TEXT_PRIMARY));
        detailsPanel.add(createDetailLabel("Receiver: ", block.receiver, TEXT_SECONDARY, TEXT_PRIMARY));
        detailsPanel.add(createDetailLabel("Amount: ", "₹" + block.amount, TEXT_SECONDARY, SUCCESS_COLOR));
        detailsPanel.add(createDetailLabel("Timestamp: ", block.timestamp, TEXT_SECONDARY, TEXT_SECONDARY));
        detailsPanel.add(createDetailLabel("Hash: ", block.hash.substring(0, 24) + "...", TEXT_SECONDARY, new Color(147, 197, 253)));

        blockPanel.add(numberPanel, BorderLayout.WEST);
        blockPanel.add(detailsPanel, BorderLayout.CENTER);

        return blockPanel;
    }

    private JLabel createDetailLabel(String key, String value, Color keyColor, Color valueColor) {
        JLabel label = new JLabel("<html><span style='color:rgb(" + keyColor.getRed() + "," + keyColor.getGreen() + "," + keyColor.getBlue() + ")'>" + key + "</span>" +
                                   "<span style='color:rgb(" + valueColor.getRed() + "," + valueColor.getGreen() + "," + valueColor.getBlue() + ")'>" + value + "</span></html>");
        label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        return label;
    }

    private void clearFields() {
        senderField.setText("");
        receiverField.setText("");
        amountField.setText("");
        senderField.requestFocus();
    }

    private void showError(String message) {
        statusLabel.setText(message);
        statusLabel.setForeground(ERROR_COLOR);
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void exportBlockchain() {
        if (blockchain.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No transactions to export.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        StringBuilder export = new StringBuilder();
        export.append("BLOCKCHAIN EXPORT\n");
        export.append("=================\n\n");
        
        for (Block block : blockchain) {
            export.append("Block #").append(block.blockNumber).append("\n");
            export.append("Sender: ").append(block.sender).append("\n");
            export.append("Receiver: ").append(block.receiver).append("\n");
            export.append("Amount: ").append(block.amount).append("\n");
            export.append("Timestamp: ").append(block.timestamp).append("\n");
            export.append("Previous Hash: ").append(block.previousHash).append("\n");
            export.append("Hash: ").append(block.hash).append("\n");
            export.append("\n");
        }

        JTextArea textArea = new JTextArea(export.toString());
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(600, 400));
        
        JOptionPane.showMessageDialog(this, scrollPane, "Blockchain Export", JOptionPane.INFORMATION_MESSAGE);
        statusLabel.setText("Blockchain exported successfully");
        statusLabel.setForeground(SUCCESS_COLOR);
    }

    private String generateHash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return "Error";
        }
    }

    // Block data structure
    private static class Block {
        int blockNumber;
        String sender;
        String receiver;
        String amount;
        String timestamp;
        String previousHash;
        String hash;

        Block(int blockNumber, String sender, String receiver, String amount, String timestamp, String previousHash, String hash) {
            this.blockNumber = blockNumber;
            this.sender = sender;
            this.receiver = receiver;
            this.amount = amount;
            this.timestamp = timestamp;
            this.previousHash = previousHash;
            this.hash = hash;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            BlockchainApp app = new BlockchainApp();
            app.setVisible(true);
        });
    }
}
