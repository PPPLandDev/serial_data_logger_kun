package serialdatalogger.disp;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.plaf.basic.BasicFileChooserUI;
import javax.swing.text.DefaultEditorKit;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MainPanel extends JPanel {

	private List<String> comboitem;
	private boolean portFound;
	private JComboBox<String> comboBox;
	private JComboBox<Integer> comboBox_1;
	Integer[] baudRateOptions = { 9600, 14400, 19200, 28800, 38400, 57600, 115200 };
	SerialPort serialPort;
	private JButton connect_button;
	private JButton close_button;
	private JTextArea tx;
	private JButton updatebutton;
	private JTextPane textPane;
	private JButton button;
	private JTextField sendtextField;
	private JButton sendbutton;
	private JButton button_1;

	enum UIState {
		PORT_OPEN, PORT_CLOSE
	}

	/**
	 * Create the panel.
	 */
	public MainPanel() {

		setLayout(new BorderLayout(0, 0));
		setPreferredSize(new Dimension(587, 484));

		JPanel panel = new JPanel();
		add(panel, BorderLayout.NORTH);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[] { 0, 0, 102, 0, 0, 0 };
		gbl_panel.rowHeights = new int[] { 0, 0, 0 };
		gbl_panel.columnWeights = new double[] { 0.0, 1.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		gbl_panel.rowWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
		panel.setLayout(gbl_panel);

		updatebutton = new JButton("更新");
		updatebutton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				initialize();
			}
		});
		GridBagConstraints gbc_updatebutton = new GridBagConstraints();
		gbc_updatebutton.insets = new Insets(0, 0, 5, 5);
		gbc_updatebutton.gridx = 0;
		gbc_updatebutton.gridy = 0;
		panel.add(updatebutton, gbc_updatebutton);

		comboBox = new JComboBox<String>();
		GridBagConstraints gbc_comboBox = new GridBagConstraints();
		gbc_comboBox.insets = new Insets(0, 0, 5, 5);
		gbc_comboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboBox.gridx = 1;
		gbc_comboBox.gridy = 0;
		panel.add(comboBox, gbc_comboBox);

		comboBox_1 = new JComboBox<Integer>();
		for (Integer baud : baudRateOptions) {
			comboBox_1.addItem(baud);
		}
		GridBagConstraints gbc_comboBox_1 = new GridBagConstraints();
		gbc_comboBox_1.insets = new Insets(0, 0, 5, 5);
		gbc_comboBox_1.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboBox_1.gridx = 2;
		gbc_comboBox_1.gridy = 0;
		panel.add(comboBox_1, gbc_comboBox_1);

		connect_button = new JButton("接続");
		connect_button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!portFound || serialPort != null) {
					return;
				}

				// ポート名
				String portName = (String) comboBox.getSelectedItem();
				System.out.println(portName);
				for (SerialPort sp : SerialPort.getCommPorts()) {
					if (sp.getSystemPortName().equals(portName)) {
						serialPort = sp;
						break;
					}
				}

				// ボーレート
				Integer baudRate = (Integer) comboBox_1.getSelectedItem();
				System.out.println(baudRate);
				serialPort.setBaudRate(baudRate);
				System.out.println("Opening " + serialPort.getSystemPortName() + " Baud Rage " + baudRate);

				serialPort.addDataListener(new SerialPortDataListener() {

					public int getListeningEvents() {
						return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
					}

					public void serialEvent(SerialPortEvent event) {
						try {
							int evt = event.getEventType();

							System.out.println("Event " + evt + " received");

							if (evt == SerialPort.LISTENING_EVENT_DATA_AVAILABLE) {

								int bytesToRead = serialPort.bytesAvailable();
								if (bytesToRead == -1) {
									System.out.println("-1 means port is closed.");
									closeSerialPort();
									updateComobobox();
									return;
								}
								System.out.println(bytesToRead + " byte(s) available.");
								byte[] newData = new byte[bytesToRead];
								serialPort.readBytes(newData, bytesToRead);
								String s = new String(newData, "UTF8");
								System.out.println("Received [" + s + "]");
								tx.append(s);

							}

						} catch (Exception ex) {
							System.err.println(ex.getMessage());
							closeSerialPort();
						}
					}
				});
				if (!serialPort.openPort()) {
					JOptionPane.showMessageDialog(null, "Unable to open the port.", "Error", JOptionPane.ERROR_MESSAGE,
							null);
					return;
				}

				updateUI(UIState.PORT_OPEN);
			}

		});
		GridBagConstraints gbc_connect_button = new GridBagConstraints();
		gbc_connect_button.insets = new Insets(0, 0, 5, 5);
		gbc_connect_button.gridx = 3;
		gbc_connect_button.gridy = 0;
		panel.add(connect_button, gbc_connect_button);

		close_button = new JButton("切断");
		close_button.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				closeSerialPort();
			}
		});

		GridBagConstraints gbc_close_button = new GridBagConstraints();
		gbc_close_button.insets = new Insets(0, 0, 5, 0);
		gbc_close_button.gridx = 4;
		gbc_close_button.gridy = 0;
		panel.add(close_button, gbc_close_button);

		sendtextField = new JTextField();
		GridBagConstraints gbc_sendtextField = new GridBagConstraints();
		gbc_sendtextField.gridwidth = 4;
		gbc_sendtextField.insets = new Insets(0, 0, 0, 5);
		gbc_sendtextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_sendtextField.gridx = 0;
		gbc_sendtextField.gridy = 1;
		panel.add(sendtextField, gbc_sendtextField);
		sendtextField.setColumns(10);

		sendbutton = new JButton("送信");
		sendbutton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					String text = sendtextField.getText();
					byte[] buff = text.getBytes();
					sendByte(buff);
				} catch (Exception ex) {
					ex.printStackTrace();
					log.error("", ex);
				}

			}
		});
		GridBagConstraints gbc_sendbutton = new GridBagConstraints();
		gbc_sendbutton.gridx = 4;
		gbc_sendbutton.gridy = 1;
		panel.add(sendbutton, gbc_sendbutton);

		JPanel panel_1 = new JPanel();

		add(panel_1, BorderLayout.CENTER);
		GridBagLayout gbl_panel_1 = new GridBagLayout();
		gbl_panel_1.columnWidths = new int[] { 0, 0 };
		gbl_panel_1.rowHeights = new int[] { 0, 0 };
		gbl_panel_1.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_panel_1.rowWeights = new double[] { 1.0, Double.MIN_VALUE };
		panel_1.setLayout(gbl_panel_1);

		//テキストエリアを作成
		tx = new JTextArea();
		tx.addMouseListener(new MouseListener() {
			private void mousePopup(MouseEvent e) {
				if (e.isPopupTrigger()) {
					// ポップアップメニューを表示する
					JComponent c = (JComponent) e.getSource();
					showPopup(c, e.getX(), e.getY());
					e.consume();
				}
			}

			public void mousePressed(MouseEvent e) {
				mousePopup(e);
			}

			public void mouseReleased(MouseEvent e) {
				mousePopup(e);
			}

			public void mouseClicked(MouseEvent e) {
				// TODO 自動生成されたメソッド・スタブ

			}

			public void mouseEntered(MouseEvent e) {
				// TODO 自動生成されたメソッド・スタブ

			}

			public void mouseExited(MouseEvent e) {
				// TODO 自動生成されたメソッド・スタブ

			}

			protected void showPopup(JComponent c, int x, int y) {
				JPopupMenu pmenu = new JPopupMenu();

				ActionMap am = c.getActionMap();

				Action cut = am.get(DefaultEditorKit.cutAction);
				addMenu(pmenu, "切り取り(X)", cut, 'X', KeyStroke.getKeyStroke(KeyEvent.VK_X, KeyEvent.CTRL_DOWN_MASK));

				Action copy = am.get(DefaultEditorKit.copyAction);
				addMenu(pmenu, "コピー(C)", copy, 'C', KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_DOWN_MASK));

				Action paste = am.get(DefaultEditorKit.pasteAction);
				addMenu(pmenu, "貼り付け(V)", paste, 'V', KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.CTRL_DOWN_MASK));

				Action all = am.get(DefaultEditorKit.selectAllAction);
				addMenu(pmenu, "すべて選択(A)", all, 'A', KeyStroke.getKeyStroke(KeyEvent.VK_A, KeyEvent.CTRL_DOWN_MASK));

				pmenu.show(c, x, y);
			}

			protected void addMenu(JPopupMenu pmenu, String text, Action action, int mnemonic, KeyStroke ks) {
				if (action != null) {
					JMenuItem mi = pmenu.add(action);
					if (text != null) {
						mi.setText(text);
					}
					if (mnemonic != 0) {
						mi.setMnemonic(mnemonic);
					}
					if (ks != null) {
						mi.setAccelerator(ks);
					}
				}
			}
		});

		//まず、テキストエリアの文字列折り返しを有効にする。
		tx.setLineWrap(true);

		//スクロール・ペインを作成。
		JScrollPane scrollPane = new JScrollPane(tx);

		//スクロール・バーは、垂直方向だけを使用します。
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

		//スクロール・ペインの大きさを設定
		scrollPane.setBounds(50, 50, 400, 300);

		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 0;
		panel_1.add(scrollPane, gbc_scrollPane);

		JPanel panel_2 = new JPanel();
		add(panel_2, BorderLayout.SOUTH);
		GridBagLayout gbl_panel_2 = new GridBagLayout();
		gbl_panel_2.columnWidths = new int[] { 65, 185, 0, 0, 0 };
		gbl_panel_2.rowHeights = new int[] { 16, 0 };
		gbl_panel_2.columnWeights = new double[] { 0.0, 1.0, 0.0, 0.0, Double.MIN_VALUE };
		gbl_panel_2.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
		panel_2.setLayout(gbl_panel_2);

		JLabel lblTest = new JLabel("ステータス");
		GridBagConstraints gbc_lblTest = new GridBagConstraints();
		gbc_lblTest.anchor = GridBagConstraints.WEST;
		gbc_lblTest.insets = new Insets(0, 0, 0, 5);
		gbc_lblTest.gridx = 0;
		gbc_lblTest.gridy = 0;
		panel_2.add(lblTest, gbc_lblTest);

		textPane = new JTextPane();
		textPane.setText("-");
		GridBagConstraints gbc_textPane = new GridBagConstraints();
		gbc_textPane.insets = new Insets(0, 0, 0, 5);
		gbc_textPane.anchor = GridBagConstraints.WEST;
		gbc_textPane.gridx = 1;
		gbc_textPane.gridy = 0;
		panel_2.add(textPane, gbc_textPane);

		button = new JButton("クリア");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				tx.setText("");
			}
		});

		button_1 = new JButton("テキスト内容保存");
		button_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				outputFile();
			}
		});
		GridBagConstraints gbc_button_1 = new GridBagConstraints();
		gbc_button_1.insets = new Insets(0, 0, 0, 5);
		gbc_button_1.gridx = 2;
		gbc_button_1.gridy = 0;
		panel_2.add(button_1, gbc_button_1);
		GridBagConstraints gbc_button = new GridBagConstraints();
		gbc_button.gridx = 3;
		gbc_button.gridy = 0;
		panel_2.add(button, gbc_button);

		//		log.debug("ログ出力テスト");
		//		log.info("ログ出力テスト");
		//		log.warn("ログ出力テスト");
		//		log.error("ログ出力テスト");


		initialize();

	}

	public void initialize() {

		portFound = false;
		comboBox.removeAllItems();
		updateComobobox();
		updateUI(UIState.PORT_CLOSE);
	}

	void closeSerialPort() {

		if (serialPort != null) {
			if (serialPort.isOpen()) {
				serialPort.closePort();
			}
			serialPort = null;
		}

		updateUI(UIState.PORT_CLOSE);
	}

	void sendByte(byte b) {
		byte[] buff = new byte[1];
		buff[0] = b;
		serialPort.writeBytes(buff, 1);
	}

	void sendByte(byte[] buff) {
		serialPort.writeBytes(buff, buff.length);
	}

	public void updateComobobox() {

		SerialPort[] serialPorts = SerialPort.getCommPorts();
		comboitem = new ArrayList<String>();
		for (SerialPort serialPort : serialPorts) {
			portFound = true;
			comboitem.add(serialPort.getSystemPortName());
			comboBox.addItem(serialPort.getSystemPortName());
		}

	}

	void updateUI(UIState uiState) {
		boolean isOpen = (uiState == UIState.PORT_OPEN);
		textPane.setText(uiState.toString());

		close_button.setEnabled(isOpen);
		connect_button.setEnabled(!isOpen);
		updatebutton.setEnabled(!isOpen);

		comboBox.setEnabled(!isOpen);
		comboBox_1.setEnabled(!isOpen);

		sendbutton.setEnabled(isOpen);
		sendtextField.setEnabled(isOpen);

	}

	/**
	 * ファイル選択
	 */
	private JFileChooser chooser = new JFileChooser();

	/**
	 * CSV出力を実行します
	 * @return
	 */
	public void outputFile() {

		BasicFileChooserUI ui = (BasicFileChooserUI) chooser.getUI();

		// 初期ファイル名作成
		String name = "output";
		ui.setFileName(name + ".txt");

		int returnVal = chooser.showSaveDialog(getRootPane());

		try {

			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = chooser.getSelectedFile();
				if (output(file.getAbsolutePath())) {
					JOptionPane.showMessageDialog(
							getRootPane(),
							"ファイル出力が完了しました。");
				} else {
					JOptionPane.showMessageDialog(
							getRootPane(),
							"ファイル出力に失敗しました。", "CSV出力エラー", JOptionPane.ERROR_MESSAGE);
				}
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	/**
	 * 出力処理
	 * @return
	 */
	private boolean output(String pathname) {

		BufferedWriter bw = null;

		/**書き込み処理***/

		try {

			bw = new BufferedWriter(new FileWriter(new File(pathname)));
			//			bw.write(",");
			//			//改行
			//			bw.newLine();
			bw.write(tx.getText());

			bw.flush();
			bw.close();

		} catch (IOException e) {

			log.debug("", e);

		} finally {
			if (bw != null) {
				try {
					bw.close();
				} catch (IOException e) {
					log.debug("", e);
				}
			}
		}

		return true;
	}

}
