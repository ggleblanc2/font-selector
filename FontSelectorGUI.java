package com.ggl.testing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

public class FontSelectorGUI implements Runnable {

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new FontSelectorGUI());
	}

	private DefaultTableModel tableModel;

	private Font[] allFonts;

	private FontTableCellRenderer renderer;

	private JComboBox<Integer> fontSizeComboBox;

	private JTable displayTable;

	private JTextField sampleTextField;
	private JTextField usefulField;

	public FontSelectorGUI() {
		this.allFonts = GraphicsEnvironment.getLocalGraphicsEnvironment()
				.getAllFonts();
	}

	@Override
	public void run() {
		JFrame frame = new JFrame("Font Selector");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		frame.add(createEntryPanel(), BorderLayout.BEFORE_FIRST_LINE);
		frame.add(createDisplayPanel(), BorderLayout.CENTER);

		frame.pack();
		frame.setLocationByPlatform(true);
		frame.setVisible(true);
	}

	private JPanel createEntryPanel() {
		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
		panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));

		JLabel sampleLabel = new JLabel("Sample Text: ");
		panel.add(sampleLabel);

		sampleTextField = new JTextField(30);
		String text = "This is a test";
		sampleTextField.setText(text);
		sampleTextField.setSelectionStart(0);
		sampleTextField.setSelectionEnd(text.length());
		panel.add(sampleTextField);

		panel.add(Box.createHorizontalStrut(10));

		JLabel fontSizeLabel = new JLabel("Font Size: ");
		panel.add(fontSizeLabel);

		Integer[] fontSizes = { 10, 12, 14, 16, 18, 24, 36, 48, 72 };
		fontSizeComboBox = new JComboBox<>(fontSizes);
		panel.add(fontSizeComboBox);

		panel.add(Box.createHorizontalStrut(10));
		
		JLabel usefulLabel = new JLabel("Number of Useful Fonts: ");
		panel.add(usefulLabel);
		
		usefulField = new JTextField(6);
		usefulField.setEditable(false);
		panel.add(usefulField);

		JButton button = new JButton("Display Fonts");
		button.addActionListener(new FontDisplayListener());
		panel.add(button);

		return panel;
	}

	private JPanel createDisplayPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

		String[] headers = { "Font Name", "Sample Text" };
		this.tableModel = new DefaultTableModel();
		for (String header : headers) {
			this.tableModel.addColumn(header);
		}

		
		displayTable = new JTable(tableModel);
		renderer = new FontTableCellRenderer(displayTable.getRowHeight());
		TableColumn column = displayTable.getColumnModel().getColumn(0);
		column.setCellRenderer(renderer);
		column = displayTable.getColumnModel().getColumn(1);
		column.setCellRenderer(renderer);

		panel.add(new JScrollPane(displayTable), BorderLayout.CENTER);
		
		return panel;
	}

	public class FontTableCellRenderer implements TableCellRenderer {
		
		private final int rowHeight;

		private final JLabel component = new JLabel();
		
		private List<Font> fonts;


		public FontTableCellRenderer(int rowHeight) {
			this.rowHeight = rowHeight;
		}

		public void setFonts(List<Font> fonts) {
			this.fonts = fonts;
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, 
				Object value, boolean isSelected, boolean hasFocus,
				int row, int column) {
			if (row >= 0 && row < fonts.size()) {
				component.setFont(fonts.get(row));
			}
			component.setText(value.toString());
			int thisRowHeight = Math.max(rowHeight, 
					component.getPreferredSize().height + 6);
			displayTable.setRowHeight(row, thisRowHeight);
			
			return component;
		}

	}

	public class FontDisplayListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent event) {
			String sampleText = sampleTextField.getText().trim();

			if (sampleText.isEmpty()) {
				return;
			}

			new Thread(new FontDisplayRunnable(sampleText)).start();
		}

	}

	public class FontDisplayRunnable implements Runnable {

		private String sampleText;

		public FontDisplayRunnable(String sampleText) {
			this.sampleText = sampleText;
		}

		@Override
		public void run() {
			int count = tableModel.getRowCount();
			for (int i = count - 1; i >= 0; i--) {
				removeRow(i);
			}

			float fontSize = (float) ((Integer) fontSizeComboBox
					.getSelectedItem());
			List<Font> usableFonts = new ArrayList<>();
			for (Font font : allFonts) {
				Font sizedFont = font.deriveFont(fontSize);
				if (sizedFont.canDisplayUpTo(sampleText) == -1) {
					usableFonts.add(sizedFont);
				}
			}
			
			usefulField(usableFonts.size());
			renderer.setFonts(usableFonts);

			for (Font font : usableFonts) {
				Object[] row = new Object[2];
				row[0] = font.getName();
				row[1] = sampleText;
				addRow(row);
			}
			
			scrollToTop();
		}
		
		private void usefulField(int count) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					usefulField.setText(Integer.toString(count));
				}
			});
		}
		
		private void removeRow(int row) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					tableModel.removeRow(row);
				}
			});
		}
		
		private void addRow(Object[] row) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					tableModel.addRow(row);
				}
			});
		}

		
		private void scrollToTop() {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					Rectangle cellRect = displayTable.getCellRect(0, 0, true);
					displayTable.scrollRectToVisible(cellRect);
				}
			});
		}
	}

}
