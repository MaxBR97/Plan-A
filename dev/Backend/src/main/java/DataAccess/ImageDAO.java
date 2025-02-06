package DataAccess;

import java.util.List;
import java.util.Map;
import Image.*;

public interface ImageDAO {
    public void insertImage(Image image);
    public Image getImageById(String id);
    public List<Image> getAllImages();
    public void updateImage(Image image);
    public void deleteImage(String id);
}