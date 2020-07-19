import java.awt.AWTException;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.Font;
import java.awt.Image;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

class fehKongChengSongWenNuanXiaoZhuShou extends fehKongChengDingShiNaoZhong {// 可调用fehKongChengDingShiNaoZhong中的方法
	static int multiple = 0;// 根据用户选择分钟还是小时为单位而变化

	static ArrayList<Integer> inputs = new ArrayList<Integer>();// 输入的时间的列表
	static ArrayList<Integer> inputsMultiple = new ArrayList<Integer>();// 输入的倍数的列表
	static ArrayList<Long> confirmTime = new ArrayList<Long>();// 输入时的时间的列表
	static ArrayList<Long> leftLimits = new ArrayList<Long>();// 时间下限的列表
	static ArrayList<Long> rightLimits = new ArrayList<Long>();// 时间上限的列表

	private static fehKongChengDingShiNaoZhong clock;

	private static boolean isInteger(String str) {// 判断字符串是否为整数
		if (str.length() == 0) {// 如果输入为空则返回假
			return false;
		} else {
			char[] chars = str.toCharArray();
			int charIndex;
			for (char aChar : chars) {// 依次判断字符串的每一个字符是否为半角阿拉伯数字字符
				charIndex = Integer.valueOf(aChar);
				if(charIndex < 48 || charIndex > 57) {
					return false;
				}
			}
			return true;
		}
	}

