package com.canary.desktop.gui;

import com.canary.desktop.prc.ImageImpl;
import com.canary.desktop.snd.AudioUnit;
import com.canary.desktop.snd.Speaker;
import com.canary.desktop.snd.SynthInput;
import com.canary.synth.FormatConverter;
import com.canary.synth.Synthesizer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;

public class SynthFrame extends JFrame {
    public static final int WINDOW_WIDTH = 900;
    public static final int WINDOW_HEIGHT = 600;

    private int width = 64;
    private int height = 16;
    private final int zoomExp = 4; // 2^4 = 16

    private File file;
    private BufferedImage song = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    private int color = 0xFF7F7F7F;
    private boolean usingDroplet = false;

    private final JFileChooser chooser;
    private BufferedImage image = new BufferedImage(width<<zoomExp, height<<zoomExp, BufferedImage.TYPE_INT_ARGB);
    private ImageIcon icon = new ImageIcon(image);
    private JLabel label = new JLabel(icon);
    private Graphics2D gfx = (Graphics2D)(image.getGraphics());
    private OptionsPanel optionsPanel;

    private Speaker spkr = new Speaker();
    private Synthesizer synth;
    private SynthInput input;

    public SynthFrame() {
        this.setTitle("Synth Editor");
        this.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);

        // set up panel
        this.getContentPane().add(new JScrollPane(label), BorderLayout.CENTER);
        optionsPanel = new OptionsPanel(this);
        this.getContentPane().add(optionsPanel, BorderLayout.NORTH);
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
                touchSquare(event.getX()>>zoomExp, event.getY()>>zoomExp, usingDroplet);
            }});
        gfx.setColor(new Color(127, 127, 127)); // TODO make default constant

        // finish set up
        this.setVisible(true);
    }

    public ImageImpl getSong() {
        return new ImageImpl(song);
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

    public void touchSquare(int x, int y, boolean usingDroplet) {
        if (usingDroplet) {
            dropletSquare(x, y);
        } else {
            drawSquare(x, y);
        }
    }

    public void drawSquare(int x, int y) {
        song.setRGB(x, y, color);

        gfx.fillRect(x<<zoomExp, y<<zoomExp, 1<<zoomExp, 1<<zoomExp);
        repaint();
        optionsPanel.update();
    }

    public void dropletSquare(int x, int y) {
        color = song.getRGB(x, y);
        optionsPanel.setColor(color);
    }

    public void setColor(int r, int g, int b) {
        gfx.setColor(new Color(r, g, b));
        color = 0xFF000000 |
                (r << 16) |
                (g << 8) |
                b;
    }

    public boolean getUsingDroplet() {
        return this.usingDroplet;
    }

    public void setUsingDroplet(boolean usingDroplet) {
        this.usingDroplet = usingDroplet;
    }

    public void displayBufferedImage(BufferedImage image) {
        icon.setImage(image);
        label.repaint();
        this.validate();
    }

    public void play() {
        spkr.setSource(input);
    }

    public void stop() {
        spkr.setSource(AudioUnit.NULL_UNIT);
    }

    public void open() {
        file = getOpenFile();
        load();
    }

    public void load() {
        if (file!=null) {
            try {
                song = ImageIO.read(file);
                displaySong();
                process();
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this,
                        "Error loading file",
                        "oops!",
                        JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }

    public void process() {
        synth = new Synthesizer(new ImageImpl(song));
        input = new SynthInput(synth);
        optionsPanel.update();
    }

    public void convert() {
        FormatConverter.toV14(new ImageImpl(song));
        displaySong();
        process();
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

    // update the display of the image stored in song property
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

        optionsPanel.update();
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

    private final JButton play, reload, update, convert, droplet;
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

        ///// BUTTONS
        play = new JButton("Play");
        add(play);
        play.addActionListener((event) ->  {
            if (!playing) {
                frame.play();
                play.setText("Stop");
                playing = true;
            } else {
                frame.stop();
                play.setText("Play");
                playing = false;
            }
        });

        reload = new JButton("Reload");
        add(reload);
        reload.addActionListener((event) -> frame.load());

        update = new JButton("Update Audio");
        add(update);
        update.addActionListener((event) ->  frame.process());

        convert = new JButton("Convert to v1.4");
        add(convert);
        convert.addActionListener((event) ->  frame.convert());

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

        tVolume.addChangeListener((ChangeEvent e) -> {
            volume = ((SpinnerNumberModel)tVolume.getModel()).getNumber().intValue();
            frame.setColor(volume, pitch, timbre);
        });
        tPitch.addChangeListener((ChangeEvent e) -> {
            pitch = ((SpinnerNumberModel)tPitch.getModel()).getNumber().intValue();
            frame.setColor(volume, pitch, timbre);
        });
        tTimbre.addChangeListener((ChangeEvent e) -> {
            timbre = ((SpinnerNumberModel)tTimbre.getModel()).getNumber().intValue();
            frame.setColor(volume, pitch, timbre);
        });

        droplet = new JButton("Pen");
        add(droplet);
        droplet.addActionListener((ActionEvent event) -> {
            if (!frame.getUsingDroplet()) {
                droplet.setText("Droplet");
                frame.setUsingDroplet(true);
            } else {
                droplet.setText("Pen");
                frame.setUsingDroplet(false);
            }
        });

        update();
    }

    public void update() {
        boolean canConvert = FormatConverter.canConvertToV14(frame.getSong());
        convert.setEnabled(canConvert);
    }

    public void setColor(int color) {
        tVolume.setValue((color & 0x00FF0000) >> 16);
        tPitch.setValue((color & 0x0000FF00) >> 8);
        tTimbre.setValue(color & 0x000000FF);
    }
}
