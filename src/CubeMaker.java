import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.JToolBar;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.awt.event.ActionEvent;
import java.awt.Dimension;
import java.awt.Point;

public class CubeMaker extends JFrame {

	private JPanel contentPane;
	private ImagePanel imagePanel;
	private JComboBox faceComboBox;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					CubeMaker frame = new CubeMaker();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public CubeMaker() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setTitle("3D Cube Maker");
		setBounds(100, 100, 1024, 768);
		contentPane = new JPanel();
		contentPane.setLayout(new GridBagLayout());
		setContentPane(contentPane);
		GridBagConstraints c = new GridBagConstraints();

		JToolBar toolBar = new JToolBar();
		toolBar.setFloatable(false);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.5;
		c.weighty = 0.0;
		c.gridx = 0;
		c.gridy = 0;
		contentPane.add(toolBar, c);

		JPanel toolbarLayoutPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 15));

		JFileChooser fc = new JFileChooser();
		FileNameExtensionFilter filter = new FileNameExtensionFilter("PNG File", "png", "png");
		fc.setFileFilter(filter);

		JButton selectImage = new JButton("Select Image");
		selectImage.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				fc.showDialog(getParent(), "Select");
				openImage(fc.getSelectedFile().toString());
			}
		});
		toolbarLayoutPanel.add(selectImage);

		JButton rotateImage = new JButton("Rotate Image");
		rotateImage.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				imagePanel.rotateImage();
			}
		});
		toolbarLayoutPanel.add(rotateImage);

		JLabel scaleSliderLabel = new JLabel("Scale Image");
		JSlider scaleSlider = new JSlider(JSlider.HORIZONTAL, 10, 500, 100);
		scaleSlider.setMajorTickSpacing(50);
		scaleSlider.setPaintTicks(true);
		java.util.Hashtable<Integer, JLabel> labelTable = new java.util.Hashtable<Integer, JLabel>();
		labelTable.put(new Integer(500), new JLabel("5.0"));
		labelTable.put(new Integer(400), new JLabel("4.0"));
		labelTable.put(new Integer(300), new JLabel("3.0"));
		labelTable.put(new Integer(200), new JLabel("2.0"));
		labelTable.put(new Integer(100), new JLabel("1.0"));
		labelTable.put(new Integer(10), new JLabel("0.1"));
		scaleSlider.setLabelTable(labelTable);
		scaleSlider.setPaintLabels(true);
		scaleSlider.addChangeListener(new javax.swing.event.ChangeListener() {
			public void stateChanged(javax.swing.event.ChangeEvent ce) {
				if (!scaleSlider.getValueIsAdjusting()) {
					double scaleFactor = scaleSlider.getValue() / 100.0;
					System.out.println(scaleSlider.getValue() / 100.0);
					imagePanel.scaleImage(scaleFactor);
				}
			}
		});
		toolbarLayoutPanel.add(scaleSliderLabel);
		toolbarLayoutPanel.add(scaleSlider);

		JLabel joystickLabel = new JLabel("Move Stencil");
		Joystick joystick = new Joystick(65, 55);
		joystick.addPropertyChangeListener(updateJoystickListener);
		toolbarLayoutPanel.add(joystickLabel);
		toolbarLayoutPanel.add(joystick);

		JLabel faceSizeLabel = new JLabel("Face Size");
		String faceSizes[] = { "64 px", "128 px", "256 px", "512 px" };
		faceComboBox = new JComboBox(faceSizes);
		faceComboBox.setSelectedIndex(1);
		faceComboBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String s = (String) faceComboBox.getSelectedItem();

				switch (s) {

				case "64 px":
					imagePanel.updateStencilFacePixels(64);
					break;

				case "128 px":
					imagePanel.updateStencilFacePixels(128);
					break;

				case "256 px":
					imagePanel.updateStencilFacePixels(256);
					break;

				case "512 px":
					imagePanel.updateStencilFacePixels(512);
					break;

				}
			}
		});
		faceSizeLabel.setPreferredSize(new Dimension(60, 20));
		toolbarLayoutPanel.add(faceSizeLabel);
		toolbarLayoutPanel.add(faceComboBox);

		JButton genTexture = new JButton("Gen Texture");
		genTexture.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				imagePanel.generateTexture();
			}
		});
		toolbarLayoutPanel.add(genTexture);

		toolBar.add(toolbarLayoutPanel);

		imagePanel = new ImagePanel();
		imagePanel.setPreferredSize(new Dimension(8192, 6144));
		imagePanel.setVisible(true);

		JScrollPane scrollFrame = new JScrollPane(imagePanel);
		imagePanel.setAutoscrolls(true);

		c.fill = GridBagConstraints.BOTH;
		c.weightx = 0.5;
		c.weighty = 1.0;
		c.gridx = 0;
		c.gridy = 1;
		contentPane.add(scrollFrame, c);

	}

	private double joystickScaleFactor() {

		String s = (String) faceComboBox.getSelectedItem();
		double scaleFactor = 1.0;

		switch (s) {

		case "64 px":
			scaleFactor = 0.75;
			break;

		case "128 px":
			scaleFactor = 1.0;
			break;

		case "256 px":
			scaleFactor = 2.0;
			break;

		case "512 px":
			scaleFactor = 3.0;
			break;

		}

		return scaleFactor;
	}

	private PropertyChangeListener updateJoystickListener = (evt) -> {
		Point newValue = (Point) evt.getNewValue();
		newValue.x = (int) (newValue.x * joystickScaleFactor());
		newValue.y = (int) (newValue.y * joystickScaleFactor());
		imagePanel.updateStencilOrigin(newValue);
	};

	private void openImage(String imageFileName) {

		imagePanel.updateImage(imageFileName);
		contentPane.validate();
	}

}