	private static void timeSet() {
		final int formWidth = 675;
		final int formHeight = 350;

		Map<String, String> map = System.getenv();
		JFrame f = new JFrame("你好，" + map.get("USERNAME") + "～");// 获取计算机用户名
		f.setLayout(null);
		f.setSize(formWidth, formHeight);

		try {// 设置窗口图标
			Image windowIconImage = ImageIO.read(f.getClass().getResource("/img/icon/top.jpg"));
			f.setIconImage(windowIconImage);
		} catch (IOException e) {
			e.printStackTrace();
		}

		JPanel imgPanel=(JPanel) f.getContentPane();//内容面板必须强转为JPanel才可以实现下面的设置透明
		imgPanel.setOpaque(false);//将内容面板设为透明
		ImageIcon backgroundImage = new ImageIcon(f.getClass().getResource("/img/background/Bg_SkyCastle_04.png"));
		JLabel backgroundImageL = new JLabel(backgroundImage);
		backgroundImageL.setBounds(-50, -300, backgroundImage.getIconWidth(), backgroundImage.getIconHeight());
		f.getLayeredPane().add(backgroundImageL, Integer.valueOf(Integer.MIN_VALUE));//标签添加到层面板
		ImageIcon portraitImage = null;
		if (inputs.size() == 0) {// 根据是否存在记录而变更立绘
			portraitImage = new ImageIcon(f.getClass().getResource("/img/portrait/67345_1.png"));
		} else {
			portraitImage = new ImageIcon(f.getClass().getResource("/img/portrait/67345_0.png"));
		}
		JLabel portraitImageL = new JLabel(portraitImage);
		portraitImageL.setBounds(450, 90, portraitImage.getIconWidth(), portraitImage.getIconHeight());
		f.getLayeredPane().add(portraitImageL, Integer.valueOf(Integer.MIN_VALUE) + 1);

		JLabel tip = new JLabel("请输入此时显示的本次有效防卫发生的时间：");
		tip.setFont(new Font("宋体", Font.PLAIN, 30));
		tip.setBounds(30, 30, 600, 40);
		tip.setForeground(Color.WHITE);
		f.add(tip);

		JTextField timeValue = new JTextField();
		timeValue.setFont(new Font("宋体", Font.PLAIN, 30));
		timeValue.setBounds(148, 100, 35, 40);
		f.add(timeValue);

		String[] listData = new String[] { "（未选择）", "分钟前", "小时前" };// 设置下拉列表
		JComboBox<String> multipleI = new JComboBox<String>(listData);
		multipleI.setFont(new Font("宋体", Font.PLAIN, 30));
		multipleI.setBounds(198, 100, 180, 40);
		multipleI.setSelectedIndex(0);
		f.add(multipleI);

		JButton confirm = new JButton("确定");
		confirm.setFont(new Font("宋体", Font.PLAIN, 30));
		confirm.setBounds(198, 170, 130, 40);
		// confirm.setIcon(portraitImage);
		// confirm.setOpaque(false);
		// confirm.setBackground(null);
		confirm.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				try {
					String time = new String();
					time = timeValue.getText();// 获取文本框中用户输入的字符串
					if (time.length() >= 1 && time.length() <= 2) {// 若用户输入的时间超过2字符或未输入则提示错误。
						if (isInteger(time)) {// 若用户输入的不是整数字符串则提示错误。
							if (Integer.valueOf(time) >= 1 && Integer.valueOf(time) <= 59) {// 若用户输入的数字超过59则提示错误。
								Date thisMoment = new Date();
								long deadline = thisMoment.getTime() + (20 * 3600 * 1000 - (Integer.valueOf(time) + 1) * multiple);// 最后期限下限（20小时-用户输入的时间+现在的时间）
								leftLimits.add(deadline);
								if (multiple == 60000) {// 最后期限上限（下限+（1小时或1分钟，根据用户输入的单位变化））
									rightLimits.add(deadline + 60 * 1000);
								} else {
									rightLimits.add(deadline + 60 * 60 * 1000);
								}
								if (Collections.max(leftLimits) <= Collections.min(rightLimits)) {
									if (Integer.valueOf(time) * multiple < 72000000) {// 若用户输入大于等于20小时前则提示保护期已经结束。
										inputs.add(Integer.valueOf(time));
										inputsMultiple.add(multiple);
										confirmTime.add(thisMoment.getTime());
										f.dispose();
										timeShow();
									} else {
										leftLimits.remove(leftLimits.size() - 1);
										rightLimits.remove(rightLimits.size() - 1);
										JOptionPane.showMessageDialog(null, "你已经不在保护期了！");
									}
								} else {
									leftLimits.remove(leftLimits.size() - 1);
									rightLimits.remove(rightLimits.size() - 1);
									JOptionPane.showMessageDialog(null, "请正确输入时间（当前输入的时间和已有的时间记录存在冲突）。");
								}
							} else {
								JOptionPane.showMessageDialog(null, "请正确输入时间（输入的数字不应超过59或小于1）。");
							}
						} else {
							JOptionPane.showMessageDialog(null, "请正确输入时间（输入应为半角阿拉伯数字）。");
						}
					} else {
						JOptionPane.showMessageDialog(null, "请正确输入时间（输入不能为空且不能超过两字符）。");
					}
				} catch (Exception e) {
					JOptionPane.showMessageDialog(null, "请正确输入时间。");
				}
			}
		});

		JButton back = new JButton("返回");
		back.setFont(new Font("宋体", Font.PLAIN, 30));
		back.setBounds(198, 230, 130, 40);
		back.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				f.dispose();
				timeShow();
			}
		});
		if (inputs.size() != 0)
			f.add(back);

		multipleI.addItemListener(new ItemListener() {// 如果用户未选择时间单位，则不显示确定按钮
			public void itemStateChanged(ItemEvent event) {
				if (event.getStateChange() == ItemEvent.SELECTED) {
					if (multipleI.getSelectedItem().toString().equals("分钟前")) {
						multiple = 1000 * 60;
						f.add(confirm);
					} else if (multipleI.getSelectedItem().toString().equals("小时前")) {
						multiple = 1000 * 60 * 60;
						f.add(confirm);
					} else {
						f.remove(confirm);
					}
				}
			}
		});

		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setResizable(false);
		f.setLocation((screenWidth - formWidth) / 2, (screenHeight - formHeight) / 2);
		f.setVisible(true);
	}

	private static void timeShow() {
		final int formWidth = 870;
		final int formHeight = 760;

		JFrame f = new JFrame("信息确认");
		f.setLayout(null);
		f.setSize(formWidth, formHeight);

		try {// 设置窗口图标
			Image windowIconImage = ImageIO.read(f.getClass().getResource("/img/icon/Milas_Turnwheel.png"));
			f.setIconImage(windowIconImage);
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		JPanel imgPanel=(JPanel) f.getContentPane();
		imgPanel.setOpaque(false);
		ImageIcon backgroundImage = new ImageIcon(f.getClass().getResource("/img/background/116290.png"));
		JLabel backgroundImageL1 = new JLabel(backgroundImage);
		backgroundImageL1.setBounds(-600, -60, backgroundImage.getIconWidth(), backgroundImage.getIconHeight());
		f.getLayeredPane().add(backgroundImageL1, Integer.valueOf(Integer.MIN_VALUE), 0);
		JLabel backgroundImageL2 = new JLabel(backgroundImage);
		backgroundImageL2.setBounds(-2648, -60, backgroundImage.getIconWidth(), backgroundImage.getIconHeight());
		f.getLayeredPane().add(backgroundImageL2, Integer.valueOf(Integer.MIN_VALUE) + 1, 0);
		JLabel backgroundImageL3 = new JLabel(backgroundImage);
		backgroundImageL3.setBounds(-600, -960, backgroundImage.getIconWidth(), backgroundImage.getIconHeight());
		f.getLayeredPane().add(backgroundImageL3, Integer.valueOf(Integer.MIN_VALUE) + 2, 0);

		JPanel inputsArea = new JPanel();
		inputsArea.setLayout(null);
		JLabel[] inputsL = new JLabel[inputs.size()];// 根据输入的记录的数量创建标签和按钮数组
		JLabel[] confirmTimeL = new JLabel[inputs.size()];
		JButton[] delete = new JButton[inputs.size()];
		for (int i = 0; i < inputs.size(); i++) {
			delete[i] = new JButton("删除");
			delete[i].setName(Integer.toString(i));
			delete[i].setFont(new Font("宋体", Font.PLAIN, 30));
			delete[i].setBounds(10, 10 + 70 * (i), 100, 50);
			delete[i].addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent event) {
					int i = Integer.valueOf(event.getSource().toString().substring(event.getSource().toString().indexOf("[") + 1, event.getSource().toString().indexOf(",")));
					inputs.remove(i);
					inputsMultiple.remove(i);
					confirmTime.remove(i);
					leftLimits.remove(i);
					rightLimits.remove(i);
					f.dispose();
					if (inputs.size() == 0) {// 如果所有记录都被删除，则打开timeSet()菜单
						timeSet();
					} else {
						timeShow();
					}
				}
			});
			inputsArea.add(delete[i]);

			String multipleStr = new String();
			if (inputsMultiple.get(i) == 60000) {
				multipleStr = "分钟前";
			} else {
				multipleStr = "小时前";
			}
			inputsL[i] = new JLabel("【" + inputs.get(i).toString() + multipleStr + "】");
			inputsL[i].setFont(new Font("宋体", Font.PLAIN, 30));
			inputsL[i].setBounds(140, 10 + 70 * i, 500, 50);
			inputsArea.add(inputsL[i]);

			confirmTimeL[i] = new JLabel("记录于" + df.format(new Date(confirmTime.get(i))));
			confirmTimeL[i].setFont(new Font("宋体", Font.PLAIN, 30));
			confirmTimeL[i].setBounds(390, 10 + 70 * i, 500, 50);
			inputsArea.add(confirmTimeL[i]);
		}
		inputsArea.setPreferredSize(new Dimension(640, 70 * inputs.size()));// 根据输入的记录的数量设定总尺寸
		JScrollPane qscroll = new JScrollPane(inputsArea, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		qscroll.setBounds(30, 30, 800, 100);// 设定卷轴最大显示范围的尺寸
		f.add(qscroll);

		JLabel tip1 = new JLabel("状态“评价下降减少中”将于");
		tip1.setFont(new Font("宋体", Font.PLAIN, 30));
		tip1.setBounds(40, 220, 500, 40);
		tip1.setForeground(Color.WHITE);
		f.add(tip1);
		JLabel tip2 = new JLabel(df.format(new Date(Collections.max(leftLimits))));
		tip2.setFont(new Font("宋体", Font.PLAIN, 60));
		tip2.setBounds(150, 280, 600, 80);
		tip2.setForeground(Color.WHITE);
		f.add(tip2);
		JLabel tip3 = new JLabel("至");
		tip3.setFont(new Font("宋体", Font.PLAIN, 30));
		tip3.setBounds(420, 380, 400, 40);
		tip3.setForeground(Color.WHITE);
		f.add(tip3);
		JLabel tip4 = new JLabel(df.format(new Date(Collections.min(rightLimits))));
		tip4.setFont(new Font("宋体", Font.PLAIN, 60));
		tip4.setBounds(150, 440, 600, 80);
		tip4.setForeground(Color.WHITE);
		f.add(tip4);
		JLabel tip5 = new JLabel("间的某一时刻结束。");
		tip5.setFont(new Font("宋体", Font.PLAIN, 30));
		tip5.setBounds(560, 540, 400, 40);
		tip5.setForeground(Color.WHITE);
		f.add(tip5);

		JButton update = new JButton("添加记录");
		update.setFont(new Font("宋体", Font.PLAIN, 30));
		update.setBounds(40, 150, 780, 50);
		update.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				f.dispose();
				timeSet();
			}
		});
		f.add(update);

		JButton confirm = new JButton("设定定时提醒");
		confirm.setFont(new Font("宋体", Font.PLAIN, 30));
		confirm.setBounds(40, 620, 780, 50);
		confirm.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				f.dispose();
				setAlarm();
			}
		});
		f.add(confirm);

		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setResizable(false);
		f.setLocation((screenWidth - formWidth) / 2, (screenHeight - formHeight) / 2);
		f.setVisible(true);
	}

	private static String calendarStr(int year, int month, int day, int hour, int minute, int second) {
		String yearStr = Integer.toString(year);
		String monthStr = Integer.toString(month);
		String dayStr = Integer.toString(day);
		String hourStr = Integer.toString(hour);
		String minuteStr = Integer.toString(minute);
		String secondStr = Integer.toString(second);
		if (yearStr.length() == 1) {
			yearStr = "000" + yearStr;
		} else if (yearStr.length() == 2) {
			yearStr = "00" + yearStr;
		} else if (yearStr.length() == 3) {
			yearStr = "0" + yearStr;
		}
		if (monthStr.length() == 1) {
			monthStr = "0" + monthStr;
		}
		if (dayStr.length() == 1) {
			dayStr = "0" + dayStr;
		}
		if (hourStr.length() == 1) {
			hourStr = "0" + hourStr;
		}
		if (minuteStr.length() == 1) {
			minuteStr = "0" + minuteStr;
		}
		if (secondStr.length() == 1) {
			secondStr = "0" + secondStr;
		}
		return yearStr + '-' + monthStr + '-' + dayStr + ' ' + hourStr + ':' + minuteStr + ':' + secondStr;
	}

	private static boolean dateLegal(int year, int month, int day) {
		if (0 <= year && year <= 9999) {
			if (month == 1 || month == 3 || month == 5 || month == 7 || month == 8 || month == 10 || month == 12) {
				if (1 <= day && day <= 31) {
					return true;
				}
			} else if (month == 4 || month == 6 || month == 9 || month == 11) {
				if (1 <= day && day <= 30) {
					return true;
				}
			} else if (month == 2) {
				if (year % 4 == 0) {
					if (1 <= day && day <= 29) {
						return true;
					}
				} else {
					if (1 <= day && day <= 28) {
						return true;
					}
				}
			}
		}
		return false;
	}

	private static boolean timeLegal(int hour, int minute, int second) {
		if (0 <= hour && hour <= 23) {
			if (0 <= minute && minute <= 59) {
				if (0 <= second && second <= 59) {
					return true;
				}
			}
		}
		return false;
	}

	private static boolean notMonkey(int year, int month, int day, int hour, int minute, int second) {
		try {
			long targetTimeLong = df.parse(calendarStr(year, month, day, hour, minute, second)).getTime();
			Date thisMoment = new Date();
			long thisMomentLong = thisMoment.getTime();
			if (dateLegal(year, month, day) && timeLegal(hour, minute, second)) {
				if (thisMomentLong < targetTimeLong) {
					return true;
				}
			}
			return false;
		} catch (Exception e) {
			return false;
		}
	}

	private static void setAlarm() {
		final int formWidth = 730;
		final int formHeight = 510;

		JFrame f = new JFrame("设定定时提醒");
		f.setLayout(null);
		f.setSize(formWidth, formHeight);

		try {// 设置窗口图标
			Image windowIconImage = ImageIO.read(f.getClass().getResource("/img/icon/Milas_Turnwheel.png"));
			f.setIconImage(windowIconImage);
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		JPanel imgPanel=(JPanel) f.getContentPane();
		imgPanel.setOpaque(false);
		ImageIcon backgroundImage = new ImageIcon(f.getClass().getResource("/img/background/116290.png"));
		ImageIcon backgroundImage2 = new ImageIcon(f.getClass().getResource("/img/background/Bg_SkyCastle_03.png"));
		JLabel backgroundImageL1 = new JLabel(backgroundImage2);
		backgroundImageL1.setBounds(0, 0, backgroundImage2.getIconWidth(), backgroundImage2.getIconHeight());
		f.getLayeredPane().add(backgroundImageL1, Integer.valueOf(Integer.MIN_VALUE), 0);
		JLabel backgroundImageL2 = new JLabel(backgroundImage);
		backgroundImageL2.setBounds(-2718, -100, backgroundImage.getIconWidth(), backgroundImage.getIconHeight());
		f.getLayeredPane().add(backgroundImageL2, Integer.valueOf(Integer.MIN_VALUE) + 1, 0);
		JLabel backgroundImageL3 = new JLabel(backgroundImage);
		backgroundImageL3.setBounds(-670, -1000, backgroundImage.getIconWidth(), backgroundImage.getIconHeight());
		f.getLayeredPane().add(backgroundImageL3, Integer.valueOf(Integer.MIN_VALUE) + 2, 0);

		JLabel connectors = new JLabel("    -  -     :  :");
		connectors.setFont(new Font("宋体", Font.PLAIN, 60));
		connectors.setBounds(70, 40, 600, 80);
		connectors.setForeground(Color.WHITE);
		f.add(connectors);
		JTextField yearL = new JTextField(df.format(new Date(Collections.max(leftLimits))).substring(0, 4));
		yearL.setFont(new Font("宋体", Font.PLAIN, 60));
		yearL.setBounds(68, 40, 125, 80);
		yearL.setHorizontalAlignment(JTextField.RIGHT);
		f.add(yearL);
		JTextField monthL = new JTextField(df.format(new Date(Collections.max(leftLimits))).substring(5, 7));
		monthL.setFont(new Font("宋体", Font.PLAIN, 60));
		monthL.setBounds(218, 40, 65, 80);
		monthL.setHorizontalAlignment(JTextField.RIGHT);
		f.add(monthL);
		JTextField dayL = new JTextField(df.format(new Date(Collections.max(leftLimits))).substring(8, 10));
		dayL.setFont(new Font("宋体", Font.PLAIN, 60));
		dayL.setBounds(308, 40, 65, 80);
		dayL.setHorizontalAlignment(JTextField.RIGHT);
		f.add(dayL);
		JTextField hourL = new JTextField(df.format(new Date(Collections.max(leftLimits))).substring(11, 13));
		hourL.setFont(new Font("宋体", Font.PLAIN, 60));
		hourL.setBounds(398, 40, 65, 80);
		hourL.setHorizontalAlignment(JTextField.RIGHT);
		f.add(hourL);
		JTextField minuteL = new JTextField(df.format(new Date(Collections.max(leftLimits))).substring(14, 16));
		minuteL.setFont(new Font("宋体", Font.PLAIN, 60));
		minuteL.setBounds(488, 40, 65, 80);
		minuteL.setHorizontalAlignment(JTextField.RIGHT);
		f.add(minuteL);
		JTextField secondL = new JTextField(df.format(new Date(Collections.max(leftLimits))).substring(17, 19));
		secondL.setFont(new Font("宋体", Font.PLAIN, 60));
		secondL.setBounds(578, 40, 65, 80);
		secondL.setHorizontalAlignment(JTextField.RIGHT);
		f.add(secondL);

		JLabel messageL = new JLabel("给未来的自己捎一段话：");
		messageL.setFont(new Font("宋体", Font.PLAIN, 30));
		messageL.setBounds(40, 160, 340, 40);
		messageL.setForeground(Color.WHITE);
		f.add(messageL);

		JRadioButton messageRB = new JRadioButton("启用语音提醒");
		messageRB.setFont(new Font("宋体", Font.PLAIN, 30));
		messageRB.setBounds(470, 160, 240, 40);
		messageRB.setSelected(voiceAlarm);
		messageRB.setForeground(Color.WHITE);
		messageRB.setOpaque(false);
		messageRB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if (messageRB.isSelected()) {
					voiceAlarm = true;
				} else {
					voiceAlarm = false;
				}
			}
		});
		f.add(messageRB);

		JTextField messageTF = new JTextField(message);
		messageTF.setFont(new Font("宋体", Font.PLAIN, 30));
		messageTF.setBounds(40, 220, 630, 40);
		f.add(messageTF);

		JButton confirm = new JButton("确认");
		confirm.setFont(new Font("宋体", Font.PLAIN, 30));
		confirm.setBounds(40, 300, 630, 50);
		confirm.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				try {
					String yearStr = yearL.getText();
					String monthStr = monthL.getText();
					String dayStr = dayL.getText();
					String hourStr = hourL.getText();
					String minuteStr = minuteL.getText();
					String secondStr = secondL.getText();
					if (isInteger(yearStr) && isInteger(monthStr) && isInteger(dayStr) && isInteger(hourStr) && isInteger(minuteStr) && isInteger(secondStr)) {
						int year = Integer.valueOf(yearStr);// 将字符串转为数字
						int month = Integer.valueOf(monthStr);
						int day = Integer.valueOf(dayStr);
						int hour = Integer.valueOf(hourStr);
						int minute = Integer.valueOf(minuteStr);
						int second = Integer.valueOf(secondStr);
						Date thisMoment = new Date();
						long thisMomentLong = thisMoment.getTime();
						if (notMonkey(year, month, day, hour, minute, second)) {
							
							try {
								message = messageTF.getText();
							} catch (Exception e) {
								message = "";
							}
							targetTime = calendarStr(year, month, day, hour, minute, second);
							f.dispose();
							timeCountdown();
						} else {
							JOptionPane.showMessageDialog(null, "请正确输入日期。");
						}
					} else {
						JOptionPane.showMessageDialog(null, "请正确输入日期。");
					}
				} catch (Exception e) {
					JOptionPane.showMessageDialog(null, "请正确输入日期。");
				}
			}
		});
		f.add(confirm);

		JButton back = new JButton("返回");
		back.setFont(new Font("宋体", Font.PLAIN, 30));
		back.setBounds(40, 370, 630, 50);
		back.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				try {
					message = messageTF.getText();
				} catch (Exception e) {
					message = "";
				}
				f.dispose();
				timeShow();
			}
		});
		f.add(back);

		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setResizable(false);
		f.setLocation((screenWidth - formWidth) / 2, (screenHeight - formHeight) / 2);
		f.setVisible(true);
	}

	private static void timeCountdown() {
		final int formWidth = 870;
		final int formHeight = 630;

		clock = new fehKongChengDingShiNaoZhong();
		clock.setSize(formWidth, formHeight);
		clock.setTitle("定时提醒");
		clock.setLayout(null);

		try {// 设置窗口图标
			Image windowIconImage = ImageIO.read(clock.getClass().getResource("/img/icon/Milas_Turnwheel.png"));
			clock.setIconImage(windowIconImage);
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		Toolkit toolKit = Toolkit.getDefaultToolkit();
		Image trayIconImage = toolKit.getImage(clock.getClass().getResource("/img/icon/Milas_Turnwheel.png"));
		trayIcon = new TrayIcon(trayIconImage, "飞空城送温暖小助手正在计时中\n目标时间：" + targetTime);
		trayIcon.setImageAutoSize(true);
		trayIcon.addMouseListener(new MouseAdapter() {// 增加鼠标监听事件
			public void mouseClicked(MouseEvent e) {
				clock.setVisible(true);
				systemTray.remove(trayIcon);
			}
		});

		JPanel imgPanel=(JPanel) clock.getContentPane();
		imgPanel.setOpaque(false);
		ImageIcon backgroundImage = new ImageIcon(clock.getClass().getResource("/img/background/116290.png"));
		ImageIcon backgroundImage2 = new ImageIcon(clock.getClass().getResource("/img/background/Bg_SkyCastle_04.png"));
		JLabel backgroundImageL0 = new JLabel(backgroundImage2);
		backgroundImageL0.setBounds(780, 200, backgroundImage2.getIconWidth(), backgroundImage2.getIconHeight());
		clock.getLayeredPane().add(backgroundImageL0, Integer.valueOf(Integer.MIN_VALUE) + 0, 0);
		JLabel backgroundImageL1 = new JLabel(backgroundImage2);
		backgroundImageL1.setBounds(-20, 0, backgroundImage2.getIconWidth(), backgroundImage2.getIconHeight());
		clock.getLayeredPane().add(backgroundImageL1, Integer.valueOf(Integer.MIN_VALUE) + 1, 0);
		JLabel backgroundImageL2 = new JLabel(backgroundImage);
		backgroundImageL2.setBounds(-2648, -100, backgroundImage.getIconWidth(), backgroundImage.getIconHeight());
		clock.getLayeredPane().add(backgroundImageL2, Integer.valueOf(Integer.MIN_VALUE) + 2, 0);
		JLabel backgroundImageL3 = new JLabel(backgroundImage);
		backgroundImageL3.setBounds(-600, -1000, backgroundImage.getIconWidth(), backgroundImage.getIconHeight());
		clock.getLayeredPane().add(backgroundImageL3, Integer.valueOf(Integer.MIN_VALUE) + 3, 0);

		JLabel targetTimeTitleL = new JLabel("目标时间");
		targetTimeTitleL.setFont(new Font("宋体", Font.PLAIN, 30));
		targetTimeTitleL.setBounds(370, 40, 600, 40);
		targetTimeTitleL.setForeground(Color.WHITE);
		clock.add(targetTimeTitleL);

		JLabel targetTimeL = new JLabel(targetTime);
		targetTimeL.setFont(new Font("宋体", Font.PLAIN, 60));
		targetTimeL.setBounds(150, 100, 600, 80);
		targetTimeL.setForeground(Color.WHITE);
		clock.add(targetTimeL);

		JLabel thisMomentTitleL = new JLabel("当前时间");
		thisMomentTitleL.setFont(new Font("宋体", Font.PLAIN, 30));
		thisMomentTitleL.setBounds(370, 220, 600, 40);
		thisMomentTitleL.setForeground(Color.WHITE);
		clock.add(thisMomentTitleL);

		thisMomentL = new JLabel();
		thisMomentL.setFont(new Font("宋体", Font.PLAIN, 60));
		thisMomentL.setBounds(150, 280, 600, 80);
		thisMomentL.setForeground(Color.WHITE);
		clock.add(thisMomentL);

		fehKongChengSongWenNuanXiaoZhuShou timer = new fehKongChengSongWenNuanXiaoZhuShou();
		timer.sendValue(targetTime, message, voiceAlarm);// 将参数传入另一个类

		Thread t = new Thread(clock);
		t.start();

		JButton back = new JButton("重新设定");
		back.setFont(new Font("宋体", Font.PLAIN, 30));
		back.setBounds(40, 400, 780, 50);
		back.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				exit = true;
				clock.dispose();
				setAlarm();
			}
		});
		clock.add(back);

		JButton hide = new JButton("将窗口最小化到托盘区");
		hide.setFont(new Font("宋体", Font.PLAIN, 30));
		hide.setBounds(40, 490, 780, 50);
		hide.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				clock.setVisible(false);
				try {
					systemTray.add(trayIcon);// 设置托盘的图标
				} catch (AWTException e) {
					e.printStackTrace();
				}
			}
		});
		clock.add(hide);

		clock.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		clock.setResizable(false);
		clock.setLocation((screenWidth - formWidth) / 2, (screenHeight - formHeight) / 2);
		clock.setVisible(true);
	}

	public static void main(String[] args) {
		timeSet();
	}
}

