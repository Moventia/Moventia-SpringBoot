package com.Moventia.movie.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * Maps directly to TMDB's JSON response.
 * @JsonIgnoreProperties(ignoreUnknown = true) means we only capture
 * the fields we care about — TMDB returns ~40 fields per movie.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TmdbMovieResponse {

    private Integer id;
    private String title;
    private String overview;
    private String tagline;

    @JsonProperty("poster_path")
    private String posterPath;

    @JsonProperty("backdrop_path")
    private String backdropPath;

    @JsonProperty("release_date")
    private String releaseDate;

    @JsonProperty("original_language")
    private String originalLanguage;

    @JsonProperty("vote_average")
    private Double voteAverage;

    @JsonProperty("vote_count")
    private Integer voteCount;

    private Integer runtime;

    private List<Genre> genres;

    private Videos videos;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Genre {
        private Integer id;
        private String name;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Videos {
        private List<Video> results;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Video {
        private String key;
        private String site;
        private String type;
    }
}
