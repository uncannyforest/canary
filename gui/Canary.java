package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.swing.*;

import audio.*;

public class Canary {
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI();
			}
		});
	}
	
	public static void createAndShowGUI() {
		JFrame frame = new CanaryFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}
}

class CanaryFrame extends JFrame {
	public static final int WIDTH = 500;
	public static final int HEIGHT = 400;
	
	private int sizeFactor = 4;
		// horiz margin = sizeFactor;
		// vert margin = sizeFactor * 3;
		// box size = sizeFactor * 12;
		// total grid size = sizeFactor * 14 x sizeFactor * 18
	
	private AudioUnitPanel movingUnit = null;
	
	public final AudioGridPanel audioGridPanel = new AudioGridPanel(this, sizeFactor);
	public final UnitsPanel unitsPanel = new UnitsPanel(this, sizeFactor);
	
	public CanaryFrame() {
		this.setTitle("Mechanical Canary");
		this.setSize(WIDTH, HEIGHT);
		
		this.getContentPane().add(audioGridPanel, BorderLayout.CENTER);
		this.getContentPane().add(unitsPanel, BorderLayout.NORTH);

		addMenu();
	}
	
	private void addMenu() {
		JMenuItem menuItem;

		JMenu fileMenu = new JMenu("File");
		
		menuItem= new JMenuItem("Open Synth Editor");
		menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				new SynthFrame(); // constructor sets visible automatically
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
	
	public AudioUnitPanel getMovingUnit() {
		return movingUnit;
	}
	
	public void setMovingUnit(AudioUnitPanel movingUnit) {
		this.movingUnit = movingUnit;
	}
}

class UnitsPanel extends JPanel {
	private CanaryFrame frame;
	
	public UnitsPanel(CanaryFrame parentFrame, int sizeFactor) {
		frame = parentFrame;
		int unitSize = sizeFactor * 12;
		
		FlowLayout layout = new FlowLayout();
		setLayout(layout);
		layout.setAlignment(FlowLayout.LEFT);

		this.add(new JLabel("inputs:"));
		this.add(new AudioUnitPanel(frame, new Microphone(), unitSize, new Color(0xFFC00000)));
		this.add(new AudioUnitPanel(frame, new Synth(), unitSize, new Color(0xFFC06000)));
		this.add(new JLabel("filters:"));
		this.add(new AudioUnitPanel(frame, new LowPass(), unitSize, new Color(0xFF60C000)));
		this.add(new AudioUnitPanel(frame, new Loop(), unitSize, new Color(0xFF00C000)));
		this.add(new JLabel("outputs:"));
		this.add(new AudioUnitPanel(frame, new Speaker(), unitSize, new Color(0xFF0000C0)));


		// set up click adapter
		this.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent event) {
				if (frame.getMovingUnit() != null) {
					frame.audioGridPanel.removeUnit(frame.getMovingUnit());
					frame.setMovingUnit(null);
				}
			}
		});
	}
}

class AudioGridPanel extends JPanel {
	private final CanaryFrame frame;
	private final int sizeFactor;
	
	public List<List<AudioUnitPanel>> grid = new ArrayList<List<AudioUnitPanel>>();
	