class fehKongChengDingShiNaoZhong extends JFrame implements Runnable{
	static SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");// 时间格式统一为年月日时分秒

	static int screenWidth = (int) Toolkit.getDefaultToolkit().getScreenSize().getWidth();// 获取屏幕的尺寸
	static int screenHeight = (int) Toolkit.getDefaultToolkit().getScreenSize().getHeight();

	static SystemTray systemTray = SystemTray.getSystemTray();
	static TrayIcon trayIcon;

	static JLabel thisMomentL;
	static boolean exit;

	static String targetTime;
	static String message = "";
	static boolean voiceAlarm = false;
	public void sendValue(String targetTimeValue, String messageValue, boolean voiceAlarmValue) {// 接收另一个类中的参数
		targetTime = targetTimeValue;
		message = messageValue;
		voiceAlarm = voiceAlarmValue;
	}

	public void run() {
		long targetTimeClockLong;
		long thisMomentLong;
		long offset;
		try {
			targetTimeClockLong = df.parse(targetTime).getTime();
		}
		catch (Exception e) {
			targetTimeClockLong = 0;
		}

		exit = false;
		while(!exit) {
			Date thisMoment = new Date();
			thisMomentLong = thisMoment.getTime();
			thisMomentL.setText(df.format(thisMomentLong + 1000));
			offset = thisMomentLong % 1000 - 500;
			if (thisMomentLong > targetTimeClockLong - 1000) {
				dispose();
				systemTray.remove(trayIcon);
				alarm();
				exit = true;
			}
			try {
				Thread.sleep(1000 - offset);// 消除延时的偏差
			}
			catch (InterruptedException e) {}
		}
	}

