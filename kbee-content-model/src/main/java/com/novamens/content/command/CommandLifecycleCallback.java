package com.novamens.content.command;

public interface CommandLifecycleCallback {

    public void start();
    public void stop();
    public void end();

}
