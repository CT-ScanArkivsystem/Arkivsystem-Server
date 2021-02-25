package no.ntnu.ctscanarkivsystemserver.dao;

import no.ntnu.ctscanarkivsystemserver.model.Tag;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.transaction.Transactional;
import java.util.List;

@Repository("tagRepo")
public class TagDao {

    @PersistenceContext
    EntityManager em;

    /**
     * Gets a tag from the database by tag name.
     * @param tagName name of tag to get.
     * @return found tag. If no tag was found null.
     */
    public Tag getTag(String tagName) {
        Query query = em.createNamedQuery(Tag.FIND_TAG_BY_NAME);
        if(tagName.trim().isEmpty()) {
            return null;
        }
        query.setParameter("tagName", tagName);
        List<Tag> queryResult = query.getResultList();
        if(queryResult.size() != 1) {
            return null;
        } else {
            return queryResult.get(0);
        }
    }

    /**
     * Insert a tag into the database.
     * @param tagName name of tag to add into the database.
     * @return the added tag.
     * @throws IllegalArgumentException if tag name is null or empty.
     */
    @Transactional
    public Tag insertTag(String tagName) throws IllegalArgumentException {
        if(tagName == null || tagName.trim().isEmpty()) {
            throw new IllegalArgumentException("Tag name cannot be null!");
        }
        Tag tagToAdd = new Tag(tagName);
        em.persist(tagToAdd);
        em.flush();
        return tagToAdd;
    }
}