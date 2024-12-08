package com.github.xhea1.stashtools.model.stash;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * Record class for saving information about a scene from Stash.
 */
@NullMarked
public record SceneRecord(
        int id,
        String title,
        String details,
        List<String> urls,
        @Nullable
        String date) {
}
