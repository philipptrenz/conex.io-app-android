package io.conex.app.datamodels;

/**
 * Created by philipp on 08.05.17.
 */

public class CategoryItem {

    public String category;
    public Mode mode;
    public String description;

    public CategoryItem(String category, Mode mode) {
        this.category = category;
        this.mode = mode;

        switch (mode) {
            case FUNCTIONS:
                this.description = "Show all devices with functionality '"+category+"'";
                break;
            case ROOMS:
                this.description = "Show all devices in group '"+category+"'";
                break;
            case GROUPS:
                this.description = "Show all devices in room '"+category+"'";
                break;
        }
    }

}
