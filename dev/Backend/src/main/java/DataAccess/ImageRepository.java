package DataAccess;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import Image.Image;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {
}
