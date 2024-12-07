package com.github.xhea1.stashtools.model.party;
/**
 * Record class to represent a single post entry.
 * This class contains only top-level fields from the `posts` array.
 */
public record PostRecord(
        int fileId,
        String id,
        String user,
        String service,
        String title,
        String published,
        String substring
) {}

