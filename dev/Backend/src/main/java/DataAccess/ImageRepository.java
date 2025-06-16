package DataAccess;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import DTO.Records.Image.ShallowImageProjection;
import Image.Image;
import java.util.List;
import org.springframework.data.domain.Pageable;

@Repository
public interface ImageRepository extends JpaRepository<Image, String> {

    @Query("SELECT i FROM Image i WHERE i.owner = :owner AND i.id = :imageId")
    Image findByIdAndOwner(@Param("imageId") String imageId, @Param("owner") String owner);

    @Query("SELECT i FROM Image i WHERE i.id = :imageId AND (i.owner = :owner OR (i.isPrivate = false))")
    Image findByIdAndAccessible(@Param("imageId") String imageId, @Param("owner") String owner);

    @Query("""
      SELECT i.id AS id, i.name AS name, i.description AS description, i.owner AS owner, i.isPrivate AS isPrivate, i.isConfigured AS isConfigured
      FROM Image i
      WHERE LOWER(i.name) LIKE LOWER(CONCAT('%', :searchPhrase, '%'))
        AND (i.owner = :owner OR i.isPrivate = false)
        AND i.isConfigured = true
  """)
  List<ShallowImageProjection> searchShallowImages(@Param("searchPhrase") String searchPhrase,
                                                   @Param("owner") String owner,
                                                   Pageable pageable);

    @Query("""
      SELECT i.id AS id, i.name AS name, i.description AS description, i.owner AS owner, i.isPrivate AS isPrivate, i.isConfigured AS isConfigured
      FROM Image i
      WHERE LOWER(i.name) LIKE LOWER(CONCAT('%', :searchPhrase, '%'))
        AND (i.isPrivate = false)
        AND i.isConfigured = true
  """)
  List<ShallowImageProjection> searchShallowImages(@Param("searchPhrase") String searchPhrase,
                                                   Pageable pageable);                                                   
  
    @Query("""
      SELECT i.id AS id, i.name AS name, i.description AS description, i.owner AS owner, i.isPrivate AS isPrivate, i.isConfigured AS isConfigured
      FROM Image i
      WHERE 
        (i.owner = :owner)
        AND (i.isConfigured = true)
      """)
      List<ShallowImageProjection> findByOwner(@Param("owner") String owner);
  

    // @Query("SELECT i FROM Image i WHERE i.owner = :owner")
    // List<Image> findByOwner(@Param("owner") String owner);

    @Query("SELECT i.owner FROM Image i WHERE i.id = :imageId")
    String findOwner(@Param("imageId") String imageId);

    //NOTE: This query doessnt work.
    @Modifying
    @Query(value = "DELETE FROM preference_module_cost_params WHERE image_id = :imageId; " +
                   "DELETE FROM modules WHERE image_id = :imageId; " +
                   "DELETE FROM image_solver_scripts WHERE image_id = :imageId; " +
                   "DELETE FROM image_saved_solutions WHERE image_id = :imageId; " +
                   "DELETE FROM images WHERE image_id = :imageId AND owner = :owner", 
           nativeQuery = true)
    void deleteImageAndRelatedData(@Param("imageId") String imageId, @Param("owner") String owner);
}
