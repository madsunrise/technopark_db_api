package com.github.madsunrise.technopark_db_api.DAO;

import com.github.madsunrise.technopark_db_api.model.Thread;
import com.github.madsunrise.technopark_db_api.response.ThreadDetails;
import com.github.madsunrise.technopark_db_api.response.ThreadDetailsExtended;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Created by ivan on 11.10.16.
 */
public interface ThreadDAO {
    Thread getById (long id);
    ThreadDetails create (String forum, String title, boolean closed, String userEmail, LocalDateTime date,
                          String message, String slug, /*optional*/ boolean deleted);
    Long close (long threadId);
    ThreadDetailsExtended getDetails (long threadId, List<String> related);
    void clear();
    long getAmount();
    long save (Thread thread);
    Long remove (long threadId);
}
