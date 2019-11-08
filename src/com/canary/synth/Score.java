package com.canary.synth;

import java.util.ArrayList;

import com.canary.synth.tone.Tone;

public class Score {
	private ArrayList<ArrayList<Tone>> notes;
	private int baseNote;
	private double notesPerSecond;
	private double volume;
	
	public Score(ArrayList<ArrayList<Tone>> notes, int baseNote, double notesPerSecond, double volume) {
		this.notes = notes;
		this.baseNote = baseNote;
		this.notesPerSecond = notesPerSecond;
		this.volume = volume;
	}
	
	public ArrayList<ArrayList<Tone>> getNotes() {
		return notes;
	}
	
	public int getBaseNote() {
		return baseNote;
	}
	
	public double getNotesPerSecond() {
		return notesPerSecond;
	}
	
	public double getVolume() {
		return volume;
	}
}
