package com.Moventia.movie.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * TMDB wraps movie lists in a paginated envelope:
 * {
 *   "page": 1,
 *   "results": [...],
 *   "total_pages": 10,
 *   "total_results": 200
 * }
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TmdbPageResponse {

    private Integer page;
    private List<TmdbMovieResponse> results;

    @JsonProperty("total_pages")
    private Integer totalPages;

    @JsonProperty("total_results")
    private Integer totalResults;
}
