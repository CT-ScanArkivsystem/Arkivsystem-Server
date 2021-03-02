package no.ntnu.ctscanarkivsystemserver.service;

import no.ntnu.ctscanarkivsystemserver.dao.TagDao;
import no.ntnu.ctscanarkivsystemserver.exception.TagExistsException;
import no.ntnu.ctscanarkivsystemserver.exception.TagNotFoundException;
import no.ntnu.ctscanarkivsystemserver.model.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.Query;
import java.util.List;

@Service
public class TagService {

    private final TagDao tagDao;

    @Autowired
    public TagService(TagDao tagDao) {
        this.tagDao = tagDao;
    }

    /**
     * Checks if there are a tag with tag name in the system.
     * @param tagName name to check if already exist.
     * @return true if no tag with name is found.
     */
    private boolean doesTagExist(String tagName) {
        return tagDao.getTag(tagName) != null;
    }

    /**
     * Create a new tag and add it into the system.
     * @param tagName name of tag to be added.
     * @return new added tag.
     * @throws TagExistsException if tag with name already exists in the system.
     * @throws IllegalArgumentException if tag name is null or empty.
     */
    public Tag createTag(String tagName) throws TagExistsException, IllegalArgumentException {
        if(!doesTagExist(tagName)) {
            return tagDao.insertTag(tagName);
        } else {
            throw new TagExistsException(tagName);
        }
    }

    /**
     * Retrieves a tag from the database.
     * @param tagName tag to be retrieved
     * @return found tag. Null if no tag was found or tag name is empty.
     */
    public Tag getTag(String tagName) throws TagNotFoundException{
        Tag tag = tagDao.getTag(tagName);
        if(tag == null) {
            throw new TagNotFoundException(tagName);
        }
        return tagDao.getTag(tagName);
    }

    /**
     * Retrieves all tags from the database.
     * @return all tags from the database.
     */
    public List<Tag> getAllTags() {
        return tagDao.getAllTags();
    }
}
