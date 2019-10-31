package com.shirali.widget.loader;

public class InvalidNumberOfPulseException extends Exception {

  @Override public String getMessage() {
    return "The number of pulse must be between 2 and 6";
  }
}
