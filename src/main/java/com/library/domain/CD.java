package com.library.domain;

import java.util.Objects;

/**
 * Represents a music CD in the library collection.
 *
 * <p>A {@code CD} is a specific type of {@link Media} that stores the name
 * of the performing artist. It inherits common media properties such as
 * ID, title, and availability status.</p>
 */
public class CD extends Media {

  /** The performing artist of this CD. */
  private final String artist;

  /**
   * Creates a new CD entry with its identifying details.
   *
   * @param id     unique internal ID for this CD
   * @param title  the CD title
   * @param artist the performing artist; must not be null
   */
  public CD(String id, String title, String artist) {
    super(id, title, MediaType.CD);
    this.artist = Objects.requireNonNull(artist, "artist");
  }

  /**
   * Returns the performing artist for this CD.
   *
   * @return the artist name
   */
  public String getArtist() {
    return artist;
  }
}

