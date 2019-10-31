package com.shirali.widget.loader;

public class LoaderGenerator {
  public static LoaderView generateLoaderView(int type) {

    Pulse pulse= null;
    try {
      pulse = new Pulse(5);
    } catch (InvalidNumberOfPulseException e) {
      e.printStackTrace();
    }
    return pulse;
  }
  public static LoaderView generateLoaderView(String type) {

    Pulse pulse= null;
    try {
      pulse = new Pulse(5);
    } catch (InvalidNumberOfPulseException e) {
      e.printStackTrace();
    }
    return pulse;
  }
}
