package com.github.xhea1.stashtools.model.party;

import org.jspecify.annotations.NullMarked;

/**
 * Record class to represent a single post-entry.
 * This class contains only top-level fields from the `posts` array.
 */
@NullMarked
public record PostRecord(
        int fileId,
        String id,
        String user,
        String service,
        String title,
        String published,
        String substring
) {}

