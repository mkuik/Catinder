package kuik.matthijs.catinder.util;

/**
 * Created by Matthijs Kuik on 7/26/2016.
 */
public class GalleryItem {
    private String image;
    private String title;

    public GalleryItem(String image) {
        super();
        this.image = image;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
