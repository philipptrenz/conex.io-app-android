package io.conex.app.datamodels;

/**
 * Created by philipp on 08.05.17.
 */

public class OverviewItem {

    public String name;
    public String description;
    public Mode mode;

    public OverviewItem(String name, Mode mode, String description) {
        this.name = name;
        this.description = description;
        this.mode = mode;
    }
}