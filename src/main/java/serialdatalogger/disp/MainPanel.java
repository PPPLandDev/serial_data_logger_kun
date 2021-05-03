package serialdatalogger.disp;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;

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

	enum UIState {
		PORT_OPEN, PORT_CLOSE
	}

	/**
	 * Create the panel.
	 */
	public MainPanel() {

		setLayout(new BorderLayout(0, 0));
		setPreferredSize(new Dimension(500, 400));

		JPanel panel = new JPanel();
		add(panel, BorderLayout.NORTH);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[] { 0, 0, 102, 0, 0, 0 };
		gbl_panel.rowHeights = new int[] { 0, 0, 0 };
		gbl_panel.columnWeights = new double[] { 0.0, 1.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		gbl_panel.rowWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
		panel.setLayout(gbl_panel);

		JButton button_1 = new JButton("更新");
		button_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				initialize();
			}
		});
		GridBagConstraints gbc_button_1 = new GridBagConstraints();
		gbc_button_1.insets = new Insets(0, 0, 5, 5);
		gbc_button_1.gridx = 0;
		gbc_button_1.gridy = 0;
		panel.add(button_1, gbc_button_1);

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
								

							}

						} catch (Exception ex) {
							System.err.println(ex.getMessage());
							closeSerialPort();
						}
					}
				});
				if (!serialPort.openPort()) {
					JOptionPane.showMessageDialog(null, "Unable to open the port.", "Error", JOptionPane.ERROR_MESSAGE,null);
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

			}
		});

		GridBagConstraints gbc_close_button = new GridBagConstraints();
		gbc_close_button.insets = new Insets(0, 0, 5, 0);
		gbc_close_button.gridx = 4;
		gbc_close_button.gridy = 0;
		panel.add(close_button, gbc_close_button);

		JPanel panel_1 = new JPanel();

		add(panel_1, BorderLayout.CENTER);
		GridBagLayout gbl_panel_1 = new GridBagLayout();
		gbl_panel_1.columnWidths = new int[] { 0, 0 };
		gbl_panel_1.rowHeights = new int[] { 0, 0 };
		gbl_panel_1.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_panel_1.rowWeights = new double[] { 1.0, Double.MIN_VALUE };
		panel_1.setLayout(gbl_panel_1);

		//テキストエリアを作成
		JTextArea tx = new JTextArea();

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
		gbc_lblTest.anchor = GridBagConstraints.NORTHWEST;
		gbc_lblTest.insets = new Insets(0, 0, 0, 5);
		gbc_lblTest.gridx = 0;
		gbc_lblTest.gridy = 0;
		panel_2.add(lblTest, gbc_lblTest);

		JTextPane textPane = new JTextPane();
		textPane.setText("-");
		GridBagConstraints gbc_textPane = new GridBagConstraints();
		gbc_textPane.insets = new Insets(0, 0, 0, 5);
		gbc_textPane.anchor = GridBagConstraints.NORTHWEST;
		gbc_textPane.gridx = 1;
		gbc_textPane.gridy = 0;
		panel_2.add(textPane, gbc_textPane);

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

		close_button.setEnabled(isOpen);
		connect_button.setEnabled(!isOpen);

		//	    closeButton.setDisable(!isOpen);
		//	    openButton.setDisable(isOpen);
		//
		//	    portChoiceBox.setDisable(isOpen);
		//	    baudChoiceBox.setDisable(isOpen);
		//	    refreshButton.setDisable(isOpen);
	}

}
