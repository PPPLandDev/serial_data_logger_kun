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
import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
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
	private JTextField textField;
	private JButton sendbutton;

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
		
		textField = new JTextField();
		GridBagConstraints gbc_textField = new GridBagConstraints();
		gbc_textField.gridwidth = 4;
		gbc_textField.insets = new Insets(0, 0, 0, 5);
		gbc_textField.fill = GridBagConstraints.HORIZONTAL;
		gbc_textField.gridx = 0;
		gbc_textField.gridy = 1;
		panel.add(textField, gbc_textField);
		textField.setColumns(10);
		
		sendbutton = new JButton("送信");
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
					JComponent c = (JComponent)e.getSource();
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
		gbl_panel_2.columnWidths = new int[] { 65, 185, 0, 0 };
		gbl_panel_2.rowHeights = new int[] { 16, 0 };
		gbl_panel_2.columnWeights = new double[] { 0.0, 1.0, 0.0, Double.MIN_VALUE };
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
		GridBagConstraints gbc_button = new GridBagConstraints();
		gbc_button.gridx = 2;
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

	}

}
