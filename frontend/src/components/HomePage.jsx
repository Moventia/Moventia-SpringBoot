import { useState, useEffect } from 'react';
import { Star, TrendingUp, Clock, Play, X } from 'lucide-react';
import { Card, CardContent, CardHeader, CardTitle } from './ui/card';
import { Button } from './ui/button';
import { Badge } from './ui/badge';
import { Avatar, AvatarFallback, AvatarImage } from './ui/avatar';
import { ImageWithFallback } from './figma/ImageWithFallback';
import { useAppNavigate as useNavigate } from '../hooks/useAppNavigate';
import { MovieCard } from './MovieCard';

const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api';

export function HomePage({ isLoggedIn }) {
  const navigate = useNavigate();
  const [movies, setMovies] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [recentReviews, setRecentReviews] = useState([]);
  const [reviewsLoading, setReviewsLoading] = useState(true);
  const [reviewsError, setReviewsError] = useState(null);
  const [trailerOpen, setTrailerOpen] = useState(false);

  useEffect(() => {
    const fetchTrending = async () => {
      try {
        setLoading(true);
        setError(null);
        const res = await fetch(`${API_URL}/movies/trending?page=1`);
        if (!res.ok) throw new Error('Failed to fetch trending movies');
        const data = await res.json();
        setMovies(data);
      } catch (err) {
        setError(err.message);
      } finally {
        setLoading(false);
      }
    };

    const fetchRecentReviews = async () => {
      try {
        setReviewsLoading(true);
        setReviewsError(null);
        const headers = {};
        const token = localStorage.getItem('token');
        if (token) headers['Authorization'] = `Bearer ${token}`;
        const res = await fetch(`${API_URL}/reviews/recent?limit=5`, { headers });
        if (!res.ok) throw new Error('Failed to fetch recent reviews');
        const data = await res.json();
        setRecentReviews(data);
      } catch (err) {
        setReviewsError(err.message);
      } finally {
        setReviewsLoading(false);
      }
    };

    fetchTrending();
    fetchRecentReviews();
  }, []);

  if (loading) {
    return (
      <div className="min-h-screen bg-background flex items-center justify-center">
        <p className="text-muted-foreground text-lg">Loading trending movies...</p>
      </div>
    );
  }

  if (error) {
    return (
      <div className="min-h-screen bg-background flex items-center justify-center">
        <p className="text-destructive text-lg">Error: {error}</p>
      </div>
    );
  }

  const featuredMovie = movies[0];
  const trendingMovies = movies.slice(0, 6);

  if (!featuredMovie) {
    return (
      <div className="min-h-screen bg-background flex items-center justify-center">
        <p className="text-muted-foreground text-lg">No trending movies available</p>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-background">
      <div className="relative w-full max-h-[80vh] bg-gradient-to-b from-[#1a1510] via-[#0f0d0a] to-background overflow-hidden" style={{ aspectRatio: '2.4/1' }}>
        <div className="absolute inset-0 opacity-20">
          <ImageWithFallback
            src={(featuredMovie.backdropUrl || featuredMovie.posterUrl)?.replace('/w500/', '/original/')}
            alt={featuredMovie.title}
            className="w-full h-full object-cover"
            loading="lazy"
          />
        </div>
        <div className="relative container mx-auto px-4 h-full flex items-center">
          <div className="max-w-2xl text-foreground">
            <Badge className="mb-4 bg-primary text-primary-foreground hover:bg-primary/90">Featured</Badge>
            <h1 className="text-5xl font-bold mb-4">{featuredMovie.title}</h1>
            <div className="flex items-center gap-4 mb-4">
              <div className="flex items-center gap-1">
                <Star className="h-5 w-5 fill-yellow-400 text-yellow-400" />
                <span className="font-semibold">{featuredMovie.tmdbRating}</span>
              </div>
              <span>{featuredMovie.releaseDate?.substring(0, 4)}</span>
              <span>{featuredMovie.runtime}</span>
              <div className="flex gap-1">
                {featuredMovie.genres?.map((g) => (
                  <Badge key={g} variant="outline" className="border-foreground/30 text-foreground/80">
                    {g}
                  </Badge>
                ))}
              </div>
            </div>
            <p className="text-lg mb-6 opacity-90">{featuredMovie.overview}</p>
            <div className="flex gap-3">
              <Button 
                size="lg" 
                onClick={() => navigate(`/movie/${featuredMovie.tmdbId}`)}
                className="bg-primary text-primary-foreground hover:bg-primary/90"
              >
                View Details
              </Button>

              {featuredMovie.trailerUrl && (
                <Button 
                  size="lg" 
                  variant="outline"
                  className="border-foreground/30 text-foreground hover:bg-foreground/10"
                  onClick={() => setTrailerOpen(true)}
                >
                  <Play className="mr-2 h-4 w-4" /> Watch Trailer
                </Button>
              )}

              <Button 
                size="lg" 
                variant="outline"
                onClick={() => navigate(`/movie/${featuredMovie.tmdbId}/review`)}
                className="border-foreground/30 text-foreground hover:bg-foreground/10"
              >
                Write Review
              </Button>
            </div>
          </div>
        </div>
      </div>

      <div className="container mx-auto px-4 py-12">
        {/* Trending Movies */}
        <div className="mb-12">
          <div className="flex items-center justify-between mb-6">
            <div className="flex items-center gap-2">
              <TrendingUp className="h-6 w-6 text-primary" />
              <h2 className="text-3xl font-bold text-foreground">Trending Now</h2>
            </div>
            <Button variant="ghost" onClick={() => navigate('/browse')}>
              View All
            </Button>
          </div>
          <div className="grid gap-4" style={{ gridTemplateColumns: 'repeat(auto-fill, minmax(210px, 1fr))' }}>
            {trendingMovies.map((movie) => (
              <MovieCard key={movie.tmdbId} movie={movie} isLoggedIn={isLoggedIn} />
            ))}
          </div>
        </div>

        {/* Recent Reviews */}
        <div>
          <div className="flex items-center gap-2 mb-6">
            <Clock className="h-6 w-6 text-primary" />
            <h2 className="text-3xl font-bold text-foreground">Recent Reviews</h2>
          </div>

          {reviewsLoading ? (
            <p className="text-muted-foreground text-center py-8">Loading recent reviews...</p>
          ) : reviewsError ? (
            <p className="text-destructive text-center py-8">Error: {reviewsError}</p>
          ) : recentReviews.length === 0 ? (
            <p className="text-muted-foreground text-center py-8">No reviews yet. Be the first to share your thoughts!</p>
          ) : (
            <div className="space-y-4">
              {recentReviews.map((review) => (
                <Card key={review.id} className="hover:shadow-md hover:shadow-primary/5 transition-shadow hover:border-primary/30">
                  <CardContent className="p-6">
                    <div className="flex gap-4">
                      <ImageWithFallback
                        src={review.moviePosterUrl}
                        alt={review.movieTitle}
                        className="w-24 h-36 object-cover rounded"
                      />
                      <div className="flex-1">
                        <div className="flex items-start justify-between mb-2">
                          <div>
                            <div 
                              className="flex items-center gap-2 mb-2 cursor-pointer hover:underline"
                              onClick={() => navigate(`/profile/${review.username}`)}
                            >
                              <Avatar className="h-8 w-8">
                                <AvatarImage src={review.userAvatarUrl} alt={review.username} />
                                <AvatarFallback>{review.username[0]}</AvatarFallback>
                              </Avatar>
                              <span className="font-semibold text-foreground">{review.username}</span>
                            </div>
                            <h3 
                              className="text-lg font-bold cursor-pointer hover:text-primary text-foreground"
                              onClick={() => navigate(`/movie/${review.tmdbId}`)}
                            >
                              {review.movieTitle}
                            </h3>
                          </div>
                          <div className="flex items-center gap-1">
                            {[...Array(5)].map((_, i) => (
                              <Star
                                key={i}
                                className={`h-4 w-4 ${
                                  i < review.rating
                                    ? 'fill-yellow-400 text-yellow-400'
                                    : 'text-muted-foreground/30'
                                }`}
                              />
                            ))}
                          </div>
                        </div>
                        <p className="font-semibold mb-2 text-foreground">{review.title}</p>
                        <p className="text-muted-foreground text-sm mb-3">{review.content}</p>
                        <div className="flex items-center gap-4 text-sm text-muted-foreground">
                          <span>{review.createdAt}</span>
                          <span>{review.likeCount} likes</span>
                        </div>
                      </div>
                    </div>
                  </CardContent>
                </Card>
              ))}
            </div>
          )}

          <div className="text-center mt-6">
            <Button variant="outline" onClick={() => navigate('/feed')}>
              View More Reviews
            </Button>
          </div>
        </div>
      </div>

      {/* Trailer Modal */}
      {trailerOpen && featuredMovie.trailerUrl && (
        <div
          style={{
            position: 'fixed', inset: 0, zIndex: 9999,
            backgroundColor: 'rgba(0,0,0,0.85)',
            display: 'flex', alignItems: 'center', justifyContent: 'center',
          }}
          onClick={() => setTrailerOpen(false)}
        >
          <div
            style={{
              position: 'relative', width: '90%', maxWidth: '960px',
              aspectRatio: '16/9', backgroundColor: '#000', borderRadius: '12px',
              overflow: 'hidden',
            }}
            onClick={(e) => e.stopPropagation()}
          >
            <iframe
              src={featuredMovie.trailerUrl.replace('watch?v=', 'embed/') + '?autoplay=1'}
              style={{ width: '100%', height: '100%', border: 'none' }}
              allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
              allowFullScreen
            />
            <button
              onClick={() => setTrailerOpen(false)}
              style={{
                position: 'absolute', top: '12px', right: '12px',
                background: 'rgba(0,0,0,0.6)', border: 'none', borderRadius: '50%',
                width: '36px', height: '36px', cursor: 'pointer',
                display: 'flex', alignItems: 'center', justifyContent: 'center',
                color: 'white',
              }}
            >
              <X size={20} />
            </button>
          </div>
        </div>
      )}
    </div>
  );
}
