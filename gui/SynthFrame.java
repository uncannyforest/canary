package gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import audio.AudioUnit;
import audio.Speaker;
import audio.Synth;

class SynthFrame extends JFrame {
	public static final int WINDOW_WIDTH = 500;
	public static final int WINDOW_HEIGHT = 400;
	
	private int width = 64;
	private int height = 16;
	private final int zoomExp = 4; // 2^4 = 16
	
	private BufferedImage song = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
	private int color = 0xFF7F7F7F;
	
	private final JFileChooser chooser;
	private BufferedImage image = new BufferedImage(width<<zoomExp, height<<zoomExp, BufferedImage.TYPE_INT_ARGB);
	private ImageIcon icon = new ImageIcon(image);
	private JLabel label = new JLabel(icon);
	private Graphics2D gfx = (Graphics2D)(image.getGraphics());
	
	private Speaker spkr = new Speaker();
	private Synth synth = new Synth();
	
	public SynthFrame() {
		this.setTitle("Synth Editor");
		this.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
		
		// set up panel
		this.getContentPane().add(new JScrollPane(label), BorderLayout.CENTER);
		this.getContentPane().add(new OptionsPanel(this), BorderLayout.NORTH);
		label.setHorizontalAlignment(SwingConstants.LEFT);
		label.setVerticalAlignment(SwingConstants.TOP);
		
		// set up menu
		addMenu();
		
		// set up file chooser dialog
		chooser = new JFileChooser();
		chooser.setCurrentDirectory(new File("."));
		
		// set up image
		setupImage();
		label.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent event) {
				drawSquare(event.getX()>>zoomExp, event.getY()>>zoomExp);
			}
		});
		gfx.setColor(new Color(127, 127, 127)); // TODO make default constant
		
		// finish set up
		this.setVisible(true);
	}
	
	private void addMenu() {
		JMenuItem menuItem;

		JMenu fileMenu = new JMenu("File");
		
		menuItem= new JMenuItem("Open PNG");
		menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				open();
			}
		});
		fileMenu.add(menuItem);
		
		menuItem= new JMenuItem("Save as PNG");
		menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				save();
			}
		});
		fileMenu.add(menuItem);
		
		menuItem= new JMenuItem("Exit");
		menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				System.exit(0);
			}
		});
		fileMenu.add(menuItem);
		
		JMenuBar menuBar = new JMenuBar();
		menuBar.add(fileMenu);
		this.setJMenuBar(menuBar);
	}
	
	private void setupImage() {
		for (int x=0; x<width; x++) {
			for (int y=0; y<height; y++) {
				song.setRGB(x, y, 0xFF000000);
			}
		}
		
		gfx.setColor(Color.BLACK);
		gfx.fillRect(0, 0, width<<zoomExp, height<<zoomExp);
		repaint();
	}
	
	public void drawSquare(int x, int y) {
		song.setRGB(x, y, color);
		
		gfx.fillRect(x<<zoomExp, y<<zoomExp, 1<<zoomExp, 1<<zoomExp);
		repaint();
	}
	
	public void zoomIn() {
		
	}
	
	public void zoomOut() {
		
	}
	
	public void setColor(int r, int g, int b) {
		gfx.setColor(new Color(r, g, b));
		color = 0xFF000000 |
				(r << 16) |
				(g << 8) |
				b;
	}
	
	public void displayBufferedImage(BufferedImage image) {
		icon.setImage(image);
		label.repaint();
		this.validate();
	}

	public void play() {
		synth.setScore(song);
		spkr.setSource(synth);
	}
	
	public void stop() {
		spkr.setSource(AudioUnit.NULL_UNIT);
		synth.setScore(null);
	}
	
	public void open() {
		try {
			File file = getOpenFile();
			if (file!=null) {
				song = ImageIO.read(file);
				displaySong();
			}
		} catch (IOException e) {
			JOptionPane.showMessageDialog(this,
					"Error saving file",
					"oops!",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	public void save() {
		try {
			javax.imageio.ImageIO.write(song, "png", getSaveFile());
		} catch (IOException e) {
			JOptionPane.showMessageDialog(this,
					"Error saving file",
					"oops!",
					JOptionPane.ERROR_MESSAGE);
		}
	}
	
	// display the image stored in song property
	// this is only necessary when loading a new song
	private void displaySong() {
		width = song.getWidth();
		height = song.getHeight();
		image = new BufferedImage(width<<zoomExp, height<<zoomExp, BufferedImage.TYPE_INT_ARGB);
		gfx = (Graphics2D)(image.getGraphics());

		int color;
		for (int x=0; x<width; x++) {
			for (int y=0; y<height; y++) {
				color = song.getRGB(x, y);
				gfx.setColor(new Color(color & 0x00FFFFFF));
				gfx.fillRect(x<<zoomExp, y<<zoomExp, 1<<zoomExp, 1<<zoomExp);
			}
		}
		icon.setImage(image);
		label.repaint();
		this.validate();
	}

	private File getOpenFile() {
		File file = null;
		if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
			file = chooser.getSelectedFile();
		return file;
	}
	
	private File getSaveFile() {
		File file = null;
		if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION)
			file = chooser.getSelectedFile();
		return file;
	}
}

class OptionsPanel extends JPanel {
	private final SynthFrame frame;
		
//	private final JButton zoomIn, zoomOut;
	private final JButton play;
	private final JLabel lVolume, lPitch, lTimbre;
	private final JSpinner tVolume, tPitch, tTimbre;
	
	private int volume = 127;
	private int pitch = 127;
	private int timbre = 127;
	
	boolean playing = false;
	
	public OptionsPanel(SynthFrame parentFrame) {
		frame = parentFrame;
		
		FlowLayout layout = new FlowLayout();
		setLayout(layout);
		layout.setAlignment(FlowLayout.LEFT);
		
		///// PLAY
		play = new JButton("Play");
		add(play);
		play.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if (!playing) {
					frame.play();
					play.setText("Stop");
					playing = true;
				} else {
					frame.stop();
					play.setText("Play");
					playing = false;
				}
			}
		});
		
		/*
		///// ZOOM
		zoomIn = new JButton("Zoom In");
		zoomOut = new JButton("Zoom Out");
		// hZoomIn = new JButton("> <"); // TODO
		// hZoomOut = new JButton("< >");
		
		add(zoomIn);
		add(zoomOut);
		// add(hZoomIn);
		// add(hZoomOut);
		
		zoomIn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				parent.zoomIn();
			}
		});
		
		zoomOut.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				parent.zoomOut();
			}
		});
		*/
		
		///// COLORS
		lVolume = new JLabel("Vol");
		lPitch = new JLabel("Pch");
		lTimbre = new JLabel("Tbr");
		
		tVolume = new JSpinner(new SpinnerNumberModel(127, 0, 255, 1));
		tPitch = new JSpinner(new SpinnerNumberModel(127, 0, 255, 1));
		tTimbre = new JSpinner(new SpinnerNumberModel(127, 0, 255, 1));
		
		add(lVolume);
		add(tVolume);
		add(lPitch);
		add(tPitch);
		add(lTimbre);
		add(tTimbre);
		
		tVolume.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				volume = ((SpinnerNumberModel)tVolume.getModel()).getNumber().intValue();
				frame.setColor(volume, pitch, timbre);
			}
		});
		tPitch.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				pitch = ((SpinnerNumberModel)tPitch.getModel()).getNumber().intValue();
				frame.setColor(volume, pitch, timbre);
			}
		});
		tTimbre.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				timbre = ((SpinnerNumberModel)tTimbre.getModel()).getNumber().intValue();
				frame.setColor(volume, pitch, timbre);
			}
		});
	}
}
