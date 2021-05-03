package serialdatalogger;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EmptyBorder;

import lombok.extern.slf4j.Slf4j;
import serialdatalogger.disp.MainPanel;

/**
 * シリアルデータ通信プログラム<br>
 *
 * 【更新履歴】<br>
 * <li>Rev01.00 2021-05-02 : 新規作成 			K.Nakamura<br>
 *
 */
@Slf4j
public class LoggerMainFrame extends JFrame {

	/**
	 * 状態監視リスト画面
	 */
	private static LoggerMainFrame frame;

	private JPanel contentPane;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {

		try {
			UIManager.put("control", new Color(0xf5, 0xf5, 0xf5));

			// lookand feel 設定
			for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
				if ("Nimbus".equals(info.getName())) {
					UIManager.setLookAndFeel(info.getClassName());
					UIDefaults defaults = UIManager.getLookAndFeelDefaults();
					defaults.put("defaultFont", new Font(Font.SANS_SERIF, Font.PLAIN, 12));
					defaults.put("Table.gridColor", new Color(214, 217, 223));
					defaults.put("Table.disabled", false);
					defaults.put("Table.showGrid", true);
					defaults.put("Table.intercellSpacing", new Dimension(1, 1));
					UIManager.put("Table.rowHeight", 20);//TODO
					break;
				}
			}
		} catch (Exception e) {
			String LF = UIManager.getSystemLookAndFeelClassName();
			try {
				UIManager.setLookAndFeel(LF);
			} catch (ClassNotFoundException e1) {
				e1.printStackTrace();
			} catch (InstantiationException e1) {
				e1.printStackTrace();
			} catch (IllegalAccessException e1) {
				e1.printStackTrace();
			} catch (UnsupportedLookAndFeelException e1) {
				e1.printStackTrace();
			}
		}
		//初期処理
		initialize();

		//		EventQueue.invokeLater(new Runnable() {
		//			public void run() {
		//				try {
		//					LoggerMainFrame frame = new LoggerMainFrame();
		//					frame.setVisible(true);
		//				} catch (Exception e) {
		//					e.printStackTrace();
		//				}
		//			}
		//		});
	}

	private MainPanel maincontent;

	/**
	 * Create the frame.
	 */
	public LoggerMainFrame() {
		EventQueue.invokeLater(new Runnable() {

			public void run() {
				try {
					setTitle("シリアルログ");
					setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
					//		setBounds(100, 100, 450, 300);
					setBounds(0, 0, 980, 740);
					setLocationRelativeTo(null);
					contentPane = new JPanel();
					contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
					contentPane.setLayout(new BorderLayout(0, 0));

					JScrollPane scrollPane = new JScrollPane();
					scrollPane.setViewportView(contentPane);
					maincontent = new MainPanel();
					contentPane.add(maincontent);
					setContentPane(scrollPane);
					//					setContentPane(contentPane);

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

	}

	private static void initialize() {
		log.info("-----プログラム起動----");

		//メイン画面
		frame = new LoggerMainFrame();
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.setVisible(true);
		frame.addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				String[] values = new String[] { "はい", "いいえ" };
				int ret = JOptionPane.showOptionDialog(
						frame.getRootPane(),
						"アプリケーションを終了しますか？",
						"確認",
						JOptionPane.YES_NO_OPTION,
						JOptionPane.WARNING_MESSAGE,
						null,
						values,
						values[1]);

				if (ret == JOptionPane.YES_OPTION) {
					frame.dispose();
				}

			}

			@Override
			public void windowClosed(WindowEvent e) {
				// 画面クローズ
				shutdown();
			}

		});
		log.info("----プログラム初期化完了----");

	}

	/**
	 * シャットダウン処理を行います。
	 */
	public static void shutdown() {
		log.info("----プログラム終了----");
		System.exit(0);
	}

}
