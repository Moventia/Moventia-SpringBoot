import { useState, useEffect } from 'react';
import { Star, Calendar, Clock, Heart, Share2 } from 'lucide-react';
import { Button } from './ui/button';
import { Card, CardContent } from './ui/card';
import { Badge } from './ui/badge';
import { Avatar, AvatarFallback, AvatarImage } from './ui/avatar';
import { Separator } from './ui/separator';
import { mockReviews } from '../lib/mockData';
import { ImageWithFallback } from './figma/ImageWithFallback';
import { useParams } from 'react-router-dom';
import { useAppNavigate as useNavigate } from '../hooks/useAppNavigate';

const API_URL = 'http://localhost:8080/api';

export function MovieDetail({ isLoggedIn }) {
  const { tmdbId } = useParams();
  const navigate = useNavigate();
  const [movie, setMovie] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // Reviews stay mock for now
  const movieReviews = mockReviews.filter((r) => r.movieId === tmdbId);

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
    fetchMovie();
  }, [tmdbId]);

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
            src={movie.backdropUrl || movie.posterUrl}
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
                <Button size="lg" variant="outline" className="bg-foreground/5 border-foreground/20 text-foreground hover:bg-foreground/10">
                  <Heart className="h-4 w-4 mr-2" />
                  Add to Favorites
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
                  <h2 className="text-2xl font-bold text-foreground">Reviews ({movie.reviewCount})</h2>
                  <Button onClick={() => navigate(`/movie/${movie.tmdbId}/review`)}>
                    Write Review
                  </Button>
                </div>

                {movieReviews.length > 0 ? (
                  <div className="space-y-6">
                    {movieReviews.map((review) => (
                      <div key={review.id}>
                        <div className="flex items-start gap-3 mb-3">
                          <Avatar 
                            className="cursor-pointer"
                            onClick={() => navigate(`/profile/${review.userId}`)}
                          >
                            <AvatarImage src={review.userAvatar} alt={review.username} />
                            <AvatarFallback>{review.username[0]}</AvatarFallback>
                          </Avatar>
                          <div className="flex-1">
                            <div className="flex items-center justify-between mb-1">
                              <span 
                                className="font-semibold cursor-pointer hover:underline text-foreground"
                                onClick={() => navigate(`/profile/${review.userId}`)}
                              >
                                {review.username}
                              </span>
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
                            <p className="text-sm text-muted-foreground mb-2">{review.createdAt}</p>
                            <h3 className="font-semibold mb-2 text-foreground">{review.title}</h3>
                            <p className="text-muted-foreground">{review.content}</p>
                            <div className="flex items-center gap-4 mt-3 text-sm text-muted-foreground">
                              <Button variant="ghost" size="sm" className="h-8 px-2">
                                <Heart className="h-4 w-4 mr-1" />
                                {review.likes}
                              </Button>
                              <span>{review.comments} comments</span>
                            </div>
                          </div>
                        </div>
                        <Separator className="mt-6" />
                      </div>
                    ))}
                  </div>
                ) : (
                  <div className="text-center py-8">
                    <p className="text-muted-foreground mb-4">No reviews yet. Be the first to review!</p>
                    <Button onClick={() => navigate(`/movie/${movie.tmdbId}/review`)}>
                      Write the First Review
                    </Button>
                  </div>
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
