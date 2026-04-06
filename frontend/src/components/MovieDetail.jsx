import { useState, useEffect } from 'react';
import { Star, Calendar, Clock, Heart, Share2, Trash2, Eye } from 'lucide-react';
import { Button } from './ui/button';
import { Card, CardContent } from './ui/card';
import { Badge } from './ui/badge';
import { Avatar, AvatarFallback, AvatarImage } from './ui/avatar';
import { Separator } from './ui/separator';
import { ImageWithFallback } from './figma/ImageWithFallback';
import { useParams } from 'react-router-dom';
import { useAppNavigate as useNavigate } from '../hooks/useAppNavigate';

const API_URL = 'http://localhost:8080/api';

const authHeaders = () => ({
  'Content-Type': 'application/json',
  'Authorization': `Bearer ${localStorage.getItem('token')}`
});

export function MovieDetail({ isLoggedIn }) {
  const { tmdbId } = useParams();
  const navigate = useNavigate();
  const [movie, setMovie] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [isFavorited, setIsFavorited] = useState(false);

  // Reviews state
  const [reviews, setReviews] = useState([]);
  const [reviewsLoading, setReviewsLoading] = useState(false);
  const [reviewsError, setReviewsError] = useState(null);
  const [revealedSpoilers, setRevealedSpoilers] = useState(new Set());

  useEffect(() => {
    const fetchMovie = async () => {
      try {
        setLoading(true);
        setError(null);
        const res = await fetch(`${API_URL}/movies/${tmdbId}`);
        if (!res.ok) throw new Error('Failed to fetch movie details');
        const data = await res.json();
        setMovie(data);
      } catch (err) {
        setError(err.message);
      } finally {
        setLoading(false);
      }
    };

    const fetchFavoriteStatus = async () => {
      try {
        const token = localStorage.getItem('token');
        if (!token) return;
        const res = await fetch(`${API_URL}/favorites/status/${tmdbId}`, {
          headers: { Authorization: `Bearer ${token}` }
        });
        if (res.ok) {
          const data = await res.json();
          setIsFavorited(data.favorited);
        }
      } catch {
        // silent fail
      }
    };

    fetchMovie();
    if (isLoggedIn && tmdbId) fetchFavoriteStatus();
  }, [tmdbId, isLoggedIn]);

  // Fetch reviews after movie loads
  useEffect(() => {
    const fetchReviews = async () => {
      try {
        setReviewsLoading(true);
        setReviewsError(null);
        const headers = {};
        const token = localStorage.getItem('token');
        if (token) {
          headers['Authorization'] = `Bearer ${token}`;
        }
        const res = await fetch(`${API_URL}/reviews/movie/${tmdbId}`, { headers });
        if (!res.ok) throw new Error('Failed to fetch reviews');
        const data = await res.json();
        setReviews(data);
      } catch (err) {
        setReviewsError(err.message);
      } finally {
        setReviewsLoading(false);
      }
    };
    if (tmdbId) fetchReviews();
  }, [tmdbId]);

  const handleLikeToggle = async (reviewId, currentlyLiked) => {
    // Optimistic update
    setReviews((prev) =>
      prev.map((r) =>
        r.id === reviewId
          ? {
              ...r,
              isLikedByMe: !currentlyLiked,
              likeCount: currentlyLiked ? r.likeCount - 1 : r.likeCount + 1,
            }
          : r
      )
    );
    try {
      const method = currentlyLiked ? 'DELETE' : 'POST';
      const res = await fetch(`${API_URL}/reviews/${reviewId}/like`, {
        method,
        headers: authHeaders(),
      });
      if (!res.ok) throw new Error('Failed');
    } catch {
      // Revert on failure
      setReviews((prev) =>
        prev.map((r) =>
          r.id === reviewId
            ? {
                ...r,
                isLikedByMe: currentlyLiked,
                likeCount: currentlyLiked ? r.likeCount + 1 : r.likeCount - 1,
              }
            : r
        )
      );
    }
  };

  const handleFavoriteToggle = async () => {
    if (!isLoggedIn) {
      navigate('/login');
      return;
    }

    const currentlyFavorited = isFavorited;
    setIsFavorited(!currentlyFavorited);

    try {
      const method = currentlyFavorited ? 'DELETE' : 'POST';
      const res = await fetch(`${API_URL}/favorites/${tmdbId}`, {
        method,
        headers: authHeaders(),
      });
      if (!res.ok) throw new Error('Failed to toggle favorite');
    } catch {
      setIsFavorited(currentlyFavorited);
    }
  };

  const handleDelete = async (reviewId) => {
    try {
      const res = await fetch(`${API_URL}/reviews/${reviewId}`, {
        method: 'DELETE',
        headers: authHeaders(),
      });
      if (!res.ok) throw new Error('Failed to delete review');
      setReviews((prev) => prev.filter((r) => r.id !== reviewId));
    } catch {
      // silent
    }
  };

  const toggleSpoilerReveal = (reviewId) => {
    setRevealedSpoilers((prev) => {
      const next = new Set(prev);
      if (next.has(reviewId)) next.delete(reviewId);
      else next.add(reviewId);
      return next;
    });
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-background flex items-center justify-center">
        <p className="text-muted-foreground text-lg">Loading movie details...</p>
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

  if (!movie) {
    return <div className="container mx-auto px-4 py-8 text-foreground">Movie not found</div>;
  }

  return (
    <div className="min-h-screen bg-background">
      {/* Hero Section */}
      <div className="relative h-[400px] bg-gradient-to-t from-background to-transparent">
        <div className="absolute inset-0">
          <ImageWithFallback
            src={(movie.backdropUrl || movie.posterUrl)?.replace('/w500/', '/original/')}
            alt={movie.title}
            className="w-full h-full object-cover opacity-40"
          />
        </div>
        <div className="relative container mx-auto px-4 h-full flex items-end pb-8">
          <div className="flex gap-6 items-end">
            <Card className="w-48 overflow-hidden shadow-2xl">
              <ImageWithFallback
                src={movie.posterUrl}
                alt={movie.title}
                className="w-full aspect-[2/3] object-cover"
              />
            </Card>
            <div className="text-foreground pb-4">
              <h1 className="text-5xl font-bold mb-3">{movie.title}</h1>
              <div className="flex items-center gap-4 mb-4">
                <div className="flex items-center gap-1 bg-primary text-primary-foreground px-3 py-1 rounded">
                  <Star className="h-5 w-5 fill-current" />
                  <span className="font-bold text-lg">{movie.tmdbRating}</span>
                </div>
                <div className="flex items-center gap-1">
                  <Calendar className="h-4 w-4" />
                  <span>{movie.releaseDate?.substring(0, 4)}</span>
                </div>
                <div className="flex items-center gap-1">
                  <Clock className="h-4 w-4" />
                  <span>{movie.runtime}</span>
                </div>
              </div>
              <div className="flex gap-2 mb-4">
                {movie.genres?.map((g) => (
                  <Badge key={g} variant="secondary" className="bg-foreground/10 text-foreground border-0">
                    {g}
                  </Badge>
                ))}
              </div>
              <div className="flex gap-2">
                <Button 
                  size="lg"
                  onClick={() => navigate(`/movie/${movie.tmdbId}/review`)}
                >
                  Write Review
                </Button>
                <Button 
                  size="lg" 
                  variant="outline" 
                  onClick={handleFavoriteToggle}
                  className={`border-foreground/20 text-foreground hover:bg-foreground/10 ${isFavorited ? 'bg-primary/20 text-primary border-primary/50 hover:bg-primary/30' : 'bg-foreground/5'}`}
                >
                  <Heart className={`h-4 w-4 mr-2 ${isFavorited ? 'fill-current' : ''}`} />
                  {isFavorited ? 'Favorited' : 'Add to Favorites'}
                </Button>
                <Button size="lg" variant="outline" className="bg-foreground/5 border-foreground/20 text-foreground hover:bg-foreground/10">
                  <Share2 className="h-4 w-4" />
                </Button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div className="container mx-auto px-4 py-8">
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          {/* Main Content */}
          <div className="lg:col-span-2 space-y-6">
            {/* Synopsis */}
            <Card>
              <CardContent className="p-6">
                <h2 className="text-2xl font-bold mb-4 text-foreground">Synopsis</h2>
                <p className="text-muted-foreground leading-relaxed">{movie.overview}</p>
              </CardContent>
            </Card>

            {/* Reviews */}
            <Card>
              <CardContent className="p-6">
                <div className="flex items-center justify-between mb-6">
                  <h2 className="text-2xl font-bold text-foreground">Reviews ({reviews.length})</h2>
                  <Button onClick={() => navigate(`/movie/${movie.tmdbId}/review`)}>
                    Write Review
                  </Button>
                </div>

                {reviewsLoading && (
                  <p className="text-muted-foreground text-center py-4">Loading reviews...</p>
                )}

                {reviewsError && (
                  <p className="text-destructive text-center py-4">Error: {reviewsError}</p>
                )}

                {!reviewsLoading && !reviewsError && reviews.length > 0 ? (
                  <div className="space-y-6">
                    {reviews.map((review) => (
                      <div key={review.id}>
                        <div className="flex items-start gap-3 mb-3">
                          <Avatar 
                            className="cursor-pointer"
                            onClick={() => navigate(`/profile/${review.username}`)}
                          >
                            <AvatarImage src={review.userAvatarUrl} alt={review.username} />
                            <AvatarFallback>{review.username[0]}</AvatarFallback>
                          </Avatar>
                          <div className="flex-1">
                            <div className="flex items-center justify-between mb-1">
                              <div className="flex items-center gap-2">
                                <span 
                                  className="font-semibold cursor-pointer hover:underline text-foreground"
                                  onClick={() => navigate(`/profile/${review.username}`)}
                                >
                                  {review.userFullName || review.username}
                                </span>
                                {review.hasSpoilers && (
                                  <Badge variant="destructive" className="text-xs">Spoiler Warning</Badge>
                                )}
                              </div>
                              <div className="flex items-center gap-2">
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
                                {isLoggedIn && review.isOwnReview && (
                                  <Button
                                    variant="ghost"
                                    size="sm"
                                    className="h-8 px-2 text-destructive hover:text-destructive"
                                    onClick={() => handleDelete(review.id)}
                                  >
                                    <Trash2 className="h-4 w-4" />
                                  </Button>
                                )}
                              </div>
                            </div>
                            <p className="text-sm text-muted-foreground mb-2">{review.createdAt}</p>
                            <h3 className="font-semibold mb-2 text-foreground">{review.title}</h3>
                            {review.hasSpoilers && !revealedSpoilers.has(review.id) ? (
                              <div className="relative">
                                <p className="text-muted-foreground select-none" style={{ filter: 'blur(8px)' }}>{review.content}</p>
                                <div className="absolute inset-0 flex items-center justify-center">
                                  <Button
                                    variant="outline"
                                    size="sm"
                                    onClick={() => toggleSpoilerReveal(review.id)}
                                  >
                                    <Eye className="h-4 w-4 mr-2" />
                                    Reveal Spoiler
                                  </Button>
                                </div>
                              </div>
                            ) : (
                              <p className="text-muted-foreground">{review.content}</p>
                            )}
                            <div className="flex items-center gap-4 mt-3 text-sm text-muted-foreground">
                              {isLoggedIn && (
                                <Button
                                  variant="ghost"
                                  size="sm"
                                  className={`h-8 px-2 ${review.isLikedByMe ? 'text-red-500' : ''}`}
                                  onClick={() => handleLikeToggle(review.id, review.isLikedByMe)}
                                >
                                  <Heart className={`h-4 w-4 mr-1 ${review.isLikedByMe ? 'fill-current' : ''}`} />
                                  {review.likeCount}
                                </Button>
                              )}
                              {!isLoggedIn && (
                                <span className="flex items-center gap-1">
                                  <Heart className="h-4 w-4" />
                                  {review.likeCount}
                                </span>
                              )}
                            </div>
                          </div>
                        </div>
                        <Separator className="mt-6" />
                      </div>
                    ))}
                  </div>
                ) : (
                  !reviewsLoading && !reviewsError && (
                    <div className="text-center py-8">
                      <p className="text-muted-foreground mb-4">No reviews yet. Be the first to review!</p>
                      <Button onClick={() => navigate(`/movie/${movie.tmdbId}/review`)}>
                        Write the First Review
                      </Button>
                    </div>
                  )
                )}
              </CardContent>
            </Card>
          </div>

          {/* Sidebar */}
          <div className="space-y-6">
            {/* Movie Info */}
            <Card>
              <CardContent className="p-6">
                <h3 className="font-bold mb-4 text-foreground">Movie Information</h3>
                <div className="space-y-3">
                  <div>
                    <p className="text-sm text-muted-foreground mb-1">Release Year</p>
                    <p className="font-semibold text-foreground">{movie.releaseDate?.substring(0, 4)}</p>
                  </div>
                  <Separator />
                  <div>
                    <p className="text-sm text-muted-foreground mb-1">Duration</p>
                    <p className="font-semibold text-foreground">{movie.runtime}</p>
                  </div>
                  <Separator />
                  <div>
                    <p className="text-sm text-muted-foreground mb-1">Language</p>
                    <p className="font-semibold text-foreground">{movie.originalLanguage?.toUpperCase()}</p>
                  </div>
                  <Separator />
                  <div>
                    <p className="text-sm text-muted-foreground mb-1">Genres</p>
                    <div className="flex flex-wrap gap-1">
                      {movie.genres?.map((g) => (
                        <Badge key={g} variant="secondary" className="text-xs">{g}</Badge>
                      ))}
                    </div>
                  </div>
                </div>
              </CardContent>
            </Card>

            {/* Stats */}
            <Card>
              <CardContent className="p-6">
                <h3 className="font-bold mb-4 text-foreground">Statistics</h3>
                <div className="space-y-3">
                  <div className="flex justify-between items-center">
                    <span className="text-muted-foreground">TMDB Rating</span>
                    <div className="flex items-center gap-1">
                      <Star className="h-4 w-4 fill-yellow-400 text-yellow-400" />
                      <span className="font-bold text-foreground">{movie.tmdbRating}</span>
                    </div>
                  </div>
                  <div className="flex justify-between items-center">
                    <span className="text-muted-foreground">Average Rating</span>
                    <div className="flex items-center gap-1">
                      <Star className="h-4 w-4 fill-yellow-400 text-yellow-400" />
                      <span className="font-bold text-foreground">{movie.averageRating}</span>
                    </div>
                  </div>
                  <div className="flex justify-between items-center">
                    <span className="text-muted-foreground">Total Reviews</span>
                    <span className="font-bold text-foreground">{movie.reviewCount}</span>
                  </div>
                </div>
              </CardContent>
            </Card>
          </div>
        </div>
      </div>
    </div>
  );
}
