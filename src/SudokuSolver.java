import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SudokuSolver extends JFrame {
    private static final int SIZE = 9;
    private JTextField[][] cells = new JTextField[SIZE][SIZE];
    private JButton solveButton, resetButton;
    private JLabel statusLabel;

    public SudokuSolver() {
        setTitle("Sudoku Solver");
        setSize(550, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Title panel
        JPanel titlePanel = new JPanel();
        titlePanel.add(new JLabel("SUDOKU SOLVER", SwingConstants.CENTER));
        add(titlePanel, BorderLayout.NORTH);

        // Instructions panel
        JPanel instructionPanel = new JPanel();
        instructionPanel.setLayout(new BoxLayout(instructionPanel, BoxLayout.Y_AXIS));
        instructionPanel.add(new JLabel("Enter your puzzle (use 0 or leave blank for empty cells)"));
        instructionPanel.add(new JLabel("Click SOLVE to find solution"));
        instructionPanel.add(statusLabel = new JLabel("Ready", SwingConstants.CENTER));
        statusLabel.setForeground(Color.BLUE);
        add(instructionPanel, BorderLayout.NORTH);

        // Sudoku grid panel with 3x3 blocks
        JPanel gridPanel = new JPanel(new GridLayout(3, 3, 4, 4));
        gridPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Create 9 panels for 3x3 blocks
        for (int block = 0; block < 9; block++) {
            JPanel blockPanel = new JPanel(new GridLayout(3, 3));
            blockPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
            gridPanel.add(blockPanel);

            // Calculate starting row and column for this block
            int startRow = (block / 3) * 3;
            int startCol = (block % 3) * 3;

            // Add 9 cells to each block
            for (int cell = 0; cell < 9; cell++) {
                int row = startRow + cell / 3;
                int col = startCol + cell % 3;

                cells[row][col] = new JTextField();
                cells[row][col].setPreferredSize(new Dimension(40, 40));
                cells[row][col].setHorizontalAlignment(JTextField.CENTER);
                cells[row][col].setFont(new Font("Arial", Font.BOLD, 18));

                // Add input validation
                cells[row][col].setDocument(new JTextFieldLimit(1));
                blockPanel.add(cells[row][col]);
            }
        }
        add(gridPanel, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel();
        solveButton = new JButton("Solve");
        solveButton.setBackground(new Color(100, 200, 100));
        solveButton.setFocusable(false);
        solveButton.setForeground(Color.WHITE);
        solveButton.setFont(new Font("Arial", Font.BOLD, 14));

        resetButton = new JButton("Reset");
        resetButton.setBackground(new Color(200, 100, 100));
        resetButton.setFocusable(false);
        resetButton.setForeground(Color.WHITE);
        resetButton.setFont(new Font("Arial", Font.BOLD, 14));

        solveButton.addActionListener(new SolveAction());
        resetButton.addActionListener(new ResetAction());

        buttonPanel.add(resetButton);
        buttonPanel.add(solveButton);
        add(buttonPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

     private class SolveAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            int[][] board = new int[SIZE][SIZE];
            statusLabel.setText("Validating input...");
            statusLabel.setForeground(Color.BLUE);

            try {
                for (int row = 0; row < SIZE; row++) {
                    for (int col = 0; col < SIZE; col++) {
                        String text = cells[row][col].getText().trim();
                        board[row][col] = text.isEmpty() ? 0 : Integer.parseInt(text);
                        if (board[row][col] < 0 || board[row][col] > 9) {
                            throw new NumberFormatException();
                        }
                    }
                }

                // Check for conflicts in initial input
                String conflict = findInputConflict(board);
                if (conflict != null) {
                    JOptionPane.showMessageDialog(SudokuSolver.this,
                        "Invalid puzzle: " + conflict + "\nPlease correct the input and try again.",
                        "Conflict Found",
                        JOptionPane.ERROR_MESSAGE);
                    statusLabel.setText("Conflict found - Check input");
                    statusLabel.setForeground(Color.RED);
                    return;
                }

                statusLabel.setText("Solving...");
                if (solveSudoku(board)) {
                    statusLabel.setText("Solved!");
                    statusLabel.setForeground(Color.GREEN);
                    displaySolution(board);
                } else {
                    statusLabel.setText("No solution exists");
                    statusLabel.setForeground(Color.RED);
                }
            } catch (NumberFormatException ex) {
                statusLabel.setText("Invalid input! (1-9 only)");
                statusLabel.setForeground(Color.RED);
            }
        }
         private String findInputConflict(int[][] board) {
            // Check rows
            for (int row = 0; row < SIZE; row++) {
                        boolean[] used = new boolean[SIZE + 1];
                for (int col = 0; col < SIZE; col++) {
                    int num = board[row][col];
                    if (num != 0) {
                        if (used[num]) {
                            return "Duplicate " + num + " in row " + (row + 1);
                        }
                        used[num] = true;
                    }
                }
            }

            // Check columns
            for (int col = 0; col < SIZE; col++) {
                boolean[] used = new boolean[SIZE + 1];
                for (int row = 0; row < SIZE; row++) {
                    int num = board[row][col];
                    if (num != 0) {
                        if (used[num]) {
                            return "Duplicate " + num + " in column " + (col + 1);
                        }
                        used[num] = true;
                    }
                }
            }
             // Check 3x3 boxes
            for (int box = 0; box < SIZE; box++) {
                boolean[] used = new boolean[SIZE + 1];
                int boxRow = (box / 3) * 3;
                int boxCol = (box % 3) * 3;
                for (int row = 0; row < 3; row++) {
                    for (int col = 0; col < 3; col++) {
                        int num = board[row + boxRow][col + boxCol];
                        if (num != 0) {
                            if (used[num]) {
                                return "Duplicate " + num + " in box " + (box + 1);
                            }
                            used[num] = true;
                        }
                    }
                }
            }

            return null;
        }
    }

    private class ResetAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            for (int row = 0; row < SIZE; row++) {
                for (int col = 0; col < SIZE; col++) {
                    cells[row][col].setText("");
                    cells[row][col].setBackground(Color.WHITE);
                }
            }
            statusLabel.setText("Ready");
            statusLabel.setForeground(Color.BLUE);
        }
    }

    private boolean solveSudoku(int[][] board) {
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                if (board[row][col] == 0) {
                    for (int num = 1; num <= SIZE; num++) {
                        if (isSafe(board, row, col, num)) {
                            board[row][col] = num;
                            if (solveSudoku(board)) {
                                return true;
                            }
                            board[row][col] = 0; // backtrack
                        }
                    }
                    return false;
                }
            }
        }
        return true;
    }

    private boolean isSafe(int[][] board, int row, int col, int num) {
        // Check row and column
        for (int x = 0; x < SIZE; x++) {
            if (board[row][x] == num || board[x][col] == num) {
                return false;
            }
        }
        // Check 3x3 box
        int boxRow = row - row % 3;
        int boxCol = col - col % 3;
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                if (board[r + boxRow][c + boxCol] == num) {
                    return false;
                }
            }
        }
        return true;
    }

    private void displaySolution(int[][] board) {
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                cells[row][col].setText(board[row][col] == 0 ? "" : String.valueOf(board[row][col]));
                // Highlight solved cells that were empty
                if (cells[row][col].getText().trim().isEmpty()) {
                    cells[row][col].setBackground(new Color(220, 255, 220));
                }
            }
        }
    }

    // Input limiter for text fields (1 digit only)
    private class JTextFieldLimit extends PlainDocument {
        private int limit;

        JTextFieldLimit(int limit) {
            super();
            this.limit = limit;
        }

        public void insertString(int offset, String str, AttributeSet attr) throws BadLocationException {
            if (str == null) return;
            if ((getLength() + str.length()) <= limit) {
                super.insertString(offset, str.replaceAll("[^1-9]", ""), attr);
            }
        }
    }
}