package com.github.madsunrise.technopark_db_api.DAO;

import com.github.madsunrise.technopark_db_api.model.Forum;
import com.github.madsunrise.technopark_db_api.response.ForumDetails;

/**
 * Created by ivan on 09.10.16.
 */
public interface ForumDAO {
    Forum getByShortName (String shortName);
    ForumDetails create(String name, String shortName, String user);
    ForumDetails getDetails(String shortName, String related);
    void clear();
    long getAmount();
}
