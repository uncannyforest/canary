package com.canary.io;

/**
 * Must be immutable: after creation, these methods will always return the same value given the same
 * parameters.
 *
 * Created on 1/13/2017.
 */
public interface AudioSource {

    double getValue(double time);

    // in seconds
    double getDuration();
}