	public AudioGridPanel(CanaryFrame parentFrame, int pSizeFactor) {
		frame = parentFrame;
		sizeFactor = pSizeFactor;
		
		this.setBackground(Color.BLACK);
		this.setLayout(null);
		
		// set up click adapter
		this.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent event) {
				if (frame.getMovingUnit() != null) {
					removeUnit(frame.getMovingUnit());
					addUnit(frame.getMovingUnit(), event.getY() / (sizeFactor * 18), event.getX() / (sizeFactor * 14));
					frame.setMovingUnit(null);
				}
			}
		});
	}

	public void removeUnit(AudioUnitPanel panel) {
		//remove from list
		for (int i=0; i<grid.size(); i++) {
			List<AudioUnitPanel> list = grid.get(i);
			for (int j=0; j<list.size(); j++) {
				if (list.get(j) == panel) {
					list.set(j, null);
				}
			}
		}
		
		//remove from GUI
		this.remove(panel);
		this.revalidate();
		this.repaint();
		
		// update audio
		updateAudio();
		updateRemovedAudioUnit(panel);
	}
	
	public void addUnit(AudioUnitPanel panel, int row, int col) {
		// First expand grid double array to fir the new panel's position
		for (int i=grid.size(); i <= row; i++) {
			grid.add(new ArrayList<AudioUnitPanel>());
		}
		List<AudioUnitPanel> list = grid.get(row);
		
		for (int j=list.size(); j <= col; j++) {
			list.add(null);
		}
		AudioUnitPanel replacedPanel = list.get(col);
		list.set(col, panel);
	
		// Next add the panel
		this.add(panel);
		Dimension size = panel.getPreferredSize();
		panel.setBounds((col * 14 + 1) * sizeFactor, (row * 18 + 1) * sizeFactor, size.width, size.height);
			// with absolute positioning, it is necessary to call setBounds on each panel added at runtime
			// that took me a really long time to figure out
		this.revalidate();

		// update audio
		updateAudio();
		updateRemovedAudioUnit(replacedPanel);
	}
	
	public void updateRemovedAudioUnit(AudioUnitPanel panel) {
		if (panel != null) 
			panel.unit.setSource(AudioUnit.NULL_UNIT);
	}
	
	public void updateAudio() {
		for (List<AudioUnitPanel> list: grid) if (list.size() > 0) {
			AudioUnitPanel previousPanel = list.get(0);
			AudioUnitPanel panel;
			boolean inMainChain = (previousPanel != null); // once it becomes false, make sure all further units have no source connections
				// that accounts for gaps in the line
			boolean validLine = true; // false if any inputs other than the beginning
			for (int i=1; i<list.size(); i++) {
				panel = list.get(i);
				if (panel==null) inMainChain = false;
				if (inMainChain) {
					validLine = panel.unit.setSource(previousPanel.unit);
					if (!validLine) break;
					previousPanel = panel;
				} else if (panel!=null) {
					panel.unit.setSource(AudioUnit.NULL_UNIT);
				}
			}
			
			// invalidate entire line if setSource returned false
			if (!validLine) for (int i=1; i<list.size(); i++) {
				panel = list.get(i);
				if (panel!=null) {
					panel.unit.setSource(null);
				}
			}
		}
	}
}

class AudioUnitPanel extends JPanel {
	private int widthRatio;
	
	private JLabel lTitle;
	private JLabel lKeys;
	
	private int size;
	private Color color;
	
	final CanaryFrame frame;
	final AudioUnit unit;
	final boolean isOriginal; // true for those in the units pane, not those in the grid
	
	public AudioUnitPanel(CanaryFrame parentFrame, AudioUnit unit, int size, Color color) {
		this(parentFrame, unit, size, color, true);
	}

	public AudioUnitPanel(CanaryFrame parentFrame, AudioUnit unit, int size, Color color, boolean isOriginal) {
		frame = parentFrame;
		this.unit = unit;
		this.size = size;
		this.color = color;
		this.isOriginal = isOriginal;
		
		String title = unit.getTitle();
		char[] keys = unit.getKeys();
		
		// compute details
		widthRatio = 1;
		StringBuilder keyStringBuilder = new StringBuilder();
		for (int i=0; ; i++) {
			keyStringBuilder.append(keys[i]);
			if (i == keys.length-1) break;
			keyStringBuilder.append(' ');
		}
		
		// set up text
		lTitle = new JLabel(title);
		lTitle.setForeground(Color.WHITE);
		lTitle.setHorizontalAlignment(SwingConstants.CENTER);
		lKeys = new JLabel(keyStringBuilder.toString());
		lKeys.setForeground(Color.WHITE);
		lKeys.setHorizontalAlignment(SwingConstants.CENTER);
		this.setLayout(new BorderLayout());
		this.add(lTitle, BorderLayout.NORTH);
		this.add(lKeys, BorderLayout.SOUTH);
		
		// set up background
		this.setBackground(color);
		this.setPreferredSize(new Dimension(size * widthRatio, size));
		
		// set up click adapter
		this.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent event) {
				if (frame.getMovingUnit() == null) frame.setMovingUnit(getAudioUnitPanel());
			}
		});
	}

	// This is a shortcut method to return this if panel is in grid, and a duplicate if in the units toolbar
	// This way those in the units toolbar never get removed
	public AudioUnitPanel getAudioUnitPanel() {
		if (!isOriginal) return this;
		else return new AudioUnitPanel(frame, unit.copy(), size, color, false);
	}
}