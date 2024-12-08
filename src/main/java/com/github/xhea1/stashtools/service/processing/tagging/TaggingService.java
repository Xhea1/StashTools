package com.github.xhea1.stashtools.service.processing.tagging;

import com.github.xhea1.stashtools.model.party.PostRecord;
import com.github.xhea1.stashtools.model.stash.SceneRecord;
import com.github.xhea1.stashtools.service.stash.StashGraphQLService;
import com.google.common.base.Strings;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

/**
 * Service for tagging scenes in Stash.
 */
@NullMarked
public class TaggingService {

    private final StashGraphQLService stashGraphQLService;
    private static final Logger logger = LogManager.getLogger(TaggingService.class);

    public TaggingService(StashGraphQLService stashGraphQLService) {
        this.stashGraphQLService = stashGraphQLService;
    }

    /**
     * Tag the scene by replacing all empty or null fields with the values of the post from party.
     *
     * @param scene scene in stash
     * @param post post returned by Party
     */
    public void tagScene(SceneRecord scene, PostRecord post) {
        // TODO: maybe we should use a better logic to decide what to replace
        String title = null;
        String details = null;
        String date = null;
        if (Strings.isNullOrEmpty( scene.title())) {
            title = post.title();
        }
        if(Strings.isNullOrEmpty(scene.details())) {
            details = post.substring();
        }
        if(Strings.isNullOrEmpty(scene.date())) {
            date = convertDate(post.published());
        }
        if(atLeastOneNonEmpty(title, details, date)) {
          var updatedScene = stashGraphQLService.updateScene(scene.id(), Strings.emptyToNull(title), Strings.emptyToNull(details), Strings.emptyToNull(date));
          if(updatedScene.isPresent()) {
              logger.info("Tagged scene {}", updatedScene);
              // TODO: do something with the updated scene. Maybe compare it with the previous scene and output the diff?
          }
        }
    }

    /**
     * @param input input strings
     * @return true if at lest one input is non-empty
     */
    private boolean atLeastOneNonEmpty(String... input) {
      return  Arrays.stream(input).anyMatch(s->!Strings.isNullOrEmpty(s));
    }

    /**
     * Convert date to YYYY-MM-DD format. This is the only format accepted by Stash.
     *
     * @param input date to convert
     * @return date in YYYY-MM-DD format
     * @see <a href="https://github.com/stashapp/CommunityScripts/issues/412">CommunityScripts#412</a>
     */
    private String convertDate(@Nullable String input) {
        if(input==null) return "";
        // Parse the input string to a LocalDateTime object
        LocalDateTime dateTime = LocalDateTime.parse(input);

        // Format the LocalDateTime to the desired YYYY-MM-DD format
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return dateTime.format(formatter);
    }
}
