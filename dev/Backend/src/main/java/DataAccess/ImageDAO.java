package DataAccess;

import java.util.List;
import java.util.Map;
import Image.*;

public interface ImageDAO {
    void insertImage(Image image);
    Image getImageById(String id);
    List<Image> getAllImages();
    void updateImage(Image image);
    void deleteImage(String id);
}