	private int getMessageLength() {
		char[] chars = message.toCharArray();
		int length = 0;
		for (char aChar : chars) {
			int charIndex = Integer.valueOf(aChar);
			if (32 <= charIndex && charIndex < 127) {
				length += 1;
			} else {
				length += 2;
			}
		}
		return length;
	}

	private void alarm() {
		if (voiceAlarm) {
			try {
				Process process = Runtime.getRuntime().exec("mshta vbscript:createobject(\"sapi.spvoice\").speak(\"" + message + "\")(window.close)");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		JFrame f = new JFrame("提醒");
		int formWidth = 500;
		int formHeight = 260;
		f.setLayout(null);
		f.setSize(formWidth, formHeight);

		try {// 设置窗口图标
			Image windowIconImage = ImageIO.read(f.getClass().getResource("/img/icon/Life_and_Death_4.png"));
			f.setIconImage(windowIconImage);
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		JPanel imgPanel=(JPanel) f.getContentPane();//内容面板必须强转为JPanel才可以实现下面的设置透明
		imgPanel.setOpaque(false);//将内容面板设为透明
		ImageIcon backgroundImage = new ImageIcon(f.getClass().getResource("/img/background/Bg_SkyCastle_03.png"));
		JLabel backgroundImageL = new JLabel(backgroundImage);
		backgroundImageL.setBounds(-50, -300, backgroundImage.getIconWidth(), backgroundImage.getIconHeight());
		f.getLayeredPane().add(backgroundImageL, Integer.valueOf(Integer.MIN_VALUE));//标签添加到层面板
		ImageIcon portraitImage = new ImageIcon(f.getClass().getResource("/img/portrait/67345_2.png"));
		JLabel portraitImageL = new JLabel(portraitImage);
		portraitImageL.setBounds(350, 0, portraitImage.getIconWidth(), portraitImage.getIconHeight());
		f.getLayeredPane().add(portraitImageL, Integer.valueOf(Integer.MIN_VALUE) + 1);

		int messageSize = 15 * getMessageLength();
		JPanel messageArea = new JPanel();
		messageArea.setLayout(null);
		JLabel messageL = new JLabel(message);
		messageL.setFont(new Font("宋体", Font.PLAIN, 30));
		messageL.setBounds(0, 0, messageSize, 40);
		messageArea.add(messageL);
		messageArea.setPreferredSize(new Dimension(messageSize, 40));
		JScrollPane mScroll = new JScrollPane(messageArea, JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		mScroll.setBounds(30, 30, 330, 60);// 设定卷轴最大显示范围的尺寸
		f.add(mScroll);

		JButton back = new JButton("我知道了");
		back.setFont(new Font("宋体", Font.PLAIN, 30));
		back.setBounds(40, 120, 310, 40);
		back.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				System.exit(0);
			}
		});
		f.add(back);

		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setResizable(false);
		f.setLocation((screenWidth - formWidth), (screenHeight - formHeight));
		f.setVisible(true);
		f.setAlwaysOnTop(true);
	}
}