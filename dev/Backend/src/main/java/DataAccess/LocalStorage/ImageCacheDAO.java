package DataAccess.LocalStorage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import DataAccess.ImageDAO;
import Image.*;

public class ImageCacheDAO implements ImageDAO {
    private final Map<String, Image> cache;

    // If true, the cache will work as if it was a db,
    // meanning, working with transactions, forbidding access
    // by pointer reference to cache objects
    private final boolean serveAsDB;

    public ImageCacheDAO() {
        cache = new HashMap<>();
        serveAsDB = true; // Default to DB-like behavior
    }

    public ImageCacheDAO(boolean serveAsDB) {
        this.cache = new HashMap<>();
        this.serveAsDB = serveAsDB;
    }

    @Override
    public void insertImage(Image image) {
        // Always store a deep copy to prevent external modifications
        if(serveAsDB)
            cache.put(image.getId(), new Image(image));
        else
            cache.put(image.getId(),image);
    }

    @Override
    public Image getImageById(String id) {
        Image image = cache.get(id);
        if (image == null) {
            return null; // Image not found
        }
        // Return a deep copy if serving as a DB
        return serveAsDB ? new Image(image) : image;
    }

    @Override
    public List<Image> getAllImages() {
        // Return deep copies if serving as a DB
        return cache.values().stream()
                .map(image -> serveAsDB ? new Image(image) : image)
                .collect(Collectors.toList());
    }

    @Override
    public void updateImage(Image image) {
        String id = image.getId();
        if (!cache.containsKey(id)) {
            throw new IllegalArgumentException("Image not found: " + id);
        }
        // Store a deep copy to enforce transactional behavior
        cache.put(id, new Image(image));
    }

    @Override
    public void deleteImage(String id) {
        if (!cache.containsKey(id)) {
            throw new IllegalArgumentException("Image not found: " + id);
        }
        cache.remove(id);
    }
}