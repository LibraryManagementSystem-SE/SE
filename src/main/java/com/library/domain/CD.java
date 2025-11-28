package com.library.domain;

import java.util.Objects;

/**
 * CD media representation.
 */
public class CD extends Media {
  private final String artist;

  public CD(String id, String title, String artist) {
    super(id, title, MediaType.CD);
    this.artist = Objects.requireNonNull(artist, "artist");
  }

  public String getArtist() {
    return artist;
  }
}
