package com.github.xhea1.stashtools.service.util;

import com.google.common.io.Files;
import com.google.common.hash.Hashing;
import com.google.common.io.ByteSource;
import org.jspecify.annotations.NullMarked;

import java.io.File;
import java.io.IOException;

/**
 * Utility class for hashing.
 */
@NullMarked
public final class HashUtil {

    private HashUtil(){}
    /**
     * Computes the SHA-256 hash of a file without loading it entirely into memory using Guava.
     *
     * @param file The input file.
     * @return The SHA-256 hash as a hexadecimal string.
     * @throws IOException If an I/O error occurs.
     */
    public static String computeSha256(File file) throws IOException {
        ByteSource byteSource = Files.asByteSource(file);
        return byteSource.hash(Hashing.sha256()).toString(); // Guava's built-in streaming hash computation
    }
}

